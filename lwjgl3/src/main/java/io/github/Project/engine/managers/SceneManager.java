package io.github.Project.engine.managers;

import com.badlogic.gdx.Screen;
import io.github.Project.engine.main.GameMaster;
import io.github.Project.engine.scenes.Scene;

/**
 * Manages game scenes/screens.
 * Handles scene transitions and lifecycle.
 */
public class SceneManager {
    private Scene currentScene;
    private GameMaster gameMaster;
    
    /**
     * Creates a new SceneManager.
     */
    public SceneManager(GameMaster gameMaster) {
    	this.gameMaster = gameMaster;
        this.currentScene = null;
        
    }
    
    public GameMaster getGameMaster() {   
        return gameMaster;
   
    }
    
    /**
     * Sets the current active scene.
     * @param scene The scene to activate
     */
    public void setState(Scene scene) {
        if (currentScene != null) {
            currentScene.hide();
            currentScene.dispose();
        }
        
        currentScene = scene;
        
        if (currentScene != null) {
            currentScene.create();
            currentScene.show();
        }
    }
    
    /**
     * Gets the current active scene.
     * @return The current scene
     */
    public Scene getCurrentScene() {
        return currentScene;
    }
    
    /**
     * Updates the current scene.
     * @param deltaTime Time elapsed since last update
     */
    public void update(float deltaTime) {
        if (currentScene != null) {
            currentScene.update(deltaTime);
        }
    }
    
    /**
     * Renders the current scene.
     */
    public void render() {
        if (currentScene != null) {
            currentScene.render();
        }
    }
    
    /**
     * Pauses the current scene.
     */
    public void pause() {
        if (currentScene != null) {
            currentScene.pause();
        }
    }
    
    /**
     * Resumes the current scene.
     */
    public void resume() {
        if (currentScene != null) {
            currentScene.resume();
        }
    }
    
    /**
     * Handles window resize.
     * @param width New width
     * @param height New height
     */
    public void resize(int width, int height) {
        if (currentScene != null) {
            currentScene.resize(width, height);
        }
    }
    
    /**
     * Disposes of all resources.
     */
    public void dispose() {
        if (currentScene != null) {
            currentScene.dispose();
        }
    }
}
