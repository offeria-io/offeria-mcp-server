# Offeria MCP Server — Architecture

## Overview

The Offeria MCP Server provides a controlled MCP interface to the Offeria Material Knowledge Base.

It allows MCP-compatible AI clients to search, retrieve, compare, translate, and request AI-assisted material suggestions without direct database access.

## Architecture

MCP Client / AI Agent
        |
        | MCP over SSE
        v
offeria-mcp-server
        |
        | REST
        v
material-service
        |
        v
PostgreSQL

## Service Responsibilities

### offeria-mcp-server

- Exposes MCP tools.
- Validates MCP tool input.
- Calls controlled material-service REST APIs.
- Has no direct PostgreSQL access.
- Does not own material domain data.
- Does not approve AI-generated knowledge.

### material-service

- Owns the Material Knowledge Base.
- Owns Material and MaterialAlias persistence.
- Performs normalization and alias resolution.
- Performs similar and semantic material search.
- Manages Iraqi material terminology.
- Generates AI-assisted suggestions.
- Owns the human approval lifecycle.

## MCP Tools

The server exposes five tools:

1. `search_material`
2. `get_material`
3. `find_similar_material`
4. `get_material_translation`
5. `suggest_material_translation`

## Material Resolution

Material resolution follows this principle:

Approved Knowledge > AI Generated Knowledge

Resolution flow:

Input
→ Existing Knowledge
→ Normalized Match
→ Alias / Iraqi Terminology
→ Similar Match
→ Semantic Search
→ AI Suggestion
→ Human Review
→ Approved Knowledge

AI suggestions must not:

- overwrite approved knowledge
- directly modify the database
- bypass domain validation
- approve themselves
- become the authoritative source for pricing

## RFQ Integration

RFQ material descriptions can be resolved through the Material Knowledge Base.

Known and approved terminology is reused whenever possible.

Unknown materials may proceed through similarity, semantic search, and AI suggestion before human approval.

## MCP Request Flow

MCP Client
→ offeria-mcp-server
→ MaterialTools
→ MaterialServiceClient
→ material-service REST API
→ Material Knowledge Base
→ PostgreSQL
→ MCP Response

The MCP server therefore acts as a controlled integration boundary rather than a database gateway.

## Infrastructure

The MCP server supports:

- Java 21
- Spring Boot
- Spring AI MCP Server
- MCP over SSE
- Docker
- Kubernetes
- Spring Boot Actuator
- non-root container execution
- environment-based configuration
- Kubernetes liveness/readiness probes
- Kubernetes ClusterIP service discovery

## Security Principles

- No direct MCP database access.
- Containers run as non-root.
- Linux capabilities are dropped in Kubernetes.
- Configuration is externalized.
- Secrets must not be committed.
- AI-generated knowledge requires human approval.

## Testing

The dedicated MCP server currently has 20 automated tests.

Automated coverage includes MaterialTools, MaterialServiceClient, validation, search integration, translation handling, AI suggestion state, missing materials, invalid UUIDs, and blank queries.

MCP initialization, tool discovery, and real tool invocation over SSE have also been manually verified.

## Final Architecture

The completed flow is:

RFQ
→ Material Knowledge Base
→ Iraqi Terminology / Aliases
→ Similar & Semantic Search
→ AI Suggestions
→ Human Approval
→ MCP Material API
→ Dedicated MCP Server
→ Docker
→ Kubernetes

The architecture keeps material domain ownership inside material-service while exposing controlled AI access through the dedicated MCP server.
