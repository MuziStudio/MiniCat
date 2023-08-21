package com.mygdx.minicat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class AssetsManager {
    public static Texture catTexture, birdTexture, background1, box, menuBackground;
    public static TextureAtlas screenUIAtlas;
    public static TextureRegionDrawable btOn, btDown;
    public static FileHandle font;
    public static FileHandle failShaderVert, failShaderFrag;
    public static Music music;
    public static Sound hurt, jump, win;

    public static void load() {
        catTexture = new Texture("cat.png");
        birdTexture = new Texture("BirdEnemy.png");
        background1 = new Texture("Background1.png");
        box = new Texture("Box.png");
        menuBackground = new Texture("MenuBackground.png");
        screenUIAtlas = new TextureAtlas("ScreenUI.atlas");
        btOn = new TextureRegionDrawable(new Texture("bton.png"));
        btDown = new TextureRegionDrawable(new Texture("btdown.png"));
        font = Gdx.files.internal("font.fnt");
        failShaderVert = Gdx.files.internal("shader/failShader.vert");
        failShaderFrag = Gdx.files.internal("shader/failShader.frag");
        music = Gdx.audio.newMusic(Gdx.files.internal("music.wav"));
        music.setLooping(true);
        music.setVolume(0.2f);
        hurt = Gdx.audio.newSound(Gdx.files.internal("Hurt.wav"));
        jump = Gdx.audio.newSound(Gdx.files.internal("Jump.wav"));
        win = Gdx.audio.newSound(Gdx.files.internal("win.wav"));
    }

    public static void dispose() {
        catTexture.dispose();
        birdTexture.dispose();
        background1.dispose();
        screenUIAtlas.dispose();
        music.dispose();
    }
}
