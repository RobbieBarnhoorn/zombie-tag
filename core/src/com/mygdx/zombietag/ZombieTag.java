package com.mygdx.zombietag;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
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



	@Override
	public void create () {
		batch = new SpriteBatch();
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
