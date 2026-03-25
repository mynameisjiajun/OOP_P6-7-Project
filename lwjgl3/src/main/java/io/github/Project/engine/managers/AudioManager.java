package io.github.Project.engine.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.ObjectMap;
import io.github.Project.engine.interfaces.AudioOutput;

public class AudioManager implements AudioOutput {

    // music file paths
    public static final String MUSIC_BACKGROUND = "audio/music/backgroundmusic.mp3";
    public static final String MUSIC_GAMEPLAY = "audio/music/gameplay.mp3";
    public static final String MUSIC_MENU = "audio/music/menu.mp3";

    // sound effect file paths
    public static final String SFX_UI_CLICK = "audio/sfx/uiclick.wav";
    public static final String SFX_COLLISION = "audio/sfx/collisionsound.wav";
    public static final String SFX_ROCKET = "audio/sfx/rocket.wav";
    public static final String SFX_REFUEL = "audio/sfx/refuel.wav";
    public static final String SFX_WIN = "audio/sfx/win.wav";

    private final ObjectMap<String, Sound> soundCache = new ObjectMap<>();
    private final ObjectMap<String, Music> musicCache = new ObjectMap<>();

    private Music currentMusic;
    private float volume = 1.0f;
    private boolean muted = false;

    /** Rocket loop is louder than other SFX to cut through the music. */
    private static final float ROCKET_LOOP_VOLUME_MULTIPLIER = 6.0f;

    // used for looping rocket sound
    private Sound rocketLoopSound;
    private long rocketLoopId = -1L;
    private boolean rocketLoopPlaying = false;

    @Override
    public void playSoundEffect(String soundEffectPath) {
        if (muted) return;
        if (!Gdx.files.internal(soundEffectPath).exists()) return;

        Sound sound = soundCache.get(soundEffectPath);
        if (sound == null) {
            sound = Gdx.audio.newSound(Gdx.files.internal(soundEffectPath));
            soundCache.put(soundEffectPath, sound);
        }

        sound.play(volume);
    }

    public void loadMusic(String name, String filePath) {
        if (!Gdx.files.internal(filePath).exists()) return;

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

        if (rocketLoopSound != null && rocketLoopPlaying && !muted) {
            rocketLoopSound.setVolume(rocketLoopId, this.volume);
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

        if (rocketLoopSound != null && rocketLoopPlaying) {
            rocketLoopSound.setVolume(rocketLoopId, muted ? 0f : volume);
        }
    }

    public void playUIClick() {
        playSoundEffect(SFX_UI_CLICK);
    }

    public void playCollisionSound() {
        playSoundEffect(SFX_COLLISION);
    }

    public void playRefuelSound() {
        playSoundEffect(SFX_REFUEL);
    }

    public void playWinSound() {
        playSoundEffect(SFX_WIN);
    }

    // starts rocket sound only once even if called every frame
    public void playRocketLoop() {
        if (muted) return;
        if (rocketLoopPlaying) return;
        if (!Gdx.files.internal(SFX_ROCKET).exists()) return;

        if (rocketLoopSound == null) {
            rocketLoopSound = Gdx.audio.newSound(Gdx.files.internal(SFX_ROCKET));
        }

        rocketLoopId = rocketLoopSound.loop(volume * ROCKET_LOOP_VOLUME_MULTIPLIER);
        rocketLoopPlaying = true;
    }

    public void stopRocketLoop() {
        if (rocketLoopSound != null && rocketLoopPlaying) {
            rocketLoopSound.stop(rocketLoopId);
            rocketLoopPlaying = false;
            rocketLoopId = -1L;
        }
    }

    public boolean isRocketLoopPlaying() {
        return rocketLoopPlaying;
    }

    public void startDefaultBackgroundMusic() {
        setBackgroundMusic(MUSIC_BACKGROUND);
    }

    public void startGameplayMusic() {
        setBackgroundMusic(MUSIC_GAMEPLAY);
    }

    public void startMenuMusic() {
        setBackgroundMusic(MUSIC_MENU);
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
    }

    public void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
        }
    }

    public void resumeMusic() {
        if (currentMusic != null && !currentMusic.isPlaying()) {
            currentMusic.play();
        }
    }

    // used when pausing or leaving gameplay
    public void pauseAllAudio() {
        pauseMusic();
        stopRocketLoop();
    }

    public void stopAllAudio() {
        stopMusic();
        stopRocketLoop();
    }

    public void dispose() {
        stopAllAudio();
        currentMusic = null;

        if (rocketLoopSound != null) {
            rocketLoopSound.dispose();
            rocketLoopSound = null;
        }

        for (Sound s : soundCache.values()) {
            s.dispose();
        }
        soundCache.clear();

        for (Music m : musicCache.values()) {
            m.dispose();
        }
        musicCache.clear();
    }

    /**
     * Checks if the game over sound is currently playing.
     */
    public boolean isGameOverSoundPlaying() {
        // Placeholder logic: Since there is no game-over sound tracked yet, safely return false.
        return false;
    }
}