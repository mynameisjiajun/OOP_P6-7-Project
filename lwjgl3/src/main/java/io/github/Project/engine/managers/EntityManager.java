package io.github.Project.engine.managers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.Entity;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages all game entities.
 * Handles entity registration, updates, and removal.
 * Entities are scene-scoped but globally tracked here.
 */

public class EntityManager {
    private final List<Entity> entities;
    
    /**
     * Creates new EntityManager.
     */
    
    public EntityManager() {
        this.entities = new ArrayList<>();
    }
    
    /**
     * Adds entity to the manager.
     */
     
    public void addEntity(Entity entity) {
        if (entity != null && !entities.contains(entity)) {
            entities.add(entity);
        }
    }
    
    /**
     * Removes entity from the manager.
     */
     
    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }
    
    /**
     * Gets all entities.
     * @return List of all entities (used by scenes for collision checking)
     */
     
    public List<Entity> getEntities() {
        return new ArrayList<>(entities); // Return copy for safety
    }
    
    /**
     * Updates all entities.(called by scenes)
     * @param deltaTime The time elapsed since the last frame
     */
     
    public void update(float deltaTime) {
        for (Entity entity : entities) {
            entity.update(deltaTime);
        }
    }
    
    /**
     * Renders all entities using shared renderers(called by scenes).
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
