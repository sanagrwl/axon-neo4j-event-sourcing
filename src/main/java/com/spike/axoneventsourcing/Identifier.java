package com.spike.axoneventsourcing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

public final class Identifier {
    private String id;
    private String branch;

    public Identifier() {}

    public Identifier(String id, String branch) {
        this.id = id;
        this.branch = branch;
    }

    public String getId() {
        return id;
    }

    public String getBranch() {
        return branch;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identifier that = (Identifier) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(branch, that.branch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, branch);
    }
}
