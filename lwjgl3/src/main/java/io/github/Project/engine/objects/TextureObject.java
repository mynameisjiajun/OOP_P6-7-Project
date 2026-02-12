package io.github.Project.engine.objects;

import io.github.Project.engine.entities.CollidableEntity;

public abstract class TextureObject extends CollidableEntity {
protected com.badlogic.gdx.graphics.Texture texture;
  

    public TextureObject(String texturefile, float x, float y, float width, float height, float speed) {
        super(x, y, speed, width, height);
        this.texture = new com.badlogic.gdx.graphics.Texture(texturefile);
    }

    @Override
    public float getWidth() { return bounds.width; }

    @Override
    public float getHeight() { return bounds.height; }
}