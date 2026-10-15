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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * ============================================================================
 * MAIN MENU SCREEN - The First Screen Players See
 * ============================================================================
 * 
 * WHAT IS THIS CLASS?
 * -------------------
 * This is the main menu - the first screen that appears when you start the game.
 * It's like a welcome screen where players can choose what to do next.
 * 
 * OOP CONCEPTS USED HERE:
 * -----------------------
 * 1. CLASS: This is a blueprint for creating a menu screen object.
 * 2. IMPLEMENTS SCREEN: This means this class follows a contract (interface) 
 *    that says "I know how to be a screen in a LibGDX game."
 * 3. PRIVATE VARIABLES: These store information that only this class can access.
 * 4. PUBLIC METHODS: These are functions that can be called from outside this class.
 * 
 * WHAT DOES THIS SCREEN DO?
 * -------------------------
 * - Shows a background image (background.jpg)
 * - Displays 5 buttons:
 *   1. Level 1 (Yellow) - Click to go directly to Level 1
 *   2. Level 2 (Blue/White) - Click to go directly to Level 2  
 *   3. Level 3 (Green) - Click to go directly to the Final Boss level
 *   4. Start Game (White) - Click to start from Level 1 (same as Level 1 button)
 *   5. Exit (Red) - Click to close the game
 * - Handles mouse clicks and keyboard input
 * 
 * HOW IT WORKS:
 * ------------
 * 1. When created, it loads the background image
 * 2. Every frame, it draws the background and buttons
 * 3. It checks if player clicked on any button
 * 4. If clicked, it switches to the appropriate game screen
 */
public class MainMenuScreen implements Screen {

    // ========================================================================
    // SECTION 1: VARIABLES TO STORE INFORMATION (Also called "Fields" or "Properties")
    // ========================================================================
    // In Java, we declare variables at the top of a class to store data.
    // Think of these as "memory boxes" that hold important information.
    
    /** Reference to the main game - allows us to switch between screens */
    private final MainGame game;
    
    /** Used to draw images and textures on screen */
    private final SpriteBatch batch;
    
    /** Camera - controls what part of the screen we can see */
    private OrthographicCamera camera;
    
    /** Used to draw shapes like rectangles (for buttons) */
    private ShapeRenderer shapeRenderer;
    
    /** Font - used to draw text on screen */
    private BitmapFont font;
    
    /** Helper to measure text size before drawing */
    private GlyphLayout layout;

    /** The background image for the menu */
    private Texture backgroundTexture;

    // ========================================================================
    // SECTION 2: CONSTANTS - Values That Never Change
    // ========================================================================
    // "static final" means these values are:
    // - static: shared by all menu screens (if we had multiple)
    // - final: cannot be changed once set (like a constant in math)
    
    /** Width of each button in pixels */
    private static final float BUTTON_WIDTH = 250f;
    
    /** Height of each button in pixels */
    private static final float BUTTON_HEIGHT = 60f;
    
    /** Space between buttons vertically */
    private static final float BUTTON_SPACING = 80f;
    
    /** Y position of the top button (how far down from top of screen) */
    private static final float BUTTON_START_Y = 400f;

    // ========================================================================
    // CONSTRUCTOR - Runs When Menu Screen is Created
    // ========================================================================
    // OOP CONCEPT: Constructor is a special method that:
    // - Has the same name as the class
    // - Runs automatically when you create a new MainMenuScreen object
    // - Used to set up (initialize) all the things we need
    
    /**
     * Constructor - prepares everything we need for the menu
     * 
     * @param game Reference to the main game class
     */
    public MainMenuScreen(MainGame game) {
        // Store references to important objects
        this.game = game;  // "this" means "this object" - we're storing the game reference
        this.batch = game.batch;  // Get the drawing tool from the game

        // Set up camera (controls what we see on screen)
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Create tools for drawing
        shapeRenderer = new ShapeRenderer();  // For drawing rectangles (buttons)
        font = new BitmapFont();  // For drawing text
        layout = new GlyphLayout();  // Helper to measure text size

        // Load the background image from the assets folder
        backgroundTexture = new Texture(Gdx.files.internal("background.jpg"));
    }

    // ========================================================================
    // RENDER METHOD - Draws Everything on Screen (Called Every Frame)
    // ========================================================================
    // OOP CONCEPT: @Override means we're replacing a method from the Screen interface
    // This method is called about 60 times per second to update the screen!
    // 
    // Game Loop Concept:
    // 1. Clear screen (erase old frame)
    // 2. Draw background
    // 3. Draw buttons
    // 4. Check for input (clicks, keyboard)
    // 5. Repeat
    
