package com.mygdx.zombietag.Tools;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.zombietag.Sprites.Player;
import com.mygdx.zombietag.Sprites.Power;
import com.mygdx.zombietag.Sprites.Zombie;
import com.mygdx.zombietag.ZombieTag;
import static com.mygdx.zombietag.ZombieTag.*;

/**
 * Created by robbie on 2016/12/10.
 */
public class WorldContactListener implements ContactListener {

    private ZombieTag game;

    public WorldContactListener(ZombieTag game) {
        this.game = game;
    }


    @Override
    public void beginContact(Contact contact) {

        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        // cDef tells us what collided with what
        int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;

        // Depending on what collided with what, handle the collision accordingly
        switch(cDef) {
            case ZOMBIE_BIT | POWER_BIT:
                if (fixA.getUserData() instanceof Zombie) {
                    Vector2 dir = new Vector2(((Power)fixB.getUserData()).b2body.getLinearVelocity());
                    dir.scl(1/dir.len());
                    ((Zombie)fixA.getUserData()).push(2, dir);
                }
                else {
                    Vector2 dir = new Vector2(((Power)fixA.getUserData()).b2body.getLinearVelocity());
                    dir.scl(1/dir.len());
                    ((Zombie)fixB.getUserData()).push(2, dir);
                }
                break;
            case ZOMBIE_BIT | PIT_BIT:
                if (fixA.getUserData() instanceof Zombie) {
                    ((Zombie)fixA.getUserData()).setToDestroy();
                }
                else {
                    ((Zombie)fixB.getUserData()).setToDestroy();
                }
                break;
            case ZOMBIE_BIT | TRAP_BIT:
                if (fixA.getUserData() instanceof Zombie) {
                    ((Zombie)fixA.getUserData()).setToDestroy();
                }
                else {
                    ((Zombie)fixB.getUserData()).setToDestroy();
                }
                break;
            case PLAYER_BIT | PIT_BIT:
                if (fixA.getUserData() instanceof Player) {
                    ((Player)fixA.getUserData()).fall();
                }
                else {
                    ((Player)fixB.getUserData()).fall();
                }
                break;
            case PLAYER_BIT | TRAP_BIT:
                if (fixA.getUserData() instanceof Player) {
                    ((Player)fixA.getUserData()).reduceHealth(0.4f);
                }
                else {
                    ((Player)fixB.getUserData()).reduceHealth(0.4f);
                }
                break;
            case PLAYER_BIT | ZOMBIE_BIT:
                if (fixA.getUserData() instanceof Player) {
                    ((Player)fixA.getUserData()).reduceHealth(0.2f);
                }
                else {
                    ((Player)fixB.getUserData()).reduceHealth(0.2f);
                }
                break;
            case PLAYER_BIT | BIG_ZOMBIE_BIT:
                if (fixA.getUserData() instanceof Player) {
                    ((Player)fixA.getUserData()).reduceHealth(0.4f);
                }
                else {
                    ((Player)fixB.getUserData()).reduceHealth(0.4f);
                }
                break;
            case PLAYER_BIT | TREE_BIT:
                if (fixA.getUserData() instanceof Player) {
                    ((Player)fixA.getUserData()).increaseHealth(0.4f);
                }
                else {
                    ((Player)fixB.getUserData()).increaseHealth(0.4f);
                }
                break;

        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}


