
import java.awt.*;
import java.awt.event.KeyEvent;
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

    float 	lift = 0.005f;
    float	gravity = 0.0002f;
    
    // Game state flags
    boolean jump = false;
    boolean goRight = false;
    boolean goLeft = false;
    boolean isFalling = true;

    // Game resources
    Animation run;
    Animation jumping;
    Animation turn;
    
    Sprite	player = null;
    ArrayList<Sprite> clouds = new ArrayList<Sprite>();

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
        screenWidth = this.getWidth();
        screenHeight = this.getHeight();
        setVisible(true);

        // Create a set of background sprites that we can 
        // rearrange to give the illusion of motion
        
        run = new Animation();
        run.loadAnimationFromSheet("images/Player/_Run.png", 10, 1, 75);
        jumping = new Animation();
        jumping.loadAnimationFromSheet("images/Player/_Jump.png", 3, 1, 160);
        turn = new Animation();
        
        // Initialise the player with an animation
        player = new Sprite(run);
        player.setSpriteType(SpriteType.Player);
        player.setUniqueHitbox(true, 44, 45, 24, 31);
        
        // Load a single cloud animation
        Animation ca = new Animation();
        ca.addFrame(loadImage("images/cloud.png"), 1000);
        
        // Create 3 clouds at random positions off the screen
        // to the right
        for (int c=0; c<3; c++)
        {
        	s = new Sprite(ca);
        	s.setX(screenWidth + (int)(Math.random()*200.0f));
        	s.setY(30 + (int)(Math.random()*150.0f));
        	s.setVelocityX(-0.02f);
        	s.show();
        	clouds.add(s);
        }

        initialiseGame();
      		
        System.out.println(tmap);
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
        player.setY(40);
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

    	// First work out how much we need to shift the view 
    	// in order to see where the player is.
        //int xo = (getWidth()/2) - (player.getCenterX());
        //int yo = (getHeight()/2) - (player.getCenterY());
        // int xo = 0;
        // int yo = 0;

        // handle reaching the edges of the screen
