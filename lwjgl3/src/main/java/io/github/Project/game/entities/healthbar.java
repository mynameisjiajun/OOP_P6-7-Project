package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.Entity;

public class healthbar extends Entity {
	private float width;
	private float height;
	private float healthPercentage = 1f; // 100% health
	
	public healthbar(float x, float y, float width, float height) {
		super(x, y, 0); // No movement for health bar
		this.width = width;
		this.height = height;
	}
	
	public void setHP(float percentage) {
		this.healthPercentage = percentage;
	}
	@Override
	public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		
		// Draw background (red)
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.rect(getPosX(), getPosY(), width, height);
		
		// Draw foreground (green) based on health percentage
		shapeRenderer.setColor(Color.GREEN);
		shapeRenderer.rect(getPosX(), getPosY(), width * healthPercentage, height);
		
		shapeRenderer.end();
	}
	
	@Override
	public float getWidth() {
		return width;
	}
	@Override
	public float getHeight() {
		return height;
	}
}