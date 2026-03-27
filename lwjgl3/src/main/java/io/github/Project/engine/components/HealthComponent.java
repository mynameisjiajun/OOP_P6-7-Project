package io.github.Project.engine.components;

public class HealthComponent {

    private final float maxHealth;
    private float currentHealth;
    
    public HealthComponent(float maxHealth) {
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("Max health must be positive");
        }
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }
    
    public void takeDamage(float amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage amount cannot be negative");
        }
        currentHealth = Math.max(0, currentHealth - amount);
    }
    
    public void heal(float amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Heal amount cannot be negative");
        }
        currentHealth = Math.min(maxHealth, currentHealth + amount);
    }
    
    public void fullyHeal() {
        currentHealth = maxHealth;
    }
    
    public boolean isAlive() {
        return currentHealth > 0;
    }
    
    // returns health as a 0.0–1.0 fraction (for health bars)
    public float getHealthPercentage() {
        return currentHealth / maxHealth;
    }
    
    public float getCurrentHealth() {
        return currentHealth;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public void reset() {
        currentHealth = maxHealth;
    }
}
