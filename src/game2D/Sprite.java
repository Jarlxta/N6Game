package game2D;

import java.awt.Image;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;

/**
 * This class provides the functionality for a moving animated image or Sprite.
 * 
 * @author David Cairns
 *
 */
public class Sprite {

	// The current Animation to use for this sprite
    private Animation anim;
    private ArrayList<Animation> atkCombo = new ArrayList<Animation>();
    private int comboPointer = 0;

    // Position (pixels)
    private float x;
    private float y;

    // Position (pixels) since last update
    private float lastX;
    private float lastY;

    // Velocity (pixels per millisecond)
    private float dx;
    private float dy;

    // Default Velocities for this sprite
    private float dxDefault = 0.1f; // right-direction value; make negative to flip
    private float jumpDefault = -0.1f; // negative dy, determines the force of the jump.
    private float gravityDefault = 0.002f; // positive dy; determines gravity force. closer to 0 = lower gravity

    private boolean goRight;
    private boolean goLeft;
    private boolean isGrounded;
    private boolean tryJump;

    // Dimensions of the sprite
    private float height;
    private float width;
    private float radius;

    // The scaleX to draw the sprite at where 1 equals normal size
    private double scaleX;
    private double scaleY;
    private boolean scaleXChange;
    private boolean scaleYChange;
    // The rotation to apply to the sprite image
    private double rotation;

    // If render is 'true', the sprite will be drawn when requested
    private boolean render;
    
    // The draw offset associated with this sprite. Used to draw it
    // relative to specific on screen position (usually the player)
    private int xoff=0;
    private int yoff=0;



    // A value to determine whether the sprite has a unique hitbox
    // Also that hitbox's parameters
    private boolean uniqueHitbox;
    private int hbXOff;
    private int hbYOff;
    private int hbWidth;
    private int hbHeight;

    private int trueHbXOff;
    private int trueHbYOff;
    private int flipHbXOff;
    private int flipHbYOff;
    private boolean flippedX;
    private boolean flippedY;
    // Determine whether the sprite is attacking; may require multiple collision boxes in 1 sprite
    private boolean attacking;
    private int atkXOff;
    private int atkYOff;
    private int atkWidth;
    private int atkHeight;
    // Sprite type may determine special interactions with certain tiles or sprites
    private SpriteType spriteType;

    /**
     *  Creates a new Sprite object with the specified Animation.
     *  
     * @param a The animation to use for the sprite.
     * 
     */
    public Sprite(Animation anim) {
        this.anim = anim;
        render = true;
        scaleX = 1.0f;
        scaleY = 1.0f;
        rotation = 0.0f;
    }

    /**
     * Change the animation for the sprite to 'a'.
     *
     * @param a The animation to use for the sprite.
     */
    public void setAnimation(Animation a)
    {
    		anim = a;
    }
    
    /**
     * Set the current animation to the given 'frame'
     * 
     * @param frame The frame to set the animation to
     */
    public void setAnimationFrame(int frame)
    {
    	anim.setAnimationFrame(frame);
    }
    
    /**
     * Pauses the animation at its current frame. Note that the 
     * sprite will continue to move, it just won't animate
     */
    public void pauseAnimation()
    {
    	anim.pause();
    }
    
    /**
     * Pause the animation when it reaches frame 'f'. 
     * 
     * @param f The frame to stop the animation at
     */
    public void pauseAnimationAtFrame(int f)
    {
    	anim.pauseAt(f);
    }
    
    /**
     * Change the speed at which the current animation runs. A
     * speed of 1 will result in a normal animation,
     * 0.5 will be half the normal rate and 2 will double it.
     * 
     * Note that if you change animation, it will run at whatever
     * speed it was previously set to.
     * 
     * @param speed	The speed to set the current animation to.
     */
    public void setAnimationSpeed(float speed)
    {
    	anim.setAnimationSpeed(speed);
    }
    
    /**
     * Starts an animation playing if it has been paused.
     */
    public void playAnimation()
    {
    	anim.play();
    }
    
    /**
     * Returns a reference to the current animation
     * assigned to this sprite.
     * 
     * @return A reference to the current animation
     */
    public Animation getAnimation()
    {
    	return anim;
    }

    public float getLastX() {
        return lastX;
    }

    public void setLastX(float lastX) {
        this.lastX = lastX;
    }

    public float getLastY() {
        return lastY;
    }

    public void setLastY(float lastY) {
        this.lastY = lastY;
    }

