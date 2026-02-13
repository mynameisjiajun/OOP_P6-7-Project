package io.github.Project.engine.objects;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;

public class Rectangle extends ShapeObject {
	public Rectangle(float x, float y, float width, float height, float speed, Color color) {
		super(x, y, width, height, speed, color);
		shapeRenderer = new ShapeRenderer();
	}


	public void render() {
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(color);
		shapeRenderer.rect(x, y, width, height);
		shapeRenderer.end();
	}
	public void update(float deltaTime) {
	}
}


