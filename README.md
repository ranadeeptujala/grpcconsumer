# gRPC Consumer with Spring Boot 3 & Virtual Threads

A Spring Boot application using Java 21 virtual threads to consume gRPC services.

## Features

- **Java 21** with Virtual Threads enabled
- **Spring Boot 3.2** with native virtual thread support
- **gRPC Client** connecting to `grpc.ranadeepdev.com`
- All 4 gRPC communication patterns supported:
  - Unary RPC
  - Server Streaming
  - Client Streaming
  - Bidirectional Streaming

## Prerequisites

- Java 21+
- Maven 3.8+

## Build & Run

```bash
# Build the project (generates gRPC stubs from proto file)
./mvnw clean compile

# Run the application
./mvnw spring-boot:run
```

## API Endpoints

### 1. Unary RPC - Single request/response
```bash
curl "http://localhost:8080/api/greeting/hello?name=John&language=en"
```

### 2. Server Streaming RPC
```bash
curl "http://localhost:8080/api/greeting/hello/stream?name=John&language=en"
```

### 3. Client Streaming RPC
```bash
curl -X POST "http://localhost:8080/api/greeting/hello/client-stream?language=en" \
  -H "Content-Type: application/json" \
  -d '["Alice", "Bob", "Charlie"]'
```

### 4. Bidirectional Streaming RPC
```bash
curl -X POST "http://localhost:8080/api/greeting/hello/bidirectional?language=en" \
  -H "Content-Type: application/json" \
  -d '["Alice", "Bob", "Charlie"]'
```

### Thread Info (verify virtual threads)
```bash
curl "http://localhost:8080/api/greeting/thread-info"
```

## Virtual Threads

Virtual threads are enabled via `application.yml`:

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

Each request handler runs on a virtual thread, which you can verify using the `/api/greeting/thread-info` endpoint.

## Project Structure

```
src/
├── main/
│   ├── java/com/example/grpcconsumer/
│   │   ├── GrpcConsumerApplication.java    # Main application
│   │   ├── controller/
│   │   │   └── GreetingController.java     # REST endpoints
│   │   ├── service/
│   │   │   └── GreetingGrpcClientService.java  # gRPC client
│   │   └── dto/
│   │       └── GreetingResponse.java       # Response DTO
│   ├── proto/
│   │   └── greeting.proto                  # gRPC service definition
│   └── resources/
│       └── application.yml                 # Configuration
└── test/
```

