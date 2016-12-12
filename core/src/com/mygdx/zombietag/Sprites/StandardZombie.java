package com.mygdx.zombietag.Sprites;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Array;
import com.mygdx.zombietag.Screens.PlayScreen;
import com.mygdx.zombietag.ZombieTag;

import static com.mygdx.zombietag.ZombieTag.*;

/**
 * Created by robbie on 2016/12/12.
 */
public class StandardZombie extends Zombie{

    private Sound deathSound;
    private Sound walkSound;

    public StandardZombie(PlayScreen screen, Vector2 spawn) {
        super(screen, spawn);

        MOVE_SPEED = 0.55f;

        Texture leftRunSheet = new Texture("sprites/zombies/standard/zombie_left.png");
        Texture rightRunSheet = new Texture("sprites/zombies/standard/zombie_right.png");
        Texture upRunSheet = new Texture("sprites/zombies/standard/zombie_up.png");
        Texture downRunSheet = new Texture("sprites/zombies/standard/zombie_down.png");
        Texture deathSheet = new Texture("sprites/zombies/standard/zombie_death.png");

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
        frames.clear();
        for (int i = 0; i < 15; i++) {
            frames.add(new TextureRegion(deathSheet, i*32, 0, 32, 32));
        }
        deathAnimation = new Animation(1/60f, frames);

        deathSound = ZombieTag.manager.get("audio/sounds/standard_zombie_death.mp3", Sound.class);
        /*walkSound = ZombieTag.manager.get("audio/sounds/standard_zombie_walk.mp3", Sound.class);
        walkSound.loop(0.2f);
        walkSound.play();*/

        // Set initial values for the textures location, width and height
        setBounds(0, 0, 32/PPM, 32/PPM);
        setRegion(downRunAnimation.getKeyFrame(stateTimer, true));
    }

    public void update(float dt) {
        stateTimer += dt;
        if (setToDestroy && !destroyed) {
            world.destroyBody(b2body);
            Vector2 p1 = screen.getPlayer().b2body.getPosition();
            Vector2 p2 = b2body.getPosition();
            if ((p1.sub(p2)).len() < 350/PPM) {
                deathSound.play(1);
            }
            stateTimer = 0;
            destroyed = true;
        }
        else if (destroyed) {
            if (stateTimer < deathAnimation.getAnimationDuration()) {
                setRegion(getFrame());
            }
            else {
                removable = true;
            }
        }
        else if (!destroyed) {
            handleMovement(dt);
            setPosition(b2body.getPosition().x - getWidth() / 2,
                    b2body.getPosition().y - getHeight() / 2 + 6/PPM);
            setRegion(getFrame());

            if (currentState != previousState) {
                stateTimer = 0;
            }

            // Update previous state
            previousState = currentState;
        }
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
        shape.setRadius(5 / PPM);
        fdef.filter.categoryBits = ZOMBIE_BIT;
        fdef.filter.maskBits = PLAYER_BIT| TRAP_BIT | WALL_BIT | ZOMBIE_BIT | POWER_BIT | PIT_BIT | TREE_BIT;
        fdef.shape = shape;
        fdef.density = 1f;
        fdef.friction = 1f;
        b2body.createFixture(fdef).setUserData(this);
    }
}
