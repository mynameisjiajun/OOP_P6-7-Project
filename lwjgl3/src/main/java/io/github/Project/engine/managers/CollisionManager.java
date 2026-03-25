package io.github.Project.engine.managers;

import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.entities.CollidableEntity;
import java.util.ArrayList;
import java.util.List;

// Manages collision detection between collidable entities

public class CollisionManager {
    private AudioManager audioManager;
    private List<CollisionListener> listeners;
    private List<CollisionPair> collidedThisFrame;

    // Holds information about collision between entities
    public static class CollisionInfo {
        public final Entity entity1;
        public final Entity entity2;
        public final String tag1;
        public final String tag2;
        public final float overlapX;
        public final float overlapY;

        public CollisionInfo(Entity e1, Entity e2, String t1, String t2,
                             float ox, float oy) {
            this.entity1 = e1;
            this.entity2 = e2;
            this.tag1 = t1;
            this.tag2 = t2;
            this.overlapX = ox;
            this.overlapY = oy;
        }

        public boolean involves(String tag) {
            return tag1.equals(tag) || tag2.equals(tag);
        }

        public boolean isBetween(String tagA, String tagB) {
            return (tag1.equals(tagA) && tag2.equals(tagB)) ||
                   (tag1.equals(tagB) && tag2.equals(tagA));
        }
    }

    public interface CollisionListener {
        void onCollision(CollisionInfo info);
    }

    // used to track collision pairs so same collision is not handled twice in one frame
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

    public CollisionManager(AudioManager audioManager) {
        this.audioManager = audioManager;
        this.listeners = new ArrayList<>();
        this.collidedThisFrame = new ArrayList<>();
    }

    public void addListener(CollisionListener listener) {
        listeners.add(listener);
    }

    public void removeListener(CollisionListener listener) {
        listeners.remove(listener);
    }

    public void clearListeners() {
        listeners.clear();
    }

    public void checkCollisions(List<Entity> entities) {
        collidedThisFrame.clear();

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

    private CollisionInfo checkOverlap(Entity e1, Entity e2) {
        if (!(e1 instanceof CollidableEntity) || !(e2 instanceof CollidableEntity)) {
            return null;
        }

        CollidableEntity ce1 = (CollidableEntity) e1;
        CollidableEntity ce2 = (CollidableEntity) e2;

        ce1.updateBounds();
        ce2.updateBounds();

        if (ce1.getBounds().overlaps(ce2.getBounds())) {
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
        // Auto-dispatch: let each entity's own onCollision handle the response
        if (info.entity1 instanceof CollidableEntity && info.entity2 instanceof CollidableEntity) {
            CollidableEntity ce1 = (CollidableEntity) info.entity1;
            CollidableEntity ce2 = (CollidableEntity) info.entity2;
            ce1.onCollision(ce2);
            ce2.onCollision(ce1);
        }

        // Notify any additional listeners (for scene-level side effects)
        for (CollisionListener listener : listeners) {
            listener.onCollision(info);
        }
    }
}