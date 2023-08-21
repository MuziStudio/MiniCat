package com.mygdx.minicat;

import com.badlogic.gdx.Game;

public class MyGdxGame extends Game {

    @Override
    public void create() {
        AssetsManager.load();
        AssetsManager.music.play();
        setScreen(new MenuScreen(this));
    }

    @Override
    public void dispose() {
        AssetsManager.dispose();
    }
}
