package com.spike.axoneventsourcing.commands;

import com.spike.axoneventsourcing.Identifier;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class CreateCategoryCommand {

    @TargetAggregateIdentifier
    private final Identifier identifier;
    private final String name;
    private final String branch = "master";

    public CreateCategoryCommand(Identifier identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public String getBranch() {
        return branch;
    }
}
