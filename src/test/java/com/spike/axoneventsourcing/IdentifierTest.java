package com.spike.axoneventsourcing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class IdentifierTest {

    @Test
    public void foo() throws IOException {
        String x = new ObjectMapper().writeValueAsString(new Identifier("a", "b"));
        System.out.println(x);
        Identifier identifier = new ObjectMapper().readValue(x, Identifier.class);
        System.out.println(identifier.getId());
        System.out.println(identifier.getBranch());
    }
}