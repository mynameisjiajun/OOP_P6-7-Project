package io.github.Project.engine.managers;

import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.entities.CollidableEntity;
import java.util.ArrayList;
import java.util.List;

public class CollisionManager {
    private EntityManager entityManager;
    private AudioManager audioManager;
    private List<CollisionListener> listeners;

    /**
     * Listener interface for collision events.
     */
    public interface CollisionListener {
        void onCollision(Entity e1, Entity e2);
    }

    public CollisionManager(EntityManager entityManager, AudioManager audioManager) {
        this.entityManager = entityManager;
        this.audioManager = audioManager;
        this.listeners = new ArrayList<>();
    }

    /**
     * Adds a collision listener.
     */
    public void addListener(CollisionListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a collision listener.
     */
    public void removeListener(CollisionListener listener) {
        listeners.remove(listener);
    }

    /**
     * Clears all listeners.
     */
    public void clearListeners() {
        listeners.clear();
    }

    /**
     * Checks all entities for collisions every frame.
     */
    public void checkCollisions() {
        List<Entity> entities = entityManager.getEntities();
        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {
                Entity e1 = entities.get(i);
                Entity e2 = entities.get(j);

                if (checkOverlap(e1, e2)) {
                    handleCollision(e1, e2);
                }
            }
        }
    }

    private boolean checkOverlap(Entity e1, Entity e2) {
        if (e1 instanceof CollidableEntity && e2 instanceof CollidableEntity) {
            CollidableEntity ce1 = (CollidableEntity) e1;
            CollidableEntity ce2 = (CollidableEntity) e2;
            ce1.updateBounds();
            ce2.updateBounds();
            return ce1.getBounds().overlaps(ce2.getBounds());
        }
        return false;
    }

    public void handleCollision(Entity e1, Entity e2) {
        // Play collision sound
        audioManager.playCollisionSound();

        // Notify all listeners
        for (CollisionListener listener : listeners) {
            listener.onCollision(e1, e2);
        }
    }
}