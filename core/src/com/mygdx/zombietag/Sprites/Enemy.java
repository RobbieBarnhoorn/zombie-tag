package com.mygdx.zombietag.Sprites;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.zombietag.Screens.PlayScreen;

/**
 * Created by robbie on 2016/12/10.
 */
public abstract class Enemy extends Sprite {

    protected PlayScreen screen;
    protected World world;
    public Body b2body;
    public Vector2 velocity;

    public Enemy(PlayScreen screen, float x, float y) {
        setPosition(x, y);
        this.screen = screen;
        this.world = screen.getWorld();
        define();
        velocity = new Vector2(0, 0);
    }

    public abstract void update(float dt);
    public abstract void define();
    public abstract void setToDestroy();

    public void draw(Batch batch) {
        super.draw(batch);
    }




}
