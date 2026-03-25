package io.github.Project.engine.core;

import com.badlogic.gdx.Screen;
import io.github.Project.engine.core.GameMaster;
import io.github.Project.engine.entities.Entity;
import java.util.ArrayList;
import java.util.List;

// Base class for all scenes.
// Each scene now manages its own entities and pause state.
 
public abstract class Scene implements Screen {
    protected GameMaster gameMaster;
    protected int width;
    protected int height;
    
    // ADDED: Scene-specific entity management
    protected List<Entity> sceneEntities;
    protected boolean isPaused;
    
    //Creates a new scene.
    //@param gameMaster Reference to the game master
   
    public Scene(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
        this.sceneEntities = new ArrayList<>();
        this.isPaused = false;
    }
    
    //Called when this screen becomes the current screen.
  // initialize scene-specific entities.
   
    @Override
    public abstract void show();
    
    //Called when the screen should render itself.
    //@param delta The time in seconds since the last render.
    
    @Override
    public abstract void render(float delta);
    
    // Called when the window is resized.
    //@param width New width
     //@param height New height
  
    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    // Called when the game is paused.
     
    @Override
    public void pause() {
        this.isPaused = true;
    }
    
    //Called when the game is resumed from a paused state.
    
    @Override
    public void resume() {
        this.isPaused = false;
    }
    
    // Called when this screen is no longer the current screen.
   
    @Override
    public void hide() {
        // Override in subclass if needed
    }
    
    // Called when this screen should release all resources.
  
    @Override
    public abstract void dispose();
    
    // Scene entity management,Adds an entity to this scene and registers it with managers.
    
    protected void addSceneEntity(Entity entity) {
        sceneEntities.add(entity);
        gameMaster.getEntityManager().addEntity(entity);
    }
    
    //Removes an entity from this scene and unregisters from managers.
   
    protected void removeSceneEntity(Entity entity) {
        sceneEntities.remove(entity);
        gameMaster.getEntityManager().removeEntity(entity);
        gameMaster.getMovementManager().unregisterEntity(entity);
    }
    
    // Gets all entities in this scene.
     
    public List<Entity> getSceneEntities() {
        return sceneEntities;
    }
    
    // Clears all entities from this scene.
   
    protected void clearSceneEntities() {
        for (Entity entity : new ArrayList<>(sceneEntities)) {
            removeSceneEntity(entity);
        }
        sceneEntities.clear();
    }
    
    //Checks if this scene is paused.
     
    public boolean isPaused() {
        return isPaused;
    }
    
    //Sets the pause state of this scene.
     
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