package com.mygdx.zombietag.Screens;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.zombietag.Sprites.Player;

import static com.mygdx.zombietag.ZombieTag.*;

/**
 * Created by robbie on 2016/12/12.
 */
public class Hud implements Disposable{


    private PlayScreen screen;
    private SpriteBatch batch;
    private Player player;
    //Scene2D.ui Stage and its own Viewport for HUD
    public Stage stage;
    private Table healthTable;
    private Table manaTable;
    private Viewport viewport;
    private Texture healthTex;
    private Texture manaTex;
    private TextureRegion[][] healthTextures;
    private TextureRegion[][] manaTextures;
    private Animation healthAnimation;
    private Animation manaAnimation;
    private float previousHealth;
    public float manaTime;


    public Hud(PlayScreen screen, SpriteBatch sb){

        this.screen = screen;
        this.batch = sb;
        this.player = screen.getPlayer();

        healthTex = new Texture("sprites/misc/health.png");
        manaTex = new Texture("sprites/misc/mana.png");

        Array<TextureRegion> frames = new Array<TextureRegion>();
        healthTextures = TextureRegion.split(healthTex, 128, 24);
        for (int i = 0; i < healthTextures[0].length; i++) {
            frames.add(healthTextures[0][i]);
        }
        healthAnimation = new Animation(1/12f, frames);
        manaTextures = TextureRegion.split(manaTex, 128, 24);
        frames.clear();
        for (int i = 0; i < manaTextures[0].length; i++) {
            frames.add(manaTextures[0][i]);
        }
        manaAnimation = new Animation(1/4.2f, frames);

        //setup the HUD viewport using a new camera seperate from our gamecam
        //define our stage using that viewport and our games spritebatch
        viewport = new FitViewport(V_WIDTH, V_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, sb);

        healthTable = new Table();
        manaTable = new Table();
        //Top-left align table
        healthTable.bottom();
        manaTable.bottom();
        healthTable.left();
        manaTable.right();

        //make the table fill the entire stage
        healthTable.setFillParent(true);
        manaTable.setFillParent(true);

        healthTable.add(new Image(healthTextures[0][0]));
        manaTable.add(new Image(manaTextures[0][14]));

        //add our table to the stage
        stage.addActor(healthTable);
        stage.addActor(manaTable);

        previousHealth = 1f;
        manaTime = 10;
    }

    public void update(float dt){

        manaTime += dt;
        if (player.getHealth() != previousHealth || player.getMana() < 1) {
            if (player.getHealth() != previousHealth) {
                previousHealth = player.getHealth();
            }

            stage.clear();
            healthTable.clear();
            manaTable.clear();
            healthTable.bottom();
            manaTable.bottom();
            healthTable.left();
            manaTable.right();
            healthTable.setFillParent(true);
            manaTable.setFillParent(true);

            if (player.getHealth() <= 0.2) {
                healthTable.add(new Image(healthTextures[0][5]));
            }
            else if (player.getHealth() <= 0.4) {
                healthTable.add(new Image(healthTextures[0][4]));
            }
            else if (player.getHealth() <= 0.6) {
                healthTable.add(new Image(healthTextures[0][3]));
            }
            else if (player.getHealth() <= 0.8) {
                healthTable.add(new Image(healthTextures[0][2]));
            }
            else if (player.getHealth() < 1) {
                healthTable.add(new Image(healthTextures[0][1]));
            }
            else {
                healthTable.add(new Image(healthTextures[0][0]));
            }

            manaTable.add(new Image(manaAnimation.getKeyFrame(manaTime, false)));


            stage.addActor(healthTable);
            stage.addActor(manaTable);
        }

    }


    @Override
    public void dispose() {
        stage.dispose();
    }

}
