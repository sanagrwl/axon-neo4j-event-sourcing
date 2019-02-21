package com.spike.axoneventsourcing.events;

public class CategoryCreatedEvent {

    private final String categoryId;
    private final String name;

    public CategoryCreatedEvent(String categoryId, String name) {
        this.categoryId = categoryId;
        this.name = name;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }
}
