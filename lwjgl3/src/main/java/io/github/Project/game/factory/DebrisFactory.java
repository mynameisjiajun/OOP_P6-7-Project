package io.github.Project.game.factory;

import io.github.Project.game.entities.Debris;
import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * PATTERN: Specialized Factory
 * 
 * Handles creation of debris clouds when satellites or other objects are destroyed.
 * Implements the game mechanic: "Satellite destruction spawns more debris (chain reaction)".
 * 
 * Usage:
 *   DebrisFactory factory = new DebrisFactory();
 *   List<Debris> debris = factory.createSatelliteDebris(satellite.getPosX(), satellite.getPosY());
 */
public class DebrisFactory {
    
    private static final float DEBRIS_SIZE = 50f;
    private static final float MIN_SPAWN_DISTANCE = 30f;
    private static final float MAX_SPAWN_DISTANCE = 120f;
    private static final float MIN_VELOCITY = 30f;
    private static final float MAX_VELOCITY = 80f;
    
    private static final int SATELLITE_MIN_DEBRIS = 3;
    private static final int SATELLITE_MAX_DEBRIS = 6;  // Satellites create MORE debris
    
    public List<Debris> createDebrisCloud(float centerX, float centerY, int count) {
        List<Debris> debrisList = new ArrayList<>();
        float angleStep = 360f / count;
        float angleOffset = MathUtils.random(0f, 360f);
        
        for (int i = 0; i < count; i++) {
            float angle = i * angleStep + angleOffset + MathUtils.random(-angleStep * 0.3f, angleStep * 0.3f);
            float angleRad = (float) Math.toRadians(angle);
            float distance = MathUtils.random(MIN_SPAWN_DISTANCE, MAX_SPAWN_DISTANCE);
            
            float x = centerX + (float) Math.cos(angleRad) * distance;
            float y = centerY + (float) Math.sin(angleRad) * distance;
            
            Debris debris = new Debris(x, y, 0, DEBRIS_SIZE, DEBRIS_SIZE);
            
            float velocity = MathUtils.random(MIN_VELOCITY, MAX_VELOCITY);
            debris.setVx((float) Math.cos(angleRad) * velocity);
            debris.setVy((float) Math.sin(angleRad) * velocity);
            
            debrisList.add(debris);
        }
        
        return debrisList;
    }
    
    public List<Debris> createSatelliteDebris(float x, float y) {
        int count = MathUtils.random(SATELLITE_MIN_DEBRIS, SATELLITE_MAX_DEBRIS);
        return createDebrisCloud(x, y, count);
    }

    // Added a method to create a single debris object
    public Debris createSingleDebris(float x, float y) {
        Debris debris = new Debris(x, y, 0, DEBRIS_SIZE, DEBRIS_SIZE);
        float velocity = MathUtils.random(MIN_VELOCITY, MAX_VELOCITY);
        float angle = MathUtils.random(0f, 360f);
        float angleRad = (float) Math.toRadians(angle);
        debris.setVx((float) Math.cos(angleRad) * velocity);
        debris.setVy((float) Math.sin(angleRad) * velocity);
        return debris;
    }
}