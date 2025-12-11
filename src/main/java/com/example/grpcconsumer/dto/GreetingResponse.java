package com.example.grpcconsumer.dto;

import com.example.grpc.proto.HelloResponse;

/**
 * DTO for REST API responses
 */
public record GreetingResponse(
        String message,
        String timestamp,
        String threadInfo,
        String clientThreadInfo
) {
    /**
     * Create a GreetingResponse from a gRPC HelloResponse
     */
    public static GreetingResponse from(HelloResponse grpcResponse) {
        Thread currentThread = Thread.currentThread();
        String clientThreadInfo = String.format(
                "Client Thread: %s (Virtual: %s, ID: %d)",
                currentThread.getName(),
                currentThread.isVirtual(),
                currentThread.threadId()
        );
        
        return new GreetingResponse(
                grpcResponse.getMessage(),
                grpcResponse.getTimestamp(),
                grpcResponse.getThreadInfo(),
                clientThreadInfo
        );
    }
}

