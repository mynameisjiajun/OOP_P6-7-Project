package io.github.Project.engine.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;

/**
 * Manages input/output operations.
 * Handles keyboard, mouse, and touch input.
 */
public class IOManager implements InputProcessor {
    private boolean[] keys;
    private int mouseX;
    private int mouseY;
    private boolean mousePressed;
    
    /**
     * Creates a new IOManager.
     */
    public IOManager() {
        keys = new boolean[256];
        Gdx.input.setInputProcessor(this);
    }
    
    /**
     * Checks if a key is currently pressed.
     * @param keycode The key code to check
     * @return true if pressed, false otherwise
     */
    public boolean isKeyPressed(int keycode) {
        if (keycode >= 0 && keycode < keys.length) {
            return keys[keycode];
        }
        return false;
    }
    
    /**
     * Gets the mouse X position.
     * @return Mouse X coordinate
     */
    public int getMouseX() {
        return mouseX;
    }
    
    /**
     * Gets the mouse Y position.
     * @return Mouse Y coordinate
     */
    public int getMouseY() {
        return mouseY;
    }
    
    /**
     * Checks if the mouse button is pressed.
     * @return true if pressed, false otherwise
     */
    public boolean isMousePressed() {
        return mousePressed;
    }
    
    @Override
    public boolean keyDown(int keycode) {
        if (keycode >= 0 && keycode < keys.length) {
            keys[keycode] = true;
        }
        return true;
    }
    
    @Override
    public boolean keyUp(int keycode) {
        if (keycode >= 0 && keycode < keys.length) {
            keys[keycode] = false;
        }
        return true;
    }
    
    @Override
    public boolean keyTyped(char character) {
        return false;
    }
    
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        mouseX = screenX;
        mouseY = screenY;
        mousePressed = true;
        return true;
    }
    
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        mouseX = screenX;
        mouseY = screenY;
        mousePressed = false;
        return true;
    }
    
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        mouseX = screenX;
        mouseY = screenY;
        return true;
    }
    
    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        mousePressed = false;
        return true;
    }
    
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        mouseX = screenX;
        mouseY = screenY;
        return true;
    }
    
    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
