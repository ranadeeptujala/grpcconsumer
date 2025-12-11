package com.example.grpcconsumer.service;

import com.example.grpc.proto.GreetingServiceGrpc;
import com.example.grpc.proto.HelloRequest;
import com.example.grpc.proto.HelloResponse;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class GreetingGrpcClientService {

    private static final Logger log = LoggerFactory.getLogger(GreetingGrpcClientService.class);

    @GrpcClient("greeting-service")
    private GreetingServiceGrpc.GreetingServiceBlockingStub blockingStub;

    @GrpcClient("greeting-service")
    private GreetingServiceGrpc.GreetingServiceStub asyncStub;

    /**
     * Unary RPC - Single request/response
     */
    public HelloResponse sayHello(String name, String language) {
        log.info("Calling sayHello on thread: {}", Thread.currentThread());
        
        HelloRequest request = HelloRequest.newBuilder()
                .setName(name)
                .setLanguage(language != null ? language : "en")
                .build();

        HelloResponse response = blockingStub.sayHello(request);
        
        log.info("Received response: {}", response.getMessage());
        return response;
    }

    /**
     * Server Streaming RPC - Single request, stream of responses
     */
    public List<HelloResponse> sayHelloServerStream(String name, String language) throws InterruptedException {
        log.info("Calling sayHelloServerStream on thread: {}", Thread.currentThread());
        
        HelloRequest request = HelloRequest.newBuilder()
                .setName(name)
                .setLanguage(language != null ? language : "en")
                .build();

        List<HelloResponse> responses = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        asyncStub.sayHelloServerStream(request, new StreamObserver<>() {
            @Override
            public void onNext(HelloResponse response) {
                log.info("Received streaming response: {}", response.getMessage());
                responses.add(response);
            }

            @Override
            public void onError(Throwable t) {
                log.error("Error in server stream", t);
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                log.info("Server stream completed");
                latch.countDown();
            }
        });

        latch.await(30, TimeUnit.SECONDS);
        return responses;
    }

    /**
     * Client Streaming RPC - Stream of requests, single response
     */
    public HelloResponse sayHelloClientStream(List<String> names, String language) throws InterruptedException {
        log.info("Calling sayHelloClientStream on thread: {}", Thread.currentThread());
        
        final HelloResponse[] responseHolder = new HelloResponse[1];
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<HelloRequest> requestObserver = asyncStub.sayHelloClientStream(
                new StreamObserver<>() {
                    @Override
                    public void onNext(HelloResponse response) {
                        log.info("Received client stream response: {}", response.getMessage());
                        responseHolder[0] = response;
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("Error in client stream", t);
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        log.info("Client stream completed");
                        latch.countDown();
                    }
                });

        // Send all names as requests
        for (String name : names) {
            HelloRequest request = HelloRequest.newBuilder()
                    .setName(name)
                    .setLanguage(language != null ? language : "en")
                    .build();
            requestObserver.onNext(request);
            log.info("Sent request for name: {}", name);
        }

        requestObserver.onCompleted();
        latch.await(30, TimeUnit.SECONDS);
        
        return responseHolder[0];
    }

    /**
     * Bidirectional Streaming RPC - Stream both ways
     */
    public List<HelloResponse> sayHelloBidirectional(List<String> names, String language) throws InterruptedException {
        log.info("Calling sayHelloBidirectional on thread: {}", Thread.currentThread());
        
        List<HelloResponse> responses = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<HelloRequest> requestObserver = asyncStub.sayHelloBidirectional(
                new StreamObserver<>() {
                    @Override
                    public void onNext(HelloResponse response) {
                        log.info("Received bidirectional response: {}", response.getMessage());
                        responses.add(response);
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("Error in bidirectional stream", t);
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        log.info("Bidirectional stream completed");
                        latch.countDown();
                    }
                });

        // Send all names as requests
        for (String name : names) {
            HelloRequest request = HelloRequest.newBuilder()
                    .setName(name)
                    .setLanguage(language != null ? language : "en")
                    .build();
            requestObserver.onNext(request);
            log.info("Sent bidirectional request for name: {}", name);
        }

        requestObserver.onCompleted();
        latch.await(30, TimeUnit.SECONDS);
        
        return responses;
    }
}

