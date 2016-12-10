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
public class Zombie extends Enemy {

    public enum State {WANDERING, CHASING, DEAD, ATTACKING}
    public State currentState;
    public State previousState;

    public enum Movement {LEFT, RIGHT, UP, DOWN,}
    public Array<Movement> movement;

    private Animation leftRunAnimation;
    private Animation rightRunAnimation;
    private Animation upRunAnimation;
    private Animation downRunAnimation;
    private Animation deathAnimation;

    private float stateTimer;
    private final static float moveSpeed = 0.9f;
    private boolean dead;
    private boolean forcing;

    public Zombie(PlayScreen screen, float x, float y) {
        super(screen, x, y);
        currentState = State.WANDERING;
        previousState = State.WANDERING;
        stateTimer = 0;
        dead = false;

        movement = new Array<Movement>();

        Texture leftRunSheet = new Texture("sprites/zombies/zombie_left.png");
        Texture rightRunSheet = new Texture("sprites/zombies/zombie_right.png");
        Texture upRunSheet = new Texture("sprites/zombies/zombie_up.png");
        Texture downRunSheet = new Texture("sprites/zombies/zombie_down.png");
        //Texture deathSheet = new Texture("sprites/player/player_death");

        Array<TextureRegion> frames = new Array<TextureRegion>();

        // Create running animations
        for (int i = 0; i < 6; i++) {
            frames.add(new TextureRegion(leftRunSheet, i*32, 0, 32, 32));
        }
        leftRunAnimation = new Animation(1/12f, frames);

        frames.clear();
        for (int i = 0; i < 6; i++) {
            frames.add(new TextureRegion(rightRunSheet, i*32, 0, 32, 32));
        }
        rightRunAnimation = new Animation(1/12f, frames);

        frames.clear();
        for (int i = 0; i < 6; i++) {
            frames.add(new TextureRegion(upRunSheet, i*32, 0, 32, 32));
        }
        upRunAnimation = new Animation(1/12f, frames);

        frames.clear();
        for (int i = 0; i < 6; i++) {
            frames.add(new TextureRegion(downRunSheet, i*32, 0, 32, 32));
        }
        downRunAnimation = new Animation(1/12f, frames);

        // Create deathAnimation animation
        /*for (int i = 0; i < 3; i++) {
            frames.add(new TextureRegion(deathSheet, i*32, 0, 32, 32));
        }*/
        //deathAnimation = new Animation(1/12f, frames);

        // Set initial values for the textures location, width and height
        setBounds(0, 0, 32/PPM, 32/PPM);
        setRegion(downRunAnimation.getKeyFrame(stateTimer, true));
    }

    public void handleMovement(float dt) {
        Vector2 playerPos = screen.getPlayer().b2body.getPosition();
        Vector2 zombiePos = b2body.getPosition();
        Vector2 diff = new Vector2(playerPos.x - zombiePos.x, playerPos.y - zombiePos.y);
        diff.scl(1/diff.len());
        b2body.applyForceToCenter(diff, true);
    }

    public void update(float dt) {
        // Get current state
        currentState = getState();
        handleMovement(dt);
        setPosition(b2body.getPosition().x - getWidth() / 2,
                b2body.getPosition().y - getHeight() / 2);
        setRegion(getFrame(dt));
        System.out.println("x" + b2body.getPosition().x);
        System.out.println("y" + b2body.getPosition().y);
    }

    public State getState(){
        if(dead) {
            return State.DEAD;
        }
        return State.WANDERING;
    }

    public TextureRegion getFrame(float dt) {
        TextureRegion region;

        // Get keyFrame corresponding to currentState
        switch(currentState) {
            /*case DEAD:
                region = deathAnimation.getKeyFrame(stateTimer);
                break;
            case ATTACKING:
                region = attackAnimation.getKeyFrame(stateTimer);
                break;
            case CHASING:
                region = forceAnimation.getKeyFrame(stateTimer);
                break;*/
            case WANDERING:
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
                region = downRunAnimation.getKeyFrame(stateTimer, true);
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
    public void define() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(getX(), getY());
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / PPM);
        fdef.filter.categoryBits = ZOMBIE_BIT;
        fdef.filter.maskBits = PLAYER_BIT| TRAP_BIT | WALL_BIT | ZOMBIE_BIT;
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

    public void setToDestroy() {

    }
}
