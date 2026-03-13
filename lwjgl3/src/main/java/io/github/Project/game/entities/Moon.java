package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Texture;
import io.github.Project.engine.entities.CollidableEntity;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Moon extends CollidableEntity {
	
	private Texture texture;
	private float size;
	
	public Moon(float x, float y, float width, float height) {
		super(x, y, 0, width, height);
		this.texture = new Texture("moon.jpg");
		this.size = width;
	}
	@Override
	public float getWidth() {
		return size;
	}
	@Override
	public float getHeight() {
		return size;
	}
	public void update(float deltaTime) {
		// Moon doesn't move, so no update logic needed
	}
	public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
		batch.draw(texture, getPosX(), getPosY(), size, size);
		// Rendering handled by the engine using the texture
	}
	public void dispose() {
        if (texture != null) texture.dispose();
    }
}
	