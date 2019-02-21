package com.spike.axoneventsourcing.events;

import com.spike.axoneventsourcing.Identifier;

public class CategoryCreatedEvent {

    private final Identifier identifier;
    private final String name;

    public CategoryCreatedEvent(Identifier categoryId, String name) {
        this.identifier = categoryId;
        this.name = name;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }
}
