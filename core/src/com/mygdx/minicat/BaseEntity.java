package com.mygdx.minicat;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class BaseEntity {
    Body body;
    Vector2 speed = Vector2.Zero;

    public void setSpeed(Vector2 speed) {
        this.speed = speed;
    }

    public void logic() {
        this.body.setLinearVelocity(speed.x, body.getLinearVelocity().y);
    }
}
