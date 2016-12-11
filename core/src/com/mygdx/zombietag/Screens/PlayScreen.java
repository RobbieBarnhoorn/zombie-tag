package com.mygdx.zombietag.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.zombietag.Sprites.Movable;
import com.mygdx.zombietag.Sprites.Player;
import com.mygdx.zombietag.Sprites.Zombie;
import com.mygdx.zombietag.Tools.B2WorldCreator;
import com.mygdx.zombietag.Tools.WorldContactListener;
import com.mygdx.zombietag.ZombieTag;
import static com.mygdx.zombietag.ZombieTag.*;

/**
 * Created by robbie on 2016/12/10.
 */
public class PlayScreen implements Screen {

    // Game properties
    private ZombieTag game;
    private OrthographicCamera gamecam;
    private Viewport gameport;
    private float gameTime;

    // Tiled map variables
    private TiledMap map;
    private MapProperties mapProperties;
    private int mapWidth;
    private int mapHeight;
    private int tilePixelWidth;
    private int tilePixelHeight;
    private int mapPxWidth;
    private int mapPxHeight;
    private OrthogonalTiledMapRenderer renderer;

    //Box2d variables
    private World world;
    private Box2DDebugRenderer b2dr;
    private B2WorldCreator creator;

    // Sprites
    private Player player;
    private Array<Zombie> zombies;

    // Variables for smooth panning of camera
    private final static float cameraSpeed = 0.06f;
    private final static float ispeed = 1.0f - cameraSpeed;

    // For printing vision lines to screen (debug)
    private static ShapeRenderer debugRenderer = new ShapeRenderer();
    public Array<Vector2> p1Array;
    public Array<Vector2> p2Array;

    public PlayScreen(ZombieTag game) {
        this.game = game;

        // Camera that follows player throughout world
        gamecam = new OrthographicCamera();
        gamecam.zoom += 0.2;

        // FitViewport maintains virtual aspect ratio, despite screen dimensions
        gameport = new FitViewport(V_WIDTH/PPM, V_HEIGHT/PPM, gamecam);
        gameTime = 0;

        // Load our map and setup our map renderer
        TmxMapLoader maploader = new TmxMapLoader();
        map = maploader.load("map/map.tmx");
        mapProperties = map.getProperties();
        mapWidth = mapProperties.get("width", Integer.class);
        mapHeight = mapProperties.get("height", Integer.class);
        tilePixelWidth = mapProperties.get("tilewidth", Integer.class);
        tilePixelHeight = mapProperties.get("tileheight", Integer.class);
        mapPxWidth = mapWidth * tilePixelWidth;
        mapPxHeight = mapHeight * tilePixelHeight;
        renderer = new OrthogonalTiledMapRenderer(map, 1/PPM);

        //initially set our gamcam to be centered correctly at the start of of map
        gamecam.position.set(gameport.getWorldWidth() / 2, gameport.getWorldHeight() / 2, 0);

        //create our Box2D world, setting no gravity in X or Y, and allow bodies to sleep
        world = new World(new Vector2(0, 0), true);
        World.setVelocityThreshold(0);

        //allows for debug lines of our box2d world.
        b2dr = new Box2DDebugRenderer();

        creator = new B2WorldCreator(this);

        player = creator.getPlayer();
        zombies = new Array<Zombie>();
        for(int i = 0; i < 20; i++) {
            Vector2 pos = new Vector2(((float)(Math.random()*0.6f + 0.7f)*player.b2body.getPosition().x),
                    (float)(Math.random()*0.6f + 0.7f)*player.b2body.getPosition().y);
            zombies.add(new Zombie(this, pos));
        }

        world.setContactListener(new WorldContactListener(game));


    }

    public void handleInput() {
        //Exit
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        // Clear what he was doing last frame
        player.movement.clear();

        //control our player
        if(player.currentState != Player.State.DEAD) {
            if (Gdx.input.isKeyPressed(Input.Keys.X) || Gdx.input.isKeyPressed(Input.Keys.J)) {
                player.push();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
                player.movement.add(Player.Movement.UP);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                player.movement.add(Player.Movement.LEFT);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                player.movement.add(Player.Movement.DOWN);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                player.movement.add(Player.Movement.RIGHT);
            }
        }
    }

