package com.spike.axoneventsourcing;

import org.axonframework.eventhandling.*;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.messaging.MetaData;
import org.axonframework.serialization.SerializedMetaData;
import org.axonframework.serialization.SerializedObject;
import org.axonframework.serialization.SimpleSerializedObject;
import org.axonframework.serialization.xml.XStreamSerializer;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.axonframework.eventhandling.EventUtils.asDomainEventMessage;
import static org.neo4j.driver.v1.Values.parameters;

public class GraphEventStorageEngine implements EventStorageEngine {
    private Driver driver;
    private final XStreamSerializer serializer;



    public GraphEventStorageEngine(Driver neo4jDriver) {
        serializer = XStreamSerializer.builder().build();
        driver = neo4jDriver;
    }

    @Override
    public void appendEvents(List<? extends EventMessage<?>> list) {
        list.stream().forEach(l -> {
            try (Session session = driver.session()) {
                DomainEventMessage<?> event = asDomainEventMessage(l);

                SerializedObject<?> payload = event.serializePayload(serializer, byte[].class);
                SerializedObject<?> metaData = event.serializeMetaData(serializer, byte[].class);


                session.writeTransaction(tx -> {
                    StatementResult result = tx.run(
                        "CREATE (a:event) " +
                            "SET a.eventIdentifier = $eventIdentifier " +
                            "SET a.aggregateIdentifier = $aggregateIdentifier " +
                            "SET a.sequenceNumber = $sequenceNumber " +
                            "SET a.type = $type " +
                            "SET a.timestamp = $timestamp " +
                            "SET a.payloadType = $payloadType " +
                            "SET a.payloadRevision = $payloadRevision " +
                            "SET a.payload = $payload " +
                            "SET a.metadata = $metadata " +
                            "RETURN id(a)",
                        parameters(
                            "eventIdentifier", event.getIdentifier(),
                            "aggregateIdentifier", event.getAggregateIdentifier(),
                            "sequenceNumber", String.valueOf(event.getSequenceNumber()),
                            "type", event.getType(),
                            "timestamp", event.getTimestamp().toString(),
                            "payloadType", payload.getType().getName(),
                            "payloadRevision", payload.getType().getRevision(),
                            "payload", payload.getData(),
                            "metadata", metaData.getData()
                        )
                    );
                    return String.valueOf(result.single().get(0));
                });
            }
        });

    }

    @Override
    public void storeSnapshot(DomainEventMessage<?> domainEventMessage) {

    }

    @Override
    public Stream<? extends TrackedEventMessage<?>> readEvents(TrackingToken trackingToken, boolean b) {
        System.out.println("Tracking Token");
        System.out.println(trackingToken);
        System.out.println(b);
        System.out.println("end tracking token");
        return null;
    }

    @Override
    public DomainEventStream readEvents(String aggregateIdentifier, long firstSequenceNumber) {

        try (Session session = driver.session()) {

            AtomicReference<Long> sequenceNumber = new AtomicReference<>();
            List<Value> values = session.run("match (e:event) return e").single().values();


            Stream<? extends DomainEventMessage<?>> stream = values.stream()
                .map(v -> {
                    Node node = values.get(0).asNode();

                    GenericDomainEventMessage message = new GenericDomainEventMessage<>(
                        node.get("type").asString(),
                        node.get("aggregateIdentifier").asString(),
                        Long.parseLong(node.get("sequenceNumber").asString()),
                        deserializePayload(node),
                        deserializeMetadata(node),
                        node.get("eventIdentifier").asString(),
                        Instant.parse(node.get("timestamp").asString())
                    );
                    return (DomainEventMessage<?>) message;
                })
                .peek(event -> {
                    if (event != null) {
                        sequenceNumber.set(event.getSequenceNumber());
                    }
                });


            return DomainEventStream.of(stream, sequenceNumber::get);
        }

    }

    private MetaData deserializeMetadata(Node node) {
        return (MetaData) serializer.doDeserialize(new SerializedMetaData(node.get("metadata").asByteArray(), byte[].class), serializer.getXStream());
    }

    private Object deserializePayload(Node node) {
        SimpleSerializedObject<byte[]> serializedObject = new SimpleSerializedObject<>(node.get("payload").asByteArray(), byte[].class, node.get("payloadType").asString(), null);
        return serializer.doDeserialize(serializedObject, serializer.getXStream());
    }

    @Override
    public Optional<DomainEventMessage<?>> readSnapshot(String s) {
        return Optional.empty();
    }
}
