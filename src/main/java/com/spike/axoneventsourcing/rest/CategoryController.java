package com.spike.axoneventsourcing.rest;

import com.spike.axoneventsourcing.Identifier;
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
        Identifier identifier = new Identifier(categoryId, "master");
        commandGateway.send(new CreateCategoryCommand(identifier, "Deluxe Chair"));
    }

    @PutMapping("/categories/{id}")
    public void updateCategory(@PathVariable String id) {
        Identifier identifier = new Identifier(id, "master");
        commandGateway.send(new UpdateCategoryCommand(identifier, "Deluxe Chair master"));
    }


    @GetMapping("/categories")
    public List<Category> findAllCategories() {
        return queryGateway.query(new FindAllCategoriesQuery(),
            ResponseTypes.multipleInstancesOf(Category.class)).join();
    }


}
