package com.mcp.mcpclient.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.mcp.mcpclient.MCPClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculatorService {

    private final MCPClient mcpClient;
    
    private static final String TOOL_NAME = "calculator";

    public double add(double a, double b) {
        return executeOperation("add", a, b);
    }

    public double subtract(double a, double b) {
        return executeOperation("subtract", a, b);
    }

    public double multiply(double a, double b) {
        return executeOperation("multiply", a, b);
    }

    public double divide(double a, double b) {
        if (b == 0) {
            throw new ArithmeticException("Division by zero is not allowed");
        }
        return executeOperation("divide", a, b);
    }

    private double executeOperation(String operation, double a, double b) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("operation", operation);
            parameters.put("a", a);
            parameters.put("b", b);
            
            Map<String, Object> result = mcpClient.executeTool(TOOL_NAME, parameters, Map.class);
            
            if (result != null && result.containsKey("result")) {
                Object resultValue = result.get("result");
                if (resultValue instanceof Number) {
                    return ((Number) resultValue).doubleValue();
                }
                throw new RuntimeException("Invalid result type: " + resultValue.getClass().getName());
            }
            
            throw new RuntimeException("Result does not contain 'result' field");
        } catch (Exception e) {
            log.error("Error executing operation {} with a={} and b={}", operation, a, b, e);
            throw new RuntimeException("Error executing operation: " + e.getMessage(), e);
        }
    }
} 