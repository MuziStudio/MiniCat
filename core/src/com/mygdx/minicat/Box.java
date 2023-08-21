package com.mygdx.minicat;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Box extends BaseEntity {
    World world;

    public Box(World world, Vector2 position) {
        this.world = world;
        createBody(position, new Vector2(0.30f, 0.30f));
    }

    public void render(SpriteBatch batch) {
        float positionX = body.getPosition().x - 64 * GameTool.B2D_SCALE / 2f;
        float positionY = body.getPosition().y - 64 * GameTool.B2D_SCALE / 2f;
        float size = 64 * GameTool.B2D_SCALE;
        batch.draw(AssetsManager.box, positionX, positionY, size / 2, size / 2, size, size,
                1.0f, 1.0f, MathUtils.radDeg * body.getAngle(), 0, 0, 16, 16, false, false);
    }

    @Override
    public void logic() {

    }

    public void createBody(Vector2 position, Vector2 size) {
        BodyDef def = new BodyDef();
        def.position.set(position);
        def.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(def);
        FixtureDef fix = new FixtureDef();
        fix.restitution = 0.2f;
        fix.density = 0.5f;
        fix.friction = 0.3f;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(size.x, size.y);
        fix.shape = shape;
        body.createFixture(fix);
        shape.dispose();
        B2dUserData userData = new B2dUserData();
        userData.label = "Box";
        userData.object = this;
        body.setUserData(userData);
    }
}
