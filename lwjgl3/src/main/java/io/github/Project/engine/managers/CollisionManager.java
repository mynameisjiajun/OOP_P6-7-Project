package io.github.Project.engine.managers;

import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.entities.CollidableEntity;
import java.util.List;

public class CollisionManager {
    private EntityManager entityManager;

    public CollisionManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * The Workhorse: This is the loop that makes it a Class.
     * It watches everyone on the field every frame.
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
        // This is where the Referee "blows the whistle"
        // The specific reaction (bouncing/scoring) goes here or in the Scene
    }
}