    /**
        Updates this Sprite's Animation and its position based
        on the elapsedTime.
        
        @param The time that has elapsed since the last call to update
    */
    public void update(long elapsedTime) {
    	if (!render) return;
        lastX = x;
        lastY = y;
        x += (dx * elapsedTime);
        y += (dy * elapsedTime);
        if (scaleX < 0){
            flippedX = true;
        }
        else {
            flippedX = false;
        }
        if (scaleXChange == true){
            updateHitbox();
        }
        anim.update(elapsedTime);
        width = anim.getImage().getWidth(null);
        height = anim.getImage().getHeight(null);
        if (width > height)
        	radius = width / 2.0f;
        else
        	radius = height / 2.0f;
    }

    /**
        Gets this Sprite's current x position.
    */
    public float getX() {
        return x;
    }

    /**
        Gets this Sprite's current y position.
    */
    public float getY() {
        return y;
    }

    /**
        Sets this Sprite's current x position.
    */
    public void setX(float x) {
        this.x = x;
    }

    /**
        Sets this Sprite's current y position.
    */
    public void setY(float y) {
        this.y = y;
    }

    /**
	    Sets this Sprite's new x and y position.
	*/
	public void setPosition(float x, float y) 
	{
	    this.x = x;
	    this.y = y;
	}

    public void shiftX(float shift)
    {
    	this.x += shift;
    }
    
    public void shiftY(float shift)
    {
    	this.y += shift;
    }
    
    /**
        Gets this Sprite's width, based on the size of the
        current image.
    */
    public int getWidth() {
        return anim.getImage().getWidth(null);
    }

    /**
        Gets this Sprite's height, based on the size of the
        current image.
    */
    public int getHeight() {
        return anim.getImage().getHeight(null);
    }

    /**
    	Gets the sprites radius in pixels
    */
    public float getRadius()
    {
    	return radius;
    }

    /**
        Gets the horizontal velocity of this Sprite in pixels
        per millisecond.
    */
    public float getVelocityX() {
        return dx;
    }

    /**
        Gets the vertical velocity of this Sprite in pixels
        per millisecond.
    */
    public float getVelocityY() {
        return dy;
    }
    

    /**
        Sets the horizontal velocity of this Sprite in pixels
        per millisecond.
    */
    public void setVelocityX(float dx) {
        this.dx = dx;
    }

    /**
        Sets the vertical velocity of this Sprite in pixels
        per millisecond.
    */
    public void setVelocityY(float dy) {
        this.dy = dy;
    }

    /**
    	Sets the horizontal and vertical velocity of this Sprite in pixels
    	per millisecond.
	*/
	public void setVelocity(float dx, float dy) {
		this.dx = dx;
		this.dy = dy;
	}

	/**
		Set the scaleX of the sprite to 's'. If s is 1
		the sprite will be drawn at normal size. If 's'
		is 0.5 it will be drawn at half size. Note that
		scaling and rotation are only applied when
		using the drawTransformed method.
	*/
    public void setScaleX(float s) { if(scaleX != s){scaleXChange = true;} scaleX = s;}

	/**
		Get the current value of the scaling attribute.
		See 'setscaleX' for more information.
	*/
    public double getScaleX()
    {
    	return scaleX;
    }

    public void setScaleY(float s) { if(scaleY != s){scaleYChange = true;}scaleY = s;}

    public double getScaleY() { return scaleY; }

	/**
		Set the rotation angle for the sprite in degrees.
		Note that scaling and rotation are only applied when
		using the drawTransformed method.
	*/
    public void setRotation(double r)
    {
    	rotation = Math.toRadians(r);
    }

	/**
		Get the current value of the rotation attribute.
		in degrees. See 'setRotation' for more information.
	*/
    public double getRotation()
    {
    	return Math.toDegrees(rotation);
    }

    /**
     	Stops the sprites movement at the current position
    */
    public void stop()
    {
    	dx = 0;
    	dy = 0;
    }

    /**
        Gets this Sprite's current image.
    */
    public Image getImage() {
        return anim.getImage();
    }

	/**
		Draws the sprite with the graphics object 'g' at
		the current x and y co-ordinates. Scaling and rotation
		transforms are NOT applied.
	*/
    public void draw(Graphics2D g)
    {
    	if (!render) return;

    	g.drawImage(getImage(),(int)x+xoff,(int)y+yoff,null);
    }

    /**
		Draws the bounding box of this sprite using the graphics object 'g' and
		the currently selected foreground colour.
	*/
    public void drawBoundingBox(Graphics2D g)
    {
    	if (!render) return;

		Image img = getImage();
        if (uniqueHitbox){
            g.drawRect((int)x+xoff+hbXOff, (int)y+yoff+hbYOff, hbWidth, hbHeight);
        }
        else {
            g.drawRect((int) x + xoff, (int) y + yoff,img.getWidth(null),img.getHeight(null));
        }
    }

