package com.mygdx.minicat;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class GameTool {
    public static float B2D_SCALE = 1 / 100f;

    public static Button createButton(String up, String down, int x, int y, int width, int height) {
        TextureRegionDrawable upDrawable = new TextureRegionDrawable(new TextureRegion(AssetsManager.screenUIAtlas.findRegion(up)));
        TextureRegionDrawable downDrawable = new TextureRegionDrawable(new TextureRegion(AssetsManager.screenUIAtlas.findRegion(down)));
        Button button = new Button(upDrawable, downDrawable);
        button.setPosition(x, y);
        button.setSize(width, height);
        return button;
    }
}
