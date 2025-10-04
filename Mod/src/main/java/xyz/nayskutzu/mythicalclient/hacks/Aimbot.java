package xyz.nayskutzu.mythicalclient.hacks;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import xyz.nayskutzu.mythicalclient.MythicalClientMod;
import java.util.Random;

public class Aimbot {
    private static final double MAX_AIMBOT_RANGE = 15.0;
    private static final double MIN_AIMBOT_RANGE = 2.5; // Minimum distance to prevent glitchy movements
    private static boolean isEnabled = false;
    private static EntityPlayer currentTarget = null;
    private static long lastTargetTime = 0;
    private static final long TARGET_TIMEOUT = 3000; // 3 seconds timeout if target not found
    
    // Smooth aiming variables
    private static float targetYaw = 0.0f;
    private static float targetPitch = 0.0f;
    private static final float AIM_SPEED = 3.5f; // Degrees per tick (realistic mouse sensitivity)
    private static final float MAX_AIM_SPEED = 8.0f; // Maximum degrees per tick
    private static final Random random = new Random();
    
    // Anti-cheat evasion
    private static long lastAimTime = 0;
    private static final long AIM_DELAY = 1; // Minimum ms between aim adjustments - maximum accuracy
    private static int aimMissCounter = 0;
    private static final int AIM_MISS_FREQUENCY = 0; // Never miss - perfect accuracy
    
    /**
     * Toggle aimbot on/off and find initial target
     */
    public static void toggle() {
        if (isEnabled) {
            disable();
        } else {
            enable();
        }
    }
    
    /**
     * Enable aimbot and find initial target
     */
    public static void enable() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        // Find closest player for initial target
        EntityPlayer closestPlayer = findNearestPlayer();
        
