package io.github.Project.game.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import io.github.Project.engine.core.GameMaster;
import io.github.Project.game.entities.Debris;
import io.github.Project.game.entities.EarthStation;
import io.github.Project.game.entities.Rocket;
import io.github.Project.game.entities.Satellite;
import io.github.Project.game.entities.SpaceStation;
import io.github.Project.game.core.factory.DebrisFactory;

/**
 * Owns all world-space rendering: background layers, entities, and
 * visual effects (explosion, atmosphere burn, station-hit sparks).
 * Drives the game camera position (including shake) each frame.
 */
public class WorldRenderer {

    // ── Zone boundaries ─────────────────────────────────────────────────
    private static final float EARTH_ZONE_END   = 2500f;
    private static final float SPACE_ZONE_START = 4000f;
    private static final float CLOUD_BAND_MIN   = 900f;
    private static final float CLOUD_BAND_MAX   = 1800f;

    // ── World geometry ───────────────────────────────────────────────────
    private static final int   TILE_SIZE        = 256;
    // ── Agency building display sizes [mission, dish, vab, tanks, tower] ─
    private static final float[] AGENCY_W        = { 400f, 100f, 120f, 200f,  60f };
    private static final float[] AGENCY_H        = { 200f, 125f, 240f, 160f, 370f };
    private static final float[] AGENCY_BASE_Y   = { -45f, -45f, -45f, -67f, -45f };
    private static final int     AGENCY_VARIANTS  = 5;
    private static final int     AGENCY_INSTANCES = 10;
    private static final float   AGENCY_SPAWN_MIN_X      = -1700f;
    private static final float   AGENCY_SPAWN_MAX_X      =  1700f;
    private static final float   AGENCY_CENTER_EXCLUSION =  320f;
    private static final float   AGENCY_MIN_SPACING      =   45f;
    private static final long    AGENCY_LAYOUT_SEED      = 77777L;

    // ── PCG random number generator constants ────────────────────────────
    private static final long PCG_MULTIPLIER          = 6364136223846793005L;
    private static final long PCG_INCREMENT           = 1442695040888963407L;
    private static final long CLOUD_INIT_SEED         = 12345L;
    private static final long STAR_INIT_SEED          = 99887L;
    private static final int  CLOUD_INSTANCE_COUNT    = 30;
    private static final int  STAR_INSTANCE_COUNT     = 400;
    private static final float AGENCY_CANDIDATE_STEP  = 25f;
    private static final float AGENCY_FALLBACK_SPACING = 260f;

    // ── Clouds ───────────────────────────────────────────────────────────
    private static final int CLOUD_COUNT = 6;

    // ── Effects ──────────────────────────────────────────────────────────
    private static final float EXPLOSION_DURATION       = 0.35f;
    private static final float EXPLOSION_SIZE           = 80f;
    private static final float ATMOSPHERE_BURN_DURATION = 0.6f;
    private static final float BURN_SIZE                = 90f;
    private static final float STATION_HIT_FX_DURATION  = 0.22f;
    private static final float STATION_HIT_FX_SIZE_MIN  = 28f;
    private static final float STATION_HIT_FX_SIZE_MAX  = 46f;

    // ── Bowl draw size ───────────────────────────────────────────────────
    private static final float BOWL_DRAW_W = 68f;
    private static final float BOWL_DRAW_H = 34f;

    // ── Background colours ───────────────────────────────────────────────
    private static final Color COLOR_SKY   = new Color(0.40f, 0.70f, 0.95f, 1f);
    private static final Color COLOR_SPACE = new Color(0.02f, 0.02f, 0.08f, 1f);

    // ── StationHitFx inner class ─────────────────────────────────────────
    private static class StationHitFx {
        float x, y, size, timer;
        StationHitFx(float x, float y, float size) {
            this.x = x; this.y = y; this.size = size;
            this.timer = STATION_HIT_FX_DURATION;
        }
    }

    // ── Dependencies ─────────────────────────────────────────────────────
    private final GameMaster    gameMaster;
    private final Rocket        rocket;
    private final SpaceStation  spaceStation;
    private final EarthStation  earthStation;
    private final Array<Satellite> satellites;
    private final DebrisFactory debrisManager;

