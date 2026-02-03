package io.github.Project.engine.interfaces;

/**
 * Interface for input movement handling.
 * Defines methods for processing user input.
 */
public interface InputMovement {
    /**
     * Checks if a key is currently pressed.
     * @param keyCode The key code to check
     * @return true if the key is pressed, false otherwise
     */
    boolean keyDown(int keyCode);
    
    /**
     * Checks if the screen/button is being touched.
     * @param screenX Screen X coordinate
     * @param screenY Screen Y coordinate
     * @param pointer Pointer for the event
     * @param button Button index
     * @return true if touched, false otherwise
     */
    boolean touchDown(int screenX, int screenY, int pointer, int button);
    
    /**
     * Checks if the touch has been released.
     * @param screenX Screen X coordinate
     * @param screenY Screen Y coordinate
     * @param pointer Pointer for the event
     * @param button Button index
     * @return true if released, false otherwise
     */
    boolean touchUp(int screenX, int screenY, int pointer, int button);
    
    /**
     * Checks if a key is currently being pressed.
     * @param keyCode The key code to check
     * @return true if key is pressed, false otherwise
     */
    boolean keyTyped(char keyCode);
    
    /**
     * Handles the scrolling event.
     * @param amountX Amount scrolled in X direction
     * @param amountY Amount scrolled in Y direction
     * @return true if handled, false otherwise
     */
    boolean scrolled(float amountX, float amountY);
}
