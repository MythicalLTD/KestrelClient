package xyz.nayskutzu.mythicalclient;

import org.lwjgl.Sys;

public class SmoothSneakingState {
    private boolean lastState;
    private boolean isAnimationDone;
    private float lastOperationTime;
    private float lastX;

    private static float getUpY(float x) {
        // quadratic function
        return -0.08F * x * x;
    }

    private static float getDownY(float x) {
        // quadratic function
        x--;
        return 0.08F * x * x - 0.08F;
    }

    public float getSneakingHeightOffset(boolean isSneaking) {
        try {
            if (lastState == isSneaking) {
                if (isAnimationDone) {
                    return isSneaking ? -0.08F : 0F;
                }
            } else {
                lastState = isSneaking;
                isAnimationDone = false;
            }

            // Protect against potential LWJGL errors
            float now;
            try {
                now = ((float) (Sys.getTime() << 3)) / Sys.getTimerResolution();
            } catch (Exception e) {
                // If timer fails, return immediate state
                return isSneaking ? -0.08F : 0F;
            }

            float timeDiff = now - lastOperationTime;
            if (lastOperationTime == 0F || timeDiff < 0F || Float.isNaN(timeDiff)) {
                timeDiff = 0F;
            }

            // Prevent extreme time differences that could cause visual glitches
            timeDiff = Math.min(timeDiff, 0.1F);

            lastOperationTime = now;
            if (isSneaking) {
                if (lastX < 1F) {
                    lastX += timeDiff;
                    if (lastX > 1F)
                        lastX = 1F;
                    return getDownY(lastX);
                } else {
                    lastX = 1F;
                    isAnimationDone = true;
                    lastOperationTime = 0F;
                    return -0.08F;
                }
            } else {
                if (lastX > 0) {
                    lastX -= timeDiff;
                    if (lastX < 0F)
                        lastX = 0F;
                    return getUpY(lastX);
                } else {
                    lastX = 0F;
                    isAnimationDone = true;
                    lastOperationTime = 0F;
                    return 0F;
                }
            }
        } catch (Exception e) {
            // If anything goes wrong, return the immediate state without animation
            return isSneaking ? -0.08F : 0F;
        }
    }
}
