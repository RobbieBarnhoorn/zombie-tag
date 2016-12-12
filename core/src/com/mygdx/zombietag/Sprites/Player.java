package com.mygdx.zombietag.Sprites;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.mygdx.zombietag.Screens.PlayScreen;
import com.mygdx.zombietag.ZombieTag;

import static com.mygdx.zombietag.ZombieTag.*;

/**
 * Created by robbie on 2016/12/10.
 */
public class Player extends Sprite {

    public enum State {IDLE, RUNNING, DEAD, POWERING}
    public State currentState;
    public State previousState;

    public enum Movement {LEFT, RIGHT, UP, DOWN,}
    public Array<Movement> movement;


    public  PlayScreen screen;
    private World world;
    public Body b2body;
    public float health;
    private float treeTime;
    public float mana;

    // Character animations
    public Animation leftRunAnimation;
    public Animation rightRunAnimation;
    public Animation upRunAnimation;
    public Animation downRunAnimation;
    public Animation currentAnimation;
    public Animation previousAnimation;
    public Animation deathAnimation;

    // Power animations
    private Animation symbolAnimation;
    public float symbolTime;
    private Power power;
    private float powerCooldown;

    private Sound footsteps;
    private Sound powerSymbol;
    Sound tree;

    private float stateTimer;
    private final static float moveSpeed = 0.95f;
    private boolean setToDestroy;
    private boolean destroyed;
    private boolean removable;

    public Player(PlayScreen screen, float x, float y) {
        setPosition(x, y);
        this.screen = screen;
        this.world = screen.getWorld();
        currentState = State.IDLE;
        previousState = State.IDLE;
        stateTimer = 0;
        symbolTime = 0;
        setToDestroy = false;
        destroyed = false;
        removable = false;
        power = null;
        powerCooldown = 0;
        health = 1f;
        mana = 1f;

        movement = new Array<Movement>();

        Texture movementSheet = new Texture("sprites/player/player_movement.png");
        Texture symbolSheet = new Texture("powers/power_symbol.png");
        Texture deathSheet = new Texture("sprites/player/player_death.png");

        Array<TextureRegion> frames = new Array<TextureRegion>();


        // RUNNING ANIMATIONS

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

        // DEATH ANIMATION

        for (int i = 0; i < 9; i++) {
            frames.add(new TextureRegion(deathSheet, i*32, 0, 32, 32));
        }
        deathAnimation = new Animation(1/12f, frames);



        // POWER SYMBOL ANIMATION

        frames.clear();
        for (int i = 0; i < 20; i++) {
            frames.add(new TextureRegion(symbolSheet, i*32, 0, 32, 32));
        }
        symbolAnimation = new Animation(1/42f, frames);



        footsteps = ZombieTag.manager.get("audio/sounds/player_walk.mp3", Sound.class);
        footsteps.loop();
        footsteps.play(1);

        powerSymbol = ZombieTag.manager.get("audio/sounds/power_symbol.mp3", Sound.class);
        tree = ZombieTag.manager.get("audio/sounds/tree.mp3", Sound.class);

        // Define the player in Box2D
        definePlayer();

        // Set initial values for the textures location, width and height
        setBounds(0, 0, 32/PPM, 32/PPM);
        setRegion(downRunAnimation.getKeyFrame(stateTimer, true));
        currentAnimation = downRunAnimation;
        previousAnimation = currentAnimation;
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
        if (setToDestroy && !destroyed) {
            world.destroyBody(b2body);
            stateTimer = 0;
            destroyed = true;
        }
        else if (destroyed) {
            if (stateTimer < deathAnimation.getAnimationDuration()) {
                setRegion(getFrame(dt));
            }
            else {
                removable = true;
            }
        }
        else if (!destroyed){
            // Get monkeys current state
            symbolTime += dt;
            treeTime -= dt;
            powerCooldown -= dt;
            if (health <= 0.2) { // 0.2 cuz i messed up and ludum dare stress
                setToDestroy();
            }
            if (mana < 1) {
                mana += (1 / 3.5f) * dt;
            }
            System.out.println();
            currentState = getState();
            if (power != null) power.update(dt);
            handleMovement(dt);
            setPosition(b2body.getPosition().x - getWidth() / 2,
                    b2body.getPosition().y - getHeight() / 2 + 12 / PPM);
            setRegion(getFrame(dt));
        }
    }

