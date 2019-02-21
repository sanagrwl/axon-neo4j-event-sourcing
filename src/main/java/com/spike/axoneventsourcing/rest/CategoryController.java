package com.spike.axoneventsourcing.rest;

import com.spike.axoneventsourcing.commands.CreateCategoryCommand;
import com.spike.axoneventsourcing.commands.UpdateCategoryCommand;
import com.spike.axoneventsourcing.queries.FindAllCategoriesQuery;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class CategoryController {

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;

    @Autowired
    public CategoryController(CommandGateway commandGateway, QueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    @PostMapping("/categories")
    public void createCategory() {
        String categoryId = UUID.randomUUID().toString();
        commandGateway.send(new CreateCategoryCommand(categoryId, "Deluxe Chair"));
    }

    @PutMapping("/categories/{id}")
    public void updateCategory(@PathVariable String id) {
        commandGateway.send(new UpdateCategoryCommand(id, "Deluxe Chair new"));
    }



    @GetMapping("/categories")
    public List<Category> findAllCategories() {
        return queryGateway.query(new FindAllCategoriesQuery(),
            ResponseTypes.multipleInstancesOf(Category.class)).join();
    }


}
