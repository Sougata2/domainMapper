package com.domain.mapper.references;

import java.util.Objects;

public class Edge {
    private final Object parent;
    private final Object child;
    private final String field;

    public Edge(Object parent, Object child, String field) {
        this.parent = parent;
        this.child = child;
        this.field = field;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return Objects.equals(parent, edge.parent) && Objects.equals(child, edge.child) && Objects.equals(field, edge.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(System.identityHashCode(parent), System.identityHashCode(child), System.identityHashCode(field));
    }
}