    public void push() {
        if (power == null && powerCooldown <= 0) {
            mana = 0f;
            screen.hud.manaTime = 0;
            Vector2 dir = new Vector2(0, 0);
            if (currentAnimation == rightRunAnimation) {
                dir.add(1, 0);
            }
            else if (currentAnimation == leftRunAnimation) {
                dir.add(-1, 0);
            }
            else if (currentAnimation == upRunAnimation) {
                dir.add(0, 1);
            }
            else if (currentAnimation == downRunAnimation) {
                dir.add(0, -1);
            }
            dir.scl(1 / dir.len());
            power = new Power(screen, this, b2body.getPosition(), dir);
            powerSymbol.play();
            powerCooldown = 3.5f;
        }
    }

    public State getState(){
        if(setToDestroy || destroyed) {
            return State.DEAD;
        }
        if (Math.abs(b2body.getLinearVelocity().x) < 0.1 && Math.abs(b2body.getLinearVelocity().y) < 0.1) {
            return State.IDLE;
        }
        return State.RUNNING;
    }

    public TextureRegion getFrame(float dt) {
        TextureRegion region;

        // Get keyFrame corresponding to currentState
        switch(currentState) {
            case DEAD:
                region = deathAnimation.getKeyFrame(stateTimer);
                break;
            case POWERING:
            case RUNNING:
                if (Math.abs(b2body.getLinearVelocity().len()) > 0.1) {
                    if (b2body.getLinearVelocity().x > 0.01f) {
                        region = rightRunAnimation.getKeyFrame(stateTimer, true);
                        previousAnimation = currentAnimation;
                        currentAnimation = rightRunAnimation;
                    } else if (b2body.getLinearVelocity().x < -0.01f) {
                        region = leftRunAnimation.getKeyFrame(stateTimer, true);
                        previousAnimation = currentAnimation;
                        currentAnimation = leftRunAnimation;
                    } else if (b2body.getLinearVelocity().y > 0.01f) {
                        region = upRunAnimation.getKeyFrame(stateTimer, true);
                        previousAnimation = currentAnimation;
                        currentAnimation = upRunAnimation;
                    } else {
                        region = downRunAnimation.getKeyFrame(stateTimer, true);
                        previousAnimation = currentAnimation;
                        currentAnimation = downRunAnimation;
                    }
                }
                else {
                    region = previousAnimation.getKeyFrame(stateTimer, true);
                }
                break;
            case IDLE:
            default:
                region = previousAnimation.getKeyFrame(stateTimer, true);
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
        fdef.filter.maskBits = ZOMBIE_BIT | TRAP_BIT | WALL_BIT | PIT_BIT | TREE_BIT;
        fdef.shape = shape;
        fdef.density = 1f;
        fdef.friction = 1f;
        b2body.createFixture(fdef).setUserData(this);
    }

    public void draw(Batch batch) {
        super.draw(batch);
        if (power != null) {
            power.draw(batch);
            batch.draw(symbolAnimation.getKeyFrame(symbolTime, false), b2body.getPosition().x - 10/PPM,
                    b2body.getPosition().y + 21/PPM, 24/PPM, 24/PPM);
        }
        else {
            symbolTime = 0;
        }
    }


    public void setToDestroy() {
        setToDestroy = true;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void removePower() {
        power = null;
    }

    public void reduceHealth(float value) {
        this.health -= value;
    }

    public void increaseHealth(float value) {
        if (treeTime <= 0) {
            this.health += value;
            tree.play();
            treeTime = 35;
        }
    }

    public float getHealth() {
        return health;
    }

    public float getMana() {
        return mana;
    }

    public boolean isRemovable() {
        return removable;
    }
}
