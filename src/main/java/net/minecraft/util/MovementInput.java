package net.minecraft.util;

public class MovementInput {
    /**
     * The speed at which the player is strafing. Postive numbers to the left and negative to the right.
     */
    public float moveStrafe, originalStrafe;

    /**
     * The speed at which the player is moving forward. Negative numbers will move backwards.
     */
    public float moveForward, originalForward;
    public boolean jump;
    public boolean sneak;

    public void updatePlayerMoveState() {
    }
}
