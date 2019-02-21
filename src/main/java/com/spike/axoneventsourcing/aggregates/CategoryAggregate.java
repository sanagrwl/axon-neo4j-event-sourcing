package com.spike.axoneventsourcing.aggregates;

import com.spike.axoneventsourcing.commands.CreateCategoryCommand;
import com.spike.axoneventsourcing.commands.UpdateCategoryCommand;
import com.spike.axoneventsourcing.events.CategoryCreatedEvent;
import com.spike.axoneventsourcing.events.CategoryUpdatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class CategoryAggregate {

    @AggregateIdentifier
    private String categoryId;
    private String name;

    @CommandHandler
    public CategoryAggregate(CreateCategoryCommand command) {
        AggregateLifecycle.apply(new CategoryCreatedEvent(command.getCategoryId(), command.getName()), MetaData.with("branch", command.getBranch()));
    }

    @CommandHandler
    public void handle(UpdateCategoryCommand command) {
        AggregateLifecycle.apply(new CategoryUpdatedEvent(command.getCategoryId(), command.getName()), MetaData.with("branch", command.getBranch()));
    }

    @EventSourcingHandler
    public void on(CategoryCreatedEvent event) {
        this.categoryId = event.getCategoryId();
        this.name = event.getName();
    }

    @EventSourcingHandler
    public void on(CategoryUpdatedEvent event) {
        this.name = event.getName();
    }


    protected CategoryAggregate() {}
}
