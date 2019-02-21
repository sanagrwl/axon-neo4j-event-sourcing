package com.spike.axoneventsourcing.aggregates;

import com.spike.axoneventsourcing.Identifier;
import com.spike.axoneventsourcing.commands.CreateCategoryCommand;
import com.spike.axoneventsourcing.commands.UpdateCategoryCommand;
import com.spike.axoneventsourcing.events.CategoryCreatedEvent;
import com.spike.axoneventsourcing.events.CategoryUpdatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class CategoryAggregate {

    @AggregateIdentifier
    private Identifier identifier;
    private String name;

    @CommandHandler
    public CategoryAggregate(CreateCategoryCommand command) {
        AggregateLifecycle.apply(new CategoryCreatedEvent(command.getIdentifier(), command.getName()));
    }

    @CommandHandler
    public void handle(UpdateCategoryCommand command) {
        AggregateLifecycle.apply(new CategoryUpdatedEvent(command.getIdentifier(), command.getName()));
    }

    @EventSourcingHandler
    public void on(CategoryCreatedEvent event) {
        this.identifier = event.getIdentifier();
        this.name = event.getName();
    }

    @EventSourcingHandler
    public void on(CategoryUpdatedEvent event) {
        this.name = event.getName();
    }


    protected CategoryAggregate() {}
}
