package io.github.Project.game.systems.damage;

public class SatelliteDamageCalculator implements DamageCalculator {
  
	private static final float DEBRIS_DAMAGE = 25f;   // 2.5x rocket damage
    private static final float ASTEROID_DAMAGE = 40f;  // 2.67x rocket damage
    
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