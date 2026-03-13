package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import io.github.Project.engine.entities.Entity;

public class Fuelbar extends Entity {
	private float width;
	private float height;
	private float fuelPercentage = 1f; // 100% fuel
	
	public Fuelbar(float x, float y, float width, float height) {
		super(x, y, 0); // No movement for fuel bar
		this.width = width;
		this.height = height;
	}
	
	public void setFuel(float percentage) {
		this.fuelPercentage = percentage;
	}
	
	@Override
	public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		
		// Draw background (gray)
		shapeRenderer.setColor(Color.GRAY);
		shapeRenderer.rect(getPosX(), getPosY(), width, height);
		
		// Draw foreground (blue) based on fuel percentage
		shapeRenderer.setColor(Color.GOLD);
		shapeRenderer.rect(getPosX(), getPosY(), width * fuelPercentage, height);
		
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
	public void dispose() {
		// No resources to dispose for the fuel bar
	}
}