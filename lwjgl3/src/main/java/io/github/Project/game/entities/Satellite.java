package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Texture;

// satellite entity 
public class Satellite extends SpaceStation {
    
    // create satellite with custom health
    public Satellite(float x, float y, float width, float height, float maxHealth) {
        super(x, y, width, height, maxHealth);
        
        // replace texture with satellite texture
        if (this.texture != null) this.texture.dispose();
        this.texture = new Texture("images/entities/Satellite.png");
        
        // set collision tag for satellite specific logic
        this.collisionTag = "Satellite";
    }
    
    // create satellite with default health
    public Satellite(float x, float y, float width, float height) {
        this(x, y, width, height, 60f);
    }
    
    // movement bounds for drifting in space zone
    private static final float DRIFT_X_MIN =  -850f;
    private static final float DRIFT_X_MAX =   850f;
    private static final float DRIFT_Y_MIN =  4100f;
    private static final float DRIFT_Y_MAX =  7200f;

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        // bounce off horizontal bounds
        if (getPosX() < DRIFT_X_MIN) { setPosX(DRIFT_X_MIN); setVx(Math.abs(getVx())); }
        if (getPosX() + getWidth() > DRIFT_X_MAX) { setPosX(DRIFT_X_MAX - getWidth()); setVx(-Math.abs(getVx())); }

        // bounce off vertical bounds
        if (getPosY() < DRIFT_Y_MIN) { setPosY(DRIFT_Y_MIN); setVy(Math.abs(getVy())); }
        if (getPosY() + getHeight() > DRIFT_Y_MAX) { setPosY(DRIFT_Y_MAX - getHeight()); setVy(-Math.abs(getVy())); }

        updateBounds();
    }
    
    @Override
    public void dispose() {
        if (texture != null) texture.dispose();
    }
}