package com.mcp.mcpclient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.mcp.mcpclient.config.MCPClientConfig;
import com.mcp.mcpclient.model.MCPResponse;
import com.mcp.mcpclient.model.Tool;
import com.mcp.mcpclient.model.ToolExecutionRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Component
@RequiredArgsConstructor
public class MCPClient {

    private final RestTemplate restTemplate;
    private final MCPClientConfig config;

    public List<Tool> getAvailableTools() {
        String url = config.getBaseUrl() + "/tools";

        ResponseEntity<MCPResponse<List<Tool>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                new ParameterizedTypeReference<MCPResponse<List<Tool>>>() {
                }
        );

        MCPResponse<List<Tool>> mcpResponse = response.getBody();
        if (mcpResponse == null || !mcpResponse.isSuccess() || mcpResponse.getData() == null) {
            throw new RuntimeException("Error getting tools: " +
                    (mcpResponse != null && mcpResponse.getError() != null ?
                            mcpResponse.getError().getMessage() : "Empty response"));
        }

        return mcpResponse.getData();
    }

    public Tool getToolInfo(String toolName) {
        String url = config.getBaseUrl() + "/tools/" + toolName;

        ResponseEntity<MCPResponse<Tool>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(),
                new ParameterizedTypeReference<MCPResponse<Tool>>() {
                }
        );

        MCPResponse<Tool> mcpResponse = response.getBody();
        if (mcpResponse == null || !mcpResponse.isSuccess() || mcpResponse.getData() == null) {
            throw new RuntimeException("Error getting tool information: " +
                    (mcpResponse != null && mcpResponse.getError() != null ?
                            mcpResponse.getError().getMessage() : "Empty response"));
        }

        return mcpResponse.getData();
    }

    public <T> T executeTool(String toolName, Map<String, Object> parameters, Class<T> responseType) {
        String url = config.getBaseUrl() + "/execute";

        ToolExecutionRequest request = ToolExecutionRequest.builder()
                .toolName(toolName)
                .parameters(parameters)
                .build();

        log.debug("Executing tool: {} with parameters: {}", toolName, parameters);

        ResponseEntity<MCPResponse<T>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(request, createHeaders()),
                new ParameterizedTypeReference<MCPResponse<T>>() {
                }
        );

        MCPResponse<T> mcpResponse = response.getBody();
        if (mcpResponse == null || !mcpResponse.isSuccess() || mcpResponse.getData() == null) {
            throw new RuntimeException("Error executing tool: " +
                    (mcpResponse != null && mcpResponse.getError() != null ?
                            mcpResponse.getError().getMessage() : "Empty response"));
        }

        return mcpResponse.getData();
    }

    public boolean isHealthy() {
        try {
            String url = config.getBaseUrl() + "/health";

            ResponseEntity<MCPResponse<Map<String, String>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createHttpEntity(),
                    new ParameterizedTypeReference<MCPResponse<Map<String, String>>>() {
                    }
            );

            MCPResponse<Map<String, String>> mcpResponse = response.getBody();
            return mcpResponse != null && mcpResponse.isSuccess() &&
                    mcpResponse.getData() != null && "ok".equals(mcpResponse.getData().get("status"));
        } catch (Exception e) {
            log.error("Error checking MCP gateway status", e);
            return false;
        }
    }

    private <T> HttpEntity<T> createHttpEntity() {
        return new HttpEntity<>(createHeaders());
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/json");

        Optional.ofNullable(config.getApiKey())
                .ifPresent(key -> headers.add("X-API-Key", key));

        return headers;
    }
} 