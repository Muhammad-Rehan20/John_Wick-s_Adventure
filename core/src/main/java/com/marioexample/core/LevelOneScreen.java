package com.marioexample.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 * ============================================================================
 * LEVEL ONE SCREEN - Desert Adventure Level
 * ============================================================================
 * 
 * WHAT IS THIS CLASS?
 * -------------------
 * This class creates Level 1 of the game - a desert-themed level where the
 * player fights enemies, collects coins, and tries to reach the portal.
 * 
 * OOP CONCEPTS USED:
 * ------------------
 * 1. CLASS: A blueprint that defines everything about Level 1
 * 2. IMPLEMENTS SCREEN: Follows the LibGDX Screen interface (contract)
 * 3. ENCAPSULATION: All data (variables) and behavior (methods) are contained
 *    in this one class
 * 4. METHODS: Break down the game into smaller, manageable functions
 *    - handleInput() - checks player input
 *    - update() - updates game state (physics, collisions, etc.)
 *    - render() - draws everything on screen
 * 
 * HOW THE GAME LOOP WORKS:
 * ------------------------
 * Every frame (about 60 times per second), LibGDX calls:
 * 1. handleInput() - "What did the player do?" (move, jump, shoot)
 * 2. update() - "What should happen?" (apply gravity, check collisions, etc.)
 * 3. render() - "Draw everything on screen"
 * 
 * LEVEL FEATURES:
 * ---------------
 * - Background: dessertB.jpg (desert theme)
 * - Ground platform with hazards (thorns)
 * - Coins above each thorn (collectible items)
 * - 4 enemies that patrol back and forth
 * - Portal door at the end (goal)
 * - Player can ONLY shoot fire bullets (no ice bullets)
 * - When player reaches portal: shows "Level 1 Completed" and goes to Level 2
 * 
 * KEY CONCEPTS FOR BEGINNERS:
 * ---------------------------
 * - RECTANGLES: We use Rectangle objects to represent positions and sizes
 *   of game objects (player, enemies, bullets, etc.). Useful for collision detection.
 * - ARRAYS: Used to store multiple objects (like multiple enemies, bullets, coins)
 * - CAMERA: Controls what part of the game world is visible on screen
 * - COLLISION DETECTION: Checking if two rectangles overlap to detect hits
 * - PHYSICS: Gravity pulls player down, velocity makes things move
 * 
 * FILE STRUCTURE:
 * ---------------
 * 1. CONSTANTS (values that never change) - Lines ~33-61
 * 2. VARIABLES (data storage) - Lines ~63-130
 * 3. CONSTRUCTOR (initialization) - Lines ~132-230
 * 4. MAIN GAME LOOP (render method) - Lines ~301-330
 * 5. INPUT HANDLING - Lines ~332-490
 * 6. GAME UPDATE LOGIC - Lines ~495-660
 * 7. HELPER METHODS - Lines ~662-779
 */
public class LevelOneScreen implements Screen {

    // World config – match final level aspect/zoom
    private static final float WORLD_WIDTH = 24f;
    private static final float WORLD_HEIGHT = 13.5f;
    // Total horizontal length of this level (can be wider than the camera width)
    private static final float LEVEL_LENGTH = 40f;

    // Player config (kept same as GameplayScreen so physics feel identical)
    private static final float PLAYER_HEIGHT = 2.5f;
    private static final float PLAYER_WIDTH = PLAYER_HEIGHT * 904f / 1277f;
    private static final float PLAYER_SPEED = 10f;
    private static final float PLAYER_ACCELERATION = 60f;
    private static final float PLAYER_DECELERATION = 80f;
    private static final float GRAVITY = -40f;
    private static final float JUMP_VELOCITY = 16f;

    // Bullet config (only fire bullets here)
    private static final int MAX_BULLETS = 6;
    private static final float BULLET_SPEED = 20f;
    private static final float BULLET_COOLDOWN = 0.25f;

