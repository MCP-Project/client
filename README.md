# Simplified MCP Java Client

A simplified Java client for interacting with the Model Context Protocol (MCP) Gateway using natural language.

## Features

- Natural language processing to access MCP Gateway tools
- Direct REST communication with the gateway
- Query interpretation and mapping to appropriate tools
- JSON-formatted responses

## Architecture

The client acts as a bridge between applications and the MCP Gateway, providing:

1. A natural language processor that interprets queries and maps to gateway tools
2. REST communication with the gateway for tool execution

## How to Use

### Running the Client

```bash
mvn clean package
java -jar target/mcp-java-client-0.0.1-SNAPSHOT.jar
```

The client will start on port 8081 by default.

### Available Endpoints

#### Health Check
- GET `/client/api/health` - Check gateway health

#### Natural Language Processing
- POST `/ai/ask` - Process natural language queries and map to appropriate tools

### Example Request

##### Natural Language Processing
```bash
curl -X POST http://localhost:8081/ai/ask \
  -H "Content-Type: application/json" \
  -d '{"query": "What is the sum of 25 and 17?"}'
```

## Configuration

Configuration options in `application.properties`:

```properties
# MCP Client Configuration
mcp.client.base-url=http://localhost:8080/mcp/api
mcp.client.timeout=30000

# Server Configuration
server.port=8081

# OpenAI API (replace with your key or leave empty to use simulation)
openai.api.key=your-api-key-here
``` 