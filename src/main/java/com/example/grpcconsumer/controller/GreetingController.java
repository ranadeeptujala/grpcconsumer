package com.example.grpcconsumer.controller;

import com.example.grpcconsumer.dto.GreetingResponse;
import com.example.grpcconsumer.service.GreetingGrpcClientService;
import com.example.grpc.proto.HelloResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller that uses Virtual Threads (enabled via spring.threads.virtual.enabled=true)
 * to handle requests and communicate with gRPC service.
 */
@RestController
@RequestMapping("/api/greeting")
public class GreetingController {

    private static final Logger log = LoggerFactory.getLogger(GreetingController.class);

    private final GreetingGrpcClientService grpcClientService;

    public GreetingController(GreetingGrpcClientService grpcClientService) {
        this.grpcClientService = grpcClientService;
    }

    /**
     * Unary RPC endpoint - Single request/response
     * GET /api/greeting/hello?name=John&language=en
     */
    @GetMapping("/hello")
    public ResponseEntity<GreetingResponse> sayHello(
            @RequestParam(defaultValue = "World") String name,
            @RequestParam(defaultValue = "en") String language) {
        
        logThreadInfo("sayHello");
        
        HelloResponse grpcResponse = grpcClientService.sayHello(name, language);
        
        return ResponseEntity.ok(GreetingResponse.from(grpcResponse));
    }

    /**
     * Server Streaming RPC endpoint - Single request, stream of responses
     * GET /api/greeting/hello/stream?name=John&language=en
     */
    @GetMapping("/hello/stream")
    public ResponseEntity<List<GreetingResponse>> sayHelloServerStream(
            @RequestParam(defaultValue = "World") String name,
            @RequestParam(defaultValue = "en") String language) throws InterruptedException {
        
        logThreadInfo("sayHelloServerStream");
        
        List<HelloResponse> grpcResponses = grpcClientService.sayHelloServerStream(name, language);
        
        List<GreetingResponse> responses = grpcResponses.stream()
                .map(GreetingResponse::from)
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Client Streaming RPC endpoint - Stream of requests, single response
     * POST /api/greeting/hello/client-stream?language=en
     * Body: ["Alice", "Bob", "Charlie"]
     */
    @PostMapping("/hello/client-stream")
    public ResponseEntity<GreetingResponse> sayHelloClientStream(
            @RequestBody List<String> names,
            @RequestParam(defaultValue = "en") String language) throws InterruptedException {
        
        logThreadInfo("sayHelloClientStream");
        
        HelloResponse grpcResponse = grpcClientService.sayHelloClientStream(names, language);
        
        if (grpcResponse == null) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(GreetingResponse.from(grpcResponse));
    }

    /**
     * Bidirectional Streaming RPC endpoint - Stream both ways
     * POST /api/greeting/hello/bidirectional?language=en
     * Body: ["Alice", "Bob", "Charlie"]
     */
    @PostMapping("/hello/bidirectional")
    public ResponseEntity<List<GreetingResponse>> sayHelloBidirectional(
            @RequestBody List<String> names,
            @RequestParam(defaultValue = "en") String language) throws InterruptedException {
        
        logThreadInfo("sayHelloBidirectional");
        
        List<HelloResponse> grpcResponses = grpcClientService.sayHelloBidirectional(names, language);
        
        List<GreetingResponse> responses = grpcResponses.stream()
                .map(GreetingResponse::from)
                .toList();
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Health check endpoint showing virtual thread info
     * GET /api/greeting/thread-info
     */
    @GetMapping("/thread-info")
    public ResponseEntity<ThreadInfoResponse> getThreadInfo() {
        Thread currentThread = Thread.currentThread();
        
        return ResponseEntity.ok(new ThreadInfoResponse(
                currentThread.getName(),
                currentThread.isVirtual(),
                currentThread.threadId(),
                Thread.currentThread().toString()
        ));
    }

    private void logThreadInfo(String methodName) {
        Thread currentThread = Thread.currentThread();
        log.info("Method: {} | Thread: {} | Virtual: {} | ID: {}",
                methodName,
                currentThread.getName(),
                currentThread.isVirtual(),
                currentThread.threadId());
    }

    public record ThreadInfoResponse(
            String threadName,
            boolean isVirtual,
            long threadId,
            String threadDetails
    ) {}
}

