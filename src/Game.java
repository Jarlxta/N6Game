
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import java.awt.*;


import game2D.*;

import javax.swing.*;

// Game demonstrates how we can override the GameCore class
// to create our own 'game'. We usually need to implement at
// least 'draw' and 'update' (not including any local event handling)
// to begin the process. You should also add code to the 'init'
// method that will initialise event handlers etc. By default GameCore
// will handle the 'Escape' key to quit the game but you should
// override this with your own event handler.

/**
 * @author David Cairns
 *
 */
@SuppressWarnings("serial")

public class Game extends GameCore 
{
	// Useful game constants
	static int screenWidth = 512;
	static int screenHeight = 384;
    ScreenManager screen;

    float 	lift = 0.005f;
    float	gravity = 0.0002f;
    
    // Game state flags
    boolean jump = false;
    boolean goRight = false;
    boolean goLeft = false;
    boolean isFalling = true;
    long atkElapsed;
    boolean combo = false;

    // Game resources
    Animation run;
    Animation jumping;
    Animation turn;
    Animation falling;
    Animation idle;
    Animation fallTransition;
    Animation attack1;
    Animation attack2;

    Animation currentAttackAnimation;
    Animation queuedAttackAnimation;
    
    Sprite	player = null;
    ArrayList<Sprite> enemies = new ArrayList<Sprite>();

    TileMap tmap = new TileMap();	// Our tile map, note that we load it in init()
    
    long total;         			// The score will be the total time elapsed since a crash

    // Offsets; testing calculating this in update instead of draw
    int xo;
    int yo;


    /**
	 * The obligatory main method that creates
     * an instance of our class and starts it running
     * 
     * @param args	The list of parameters this program might use (ignored)
     */
    public static void main(String[] args) {

        Game gct = new Game();
        // Make the game close on hitting the appropriate GUI Button;
        // I'm not learning to close this program with ESC just for this project
        gct.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gct.initializeAnimations();
        gct.init();
        // Start in windowed mode with the given screen height and width
        gct.run(false,screenWidth,screenHeight);
    }

    /**
     * Initialise the class, e.g. set up variables, load images,
     * create animations, register event handlers
     */
    public void init()
    {         
        Sprite s;	// Temporary reference to a sprite

        // Load the tile map and print it out so we can check it is valid
        tmap.loadMap("maps", "levelone.txt");
        
        setSize(tmap.getPixelWidth()/2, tmap.getPixelHeight()/2);
        // setSize(1920, 1080);
        screenWidth = this.getWidth();
        screenHeight = this.getHeight();
        setVisible(true);

        // Create a set of background sprites that we can 
        // rearrange to give the illusion of motion

        // Initialise the player with an animation and some other relevant values
        player = new Sprite(idle);
        player.setSpriteType(SpriteType.Player);
        player.setUniqueHitbox(true, 44, 45, 24, 31);
        player.createCombo(attack1, attack2);
        player.setDxDefault(0.125f);
        player.setJumpDefault(-0.175f);
        
        // Create 3 enemies at random positions off the screen
        // to the right
        for (int c=0; c<3; c++)
        {
        	s = new Sprite(idle);
        	s.setX(40*30 - (30*c));
            s.setUniqueHitbox(true, 44, 45, 24, 31);
        	s.setY(64);
        	s.show();
        	enemies.add(s);
        }

        initialiseGame();
    }

    /**
     * You will probably want to put code to restart a game in
     * a separate method so that you can call it to restart
     * the game.
     */
    public void initialiseGame()
    {
    	total = 0;
    	      
        player.setX(64*30);
        player.setY(64);
        player.setVelocityX(0);
        player.setVelocityY(0);
        player.show();
    }
    
