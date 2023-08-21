package com.mygdx.minicat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameScreen implements Screen {
    SpriteBatch batch, backgroundBatch;
    FitViewport fitViewport;
    Stage uiStage;
    OrthographicCamera camera, backgroundCamera;
    Button leftButton, rightButton, jumpButton, attackButton, restartButton;
    Label.LabelStyle deadLabelStyle, winLabelStyle;
    public Label restartLabel;
    BitmapFont font;
    World world;
    //Box2DDebugRenderer debugRenderer;
    ShaderProgram failShader, defaultShader;
    FrameBuffer fbo;

    TiledMap map;
    TiledMapRenderer mapRenderer;
    float mapScale = 36f;
    float deadTimer = 0.0f, winTimer = 0.0f;
    boolean isDead = false, isWin = false;
    Texture fboTexture;
    TextureRegion fboTextureRegion;
    Player player;
    Array<BirdEnemy> birdEnemies;
    Array<Box> boxes;
    int currentLevel = 1, maxLevel = 2;

    @Override
    public void show() {
        batch = new SpriteBatch();
        backgroundBatch = new SpriteBatch();
        fitViewport = new FitViewport(800, 480);
        uiStage = new Stage(fitViewport);
        camera = new OrthographicCamera(800 * GameTool.B2D_SCALE, 480 * GameTool.B2D_SCALE);
        backgroundCamera = new OrthographicCamera(800, 480);
        font = new BitmapFont(AssetsManager.font);
        font.getData().setScale(0.5f);
        leftButton = GameTool.createButton("flatDark23", "flatLight22", 20, 20, 100, 100);
        rightButton = GameTool.createButton("flatDark24", "flatLight23", 140, 20, 100, 100);
        attackButton = GameTool.createButton("flatDark48", "flatLight47", 680, 20, 100, 100);
        jumpButton = GameTool.createButton("flatDark44", "flatLight43", 560, 20, 100, 100);
        restartButton = GameTool.createButton("flatDark27", "flatLight26", 10, 410, 60, 60);
        deadLabelStyle = new Label.LabelStyle(font, Color.RED);
        winLabelStyle = new Label.LabelStyle(font, Color.GREEN);
        restartLabel = new Label("Press any button to restart", deadLabelStyle);
        restartLabel.setVisible(false);
        uiStage.addActor(leftButton);
        uiStage.addActor(rightButton);
        uiStage.addActor(attackButton);
        uiStage.addActor(jumpButton);
        uiStage.addActor(restartLabel);
        uiStage.addActor(restartButton);
        Gdx.input.setInputProcessor(uiStage);
        world = new World(new Vector2(0, -9.8f), true);
        //debugRenderer = new Box2DDebugRenderer();

        failShader = new ShaderProgram(AssetsManager.failShaderVert, AssetsManager.failShaderFrag);
        defaultShader = SpriteBatch.createDefaultShader();
        ShaderProgram.pedantic = false;
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);

        prepareButton();
        player = new Player(world);
        loadGame(currentLevel);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        double dt = delta / (1 / 120f);
        for (int i = 0; i < dt; i++) {
            world.step(1 / 120f, 6, 4);
        }
        batch.setProjectionMatrix(camera.combined);
        backgroundCamera.position.set(400, 240, 0);
        backgroundCamera.update();
        backgroundBatch.setShader(defaultShader);
        backgroundBatch.setProjectionMatrix(backgroundCamera.combined);
        fbo.begin();
        ScreenUtils.clear(0, 0, 0, 1);
        backgroundBatch.begin();
        backgroundBatch.draw(AssetsManager.background1, 0, 0, 800, 480);
        backgroundBatch.end();
        mapRenderer.setView(camera);
        mapRenderer.render();
        batch.begin();
        for (BirdEnemy enemy : birdEnemies) {
            enemy.render(batch);
            enemy.logic();
            if (((B2dUserData) (enemy.body.getUserData())).isDead) {
                world.destroyBody(enemy.body);
                birdEnemies.removeValue(enemy, false);
            }
        }
        for (Box box : boxes) {
            box.render(batch);
            box.logic();
        }
        player.render(batch);
        batch.end();
        //debugRenderer.render(world, camera.combined);
        fbo.end();
        player.logic();
        fitViewport.apply();
        fboTexture = fbo.getColorBufferTexture();
        fboTextureRegion = new TextureRegion(fboTexture);
        fboTextureRegion.flip(false, true);
        if (isDead) {
            if (deadTimer < 2.0f) {
                deadTimer += Gdx.graphics.getDeltaTime() * 2.0f;
            }
        } else {
            if (deadTimer > 0.0f) {
                deadTimer -= Gdx.graphics.getDeltaTime() * 2.0f;
            }
        }
        if (isWin) {
            if (winTimer < 3.0f) {
                winTimer += Gdx.graphics.getDeltaTime();
            } else {
                currentLevel++;
                loadGame(currentLevel);
            }
        }
        failShader.bind();
        failShader.setUniformf("progress", Math.min(deadTimer, 1.0f));
        backgroundBatch.setShader(failShader);
        backgroundBatch.begin();
        backgroundBatch.draw(fboTextureRegion, 0, 0, 800, 480);
        backgroundBatch.end();
        uiStage.act();
        uiStage.draw();
        if (player.state == Player.State.Dead && !isDead) {
            setDeadState(true);
        }
        if (player.state == Player.State.Win && !isWin) {
            AssetsManager.win.play();
            setWinState(true);
        }
        cameraWithPlayer();
    }

    public void setDeadState(boolean isDie) {
        if (isDie) {
            restartLabel.setStyle(deadLabelStyle);
            restartLabel.setText("Press any button to restart");
            restartLabel.setPosition(400 - restartLabel.getPrefWidth() / 2, 240);
            restartLabel.setVisible(true);
            isDead = true;
            deadTimer = 0.0f;
        } else {
            deadTimer = 1.0f;
            player.restartPlayer();
            cameraWithPlayer();
            restartLabel.setVisible(false);
            isDead = false;
        }
    }

    public void setWinState(boolean isW) {
        if (isW) {
            restartLabel.setStyle(winLabelStyle);
            restartLabel.setText("Congratulations!You are WIN!");
            restartLabel.setPosition(400 - restartLabel.getPrefWidth() / 2, 260);
            restartLabel.setVisible(true);
            isWin = true;
            winTimer = 0.0f;
        } else {
            restartLabel.setVisible(false);
            isWin = false;
        }
    }

    private void prepareButton() {
        leftButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                player.walk(false);
                if (player.state == Player.State.Dead && deadTimer >= 2.0f) {
                    setDeadState(false);
                }
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                player.stop();
                player.tempSpeed = Vector2.Zero;
                super.touchUp(event, x, y, pointer, button);
            }
        });

        rightButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                player.walk(true);
                if (player.state == Player.State.Dead && deadTimer >= 2.0f) {
                    setDeadState(false);
                }
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                player.stop();
                player.tempSpeed = Vector2.Zero;
                super.touchUp(event, x, y, pointer, button);
            }
        });
        jumpButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                player.jump();
                if (player.state == Player.State.Dead && deadTimer >= 2.0f) {
                    setDeadState(false);
                }
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        attackButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                player.attack();
                if (player.state == Player.State.Dead && deadTimer >= 2.0f) {
                    setDeadState(false);
                }
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        restartButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (player.state == Player.State.Dead && deadTimer >= 2.0f) {
                    setDeadState(false);
                }
                loadGame(currentLevel);
                return super.touchDown(event, x, y, pointer, button);
            }
        });
    }

    private void cameraWithPlayer() {
        camera.position.set(player.body.getPosition().x, player.body.getPosition().y + 0.5f, 0);
        camera.update();
    }

    public void loadGame(int gameLevel) {
        setWinState(false);
        gameLevel = Math.min(gameLevel, maxLevel);
        Array<Body> bodies = new Array<>();
        world.getBodies(bodies);
        for (Body b : bodies) {
            String label = ((B2dUserData) b.getUserData()).label;
            if (label.equals("Ground") || label.equals("Enemy") || label.equals("Box")) {
                world.destroyBody(b);
            }
        }
        player.restartPlayer();
        map = new TmxMapLoader().load("Map" + gameLevel + ".tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / mapScale);
        MapObjects objects = map.getLayers().get("Ground").getObjects();
        for (MapObject m : objects) {
            if (m instanceof RectangleMapObject) {
                createGround(new Vector2(((RectangleMapObject) m).getRectangle().x / mapScale, ((RectangleMapObject) m).getRectangle().y / mapScale)
                        , new Vector2(((RectangleMapObject) m).getRectangle().width / mapScale / 2, ((RectangleMapObject) m).getRectangle().height / mapScale / 2));
            }
        }
        birdEnemies = new Array<>();
        objects = map.getLayers().get("Bird").getObjects();
        for (MapObject m : objects) {
            if (m instanceof RectangleMapObject) {
                BirdEnemy enemy = new BirdEnemy(world, player, new Vector2(((RectangleMapObject) m).getRectangle().x / mapScale, ((RectangleMapObject) m).getRectangle().y / mapScale));
                birdEnemies.add(enemy);
            }
        }
        boxes = new Array<>();
        objects = map.getLayers().get("Box").getObjects();
        for (MapObject m : objects) {
            if (m instanceof RectangleMapObject) {
                Box box = new Box(world, new Vector2(((RectangleMapObject) m).getRectangle().x / mapScale, ((RectangleMapObject) m).getRectangle().y / mapScale));
                boxes.add(box);
            }
        }
        cameraWithPlayer();
    }

    public void createGround(Vector2 position, Vector2 size) {
        BodyDef def = new BodyDef();
        def.position.set(position.add(size));
        def.type = BodyDef.BodyType.StaticBody;
        Body body = world.createBody(def);
        FixtureDef fix = new FixtureDef();
        fix.restitution = 0f;
        fix.density = 1.0f;
        fix.friction = 0.7f;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(size.x, size.y);
        fix.shape = shape;
        body.createFixture(fix);
        shape.dispose();
        B2dUserData userData = new B2dUserData();
        userData.label = "Ground";
        userData.object = body;
        body.setUserData(userData);
    }

    @Override
    public void resize(int width, int height) {

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
        backgroundBatch.dispose();
        uiStage.dispose();
        font.dispose();
        world.dispose();
        //debugRenderer.dispose();
        fbo.dispose();
        fboTexture.dispose();
        failShader.dispose();
        defaultShader.dispose();
        map.dispose();
    }
}
