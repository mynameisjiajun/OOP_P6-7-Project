package io.github.Project.game.entities;

import com.badlogic.gdx.graphics.Texture;


public class satellite extends SpaceStation {
	
	public satellite(float x, float y, float width, float height) {
		super(x, y, width, height);
		this.texture = new Texture("satellite.png");
		this.collisionTag = "SpaceStation";
	}
	
	public void update(float deltaTime) {
		// SpaceStation doesn't move, so no update logic needed
	}
	@Override
	public void dispose() {
		if (texture != null) texture.dispose();
	}
	
}