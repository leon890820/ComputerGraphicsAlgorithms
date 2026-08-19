package org.example.engine.player;

public class PlayerInput {
    public boolean forward;
    public boolean left;
    public boolean backward;
    public boolean right;
    public boolean sprint;
    public boolean lookActive;
    public float mouseDeltaX;
    public float mouseDeltaY;

    public void endFrame() {
        mouseDeltaX = 0.0f;
        mouseDeltaY = 0.0f;
    }
}
