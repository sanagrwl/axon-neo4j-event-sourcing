package com.spike.axoneventsourcing;

import org.axonframework.eventhandling.*;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.messaging.MetaData;
import org.axonframework.serialization.SerializedMetaData;
import org.axonframework.serialization.SerializedObject;
import org.axonframework.serialization.SimpleSerializedObject;
import org.axonframework.serialization.xml.XStreamSerializer;
import org.neo4j.driver.v1.*;

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
                    "match (e:event) " +
                            "where e.aggregateIdentifier = $aggregateIdentifier " +
                                "and e.branch in [$branch, 'master'] " +
                            "with e.branch as branch, e order by e.sequenceNumber desc " +
                            "with filter(x in collect(e) where x.branch = 'test') as branchNodes, " +
                                 "filter(x in collect(e) where x.branch = 'master') as masterNodes " +
                            "with head(branchNodes) as br, head(masterNodes) as mn " +
                            "with case when (br is not null) then br else mn end as latest_event " +
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
                            "SET a.branch = $branch " +
                        "FOREACH (n IN case when (latest_event is not null) then [1] else [] end | " +
                            "create (latest_event)-[:next {branch: $branch}]->(a) " +
                        ") " +
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
                            "metadata", metaData.getData(),
                            "branch", event.getMetaData().get("branch")
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
        try (Session session = driver.session()) {

            List<Record> records = session.run("match (e:event) return e").list();
            if (records.isEmpty()) {
                return Stream.empty();
            }

            List<Value> values = records.get(records.size() - 1).values();


            Stream<? extends TrackedEventMessage<?>> stream = values.stream()
                .map(nodeValue -> {

                    GenericDomainEventMessage message = new GenericDomainEventMessage<>(
                        nodeValue.get("type").asString(),
                        nodeValue.get("aggregateIdentifier").asString(),
                        Long.parseLong(nodeValue.get("sequenceNumber").asString()),
                        deserializePayload(nodeValue),
                        deserializeMetadata(nodeValue),
                        nodeValue.get("eventIdentifier").asString(),
                        Instant.parse(nodeValue.get("timestamp").asString())
                    );

                    TrackedEventMessage<?> m = new GenericTrackedDomainEventMessage(trackingToken, message);

                    return m;
                });


            return stream;
        }
    }

    @Override
    public DomainEventStream readEvents(String aggregateIdentifier, long firstSequenceNumber) {
        Identifier identifier = Identifier.from(aggregateIdentifier);

        try (Session session = driver.session()) {

            AtomicReference<Long> sequenceNumber = new AtomicReference<>();
            List<Record> records = getAllEventsForAggregate(identifier.getId(), identifier.getBranch());

            if (records.isEmpty() && !"master".equals(identifier.getBranch())) {
                records = getAllEventsForAggregate(identifier.getId(), "master");
            }

            if (records.isEmpty()) {
                return DomainEventStream.of(Stream.empty(), null);
            }


            Stream<? extends DomainEventMessage<?>> stream = records.stream()
                .map(record -> {
                    Value nodeValue = record.values().get(0);
                    GenericDomainEventMessage message = new GenericDomainEventMessage<>(
                        nodeValue.get("type").asString(),
                        nodeValue.get("aggregateIdentifier").asString(),
                        Long.parseLong(nodeValue.get("sequenceNumber").asString()),
                        deserializePayload(nodeValue),
                        deserializeMetadata(nodeValue),
                        nodeValue.get("eventIdentifier").asString(),
                        Instant.parse(nodeValue.get("timestamp").asString())
                    );
                    return (DomainEventMessage<?>) message;
                })
                .peek(event -> {
                    sequenceNumber.set(event.getSequenceNumber());
                });


            return DomainEventStream.of(stream, sequenceNumber::get);
        }

    }

    private List<Record> getAllEventsForAggregate(String aggregateId, String branch) {
        try (Session session = driver.session()) {
            return session.run(
                "match p = (e:event)<-[:next*0..]-(:event)" +
                    " where e.aggregateIdentifier = $aggregateIdentifier and e.branch = $branch" +
                    " unwind nodes(p) as n" +
                    " return n order by n.sequenceNumber",
                parameters(
                    "aggregateIdentifier", aggregateId,
                    "branch", branch
                )
            ).list();
        }
    }

    private MetaData deserializeMetadata(Value nodeValue) {
        return (MetaData) serializer.doDeserialize(new SerializedMetaData(nodeValue.get("metadata").asByteArray(), byte[].class), serializer.getXStream());
    }

    private Object deserializePayload(Value nodeValue) {
        SimpleSerializedObject<byte[]> serializedObject = new SimpleSerializedObject<>(nodeValue.get("payload").asByteArray(), byte[].class, nodeValue.get("payloadType").asString(), null);
        return serializer.doDeserialize(serializedObject, serializer.getXStream());
    }

    @Override
    public Optional<DomainEventMessage<?>> readSnapshot(String s) {
        return Optional.empty();
    }
}
