package com.example.accounting_demo.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.core.format.EventFormat;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.protobuf.ProtobufFormat;
import io.cloudevents.v1.proto.CloudEvent;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.cyoda.cloud.api.event.*;
import org.cyoda.cloud.api.grpc.CloudEventsServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class CyodaCalculationMemberClient implements DisposableBean, InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(CyodaCalculationMemberClient.class);

    private ManagedChannel managedChannel;
    private CloudEventsServiceGrpc.CloudEventsServiceStub cloudEventsServiceStub;
    private StreamObserver<CloudEvent> cloudEventStreamObserver;
    private EventFormat eventFormat;
    private final ObjectMapper objectMapper;
    private final CyodaCalculationMemberProcessor calculationMemberProcessor;

    @Value("${grpc.server.host}")
    private String grpcServerAddress;

    @Value("${grpc.server.port}")
    private int grpcServerPort;

    @Value("${grpc.server.tls}")
    private boolean tls;

    public CyodaCalculationMemberClient(ObjectMapper objectMapper, CyodaCalculationMemberProcessor processor) {
        this.objectMapper = objectMapper;
        this.calculationMemberProcessor = processor;
    }

    //./grpcurl -plaintext kube-cyoda-develop-unified-grpc.cyoda.org:443 list
    @Override
    public void afterPropertiesSet() {
        try {
            managedChannel = null;
            if (tls) {
                managedChannel = ManagedChannelBuilder.forAddress(grpcServerAddress, grpcServerPort)
                        .build();
            } else {
                managedChannel = ManagedChannelBuilder.forAddress(grpcServerAddress, grpcServerPort)
                        .usePlaintext()
                        .build();
            }
            cloudEventsServiceStub = CloudEventsServiceGrpc.newStub(managedChannel)
                    .withWaitForReady();
            eventFormat = EventFormatProvider.getInstance().resolveFormat(ProtobufFormat.PROTO_CONTENT_TYPE);
            if (eventFormat == null) {
                throw new IllegalStateException("Unable to resolve protobuf event format");
            }
            logger.info("gRPC client initialized successfully with server address: {} and port: {}", grpcServerAddress, grpcServerPort);
        } catch (Exception e) {
            logger.error("Failed to initialize gRPC client", e);
            throw new RuntimeException("Failed to initialize gRPC client", e);
        }
    }

    @Override
    public void destroy() {
        if (cloudEventStreamObserver != null) {
            cloudEventStreamObserver.onCompleted();
        }
        if (managedChannel != null) {
            try {
                managedChannel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
                if (!managedChannel.isTerminated()) {
                    logger.warn("Forcing gRPC channel shutdown");
                    managedChannel.shutdownNow();
                }
                logger.info("gRPC channel shut down successfully");
            } catch (InterruptedException e) {
                logger.error("Interrupted while shutting down gRPC channel", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        try {
            cloudEventStreamObserver = cloudEventsServiceStub.startStreaming(new StreamObserver<>() {
                @Override
                public void onNext(CloudEvent cloudEvent) {
//                    logger.info(">> Got EVENT:\n" + cloudEvent);
                    handleCloudEvent(cloudEvent);
                }

                @Override
                public void onError(Throwable t) {
                    logger.error("Error received from remote backend", t);
                }

                @Override
                public void onCompleted() {
                    logger.info("Stream completed by remote backend");
                }
            });
            logger.info("Started streaming events from gRPC server");
            CalculationMemberJoinEvent event = new CalculationMemberJoinEvent();
            event.setOwner("PLAY");
            event.setTags(List.of("accounting"));
            sendEvent(event);
        } catch (Exception e) {
            logger.error("Failed to start streaming events from gRPC server", e);
        }
    }

    private void handleCloudEvent(CloudEvent cloudEvent) {
        try {
//            logger.info("<< Received event: \n{}", cloudEvent.getTextData());
            Object json = objectMapper.readValue(cloudEvent.getTextData(), Object.class);
            logger.info("<< Received event: \n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
            switch (cloudEvent.getType()) {
                case "EntityProcessorCalculationRequest":
                    EntityProcessorCalculationRequest request = objectMapper.readValue(cloudEvent.getTextData(), EntityProcessorCalculationRequest.class);
                    logger.info("Processing EntityProcessorCalculationRequest: {}", request.getProcessorName());
                    BaseEvent response = calculationMemberProcessor.calculate(request);
                    sendEvent(response);
                    break;
                case "EventAckResponse":
//                    logger.info("Received EventAckResponse";
                    break;

                default:
                    logger.info("Unhandled event type: {}", cloudEvent.getType());
            }
        } catch (IOException e) {
            logger.error("Error processing event: {}", cloudEvent, e);
        } catch (InterruptedException e) {
            logger.error("Interrupted while processing event: {}", cloudEvent, e);
            Thread.currentThread().interrupt();
        }
    }

    public void sendEvent(BaseEvent event) throws InvalidProtocolBufferException {
        CloudEvent cloudEvent = CloudEvent.parseFrom(
                eventFormat.serialize(
                        CloudEventBuilder.v1()
                                .withType(event.getClass().getSimpleName())
                                .withSource(URI.create("AccountingDemo"))
                                .withId(UUID.randomUUID().toString())
                                .withData(PojoCloudEventData.wrap(event, eventData -> {
                                    try {
                                        return objectMapper.writeValueAsBytes(eventData);
                                    } catch (JsonProcessingException e) {
                                        throw new RuntimeException("Error serializing event data", e);
                                    }
                                }))
                                .build()
                )
        );

        var observer = cloudEventStreamObserver;

        if (observer == null) {
            throw new IllegalStateException("Stream observer is not initialized");
        }

//        logger.info("<< Sending event:\n" + event);
        logger.info("<< Sending event: success: {}\n", event.getSuccess());

        // stream observer is not thread safe, for production usage this should be managed by some pooling for such cases
        synchronized (observer) {
            observer.onNext(cloudEvent);
        }
    }
}