    /**
     * Overloaded method to allow the drawing of bounding boxes that are smaller than the sprite image.
     * Use case is for sprites with attacks in their sprite animation.
     * Defunct; incorporated in  the default draw bounding box method
     * @param g - graphics 2d object where everything is being drawn
     * @param xo - further x offset to locate the x location of the top-left corner of the desired hitbox
     * @param yo - same offset as above but for the y location instead of x.
     * @param width - desired width of the hitbox
     * @param height - desired height of the hitbox
     */
//    public void drawBoundingBox(Graphics2D g, int xo, int yo, int width, int height){
//        if (!render) return;
//
//        Image img = getImage();
//        g.drawRect((int)x+xoff+xo, (int)y+yoff+yo, width, height);
//    }
    
    /**
		Draws the bounding circle of this sprite using the graphics object 'g' and
		the currently selected foreground colour.
	*/
    public void drawBoundingCircle(Graphics2D g)
    {
    	if (!render) return;

		Image img = getImage();
		
    	g.drawArc((int)x+xoff,(int)y+yoff,img.getWidth(null),img.getHeight(null),0, 360);
    }
    
	/**
		Draws the sprite with the graphics object 'g' at
		the current x and y co-ordinates with the current scaling
		and rotation transforms applied.
		
		@param g The graphics object to draw to,
	*/
    public void drawTransformed(Graphics2D g)
    {
    	if (!render) return;

		AffineTransform transform = new AffineTransform();
        double flipX = 0;
        double flipY = 0;
        if (scaleX < 0){
            flipX = width * -scaleX;
        }
		transform.translate(Math.round(x)+xoff+flipX,Math.round(y)+yoff);
		transform.scale(scaleX,scaleY);
		transform.rotate(rotation,getImage().getWidth(null)/2,getImage().getHeight(null)/2);
		// Apply transform to the image and draw it
		g.drawImage(getImage(),transform,null);
    }


	/**
		Hide the sprite.
	*/
    public void hide()  {	render = false;  }

	/**
		Show the sprite
	*/
    public void show()  {  	render = true;   }

	/**
		Check the visibility status of the sprite.
	*/
    public boolean isVisible() { return render; }

	/**
		Set an x & y offset to use when drawing the sprite.
		Note this does not affect its actual position, just
		moves the drawn position.
	*/
    public void setOffsets(int x, int y)
    {
    	xoff = x;
    	yoff = y;
    }

    public int getXoff() {
        return xoff;
    }

    public int getYoff() {
        return yoff;
    }

    /**
     * Get the default horizontal speed for this sprite.
     * @return float dxDefault
     */
    public float getDxDefault() {
        return dxDefault;
    }

    /**
     * Replace the default horizontal speed for this sprite.
     * @param dxDefault - the speed as a float value. Should be positive!
     */
    public void setDxDefault(float dxDefault) {
        this.dxDefault = dxDefault;
    }

    /**
     * Get the default starting speed of a jump for this sprite. Higher value = higher jumps.
     * @return
     */
    public float getJumpDefault() {
        return jumpDefault;
    }

    /**
     * Replace the default starting speed of a jump for this sprite. Higher value = higher jumps.
     * @param  - the jump force as a float value.
     */
    public void setJumpDefault(float jumpDefault) {
        this.jumpDefault = jumpDefault;
    }

    /**
     * Get the default force of gravity for this sprite. Closer to 0 = lower gravity.
     * @return
     */
    public float getGravityDefault() {
        return gravityDefault;
    }

    /**
     * Replace the
     * @param gravityDefault - the gravity force as a float value.
     */
    public void setGravityDefault(float gravityDefault) {
        this.gravityDefault = gravityDefault;
    }

    /**
     * Determine whether the sprite is trying to go right.
     * If the sprite is knocked to the left but still facing right this should remain true!
     * @return
     */
    public boolean isGoRight() {
        return goRight;
    }

    /**
     * Declare whether the srpite is trying to go to the right.
     * Even if the sprite is knocked to the left but still facing right this should remain true!
     * @param goRight
     */
    public void setGoRight(boolean goRight) {
        this.goRight = goRight;
    }

    /**
     * Determine whether the sprite is trying to go to the left.
     * Even if the sprite is knocked to the right but still facing left this should remain true!
     * @return
     */
    public boolean isGoLeft() {
        return goLeft;
    }

