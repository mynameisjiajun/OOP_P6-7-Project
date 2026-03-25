package io.github.Project.game.scenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.github.Project.engine.core.GameMaster;
import io.github.Project.game.ui.UISkinFactory;

/**
 * Options/Settings scene — space-themed.
 * Extends the base skin with a slider style for volume control.
 */
public class OptionsScene extends StarBackgroundScene {

    public enum BackTarget { MAIN_MENU, PAUSE_MENU }

    private static final Color BG     = new Color(0.02f, 0.02f, 0.08f, 1f);
    private static final Color UP     = new Color(0.08f, 0.12f, 0.28f, 1f);
    private static final Color OVER   = new Color(0.80f, 0.40f, 0.05f, 1f);
    private static final Color DOWN   = new Color(0.50f, 0.25f, 0.02f, 1f);
    private static final Color TITLE  = new Color(1.00f, 0.60f, 0.10f, 1f);
    private static final Color ACCENT = new Color(1.00f, 0.55f, 0.05f, 1f);

    private final BackTarget backTarget;

    public OptionsScene(GameMaster gameMaster) {
        this(gameMaster, BackTarget.MAIN_MENU);
    }

    public OptionsScene(GameMaster gameMaster, BackTarget backTarget) {
        super(gameMaster, BG, UP, OVER, DOWN, TITLE, Color.WHITE, 77L, 110);
        this.backTarget = backTarget;
    }

    @Override
    protected void buildUI() {
        // OptionsScene also needs the slider style
        UISkinFactory.addSliderStyle(skin, ACCENT);

        float currentVolume = gameMaster.getAudioManager().getVolume();
        final Label volumeLabel = new Label("Volume: " + (int)(currentVolume * 100) + "%", skin);

        Slider volumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
        volumeSlider.setValue(currentVolume);
        volumeSlider.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                float val = ((Slider) actor).getValue();
                gameMaster.getAudioManager().setVolume(val);
                volumeLabel.setText("Volume: " + (int)(val * 100) + "%");
            }
        });

        final TextButton muteButton = new TextButton(gameMaster.getAudioManager().isMuted() ? "UNMUTE" : "MUTE", skin);
        muteButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                boolean nowMuted = !gameMaster.getAudioManager().isMuted();
                gameMaster.getAudioManager().setMuted(nowMuted);
                muteButton.setText(nowMuted ? "UNMUTE" : "MUTE");
            }
        });

        TextButton backButton = new TextButton("BACK", skin);
        backButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                gameMaster.getAudioManager().playUIClick();
                if (backTarget == BackTarget.PAUSE_MENU) {
                    gameMaster.getSceneManager().setState(new PauseScene(gameMaster));
                } else {
                    gameMaster.getSceneManager().setState(new MainMenuScene(gameMaster));
                }
            }
        });

        Label titleLabel = new Label("OPTIONS", skin, "title");
        titleLabel.setFontScale(2f);

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.add(titleLabel).padBottom(50).colspan(2).row();
        table.add(volumeLabel).padBottom(10).colspan(2).row();
        table.add(volumeSlider).width(300).padBottom(10).colspan(2).row();
        table.add(muteButton).width(200).height(50).padBottom(40).colspan(2).row();
        table.add(backButton).width(200).height(60).colspan(2).row();

        stage.addActor(table);
    }
}
