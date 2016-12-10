package com.mygdx.zombietag.Sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.mygdx.zombietag.Screens.PlayScreen;
import static com.mygdx.zombietag.ZombieTag.*;

/**
 * Created by robbie on 2016/12/10.
 */
public class Player extends Sprite {

    public enum State {IDLE, RUNNING, DEAD, FORCING}
    public State currentState;
    public State previousState;

    public enum Movement {LEFT, RIGHT, UP, DOWN,}
    public Array<Movement> movement;

    public World world;
    public Body b2body;

    private Animation leftRunAnimation;
    private Animation rightRunAnimation;
    private Animation upRunAnimation;
    private Animation downRunAnimation;
    private Animation deathAnimation;

    private float stateTimer;
    private final static float moveSpeed = 1.1f;
    private boolean dead;
    private boolean forcing;

    public Player(PlayScreen screen, float x, float y) {
        setPosition(x, y);
        this.world = screen.getWorld();
        currentState = State.IDLE;
        previousState = State.IDLE;
        stateTimer = 0;
        dead = false;

        movement = new Array<Movement>();

        Texture movementSheet = new Texture("sprites/player/player_movement.png");
        //Texture deathSheet = new Texture("sprites/player/player_death");

        Array<TextureRegion> frames = new Array<TextureRegion>();

        // Create running animations
        for (int i = 0; i < 4; i++) {
            frames.add(new TextureRegion(movementSheet, i*32, 32, 32, 32));
        }
        leftRunAnimation = new Animation(1/12f, frames);

        frames.clear();
        for (int i = 0; i < 4; i++) {
            frames.add(new TextureRegion(movementSheet, i*32, 64, 32, 32));
        }
        rightRunAnimation = new Animation(1/12f, frames);

        frames.clear();
        for (int i = 0; i < 4; i++) {
            frames.add(new TextureRegion(movementSheet, i*32, 96, 32, 32));
        }
        upRunAnimation = new Animation(1/12f, frames);

        frames.clear();
        for (int i = 0; i < 4; i++) {
            frames.add(new TextureRegion(movementSheet, i*32, 0, 32, 32));
        }
        downRunAnimation = new Animation(1/12f, frames);

        // Create deathAnimation animation
        /*for (int i = 0; i < 3; i++) {
            frames.add(new TextureRegion(deathSheet, i*32, 0, 32, 32));
        }*/
        //deathAnimation = new Animation(1/12f, frames);

        // Define the player in Box2D
        definePlayer();

        // Set initial values for the textures location, width and height
        setBounds(0, 0, 32/PPM, 32/PPM);
        setRegion(downRunAnimation.getKeyFrame(stateTimer, true));
    }

    public void handleMovement(float dt) {
        Vector2 vel = b2body.getLinearVelocity();
        Vector2 desiredVel = new Vector2(0, 0);
        for (int i = 0; i < movement.size; i++) {
            if (movement.get(i) == Movement.LEFT) {
                desiredVel.x -= moveSpeed;
            }
            if (movement.get(i) == Movement.RIGHT) {
                desiredVel.x += moveSpeed;
            }
            if (movement.get(i) == Movement.UP) {
                desiredVel.y += moveSpeed;
            }
            if (movement.get(i) == Movement.DOWN) {
                desiredVel.y -= moveSpeed;
            }

        }
        Vector2 velChange = new Vector2(desiredVel.x - vel.x, desiredVel.y - vel.y);
        Vector2 force = new Vector2(b2body.getMass() * velChange.x / dt, b2body.getMass() * velChange.y / dt); // f = mv/t
        b2body.applyForceToCenter(new Vector2(force.x, force.y), true);
    }

    public void update(float dt) {
        // Get monkeys current state
        currentState = getState();
        handleMovement(dt);
        setPosition(b2body.getPosition().x - getWidth() / 2,
                b2body.getPosition().y - getHeight() / 2);
        setRegion(getFrame(dt));
    }

    public State getState(){
        if(dead) {
            return State.DEAD;
        }
        if (b2body.getLinearVelocity().x == 0 && b2body.getLinearVelocity().y == 0) {
            return State.IDLE;
        }
        if (forcing) {
            return State.FORCING;
        }
        return State.RUNNING;
    }

    public TextureRegion getFrame(float dt) {
        TextureRegion region;

        // Get keyFrame corresponding to currentState
        switch(currentState) {
            /*case DEAD:
                region = deathAnimation.getKeyFrame(stateTimer);
                break;
            case FORCING:
                region = forceAnimation.getKeyFrame(stateTimer);
                break;*/
            case RUNNING:
                if (b2body.getLinearVelocity().x > 0.01f) {
                    region = rightRunAnimation.getKeyFrame(stateTimer, true);
                }
                else if (b2body.getLinearVelocity().x < -0.01f) {
                    region = leftRunAnimation.getKeyFrame(stateTimer, true);
                }
                else if (b2body.getLinearVelocity().y > 0.01f) {
                    region = upRunAnimation.getKeyFrame(stateTimer, true);
                }
                else {
                    region = downRunAnimation.getKeyFrame(stateTimer, true);
                }
                break;
            default:
                region = downRunAnimation.getKeyFrame(stateTimer);
        }

        //if the current state is the same as the previous state increase the state timer.
        //otherwise the state has changed and we need to reset timer.
        if (currentState == previousState) {
            stateTimer += dt;
        }
        else {
            stateTimer = 0;
        }

        //update previous state before we update the current state
        previousState = currentState;
        //return our final adjusted frame
        return region;
    }

    /**
     * Define the player in Box2D
     */
    public void definePlayer() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(getX(), getY());
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / PPM);
        fdef.filter.categoryBits = PLAYER_BIT;
        fdef.filter.maskBits = ZOMBIE_BIT | TRAP_BIT;
        fdef.shape = shape;
        fdef.density = 1f;
        fdef.friction = 1f;
        b2body.createFixture(fdef).setUserData(this);
    }

    public void draw(Batch batch) {
        super.draw(batch);
    }

    public void kill() {
        dead = true;
    }

    public boolean isDead() {
        return dead;
    }
}