    /**
     * Draw the current state of the game
     */
    public void draw(Graphics2D g)
    {    	
    	// Be careful about the order in which you draw objects - you
    	// should draw the background first, then work your way 'forward'

        // Change the offsets when reaching edges.
        // Probably want to make tilemaps that avoid this but just in case.
        g.setColor(Color.white);
        g.fillRect(0, 0, screenWidth, screenHeight);
        //g.fillRect(0, 0, 1920, 1080);

        // Apply offsets to tile map and draw  it
        tmap.draw(g,xo,yo);

        // Apply offsets to sprites then draw them
        for (Sprite s: enemies)
        {
            s.setOffsets(xo,yo);
            s.draw(g);
            g.setColor(Color.blue);
            s.drawBoundingBox(g);
        }
        // Apply offsets to player and draw
        // player.setOffsets(xo, yo);
        player.drawTransformed(g);
        
        // Show score and status information
        String msg = String.format("Score: %d", total/100);
        g.setColor(Color.darkGray);
        g.drawString(msg, getWidth() - 80, 50);

        // debug to live view the offsets
        g.drawString("X hitbox pos: " + Float.toString(player.getX() + player.getHbXOff()), 20, getHeight() - 80);
        g.drawString("X pos: " + Float.toString(player.getX()), 20, getHeight() - 65);
        g.drawString(Integer.toString(screenWidth), 80, 50);
        g.drawString(Float.toString(getFPS()), 20, getHeight() - 25);
        g.drawString("Attacking: " + Boolean.toString(player.getAttacking()), 20, getHeight() - 10);
        g.setColor(Color.red);
        player.drawBoundingBox(g);
        g.setColor(Color.green);
        //player.drawBoundingBox(g, 44, 44, 24, 36);

    }

    /**
     * Update any sprites and check for collisions
     * 
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */    
    public void update(long elapsed)
    {
    	
        // Make adjustments to the speed of the sprite due to gravity
        screenWidth = this.getWidth();
        screenHeight = this.getHeight();
        //setSize(screenWidth, screenHeight);
    	    	
       	player.setAnimationSpeed(1.0f);

           // check the state of a player's attack
        if (player.getAttacking() == true && currentAttackAnimation.getTotalDuration() <= atkElapsed){
            player.setAttacking(false);
            atkElapsed = 0;
        }
        // determining velocity: from grounded
        if (player.isGoRight() && !player.isGoLeft()) {
            player.setVelocityX(player.getDxDefault());
            // player.setAnimation(run);
            //player.setRotation(0);
            // player.setAnimationSpeed(1f);
            player.setScaleX(1);
        } else if (player.isGoLeft() && !player.isGoRight()) {
            player.setVelocityX(-player.getDxDefault());
            //player.setAnimation(run);
            //player.setRotation(0);
            //player.setAnimationSpeed(1f);
            player.setScaleX(-1);
        } else if (player.isGoLeft() && player.isGoRight()) {
            float dx = player.getVelocityX();
            if (dx > 0) {
                player.setGoLeft(false);
            } else if (dx < 0) {
                player.setGoRight(false);
            }
        } else if (!player.isGoLeft() && !player.isGoRight()) {
            player.setVelocityX(0);
            //player.setAnimation(idle);
            //player.setAnimationSpeed(1f);
        }
        if (player.isGrounded()) {
            if (player.isTryJump()) {
                player.setVelocityY(player.getJumpDefault());
                //player.setAnimation(jumping);
                //player.setAnimationSpeed(0.5f);
            }
        }
        else {
            // the player is midair, so affect their Y velocity with gravity
            player.setVelocityY(player.getVelocityY() + (gravity * elapsed));
        }
        // animations
        // if not attacking, decide the animation
        if (!player.getAttacking() && player.isGrounded()){
            // the player is running
            if (player.getVelocityX() != 0 && player.getVelocityY() == 0){
                player.setAnimation(run);
                player.setAnimationSpeed(1f);
            }
            // the player is not moving
            else {
                player.setAnimation(idle);
                player.setAnimationSpeed(0.75f);
            }
        }
        else if (!player.getAttacking()) {
            if (player.getVelocityY() < -0.05) {
                player.setAnimation(jumping);
            } else if (player.getVelocityY() > 0.05) {
                player.setAnimation(falling);
            } else if (player.getVelocityY() >= -0.05 && player.getVelocityY() <= 0.05) {
                player.setAnimation(fallTransition);
            }
        }
        // if attacking, we want an attack animation
        else {
            player.setAnimation(currentAttackAnimation);
            player.setAnimationSpeed(1f);
            if (player.isGrounded()) {player.setVelocityX(player.getVelocityX() * 0.05f);}
            atkElapsed += elapsed;

            if (currentAttackAnimation.getTotalDuration() <= atkElapsed && combo == true){
                currentAttackAnimation = queuedAttackAnimation;
                atkElapsed = 0;
                combo = false;
            }
        }
            player.setAnimationSpeed(1f);

        xo = (screenWidth/2) - (player.getCenterX());
        yo = (screenHeight/2) - (player.getCenterY());

        // handle reaching the edges of the screen
        if (xo > 0){ xo = 0;}
        if (yo > 0) { yo = 0;}
        if (xo < -(tmap.getPixelWidth() - screenWidth)) { xo = -(tmap.getPixelWidth() - screenWidth);}
        if (yo < -(tmap.getPixelHeight() - screenHeight)) { yo = -(tmap.getPixelHeight() - screenHeight);}

        player.setOffsets(xo, yo);

        for (Sprite s : enemies){
            s.update(elapsed);
            checkTileCollision(s, tmap, elapsed);
            s.setVelocityY(0.01f);
        }

        // Now update the sprites animation and position
        player.update(elapsed);
        checkTileCollision(player, tmap, elapsed);
        // Then check for any collisions that may have occurred
        handleScreenEdge(player, tmap, elapsed);
    }
    
    
    /**
     * Checks and handles collisions with the edge of the screen
     * 
     * @param s			The Sprite to check collisions for
     * @param tmap		The tile map to check 
     * @param elapsed	How much time has gone by since the last call
     */
    public void handleScreenEdge(Sprite s, TileMap tmap, long elapsed)
    {
    	// This method just checks if the sprite has gone off the bottom screen.
    	// Ideally you should use tile collision instead of this approach
    	
        if (s.getY() + s.getHeight() > tmap.getPixelHeight())
        {
        	// Put the player back on the map 1 pixel above the bottom
        	s.setY(tmap.getPixelHeight() - s.getHeight() - 1); 
        	
        	// and make them bounce
        	s.setVelocityY(-s.getVelocityY());
        }
    }

