package io.github.Project.engine.components;

public class BoundaryConstraint {
    private float minX, maxX, minY, maxY;
    
    public BoundaryConstraint(float minX, float maxX, float minY, float maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }
    
    public void constrain(float[] position, float width, float height) {
        // Clamp X
        if (position[0] < minX) position[0] = minX;
        if (position[0] + width > maxX) position[0] = maxX - width;
        
        // Clamp Y
        if (position[1] < minY) position[1] = minY;
        if (position[1] + height > maxY) position[1] = maxY - height;
    }
}