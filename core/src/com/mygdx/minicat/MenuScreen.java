package com.mygdx.minicat;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MenuScreen implements Screen {
    SpriteBatch batch;
    FitViewport uiViewport;
    Stage stage;
    BitmapFont buttonFont, labelFont;
    ImageTextButton playButton;
    Game game;


    public MenuScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        uiViewport = new FitViewport(800, 480);
        uiViewport.apply();
        stage = new Stage(uiViewport);
        Gdx.input.setInputProcessor(stage);
        initMenu();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        stage.act();
        stage.draw();
    }

    void initMenu() {
        Image background = new Image(AssetsManager.menuBackground);
        background.setSize(800, 480);
        buttonFont = new BitmapFont(AssetsManager.font);
        buttonFont.getData().setScale(0.6f);
        labelFont = new BitmapFont(AssetsManager.font);
        labelFont.getData().setScale(1.2f);
        Label.LabelStyle labelStyle = new Label.LabelStyle(labelFont, Color.ORANGE);
        Label label = new Label("Mini Cat", labelStyle);
        label.setPosition(400 - label.getWidth() / 2, 280);
        ImageTextButton.ImageTextButtonStyle playButtonStyle =
                new ImageTextButton.ImageTextButtonStyle(AssetsManager.btOn, AssetsManager.btDown, AssetsManager.btDown, buttonFont);
        playButton = new ImageTextButton("PLAY", playButtonStyle);
        playButton.setTransform(true);
        playButton.setSize(250, 80);
        playButton.setPosition(400 - playButton.getWidth() / 2, 80);
        playButton.addListener(new ClickListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                game.setScreen(new GameScreen());
                super.touchUp(event, x, y, pointer, button);
            }
        });
        stage.addActor(background);
        stage.addActor(label);
        stage.addActor(playButton);
    }


    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
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
        batch.dispose();
        buttonFont.dispose();
        stage.dispose();
    }
}