    public void update(float dt) {
        // Clear the lines which show us vision
        /*p1Array.clear();
        p2Array.clear();*/

        // Handle user input
        handleInput();

        // Take one step in the physics simulation
        world.step(1/60f, 6, 2);

        // Update the player
        player.update(dt);

        // Update the enemies
        for(Zombie zombie: zombies) {
            if (zombie.destroyed()) {
                zombies.removeValue(zombie, true);
            }
            else zombie.update(dt);
        }

        // Make our camera track our players position smoothly
        Vector3 cameraPosition = new Vector3(gamecam.position);
        cameraPosition.scl(ispeed);
        Vector3 target = new Vector3(player.b2body.getPosition().x, player.b2body.getPosition().y, 0);
        target.scl(cameraSpeed);
        cameraPosition.add(target);

        // Don't allow the camera to show any of the black portion beyond the edge of the map
        if (cameraPosition.x < V_WIDTH/PPM/2) {
            cameraPosition.x = V_WIDTH/PPM/2;
        }
        else if (cameraPosition.x > mapPxWidth/PPM - V_WIDTH/PPM/2) {
            cameraPosition.x = mapPxWidth/PPM - V_WIDTH/PPM/2;
        }
        if (cameraPosition.y < V_HEIGHT/PPM/2) {
            cameraPosition.y = V_HEIGHT/PPM/2;
        }
        else if (cameraPosition.y > mapPxHeight/PPM - V_HEIGHT/PPM/2) {
            cameraPosition.y = mapPxHeight/PPM - V_HEIGHT/PPM/2;
        }
        gamecam.position.set(cameraPosition.x, cameraPosition.y, 0);

        // Update our gamecam with correct coordinates after changes
        gamecam.update();

        // Tell our renderer to draw only what our camera can see in our game world
        renderer.setView(gamecam);
    }

    @Override
    public void render(float dt) {
        // First update the game world, then render it
        update(dt);

        // Clear the game screen with black
        Gdx.gl.glClearColor(0.5f, 0.25f, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render our map
        renderer.render();

        game.batch.setProjectionMatrix(gamecam.combined);
        game.batch.begin();

        // Draw the player
        if (!player.isDestroyed()) player.draw(game.batch);

        // Draw the enemies
        for (Zombie zombie: zombies) {
            zombie.draw(game.batch);
        }


        game.batch.end();

        // Render our Box2DDebugLines
        //b2dr.render(world, gamecam.combined);


        // Render our vision lines
       /* for (int i = 0; i < p1Array.size; i++) {
            drawDebugLine(p1Array.get(i), p2Array.get(i), gamecam.combined);
        }*/
    }

    @Override
    public void show() {

    }

    @Override
    public void resize(int width, int height) {
        // Update our game viewport
        gameport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        //dispose of all our opened resources
        map.dispose();
        renderer.dispose();
        world.dispose();
        b2dr.dispose();
        game.music.dispose();
    }

    public World getWorld() {
        return world;
    }

    public TiledMap getMap() {
        return map;
    }

    public Player getPlayer() {
        return player;
    }

    public Array<Zombie> getEnemies() {
        return zombies;
    }

    public void addRay(Vector2 start, Vector2 end) {
        p1Array.add(start);
        p2Array.add(end);
    }

    public static void drawDebugLine(Vector2 start, Vector2 end, Matrix4 projectionMatrix) {
        Gdx.gl.glLineWidth(2);
        debugRenderer.setProjectionMatrix(projectionMatrix);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);
        debugRenderer.setColor(Color.WHITE);
        debugRenderer.line(start, end);
        debugRenderer.end();
        Gdx.gl.glLineWidth(1);
    }

    public Vector2 getMousePos() {
        return new Vector2(Gdx.input.getX(), Gdx.input.getY());
    }

    public Vector2 getMousePosInGameWorld() {
        Vector3 temp = gamecam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        return new Vector2(temp.x, temp.y);
    }

}
