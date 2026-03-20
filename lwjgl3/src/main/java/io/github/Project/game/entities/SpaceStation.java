package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Texture;
import io.github.Project.engine.entities.CollidableEntity;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class SpaceStation extends CollidableEntity {
	protected Texture texture;
	private float width;
	private float height;
	
	public SpaceStation(float x, float y, float width, float height) {
		super(x, y, 0, width, height);
		this.texture = new Texture("Space station.png");
		this.width = width;
		this.height = height;
		
		this.collisionTag = "SpaceStation";
	}
	
	@Override
	public void update(float deltaTime) {
		updateBounds();
	}
	@Override
	public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
		batch.draw(texture, getPosX(), getPosY(), width, height);
		// Rendering handled by the engine using the texture
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