    // ── Background textures ──────────────────────────────────────────────
    private Texture   grassTopTex;
    private Texture   dirtTex;
    private Texture[] agencyTextures;
    private float[]   agencyData;
    private Texture[] cloudTextures;
    private float[]   cloudPositions;
    private float[]   starPositions;

    // ── Effect textures ──────────────────────────────────────────────────
    private Texture explosionTex;
    private Texture atmosphereBurnTex;
    private Texture bowlTex;

    // ── Effect state ─────────────────────────────────────────────────────
    private float explosionTimer;
    private float explosionX, explosionY;
    private float atmosphereBurnTimer;
    private float atmosphereBurnX, atmosphereBurnY;
    private final Array<StationHitFx> stationHitFxList = new Array<>();

    // ── Reusable colour ──────────────────────────────────────────────────
    private final Color tempColor = new Color();

    // ── Constructor ──────────────────────────────────────────────────────

    public WorldRenderer(GameMaster gameMaster, Rocket rocket,
                         SpaceStation spaceStation, EarthStation earthStation,
                         Array<Satellite> satellites, DebrisFactory debrisManager) {
        this.gameMaster   = gameMaster;
        this.rocket       = rocket;
        this.spaceStation = spaceStation;
        this.earthStation = earthStation;
        this.satellites   = satellites;
        this.debrisManager = debrisManager;
    }

    // ── Init ─────────────────────────────────────────────────────────────

    public void init() {
        // Ground textures
        grassTopTex = new Texture(Gdx.files.internal("images/backgrounds/desert top.png"));
        dirtTex     = new Texture(Gdx.files.internal("images/backgrounds/Dirt.png"));

        // Space agency buildings
        agencyTextures = new Texture[AGENCY_VARIANTS];
        agencyTextures[0] = new Texture(Gdx.files.internal("images/ground/mission_control.png"));
        agencyTextures[1] = new Texture(Gdx.files.internal("images/ground/satellite_dish.png"));
        agencyTextures[2] = new Texture(Gdx.files.internal("images/ground/vab_building.png"));
        agencyTextures[3] = new Texture(Gdx.files.internal("images/ground/fuel_tanks.png"));
        agencyTextures[4] = new Texture(Gdx.files.internal("images/ground/launch_tower.png"));

        agencyData = new float[AGENCY_INSTANCES * 2];
        float[] fixedX = { -1300f, -900f, -450f, 450f, 900f };
        for (int i = 0; i < AGENCY_VARIANTS; i++) {
            agencyData[i * 2]     = fixedX[i];
            agencyData[i * 2 + 1] = i;
        }
        long seed = AGENCY_LAYOUT_SEED;
        for (int i = AGENCY_VARIANTS; i < AGENCY_INSTANCES; i++) {
            seed = seed * PCG_MULTIPLIER + PCG_INCREMENT;
            int type = (int)(((seed >> 17) & 0xFF) % AGENCY_VARIANTS);
            float x = 0f;
            boolean placed = false;
            for (int attempt = 0; attempt < 32; attempt++) {
                seed = seed * PCG_MULTIPLIER + PCG_INCREMENT;
                x = ((seed >> 17) & 0xFFF) / (float) 0xFFF
                    * (AGENCY_SPAWN_MAX_X - AGENCY_SPAWN_MIN_X) + AGENCY_SPAWN_MIN_X;
                if (Math.abs(x) < AGENCY_CENTER_EXCLUSION)
                    x += (x < 0f ? -AGENCY_CENTER_EXCLUSION : AGENCY_CENTER_EXCLUSION);
                if (!isAgencySpawnTooClose(i, x, type)) { placed = true; break; }
            }
            if (!placed) {
                for (float candidate = AGENCY_SPAWN_MIN_X; candidate <= AGENCY_SPAWN_MAX_X; candidate += AGENCY_CANDIDATE_STEP) {
                    if (Math.abs(candidate) < AGENCY_CENTER_EXCLUSION) continue;
                    if (!isAgencySpawnTooClose(i, candidate, type)) { x = candidate; placed = true; break; }
                }
            }
            if (!placed) x = AGENCY_SPAWN_MAX_X + (i - AGENCY_VARIANTS + 1) * AGENCY_FALLBACK_SPACING;
            agencyData[i * 2]     = x;
            agencyData[i * 2 + 1] = type;
        }

        // Clouds
        cloudTextures = new Texture[CLOUD_COUNT];
        for (int i = 0; i < CLOUD_COUNT; i++)
            cloudTextures[i] = new Texture(Gdx.files.internal("images/backgrounds/tiles/pixelart_clouds/x5/cloud" + (i + 1) + ".png"));

        cloudPositions = new float[CLOUD_INSTANCE_COUNT * 3];
        long cs = CLOUD_INIT_SEED;
        for (int i = 0; i < CLOUD_INSTANCE_COUNT; i++) {
            cs = cs * PCG_MULTIPLIER + 1; cloudPositions[i*3]   = ((cs>>17)&0x1FFF)/(float)0x1FFF*3600f-1800f;
            cs = cs * PCG_MULTIPLIER + 1; cloudPositions[i*3+1] = CLOUD_BAND_MIN + ((cs>>17)&0xFFF)/(float)0xFFF*(CLOUD_BAND_MAX-CLOUD_BAND_MIN);
            cs = cs * PCG_MULTIPLIER + 1; cloudPositions[i*3+2] = (int)(((cs>>17)&0xFF) % CLOUD_COUNT);
        }

        // Stars
        starPositions = new float[STAR_INSTANCE_COUNT * 3];
        long ss = STAR_INIT_SEED;
        for (int i = 0; i < STAR_INSTANCE_COUNT; i++) {
            ss = ss * PCG_MULTIPLIER + 1; starPositions[i*3]   = ((ss>>17)&0x1FFF)/(float)0x1FFF*6000f-3000f;
            ss = ss * PCG_MULTIPLIER + 1; starPositions[i*3+1] = EARTH_ZONE_END + ((ss>>17)&0x3FFF)/(float)0x3FFF*8000f;
            ss = ss * PCG_MULTIPLIER + 1; starPositions[i*3+2] = 0.5f + ((ss>>17)&0xFF)/(float)0xFF*2f;
        }

        // Effect textures
        explosionTex      = new Texture(Gdx.files.internal("images/effects/Explosion_01.png"));
        atmosphereBurnTex = new Texture(Gdx.files.internal("images/effects/Atmosphere_Burn.png"));
        bowlTex           = new Texture(Gdx.files.internal("images/effects/Bowl.png"));
    }

