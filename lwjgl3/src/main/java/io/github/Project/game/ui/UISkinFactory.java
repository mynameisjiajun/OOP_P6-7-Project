package io.github.Project.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

/**
 * PATTERN: Abstract Factory (applied as a static Factory)
 *
 * All three UI scenes — MainMenuScene, PauseScene, and OptionsScene — had an
 * identical private createSkin() method (~20 lines each), duplicated verbatim.
 * This factory centralises that logic so:
 *   - A single change here is reflected in every scene automatically.
 *   - Scenes no longer need to import Pixmap / BitmapFont / TextButton.TextButtonStyle.
 *   - The slider variant used only by OptionsScene is kept as a separate method,
 *     so OptionsScene composes the two skins: base + withSlider().
 *
 * The "Abstract Factory" framing here is that this class defines a family of
 * related UI products (base skin, slider extension, label styles) produced
 * together — the scene picks the product family it needs.
 *
 * Usage:
 *   // MainMenuScene / PauseScene:
 *   Skin skin = UISkinFactory.createSpaceSkin(BTN_UP, BTN_OVER, BTN_DOWN);
 *
 *   // OptionsScene (needs slider controls in addition):
 *   Skin skin = UISkinFactory.createSpaceSkin(BTN_UP, BTN_OVER, BTN_DOWN);
 *   UISkinFactory.addSliderStyle(skin, ACCENT_COLOR);
 */
public class UISkinFactory {

    private UISkinFactory() {
        // Static utility class — not instantiated
    }

    /**
     * Builds the base space-themed Skin shared by all UI scenes.
     * Includes: default BitmapFont, white 1×1 Texture, and TextButton style.
     *
     * @param btnUp   Background color for unpressed button state
     * @param btnOver Background color for hovered button state
     * @param btnDown Background color for pressed button state
     * @return a fully configured Skin ready for use with Stage widgets
     */
    public static Skin createSpaceSkin(Color btnUp, Color btnOver, Color btnDown) {
        Skin skin = new Skin();

        // ── Font ────────────────────────────────────────────────────────────
        BitmapFont font = new BitmapFont();
        font.getData().setScale(1.1f);
        skin.add("default", font);

        // ── 1×1 white Pixmap (used as coloured drawable source) ─────────────
        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(Color.WHITE);
        px.fill();
        skin.add("white", new Texture(px));
        px.dispose(); // Safe to dispose after Texture takes ownership

        // ── Button style ─────────────────────────────────────────────────────
        TextButton.TextButtonStyle btn = new TextButton.TextButtonStyle();
        btn.up        = skin.newDrawable("white", btnUp);
        btn.over      = skin.newDrawable("white", btnOver);
        btn.down      = skin.newDrawable("white", btnDown);
        btn.font      = font;
        btn.fontColor = Color.WHITE;
        skin.add("default", btn);

        return skin;
    }

    /**
     * Extends an existing Skin with a horizontal Slider style.
     * Called additionally by OptionsScene on top of the base skin.
     *
     * @param skin        the Skin to extend (must already contain "white" drawable)
     * @param accentColor the fill colour for the portion of the track before the knob
     */
    public static void addSliderStyle(Skin skin, Color accentColor) {
        // ── Knob ─────────────────────────────────────────────────────────────
        Pixmap knobPix = new Pixmap(20, 20, Pixmap.Format.RGBA8888);
        knobPix.setColor(Color.WHITE);
        knobPix.fill();
        skin.add("knob", new Texture(knobPix));
        knobPix.dispose();

        // ── Track background ─────────────────────────────────────────────────
        Pixmap trackPix = new Pixmap(1, 10, Pixmap.Format.RGBA8888);
        trackPix.setColor(new Color(0.15f, 0.15f, 0.25f, 1f));
        trackPix.fill();
        skin.add("track", new Texture(trackPix));
        trackPix.dispose();

        // ── Track fill (accent colour, left of knob) ─────────────────────────
        Pixmap fillPix = new Pixmap(1, 10, Pixmap.Format.RGBA8888);
        fillPix.setColor(accentColor);
        fillPix.fill();
        skin.add("trackFill", new Texture(fillPix));
        fillPix.dispose();

        // ── Assemble SliderStyle ─────────────────────────────────────────────
        Slider.SliderStyle sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = skin.newDrawable("track");
        sliderStyle.knob       = skin.newDrawable("knob");
        sliderStyle.knobBefore = skin.newDrawable("trackFill");
        skin.add("default-horizontal", sliderStyle);
    }
}
