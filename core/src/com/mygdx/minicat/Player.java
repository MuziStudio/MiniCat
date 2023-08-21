package com.mygdx.minicat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;

import java.util.Arrays;


public class Player extends BaseEntity {
    enum State {
        Idle, Walk, Jumping, Attacking, Dead, Win
    }

    enum AttackState {
        None, UpperCut, DownWardKick
    }

    TextureRegion[][] catAtlas;
    Animation<TextureRegion> idleAnimation, walkAnimation, jumpAnimation, spinAnimation, deadAnimation, powerShotAnimation;
    Animation<TextureRegion> fastShotAnimation, flyingKickAnimation, superUppercutAnimation, oneTwoComboAnimation;
    Animation<TextureRegion> lowAndMiddleKickAnimation, highKickAnimation, downWardKickAnimation, twoSideAnimation;
    Animation<TextureRegion> roundKickAnimation, upperCutAnimation;
    State state = State.Idle;
    AttackState attackState = AttackState.None;
    float animationDuration = 0.1f;
    float time = 0f, attackTime = 0f, deadTime = 0f;
    boolean isFlip = false;
    public Vector2 tempSpeed = Vector2.Zero;
    World world;
    Body attackBody;
    boolean isMove = false;
    int contactNumber = 0;

    public Player(final World world) {
        catAtlas = TextureRegion.split(AssetsManager.catTexture, 64, 64);
        idleAnimation = prepareAnimation(0, 0, 4, Animation.PlayMode.LOOP);
        walkAnimation = prepareAnimation(1, 0, 8, Animation.PlayMode.LOOP);
        jumpAnimation = prepareAnimation(2, 2, 4, Animation.PlayMode.LOOP);
        spinAnimation = prepareAnimation(3, 4, 9, Animation.PlayMode.LOOP);
        deadAnimation = prepareAnimation(4, 0, 7, Animation.PlayMode.NORMAL);
        powerShotAnimation = prepareAnimation(5, 0, 7, Animation.PlayMode.LOOP);
        fastShotAnimation = prepareAnimation(6, 0, 6, Animation.PlayMode.LOOP);
        flyingKickAnimation = prepareAnimation(7, 0, 8, Animation.PlayMode.LOOP);
        superUppercutAnimation = prepareAnimation(8, 4, 11, Animation.PlayMode.LOOP);
        oneTwoComboAnimation = prepareAnimation(9, 0, 10, Animation.PlayMode.LOOP);
        lowAndMiddleKickAnimation = prepareAnimation(10, 0, 12, Animation.PlayMode.LOOP);
        highKickAnimation = prepareAnimation(11, 0, 6, Animation.PlayMode.LOOP);
        downWardKickAnimation = prepareAnimation(12, 3, 8, Animation.PlayMode.NORMAL);
        twoSideAnimation = prepareAnimation(13, 0, 8, Animation.PlayMode.LOOP);
        roundKickAnimation = prepareAnimation(14, 0, 8, Animation.PlayMode.LOOP);
        upperCutAnimation = prepareAnimation(15, 0, 6, Animation.PlayMode.NORMAL);
        this.world = world;
        createBody();
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Fixture A = contact.getFixtureA();
                Fixture B = contact.getFixtureB();
                B2dUserData userDataA = (B2dUserData) A.getBody().getUserData();
                B2dUserData userDataB = (B2dUserData) B.getBody().getUserData();
                if (userDataA.label.equals("Player")) {
                    if (A.isSensor()) {
                        contactNumber++;
                    }
                } else if (userDataB.label.equals("Player")) {
                    if (B.isSensor()) {
                        contactNumber++;
                    }
                }
                if (userDataA.label.equals("Player") && userDataB.label.equals("Enemy") && !A.isSensor()) {
                    if (userDataB.object instanceof BirdEnemy && ((BirdEnemy) userDataB.object).state == BirdEnemy.State.Idle) {
                        dead();
                        ((BirdEnemy) userDataB.object).gotoDead();
                    }
                } else if (userDataB.label.equals("Player") && userDataA.label.equals("Enemy") && !B.isSensor()) {
                    if (userDataA.object instanceof BirdEnemy && ((BirdEnemy) userDataA.object).state == BirdEnemy.State.Idle) {
                        dead();
                        ((BirdEnemy) userDataA.object).gotoDead();
                    }
                }
                if (userDataA.label.equals("AttackBlock") && userDataB.label.equals("Enemy")) {
                    if (userDataB.object instanceof BirdEnemy) {
                        AssetsManager.hurt.play();
                        ((BirdEnemy) userDataB.object).hurt();
                    }
                } else if (userDataB.label.equals("AttackBlock") && userDataA.label.equals("Enemy")) {
                    if (userDataA.object instanceof BirdEnemy) {
                        AssetsManager.hurt.play();
                        ((BirdEnemy) userDataA.object).hurt();
                    }
                }
            }

