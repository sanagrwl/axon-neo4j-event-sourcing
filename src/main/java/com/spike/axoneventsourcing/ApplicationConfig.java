package com.spike.axoneventsourcing;

import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {


    @Bean
    public EventStorageEngine eventStore() {

        Driver neo4jDriver = GraphDatabase.driver("bolt://localhost", AuthTokens.basic("neo4j", "1234"));
        return new GraphEventStorageEngine(neo4jDriver);


    }
}
