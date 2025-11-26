package com.domain.mapper.references;

public class ParentChild<P, C> {
    private final P parent;
    private final C child;
    private int level;

    public ParentChild(P parent, C child) {
        this(parent, child, 10);
    }

    public ParentChild(P parent, C child, int level) {
        this.parent = parent;
        this.child = child;
        this.level = level;
    }

    public P parent(){
        return parent;
    }

    public C child(){
        return child;
    }
}
