package io.github.Project.engine.components;

public class FuelComponent {
    
    private final float maxFuel;
    private float currentFuel;
    private final float drainRate;  // Fuel consumed per second
    
    /**
     * Creates a fuel component with specified parameters.
     * 
     * @param maxFuel maximum fuel capacity (must be > 0)
     * @param drainRate fuel consumption rate per second (must be >= 0)
     */
    public FuelComponent(float maxFuel, float drainRate) {
        if (maxFuel <= 0) {
            throw new IllegalArgumentException("Max fuel must be positive");
        }
        if (drainRate < 0) {
            throw new IllegalArgumentException("Drain rate cannot be negative");
        }
        this.maxFuel = maxFuel;
        this.currentFuel = maxFuel;
        this.drainRate = drainRate;
    }
    
    /**
     * Consumes fuel based on elapsed time and drain rate.
     * Fuel cannot go below zero.
     * 
     * @param deltaTime time elapsed since last update (in seconds)
     */
    public void consume(float deltaTime) {
        float consumption = drainRate * deltaTime;
        currentFuel = Math.max(0, currentFuel - consumption);
    }
    
    /**
     * Consumes a specific amount of fuel immediately.
     * Useful for special abilities or instant consumption.
     * 
     * @param amount fuel to consume (must be >= 0)
     */
    public void consumeAmount(float amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Consume amount cannot be negative");
        }
        currentFuel = Math.max(0, currentFuel - amount);
    }
    
    /**
     * Restores fuel by the specified amount.
     * Fuel cannot exceed maximum.
     * 
     * @param amount fuel to restore (must be >= 0)
     */
    public void refuel(float amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Refuel amount cannot be negative");
        }
        currentFuel = Math.min(maxFuel, currentFuel + amount);
    }
    
    /**
     * Fully refuels to maximum capacity.
     */
    public void fullyRefuel() {
        currentFuel = maxFuel;
    }
    
    /**
     * Checks if fuel is completely depleted.
     * 
     * @return true if current fuel is zero
     */
    public boolean isEmpty() {
        return currentFuel <= 0;
    }
    
    /**
     * Checks if there's enough fuel for operation.
     * 
     * @param required minimum fuel needed
     * @return true if current fuel >= required
     */
    public boolean hasEnough(float required) {
        return currentFuel >= required;
    }
    
    /**
     * Returns current fuel as a percentage of maximum (0.0 to 1.0).
     * Useful for fuel gauges and UI display.
     * 
     * @return fuel percentage (0.0 = empty, 1.0 = full)
     */
    public float getFuelPercentage() {
        return currentFuel / maxFuel;
    }
    
    /**
     * Returns the current fuel value.
     */
    public float getCurrentFuel() {
        return currentFuel;
    }
    
    /**
     * Returns the maximum fuel capacity.
     */
    public float getMaxFuel() {
        return maxFuel;
    }
    
    /**
     * Returns the drain rate (fuel consumed per second).
     */
    public float getDrainRate() {
        return drainRate;
    }
    
    /**
     * Resets fuel to maximum capacity.
     * Useful for respawning or level restart.
     */
    public void reset() {
        currentFuel = maxFuel;
    }
}
