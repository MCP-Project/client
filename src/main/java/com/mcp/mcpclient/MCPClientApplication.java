package com.mcp.mcpclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class MCPClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(MCPClientApplication.class, args);
    }
} 