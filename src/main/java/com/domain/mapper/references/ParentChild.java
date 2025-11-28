package com.domain.mapper.references;

public class ParentChild<P, C> {
    private final P parent;
    private final C child;
    private final int level;
    private final String childName;

    public ParentChild(P parent, C child, String childName) {
        this(parent, child, childName, 0);
    }

    public ParentChild(P parent, C child, String childName, int level) {
        this.childName = childName;
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

    public String childName() {
        return childName;
    }
}
