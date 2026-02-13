package io.github.Project.game.scenes;

import io.github.Project.engine.scenes.Scene;
import io.github.Project.engine.main.GameMaster;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Options/Settings menu scene.
 * Allows players to configure volume via a slider.
 */
public class OptionsScene extends Scene {
    private Stage stage;
    private Skin skin;

    public OptionsScene(GameMaster gameMaster) {
        super(gameMaster);
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = createSkin();

        // --- Label style ---
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = skin.getFont("default");
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        // --- Title ---
        Label titleLabel = new Label("OPTIONS", skin);
        titleLabel.setFontScale(2);

        // --- Volume label + slider ---
        float currentVolume = gameMaster.getAudioManager().getVolume();
        final Label volumeLabel = new Label("Volume: " + (int)(currentVolume * 100) + "%", skin);

        Slider volumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
        volumeSlider.setValue(currentVolume);

        volumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float val = ((Slider) actor).getValue();
                gameMaster.getAudioManager().setVolume(val);
                volumeLabel.setText("Volume: " + (int)(val * 100) + "%");
            }
        });

        // --- Mute button ---
        final boolean isMuted = gameMaster.getAudioManager().isMuted();
        final TextButton muteButton = new TextButton(isMuted ? "Unmute" : "Mute", skin);
        muteButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                boolean nowMuted = !gameMaster.getAudioManager().isMuted();
                gameMaster.getAudioManager().setMuted(nowMuted);
                muteButton.setText(nowMuted ? "Unmute" : "Mute");
            }
        });

        // --- Back button ---
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameMaster.getSceneManager().setState(new MainMenuScene(gameMaster));
            }
        });

        // --- Layout ---
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

    private Skin createSkin() {
        Skin skin = new Skin();
        BitmapFont font = new BitmapFont();
        skin.add("default", font);

        // 1x1 white pixel used for all drawables
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));
        pixmap.dispose();

        // --- Button style ---
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
        btnStyle.down = skin.newDrawable("white", Color.GRAY);
        btnStyle.over = skin.newDrawable("white", Color.LIGHT_GRAY);
        btnStyle.font = font;
        btnStyle.fontColor = Color.WHITE;
        skin.add("default", btnStyle);

        // --- Slider style (programmatic, no textures needed) ---
        // Knob: small square the user drags
        Pixmap knobPix = new Pixmap(20, 20, Pixmap.Format.RGBA8888);
        knobPix.setColor(Color.WHITE);
        knobPix.fill();
        skin.add("knob", new Texture(knobPix));
        knobPix.dispose();

        // Track background
        Pixmap trackPix = new Pixmap(1, 10, Pixmap.Format.RGBA8888);
        trackPix.setColor(Color.GRAY);
        trackPix.fill();
        skin.add("track", new Texture(trackPix));
        trackPix.dispose();

        // Filled portion before knob
        Pixmap beforePix = new Pixmap(1, 10, Pixmap.Format.RGBA8888);
        beforePix.setColor(Color.CYAN);
        beforePix.fill();
        skin.add("trackBefore", new Texture(beforePix));
        beforePix.dispose();

        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = skin.newDrawable("track");
        sliderStyle.knob = skin.newDrawable("knob");
        sliderStyle.knobBefore = skin.newDrawable("trackBefore");
        skin.add("default-horizontal", sliderStyle);

        return skin;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
    }
}
