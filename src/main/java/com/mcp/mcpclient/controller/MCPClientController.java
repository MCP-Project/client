package com.mcp.mcpclient.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mcp.mcpclient.MCPClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/client/api")
@RequiredArgsConstructor
public class MCPClientController {
    
    private final MCPClient mcpClient;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        boolean isHealthy = mcpClient.isHealthy();
        
        Map<String, String> response = new HashMap<>();
        response.put("status", isHealthy ? "OK" : "Error");
        response.put("message", isHealthy ? "MCP Gateway is responding" : "MCP Gateway is not responding");
        
        return ResponseEntity.ok(response);
    }
} 