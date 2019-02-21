package com.spike.axoneventsourcing.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class CreateCategoryCommand {

    @TargetAggregateIdentifier
    private final String categoryId;
    private final String name;
    private final String branch = "master";

    public CreateCategoryCommand(String categoryId, String name) {
        this.categoryId = categoryId;
        this.name = name;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public String getBranch() {
        return branch;
    }
}
