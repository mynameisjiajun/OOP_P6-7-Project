package io.github.Project.game.movementstrategy;

import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.interfaces.IMovementStrategy;
import io.github.Project.engine.input.InputMovement;
import io.github.Project.game.entities.Rocket;
import com.badlogic.gdx.Gdx;

public class RocketMovementStrategy implements IMovementStrategy {
    
    // --- Configurable Physics Values ---
    private float thrustPower = 600f;    // How strong the engine is
    private float rotationSpeed = 180f;  // How fast it turns (degrees per second)
    private float baseGravity = 350f;    // Gravity strength on the ground
    private float spaceThreshold = 3000f;// The Y-coordinate where "Outer Space" begins
    
    @Override
    public void updateVelocity(Entity entity) {
        Rocket rocket = (Rocket) entity;
        InputMovement input = rocket.getInput();
        float deltaTime = Gdx.graphics.getDeltaTime();
        
        // ---------------------------------------------------------
        // 1. ROTATION LOGIC (Left / Right)
        // ---------------------------------------------------------
        // (This replaces the update logic you had inside Rocket.java)
        if (input.keyLeft) {
            rocket.setRotation(rocket.getRotation() + (rotationSpeed * deltaTime));
        }
        if (input.keyRight) {
            rocket.setRotation(rocket.getRotation() - (rotationSpeed * deltaTime));
        }

        // ---------------------------------------------------------
        // 2. VARIABLE GRAVITY LOGIC
        // ---------------------------------------------------------
        float currentGravity = 0f;
        
        // If we are below the "Space Threshold", apply gravity
        if (rocket.getPosY() < spaceThreshold) {
            // Calculate a percentage. 1.0 at the ground, 0.0 at space edge.
            float gravityFactor = 1.0f - (rocket.getPosY() / spaceThreshold);
            currentGravity = baseGravity * gravityFactor;
        }
        
        // Constantly pull the rocket down based on current gravity
        rocket.setVy(rocket.getVy() - (currentGravity * deltaTime));

        // ---------------------------------------------------------
        // 3. THRUST LOGIC (Up Key)
        // ---------------------------------------------------------
        if (input.keyUp) {
            // Convert degrees to radians for math functions
            float radians = (float) Math.toRadians(rocket.getRotation());
            
            // Calculate X and Y thrust vectors based on which way it's pointing
            float thrustX = (float) Math.cos(radians) * thrustPower * deltaTime;
            float thrustY = (float) Math.sin(radians) * thrustPower * deltaTime;
            
            // Add thrust to current velocity (creates momentum)
            rocket.setVx(rocket.getVx() + thrustX);
            rocket.setVy(rocket.getVy() + thrustY);
        }

        // ---------------------------------------------------------
        // 4. THE GROUND (Launchpad collision)
        // ---------------------------------------------------------
        // Prevent the rocket from falling through the floor before liftoff
        if (rocket.getPosY() <= 0) {
            rocket.setPosY(0);
            if (rocket.getVy() < 0) {
                rocket.setVy(0); // Stop falling
            }
            // Apply ground friction so it doesn't slide endlessly on the floor
            rocket.setVx(rocket.getVx() * 0.95f); 
        }
    }
}