    // ── Update ───────────────────────────────────────────────────────────

    public void update(float delta) {
        // Tick station-hit spark timers
        for (int i = stationHitFxList.size - 1; i >= 0; i--) {
            StationHitFx fx = stationHitFxList.get(i);
            fx.timer -= delta;
            if (fx.timer <= 0f) stationHitFxList.removeIndex(i);
        }
        if (explosionTimer      > 0f) explosionTimer      -= delta;
        if (atmosphereBurnTimer > 0f) atmosphereBurnTimer -= delta;
    }

    // ── Effect triggers ──────────────────────────────────────────────────

    public void triggerExplosion(float x, float y) {
        explosionX     = x;
        explosionY     = y;
        explosionTimer = EXPLOSION_DURATION;
    }

    public void triggerAtmosphereBurn(float x, float y) {
        atmosphereBurnX     = x;
        atmosphereBurnY     = y;
        atmosphereBurnTimer = ATMOSPHERE_BURN_DURATION;
    }

    public void triggerStationHitFx(float hitX, float hitY) {
        stationHitFxList.add(new StationHitFx(
            hitX, hitY,
            MathUtils.random(STATION_HIT_FX_SIZE_MIN, STATION_HIT_FX_SIZE_MAX)));
        if (stationHitFxList.size > 24) stationHitFxList.removeIndex(0);
        spaceStation.triggerShake(0.35f, 14f);
    }

    // ── Draw ─────────────────────────────────────────────────────────────

