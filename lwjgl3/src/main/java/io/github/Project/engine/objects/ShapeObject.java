package io.github.Project.engine.objects;

import io.github.Project.engine.entities.CollidableEntity;

public abstract class ShapeObject extends CollidableEntity {
    protected com.badlogic.gdx.graphics.Color color;

    public ShapeObject(float x, float y, float width, float height, float speed, com.badlogic.gdx.graphics.Color color) {
        // Pass width/height to CollidableEntity to create the hitbox
        super(x, y, speed, width, height); 
        this.color = color;
    }
    
    @Override
    public float getWidth() { return bounds.width; }
    
    @Override
    public float getHeight() { return bounds.height; }
}