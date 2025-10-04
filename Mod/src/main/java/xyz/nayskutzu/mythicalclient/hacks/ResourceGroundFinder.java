package xyz.nayskutzu.mythicalclient.hacks;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.init.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import xyz.nayskutzu.mythicalclient.MythicalClientMod;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

public class ResourceGroundFinder {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static boolean enabled;
    private static final ResourceGroundFinder instance = new ResourceGroundFinder();
    
    // List of items to track
    private static final List<Item> VALUABLE_ITEMS = Arrays.asList(
        Items.diamond,
        Items.emerald,
        Items.gold_ingot,
        Items.iron_ingot
    );

    public static void main() {
        try {
            System.out.println("ResourceGroundFinder class booting up...");
            if (enabled) {
                enabled = false;
                MinecraftForge.EVENT_BUS.unregister(instance);
                MythicalClientMod.sendMessageToChat("&7Resource Ground Finder is now &cdisabled&7.", false);
            } else {
                enabled = true;
                MinecraftForge.EVENT_BUS.register(instance);
                MythicalClientMod.sendMessageToChat("&7Resource Ground Finder is now &aenabled&7.", false);
            }
        } catch (Exception e) {
            System.out.println("Failed to toggle ResourceGroundFinder: " + e.getMessage());
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        try {
            if (!enabled || mc.theWorld == null || mc.thePlayer == null) return;

            // Get all entities in the world
            for (Object obj : mc.theWorld.loadedEntityList) {
                if (!(obj instanceof EntityItem)) continue;

                EntityItem item = (EntityItem) obj;
                ItemStack stack = item.getEntityItem();

                if (stack == null || !VALUABLE_ITEMS.contains(stack.getItem())) continue;

                // Calculate positions
                double x = item.lastTickPosX + (item.posX - item.lastTickPosX) * event.partialTicks - mc.getRenderManager().viewerPosX;
                double y = item.lastTickPosY + (item.posY - item.lastTickPosY) * event.partialTicks - mc.getRenderManager().viewerPosY;
                double z = item.lastTickPosZ + (item.posZ - item.lastTickPosZ) * event.partialTicks - mc.getRenderManager().viewerPosZ;

                // Get color based on item type
                Color color = getColorForItem(stack.getItem());
                
                // Setup GL state
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.disableTexture2D();
                GlStateManager.disableDepth();
                GlStateManager.disableLighting();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                
                try {
                    // Draw text
                    String text = String.format("%s x%d", stack.getDisplayName(), stack.stackSize);
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(x, y + 0.5, z);
                    GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
                    GlStateManager.scale(-0.025F, -0.025F, 0.025F);
                    
                    mc.fontRendererObj.drawString(text, -mc.fontRendererObj.getStringWidth(text) / 2, 0, color.getRGB());
                    GlStateManager.popMatrix();

                    // Draw tracers if player is far enough
                    double distance = mc.thePlayer.getDistanceToEntity(item);
                    if (distance > 5) {
                        @SuppressWarnings("unused")
                        Vec3 eyes = mc.thePlayer.getPositionEyes(event.partialTicks);
                        GL11.glLineWidth(1.5F);
                        GL11.glBegin(GL11.GL_LINES);
                        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 0.4F);
                        GL11.glVertex3d(0, mc.thePlayer.getEyeHeight(), 0);
                        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1.0F);
                        GL11.glVertex3d(x, y, z);
                        GL11.glEnd();
                    }
                } finally {
                    // Reset GL state
                    GlStateManager.enableTexture2D();
                    GlStateManager.enableDepth();
                    GlStateManager.enableLighting();
                    GlStateManager.disableBlend();
                    GlStateManager.popMatrix();
                }
            }
        } catch (Exception e) {
            // Silently fail rather than crash
            System.out.println("Error in ResourceGroundFinder render: " + e.getMessage());
        }
    }

    private Color getColorForItem(Item item) {
        if (item == Items.diamond) {
            return new Color(0x55FFFF); // Cyan
        } else if (item == Items.emerald) {
            return new Color(0x55FF55); // Green
        } else if (item == Items.gold_ingot) {
            return new Color(0xFFAA00); // Gold
        } else if (item == Items.iron_ingot) {
            return new Color(0xAAAAAA); // Gray
        }
        return new Color(0xFFFFFF); // White for unknown
    }
} 