package io.github.Project.engine.interfaces;

/**
 * Interface for audio output handling.
 * Defines methods for playing sounds and music.
 */
public interface AudioOutput {
    /**
     * Plays a sound effect.
     * @param soundEffect The sound effect identifier
     */
    void playSoundEffect(String soundEffect);
    
    /**
     * Sets the background music.
     * @param backgroundMusic The background music identifier
     */
    void setBackgroundMusic(String backgroundMusic);
    
    /**
     * Sets the music volume.
     * @param volume Volume level (0.0 to 1.0)
     */
    void setVolume(float volume);
    
    /**
     * Gets the current volume.
     * @return Volume level as float
     */
    float getVolume();
    
    /**
     * Checks if music is muted.
     * @return true if muted, false otherwise
     */
    boolean isMuted();
    
    /**
     * Sets the mute state.
     * @param muted true to mute, false to unmute
     */
    void setMuted(boolean muted);
}
