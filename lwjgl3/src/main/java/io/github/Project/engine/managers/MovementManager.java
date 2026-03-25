package io.github.Project.engine.managers;

import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.interfaces.IMovementStrategy;
import com.badlogic.gdx.utils.ObjectMap;

public class MovementManager { 
    
	private final ObjectMap<Entity, IMovementStrategy> entityStrategies;

    public MovementManager() {
    	this.entityStrategies = new ObjectMap<>();
    }

    public void registerEntity(Entity entity, IMovementStrategy strategy) {
        if (entity != null && strategy != null && !entityStrategies.containsKey(entity)) {
            entityStrategies.put(entity, strategy);
        }
    }

    public void unregisterEntity(Entity entity) {
        entityStrategies.remove(entity);
    }

    // Updates all registered entities' movements.
    public void updateMovements(float deltaTime) {
    	for (ObjectMap.Entry<Entity, IMovementStrategy> entry : entityStrategies) {
    	    Entity entity = entry.key;
    	    IMovementStrategy strategy = entry.value;
            // A. Strategy Phase: Calculate Velocity (Input -> Velocity)
            // (The Player/Entity determines its own desired velocity)
            	strategy.updateVelocity(entity);
        }
    }
    
}