    // Enemy config
    private static final int ENEMY_COUNT = 4;
    // Slightly larger enemies in Level 1 (keep square ratio so sprite is not stretched)
    private static final float ENEMY_WIDTH = 2.5f;
    private static final float ENEMY_HEIGHT = 2.5f;

    // HUD / Exit button (match final level style)
    private static final float EXIT_BTN_WIDTH = 80f;
    private static final float EXIT_BTN_HEIGHT = 30f;

    // Core references
    private final MainGame game;
    private final SpriteBatch batch;
    private OrthographicCamera worldCamera;
    private OrthographicCamera hudCamera;

    // Rendering helpers
    private BitmapFont font;
    private GlyphLayout layout;

    // Textures
    private Texture backgroundTexture;
    private Texture groundTexture;
    private Texture standingTexture;
    private Texture jumpTexture;
    private Texture walk1Texture;
    private Texture walk2Texture;
    private Texture walk3Texture;
    private Texture enemyTexture;
    private Texture fireBulletTexture;
    private Texture coinTexture;
    private Texture thornTexture;
    private Texture portalTexture;

    // Sounds (similar to final level but simplified for this intro level)
    private Sound jumpSound;
    private Sound fireBulletSound;
    private Sound coinSound;
    private Sound enemyDeathSound;
    private Sound levelCompleteSound;
    private Music backgroundMusic;

    // Player state
    private Rectangle playerRect;
    private float playerVX;
    private float playerVY;
    private boolean onGround;
    private boolean facingRight = true;
    private float walkAnimTime = 0f;

    // Simple animation state
    private enum PlayerAnimState { IDLE, WALKING, JUMPING }
    private PlayerAnimState playerAnimState = PlayerAnimState.IDLE;
    private static final float WALK_ANIM_SPEED = 8f;

    // Level geometry
    private Array<Rectangle> platforms = new Array<Rectangle>();
    private Array<Rectangle> hazards = new Array<Rectangle>();
    private Rectangle goalPortal;
    private float levelWidth;

    // Enemies
    private float[] enemyX = new float[ENEMY_COUNT];
    private float[] enemyY = new float[ENEMY_COUNT];
    private float[] enemyVX = new float[ENEMY_COUNT];
    private boolean[] enemyAlive = new boolean[ENEMY_COUNT];
    private float[] enemyLeftPatrol = new float[ENEMY_COUNT];
    private float[] enemyRightPatrol = new float[ENEMY_COUNT];

    // Coins (one above each thorn)
    private float[] coinX;
    private float[] coinY;
    private boolean[] coinActive;
    private int coinsCollected = 0;

    // Player bullets (fire only)
    private float[] bulletX = new float[MAX_BULLETS];
    private float[] bulletY = new float[MAX_BULLETS];
    private float[] bulletVX = new float[MAX_BULLETS];
    private boolean[] bulletActive = new boolean[MAX_BULLETS];
    private float bulletCooldownTimer = 0f;

    // Game state
    private int lives = 3;
    private int score = 0;
    private boolean levelComplete = false;
    private boolean playerDead = false;
    private float levelCompleteTimer = 0f;
    private float playerAlpha = 1f;

    public LevelOneScreen(MainGame game) {
        this.game = game;
        this.batch = game.batch;

        worldCamera = new OrthographicCamera();
        worldCamera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
        worldCamera.zoom = 1.1f; // same zoom as final level
        worldCamera.position.y = WORLD_HEIGHT / 2f + 0.7f; // same vertical offset as final level

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        font = new BitmapFont();
        layout = new GlyphLayout();

        loadTextures();
        loadSounds();
        initLevelLayout();
        resetPlayer();
    }

