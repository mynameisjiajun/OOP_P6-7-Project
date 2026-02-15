package io.github.Project.engine.managers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.Entity;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages all game entities.
 * Handles entity registration, updates, and removal.
 */
public class EntityManager {
    private List<Entity> entities;
    
    /**
     * Creates a new EntityManager.
     */
    public EntityManager() {
        this.entities = new ArrayList<>();
    }
    
    /**
     * Adds an entity to the manager.
     * @param entity The entity to add
     */
    public void addEntity(Entity entity) {
        if (entity != null && !entities.contains(entity)) {
            entities.add(entity);
        }
    }
    
    /**
     * Removes an entity from the manager.
     * @param entity The entity to remove
     */
    public void update(float deltaTime) {
        for (Entity entity : entities) {
            entity.update(deltaTime);
        }
    }
    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }
    
    /**
     * Gets all entities.
     * @return List of all entities
     */
    public List<Entity> getEntities() {
        return entities;
    }
    /**
     * Renders all entities using shared renderers.
     * @param batch Shared SpriteBatch for texture-based entities
     * @param shapeRenderer Shared ShapeRenderer for shape-based entities
     */
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        for (Entity entity : entities) {
            entity.render(batch, shapeRenderer);
        }
    }
     /**
     * Clears all entities.
     */
    public void clear() {
        entities.clear();
    }
}