    /**
     * This method runs 60 times per second to draw the menu screen
     * 
     * @param delta Time since last frame (useful for smooth animations, 
     *              but we don't use it much in a simple menu)
     */
    @Override
    public void render(float delta) {
        // Step 1: Clear the screen (erase everything from last frame)
        // We clear to black color (0 = no red, 0 = no green, 0 = no blue, 1 = fully opaque)
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Step 2: Update camera and prepare for drawing
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();  // Tell LibGDX "I'm starting to draw images now"

        // Step 3: Draw the background image
        // Get screen dimensions
        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();
        // Draw background to fill entire screen (starting at 0,0 with full width and height)
        batch.draw(backgroundTexture, 0, 0, screenW, screenH);

        batch.end();  // Tell LibGDX "I'm done drawing images for now"

        // Step 4: Draw buttons (using ShapeRenderer for rectangles)
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Calculate center of screen (where to place buttons)
        float centerX = screenW / 2f;
        float currentY = BUTTON_START_Y;  // Start at top button position

        // Draw all buttons from top to bottom
        // Level 1 button (Yellow)
        drawButton(centerX, currentY, BUTTON_WIDTH, BUTTON_HEIGHT, Color.YELLOW, "Level 1");
        currentY -= BUTTON_SPACING;  // Move down for next button

        // Level 2 button (Blue/White - we create a custom light blue color)
        Color level2Color = new Color(0.4f, 0.6f, 1.0f, 1.0f); // (Red, Green, Blue, Alpha)
        drawButton(centerX, currentY, BUTTON_WIDTH, BUTTON_HEIGHT, level2Color, "Level 2");
        currentY -= BUTTON_SPACING;  // Move down

        // Level 3 button (Green)
        drawButton(centerX, currentY, BUTTON_WIDTH, BUTTON_HEIGHT, Color.GREEN, "Level 3");
        currentY -= BUTTON_SPACING;  // Move down

        // Start Game button (White)
        drawButton(centerX, currentY, BUTTON_WIDTH, BUTTON_HEIGHT, Color.WHITE, "Start Game");
        currentY -= BUTTON_SPACING;  // Move down

        // Exit button (Red)
        drawButton(centerX, currentY, BUTTON_WIDTH, BUTTON_HEIGHT, Color.RED, "Exit");

        // Step 5: Check if player clicked any button or pressed a key
        handleInput();
    }

    // ========================================================================
    // HELPER METHOD - Draws a Single Button
    // ========================================================================
    // OOP CONCEPT: This is a "private method" - only this class can use it.
    // It's like a helper function that does one specific job.
    // We use it multiple times with different colors and text to draw all buttons.
    
    /**
     * Draws one button on the screen
     * 
     * @param centerX X position of button center (horizontal position)
     * @param y Y position of button bottom (vertical position)
     * @param width How wide the button is
     * @param height How tall the button is
     * @param color What color the button should be
     * @param text What text to write on the button
     */
    private void drawButton(float centerX, float y, float width, float height, Color color, String text) {
        // Calculate left edge of button (center minus half width)
        float x = centerX - width / 2f;

        // Draw the button as a filled rectangle
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);  // Set the button color
        shapeRenderer.rect(x, y, width, height);  // Draw rectangle at position (x, y) with size (width, height)
        
        // Draw a black border around the button (makes it look nicer)
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(x, y, width, 2f); // Top border (2 pixels thick)
        shapeRenderer.rect(x, y + height - 2f, width, 2f); // Bottom border
        shapeRenderer.rect(x, y, 2f, height); // Left border
        shapeRenderer.rect(x + width - 2f, y, 2f, height); // Right border
        shapeRenderer.end();  // Done drawing shapes

        // Now draw the text on top of the button
        batch.setProjectionMatrix(camera.combined);
        batch.begin();  // Start drawing text/images
        
        // Calculate where to put the text (centered on button)
        layout.setText(font, text);  // Measure the text size
        float textX = centerX - layout.width / 2f;  // Center text horizontally
        float textY = y + height / 2f + layout.height / 2f;  // Center text vertically
        
        // Draw text with shadow effect (black shadow behind white text)
        font.setColor(Color.BLACK);
        font.draw(batch, text, textX + 2, textY - 2);  // Draw shadow (offset slightly)
        font.setColor(Color.WHITE);
        font.draw(batch, text, textX, textY);  // Draw main text
        
