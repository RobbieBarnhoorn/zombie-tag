package com.mygdx.zombietag.Tools;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject; import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.mygdx.zombietag.Screens.PlayScreen;
import com.mygdx.zombietag.Sprites.Player;
import static com.mygdx.zombietag.ZombieTag.*;

/**
 * Created by robbie on 2016/12/10.
 */
public class B2WorldCreator {

    private Player player;

    private PlayScreen screen;
    private World world;
    private TiledMap map;
    private BodyDef bdef;
    private FixtureDef fdef;
    private PolygonShape shape;
    private Body body;

    public B2WorldCreator(PlayScreen screen) {
        this.screen = screen;
        world = screen.getWorld();
        map = screen.getMap();

        // Create body and fixture variables
        fdef = new FixtureDef();
        bdef = new BodyDef();
        shape = new PolygonShape();

        createWalls();
        createPlayer();
    }

    private void createWalls() {
        for (MapObject object : map.getLayers().get(1).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set((rect.getX() + rect.getWidth() / 2) / PPM, (rect.getY() + rect.getHeight() / 2) / PPM);

            body = world.createBody(bdef);

            shape.setAsBox(rect.getWidth() / 2 / PPM, rect.getHeight() / 2 / PPM);
            fdef.shape = shape;
            fdef.filter.categoryBits = WALL_BIT;
            body.createFixture(fdef);
        }
    }

    private void createPlayer() {
        MapObject object = map.getLayers().get(2).getObjects().getByType(RectangleMapObject.class).get(0);
        Rectangle rect = ((RectangleMapObject)object).getRectangle();
        player = new Player(screen, (rect.getX() + rect.getWidth()/2)/PPM,
                (rect.getY() + rect.getHeight()/2)/PPM);
    }

    public Player getPlayer() {
        return player;
    }
}