    public boolean boundingBoxCollision(Sprite s1, Sprite s2)
    {
    	return false;   	
    }
    
    /**
     * Check and handles collisions with a tile map for the
     * given sprite 's'. Initial functionality is limited...
     * 
     * @param s			The Sprite to check collisions for
     * @param tmap		The tile map to check 
     */

    public void checkTileCollision(Sprite s, TileMap tmap, long elapsed)
    {
        // sx: top left pixel x coord
        // sy: top left pixel y coord
        // sw: sprite width
        // sh: sprite height
        float sx;
        float sy;
        float sw;
        float sh;
        // lx: x coordinate at the previous update
        // ly: y coordinate at the previous update
        // lxTile: the X coordinate of the tile the sprite was previously in
        // lyTile: the Y coordinate of the tile the sprite was previously in
        // lCh: the character of the tile the sprite was previously in
        float lx;
        float ly;
        int lxTile;
        int lyTile;
        char lCh;

        // A set of boolean values which will determine what combination of tiles are colliding
        boolean topLeft = false;
        boolean bottomLeft = false;
        boolean topRight = false;
        boolean bottomRight = false;

        // Take a note of a sprite's current position
        // If the sprite has a unique hitbox, it will have to be offset a bit.
        if (s.isUniqueHitbox()){
            sx = s.getX() + s.getHbXOff();
            sy = s.getY() + s.getHbYOff();
            sw = s.getHbWidth();
            sh = s.getHbHeight();
            lx = s.getLastX() + s.getHbXOff();
            ly = s.getLastY() + s.getHbYOff();
        }
        else{
            sx = s.getX();
            sy = s.getY();
            sw = s.getWidth();
            sh = s.getHeight();
            lx = s.getLastX();
            ly = s.getLastY();
        }
    	float tileWidth = tmap.getTileWidth();
    	float tileHeight = tmap.getTileHeight();

        /**
         * TOP LEFT DETECTION
         */
    	int	xtile = (int)(sx / tileWidth);
    	int ytile = (int)(sy / tileHeight);

    	char ch = tmap.getTileChar(xtile, ytile);

    	if (ch != '.')
    	{
            topLeft = true;
    	}

        /**
         * BOTTOM LEFT DETECTION
         */
    	xtile = (int)(sx / tileWidth);
    	ytile = (int)((sy + sh)/ tileHeight);
    	ch = tmap.getTileChar(xtile, ytile);

     	if (ch != '.') 
    	{
            bottomLeft = true;
    	}

         /**
         * TOP RIGHT DETECTION
         */

         xtile = (int)((sx + sw) / tileWidth);
         ytile = (int)(sy / tileHeight);
         ch = tmap.getTileChar(xtile, ytile);

         if (ch != '.'){
             topRight = true;
         }

         /**
          * BOTTOM RIGHT DETECTION
          */
         xtile = (int)((sx + sw) / tileWidth);
        ytile = (int)((sy + sh) / tileHeight);
        ch = tmap.getTileChar(xtile, ytile);

        if (ch != '.'){
            bottomRight = true;
        }


        /**
         * Bottom tile collisions both NOT detected
         * CASE: In the air. Enable gravity
         */
        if (!bottomLeft && !bottomRight){
            s.setGrounded(false);
        }
        // I wanted to do a switch statement for this, but I'm not sure if there's a better way in Java to do this
        // for a series of booleans. I could probably assign numbers to some variables based on if something is
        // true or false, but I think this is better because it's more readable.

        /**
         * No Collisions Detected
         */
        if (!(topLeft || topRight || bottomLeft || bottomRight)){
            return;
        }
        /**
         * Top Left Collision only
         * CASES: The sprite is either colliding with a tile's side while coming from the right,
         * or the sprite is barely colliding with the ceiling.
         */
        else if(topLeft && !(topRight || bottomLeft || bottomRight)){
            lxTile = (int)(lx/tileWidth);
            lyTile = (int)(ly/tileHeight);
            lCh = tmap.getTileChar(lxTile, lyTile);
            xtile = (int)(sx / tileWidth);
            ytile = (int)(sy / tileHeight);

            // The sprite is coming from the right, therefore is hitting the right side of the tile.
            // The char is also empty, meaning the sprite is actually coming from that direction.
            // (The char clause is needed, because otherwise you can clip into the ceiling while holding left/right)
            if (lxTile > xtile && lCh == '.'){
                //goLeft = false;
                s.setX((xtile+1) * 32 - s.getHbXOff());
            }

            // Otherwise, the sprite is coming from below, and hitting the bottom of the tile.
            else {
                s.setY((ytile+1) * 32 - s.getHbYOff());
                s.setVelocityY(0);
            }
        } // Top Left Collision End

        /**
         * Top Right Collision Only
         * CASES: The sprite is either colliding with a tile's side while coming from the left,
         * or the sprite is barely colliding with the ceiling.
         */
        else if(topRight && !(topLeft || bottomLeft || bottomRight)){
            lxTile = (int)((lx+sw-1)/tileWidth);
            lyTile = (int)(ly/tileHeight);
            lCh = tmap.getTileChar(lxTile, lyTile);
            xtile = (int)((sx+sw) / tileWidth);
            ytile = (int)(sy / tileHeight);

            // The sprite is coming from the left, therefore is hitting the left side of the tile.
            // The char is also empty, meaning the sprite is actually coming from that direction.
            // (The char clause is needed, because otherwise you can clip into the ceiling while holding left/right)
            if (lxTile < xtile && lCh == '.'){
                //goRight = false;
                s.setX((xtile) * 32 - (s.getHbYOff() + sw));
            }

            // Otherwise, the sprite is coming from below, and hitting the bottom of the tile.
            else {
                s.setY((ytile+1) * 32 - s.getHbYOff());
                s.setVelocityY(0);
                s.setGrounded(false);
            }
        } // Top Right Collision End

        /**
         * Bottom Left Collision Only
         * CASES: Either the sprite is colliding with a tile's side while coming from the right,
         * or the sprite is barely landing on a platform.
         */
        else if(bottomLeft && !(topLeft || topRight|| bottomRight)){
            lxTile = (int)(lx / tileWidth);
            lyTile = (int)((ly+sh-1) / tileHeight);
            lCh = tmap.getTileChar(lxTile, lyTile);
            xtile = (int)(sx / tileWidth);
            ytile = (int)((sy+sh) / tileHeight);

            // The sprite is coming from the right, therefore is hitting the right side of the tile.
            // The char is also empty, meaning the sprite is actually coming from that direction.
            if (lxTile > xtile && lCh == '.'){
                //goLeft = false;
                s.setX((xtile+1) * 32 - s.getHbXOff());
            }

            // Otherwise, the sprite is coming from above, and hitting the top of the tile.
            else {
                s.setY((ytile) * 32 - (s.getHbYOff() + sh));
                s.setVelocityY(0);
                s.setGrounded(true);
            }
        }

        /**
         * Bottom Right Collision only
         * CASES: Either the sprite collides with a tile's side coming from the left,
         * or the sprite is barely landing on a platform.
         */
        else if(bottomRight && !(topLeft || topRight || bottomLeft)){
            lxTile = (int)((lx+sw-1) / tileWidth);
            lyTile = (int)((ly+sh-1) / tileHeight);
            lCh = tmap.getTileChar(lxTile, lyTile);
            xtile = (int)((sx+sw) / tileWidth);
            ytile = (int)((sy+sh) / tileHeight);

            // The sprite is coming from the left, therefore is hitting the left side of the tile.
            // The char is also empty, meaning the sprite is actually coming from that direction.
            if (lxTile < xtile && lCh == '.'){
                //goRight = false;
                s.setX((xtile) * 32 - (s.getHbXOff() + sw));
            }

            // Otherwise, the sprite is coming from above, and hitting the top of the tile.
            else {
                s.setY((ytile) * 32 - (s.getHbYOff() + sh));
                s.setVelocityY(0);
                s.setGrounded(true);
            }
        }
        /**
         * Top Left & Right
         * CASE: Sprite collides firmly into the ceiling.
         */
        else if(topLeft && topRight && !(bottomLeft || bottomRight)){
            ytile = (int)(sy / tileHeight);
            s.setY((ytile+1) * 32 - s.getHbYOff());
            s.setVelocityY(0);
            s.setGrounded(false);
        }
        /**
         * Bottom Left & Right
         * CASE: Both feet are firmly planted on the ground.
         */
        else if(bottomLeft && bottomRight && !(topLeft || topRight)){
            ytile = (int)((sy+sh) / tileHeight);
            s.setY((ytile) * 32 - (s.getHbYOff() + sh));
            s.setVelocityY(0);
            s.setGrounded(true);
        }

        /**
         * Bottom Left & Right
         * Top Right
         * CASE: Running into corner to the sprite's right
         */
        else if(bottomLeft && bottomRight && topRight && !topLeft){
            xtile = (int)((sx+sw) / tileWidth);
            //goRight = false;
            s.setX((xtile) * 32 - (s.getHbXOff() + sw));
            s.setVelocityY(0);
            s.setGrounded(true);
        }

        /**
         * Bottom Left & Right
         * Top Left
         * CASE: Running into corner to the sprite's left
         */
        else if(bottomLeft && bottomRight && topLeft && !topRight){
            xtile = (int)(sx / tileWidth);
            //goLeft = false;
            s.setX((xtile+1) * 32 - s.getHbXOff());
            s.setVelocityY(0);
            s.setGrounded(true);
        }

        /**
         * Top Left & Bottom Left
         * CASE: Jumping into a full wall to the sprite's left.
         */
        else if(bottomLeft && topLeft && !(bottomRight || topRight)){
            xtile = (int)(sx / tileWidth);
            //goLeft = false;
            s.setGrounded(false);
            s.setX((xtile+1) * 32 - s.getHbXOff());
        }

        /**
         * Top Right & Bottom Right
         * CASE: Jumping into a full wall to the sprite's right.
         */
        else if(bottomRight && topRight && !(topLeft || bottomLeft)){
            xtile = (int)((sx+sw)/tileWidth);
            //goRight = false;
            s.setGrounded(false);
            s.setX((xtile) * 32 - (s.getHbXOff() + sw));
        }

        /**
         * Top Left & Right
         * Bottom Left
         * CASE: Jumping into a full corner wall to the sprite's left.
         */
        else if(bottomLeft && topLeft && topRight && !bottomRight){
            xtile = (int)(sx / tileWidth);
            ytile = (int)(sy / tileHeight);
            //goLeft = false;
            s.setX((xtile+1) * 32 - s.getHbXOff());
            s.setY((ytile+1) * 32 - s.getHbYOff());
            s.setVelocityY(0);
            s.setGrounded(false);
        }

        /**
         * Top Left & Right
         * Bottom Right
         * CASE: Jumping into a full corner wall to the sprite's left.
         */
        else if(bottomRight && topLeft && topRight && !bottomLeft){
            xtile = (int)((sx+sw) / tileWidth);
            ytile = (int)(sy / tileHeight);
            //goRight = false;
            s.setX((xtile) * 32 - (s.getHbXOff() + sw));
            s.setY((ytile+1) * 32 - s.getHbYOff());
            s.setVelocityY(0);
            s.setGrounded(false);
        }

    }

