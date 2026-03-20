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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.Random;

/**
 * Main menu scene - space-themed entry screen.
 */
public class MainMenuScene extends Scene {

    // ── Space colour palette ──
    private static final Color BG_COLOR      = new Color(0.02f, 0.02f, 0.08f, 1f);
    private static final Color BTN_UP        = new Color(0.08f, 0.12f, 0.28f, 1f);
    private static final Color BTN_OVER      = new Color(0.80f, 0.40f, 0.05f, 1f);
    private static final Color BTN_DOWN      = new Color(0.50f, 0.25f, 0.02f, 1f);
    private static final Color TITLE_COLOR   = new Color(1.00f, 0.60f, 0.10f, 1f);

    private Stage             stage;
    private Skin              skin;
    private OrthographicCamera uiCamera;

    // Stars: x, y, radius stored flat [x0,y0,r0, x1,y1,r1, ...]
    private float[] stars;

    public MainMenuScene(GameMaster gameMaster) {
        super(gameMaster);
    }

    @Override
    public void show() {
        gameMaster.getAudioManager().startDefaultBackgroundMusic();

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        // Screen-space camera for star drawing
        uiCamera = new OrthographicCamera(w, h);
        uiCamera.position.set(w / 2f, h / 2f, 0);
        uiCamera.update();

        // Generate random star field (fixed seed = same every run)
        stars = generateStars((int) w, (int) h, 120);

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = createSkin();

        // Title label
        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = skin.getFont("default");
        titleStyle.fontColor = TITLE_COLOR;
        skin.add("title", titleStyle);

        // Normal label style
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = skin.getFont("default");
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        Label titleLabel = new Label("ROCKET JOURNEY", skin, "title");
        titleLabel.setFontScale(2.5f);

        Label subLabel = new Label("reach the moon", skin);
        subLabel.setFontScale(1f);

        TextButton playButton    = new TextButton("LAUNCH",  skin);
        TextButton optionsButton = new TextButton("OPTIONS", skin);
        TextButton exitButton    = new TextButton("EXIT",    skin);

        playButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                gameMaster.getAudioManager().playUIClick();
                gameMaster.getSceneManager().setState(new PlayScene(gameMaster));
            }
        });
        optionsButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                gameMaster.getAudioManager().playUIClick();
                gameMaster.getSceneManager().setState(new OptionsScene(gameMaster));
            }
        });
        exitButton.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                gameMaster.getAudioManager().playUIClick();
                Gdx.app.exit();
            }
        });

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.add(titleLabel).padBottom(8).row();
        table.add(subLabel).padBottom(60).row();
        table.add(playButton).width(220).height(60).padBottom(18).row();
        table.add(optionsButton).width(220).height(60).padBottom(18).row();
        table.add(exitButton).width(220).height(60).row();

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        // Deep space background
        Gdx.gl.glClearColor(BG_COLOR.r, BG_COLOR.g, BG_COLOR.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Star field
        ShapeRenderer sr = gameMaster.getSharedShapeRenderer();
        sr.setProjectionMatrix(uiCamera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(1f, 1f, 1f, 1f);
        for (int i = 0; i < stars.length; i += 3) {
            sr.circle(stars[i], stars[i + 1], stars[i + 2]);
        }
        sr.end();

        // Rocket sprite decoration (top-right corner)
        SpriteBatch batch = gameMaster.getSharedBatch();
        batch.setProjectionMatrix(uiCamera.combined);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (stage   != null) stage.getViewport().update(width, height, true);
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

    // ── Helpers ──────────────────────────────────
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

        return s;
    }

    private float[] generateStars(int w, int h, int count) {
        Random rng = new Random(42L);
        float[] arr = new float[count * 3];
        for (int i = 0; i < count; i++) {
            arr[i * 3]     = rng.nextFloat() * w;
            arr[i * 3 + 1] = rng.nextFloat() * h;
            arr[i * 3 + 2] = 0.5f + rng.nextFloat() * 1.5f; // radius 0.5–2
        }
        return arr;
    }
}
