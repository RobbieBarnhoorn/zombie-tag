package com.mygdx.zombietag.Sprites;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.mygdx.zombietag.Screens.PlayScreen;
import com.mygdx.zombietag.ZombieTag;

import static com.mygdx.zombietag.ZombieTag.*;

/**
 * Created by robbie on 2016/12/11.
 */
public class Power extends Movable {

    private Player player;
    private Texture tex;
    private Animation wave;
    float stateTimer;

    private boolean setToDestroy;
    private boolean destroyed;
    private final static float VELOCITY = 3f;

    private Sound whoosh;


    public Power(PlayScreen screen, Player player, Vector2 spawn, Vector2 dir) {
        super(screen, spawn);
        this.player = player;
        stateTimer = 0;
        tex = new Texture("powers/wave.png");
        Array<TextureRegion> frames = new Array<TextureRegion>();

        for (int i = 0; i < 16; i++) {
            frames.add(new TextureRegion(tex, 0, i*32, 48, 32));
        }
        wave = new Animation(1/24f, frames);

        // Set initial values for the textures location, width and height
        setBounds(0, 0, 48/PPM, 32/PPM);
        setRegion(wave.getKeyFrame(stateTimer, false));
        setOriginCenter();
        if (player.currentAnimation == player.leftRunAnimation) {
            setRotation(90);
        }
        else if (player.currentAnimation == player.downRunAnimation) {
            setRotation(180);
        }
        else if (player.currentAnimation == player.rightRunAnimation) {
            setRotation(270);
        }
        b2body.applyLinearImpulse(dir.scl(VELOCITY), b2body.getWorldCenter(), true);
        setToDestroy = false;
        destroyed = false;

        whoosh = ZombieTag.manager.get("audio/sounds/whoosh.mp3", Sound.class);
        whoosh.play();
    }

    public void update(float dt) {
        stateTimer += dt;
        if (setToDestroy && !destroyed) {
            world.destroyBody(b2body);
            dispose();
            player.removePower();
            player.symbolTime = 0;
            destroyed = true;
        }
        else if (!destroyed) {
            if (stateTimer > 0.5) {
                setToDestroy();
            }
            else {
                setRegion(wave.getKeyFrame(stateTimer, false));
            }

            // Move the TextureRegion to where the b2body is
            setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);
        }
    }

    public void define() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(getX(), getY());
        bdef.type = BodyDef.BodyType.DynamicBody;

        b2body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(30/PPM, 30/PPM);
        FixtureDef fdef = new FixtureDef();
        fdef.filter.categoryBits = POWER_BIT;
        fdef.filter.maskBits = ZOMBIE_BIT;
        fdef.shape = shape;
        fdef.isSensor = true;
        b2body.createFixture(fdef).setUserData(this);
    }

    public void setToDestroy() {
        setToDestroy = true;
    }

    public void draw(Batch batch) {
        if (!destroyed) {
            super.draw(batch);
        }
    }

    public void dispose() {
        tex.dispose();
    }


}
