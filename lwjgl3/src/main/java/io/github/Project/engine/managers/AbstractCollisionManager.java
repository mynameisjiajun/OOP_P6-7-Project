package io.github.Project.engine.managers;

import java.util.List;
import io.github.Project.engine.entities.CollidableEntity;
import io.github.Project.engine.entities.Entity;

public abstract class AbstractCollisionManager implements CollisionManager {

    public void resolveAll(EntityManager entityManager) {
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

    protected boolean checkOverlap(Entity e1, Entity e2) {
        if (e1 instanceof CollidableEntity && e2 instanceof CollidableEntity) {
            CollidableEntity ce1 = (CollidableEntity) e1;
            CollidableEntity ce2 = (CollidableEntity) e2;
            
            // Always sync the box to the current position before checking
            ce1.updateBounds();
            ce2.updateBounds();
            
            return ce1.getBounds().overlaps(ce2.getBounds());
        }
        return false;
    }
   }