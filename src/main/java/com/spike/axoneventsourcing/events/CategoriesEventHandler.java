package com.spike.axoneventsourcing.events;

import com.spike.axoneventsourcing.queries.FindAllCategoriesQuery;
import com.spike.axoneventsourcing.rest.Category;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoriesEventHandler {

    private final Map<String, Category> categories = new HashMap<>();


    @EventHandler
    public void on(CategoryCreatedEvent event) {
        String categoryId = event.getIdentifier().getId();
        categories.put(categoryId, new Category(categoryId, event.getName()));
    }

    @EventHandler
    public void on(CategoryUpdatedEvent event) {
        String categoryId = event.getIdentifier().getId();
        categories.put(categoryId, new Category(categoryId, event.getName()));
    }

    @QueryHandler
    public List<Category> handle(FindAllCategoriesQuery query) {
        return new ArrayList<>(categories.values());
    }

}
