package io.github.Project.game.movementstrategy;

import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.interfaces.IMovementStrategy;
import io.github.Project.engine.input.InputMovement;
import io.github.Project.game.entities.Rocket;
import com.badlogic.gdx.Gdx;

public class RocketMovementStrategy implements IMovementStrategy {
    
    // --- Configurable Physics Values ---
	// --- Configurable Physics Values ---
    private float thrustPower = 500f;    // Buffed from 450f for a stronger liftoff
    private float rotationSpeed = 180f;  
    private float baseGravity = 300f;    
    private float spaceThreshold = 3000f;
    
    // --- ADJUSTED: Aerodynamic Drag Values ---
    private float earthDrag = 0.995f;    // Air resistance (You will actually pierce the atmosphere now!)
    private float spaceDrag = 0.999f;    // Vacuum drifting
    
    @Override
    public void updateVelocity(Entity entity) {
        Rocket rocket = (Rocket) entity;
        InputMovement input = rocket.getInput();
        float deltaTime = Gdx.graphics.getDeltaTime();
        
        // ---------------------------------------------------------
        // 1. ROTATION LOGIC (Left / Right)
        // ---------------------------------------------------------
        if (input.keyLeft) {
            rocket.setRotation(rocket.getRotation() + (rotationSpeed * deltaTime));
        }
        if (input.keyRight) {
            rocket.setRotation(rocket.getRotation() - (rotationSpeed * deltaTime));
        }

        // ---------------------------------------------------------
        // 2. VARIABLE GRAVITY & DRAG LOGIC
        // ---------------------------------------------------------
        float currentGravity = 0f;
        float currentDrag = spaceDrag; // Default to slippery space
        
        // If we are inside the atmosphere (below spaceThreshold)
        if (rocket.getPosY() < spaceThreshold) {
            float atmosphereFactor = 1.0f - (rocket.getPosY() / spaceThreshold);
            currentGravity = baseGravity * atmosphereFactor;
            currentDrag = spaceDrag - ((spaceDrag - earthDrag) * atmosphereFactor);
        }
        
        // Apply Gravity (Pulls down)
        rocket.setVy(rocket.getVy() - (currentGravity * deltaTime));

        // Apply Drag EQUALLY to X and Y
        // (The artificial brake has been removed. You will now coast upwards in space!)
        rocket.setVx(rocket.getVx() * currentDrag);
        rocket.setVy(rocket.getVy() * currentDrag);

        // ---------------------------------------------------------
        // 3. THRUST LOGIC (Up Key)
        // ---------------------------------------------------------
        if (input.keyUp) {
            float radians = (float) Math.toRadians(rocket.getRotation());
            float thrustX = (float) Math.cos(radians) * thrustPower * deltaTime;
            float thrustY = (float) Math.sin(radians) * thrustPower * deltaTime;
            
            rocket.setVx(rocket.getVx() + thrustX);
            rocket.setVy(rocket.getVy() + thrustY);
        }

        // ---------------------------------------------------------
        // 4. THE GROUND (Launchpad collision)
        // ---------------------------------------------------------
        if (rocket.getPosY() <= 0) {
            rocket.setPosY(0);
            if (rocket.getVy() < 0) {
                rocket.setVy(0); // Stop falling
            }
            // Ground friction so it doesn't slide like ice on the launchpad
            rocket.setVx(rocket.getVx() * 0.85f); 
        }
    }
}