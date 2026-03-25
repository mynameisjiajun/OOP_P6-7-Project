package io.github.Project.game.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.Project.engine.core.GameMaster;
import io.github.Project.engine.core.Scene;
import io.github.Project.game.ui.UISkinFactory;

import java.util.Random;

/**
 * Abstract base for all UI scenes that share a star-field background,
 * a Scene2D stage, and a UISkin. Eliminates duplicate boilerplate across
 * MainMenuScene, PauseScene, GameOverScene, and WinScene.
 *
 * Subclasses only need to implement {@link #buildUI()} and provide
 * colour constants via constructor.
 */
public abstract class StarBackgroundScene extends Scene {

    protected Stage              stage;
    protected Skin               skin;
    protected OrthographicCamera uiCamera;

    private final Color bgColor;
    private final Color btnUp;
    private final Color btnOver;
    private final Color btnDown;
    private final Color titleColor;
    private final Color starColor;
    private final long  starSeed;
    private final int   starCount;

    private float[] stars;

    protected StarBackgroundScene(GameMaster gameMaster,
                                  Color bgColor, Color btnUp, Color btnOver, Color btnDown,
                                  Color titleColor, Color starColor,
                                  long starSeed, int starCount) {
        super(gameMaster);
        this.bgColor    = bgColor;
        this.btnUp      = btnUp;
        this.btnOver    = btnOver;
        this.btnDown    = btnDown;
        this.titleColor = titleColor;
        this.starColor  = starColor;
        this.starSeed   = starSeed;
        this.starCount  = starCount;
    }

    // ── Template method ──────────────────────────────────────────────────

    @Override
    public void show() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        uiCamera = new OrthographicCamera(w, h);
        uiCamera.position.set(w / 2f, h / 2f, 0);
        uiCamera.update();

        stars = generateStars((int) w, (int) h);

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = UISkinFactory.createSpaceSkin(btnUp, btnOver, btnDown);
        addLabelStyle("title",   titleColor);
        addLabelStyle("default", Color.WHITE);

        buildUI();
    }

    /** Subclasses create their labels, buttons, and table layout here. */
    protected abstract void buildUI();

    // ── Shared helpers ───────────────────────────────────────────────────

    /** Add a label style to the skin with the given name and colour. */
    protected void addLabelStyle(String name, Color color) {
        Label.LabelStyle style = new Label.LabelStyle();
        style.font      = skin.getFont("default");
        style.fontColor = color;
        skin.add(name, style);
    }

    // ── Rendering ────────────────────────────────────────────────────────

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(bgColor.r, bgColor.g, bgColor.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        ShapeRenderer sr = gameMaster.getSharedShapeRenderer();
        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(starColor.r, starColor.g, starColor.b, 1f);
        for (int i = 0; i < stars.length; i += 3)
            sr.circle(stars[i], stars[i + 1], stars[i + 2]);
        sr.end();

        stage.act(delta);
        stage.draw();
    }

    // ── Lifecycle ────────────────────────────────────────────────────────

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        stars = generateStars(width, height);
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

    // ── Star generation ──────────────────────────────────────────────────

    private float[] generateStars(int w, int h) {
        Random rng = new Random(starSeed);
        float[] arr = new float[starCount * 3];
        for (int i = 0; i < starCount; i++) {
            arr[i * 3]     = rng.nextFloat() * w;
            arr[i * 3 + 1] = rng.nextFloat() * h;
            arr[i * 3 + 2] = 0.5f + rng.nextFloat() * 1.5f;
        }
        return arr;
    }
}
