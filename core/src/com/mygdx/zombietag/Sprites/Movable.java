package com.mygdx.zombietag.Sprites;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.zombietag.Screens.PlayScreen;

/**
 * Created by robbie on 2016/12/10.
 */
public abstract class Movable extends Sprite {

    protected World world;
    protected PlayScreen screen;
    public Body b2body;

    public Movable(PlayScreen screen, Vector2 spawn) {
        this.screen = screen;
        this.world = screen.getWorld();
        setPosition(spawn.x, spawn.y);
        define();
    }

    public abstract void update(float dt);
    public abstract void define();
    public abstract void setToDestroy();

    public void draw(Batch batch) {
        super.draw(batch);
    }

    public void push(float force, Vector2 dir) {
        b2body.applyForceToCenter(dir.scl(force), true);
    }




}
