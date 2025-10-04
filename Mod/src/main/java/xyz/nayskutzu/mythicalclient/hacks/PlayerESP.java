package xyz.nayskutzu.mythicalclient.hacks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xyz.nayskutzu.mythicalclient.MythicalClientMod;
import xyz.nayskutzu.mythicalclient.utils.FriendlyPlayers;

import org.lwjgl.opengl.GL11;

public class PlayerESP {

    private static final Minecraft mc = Minecraft.getMinecraft();
    public static boolean enabled;
    private static final PlayerESP instance = new PlayerESP();

    public static void main() {
        System.out.println("PlayerESP class booting up...");
        if (enabled) {
            enabled = false;
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(instance);
            MythicalClientMod.sendMessageToChat("&7Player ESP is now &cdisabled&7.",false);
        } else {
            enabled = true;
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(instance);
            MythicalClientMod.sendMessageToChat("&7Player ESP is now &aenabled&7.",false);
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null)
            return;

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player == mc.thePlayer || player.isDead)
                continue;
            String playerName = player.getName();
            if (playerName != null && !playerName.isEmpty() && !playerName.matches(".*§.*")
                    && !playerName.matches(".*&.*") && !playerName.matches(".*CIT-.*")) {
                
                // Additional NPC checks for Hypixel and other servers
                if (isNPC(playerName, player)) {
                    continue; // Skip rendering for NPCs
                }
                
                if (FriendlyPlayers.isFriendly(playerName)) {
                    continue; // Skip rendering for friendly players
                }
                double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks
                        - mc.getRenderManager().viewerPosX;
                double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks
                        - mc.getRenderManager().viewerPosY;
                double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks
                        - mc.getRenderManager().viewerPosZ;
                if (player.isSneaking()) {
                    y -= 0.2;
                }
                if (player.isSprinting()) {
                    y += 0.2;
                }
                
                if (player == mc.thePlayer || mc.thePlayer.isSpectator()) {
                    return;
                }

                drawESP(x, y, z, player.width, player.height);
            }
        }
    }

    private void drawESP(double x, double y, double z, float width, float height) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Draw filled box with transparency - using chroma key green
        GL11.glLineWidth(1.0F);
        float[] rgbFill = getChromaKeyColor(0.8f); // Dark chroma green for fill
        GlStateManager.color(rgbFill[0], rgbFill[1], rgbFill[2], 0.2F);
        AxisAlignedBB box = new AxisAlignedBB(-width / 2, 0, -width / 2, width / 2, height, width / 2);
        drawFilledBox(box);

        // Draw outline with chroma key green
        GL11.glLineWidth(2.5F);
        drawChromaKeyOutline(box);

        // Draw corner highlights
        GL11.glLineWidth(3.0F);
        drawBoxHighlights(box);

        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private float[] getChromaKeyColor(float intensity) {
        // Return chroma key compatible green with better visual appearance
        // Using a slightly more visually appealing green that's still chroma key friendly
        return new float[] {
            0.05f * intensity,       // Tiny bit of red for better visuals
            intensity,               // Green = intensity (0.0 to 1.0)  
            0.1f * intensity         // Tiny bit of blue for better visuals
        };
    }

    private void drawChromaKeyOutline(AxisAlignedBB box) {
        GL11.glBegin(GL11.GL_LINES);
        
        // Draw vertical lines with chroma key green gradient
        for (int i = 0; i < 4; i++) {
            float[] bottomColor = getChromaKeyColor(1.0f);
            float[] topColor = getChromaKeyColor(0.9f);
            
            switch(i) {
                case 0:
                    GlStateManager.color(bottomColor[0], bottomColor[1], bottomColor[2], 1.0F);
                    GL11.glVertex3d(box.minX, box.minY, box.minZ);
                    GlStateManager.color(topColor[0], topColor[1], topColor[2], 1.0F);
                    GL11.glVertex3d(box.minX, box.maxY, box.minZ);
                    break;
                case 1:
                    GlStateManager.color(bottomColor[0], bottomColor[1], bottomColor[2], 1.0F);
                    GL11.glVertex3d(box.maxX, box.minY, box.minZ);
                    GlStateManager.color(topColor[0], topColor[1], topColor[2], 1.0F);
                    GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
                    break;
                case 2:
                    GlStateManager.color(bottomColor[0], bottomColor[1], bottomColor[2], 1.0F);
                    GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
                    GlStateManager.color(topColor[0], topColor[1], topColor[2], 1.0F);
                    GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
                    break;
                case 3:
                    GlStateManager.color(bottomColor[0], bottomColor[1], bottomColor[2], 1.0F);
                    GL11.glVertex3d(box.minX, box.minY, box.maxZ);
                    GlStateManager.color(topColor[0], topColor[1], topColor[2], 1.0F);
                    GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
                    break;
            }
        }
        
        // Bottom edges
        for (int i = 0; i < 4; i++) {
            float[] color = getChromaKeyColor(1.0f);
            GlStateManager.color(color[0], color[1], color[2], 1.0F);
            
            switch(i) {
                case 0:
                    GL11.glVertex3d(box.minX, box.minY, box.minZ);
                    GL11.glVertex3d(box.maxX, box.minY, box.minZ);
                    break;
                case 1:
                    GL11.glVertex3d(box.maxX, box.minY, box.minZ);
                    GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
                    break;
                case 2:
                    GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
                    GL11.glVertex3d(box.minX, box.minY, box.maxZ);
                    break;
                case 3:
                    GL11.glVertex3d(box.minX, box.minY, box.maxZ);
                    GL11.glVertex3d(box.minX, box.minY, box.minZ);
                    break;
            }
        }

        // Top edges with brighter colors
        for (int i = 0; i < 4; i++) {
            float[] color = getChromaKeyColor(0.95f);
            GlStateManager.color(color[0], color[1], color[2], 1.0F);
            
            switch(i) {
                case 0:
                    GL11.glVertex3d(box.minX, box.maxY, box.minZ);
                    GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
                    break;
                case 1:
                    GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
                    GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
                    break;
                case 2:
                    GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
                    GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
                    break;
                case 3:
                    GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
                    GL11.glVertex3d(box.minX, box.maxY, box.minZ);
                    break;
            }
        }
        GL11.glEnd();
    }

    private void drawBoxHighlights(AxisAlignedBB box) {
        float[] highlightColor = getChromaKeyColor(0.85f); // Chroma key green for highlights
        GlStateManager.color(highlightColor[0], highlightColor[1], highlightColor[2], 1.0F);
        GL11.glBegin(GL11.GL_LINES);
        
        float highlightLength = 0.15f;
        
        // Draw all corner highlights
        // Top front right
        drawCornerHighlight(box.maxX, box.maxY, box.maxZ, highlightLength);
        // Top front left
        drawCornerHighlight(box.minX, box.maxY, box.maxZ, highlightLength);
        // Top back right
        drawCornerHighlight(box.maxX, box.maxY, box.minZ, highlightLength);
        // Top back left
        drawCornerHighlight(box.minX, box.maxY, box.minZ, highlightLength);
        // Bottom front right
        drawCornerHighlight(box.maxX, box.minY, box.maxZ, highlightLength);
        // Bottom front left
        drawCornerHighlight(box.minX, box.minY, box.maxZ, highlightLength);
        // Bottom back right
        drawCornerHighlight(box.maxX, box.minY, box.minZ, highlightLength);
        // Bottom back left
        drawCornerHighlight(box.minX, box.minY, box.minZ, highlightLength);
        
        GL11.glEnd();
    }

    private void drawCornerHighlight(double x, double y, double z, float length) {
        // Horizontal line
        GL11.glVertex3d(x, y, z);
        GL11.glVertex3d(x - length, y, z);
        // Vertical line
        GL11.glVertex3d(x, y, z);
        GL11.glVertex3d(x, y - length, z);
        // Depth line
        GL11.glVertex3d(x, y, z);
        GL11.glVertex3d(x, y, z - length);
    }

    private void drawFilledBox(AxisAlignedBB box) {
        GL11.glBegin(GL11.GL_QUADS);
        // Bottom
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        
        // Top
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        
        // Front
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        
        // Back
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        
        // Left
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        
        // Right
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glEnd();
    }

    public static boolean isNPC(String playerName, EntityPlayer player) {
        // 1. Check if player is not in the actual world (ghost tab list entry)
        if (player == null || player.worldObj == null) {
            return true;
        }
    
        // 2. Check if the entity is not a real player instance (some NPCs are not EntityOtherPlayerMP)
        if (!(player instanceof EntityOtherPlayerMP)) {
            return true;
        }
    
        // 3. Heuristic name check (used by plugins like Citizens)
        if (playerName.startsWith("NPC_") || playerName.startsWith("Citizens")) {
            return true;
        }
    
        return false; // Probably a real player
    }
    
    
}
