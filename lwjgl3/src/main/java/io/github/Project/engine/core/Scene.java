package io.github.Project.engine.core;

import com.badlogic.gdx.Screen;
import io.github.Project.engine.core.GameMaster;
import io.github.Project.engine.entities.Entity;
import java.util.ArrayList;
import java.util.List;

// Base class for all scenes. Each scene manages its own entities and pause state.

public abstract class Scene implements Screen {
    protected GameMaster gameMaster;
    protected int width;
    protected int height;

    protected List<Entity> sceneEntities;
    protected boolean isPaused;

    public Scene(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
        this.sceneEntities = new ArrayList<>();
        this.isPaused = false;
    }

    @Override
    public abstract void show();

    @Override
    public abstract void render(float delta);

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void pause() {
        this.isPaused = true;
    }

    @Override
    public void resume() {
        this.isPaused = false;
    }

    @Override
    public void hide() {
        // override in subclass if needed
    }

    @Override
    public abstract void dispose();

    // adds entity to this scene and registers it with managers
    protected void addSceneEntity(Entity entity) {
        sceneEntities.add(entity);
        gameMaster.getEntityManager().addEntity(entity);
    }

    // removes entity from this scene and unregisters from managers
    protected void removeSceneEntity(Entity entity) {
        sceneEntities.remove(entity);
        gameMaster.getEntityManager().removeEntity(entity);
        gameMaster.getMovementManager().unregisterEntity(entity);
    }

    public List<Entity> getSceneEntities() {
        return sceneEntities;
    }

    protected void clearSceneEntities() {
        for (Entity entity : new ArrayList<>(sceneEntities)) {
            removeSceneEntity(entity);
        }
        sceneEntities.clear();
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }
    

    public GameMaster getGameMaster() {
        return gameMaster;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
}