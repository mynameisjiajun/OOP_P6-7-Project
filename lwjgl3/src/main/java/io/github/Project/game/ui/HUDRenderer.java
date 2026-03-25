package io.github.Project.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import io.github.Project.engine.core.GameMaster;
import io.github.Project.game.systems.collisionstrategies.RocketCollisionStrategy;
import io.github.Project.game.entities.Rocket;
import io.github.Project.game.entities.SpaceStation;
import io.github.Project.game.entities.HealthBar;
import io.github.Project.game.core.factory.DebrisFactory;

/**
 * Renders the in-game HUD: stats panel, station health bar,
 * Kessler warning, landing guide, repair message, control hints,
 * and fact/notification popups.
 */
public class HUDRenderer {

    // ── Notification constants ───────────────────────────────────────────
    private static final float NOTIF_DURATION = 5.5f;
    private static final float NOTIF_FADE_IN  = NOTIF_DURATION * 0.12f;
    private static final float NOTIF_FADE_OUT = NOTIF_DURATION * 0.20f;

    private final GameMaster    gameMaster;
    private final Rocket        rocket;
    private final SpaceStation  spaceStation;
    private final DebrisFactory debrisManager;

    private final BitmapFont  font;
    private final GlyphLayout glyphLayout;
    private final HealthBar   stationHealthBar;

    // ── Notification state ───────────────────────────────────────────────
    private String notifText  = null;
    private float  notifTimer = 0f;

    /** Shows a fact/toast notification at the bottom of the screen. */
    public void show(String text) {
        this.notifText  = text;
        this.notifTimer = NOTIF_DURATION;
    }

    public HUDRenderer(GameMaster gameMaster, Rocket rocket,
                       SpaceStation spaceStation,
                       DebrisFactory debrisManager,
                       HealthBar stationHealthBar) {
        this.gameMaster   = gameMaster;
        this.rocket       = rocket;
        this.spaceStation = spaceStation;
        this.debrisManager = debrisManager;
        this.font         = new BitmapFont();
        this.glyphLayout  = new GlyphLayout();
        this.stationHealthBar = stationHealthBar;
    }

    // ── Layout constants ─────────────────────────────────────────────────
    private static final float PANEL_WIDTH          = 230f;
    private static final float PANEL_HEIGHT         = 135f;
    private static final float HEALTH_BAR_WIDTH     = 300f;
    private static final float HEALTH_BAR_Y_OFFSET  = 28f;
    private static final float LANDING_GUIDE_Y      = 108f;
    private static final float LANDING_GUIDE_HEIGHT = 80f;
    private static final float LANDING_ALT_THRESHOLD = 500f;
    private static final float REPAIR_FADE_DIVISOR  = 0.75f; // 2.5f * 0.3f

    /**
     * Main HUD draw entry point. Delegates each visual concern to a focused
     * private method to keep cyclomatic complexity low.
     */
    public void draw(OrthographicCamera hudCamera,
                     float repairCooldownTimer,
                     float repairMessageTimer,
                     float stationRepairAmount) {

        float vW = hudCamera.viewportWidth;
        float vH = hudCamera.viewportHeight;

        boolean stationWarningActive = debrisManager.isStationWarningActive();
        float   stationWarningPulse  = debrisManager.getStationWarningPulse();
        boolean kesslerActive        = debrisManager.isKesslerActive();
        float   kesslerPulse         = debrisManager.getKesslerPulse();

        ShapeRenderer sr = gameMaster.getSharedShapeRenderer();
        sr.setProjectionMatrix(hudCamera.combined);
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        sr.begin(ShapeRenderer.ShapeType.Filled);

        drawStatsPanelBackground(sr, vH);
        drawStationHealthBarBackground(sr, vW, vH, stationWarningActive, stationWarningPulse);
        if (kesslerActive) drawKesslerBanner(sr, vW, vH, kesslerPulse);

        sr.end();

        SpriteBatch batch = gameMaster.getSharedBatch();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        drawStationHealthBarLabel(batch, vW, vH, stationWarningActive);
        if (kesslerActive) drawKesslerText(batch, vW, vH);
        drawStatsText(batch, vH, repairCooldownTimer);
        if (repairMessageTimer > 0f) drawRepairMessage(batch, vW, vH, repairMessageTimer, stationRepairAmount);
        drawNotification(batch, vW, Gdx.graphics.getDeltaTime());
        if (rocket.getPosY() < LANDING_ALT_THRESHOLD) drawLandingGuide(batch, sr);
        drawControlHints(batch);

        batch.end();
    }

    // ── Private draw helpers ─────────────────────────────────────────────