    /**
     * Declare whether the sprite is trying to go left.
     * If the sprite is knocked to the right but still facing left this should remain true!
     * @param goLeft
     */
    public void setGoLeft(boolean goLeft) {
        this.goLeft = goLeft;
    }

    /**
     * Determine whether the sprite is on the ground or not.
     * @return
     */
    public boolean isGrounded() {
        return isGrounded;
    }

    /**
     * Declare whether the sprite is on the ground or not.
     * @param grounded
     */
    public void setGrounded(boolean grounded) {
        isGrounded = grounded;
    }

    /**
     * Determine whether the sprite is trying to jump.
     * @return
     */
    public boolean isTryJump() {
        return tryJump;
    }

    /**
     * Declare whether the sprite is trying to jump.
     * @param tryJump
     */
    public void setTryJump(boolean tryJump) {
        this.tryJump = tryJump;
    }
    /**
     * A method to get the center X coordinate of a sprite.
     * I have a feeling I will need to calculate this a lot, so might as well make these methods.
     */

    public int getCenterX() {
        return ((int) x + getWidth());
    }

    /**
     * Same as before, but for the Y Coordinate.
     */
    public int getCenterY (){
        return ((int) y + getHeight());
    }

    /**
     * Get/Set a unique hitbox; drawing a box that's different from the sprite size.
     * It's unlikely that this will ever have to be explicitly set to false, which is why
     * I feel comfortable adding the parameters into the setter.
     */
    public boolean isUniqueHitbox() {return uniqueHitbox;}

    public void setUniqueHitbox(boolean uniqueHitbox, float xo, float yo, int newWidth, int newHeight) {
        this.uniqueHitbox = uniqueHitbox;

        if (uniqueHitbox) {
            hbXOff = (int) xo;
            hbYOff = (int) yo;
            hbWidth = newWidth;
            hbHeight = newHeight;
            trueHbXOff = (int) xo;
            trueHbYOff = (int) yo;
            flipHbXOff = (int) width - hbXOff - hbWidth;
            flipHbYOff = (int) height - hbYOff - hbHeight;
        }
    }

    /**
     * This method tries to ensure two things; one, that the image doesn't flip around the (0,0) coordinate of the sprite
     * second, it tries to ensure that the hitbox does not move when flipping the image, just the sprite.
     */
    public void updateHitbox(){
        if (flippedX == false) {
            hbXOff = trueHbXOff;
            shiftX(-((width/2) - trueHbXOff - hbWidth));
        }
        else {
            hbXOff = flipHbXOff + (int) width;
            shiftX((width/2) - trueHbXOff - hbWidth);
        }
        if (flippedY == false){
            hbYOff = trueHbYOff;
        }
        else {
            hbYOff = flipHbYOff;
        }
        scaleXChange = false;
        scaleYChange = false;
    }

    /**
     * Get/Set the sprite type; if I get to it, functionality with certain types of tiles
     */
    public SpriteType getSpriteType() {return spriteType;}
    public void setSpriteType(SpriteType type){spriteType = type;}

    /**
     * Get/Set an attack flag; this may be the wrong way to implement this,
     * but if this is true, then a new sprite may have to be generated for a projectile
     * or a second bounding box for a melee attack may have to be drawn.
     */
    public boolean getAttacking() {return attacking;}
    public void setAttacking(boolean isAttacking) {attacking = isAttacking;}

    /**
     *   Unique Hitbox items
     */
    public int getHbXOff() {return hbXOff;}
    public void setHbXOff(int hbXOff) {this.hbXOff = hbXOff;}

    public int getHbYOff() {return hbYOff;}
    public void setHbYOff(int hbYOff) {this.hbYOff = hbYOff;}

    public int getHbWidth() {return hbWidth;}
    public void setHbWidth(int hbWidth) {this.hbWidth = hbWidth;}

    public int getHbHeight() {return hbHeight;}
    public void setHbHeight(int hbHeight) {this.hbHeight = hbHeight;}

    public void createCombo(Animation... attack){
        atkCombo.clear();
        for (Animation atk : attack) {
            atkCombo.add(atk);
        }
    }

    public int getComboLength() {
        return atkCombo.size();
    }

    public Animation getFirstAttack() {
        return atkCombo.get(0);
    }
    public Animation getNextAttack() {
        if (atkCombo.size() <= 1){
            return atkCombo.get(0);
        }
        else {
            if (comboPointer + 1 >= atkCombo.size()){
                comboPointer = 0;
            }
            else {
                comboPointer++;
            }
            return atkCombo.get(comboPointer);
        }
    }
}
