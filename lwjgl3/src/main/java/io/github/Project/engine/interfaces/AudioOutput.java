package io.github.Project.engine.interfaces;

// Interface for audio output: sound effects, music, volume, and mute control.
public interface AudioOutput {
    void playSoundEffect(String soundEffect);
    void setBackgroundMusic(String backgroundMusic);
    void setVolume(float volume);
    float getVolume();
    boolean isMuted();
    void setMuted(boolean muted);
}
