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
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class CyodaCalculationMemberClient implements DisposableBean, InitializingBean {

    private ManagedChannel managedChannel;
    private CloudEventsServiceGrpc.CloudEventsServiceStub clientStub;
    private StreamObserver<CloudEvent> streamingObserver;
    private EventFormat eventFormat;
    private final ObjectMapper objectMapper;
    private final CyodaCalculationMemberProcessor processor;

    public CyodaCalculationMemberClient(ObjectMapper objectMapper, CyodaCalculationMemberProcessor processor) {
        this.objectMapper = objectMapper;
        this.processor = processor;
    }

    @Override
    public void afterPropertiesSet() {
        managedChannel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
        clientStub = CloudEventsServiceGrpc.newStub(managedChannel)
                .withWaitForReady();
        eventFormat = EventFormatProvider.getInstance().resolveFormat(ProtobufFormat.PROTO_CONTENT_TYPE);
        if (eventFormat == null) {
            throw new NullPointerException("Unable to resolve protobuf event format");
        }
    }

    @Override
    public void destroy() {
        if (streamingObserver != null) {
            streamingObserver.onCompleted();
        }
        if (managedChannel != null) {
            try {
                managedChannel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) throws InvalidProtocolBufferException {
        streamingObserver = clientStub.startStreaming(new StreamObserver<CloudEvent>() {

            @Override
            public void onNext(CloudEvent value) {
                System.out.println(">> Got EVENT:\n" + value);
                switch (value.getType()) {
                    case "EntityProcessorCalculationRequest":
                        try {
                            EntityProcessorCalculationRequest request = objectMapper.readValue(value.getTextData(), EntityProcessorCalculationRequest.class);
                            BaseEvent response = processor.calculate(request);
                            sendEvent(response);
                        } catch (IOException e) {
                            System.err.println("Error processing EntityProcessorCalculationRequest: " + e.getMessage() + value);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    default:
                        System.out.println("Skipping message as no processing required");
                        break;
                }
            }

            @Override
            public void onError(Throwable t) {
                System.err.println(">> Got ERROR from remote backend");
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println(">> Got COMPLETE from remote backend");
            }
        });

        CalculationMemberJoinEvent event = new CalculationMemberJoinEvent();
        event.setOwner("PLAY");
        event.setTags(List.of("accounting"));
        sendEvent(event);
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

        var observer = streamingObserver;

        if (observer == null) {
            throw new IllegalStateException("Stream observer is not initialized");
        }

        System.out.println("<< Sending EVENT:\n" + event);

        // stream observer is not thread safe, for production usage this should be managed by some pooling for such cases
        synchronized (observer) {
            observer.onNext(cloudEvent);
        }
    }

}