    /**
     * Override of the keyPressed event defined in GameCore to catch our
     * own events
     *
     *  @param e The event that has been generated
     */
    public void keyPressed(KeyEvent e)
    {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE: stop(); break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                if (player.isTryJump() == false) {player.setTryJump(true); player.setGrounded(false);} break;
            case KeyEvent.VK_S: {
                // Example of playing a sound as a thread
                Sound s = new Sound("sounds/caw.wav");
                s.start();
            } break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                if(!player.getAttacking()){player.setGoRight(true); player.setGoLeft(false);}break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                if(!player.getAttacking()){player.setGoRight(false); player.setGoLeft(true);} break;
        }
        e.consume();
    }

	public void keyReleased(KeyEvent e) { 

		int key = e.getKeyCode();

		// Switch statement instead of lots of ifs...
		// Need to use break to prevent fall through.
		switch (key)
		{
			case KeyEvent.VK_ESCAPE : stop(); break;
			case KeyEvent.VK_UP     :
            case KeyEvent.VK_W:
                player.setTryJump(false); break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                player.setGoRight(false); break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                player.setGoLeft(false); break;
			default :  break;
		}
	}

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        int button = e.getButton();
        switch (button){
            case MouseEvent.BUTTON1: {
                if (combo == false) {
                    if (player.getAttacking() == false) {
                        player.setAttacking(true);
                        currentAttackAnimation = player.getFirstAttack();
                        atkElapsed = 0;
                    } else if (atkElapsed <= currentAttackAnimation.getTotalDuration() / 1.1) {
                        combo = true;
                        queuedAttackAnimation = player.getNextAttack();
                    }
                }
                 } break;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

        int button = e.getButton();

        switch (button) {
            case MouseEvent.BUTTON1: {
               // if (atkElapsed <= currentAttackAnimation.getTotalDuration()/1.25) { combo = false; } break;
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    public void initializeAnimations(){
        // Create a set of background sprites that we can
        // rearrange to give the illusion of motion

        idle = new Animation();
        idle.loadAnimationFromSheet("images/Player/_Idle.png", 10, 1, 200);
        run = new Animation();
        run.loadAnimationFromSheet("images/Player/_Run.png", 10, 1, 75);
        jumping = new Animation();
        jumping.loadAnimationFromSheet("images/Player/_Jump.png", 3, 1, 160);
        turn = new Animation();
        turn.loadAnimationFromSheet("images/Player/_TurnAround.png", 3, 1, 250);
        falling = new Animation();
        falling.loadAnimationFromSheet("images/Player/_Fall.png", 3, 1, 160);
        fallTransition = new Animation();
        fallTransition.loadAnimationFromSheet("images/Player/_JumpFallInBetween.png", 2, 1, 500);
        // fallTransition.setLoop(false);
        attack1 = new Animation();
        attack1.loadAnimationFromSheet("images/Player/_AttackNoMovement.png", 4, 1, 105);
        // attack1.setLoop(false);
        attack2 = new Animation();
        attack2.loadAnimationFromSheet("images/Player/_Attack2NoMovement.png", 6, 1, 85);
    }
}
