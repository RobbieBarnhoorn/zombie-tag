package com.mygdx.zombietag.Sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.mygdx.zombietag.Screens.PlayScreen;

import static com.mygdx.zombietag.ZombieTag.*;

/**
 * Created by robbie on 2016/12/10.
 */
public abstract class Zombie extends Movable {

    public enum State {WANDERING, CHASING, DEAD, ATTACKING}
    public State currentState;
    public State previousState;

    public enum Movement {LEFT, RIGHT, UP, DOWN,}
    public Array<Movement> movement;

    protected Animation leftRunAnimation;
    protected Animation rightRunAnimation;
    protected Animation upRunAnimation;
    protected Animation downRunAnimation;
    protected Animation deathAnimation;

    public float stateTimer;
    protected float MOVE_SPEED;
    protected final static float SPREAD_FACTOR = 700/PPM;
    protected Vector2 chaseOffset;
    protected float chaseTimer;
    protected boolean setToDestroy;
    protected boolean destroyed;
    protected boolean removable;

    public Zombie(PlayScreen screen, Vector2 spawn) {
        super(screen, spawn);
        currentState = State.WANDERING;
        previousState = State.WANDERING;
        stateTimer = 0;
        chaseOffset = new Vector2((float)Math.random()*SPREAD_FACTOR - 0.5f*SPREAD_FACTOR,
                (float)Math.random()*SPREAD_FACTOR - 0.5f*SPREAD_FACTOR);
        setToDestroy = false;
        destroyed = false;
        removable = false;

        movement = new Array<Movement>();
    }

    public void handleMovement(float dt) {
        // Takes the vector difference of their positions and adds a small random amount to it
        // making zombies less accurate the closer they are (to avoid clumping)
        chaseTimer += dt;
        if (chaseTimer > 3) {
            chaseOffset.set((float)Math.random()*SPREAD_FACTOR - 0.5f*SPREAD_FACTOR,
                    (float)Math.random()*SPREAD_FACTOR - 0.5f*SPREAD_FACTOR);
            chaseTimer = 0;
        }

        Vector2 playerPos = screen.getPlayer().b2body.getPosition();
        Vector2 zombiePos = b2body.getPosition();
        Vector2 diff = new Vector2(playerPos.x - zombiePos.x, playerPos.y - zombiePos.y);
        if (diff.len() > 150/PPM) {
            diff.add(chaseOffset.x, chaseOffset.y);
        }
        diff.scl(1/diff.len());

        Vector2 vel = b2body.getLinearVelocity();
        Vector2 desiredVel = new Vector2(0, 0);
        desiredVel.x += diff.x*MOVE_SPEED;
        desiredVel.y += diff.y*MOVE_SPEED;
        Vector2 velChange = new Vector2(desiredVel.x - vel.x, desiredVel.y - vel.y);
        Vector2 force = new Vector2(b2body.getMass() * velChange.x / dt, b2body.getMass() * velChange.y / dt); // f = mv/t
        b2body.applyForceToCenter(new Vector2(force.x/25, force.y/25), true);
    }


    public State getState(){
        if(setToDestroy || destroyed) {
            return State.DEAD;
        }
        return State.WANDERING;
    }

    public TextureRegion getFrame() {

        currentState = getState();
        TextureRegion region;

        // Get keyFrame corresponding to currentState
        switch(currentState) {
            case DEAD:
                region = deathAnimation.getKeyFrame(stateTimer, true);
                break;
            /*case ATTACKING:
                region = attackAnimation.getKeyFrame(stateTimer);
                break;*/
            case CHASING:
            case WANDERING:
                Vector2 vel = b2body.getLinearVelocity();
                float maxVel = Math.max(Math.abs(vel.x), Math.abs(vel.y));
                if (Math.abs(maxVel) > 0.01) {
                    if (Math.abs(vel.x) == maxVel) {
                        if (vel.x > 0) {
                            region = rightRunAnimation.getKeyFrame(stateTimer, true);
                        }
                        else {
                            region = leftRunAnimation.getKeyFrame(stateTimer, true);
                        }
                    }
                    else {
                        if (vel.y > 0) {
                            region = upRunAnimation.getKeyFrame(stateTimer, true);
                        }
                        else {
                            region = downRunAnimation.getKeyFrame(stateTimer, true);
                        }
                    }
                }
                else {
                    region = downRunAnimation.getKeyFrame(stateTimer, true);
                }
                break;
            default:
                region = downRunAnimation.getKeyFrame(stateTimer, true);
                break;
        }

        //update previous state before we update the current state
        previousState = currentState;
        //return our final adjusted frame
        return region;
    }


    public void draw(Batch batch) {
        super.draw(batch);
    }

    public void setToDestroy() {
        setToDestroy = true;
    }

    public boolean destroyed() {
        return destroyed;
    }

    public boolean removable() {
        return removable;
    }

}