    /** Draws the semi-transparent background behind the top-left stats block. */
    private void drawStatsPanelBackground(ShapeRenderer sr, float vH) {
        sr.setColor(0f, 0f, 0f, 0.55f);
        sr.rect(0f, vH - PANEL_HEIGHT, PANEL_WIDTH, PANEL_HEIGHT);
    }

    /** Draws the station health bar track and optional warning pulse overlay. */
    private void drawStationHealthBarBackground(ShapeRenderer sr, float vW, float vH,
                                                boolean warningActive, float warningPulse) {
        float barW = HEALTH_BAR_WIDTH;
        float barX = (vW - barW) / 2f;
        float barY = vH - HEALTH_BAR_Y_OFFSET;
        sr.setColor(0f, 0f, 0f, 0.60f);
        sr.rect(barX - 8f, barY - 4f, barW + 16f, 40f);

        if (warningActive) {
            float pulse = (MathUtils.sin(warningPulse) + 1f) / 2f;
            sr.setColor(1f, 0.2f, 0f, 0.4f * pulse);
            sr.rect(barX - 8f, barY - 4f, barW + 16f, 40f);
        }

        float hp = spaceStation.getHealthPercentage();
        stationHealthBar.setPosX(barX);
        stationHealthBar.setPosY(barY);
        stationHealthBar.setHP(hp);
        stationHealthBar.render(null, sr);
    }

    /** Draws the pulsing red Kessler cascade banner across the screen centre. */
    private void drawKesslerBanner(ShapeRenderer sr, float vW, float vH, float kesslerPulse) {
        float pulse = (MathUtils.sin(kesslerPulse) + 1f) / 2f;
        sr.setColor(0.8f, 0f, 0f, 0.35f + 0.25f * pulse);
        sr.rect(0f, vH / 2f - 22f, vW, 44f);
    }

    /** Draws the station health bar label, switching to a warning string when threatened. */
    private void drawStationHealthBarLabel(SpriteBatch batch, float vW, float vH, boolean warningActive) {
        font.getData().setScale(0.75f);
        String label = warningActive ? "!! STATION UNDER THREAT !!" : "SPACE STATION HEALTH";
        font.setColor(warningActive ? new Color(1f, 0.35f, 0f, 1f) : Color.WHITE);
        glyphLayout.setText(font, label);
        font.draw(batch, label, (vW - glyphLayout.width) / 2f, vH - 31f);
        font.setColor(Color.WHITE);
    }

    /** Draws the "KESSLER CASCADE ACTIVE" warning text in the screen centre. */
    private void drawKesslerText(SpriteBatch batch, float vW, float vH) {
        font.getData().setScale(0.9f);
        font.setColor(1f, 0.25f, 0.1f, 1f);
        String msg = "!! KESSLER CASCADE ACTIVE !!";
        glyphLayout.setText(font, msg);
        font.draw(batch, msg, (vW - glyphLayout.width) / 2f, vH / 2f + 8f);
        font.setColor(Color.WHITE);
    }

    /** Draws the top-left stats block: altitude, score, orbit count, bowl, and pad status. */
    private void drawStatsText(SpriteBatch batch, float vH, float repairCooldownTimer) {
        font.getData().setScale(0.8f);
        font.setColor(Color.WHITE);
        font.draw(batch, "ALT:   " + (int) rocket.getPosY() + " m", 10f, vH - 15f);
        font.draw(batch, "SCORE: " + debrisManager.getDebrisCollected() + "/" + DebrisFactory.WIN_CLEAR_SCORE, 10f, vH - 35f);

        int inFlight = debrisManager.getFlying().size;
        font.setColor(orbitColor(inFlight));
        font.draw(batch, "ORBIT: " + inFlight + " debris", 10f, vH - 55f);

        int bowlCount = debrisManager.getAttached().size;
        font.setColor(bowlCount > 0 ? Color.CYAN : Color.LIGHT_GRAY);
        font.draw(batch, "BOWL:  " + bowlCount + "/" + DebrisFactory.MAX_BOWL_CAPACITY, 10f, vH - 75f);
        font.setColor(Color.WHITE);

        font.getData().setScale(0.65f);
        font.setColor(0.65f, 0.65f, 0.65f, 1f);
        font.draw(batch, "STATION: " + (int)(spaceStation.getHealthPercentage() * 100) + "% HP", 10f, vH - 95f);

        font.getData().setScale(0.8f);
        if (repairCooldownTimer > 0f) {
            font.setColor(1f, 0.6f, 0.1f, 1f);
            font.draw(batch, "PAD: ready in " + (int) Math.ceil(repairCooldownTimer) + "s", 10f, vH - 118f);
        } else {
            font.setColor(0.2f, 1f, 0.4f, 1f);
            font.draw(batch, "PAD: READY", 10f, vH - 118f);
        }
        font.setColor(Color.WHITE);
    }

