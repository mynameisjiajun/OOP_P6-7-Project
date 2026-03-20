package io.github.Project.game.scenes;

import io.github.Project.engine.scenes.Scene;
import io.github.Project.engine.main.GameMaster;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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

import java.util.Random;

/**
 * Options/Settings scene — space-themed.
 */
public class OptionsScene extends Scene {

    private static final Color BG_COLOR    = new Color(0.02f, 0.02f, 0.08f, 1f);
    private static final Color BTN_UP      = new Color(0.08f, 0.12f, 0.28f, 1f);
    private static final Color BTN_OVER    = new Color(0.80f, 0.40f, 0.05f, 1f);
    private static final Color BTN_DOWN    = new Color(0.50f, 0.25f, 0.02f, 1f);
    private static final Color TITLE_COLOR = new Color(1.00f, 0.60f, 0.10f, 1f);
    private static final Color ACCENT      = new Color(1.00f, 0.55f, 0.05f, 1f);

    private Stage              stage;
    private Skin               skin;
    private OrthographicCamera uiCamera;
    private float[]            stars;

    public OptionsScene(GameMaster gameMaster) {
        super(gameMaster);
    }

    @Override
    public void show() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        uiCamera = new OrthographicCamera(w, h);
        uiCamera.position.set(w / 2f, h / 2f, 0);
        uiCamera.update();

        stars = generateStars((int) w, (int) h, 110);

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = createSkin();

        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = skin.getFont("default");
        titleStyle.fontColor = TITLE_COLOR;
        skin.add("title", titleStyle);

        Label.LabelStyle defaultStyle = new Label.LabelStyle();
        defaultStyle.font = skin.getFont("default");
        defaultStyle.fontColor = Color.WHITE;
        skin.add("default", defaultStyle);

        Label titleLabel = new Label("OPTIONS", skin, "title");
        titleLabel.setFontScale(2f);

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

        final boolean isMuted = gameMaster.getAudioManager().isMuted();
        final TextButton muteButton = new TextButton(isMuted ? "UNMUTE" : "MUTE", skin);
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
                gameMaster.getSceneManager().setState(new MainMenuScene(gameMaster));
            }
        });

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

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(BG_COLOR.r, BG_COLOR.g, BG_COLOR.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        ShapeRenderer sr = gameMaster.getSharedShapeRenderer();
        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(1f, 1f, 1f, 1f);
        for (int i = 0; i < stars.length; i += 3) {
            sr.circle(stars[i], stars[i + 1], stars[i + 2]);
        }
        sr.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (stage != null) stage.getViewport().update(width, height, true);
        if (uiCamera != null) {
            uiCamera.viewportWidth  = width;
            uiCamera.viewportHeight = height;
            uiCamera.position.set(width / 2f, height / 2f, 0);
            uiCamera.update();
        }
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin  != null) skin.dispose();
    }

    private Skin createSkin() {
        Skin s = new Skin();
        BitmapFont font = new BitmapFont();
        font.getData().setScale(1.1f);
        s.add("default", font);

        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(Color.WHITE);
        px.fill();
        s.add("white", new Texture(px));
        px.dispose();

        TextButton.TextButtonStyle btn = new TextButton.TextButtonStyle();
        btn.up   = s.newDrawable("white", BTN_UP);
        btn.over = s.newDrawable("white", BTN_OVER);
        btn.down = s.newDrawable("white", BTN_DOWN);
        btn.font      = font;
        btn.fontColor = Color.WHITE;
        s.add("default", btn);

        // Slider style — orange fill, white knob
        Pixmap knobPix = new Pixmap(20, 20, Pixmap.Format.RGBA8888);
        knobPix.setColor(Color.WHITE);
        knobPix.fill();
        s.add("knob", new Texture(knobPix));
        knobPix.dispose();

        Pixmap trackPix = new Pixmap(1, 10, Pixmap.Format.RGBA8888);
        trackPix.setColor(new Color(0.15f, 0.15f, 0.25f, 1f));
        trackPix.fill();
        s.add("track", new Texture(trackPix));
        trackPix.dispose();

        Pixmap fillPix = new Pixmap(1, 10, Pixmap.Format.RGBA8888);
        fillPix.setColor(ACCENT);
        fillPix.fill();
        s.add("trackFill", new Texture(fillPix));
        fillPix.dispose();

        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background  = s.newDrawable("track");
        sliderStyle.knob        = s.newDrawable("knob");
        sliderStyle.knobBefore  = s.newDrawable("trackFill");
        s.add("default-horizontal", sliderStyle);

        return s;
    }

    private float[] generateStars(int w, int h, int count) {
        Random rng = new Random(77L);
        float[] arr = new float[count * 3];
        for (int i = 0; i < count; i++) {
            arr[i * 3]     = rng.nextFloat() * w;
            arr[i * 3 + 1] = rng.nextFloat() * h;
            arr[i * 3 + 2] = 0.5f + rng.nextFloat() * 1.5f;
        }
        return arr;
    }
}
