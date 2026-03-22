package io.github.Project.engine.components;

public class HealthComponent {
    
    private final float maxHealth;
    private float currentHealth;
    private boolean invulnerable = false;
    
    /**
     * Creates a health component with the specified maximum health.
     * Current health starts at maximum.
     * 
     * @param maxHealth the maximum health value (must be > 0)
     */
    public HealthComponent(float maxHealth) {
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("Max health must be positive");
        }
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }
    
    /**
     * Reduces current health by the specified amount.
     * Health cannot go below zero.
     * 
     * @param amount damage to apply (must be >= 0)
     */
    public void takeDamage(float amount) {
        if (invulnerable) return;
        if (amount < 0) {
            throw new IllegalArgumentException("Damage amount cannot be negative");
        }
        currentHealth = Math.max(0, currentHealth - amount);
    }
    
    /**
     * Increases current health by the specified amount.
     * Health cannot exceed maximum.
     * 
     * @param amount health to restore (must be >= 0)
     */
    public void heal(float amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Heal amount cannot be negative");
        }
        currentHealth = Math.min(maxHealth, currentHealth + amount);
    }
    
    /**
     * Fully restores health to maximum.
     */
    public void fullyHeal() {
        currentHealth = maxHealth;
    }
    
    /**
     * Checks if the entity is still alive (health > 0).
     * 
     * @return true if current health is greater than 0
     */
    public boolean isAlive() {
        return currentHealth > 0;
    }
    
    /**
     * Returns current health as a percentage of maximum (0.0 to 1.0).
     * Useful for health bars and UI display.
     * 
     * @return health percentage (0.0 = dead, 1.0 = full health)
     */
    public float getHealthPercentage() {
        return currentHealth / maxHealth;
    }
    
    /**
     * Returns the current health value.
     */
    public float getCurrentHealth() {
        return currentHealth;
    }
    
    /**
     * Returns the maximum health value.
     */
    public float getMaxHealth() {
        return maxHealth;
    }
    
    /**
     * Sets invulnerability state (e.g., during respawn or power-up).
     * When invulnerable, takeDamage() has no effect.
     * 
     * @param invulnerable true to enable invulnerability
     */
    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }
    
    /**
     * Checks if currently invulnerable.
     */
    public boolean isInvulnerable() {
        return invulnerable;
    }
    
    /**
     * Resets health to maximum and clears invulnerability.
     * Useful for entity respawning or level restart.
     */
    public void reset() {
        currentHealth = maxHealth;
        invulnerable = false;
    }
}
