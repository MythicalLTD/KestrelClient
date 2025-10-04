package xyz.nayskutzu.mythicalclient.hacks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xyz.nayskutzu.mythicalclient.MythicalClientMod;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class Trajectories {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static boolean enabled;
    private static final Trajectories instance = new Trajectories();
    
    // Cache for item properties to avoid repeated calculations
    private static final Map<Class<? extends Item>, Float> VELOCITY_CACHE = new HashMap<>();
    private static final Map<Class<? extends Item>, Double> GRAVITY_CACHE = new HashMap<>();
    private static final Map<Class<? extends Item>, float[]> COLOR_CACHE = new HashMap<>();
    
    // Reusable lists and vectors to avoid garbage collection
    private final List<Vec3> trajectoryPoints = new ArrayList<>();
    private Vec3 motionVec = new Vec3(0, 0, 0);
    private Vec3 positionVec = new Vec3(0, 0, 0);
    
    // Constants
    private static final float AIR_RESISTANCE = 0.99F;
    private static final float SIMULATION_STEP = 0.1F;
    private static final int MAX_STEPS = 100;
    private static final float BOW_BASE_VELOCITY = 1.5F;
    private static final double CROSS_SIZE = 0.3;

    static {
        // Initialize caches
        initializeCaches();
    }

    private static void initializeCaches() {
        // Velocity cache
        VELOCITY_CACHE.put(ItemBow.class, 1.5F);
        VELOCITY_CACHE.put(ItemEnderPearl.class, 1.5F);
        VELOCITY_CACHE.put(ItemExpBottle.class, 0.7F);
        
        // Gravity cache
        GRAVITY_CACHE.put(ItemBow.class, 0.05);
        GRAVITY_CACHE.put(ItemEnderPearl.class, 0.03);
        GRAVITY_CACHE.put(ItemExpBottle.class, 0.07);
        
        // Color cache
        COLOR_CACHE.put(ItemBow.class, new float[]{1.0F, 0.0F, 0.0F});
        COLOR_CACHE.put(ItemEnderPearl.class, new float[]{0.5F, 0.0F, 0.5F});
        COLOR_CACHE.put(ItemExpBottle.class, new float[]{0.0F, 1.0F, 0.0F});
    }

    public static void main() {
        if (enabled) {
            enabled = false;
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(instance);
            MythicalClientMod.sendMessageToChat("&7Trajectories is now &cdisabled&7.", false);
        } else {
            enabled = true;
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(instance);
            MythicalClientMod.sendMessageToChat("&7Trajectories is now &aenabled&7.", false);
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!shouldRender()) return;

        Item item = mc.thePlayer.getHeldItem().getItem();
        if (!isThrowable(item)) return;

        calculateAndRenderTrajectory(item, event.partialTicks);
    }

    private boolean shouldRender() {
        return enabled && mc.theWorld != null && mc.thePlayer != null && mc.thePlayer.getHeldItem() != null;
    }

    private boolean isThrowable(Item item) {
        return item instanceof ItemBow || 
               item instanceof ItemEnderPearl || 
               item instanceof ItemEgg || 
               item instanceof ItemSnowball || 
               item == Items.potionitem || 
               item instanceof ItemExpBottle;
    }

    private void calculateAndRenderTrajectory(Item item, float partialTicks) {
        trajectoryPoints.clear();
        
        // Get initial position
        updatePosition(partialTicks);
        
        // Calculate initial motion
        float velocity = getInitialVelocity(item);
        if (item instanceof ItemBow) {
            velocity *= calculateBowPower();
        }
        
        updateMotion(velocity);
        
        // Simulate trajectory
        simulateTrajectory(item);
        
        // Render if we have points
        if (!trajectoryPoints.isEmpty()) {
            renderTrajectory(getLandingColor(item));
        }
    }

    private void updatePosition(float partialTicks) {
        double x = interpolate(mc.thePlayer.lastTickPosX, mc.thePlayer.posX, partialTicks);
        double y = interpolate(mc.thePlayer.lastTickPosY, mc.thePlayer.posY, partialTicks);
        double z = interpolate(mc.thePlayer.lastTickPosZ, mc.thePlayer.posZ, partialTicks);
        
        double cosYaw = Math.cos(Math.toRadians(mc.thePlayer.rotationYaw));
        double sinYaw = Math.sin(Math.toRadians(mc.thePlayer.rotationYaw));
        
        positionVec = new Vec3(
            x - cosYaw * 0.16F,
            y + mc.thePlayer.getEyeHeight() - 0.1,
            z - sinYaw * 0.16F
        );
    }

    private double interpolate(double last, double current, float partialTicks) {
        return last + (current - last) * partialTicks;
    }

    private void updateMotion(float velocity) {
        float yaw = (float) Math.toRadians(mc.thePlayer.rotationYaw);
        float pitch = (float) Math.toRadians(mc.thePlayer.rotationPitch);
        
        float cosPitch = (float) Math.cos(pitch);
        motionVec = new Vec3(
            -Math.sin(yaw) * cosPitch * velocity,
            -Math.sin(pitch) * velocity,
            Math.cos(yaw) * cosPitch * velocity
        );
    }

    private float calculateBowPower() {
        float power = (72000 - mc.thePlayer.getItemInUseCount()) / 20.0F;
        power = (power * power + power * 2.0F) / 3.0F;
        return Math.min(1.0F, power) * 2;
    }

    private void simulateTrajectory(Item item) {
        double gravity = getGravity(item);
        double x = positionVec.xCoord;
        double y = positionVec.yCoord;
        double z = positionVec.zCoord;
        double motX = motionVec.xCoord;
        double motY = motionVec.yCoord;
        double motZ = motionVec.zCoord;

        for (int step = 0; step < MAX_STEPS; step++) {
            trajectoryPoints.add(new Vec3(x, y, z));

            // Update position
            x += motX * SIMULATION_STEP;
            y += motY * SIMULATION_STEP;
            z += motZ * SIMULATION_STEP;

            // Update motion
            motX *= AIR_RESISTANCE;
            motY = motY * AIR_RESISTANCE - gravity * SIMULATION_STEP;
            motZ *= AIR_RESISTANCE;

            // Check collision
            BlockPos blockPos = new BlockPos(x, y, z);
            if (mc.theWorld.getBlockState(blockPos).getBlock().getMaterial().isSolid()) {
                trajectoryPoints.add(new Vec3(x, y, z));
                break;
            }
        }
    }

    private float getInitialVelocity(Item item) {
        if (item == Items.potionitem) return 0.5F;
        return VELOCITY_CACHE.getOrDefault(item.getClass(), BOW_BASE_VELOCITY);
    }

    private double getGravity(Item item) {
        if (item == Items.potionitem) return 0.05;
        return GRAVITY_CACHE.getOrDefault(item.getClass(), 0.03);
    }

    private float[] getLandingColor(Item item) {
        if (item == Items.potionitem) return new float[]{0.0F, 0.0F, 1.0F};
        return COLOR_CACHE.getOrDefault(item.getClass(), new float[]{1.0F, 1.0F, 1.0F});
    }

    private void renderTrajectory(float[] color) {
        setupRendering();
        
        drawTrajectoryLine(color);
        
        if (!trajectoryPoints.isEmpty()) {
            drawLandingPoint(trajectoryPoints.get(trajectoryPoints.size() - 1), color);
        }
        
        resetRendering();
    }

    private void setupRendering() {
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(false);
    }

    private void resetRendering() {
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawTrajectoryLine(float[] color) {
        GL11.glLineWidth(2.0F);
        GlStateManager.color(color[0], color[1], color[2], 1.0F);
        
        GL11.glBegin(GL11.GL_LINE_STRIP);
        RenderManager renderManager = mc.getRenderManager();
        
        for (Vec3 point : trajectoryPoints) {
            GL11.glVertex3d(
                point.xCoord - renderManager.viewerPosX,
                point.yCoord - renderManager.viewerPosY,
                point.zCoord - renderManager.viewerPosZ
            );
        }
        GL11.glEnd();
    }

    private void drawLandingPoint(Vec3 landing, float[] color) {
        RenderManager renderManager = mc.getRenderManager();
        double x = landing.xCoord - renderManager.viewerPosX;
        double y = landing.yCoord - renderManager.viewerPosY;
        double z = landing.zCoord - renderManager.viewerPosZ;

        // Draw point
        GL11.glPointSize(5.0F);
        GlStateManager.color(color[0], color[1], color[2], 1.0F);
        GL11.glBegin(GL11.GL_POINTS);
        GL11.glVertex3d(x, y, z);
        GL11.glEnd();

        // Draw cross
        GL11.glBegin(GL11.GL_LINES);
        drawCross(x, y, z);
        GL11.glEnd();
    }

    private void drawCross(double x, double y, double z) {
        GL11.glVertex3d(x - CROSS_SIZE, y, z);
        GL11.glVertex3d(x + CROSS_SIZE, y, z);
        GL11.glVertex3d(x, y - CROSS_SIZE, z);
        GL11.glVertex3d(x, y + CROSS_SIZE, z);
        GL11.glVertex3d(x, y, z - CROSS_SIZE);
        GL11.glVertex3d(x, y, z + CROSS_SIZE);
    }
} 