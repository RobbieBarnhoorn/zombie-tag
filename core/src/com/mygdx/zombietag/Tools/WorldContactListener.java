package com.mygdx.zombietag.Tools;

import com.badlogic.gdx.physics.box2d.*;
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


