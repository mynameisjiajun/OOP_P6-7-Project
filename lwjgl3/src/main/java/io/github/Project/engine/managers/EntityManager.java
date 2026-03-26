package io.github.Project.engine.managers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.Entity;
import java.util.ArrayList;
import java.util.List;

// manages all entities in the game (add, update, render, remove)
public class EntityManager {

    // stores all active entities
    private final List<Entity> entities;

    public EntityManager() {
        this.entities = new ArrayList<>();
    }

    // add entity if not null and not already added
    public void addEntity(Entity entity) {
        if (entity != null && !entities.contains(entity)) {
            entities.add(entity);
        }
    }

    // remove entity from manager
    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }

    // returns copy of entity list (prevents external modification)
    public List<Entity> getEntities() {
        return new ArrayList<>(entities);
    }

    // update all entities every frame
    public void update(float deltaTime) {
        for (Entity entity : entities) {
            entity.update(deltaTime);
        }
    }

    // render all entities using shared renderers
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        for (Entity entity : entities) {
            entity.render(batch, shapeRenderer);
        }
    }

    // clears all entities (used when changing scenes)
    public void clear() {
        entities.clear();
    }
}