    /** Draws the "STATION REPAIRED" pop-up message with fade-in alpha. */
    private void drawRepairMessage(SpriteBatch batch, float vW, float vH,
                                   float repairMessageTimer, float stationRepairAmount) {
        float alpha = Math.min(1f, repairMessageTimer / REPAIR_FADE_DIVISOR);
        font.getData().setScale(0.9f);
        font.setColor(0.30f, 1.00f, 0.40f, alpha);
        glyphLayout.setText(font, "STATION REPAIRED +" + (int) stationRepairAmount);
        font.draw(batch, glyphLayout, (vW - glyphLayout.width) / 2f, vH / 2f + 50f);
        font.setColor(Color.WHITE);
    }

    /**
     * Draws the bottom-centre notification/fact toast with fade-in and fade-out alpha.
     *
     * @param delta seconds since last frame (used to tick the notification timer)
     */
    private void drawNotification(SpriteBatch batch, float vW, float delta) {
        if (notifTimer > 0f) notifTimer -= delta;
        if (notifText == null || notifTimer <= 0f) return;

        float alpha;
        if      (notifTimer > NOTIF_DURATION - NOTIF_FADE_IN) alpha = 1f - (notifTimer - (NOTIF_DURATION - NOTIF_FADE_IN)) / NOTIF_FADE_IN;
        else if (notifTimer < NOTIF_FADE_OUT)                  alpha = notifTimer / NOTIF_FADE_OUT;
        else                                                    alpha = 1f;

        font.getData().setScale(1.1f);
        font.setColor(0.95f, 0.88f, 0.55f, alpha);

        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        for (String w : notifText.split(" ")) {
            glyphLayout.setText(font, sb1 + w + " ");
            if (glyphLayout.width < vW * 0.75f) sb1.append(w).append(" ");
            else                                sb2.append(w).append(" ");
        }
        drawCentredLine(batch, sb1.toString().trim(), vW, 72f);
        if (sb2.length() > 0) drawCentredLine(batch, sb2.toString().trim(), vW, 50f);
        font.setColor(Color.WHITE);
    }

    /** Draws a single line of text centred horizontally, with a 1px bold offset. */
    private void drawCentredLine(SpriteBatch batch, String text, float vW, float y) {
        glyphLayout.setText(font, text);
        float x = (vW - glyphLayout.width) / 2f;
        font.draw(batch, text, x + 1f, y);
        font.draw(batch, text, x,       y);
    }

    /**
     * Draws the landing approach guide panel (speed and angle readouts)
     * when the rocket is within landing altitude.
     */
    private void drawLandingGuide(SpriteBatch batch, ShapeRenderer sr) {
        batch.end();
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.70f);
        sr.rect(6f, LANDING_GUIDE_Y, PANEL_WIDTH, LANDING_GUIDE_HEIGHT);
        sr.end();
        batch.begin();

        float   speed  = Math.abs(rocket.getVy());
        float   angle  = rocket.getRotation();
        boolean spdOk  = speed <= RocketCollisionStrategy.MAX_SAFE_LANDING_SPEED;
        boolean angOk  = angle >= RocketCollisionStrategy.MIN_UPRIGHT_ANGLE
                      && angle <= RocketCollisionStrategy.MAX_UPRIGHT_ANGLE;

        font.getData().setScale(1.1f);
        font.setColor(0.9f, 0.9f, 0.9f, 1f);
        font.draw(batch, "LANDING APPROACH", 14f, 182f);

        font.getData().setScale(1.0f);
        font.setColor(spdOk ? Color.GREEN : Color.RED);
        font.draw(batch, "SPD: " + (int) speed + " / " + (int) RocketCollisionStrategy.MAX_SAFE_LANDING_SPEED, 14f, 160f);
        font.setColor(angOk ? Color.GREEN : Color.RED);
        font.draw(batch, "ANG: " + (int) angle + "  (aim 65-115)", 14f, 136f);
        font.setColor(Color.WHITE);
    }

    /** Returns the colour used for the orbit debris count based on how many are in flight. */
    private Color orbitColor(int inFlight) {
        if (inFlight > 10) return Color.RED;
        if (inFlight >  5) return Color.YELLOW;
        return Color.LIGHT_GRAY;
    }

    /** Draws the bottom control hints bar. */
    private void drawControlHints(SpriteBatch batch) {
        font.getData().setScale(0.85f);
        font.setColor(0.70f, 0.70f, 0.70f, 1f);
        font.draw(batch, "[W / UP] Thrust    [A / D] Rotate    [E] Launch debris", 10f, 36f);
        font.draw(batch, "[Land on pad] Repair station    [ESC] Pause", 10f, 16f);
        font.setColor(Color.WHITE);
        font.getData().setScale(1f);
    }

    public void dispose() {
        if (font != null) font.dispose();
    }
}