    private void loadTextures() {
        backgroundTexture = new Texture(Gdx.files.internal("dessertB.jpg"));
        groundTexture = new Texture(Gdx.files.internal("ground.png"));
        standingTexture = new Texture(Gdx.files.internal("standing.png"));
        jumpTexture = new Texture(Gdx.files.internal("jump.png"));
        walk1Texture = new Texture(Gdx.files.internal("walk1.png"));
        walk2Texture = new Texture(Gdx.files.internal("walk2.png"));
        walk3Texture = new Texture(Gdx.files.internal("walk3.png"));
        // Level 1 uses a different enemy sprite than the final level
        enemyTexture = new Texture(Gdx.files.internal("denemy.png"));
        fireBulletTexture = new Texture(Gdx.files.internal("firebullet.png"));
        coinTexture = new Texture(Gdx.files.internal("coin.png"));
        thornTexture = new Texture(Gdx.files.internal("thorn.png"));
        portalTexture = new Texture(Gdx.files.internal("door.png"));
    }

    private void loadSounds() {
        try {
            jumpSound = Gdx.audio.newSound(Gdx.files.internal("jump.wav"));
        } catch (Exception e) {
            Gdx.app.log("Sound", "jump.wav not found for Level 1");
        }
        try {
            fireBulletSound = Gdx.audio.newSound(Gdx.files.internal("firebullet.mp3"));
        } catch (Exception e) {
            Gdx.app.log("Sound", "firebullet.mp3 not found for Level 1");
        }
        try {
            coinSound = Gdx.audio.newSound(Gdx.files.internal("coin.wav"));
        } catch (Exception e) {
            Gdx.app.log("Sound", "coin.wav not found for Level 1");
        }
        try {
            enemyDeathSound = Gdx.audio.newSound(Gdx.files.internal("enemy_death.wav"));
        } catch (Exception e) {
            Gdx.app.log("Sound", "enemy_death.wav not found for Level 1");
        }

        try {
            levelCompleteSound = Gdx.audio.newSound(Gdx.files.internal("level_complete.wav"));
        } catch (Exception e) {
            Gdx.app.log("Sound", "level_complete.wav not found for Level 1");
        }
        try {
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background_music.mp3"));
            if (backgroundMusic != null) {
                backgroundMusic.setLooping(true);
                backgroundMusic.setVolume(0.25f);
                // Stop any existing music before playing to prevent duplicates
                if (backgroundMusic.isPlaying()) {
                    backgroundMusic.stop();
                }
                backgroundMusic.play();
            }
        } catch (Exception e) {
            Gdx.app.log("Sound", "background_music.mp3 not found for Level 1");
        }
    }

    private void initLevelLayout() {
        // Flat ground across most of the level
        platforms.add(new Rectangle(0, 0, LEVEL_LENGTH, 1.2f));

        // Simple thorns with coins above them
        float hazardSize = 1.1f;
        float hazardY = 1.2f;

        float[] thornXs = new float[] { 6f, 11f, 16f, 21f, 24.5f };
        int thornCount = thornXs.length;
        coinX = new float[thornCount];
        coinY = new float[thornCount];
        coinActive = new boolean[thornCount];

        for (int i = 0; i < thornCount; i++) {
            float x = thornXs[i];
            Rectangle h = new Rectangle(x, hazardY, hazardSize, hazardSize);
            hazards.add(h);
            // Coin directly above each thorn
            coinX[i] = x + hazardSize / 2f;
            coinY[i] = hazardY + hazardSize + 1.2f;
            coinActive[i] = true;
        }

        // Portal at the far right
        float portalWidth = 3.0f;
        float portalX = LEVEL_LENGTH - portalWidth - 1f;
        goalPortal = new Rectangle(portalX, 0f, portalWidth, 1f);
        if (portalTexture != null) {
            float aspect = (float) portalTexture.getHeight() / (float) portalTexture.getWidth();
            goalPortal.height = goalPortal.width * aspect;
        } else {
            goalPortal.height = 5f;
        }

        levelWidth = LEVEL_LENGTH;

        // Enemies: 4 simple ground patrols
        // Three in the mid section, last one is before a clear run to the portal
        setEnemy(0, 9f, 1.2f, 8f, 13f, 1.6f);
        setEnemy(1, 14f, 1.2f, 13f, 18f, 1.5f);
        setEnemy(2, 21f, 1.2f, 20f, 25f, 1.7f);
        // Final enemy patrols well before the portal to give a visible gap
        setEnemy(3, 27f, 1.2f, 26f, 31f, 1.6f);
    }

