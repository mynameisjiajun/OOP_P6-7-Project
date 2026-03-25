package io.github.Project.game.systems.damage;

public class RocketDamageCalculator implements DamageCalculator {
	// Damage values for rocket
    private static final float DEBRIS_DAMAGE = 10f;
    private static final float ASTEROID_DAMAGE = 15f;
    private static final float GROUND_CRASH_DAMAGE = 999f;  // Instant death on ground contact
    
    @Override
    public float calculateDamage(String collisionTag) {
        switch (collisionTag) {
            case "Debris":
                return DEBRIS_DAMAGE;
            case "Asteroid":
                return ASTEROID_DAMAGE;
            case "Ground":
                return GROUND_CRASH_DAMAGE;  // Instant death
            default:
                return 0f;  // No damage from other types
        }
    }
}