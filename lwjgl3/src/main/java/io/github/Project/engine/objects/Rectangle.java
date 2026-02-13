package io.github.Project.engine.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import io.github.Project.engine.entities.Entity;

public class Rectangle extends ShapeObject {
	public Rectangle(float x, float y, float width, float height, float speed, Color color) {
		super(x, y, width, height, speed, color);
	}


	public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(color);
		shapeRenderer.rect(posX, posY, width, height);
		shapeRenderer.end();
	}
	public void update(float deltaTime) {
	}


	@Override
	public float getHeight() {
		// TODO Auto-generated method stub
		return 0;
	}
}


