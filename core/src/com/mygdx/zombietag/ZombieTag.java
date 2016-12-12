package com.mygdx.zombietag;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.zombietag.Screens.PlayScreen;

public class ZombieTag extends Game {

	public SpriteBatch batch;
    private Screen currentScreen;
    public Music music;

	public static final int V_WIDTH = 16*32;
	public static final int V_HEIGHT = 9*32;
	public static final float PPM = 100;

    // Collision bits
    public static final int NOTHING_BIT = 0;
    public static final int PLAYER_BIT = 1;
    public static final int ZOMBIE_BIT = 2;
    public static final int TRAP_BIT = 4;
    public static final int WALL_BIT = 8;
    public static final int POWER_BIT = 16;
    public static final int PIT_BIT = 32;
    public static final int BIG_ZOMBIE_BIT = 64;
    public static final int TREE_BIT = 128;
    public static final int TEST_BIT = 256;

    public static AssetManager manager;


	@Override
	public void create () {
		batch = new SpriteBatch();
        manager = new AssetManager();

        manager.load("audio/sounds/player_walk.mp3", Sound.class);
        manager.load("audio/sounds/standard_zombie_death.mp3", Sound.class);
        manager.load("audio/sounds/standard_zombie_walk.mp3", Sound.class);
        manager.load("audio/sounds/power_symbol.mp3", Sound.class);
        manager.load("audio/sounds/big_growl_2.mp3", Sound.class);
        manager.load("audio/sounds/tree.mp3", Sound.class);
        manager.load("audio/music/dungeon.mp3", Music.class);
        manager.load("audio/music/game_over.mp3", Music.class);
        manager.finishLoading();

        setScreen(new PlayScreen(this));

	}

	@Override
	public void render () {
        super.render();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}


	@Override
	public void setScreen(Screen screen) {
		currentScreen = screen;
		super.setScreen(screen);
	}

}