//        if (xo > 0){ xo = 0;}
//        if (yo > 0) { yo = 0;}
//        if (xo < -(tmap.getPixelWidth() - screenWidth)) { xo = -(tmap.getPixelWidth() - screenWidth);}
//        if (yo < -(tmap.getPixelHeight() - screenHeight)) { yo = -(tmap.getPixelHeight() - screenHeight);}


        // Change the offsets when reaching edges.
        // Probably want to make tilemaps that avoid this but just in case.
        
        g.setColor(Color.white);
        g.fillRect(0, 0, screenWidth, screenHeight);
        
        // Apply offsets to sprites then draw them
        for (Sprite s: clouds)
        {
        	s.setOffsets(xo,yo);
        	s.draw(g);
            g.setColor(Color.blue);
            // s.drawBoundingBox(g);
        }

        // Apply offsets to player and draw
        // player.setOffsets(xo, yo);
        player.drawTransformed(g);
                
        // Apply offsets to tile map and draw  it
        tmap.draw(g,xo,yo);    
        
        // Show score and status information
        String msg = String.format("Score: %d", total/100);
        g.setColor(Color.darkGray);
        g.drawString(msg, getWidth() - 80, 50);

        // debug to live view the offsets
        g.drawString("X hitbox pos: " + Float.toString(player.getX() + player.getHbXOff()), 20, getHeight() - 80);
        g.drawString("X pos: " + Float.toString(player.getX()), 20, getHeight() - 65);
        g.drawString(Integer.toString(screenWidth), 20, getHeight() - 40);
        g.drawString(player.getSpriteType().name(), 20, getHeight() - 25);
        g.drawString("Attacking: " + Boolean.toString(player.getAttacking()), 20, getHeight() - 10);
        g.setColor(Color.red);
        player.drawBoundingBox(g);
        player.drawBoundingCircle(g);
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
        if (isFalling){
            player.setVelocityY(player.getVelocityY()+(gravity*elapsed));
        }
    	    	
       	player.setAnimationSpeed(1.0f);

           if (goRight){
               player.setVelocityX(0.1f);
               player.setAnimation(run);
               player.setRotation(0);
               player.setAnimationSpeed(1f);
               player.setScaleX(1);
           }
           if (goLeft){
               player.setVelocityX(-0.1f);
               player.setAnimation(run);
               player.setRotation(0);
               player.setAnimationSpeed(1f);
               player.setScaleX(-1);
           }

        if (jump) {
            player.setVelocityY(-0.2f);
            player.setAnimation(jumping);
            player.setAnimationSpeed(0.5f);
        }
                
       	for (Sprite s: clouds)
       		s.update(elapsed);


        xo = (getWidth()/2) - (player.getCenterX());
        yo = (getHeight()/2) - (player.getCenterY());

        // handle reaching the edges of the screen
        if (xo > 0){ xo = 0;}
        if (yo > 0) { yo = 0;}
        if (xo < -(tmap.getPixelWidth() - screenWidth)) { xo = -(tmap.getPixelWidth() - screenWidth);}
        if (yo < -(tmap.getPixelHeight() - screenHeight)) { yo = -(tmap.getPixelHeight() - screenHeight);}

        player.setOffsets(xo, yo);

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
                if (jump == false) {jump = true; isFalling = true;} break;
            case KeyEvent.VK_S: {
                // Example of playing a sound as a thread
                Sound s = new Sound("sounds/caw.wav");
                s.start();
            } break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                if (e.getKeyCode() != KeyEvent.VK_LEFT && e.getKeyCode() != KeyEvent.VK_A) {goRight = true;} break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                if (e.getKeyCode() != KeyEvent.VK_RIGHT && e.getKeyCode() != KeyEvent.VK_D){ goLeft = true; }break;
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

        float dx = s.getVelocityX();
        float dy = s.getVelocityY();

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
    	// Find out how wide and how tall a tile is
    	float tileWidth = tmap.getTileWidth();
    	float tileHeight = tmap.getTileHeight();

        /**
         * TOP LEFT DETECTION
         */
        // Divide the sprite’s x coordinate by the width of a tile, to get
    	// the number of tiles across the x axis that the sprite is positioned at
        // The same applies to the y coordinate
    	int	xtile = (int)(sx / tileWidth);
    	int ytile = (int)(sy / tileHeight);
    	
    	// What tile character is at the top left of the sprite s?
    	char ch = tmap.getTileChar(xtile, ytile);
    	
    	
    	if (ch != '.') // If it's not a dot (empty space), handle it
    	{
    		// Try to stop the sprite from clipping into the wall
            topLeft = true;
           // s.stop();
//            goLeft = false;
    		// You should move the sprite to a position that is not colliding
//            s.setX((xtile+1) * 32 - s.getHbXOff());
//            s.shiftY(-dy * elapsed);
//            return;
    	}

        /**
         * BOTTOM LEFT DETECTION
         */
    	xtile = (int)(sx / tileWidth);
    	ytile = (int)((sy + sh)/ tileHeight);
    	ch = tmap.getTileChar(xtile, ytile);
    	
    	// If it's not empty space
     	if (ch != '.') 
    	{
    		// Stop the sprite from falling
            bottomLeft = true;
//            s.stop();
//    		isFalling = false;
//            s.shiftX(-dx * elapsed);
//            s.shiftY(-dy * elapsed);
//            return;
    	}

         /**
         * TOP RIGHT DETECTION
         */

         xtile = (int)((sx + sw) / tileWidth);
         ytile = (int)(sy / tileHeight);
         ch = tmap.getTileChar(xtile, ytile);

         if (ch != '.'){
             // Try to stop the sprite from clipping into the wall
             topRight = true;
//             s.stop();
//             goRight = false;
//             // You should move the sprite to a position that is not colliding
//             s.shiftX(-dx * elapsed);
//             s.shiftY(-dy * elapsed);
//             return;
         }

         /**
          * BOTTOM RIGHT DETECTION
          */
         xtile = (int)((sx + sw) / tileWidth);
        ytile = (int)((sy + sh) / tileHeight);
        ch = tmap.getTileChar(xtile, ytile);

        if (ch != '.'){
            // Try to stop the sprite from clipping into the wall
            bottomRight = true;
//            s.stop();
//            isFalling = false;
//            // You should move the sprite to a position that is not colliding
//            s.shiftX(-dx * elapsed);
//            s.shiftY(-dy * elapsed);
//            return;
        }


        /**
         * Bottom tile collisions both NOT detected
         * CASE: In the air. Enable gravity
         */
        if (!bottomLeft && !bottomRight){
            isFalling = true;
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
                goLeft = false;
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
                goRight = false;
                s.setX((xtile) * 32 - (s.getHbYOff() + s.getHbWidth()));
            }

            // Otherwise, the sprite is coming from below, and hitting the bottom of the tile.
            else {
                s.setY((ytile+1) * 32 - s.getHbYOff());
                s.setVelocityY(0);
                isFalling = true;
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
                goLeft = false;
                s.setX((xtile+1) * 32 - s.getHbXOff());
            }

            // Otherwise, the sprite is coming from above, and hitting the top of the tile.
            else {
                s.setY((ytile) * 32 - (s.getHbYOff() + s.getHbHeight()));
                s.setVelocityY(0);
                isFalling = false;
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
                goRight = false;
                s.setX((xtile) * 32 - (s.getHbXOff() + s.getHbWidth()));
            }

            // Otherwise, the sprite is coming from above, and hitting the top of the tile.
            else {
                s.setY((ytile) * 32 - (s.getHbYOff() + s.getHbHeight()));
                s.setVelocityY(0);
                isFalling = false;
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
            isFalling = true;
        }
        /**
         * Bottom Left & Right
         * CASE: Both feet are firmly planted on the ground.
         */
        else if(bottomLeft && bottomRight && !(topLeft || topRight)){
            ytile = (int)((sy+sh) / tileHeight);
            s.setY((ytile) * 32 - (s.getHbYOff() + s.getHbHeight()));
            s.setVelocityY(0);
            isFalling = false;
        }

        /**
         * Bottom Left & Right
         * Top Right
         * CASE: Running into corner to the sprite's right
         */
        else if(bottomLeft && bottomRight && topRight && !topLeft){
            xtile = (int)((sx+sw) / tileWidth);
            goRight = false;
            s.setX((xtile) * 32 - (s.getHbXOff() + s.getHbWidth()));
            s.setVelocityY(0);
            isFalling = false;
        }

        /**
         * Bottom Left & Right
         * Top Left
         * CASE: Running into corner to the sprite's left
         */
        else if(bottomLeft && bottomRight && topLeft && !topRight){
            xtile = (int)(sx / tileWidth);
            goLeft = false;
            s.setX((xtile+1) * 32 - s.getHbXOff());
            s.setVelocityY(0);
            isFalling = false;
        }

        /**
         * Top Left & Bottom Left
         * CASE: Jumping into a full wall to the sprite's left.
         */
        else if(bottomLeft && topLeft && !(bottomRight || topRight)){
            xtile = (int)(sx / tileWidth);
            goLeft = false;
            isFalling = true;
            s.setX((xtile+1) * 32 - s.getHbXOff());
        }

        /**
         * Top Right & Bottom Right
         * CASE: Jumping into a full wall to the sprite's right.
         */
        else if(bottomRight && topRight && !(topLeft || bottomLeft)){
            xtile = (int)((sx+sw)/tileWidth);
            goRight = false;
            isFalling = true;
            s.setX((xtile) * 32 - (s.getHbXOff() + s.getHbWidth()));
        }

        /**
         * Top Left & Right
         * Bottom Left
         * CASE: Jumping into a full corner wall to the sprite's left.
         */
        else if(bottomLeft && topLeft && topRight && !bottomRight){
            xtile = (int)(sx / tileWidth);
            ytile = (int)(sy / tileHeight);
            goLeft = false;
            s.setX((xtile+1) * 32 - s.getHbXOff());
            s.setY((ytile+1) * 32 - s.getHbYOff());
            s.setVelocityY(0);
            isFalling = true;
        }

        /**
         * Top Left & Right
         * Bottom Right
         * CASE: Jumping into a full corner wall to the sprite's left.
         */
        else if(bottomRight && topLeft && topRight && !bottomLeft){
            xtile = (int)((sx+sw) / tileWidth);
            ytile = (int)(sy / tileHeight);
            goRight = false;
            s.setX((xtile) * 32 - (s.getHbXOff() + s.getHbWidth()));
            s.setY((ytile+1) * 32 - s.getHbYOff());
            s.setVelocityY(0);
            isFalling = true;
        }

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
                jump = false; player.setAnimation(run); break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                goRight = false;
                player.setVelocityX(0f); break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                goLeft = false;
                player.setVelocityX(0f); break;
			default :  break;
		}
	}
}
