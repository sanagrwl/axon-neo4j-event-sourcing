package com.spike.axoneventsourcing.commands;

import com.spike.axoneventsourcing.Identifier;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class UpdateCategoryCommand {
    @TargetAggregateIdentifier
    private final Identifier identifier;
    private final String name;

    public UpdateCategoryCommand(Identifier identifier, String name) {
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
