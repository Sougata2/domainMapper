package com.domain.mapper.references;

public class ParentChild<P, C> {
    private final P parent;
    private final C child;
    private final int level;
    private final String relationName;

    public ParentChild(P parent, C child, String relationName) {
        this(parent, child, relationName, 0);
    }

    public ParentChild(P parent, C child, String relationName, int level) {
        this.relationName = relationName;
        this.parent = parent;
        this.child = child;
        this.level = level;
    }

    public P parent() {
        return parent;
    }

    public C child() {
        return child;
    }

    public int level() {
        return level;
    }

    public String relationName() {
        return relationName;
    }
}
