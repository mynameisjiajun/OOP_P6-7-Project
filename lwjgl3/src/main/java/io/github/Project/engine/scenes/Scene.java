package io.github.Project.engine.scenes;

import com.badlogic.gdx.Screen;
import io.github.Project.engine.main.GameMaster;

/**
 * Abstract base class for all game scenes/screens.
 * Implements LibGDX's Screen interface for proper lifecycle management.
 */
public abstract class Scene implements Screen {
    protected GameMaster gameMaster;
    protected int width;
    protected int height;
    
    /**
     * Creates a new scene.
     * @param gameMaster Reference to the game master
     */
    public Scene(GameMaster gameMaster) {
        this.gameMaster = gameMaster;
    }
    
    /**
     * Called when this screen becomes the current screen.
     * Use this to initialize resources.
     */
    @Override
    public abstract void show();
    
    /**
     * Called when the screen should render itself.
     * @param delta The time in seconds since the last render.
     */
    @Override
    public abstract void render(float delta);
    
    /**
     * Called when the window is resized.
     * @param width New width
     * @param height New height
     */
    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    /**
     * Called when the game is paused.
     */
    @Override
    public void pause() {
        // Override in subclass if needed
    }
    
    /**
     * Called when the game is resumed from a paused state.
     */
    @Override
    public void resume() {
        // Override in subclass if needed
    }
    
    /**
     * Called when this screen is no longer the current screen.
     */
    @Override
    public void hide() {
        // Override in subclass if needed
    }
    
    /**
     * Called when this screen should release all resources.
     */
    @Override
    public abstract void dispose();
    
    // Getters
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
