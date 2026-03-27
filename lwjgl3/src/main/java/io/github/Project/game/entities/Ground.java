package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.CollidableEntity;

// Ground collision surface. Landing validation is handled by RocketCollisionStrategy.
// Collision tag "Ground" triggers crash logic; tag "EarthStation" triggers safe landing.
public class Ground extends CollidableEntity {
    
    private Texture texture;
    private final float width;
    private final float height;
    
    public Ground(float x, float y, float width, float height) {
        super(x, y, 0, width, height);
        this.width = width;
        this.height = height;
        this.texture = new Texture("images/backgrounds/desert top.png");
        this.collisionTag = "Ground";
    }
    
    @Override
    public void update(float deltaTime) {
        // Ground is static, no update needed
        updateBounds();
    }
    
    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        float tileWidth = texture.getWidth();
        int numTiles = (int) Math.ceil(width / tileWidth);
        for (int i = 0; i < numTiles; i++) {
            float x = getPosX() + (i * tileWidth);
            float drawWidth = Math.min(tileWidth, width - (i * tileWidth));
            batch.draw(texture,
                x, getPosY(),
                0, 0,
                (int) drawWidth, (int) height,
                1.0f, 1.0f,
                0,
                0, 0,
                (int) drawWidth, (int) height,
                false, false
            );
        }
    }
    
    @Override
    public float getWidth() {
        return width;
    }
    
    @Override
    public float getHeight() {
        return height;
    }
    
    public void dispose() {
        if (texture != null) texture.dispose();
    }
}