package com.artemis;

import com.artemis.utils.EntityDescription;

import java.util.HashMap;

public class EntitySpawnSystem extends BaseSystem {

    @Override
    protected void initialize() {
        super.setEnabled(false);
        super.initialize();
    }

    @Override
    protected void processSystem() {

    }

    /**
     * The Descriptions are safe on the MultiWorld.
     * @param entityName the Name from the Entity Description
     * @param description the Entity Description {@link EntityDescription}
     */
    public void loadEntityDescription(String entityName, EntityDescription description) {
        super.multiWorld.loadedEntityDescriptions.put(entityName, description);
    }

    public Entity spawnEntity(String entityName) {
        EntityDescription description = super.multiWorld.loadedEntityDescriptions.get(entityName);
        return description == null ? null : description.createEntity(super.world);
    }
}