        if (closestPlayer != null) {
            isEnabled = true;
            currentTarget = closestPlayer;
            lastTargetTime = System.currentTimeMillis();
            // Initialize smooth aiming with current rotation
            targetYaw = mc.thePlayer.rotationYaw;
            targetPitch = mc.thePlayer.rotationPitch;
            MythicalClientMod.sendToggleNotification("Aimbot", true);
            MythicalClientMod.sendMessageToChat("&a&l[✓] &aAimbot enabled! Tracking: &f" + closestPlayer.getName(), false);
        } else {
            MythicalClientMod.sendMessageToChat("&c&l[!] &cNo players found within aimbot range (" + (int)MAX_AIMBOT_RANGE + " blocks)!", false);
        }
    }
    
    /**
     * Disable aimbot
     */
    public static void disable() {
        isEnabled = false;
        currentTarget = null;
        MythicalClientMod.sendToggleNotification("Aimbot", false);
        MythicalClientMod.sendMessageToChat("&c&l[✗] &cAimbot disabled!", false);
    }
    
    /**
     * Update aimbot tracking (call this every client tick)
     */
    public static void update() {
        if (!isEnabled) return;
        
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) {
            disable();
            return;
        }
        
        // Check if current target is still valid
        if (currentTarget != null) {
            // Check if target is dead
            if (currentTarget.isDead || currentTarget.getHealth() <= 0) {
                MythicalClientMod.sendMessageToChat("&e&l[!] &eTarget died! Searching for new target...", false);
                currentTarget = null;
                lastTargetTime = System.currentTimeMillis();
                return;
            }
            
            // Check if target is still in range
            double distance = mc.thePlayer.getDistanceToEntity(currentTarget);
            if (distance <= MAX_AIMBOT_RANGE && distance >= MIN_AIMBOT_RANGE) {
                // Aim at current target with smooth movement
                smoothAimAtPlayer(currentTarget, distance);
                return;
            } else if (distance < MIN_AIMBOT_RANGE) {
                // Too close - stop aiming to prevent glitchy movements
                return;
            } else {
                MythicalClientMod.sendMessageToChat("&e&l[!] &eTarget out of range! Searching for new target...", false);
                currentTarget = null;
                lastTargetTime = System.currentTimeMillis();
            }
        }
        
        // Try to find a new target
        EntityPlayer newTarget = findNearestPlayer();
        if (newTarget != null) {
            currentTarget = newTarget;
            lastTargetTime = System.currentTimeMillis();
            MythicalClientMod.sendMessageToChat("&a&l[✓] &aNew target acquired: &f" + newTarget.getName(), false);
        } else {
            // Check timeout
            if (System.currentTimeMillis() - lastTargetTime > TARGET_TIMEOUT) {
                MythicalClientMod.sendMessageToChat("&c&l[!] &cNo targets found for " + (TARGET_TIMEOUT/1000) + " seconds. Disabling aimbot.", false);
                disable();
            }
        }
    }
    
    /**
     * Find the nearest player within range
     * @return Nearest player or null if none found
     */
    private static EntityPlayer findNearestPlayer() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return null;

        EntityPlayer closestPlayer = null;
        double closestDistance = Double.MAX_VALUE;
        
        for (Object obj : mc.theWorld.playerEntities) {
            if (obj instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) obj;
                if (player != mc.thePlayer && !player.isDead && player.getHealth() > 0) {
                    double distance = mc.thePlayer.getDistanceToEntity(player);
                    if (distance < closestDistance && distance <= MAX_AIMBOT_RANGE && distance >= MIN_AIMBOT_RANGE) {
                        closestDistance = distance;
                        closestPlayer = player;
                    }
                }
            }
        }
        
        return closestPlayer;
    }
    
    /**
     * Smooth aim at a specific player with anti-cheat evasion
     * @param target The target player
     * @param distance The distance to the target
     */
    private static void smoothAimAtPlayer(EntityPlayer target, double distance) {
        Minecraft mc = Minecraft.getMinecraft();
        
        // Anti-cheat: Add delay between aim adjustments
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAimTime < AIM_DELAY) {
            return;
        }
        lastAimTime = currentTime;
        
        // Anti-cheat: Occasionally "miss" to simulate human imperfection
        aimMissCounter++;
        if (aimMissCounter >= AIM_MISS_FREQUENCY) {
            aimMissCounter = 0;
            // Skip this aim update to simulate human error
            if (random.nextInt(3) == 0) { // 33% chance to miss
                return;
            }
        }
        
        // Calculate target position with slight randomness for anti-cheat
        double targetX = target.posX + (random.nextGaussian() * 0.1); // Small random offset
        double targetY = target.posY + target.getEyeHeight() + (random.nextGaussian() * 0.05);
        double targetZ = target.posZ + (random.nextGaussian() * 0.1);
        
        // Calculate the angles needed to look at the target
        double deltaX = targetX - mc.thePlayer.posX;
        double deltaY = targetY - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double deltaZ = targetZ - mc.thePlayer.posZ;
        
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        
        // Calculate target yaw and pitch
        float newTargetYaw = (float) (Math.atan2(deltaZ, deltaX) * 180.0 / Math.PI) - 90.0f;
        float newTargetPitch = (float) -(Math.atan2(deltaY, horizontalDistance) * 180.0 / Math.PI);
        
        // Normalize angles
        newTargetYaw = normalizeYaw(newTargetYaw);
        newTargetPitch = clampPitch(newTargetPitch);
        
        // Update target angles
        targetYaw = newTargetYaw;
        targetPitch = newTargetPitch;
        
        // Smooth movement towards target
        applySmoothAiming();
    }
    
    /**
     * Apply smooth aiming movement
     */
    private static void applySmoothAiming() {
        Minecraft mc = Minecraft.getMinecraft();
        
        float currentYaw = mc.thePlayer.rotationYaw;
        float currentPitch = mc.thePlayer.rotationPitch;
        
        // Calculate yaw difference
        float yawDiff = normalizeYaw(targetYaw - currentYaw);
        float pitchDiff = targetPitch - currentPitch;
        
        // Calculate distance-based aim speed (closer = slower for more precision)
        float aimSpeedMultiplier = 1.0f;
        if (currentTarget != null) {
            double distance = mc.thePlayer.getDistanceToEntity(currentTarget);
            // Slower aim when closer for better precision
            aimSpeedMultiplier = (float) Math.max(0.3, Math.min(1.0, distance / 10.0));
        }
        
        // Apply speed limits with some randomness
        float actualAimSpeed = AIM_SPEED * aimSpeedMultiplier * (0.8f + random.nextFloat() * 0.4f);
        actualAimSpeed = Math.min(actualAimSpeed, MAX_AIM_SPEED);
        
        // Smooth yaw movement
        if (Math.abs(yawDiff) > actualAimSpeed) {
            float yawStep = Math.signum(yawDiff) * actualAimSpeed;
            mc.thePlayer.rotationYaw = normalizeYaw(currentYaw + yawStep);
        } else {
            mc.thePlayer.rotationYaw = targetYaw;
        }
        
        // Smooth pitch movement
        if (Math.abs(pitchDiff) > actualAimSpeed) {
            float pitchStep = Math.signum(pitchDiff) * actualAimSpeed;
            mc.thePlayer.rotationPitch = clampPitch(currentPitch + pitchStep);
        } else {
            mc.thePlayer.rotationPitch = targetPitch;
        }
    }
    
    /**
     * Normalize yaw angle to be between -180 and 180 degrees
     * @param yaw The yaw angle to normalize
     * @return Normalized yaw angle
     */
    private static float normalizeYaw(float yaw) {
        while (yaw > 180.0f) yaw -= 360.0f;
        while (yaw < -180.0f) yaw += 360.0f;
        return yaw;
    }
    
    /**
     * Clamp pitch angle to be between -90 and 90 degrees
     * @param pitch The pitch angle to clamp
     * @return Clamped pitch angle
     */
    private static float clampPitch(float pitch) {
        if (pitch > 90.0f) return 90.0f;
        if (pitch < -90.0f) return -90.0f;
        return pitch;
    }
    
    /**
     * Check if aimbot is currently enabled
     * @return True if aimbot is enabled
     */
    public static boolean isEnabled() {
        return isEnabled;
    }
    
    /**
     * Get current target player
     * @return Current target or null if no target
     */
    public static EntityPlayer getCurrentTarget() {
        return currentTarget;
    }
    
    /**
     * Get the maximum aimbot range
     * @return Maximum range in blocks
     */
    public static double getMaxRange() {
        return MAX_AIMBOT_RANGE;
    }
    
    /**
     * Get the minimum aimbot range
     * @return Minimum range in blocks
     */
    public static double getMinRange() {
        return MIN_AIMBOT_RANGE;
    }
} 