    private void setEnemy(int index, float x, float y, float left, float right, float speed) {
        enemyX[index] = x;
        enemyY[index] = y;
        enemyVX[index] = speed;
        enemyAlive[index] = true;
        enemyLeftPatrol[index] = left;
        enemyRightPatrol[index] = right;
    }

    private void resetPlayer() {
        playerRect = new Rectangle(2f, 1.2f, PLAYER_WIDTH, PLAYER_HEIGHT);
        playerVX = 0f;
        playerVY = 0f;
        onGround = false;
        facingRight = true;
        levelComplete = false;
        playerDead = false;
        levelCompleteTimer = 0f;
        playerAlpha = 1f;

        // Reset bullets
        for (int i = 0; i < MAX_BULLETS; i++) {
            bulletActive[i] = false;
        }
        bulletCooldownTimer = 0f;
    }

    @Override
    public void render(float delta) {
        handleInput(delta);
        update(delta);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw background stretched to screen
        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();
        hudCamera.setToOrtho(false, screenW, screenH);
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, screenW, screenH);
        batch.end();

        // Draw world
        worldCamera.update();
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();

        // Platforms
        for (Rectangle p : platforms) {
            float tileSize = 1f;
            int tiles = MathUtils.ceil(p.width / tileSize);
            for (int i = 0; i < tiles; i++) {
                float gx = p.x + i * tileSize;
                batch.draw(groundTexture, gx, p.y, tileSize, p.height);
            }
        }

        // Portal
        batch.draw(portalTexture, goalPortal.x, goalPortal.y, goalPortal.width, goalPortal.height);

        // Hazards (thorns)
        for (Rectangle h : hazards) {
            batch.draw(thornTexture, h.x, h.y, h.width, h.height);
        }

        // Coins
        for (int i = 0; i < coinX.length; i++) {
            if (!coinActive[i]) continue;
            float size = 1.0f;
            batch.draw(coinTexture, coinX[i] - size / 2f, coinY[i] - size / 2f, size, size);
        }

        // Enemies
        for (int i = 0; i < ENEMY_COUNT; i++) {
            if (!enemyAlive[i]) continue;
            float ex = enemyX[i];
            float ey = enemyY[i];
            if (enemyVX[i] > 0) {
                batch.draw(enemyTexture, ex + ENEMY_WIDTH, ey, -ENEMY_WIDTH, ENEMY_HEIGHT);
            } else {
                batch.draw(enemyTexture, ex, ey, ENEMY_WIDTH, ENEMY_HEIGHT);
            }
        }

        // Bullets (fire only)
        float bw = 1.2f;
        float bh = 0.4f;
        for (int i = 0; i < MAX_BULLETS; i++) {
            if (!bulletActive[i]) continue;
            batch.draw(fireBulletTexture, bulletX[i], bulletY[i] - bh / 2f, bw, bh);
        }

        // Player
        batch.setColor(1f, 1f, 1f, playerAlpha);
        Texture currentTex = standingTexture;
        if (playerAnimState == PlayerAnimState.JUMPING) {
            currentTex = jumpTexture;
        } else if (playerAnimState == PlayerAnimState.WALKING) {
            int frameIndex = (int)(walkAnimTime * WALK_ANIM_SPEED) % 3;
            currentTex = (frameIndex == 0) ? walk1Texture : (frameIndex == 1 ? walk2Texture : walk3Texture);
        }
        if (facingRight) {
            batch.draw(currentTex, playerRect.x, playerRect.y, playerRect.width, playerRect.height);
        } else {
            batch.draw(currentTex, playerRect.x + playerRect.width, playerRect.y, -playerRect.width, playerRect.height);
        }
        batch.setColor(Color.WHITE);

