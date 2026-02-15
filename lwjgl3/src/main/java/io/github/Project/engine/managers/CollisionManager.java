package io.github.Project.engine.managers;

import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.entities.CollidableEntity;
import java.util.ArrayList;
import java.util.List;

public class CollisionManager {
    private EntityManager entityManager;
    private AudioManager audioManager;
    private List<CollisionListener> listeners;
    private List<CollisionPair> collidedThisFrame;

    // Holds information about a collision between two entities.
     
    public static class CollisionInfo {
        public final Entity entity1;
        public final Entity entity2;
        public final String tag1;
        public final String tag2;
        public final float overlapX;  // Penetration depth in X
        public final float overlapY;  // Penetration depth in Y
        
        public CollisionInfo(Entity e1, Entity e2, String t1, String t2, 
                            float ox, float oy) {
            this.entity1 = e1;
            this.entity2 = e2;
            this.tag1 = t1;
            this.tag2 = t2;
            this.overlapX = ox;
            this.overlapY = oy;
        }
        
        //Check if this collision involves a specific tag.
         
        public boolean involves(String tag) {
            return tag1.equals(tag) || tag2.equals(tag);
        }
        
        //Check if this collision is between two specific tags.
         
        public boolean isBetween(String tagA, String tagB) {
            return (tag1.equals(tagA) && tag2.equals(tagB)) ||
                   (tag1.equals(tagB) && tag2.equals(tagA));
        }
    }

    // Listener interface for collision events.
     
    public interface CollisionListener {
        void onCollision(CollisionInfo info);
    }
    
    // Helper class to track collision pairs and prevent duplicate handling.
     
    private static class CollisionPair {
        Entity e1, e2;
        
        CollisionPair(Entity e1, Entity e2) {
            this.e1 = e1;
            this.e2 = e2;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CollisionPair)) return false;
            CollisionPair other = (CollisionPair) obj;
            return (e1 == other.e1 && e2 == other.e2) ||
                   (e1 == other.e2 && e2 == other.e1);
        }
        
        @Override
        public int hashCode() {
            return e1.hashCode() + e2.hashCode();
        }
    }

    public CollisionManager(EntityManager entityManager, AudioManager audioManager) {
        this.entityManager = entityManager;
        this.audioManager = audioManager;
        this.listeners = new ArrayList<>();
        this.collidedThisFrame = new ArrayList<>();
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
        collidedThisFrame.clear();
        List<Entity> entities = entityManager.getEntities();
        
        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {
                Entity e1 = entities.get(i);
                Entity e2 = entities.get(j);

                CollisionInfo info = checkOverlap(e1, e2);
                if (info != null) {
                    CollisionPair pair = new CollisionPair(e1, e2);
                    if (!collidedThisFrame.contains(pair)) {
                        collidedThisFrame.add(pair);
                        handleCollision(info);
                    }
                }
            }
        }
    }

    /**
     * Enhanced overlap detection with penetration depth calculation.
     */
    private CollisionInfo checkOverlap(Entity e1, Entity e2) {
        if (!(e1 instanceof CollidableEntity) || !(e2 instanceof CollidableEntity)) {
            return null;
        }
        
        CollidableEntity ce1 = (CollidableEntity) e1;
        CollidableEntity ce2 = (CollidableEntity) e2;
        
        ce1.updateBounds();
        ce2.updateBounds();
        
        if (ce1.getBounds().overlaps(ce2.getBounds())) {
            // Calculate penetration depth
            float overlapX = Math.min(
                ce1.getBounds().x + ce1.getBounds().width - ce2.getBounds().x,
                ce2.getBounds().x + ce2.getBounds().width - ce1.getBounds().x
            );
            
            float overlapY = Math.min(
                ce1.getBounds().y + ce1.getBounds().height - ce2.getBounds().y,
                ce2.getBounds().y + ce2.getBounds().height - ce1.getBounds().y
            );
            
            return new CollisionInfo(
                e1, e2,
                ce1.getCollisionTag(),
                ce2.getCollisionTag(),
                overlapX, overlapY
            );
        }
        
        return null;
    }

    private void handleCollision(CollisionInfo info) {
        // Play collision sound
        audioManager.playCollisionSound();

        // Notify all listeners
        for (CollisionListener listener : listeners) {
            listener.onCollision(info);
        }
    }
}