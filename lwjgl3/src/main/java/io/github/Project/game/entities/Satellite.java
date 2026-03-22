package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Texture;

/**
 * REFACTORED: Fixed collision tag and health values.
 * 
 * CRITICAL FIXES:
 * 1. ✅ Collision tag changed from "SpaceStation" to "Satellite"
 *    - Enables differential damage handling
 *    - Allows debris spawning trigger
 * 
 * 2. ✅ Class name capitalized (was "satellite", now "Satellite")
 *    - Follows Java naming conventions
 * 
 * GAME MECHANICS:
 * - "Satellites should take more damage than the space station"
 *   → Lower max health (50 vs 200)
 *   → Uses SatelliteDamageCalculator (higher damage multipliers)
 * 
 * - "When a satellite's health reaches 0, it spawns additional space debris"
 *   → Handled by SatelliteCollisionStrategy
 *   → Creates 3-5 debris pieces on destruction
 */
public class Satellite extends SpaceStation {
    
    /**
     * Creates a satellite with specified health.
     * 
     * @param x X position
     * @param y Y position
     * @param width width
     * @param height height
     * @param maxHealth maximum health (default: 50)
     */
    public Satellite(float x, float y, float width, float height, float maxHealth) {
        super(x, y, width, height, maxHealth);
        
        // Replace texture with satellite-specific texture
        if (this.texture != null) this.texture.dispose();
        this.texture = new Texture("satellite.png");
        
        // ✅ FIX: Use "Satellite" tag instead of "SpaceStation"
        // This enables differential damage and debris spawning
        this.collisionTag = "Satellite";
    }
    
    /**
     * Convenience constructor with default satellite health.
     * Default: 50 health (lower than station, higher than debris).
     * 
     * DAMAGE SCALING:
     * - SpaceStation: 200 health, low damage taken
     * - Satellite: 50 health, high damage taken
     * - Rocket: 100 health, medium damage taken
     */
    public Satellite(float x, float y, float width, float height) {
        this(x, y, width, height, 50f);  // Default: fragile satellite
    }
    
    @Override
    public void update(float deltaTime) {
        // Satellites don't move (future: could add orbital mechanics)
        updateBounds();
    }
    
    @Override
    public void dispose() {
        if (texture != null) texture.dispose();
    }
}