    /**
     * Main render entry point. Delegates each visual layer to a focused
     * private method: camera, background, entities, effects, and UI overlays.
     *
     * @param delta      seconds since last frame
     * @param gameCamera world-space camera to position and render through
     */
    public void draw(float delta, OrthographicCamera gameCamera) {
        updateCamera(gameCamera);

        float camX  = gameCamera.position.x;
        float camY  = gameCamera.position.y;
        float halfW = gameCamera.viewportWidth  / 2f;
        float halfH = gameCamera.viewportHeight / 2f;

        clearBackground(camY);
        drawStars(gameCamera, camX, camY, halfW, halfH);

        SpriteBatch batch = gameMaster.getSharedBatch();
        batch.setProjectionMatrix(gameCamera.combined);
        batch.begin();
        drawBackground(batch, camX, camY, halfW, halfH);
        drawEntities(batch);
        drawEffects(batch);
        batch.end();

        drawSatelliteHealthBars(gameCamera);
    }

    // ── Private draw helpers ──────────────────────────────────────────────

    /**
     * Positions the camera to follow the rocket centre, applying shake
     * offsets from the station and any shaking satellite.
     */
    private void updateCamera(OrthographicCamera gameCamera) {
        float baseCamX = rocket.getPosX() + rocket.getWidth()  / 2f;
        float baseCamY = rocket.getPosY() + rocket.getHeight() / 2f;

        if (spaceStation.getShakeTimer() > 0f) {
            float t     = spaceStation.getShakeTimer() / spaceStation.getShakeDuration();
            float amp   = spaceStation.getShakeAmplitude() * t * 0.5f;
            float phase = spaceStation.getShakeTimer() * 60f;
            baseCamX += MathUtils.sin(phase) * amp;
            baseCamY += MathUtils.cos(phase * 0.8f) * amp * 0.4f;
        }
        for (int i = 0; i < satellites.size; i++) {
            Satellite sat = satellites.get(i);
            if (sat.getShakeTimer() > 0f) {
                float t     = sat.getShakeTimer() / sat.getShakeDuration();
                float amp   = sat.getShakeAmplitude() * t * 0.5f;
                float phase = sat.getShakeTimer() * 65f;
                baseCamX += MathUtils.sin(phase) * amp;
                baseCamY += MathUtils.cos(phase * 0.9f) * amp * 0.4f;
                break;
            }
        }
        gameCamera.position.set(baseCamX, baseCamY, 0f);
        gameCamera.update();
    }

