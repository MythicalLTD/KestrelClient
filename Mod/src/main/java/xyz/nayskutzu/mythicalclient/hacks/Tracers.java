package xyz.nayskutzu.mythicalclient.hacks;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xyz.nayskutzu.mythicalclient.MythicalClientMod;
import xyz.nayskutzu.mythicalclient.utils.FriendlyPlayers;

public class Tracers {

    private static final Minecraft mc = Minecraft.getMinecraft();
    public static boolean enabled;
    private static final Tracers instance = new Tracers();

    public static void main() {
        if (enabled) {
            enabled = false;
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(instance);
            MythicalClientMod.sendMessageToChat("&7Tracers are now &cdisabled&7.",false);
        } else {
            enabled = true;
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(instance);
            MythicalClientMod.sendMessageToChat("&7Tracers are now &aenabled&7.",false);
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null)
            return;
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player == mc.thePlayer || !player.isEntityAlive())
                continue;
            String playerName = player.getName();
            if (playerName != null && !playerName.isEmpty() && !playerName.matches(".*§.*")
                    && !playerName.matches(".*&.*") && !playerName.matches(".*CIT-.*")) {
                if (FriendlyPlayers.isFriendly(playerName)) {
                    continue;
                }
                if (isNPC(playerName, player)) {
                    continue;
                }
                double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks
                        - mc.getRenderManager().viewerPosX;
                double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks
                        - mc.getRenderManager().viewerPosY;
                double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks
                        - mc.getRenderManager().viewerPosZ;
                if (player == mc.thePlayer || mc.thePlayer.isSpectator()) {
                    return;
                }
                drawTracer(x, y, z);
            }
        }
    }

    private void drawTracer(double x, double y, double z) {
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Use chroma key green colors instead of rainbow
        float[] rgb = getChromaKeyColor();
        
        GL11.glLineWidth(2.0F);
        GlStateManager.color(rgb[0], rgb[1], rgb[2], 0.75F);

        GL11.glBegin(GL11.GL_LINES);
        GlStateManager.color(rgb[0], rgb[1], rgb[2], 0.1F);
        GL11.glVertex3d(0, mc.thePlayer.getEyeHeight(), 0);
        
        GlStateManager.color(rgb[0], rgb[1], rgb[2], 1.0F);
        GL11.glVertex3d(x, y + 1.62, z);
        GL11.glEnd();

        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private float[] getChromaKeyColor() {
        // Return chroma key compatible green with better visual appearance
        // Using a slightly more visually appealing green that's still chroma key friendly
        return new float[] {
            0.05f,  // Tiny bit of red for better visuals
            1.0f,   // Green = full brightness (main chroma key color)
            0.1f    // Tiny bit of blue for better visuals
        };
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