        batch.end();  // Done drawing text/images
    }

    // ========================================================================
    // INPUT HANDLING - Checks What Player Clicked or Pressed
    // ========================================================================
    // This method runs every frame to check:
    // - Did player click on a button?
    // - Did player press a keyboard key?
    // If yes, it switches to the appropriate screen
    
    /**
     * Checks if player clicked any button or pressed any key
     * and switches to the correct screen
     */
    private void handleInput() {
        // ====================================================================
        // PART 1: Check for Mouse/Touch Clicks
        // ====================================================================
        // "justTouched()" returns true only on the frame when player clicks
        // (not while holding, just when they first press)
        if (Gdx.input.justTouched()) {
            // Get screen size and click position
            float screenW = Gdx.graphics.getWidth();
            float screenH = Gdx.graphics.getHeight();
            float touchX = Gdx.input.getX();  // X position of click
            float touchY = screenH - Gdx.input.getY(); // Y position (inverted because screen Y is top-to-bottom)

            // Calculate button positions (same as when we drew them)
            float centerX = screenW / 2f;
            float currentY = BUTTON_START_Y;

            // Check each button from top to bottom:
            // Level 1 button (Yellow)
            if (isButtonClicked(touchX, touchY, centerX, currentY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                game.setScreen(new LevelOneScreen(game));  // Switch to Level 1
                return;  // Stop checking other buttons
            }
            currentY -= BUTTON_SPACING;  // Move to next button position

            // Level 2 button (Blue/White)
            if (isButtonClicked(touchX, touchY, centerX, currentY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                game.setScreen(new LevelTwoScreen(game));  // Switch to Level 2
                return;
            }
            currentY -= BUTTON_SPACING;

            // Level 3 button (Green)
            if (isButtonClicked(touchX, touchY, centerX, currentY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                game.setScreen(new GameplayScreen(game));  // Switch to Final Boss level
                return;
            }
            currentY -= BUTTON_SPACING;

            // Start Game button (White) - same as Level 1
            if (isButtonClicked(touchX, touchY, centerX, currentY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                game.setScreen(new LevelOneScreen(game));  // Start from Level 1
                return;
            }
            currentY -= BUTTON_SPACING;

            // Exit button (Red)
            if (isButtonClicked(touchX, touchY, centerX, currentY, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                Gdx.app.exit();  // Close the game
                return;
            }
        }

        // ====================================================================
        // PART 2: Check for Keyboard Input (Alternative way to select)
        // ====================================================================
        // "isKeyJustPressed()" returns true only on the frame when key is first pressed
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            // Enter key = Start Game
            game.setScreen(new LevelOneScreen(game));
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            // Escape key = Exit game
            Gdx.app.exit();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            // Number 1 key = Go to Level 1
            game.setScreen(new LevelOneScreen(game));
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            // Number 2 key = Go to Level 2
            game.setScreen(new LevelTwoScreen(game));
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            // Number 3 key = Go to Level 3 (Final Boss)
            game.setScreen(new GameplayScreen(game));
        }
    }

    // ========================================================================
    // HELPER METHOD - Checks If Click Is Inside a Button
    // ========================================================================
    // OOP CONCEPT: This method returns a boolean (true or false)
    // It checks if the click position is inside the button's rectangle
    
    /**
     * Checks if a click/touch is inside a button's area
     * 
     * @param touchX X position where player clicked
     * @param touchY Y position where player clicked
     * @param centerX X position of button center
     * @param y Y position of button bottom
     * @param width Button width
     * @param height Button height
     * @return true if click is inside button, false otherwise
     */
    private boolean isButtonClicked(float touchX, float touchY, float centerX, float y, float width, float height) {
        // Calculate button's left edge
        float x = centerX - width / 2f;
        
        // Check if click is inside button rectangle
        // Click must be: 
        // - To the right of left edge (touchX >= x)
        // - To the left of right edge (touchX <= x + width)
        // - Above bottom edge (touchY >= y)
        // - Below top edge (touchY <= y + height)
        boolean isInside = touchX >= x && touchX <= x + width && touchY >= y && touchY <= y + height;
        return isInside;
    }

    // ========================================================================
    // REQUIRED SCREEN METHODS - Part of the Screen Interface Contract
    // ========================================================================
    // OOP CONCEPT: These methods are required because we "implement Screen"
    // Even if we don't use them, we must include them (Java requirement)
    
    /**
     * Called when screen size changes (player resizes window)
     */
    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);  // Update camera for new screen size
    }

    /**
     * Called when this screen becomes active (shown)
     */
    @Override
    public void show() {
        // Nothing special needed when menu appears
    }

    /**
     * Called when this screen is hidden (another screen takes over)
     */
    @Override
    public void hide() {
        // Nothing special needed when menu disappears
    }

    /**
     * Called when game is paused (player switches to another app)
     */
    @Override
    public void pause() {
        // Menu doesn't need special pause handling
    }

    /**
     * Called when game resumes after being paused
     */
    @Override
    public void resume() {
        // Menu doesn't need special resume handling
    }

    // ========================================================================
    // CLEANUP METHOD - Free Memory When Done
    // ========================================================================
    // OOP CONCEPT: "Dispose" means "clean up" or "free resources"
    // We must clean up textures, sounds, etc. to prevent memory leaks
    
    /**
     * Called when screen is removed - cleans up resources to free memory
     * This is important! If we don't clean up, the game will use more and more memory.
     */
    @Override
    public void dispose() {
        // Dispose (free) each resource we created
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        // Note: We don't dispose 'batch' or 'game' because they're shared with other screens
    }
}
