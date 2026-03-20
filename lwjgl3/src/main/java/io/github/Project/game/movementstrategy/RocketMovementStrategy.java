package io.github.Project.game.movementstrategy;

import io.github.Project.engine.entities.Entity;
import io.github.Project.engine.interfaces.IMovementStrategy;
import io.github.Project.engine.input.InputMovement;
import io.github.Project.game.entities.Rocket;
import io.github.Project.game.entities.Moon;
import com.badlogic.gdx.Gdx;

public class RocketMovementStrategy implements IMovementStrategy {
    
    // --- Configurable Earth Physics Values ---
    private float thrustPower = 500f;    
    private float rotationSpeed = 180f;  
    private float baseGravity = 300f;    
    private float spaceThreshold = 3000f;
    
    // --- Configurable Moon Physics Values ---
    private Moon moon; // Reference to the moon
    private float moonBaseGravity = 80f;     // Much weaker than Earth's 300f
    private float moonGravityRadius = 1200f; // How close you must be to feel the pull
    
    private float earthDrag = 0.995f;    
    private float spaceDrag = 0.999f;    
    
    // Constructor to pass the Moon into the Strategy
    public RocketMovementStrategy(Moon moon) {
        this.moon = moon;
    }
    
    // Overloaded constructor just in case you want to test without a moon
    public RocketMovementStrategy() {
        this.moon = null;
    }

    @Override
    public void updateVelocity(Entity entity) {
        Rocket rocket = (Rocket) entity;
        InputMovement input = rocket.getInput();
        float deltaTime = Gdx.graphics.getDeltaTime();
        
        // 1. ROTATION LOGIC
        if (input.keyLeft) {
            rocket.setRotation(rocket.getRotation() + (rotationSpeed * deltaTime));
        }
        if (input.keyRight) {
            rocket.setRotation(rocket.getRotation() - (rotationSpeed * deltaTime));
        }

        // 2. EARTH GRAVITY & DRAG LOGIC
        float currentGravity = 0f;
        float currentDrag = spaceDrag; 
        
        if (rocket.getPosY() < spaceThreshold) {
            float atmosphereFactor = 1.0f - (rocket.getPosY() / spaceThreshold);
            currentGravity = baseGravity * atmosphereFactor;
            currentDrag = spaceDrag - ((spaceDrag - earthDrag) * atmosphereFactor);
        }
        
        // Apply Earth Gravity (Pulls straight down)
        rocket.setVy(rocket.getVy() - (currentGravity * deltaTime));

        // 3. MOON GRAVITY LOGIC (Radial Pull)
        if (moon != null) {
            // Find the exact center points of both objects
            float moonCenterX = moon.getPosX() + (moon.getWidth() / 2f);
            float moonCenterY = moon.getPosY() + (moon.getHeight() / 2f);
            float rocketCenterX = rocket.getPosX() + (rocket.getWidth() / 2f);
            float rocketCenterY = rocket.getPosY() + (rocket.getHeight() / 2f);
            
            // Calculate distance between them using Pythagorean theorem
            float dx = moonCenterX - rocketCenterX;
            float dy = moonCenterY - rocketCenterY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            
            // If the rocket is inside the Moon's gravity well
            if (distance > 0 && distance < moonGravityRadius) {
                // Gravity gets stronger the closer you are to the moon
                float pullStrength = moonBaseGravity * (1.0f - (distance / moonGravityRadius));
                
                // Normalize the direction vector (creates a value between -1 and 1)
                float dirX = dx / distance;
                float dirY = dy / distance;
                
                // Apply the pull pointing directly at the moon
                rocket.setVx(rocket.getVx() + (dirX * pullStrength * deltaTime));
                rocket.setVy(rocket.getVy() + (dirY * pullStrength * deltaTime));
            }
        }

        // Apply Drag EQUALLY to X and Y
        rocket.setVx(rocket.getVx() * currentDrag);
        rocket.setVy(rocket.getVy() * currentDrag);

        // 4. THRUST LOGIC 
        if (input.keyUp) {
            float radians = (float) Math.toRadians(rocket.getRotation());
            float thrustX = (float) Math.cos(radians) * thrustPower * deltaTime;
            float thrustY = (float) Math.sin(radians) * thrustPower * deltaTime;
            
            rocket.setVx(rocket.getVx() + thrustX);
            rocket.setVy(rocket.getVy() + thrustY);
        }

        // 5. THE GROUND (Earth Launchpad collision)
        if (rocket.getPosY() <= 0) {
            rocket.setPosY(0);
            if (rocket.getVy() < 0) {
                rocket.setVy(0); 
            }
            rocket.setVx(rocket.getVx() * 0.85f); 
        }
    }
}