            @Override
            public void endContact(Contact contact) {
                Fixture A = contact.getFixtureA();
                Fixture B = contact.getFixtureB();
                B2dUserData userDataA = (B2dUserData) A.getBody().getUserData();
                B2dUserData userDataB = (B2dUserData) B.getBody().getUserData();
                if (userDataA.label.equals("Player")) {
                    if (A.isSensor()) {
                        contactNumber--;
                    }
                } else if (userDataB.label.equals("Player")) {
                    if (B.isSensor()) {
                        contactNumber--;
                    }
                }
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });
    }

    public void render(SpriteBatch batch) {
        time += Gdx.graphics.getDeltaTime();
        TextureRegion tempRegion = null;
        switch (state) {
            case Idle:
                tempRegion = idleAnimation.getKeyFrame(time);
                break;
            case Walk:
                tempRegion = walkAnimation.getKeyFrame(time);
                break;
            case Jumping:
                tempRegion = jumpAnimation.getKeyFrame(time);
                break;
            case Dead:
                deadTime += Gdx.graphics.getDeltaTime();
                tempRegion = deadAnimation.getKeyFrame(deadTime);
                break;
            case Win:
                tempRegion = superUppercutAnimation.getKeyFrame(time);
                break;
            case Attacking:
                attackTime += Gdx.graphics.getDeltaTime();
                switch (attackState) {
                    case None:
                        break;
                    case UpperCut:
                        tempRegion = upperCutAnimation.getKeyFrame(attackTime);
                        break;
                    case DownWardKick:
                        tempRegion = downWardKickAnimation.getKeyFrame(attackTime);
                        break;
                }
                break;
        }
        batch.draw(tempRegion, body.getPosition().x + (isFlip ? 192 * GameTool.B2D_SCALE : 0) - 192 * GameTool.B2D_SCALE / 2f,
                body.getPosition().y - 192 * GameTool.B2D_SCALE / 2f + 0.4f, 192 * GameTool.B2D_SCALE * (isFlip ? -1 : 1), 192 * GameTool.B2D_SCALE);
    }

    @Override
    public void logic() {
        super.logic();
        if (body.getLinearVelocity().x > 0) {
            isFlip = false;
        } else if (body.getLinearVelocity().x < 0) {
            isFlip = true;
        }
        if (state == State.Jumping || state == State.Idle || state == State.Walk) {
            state = Math.abs(body.getLinearVelocity().x) > 0.01f ? State.Walk : State.Idle;
            state = Math.abs(body.getLinearVelocity().y) > 0.01f && contactNumber == 0 ? State.Jumping : state;
            attackState = AttackState.None;
        } else if (state == State.Attacking) {
            switch (attackState) {
                case UpperCut:
                    checkAttackFinish(upperCutAnimation);
                    makeAttackBodyMove(upperCutAnimation);
                    break;
                case DownWardKick:
                    checkAttackFinish(downWardKickAnimation);
                    makeAttackBodyMove(downWardKickAnimation);
                    if (attackBody != null) {
                        attackBody.setLinearVelocity(body.getLinearVelocity());
                    }
                    break;
            }
        }
        if (state == State.Jumping && contactNumber > 0) {
            state = State.Idle;
        }
        if (state != State.Dead && body.getPosition().x > 43) {
            win();
        }
    }

    public void restartPlayer() {
        isFlip = false;
        state = State.Idle;
        body.setTransform(2, 6, 0);
        body.setAwake(true);
    }

    public void walk(boolean isRight) {
        float speed = 1f;
        if (state != State.Attacking && state != State.Dead && state != State.Win) {
            setSpeed(new Vector2(isRight ? speed : -speed, 0));
        } else {
            tempSpeed = new Vector2(isRight ? speed : -speed, 0);
        }
    }

    public void win() {
        if (state != State.Dead) {
            stop();
            state = State.Win;
        }
    }

    public void dead() {
        if (state != State.Dead && state != State.Win) {
            deadTime = 0f;
            stop();
            state = State.Dead;
        }
    }

    public void stop() {
        setSpeed(new Vector2(0, 0));
    }

    public void attack() {
        if (state != State.Attacking && state != State.Dead && state != State.Win) {
            attackTime = 0;
            tempSpeed = speed;
            stop();
            if (state != State.Jumping) {
                attackState = AttackState.UpperCut;
            } else {
                attackState = AttackState.DownWardKick;
                body.setLinearVelocity(0, 5);
            }
            state = State.Attacking;
            isMove = false;
        }
    }

    public void jump() {
        if (state != State.Attacking && state != State.Jumping && state != State.Dead && state != State.Win) {
            AssetsManager.jump.play();
            body.applyLinearImpulse(new Vector2(0, 0.35f), body.getWorldCenter(), true);
        }
    }

    private void checkAttackFinish(Animation<TextureRegion> animation) {
        float time = attackTime;
        if (animation == downWardKickAnimation) {
            time -= 0.4f;
        }
        if (animation == upperCutAnimation) {
            time += 0.1f;
        }
        if (animation.isAnimationFinished(time) && Math.abs(body.getLinearVelocity().y) <= 0.01f) {
            attackTime = 0;
            state = State.Idle;
            attackState = AttackState.None;
            setSpeed(tempSpeed);
            if (attackBody != null) {
                world.destroyBody(attackBody);
                attackBody = null;
            }
        }
    }

    private void makeAttackBodyMove(Animation<TextureRegion> animation) {
        if (animation == upperCutAnimation && animation.getKeyFrameIndex(attackTime) == 4 && !isMove) {
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(0.2f, 0.1f, Vector2.Zero, isFlip ? 60 : -60);
            createAttackBlock(new Vector2(body.getPosition().x + (isFlip ? -0.15f : 0.15f), body.getPosition().y - 0.15f), true, shape);
            shape.dispose();
            attackBody.setLinearVelocity(isFlip ? -1f : 1f, 5f);
            isMove = true;
        }
        if (animation == downWardKickAnimation && animation.getKeyFrameIndex(attackTime) >= 0 && !isMove) {
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(0.3f, 0.3f);
            createAttackBlock(new Vector2(body.getPosition().x + (isFlip ? -0.2f : 0.2f), body.getPosition().y - 0.3f), false, shape);
            shape.dispose();
            isMove = true;
        }
    }

    private Animation<TextureRegion> prepareAnimation(int pos, int from, int to, Animation.PlayMode mode) {
        Animation<TextureRegion> animation = new Animation<>(animationDuration, Arrays.copyOfRange(catAtlas[pos], from, to));
        animation.setPlayMode(mode);
        return animation;
    }

    private void createBody() {
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.fixedRotation = true;
        def.position.set(2, 6);
        body = world.createBody(def);
        FixtureDef fix = new FixtureDef();
        fix.friction = 0f;
        fix.density = 1f;
        fix.restitution = 0f;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.1f, 0.2f);
        fix.shape = shape;
        fix.filter.maskBits = 0x0001;
        body.createFixture(fix);
        shape.dispose();

        FixtureDef fixFoot = new FixtureDef();
        fixFoot.isSensor = true;
        PolygonShape shapeFoot = new PolygonShape();
        shapeFoot.setAsBox(0.08f, 0.1f, new Vector2(0f, -0.2f), 0);
        fixFoot.shape = shapeFoot;
        fixFoot.filter.maskBits = 0x0001;
        body.createFixture(fixFoot);
        B2dUserData userData = new B2dUserData();
        userData.label = "Player";
        userData.object = this;
        body.setUserData(userData);
        shapeFoot.dispose();
    }

    private void createAttackBlock(Vector2 position, boolean isCollision, Shape shape) {
        if (attackBody != null) {
            world.destroyBody(attackBody);
            attackBody = null;
        }
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.KinematicBody;
        def.position.set(position);
        attackBody = world.createBody(def);
        FixtureDef fix = new FixtureDef();
        fix.isSensor = !isCollision;
        fix.friction = 0f;
        fix.density = 2f;
        fix.restitution = 0f;
        fix.shape = shape;
        fix.filter.categoryBits = 0x0002;
        attackBody.createFixture(fix);
        B2dUserData userData = new B2dUserData();
        userData.label = "AttackBlock";
        userData.object = attackBody;
        attackBody.setUserData(userData);
    }
}
