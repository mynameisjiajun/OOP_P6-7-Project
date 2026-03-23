package io.github.Project.game.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.Project.engine.main.GameMaster;
import io.github.Project.engine.scenes.Scene;
import io.github.Project.game.ui.UISkinFactory;

import java.util.Random;

/**
 * Options/Settings scene — space-themed.
 *
 * CHANGES:
 * 1. Replaced the private createSkin() method (~40 lines including slider code)
 *    with two factory calls:
 *      - UISkinFactory.createSpaceSkin() builds the base skin (buttons + font)
 *      - UISkinFactory.addSliderStyle()  extends it with the slider style
 *    OptionsScene is the only scene that needs the slider, so the extension
 *    call is made here rather than in the base skin.
 *
 * 2. Removed now-unused imports: BitmapFont, Pixmap, Texture.
 *
 * NOTE: generateStars() keeps java.util.Random with a fixed seed (77L).
 */
public class OptionsScene extends Scene {
    public enum BackTarget {
        MAIN_MENU,
        PAUSE_MENU
    }

    // ── Space colour palette ─────────────────────────────────────────────────
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
    private final BackTarget   backTarget;

    public OptionsScene(GameMaster gameMaster) {
        this(gameMaster, BackTarget.MAIN_MENU);
    }

    public OptionsScene(GameMaster gameMaster, BackTarget backTarget) {
        super(gameMaster);
        this.backTarget = backTarget;
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

        // ── Skin (via factory) ───────────────────────────────────────────────
        skin = UISkinFactory.createSpaceSkin(BTN_UP, BTN_OVER, BTN_DOWN);
        // OptionsScene also needs the slider style — extend the base skin
        UISkinFactory.addSliderStyle(skin, ACCENT);

        // ── Label styles ─────────────────────────────────────────────────────
        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font      = skin.getFont("default");
        titleStyle.fontColor = TITLE_COLOR;
        skin.add("title", titleStyle);

        Label.LabelStyle defaultStyle = new Label.LabelStyle();
        defaultStyle.font      = skin.getFont("default");
        defaultStyle.fontColor = Color.WHITE;
        skin.add("default", defaultStyle);

        // ── Widgets ───────────────────────────────────────────────────────────
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
                if (backTarget == BackTarget.PAUSE_MENU) {
                    gameMaster.getSceneManager().setState(new PauseScene(gameMaster));
                } else {
                    gameMaster.getSceneManager().setState(new MainMenuScene(gameMaster));
                }
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
        if (stage    != null) stage.getViewport().update(width, height, true);
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

    // ── Helpers ───────────────────────────────────────────────────────────────

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
