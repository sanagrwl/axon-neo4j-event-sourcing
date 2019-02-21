package com.spike.axoneventsourcing.events;

import com.spike.axoneventsourcing.Identifier;

public class CategoryUpdatedEvent {

    private final Identifier identifier;
    private final String name;

    public CategoryUpdatedEvent(Identifier identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }
}
