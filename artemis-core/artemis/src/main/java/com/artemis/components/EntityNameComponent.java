package com.artemis.components;

import com.artemis.PooledComponent;

public class EntityNameComponent extends PooledComponent {

    public String name;

    @Override
    protected void reset() {
        this.name = null;
    }
}