        batch.end();

        // HUD / texts
        renderHud();
    }

    private void handleInput(float delta) {
        // Handle mouse/touch for exit button (screen coordinates)
        if (Gdx.input.justTouched()) {
            float sx = Gdx.input.getX();
            float sy = Gdx.graphics.getHeight() - Gdx.input.getY(); // invert Y
            float btnX = Gdx.graphics.getWidth() - EXIT_BTN_WIDTH - 10f;
            float btnY = Gdx.graphics.getHeight() - EXIT_BTN_HEIGHT - 10f;
            if (sx >= btnX && sx <= btnX + EXIT_BTN_WIDTH && sy >= btnY && sy <= btnY + EXIT_BTN_HEIGHT) {
                Gdx.app.exit();
                return;
            }
        }

        // Allow restart with R when dead or after finishing the level
        if (playerDead || levelComplete) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                // Stop music before recreating screen to prevent duplicates
                if (backgroundMusic != null && backgroundMusic.isPlaying()) {
                    backgroundMusic.stop();
                }
                // Simple restart: recreate Level 1 screen
                game.setScreen(new LevelOneScreen(game));
            }
            return;
        }

        boolean left = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);

        if (left) {
            facingRight = false;
            playerVX -= PLAYER_ACCELERATION * delta;
            if (playerVX < -PLAYER_SPEED) playerVX = -PLAYER_SPEED;
        } else if (right) {
            facingRight = true;
            playerVX += PLAYER_ACCELERATION * delta;
            if (playerVX > PLAYER_SPEED) playerVX = PLAYER_SPEED;
        } else {
            if (playerVX > 0) {
                playerVX -= PLAYER_DECELERATION * delta;
                if (playerVX < 0) playerVX = 0;
            } else if (playerVX < 0) {
                playerVX += PLAYER_DECELERATION * delta;
                if (playerVX > 0) playerVX = 0;
            }
        }

        // Animate
        if (!onGround || playerVY > 0) {
            playerAnimState = PlayerAnimState.JUMPING;
        } else if (Math.abs(playerVX) > 0.1f) {
            playerAnimState = PlayerAnimState.WALKING;
            walkAnimTime += delta;
        } else {
            playerAnimState = PlayerAnimState.IDLE;
            walkAnimTime = 0f;
        }

        // Jump
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Input.Keys.W)
                || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            if (onGround) {
                playerVY = JUMP_VELOCITY;
                onGround = false;
                if (jumpSound != null) {
                    jumpSound.play(0.6f);
                }
            }
        }

        // Shooting (K) – ONLY fire bullets in this level
        bulletCooldownTimer -= delta;
        if (bulletCooldownTimer < 0) bulletCooldownTimer = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.K) && bulletCooldownTimer <= 0) {
            shootFireBullet();
            bulletCooldownTimer = BULLET_COOLDOWN;
        }
    }

    private void shootFireBullet() {
        for (int i = 0; i < MAX_BULLETS; i++) {
            if (!bulletActive[i]) {
                bulletActive[i] = true;
                // Bullet spawns from gun nozzle: near front edge (95% of width), at gun height (60% of height)
                float muzzleOffsetX = playerRect.width * 0.95f;
                float muzzleOffsetY = playerRect.height * 0.6f;
                if (facingRight) {
                    bulletX[i] = playerRect.x + muzzleOffsetX;
                    bulletVX[i] = BULLET_SPEED;
                } else {
                    // When facing left, nozzle is at 5% from left edge (same as final level)
                    bulletX[i] = playerRect.x + playerRect.width * (1f - 0.95f);
                    bulletVX[i] = -BULLET_SPEED;
                }
                bulletY[i] = playerRect.y + muzzleOffsetY;
                if (fireBulletSound != null) {
                    fireBulletSound.play(0.7f);
                }
                break;
            }
        }
    }

    // ========================================================================
    // UPDATE METHOD - The Heart of the Game Logic
    // ========================================================================
    // This method runs 60 times per second to update everything in the game.
    // Think of it as the "game engine" that makes everything move and interact.
    //
    // OOP CONCEPT: This is a private method - only this class can call it.
    // It's called from render() to keep the game running smoothly.
    //
    // What it does step by step:
    // 1. Check if level is complete (player won)
    // 2. Check if player is dead
    // 3. Apply physics (gravity, movement)
    // 4. Check collisions (player with platforms, enemies, hazards)
    // 5. Update enemy positions
    // 6. Update bullet positions
    // 7. Check bullet-enemy collisions
    // 8. Check player-enemy collisions
    // 9. Check player-hazard collisions
    // 10. Check coin collection
    // 11. Check if player reached portal
    // 12. Update camera to follow player
    
    /**
     * Updates all game objects every frame
     * 
     * @param delta Time since last frame (used to make movement smooth and frame-rate independent)
     */
    private void update(float delta) {
        // ====================================================================
        // STEP 1: Handle Level Completion (Player Reached Portal)
        // ====================================================================
        if (levelComplete) {
            levelCompleteTimer += delta;  // Count up the timer
            float fadeDuration = 1.3f;  // How long to fade out player (1.3 seconds)
            // Make player fade out (alpha goes from 1.0 to 0.0)
            playerAlpha = Math.max(0f, 1f - levelCompleteTimer / fadeDuration);

            // After 2 seconds, switch to Level 2
            if (levelCompleteTimer > 2.0f) {
                // Stop music before switching screens
                if (backgroundMusic != null && backgroundMusic.isPlaying()) {
                    backgroundMusic.stop();
                }
                // Switch to Level 2 screen
                game.setScreen(new LevelTwoScreen(game));
            }
            return;  // Don't do anything else if level is complete
        }

        // ====================================================================
        // STEP 2: Skip Everything If Player Is Dead
        // ====================================================================
        if (playerDead) {
            return;  // Don't update anything if player is dead
        }

        // ====================================================================
        // STEP 3: Apply Physics (Gravity and Movement)
        // ====================================================================
        // Physics Concept: Velocity is speed in a direction
        // - playerVX = horizontal velocity (left/right speed)
        // - playerVY = vertical velocity (up/down speed)
        // - GRAVITY constantly pulls player down (makes playerVY more negative)
        
        playerVY += GRAVITY * delta;  // Gravity pulls player down
        // Calculate where player wants to move to
        float newX = playerRect.x + playerVX * delta;  // New X position
        float newY = playerRect.y + playerVY * delta;  // New Y position

        // Horizontal collisions
        Rectangle newRectX = new Rectangle(newX, playerRect.y, playerRect.width, playerRect.height);
        for (Rectangle p : platforms) {
            if (newRectX.overlaps(p)) {
                if (playerVX > 0) {
                    newX = p.x - playerRect.width;
                } else if (playerVX < 0) {
                    newX = p.x + p.width;
                }
                playerVX = 0;
                newRectX.x = newX;
            }
        }

        // Vertical collisions
        Rectangle newRectY = new Rectangle(newX, newY, playerRect.width, playerRect.height);
        onGround = false;
        for (Rectangle p : platforms) {
            if (newRectY.overlaps(p)) {
                if (playerVY < 0) {
                    newY = p.y + p.height;
                    onGround = true;
                } else if (playerVY > 0) {
                    newY = p.y - playerRect.height;
                }
                playerVY = 0;
                newRectY.y = newY;
            }
        }

        playerRect.x = newX;
        playerRect.y = newY;

        // Clamp to world
        if (playerRect.x < 0) playerRect.x = 0;
        if (playerRect.x + playerRect.width > levelWidth)
            playerRect.x = levelWidth - playerRect.width;

        if (playerRect.y < -5) {
            killPlayer();
        }

        // Enemies patrol
        for (int i = 0; i < ENEMY_COUNT; i++) {
            if (!enemyAlive[i]) continue;
            enemyX[i] += enemyVX[i] * delta;
            if (enemyX[i] < enemyLeftPatrol[i]) {
                enemyX[i] = enemyLeftPatrol[i];
                enemyVX[i] = Math.abs(enemyVX[i]);
            } else if (enemyX[i] + ENEMY_WIDTH > enemyRightPatrol[i]) {
                enemyX[i] = enemyRightPatrol[i] - ENEMY_WIDTH;
                enemyVX[i] = -Math.abs(enemyVX[i]);
            }
        }

        // Bullets
        for (int i = 0; i < MAX_BULLETS; i++) {
            if (!bulletActive[i]) continue;
            bulletX[i] += bulletVX[i] * delta;
            if (bulletX[i] < -5 || bulletX[i] > levelWidth + 5) {
                bulletActive[i] = false;
            }
        }

        // Bullet-enemy collisions
        Rectangle bulletRect = new Rectangle();
        Rectangle enemyRect = new Rectangle();
        for (int i = 0; i < MAX_BULLETS; i++) {
            if (!bulletActive[i]) continue;
            bulletRect.set(bulletX[i], bulletY[i] - 0.2f, 1.2f, 0.4f);
            for (int j = 0; j < ENEMY_COUNT; j++) {
                if (!enemyAlive[j]) continue;
                enemyRect.set(enemyX[j], enemyY[j], ENEMY_WIDTH, ENEMY_HEIGHT);
                if (bulletRect.overlaps(enemyRect)) {
                    bulletActive[i] = false;
                    enemyAlive[j] = false;
                    score += 50;
                    if (enemyDeathSound != null) {
                        enemyDeathSound.play(0.6f);
                    }
                    break;
                }
            }
        }

        // Player-enemy collisions
        Rectangle playerHit = new Rectangle(playerRect);
        for (int i = 0; i < ENEMY_COUNT; i++) {
            if (!enemyAlive[i]) continue;
            enemyRect.set(enemyX[i], enemyY[i], ENEMY_WIDTH, ENEMY_HEIGHT);
            if (playerHit.overlaps(enemyRect)) {
                killPlayer();
                break;
            }
        }

        // Player-thorn collisions
        Rectangle thornRect = new Rectangle();
        for (Rectangle h : hazards) {
            thornRect.set(h);
            if (playerHit.overlaps(thornRect)) {
                killPlayer();
                break;
            }
        }

        // Coins
        for (int i = 0; i < coinX.length; i++) {
            if (!coinActive[i]) continue;
            Rectangle coinRect = new Rectangle(coinX[i] - 0.5f, coinY[i] - 0.5f, 1f, 1f);
            if (playerHit.overlaps(coinRect)) {
                coinActive[i] = false;
                coinsCollected++;
                score += 10;
                if (coinSound != null) {
                    coinSound.play(0.7f);
                }
            }
        }

        // Portal / level complete (no boss requirement here)
        if (playerHit.overlaps(goalPortal)) {
            if (!levelComplete) {
                levelComplete = true;
                if (levelCompleteSound != null) {
                    levelCompleteSound.play(0.8f);
                }
                if (backgroundMusic != null && backgroundMusic.isPlaying()) {
                    backgroundMusic.stop();
                }
            }
        }

        // Camera follow – same style as final level
        float halfViewportWidth = worldCamera.viewportWidth / 2f;
        float camX = playerRect.x + playerRect.width / 2f;
        camX = MathUtils.clamp(camX, halfViewportWidth, levelWidth - halfViewportWidth);
        worldCamera.position.x = camX;
        // Keep vertical offset so ground sits a bit lower against the background
        worldCamera.position.y = WORLD_HEIGHT / 2f + 0.7f;
    }

    private void killPlayer() {
        lives--;
        if (lives > 0) {
            resetPlayer();
        } else {
            playerDead = true;
        }
    }

    private void renderHud() {
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.update();

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        String title = "Level 1 – Desert Run";
        String controls = "Move: LEFT/RIGHT or A/D   Jump: SPACE/W/UP   Shoot Fire: K";
        String stats = "Score: " + score + "   Coins: " + coinsCollected + "   Lives: " + lives;

        float x = 20f;
        float y = Gdx.graphics.getHeight() - 20f;
        drawShadowedText(title, x, y, Color.WHITE);
        drawShadowedText(controls, x, y - 25f, Color.LIGHT_GRAY);
        drawShadowedText(stats, x, y - 50f, Color.GOLD);

        // Exit button (top-right) – same visual style as final level
        float btnX = Gdx.graphics.getWidth() - EXIT_BTN_WIDTH - 10f;
        float btnY = Gdx.graphics.getHeight() - 10f;
        String exitLabel = "Exit";
        drawShadowedText(exitLabel, btnX + 15f, btnY - 5f, Color.RED);

        if (levelComplete) {
            String msg = "Level 1 Completed!";
            layout.setText(font, msg);
            float mx = (Gdx.graphics.getWidth() - layout.width) / 2f;
            float my = Gdx.graphics.getHeight() / 2f;
            drawShadowedText(msg, mx, my, Color.GREEN);

            String next = "Get ready for Level 2...";
            layout.setText(font, next);
            float nx = (Gdx.graphics.getWidth() - layout.width) / 2f;
            drawShadowedText(next, nx, my - 30f, Color.WHITE);
        } else if (playerDead) {
            String msg = "You Died! Restart the game to try again.";
            layout.setText(font, msg);
            float mx = (Gdx.graphics.getWidth() - layout.width) / 2f;
            float my = Gdx.graphics.getHeight() / 2f;
            drawShadowedText(msg, mx, my, Color.RED);
        }

        batch.end();
    }

    private void drawShadowedText(String text, float x, float y, Color color) {
        font.setColor(Color.BLACK);
        font.draw(batch, text, x + 2, y - 2);
        font.setColor(color);
        font.draw(batch, text, x, y);
    }

    @Override
    public void resize(int width, int height) {
        hudCamera.setToOrtho(false, width, height);
    }

    @Override
    public void show() {
        // Ensure background music is playing when screen is shown
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.play();
        }
    }

    @Override
    public void hide() {
        // Stop background music when screen is hidden to prevent overlap
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.stop();
        }
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void dispose() {
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (groundTexture != null) groundTexture.dispose();
        if (standingTexture != null) standingTexture.dispose();
        if (jumpTexture != null) jumpTexture.dispose();
        if (walk1Texture != null) walk1Texture.dispose();
        if (walk2Texture != null) walk2Texture.dispose();
        if (walk3Texture != null) walk3Texture.dispose();
        if (enemyTexture != null) enemyTexture.dispose();
        if (fireBulletTexture != null) fireBulletTexture.dispose();
        if (coinTexture != null) coinTexture.dispose();
        if (thornTexture != null) thornTexture.dispose();
        if (portalTexture != null) portalTexture.dispose();
        if (font != null) font.dispose();
        if (jumpSound != null) jumpSound.dispose();
        if (fireBulletSound != null) fireBulletSound.dispose();
        if (coinSound != null) coinSound.dispose();
        if (enemyDeathSound != null) enemyDeathSound.dispose();

        if (levelCompleteSound != null) levelCompleteSound.dispose();
        if (backgroundMusic != null) backgroundMusic.dispose();
    }
}