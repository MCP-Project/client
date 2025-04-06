package com.mcp.mcpclient.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcp.mcpclient.MCPClient;
import com.mcp.mcpclient.model.Tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AIProxyController {
    
    private final MCPClient mcpClient;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${openai.api.key:}")
    private String openAiApiKey;
    
    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> processNaturalLanguageQuery(
            @RequestBody Map<String, String> request) {
        
        String userQuery = request.get("query");
        log.info("Received query: {}", userQuery);
        
        try {
            List<Tool> availableTools = mcpClient.getAvailableTools();
            
            Map<String, Object> llmInterpretation = interpretUserQuery(userQuery, availableTools);
            
            String toolName = (String) llmInterpretation.get("tool");
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) llmInterpretation.get("parameters");
            
            log.info("Chosen tool: {}", toolName);
            log.info("Extracted parameters: {}", parameters);
            
            Object result = mcpClient.executeTool(toolName, parameters, Object.class);
            
            Map<String, Object> response = new HashMap<>();
            response.put("query", userQuery);
            response.put("tool", toolName);
            response.put("parameters", parameters);
            response.put("result", result);
            response.put("explanation", llmInterpretation.get("explanation"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing query", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Could not process your query");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    private Map<String, Object> interpretUserQuery(String query, List<Tool> availableTools) throws JsonProcessingException {
        StringBuilder toolsInfoBuilder = new StringBuilder();
        toolsInfoBuilder.append("Available tools:\n\n");
        
        for (Tool tool : availableTools) {
            toolsInfoBuilder.append("Name: ").append(tool.getName()).append("\n");
            toolsInfoBuilder.append("Description: ").append(tool.getDescription()).append("\n");
            toolsInfoBuilder.append("Parameters: \n");
            
            if (tool.getParameters() != null) {
                tool.getParameters().forEach(param -> {
                    toolsInfoBuilder.append("  - ").append(param.getName())
                            .append(" (").append(param.getType()).append("): ")
                            .append(param.getDescription());
                    
                    if (param.isRequired()) {
                        toolsInfoBuilder.append(" [REQUIRED]");
                    }
                    toolsInfoBuilder.append("\n");
                });
            }
            
            toolsInfoBuilder.append("\n");
        }
        
        String systemPrompt = "You are an assistant specialized in interpreting queries and mapping to the correct tool. " +
                "Analyze the user's query and determine which tool to use and what parameters to send. " +
                "Reply ONLY in JSON format with the fields: \n" +
                "- tool: name of the tool to use\n" +
                "- parameters: object with the necessary parameters\n" +
                "- explanation: brief explanation of what you understood\n\n" +
                toolsInfoBuilder.toString();
        
        if (openAiApiKey == null || openAiApiKey.isEmpty() || openAiApiKey.equals("your-api-key-here")) {
            log.warn("API Key not configured. Using simulated interpretation");
            return simulateInterpretation(query, availableTools);
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openAiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", query)
        ));
        requestBody.put("temperature", 0.2);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                entity,
                String.class
        );
        
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        String content = jsonResponse.path("choices").get(0).path("message").path("content").asText();
        
        try {
            content = extractJsonFromContent(content);
            return objectMapper.readValue(content, Map.class);
        } catch (Exception e) {
            log.error("Error parsing LLM response", e);
            throw new RuntimeException("Could not interpret the LLM response");
        }
    }
    
    private String extractJsonFromContent(String content) {
        int startBrace = content.indexOf('{');
        int endBrace = content.lastIndexOf('}');
        
        if (startBrace >= 0 && endBrace > startBrace) {
            return content.substring(startBrace, endBrace + 1);
        }
        
        throw new RuntimeException("Could not find valid JSON in the response");
    }
    
    private Map<String, Object> simulateInterpretation(String query, List<Tool> availableTools) {
        Map<String, Object> result = new HashMap<>();
        result.put("tool", availableTools.isEmpty() ? "unknown" : availableTools.get(0).getName());
        result.put("parameters", new HashMap<>());
        result.put("explanation", "Simulated interpretation of query: " + query);
        
        return result;
    }
} 