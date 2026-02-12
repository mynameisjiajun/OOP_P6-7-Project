package io.github.Project.engine.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.ObjectMap;
import io.github.Project.engine.interfaces.AudioOutput;

/**
 * AudioManager with support for multiple background tracks. Handles sound
 * effects, multiple music tracks, volume, and mute control.
 */
public class AudioManager implements AudioOutput {

    public static final String MUSIC_BACKGROUND = "music/backgroundmusic.mp3";
    public static final String SFX_UI_CLICK = "sounds/uiclick.wav";
    public static final String SFX_COLLISION = "sounds/collisionsound.wav";

    private final ObjectMap<String, Sound> soundCache = new ObjectMap<>();
    private final ObjectMap<String, Music> musicCache = new ObjectMap<>();
    private Music currentMusic;
    private float volume = 1.0f;
    private boolean muted = false;

    @Override
    public void playSoundEffect(String soundEffectPath) {
        if (muted) return;

        Sound sound = soundCache.get(soundEffectPath);
        if (sound == null) {
            sound = Gdx.audio.newSound(Gdx.files.internal(soundEffectPath));
            soundCache.put(soundEffectPath, sound);
        }
        sound.play(volume);
    }

    public void loadMusic(String name, String filePath) {
        Music old = musicCache.get(name);
        if (old != null) {
            old.stop();
            old.dispose();
        }

        Music music = Gdx.audio.newMusic(Gdx.files.internal(filePath));
        musicCache.put(name, music);
    }

    public void playMusic(String name, boolean loop) {
        if (currentMusic != null) {
            currentMusic.stop();
        }
        currentMusic = musicCache.get(name);
        if (currentMusic != null) {
            currentMusic.setLooping(loop);
            currentMusic.setVolume(muted ? 0f : volume);
            currentMusic.play();
        }
    }

    @Override
    public void setBackgroundMusic(String backgroundMusicPath) {
        loadMusic("default", backgroundMusicPath);
        playMusic("default", true);
    }

    @Override
    public void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(1f, volume));
        if (currentMusic != null && !muted) {
            currentMusic.setVolume(this.volume);
        }
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public boolean isMuted() {
        return muted;
    }

    @Override
    public void setMuted(boolean muted) {
        this.muted = muted;
        if (currentMusic != null) {
            currentMusic.setVolume(muted ? 0f : volume);
        }
    }

    public void playUIClick() {
        playSoundEffect(SFX_UI_CLICK);
    }

    public void playCollisionSound() {
        playSoundEffect(SFX_COLLISION);
    }

    public void startDefaultBackgroundMusic() {
        setBackgroundMusic(MUSIC_BACKGROUND);
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
    }

    public void dispose() {
        stopMusic();
        currentMusic = null;

        for (Sound s : soundCache.values()) {
            s.dispose();
        }
        soundCache.clear();

        for (Music m : musicCache.values()) {
            m.dispose();
        }
        musicCache.clear();
    }
}