    /** Clears the screen with the sky-to-space gradient colour for the current altitude. */
    private void clearBackground(float camY) {
        getZoneColor(camY, tempColor);
        Gdx.gl.glClearColor(tempColor.r, tempColor.g, tempColor.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    /** Renders star dots via ShapeRenderer; fades in as the camera enters the space zone. */
    private void drawStars(OrthographicCamera gameCamera, float camX, float camY,
                           float halfW, float halfH) {
        float starAlpha = camY >= SPACE_ZONE_START ? 1f
            : camY > EARTH_ZONE_END
                ? (camY - EARTH_ZONE_END) / (SPACE_ZONE_START - EARTH_ZONE_END)
            : 0f;
        if (starAlpha <= 0f) return;

        ShapeRenderer sr = gameMaster.getSharedShapeRenderer();
        sr.setProjectionMatrix(gameCamera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < starPositions.length; i += 3) {
            float sx = starPositions[i];
            float sy = starPositions[i + 1];
            if (sy < camY - halfH || sy > camY + halfH) continue;
            if (sx < camX - halfW || sx > camX + halfW) continue;
            sr.setColor(1f, 1f, 1f, starAlpha);
            sr.circle(sx, sy, starPositions[i + 2]);
        }
        sr.end();
    }

    /** Renders all game entities: station, earth pad, satellites, debris, rocket, and bowl. */
    private void drawEntities(SpriteBatch batch) {
        spaceStation.render(batch, null);
        earthStation.render(batch, null);
        drawStationHitFx(batch);
        for (int i = 0; i < satellites.size; i++) satellites.get(i).render(batch, null);
        drawDebris(batch);
        rocket.render(batch, null);
        drawBowl(batch);
    }

    /** Draws the short spark/flash bursts at each recent station impact point. */
    private void drawStationHitFx(SpriteBatch batch) {
        for (int i = 0; i < stationHitFxList.size; i++) {
            StationHitFx fx = stationHitFxList.get(i);
            float alpha = fx.timer / STATION_HIT_FX_DURATION;
            batch.setColor(1f, 0.75f, 0.3f, alpha);
            batch.draw(explosionTex,
                fx.x - fx.size / 2f, fx.y - fx.size / 2f, fx.size, fx.size);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /**
     * Renders all flying and attached debris.
     * Colour tinting is handled entirely by {@link Debris#render} via its
     * heat-level system — no external tint is applied here.
     */
    private void drawDebris(SpriteBatch batch) {
        Array<Debris> flying = debrisManager.getFlying();
        for (int i = 0; i < flying.size; i++) flying.get(i).render(batch, null);

        Array<Debris> attached = debrisManager.getAttached();
        for (int i = 0; i < attached.size; i++) attached.get(i).render(batch, null);
    }

    /** Draws the bowl attachment texture at the rocket's nose tip. */
    private void drawBowl(SpriteBatch batch) {
        if (bowlTex == null) return;
        float rCX   = rocket.getPosX() + rocket.getWidth()  / 2f;
        float rCY   = rocket.getPosY() + rocket.getHeight() / 2f;
        float rot   = rocket.getRotation();
        float noseX = rCX + MathUtils.cosDeg(rot) * (rocket.getHeight() / 2f);
        float noseY = rCY + MathUtils.sinDeg(rot) * (rocket.getHeight() / 2f);
        batch.draw(bowlTex,
            noseX - BOWL_DRAW_W / 2f, noseY - BOWL_DRAW_H / 2f,
            BOWL_DRAW_W / 2f, BOWL_DRAW_H / 2f, BOWL_DRAW_W, BOWL_DRAW_H,
            1f, 1f, rot - 90f,
            0, 0, bowlTex.getWidth(), bowlTex.getHeight(),
            false, false);
    }

    /** Renders the satellite explosion flash and the atmosphere-burn streak effects. */
    private void drawEffects(SpriteBatch batch) {
        if (explosionTimer > 0f) {
            batch.setColor(1f, 1f, 1f, explosionTimer / EXPLOSION_DURATION);
            batch.draw(explosionTex,
                explosionX - EXPLOSION_SIZE / 2f, explosionY - EXPLOSION_SIZE / 2f,
                EXPLOSION_SIZE, EXPLOSION_SIZE);
            batch.setColor(1f, 1f, 1f, 1f);
        }
        if (atmosphereBurnTimer > 0f && atmosphereBurnTex != null) {
            float alpha = atmosphereBurnTimer / ATMOSPHERE_BURN_DURATION;
            batch.setColor(1f, 0.55f + 0.45f * alpha, 0.1f * alpha, alpha);
            batch.draw(atmosphereBurnTex,
                atmosphereBurnX - BURN_SIZE / 2f, atmosphereBurnY - BURN_SIZE / 2f,
                BURN_SIZE, BURN_SIZE);
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    /** Draws a small health bar above each satellite using ShapeRenderer in world space. */
    private void drawSatelliteHealthBars(OrthographicCamera gameCamera) {
        ShapeRenderer sr = gameMaster.getSharedShapeRenderer();
        sr.setProjectionMatrix(gameCamera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < satellites.size; i++) {
            Satellite sat = satellites.get(i);
            float bx = sat.getPosX();
            float by = sat.getPosY() + sat.getHeight() + 6f;
            float bw = sat.getWidth();
            float hp = sat.getHealthPercentage();
            sr.setColor(0.2f, 0.2f, 0.2f, 0.7f);
            sr.rect(bx, by, bw, 6f);
            sr.setColor(satelliteHealthColor(hp));
            sr.rect(bx, by, bw * hp, 6f);
        }
        sr.end();
    }

    /** Returns green/yellow/red depending on satellite health percentage. */
    private Color satelliteHealthColor(float hp) {
        if (hp > 0.5f)  return Color.GREEN;
        if (hp > 0.25f) return Color.YELLOW;
        return Color.RED;
    }

    // ── Dispose ──────────────────────────────────────────────────────────

    public void dispose() {
        if (grassTopTex       != null) { grassTopTex.dispose();       grassTopTex       = null; }
        if (dirtTex           != null) { dirtTex.dispose();           dirtTex           = null; }
        if (explosionTex      != null) { explosionTex.dispose();      explosionTex      = null; }
        if (atmosphereBurnTex != null) { atmosphereBurnTex.dispose(); atmosphereBurnTex = null; }
        if (bowlTex           != null) { bowlTex.dispose();           bowlTex           = null; }
        if (agencyTextures    != null) {
            for (Texture t : agencyTextures) if (t != null) t.dispose();
            agencyTextures = null;
        }
        if (cloudTextures     != null) {
            for (Texture t : cloudTextures) if (t != null) t.dispose();
            cloudTextures = null;
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────

    private void drawBackground(SpriteBatch batch, float camX, float camY,
                                float halfW, float halfH) {
        float left  = camX - halfW;
        float right = camX + halfW;
        float bot   = camY - halfH;

        // Ground tiles
        int txS = MathUtils.floor(left  / TILE_SIZE);
        int txE = MathUtils.floor(right / TILE_SIZE);
        int tyS = MathUtils.floor(bot   / TILE_SIZE);
        for (int ty = tyS; ty <= -1; ty++) {
            float wy = ty * (float) TILE_SIZE;
            if (wy + TILE_SIZE > 0f) continue;
            Texture tex = (ty == -1) ? grassTopTex : dirtTex;
            for (int tx = txS; tx <= txE; tx++)
                batch.draw(tex, tx * (float) TILE_SIZE, wy, TILE_SIZE, TILE_SIZE);
        }

        // Space agency buildings
        float bldTop = CLOUD_BAND_MAX + 400f;
        if (camY - halfH < bldTop) {
            for (int i = 0; i < AGENCY_INSTANCES; i++) {
                float bx  = agencyData[i * 2];
                int   idx = (int) agencyData[i * 2 + 1];
                float bw  = AGENCY_W[idx];
                float bh  = AGENCY_H[idx];
                float by  = AGENCY_BASE_Y[idx];
                if (bx + bw < left || bx > right || by + bh < bot) continue;
                batch.draw(agencyTextures[idx], bx, by, bw, bh);
            }
        }

        // Clouds
        if (camY + halfH > CLOUD_BAND_MIN && camY - halfH < CLOUD_BAND_MAX + 200f) {
            for (int i = 0; i < cloudPositions.length; i += 3) {
                float cx  = cloudPositions[i], cy = cloudPositions[i + 1];
                int   idx = (int) cloudPositions[i + 2];
                Texture c = cloudTextures[idx];
                if (cx + c.getWidth() < left || cx > right) continue;
                batch.draw(c, cx, cy, c.getWidth(), c.getHeight());
            }
        }
    }

    private void getZoneColor(float alt, Color out) {
        if (alt <= EARTH_ZONE_END) {
            out.set(COLOR_SKY);
        } else if (alt >= SPACE_ZONE_START) {
            out.set(COLOR_SPACE);
        } else {
            float t = (alt - EARTH_ZONE_END) / (SPACE_ZONE_START - EARTH_ZONE_END);
            out.r = MathUtils.lerp(COLOR_SKY.r, COLOR_SPACE.r, t);
            out.g = MathUtils.lerp(COLOR_SKY.g, COLOR_SPACE.g, t);
            out.b = MathUtils.lerp(COLOR_SKY.b, COLOR_SPACE.b, t);
            out.a = 1f;
        }
    }

    private boolean isAgencySpawnTooClose(int placedCount, float x, int type) {
        float halfW   = AGENCY_W[type] * 0.5f;
        float centerX = x + halfW;
        for (int j = 0; j < placedCount; j++) {
            float otherX      = agencyData[j * 2];
            int   otherType   = (int) agencyData[j * 2 + 1];
            float otherHalfW  = AGENCY_W[otherType] * 0.5f;
            float otherCenterX = otherX + otherHalfW;
            float minCenterDist = halfW + otherHalfW + AGENCY_MIN_SPACING;
            if (Math.abs(centerX - otherCenterX) < minCenterDist) return true;
        }
        return false;
    }
}
