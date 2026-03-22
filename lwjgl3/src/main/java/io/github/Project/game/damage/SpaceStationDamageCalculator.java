package io.github.Project.game.damage;

public class SpaceStationDamageCalculator implements DamageCalculator {
  
	// Station takes LESS damage (armored structure)
    private static final float DEBRIS_DAMAGE = 5f;    // 0.5x rocket damage
    private static final float ASTEROID_DAMAGE = 10f;  // 0.67x rocket damage
    
    @Override
    public float calculateDamage(String collisionTag) {
        switch (collisionTag) {
            case "Debris":
                return DEBRIS_DAMAGE;
            case "Asteroid":
                return ASTEROID_DAMAGE;
            default:
                return 0f;
        }
    }
}