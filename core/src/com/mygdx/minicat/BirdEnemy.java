package com.mygdx.minicat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import java.util.Arrays;

public class BirdEnemy extends BaseEntity {
    enum State {
        Idle, Hurt, Dead
    }

    TextureRegion[][] birdAtlas;
    Animation<TextureRegion> idleAnimation, hurtAnimation;
    State state = State.Idle;

    World world;
    Player player;
    float animationDuration = 0.1f;
    float time = 0f, hurtTimer = 0.0f;
    int blood = 1;
    boolean isFlip = false;

    public BirdEnemy(World world, Player player, Vector2 position) {
        this.world = world;
        this.player = player;
        birdAtlas = TextureRegion.split(AssetsManager.birdTexture, 16, 16);
        idleAnimation = new Animation<>(animationDuration, Arrays.copyOfRange(birdAtlas[0], 0, 4));
        idleAnimation.setPlayMode(Animation.PlayMode.LOOP);
        hurtAnimation = new Animation<>(animationDuration, Arrays.copyOfRange(birdAtlas[0], 4, 7));
        hurtAnimation.setPlayMode(Animation.PlayMode.NORMAL);
        createBody(position, new Vector2(0.1f, 0.1f));
    }

    public void render(SpriteBatch batch) {
        time += Gdx.graphics.getDeltaTime();
        TextureRegion tempRegion = null;
        switch (state) {
            case Idle:
                tempRegion = idleAnimation.getKeyFrame(time);
                break;
            case Hurt:
                hurtTimer += Gdx.graphics.getDeltaTime();
                tempRegion = hurtAnimation.getKeyFrame(hurtTimer);
                if (hurtAnimation.isAnimationFinished(hurtTimer - 1.0f)) {
                    if (blood > 0) {
                        state = State.Idle;
                    } else {
                        dead();
                    }
                }
                break;
        }
        if (tempRegion != null) {
            batch.draw(tempRegion, body.getPosition().x + (isFlip ? 32 * GameTool.B2D_SCALE : 0) - 32 * GameTool.B2D_SCALE / 2f,
                    body.getPosition().y - 32 * GameTool.B2D_SCALE / 2f, 32 * GameTool.B2D_SCALE * (isFlip ? -1 : 1), 32 * GameTool.B2D_SCALE);
        }
    }

    @Override
    public void logic() {
        if (state == State.Idle) {
            super.logic();
            if (player.body.getPosition().sub(body.getPosition()).len() < 2f) {
                float dir = player.body.getPosition().x - body.getPosition().x < 0f ? -0.5f : 0.5f;
                body.setLinearVelocity(dir, body.getLinearVelocity().y);
            }
            if (body.getLinearVelocity().x < 0) {
                isFlip = false;
            } else if (body.getLinearVelocity().x > 0) {
                isFlip = true;
            }
        }
    }

    public void gotoDead()
    {
        blood = 0;
        hurt();
    }
    public void dead() {
        state = State.Dead;
        ((B2dUserData) (body.getUserData())).isDead = true;
    }

    public void hurt() {
        if (state == State.Idle) {
            hurtTimer = 0.0f;
            state = State.Hurt;
            blood--;
        }
    }

    public void createBody(Vector2 position, Vector2 size) {
        BodyDef def = new BodyDef();
        def.position.set(position);
        def.type = BodyDef.BodyType.DynamicBody;
        def.fixedRotation = true;
        body = world.createBody(def);
        FixtureDef fix = new FixtureDef();
        fix.restitution = 0.2f;
        fix.density = 1.0f;
        fix.friction = 0.4f;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(size.x, size.y);
        fix.shape = shape;
        body.createFixture(fix);
        shape.dispose();
        B2dUserData userData = new B2dUserData();
        userData.label = "Enemy";
        userData.object = this;
        body.setUserData(userData);
    }
}

