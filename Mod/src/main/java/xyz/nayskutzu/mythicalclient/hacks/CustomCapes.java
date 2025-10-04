package xyz.nayskutzu.mythicalclient.hacks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xyz.nayskutzu.mythicalclient.utils.CapeManager;
import org.lwjgl.opengl.GL11;

public class CustomCapes {
    public static final CustomCapes instance = new CustomCapes();
    
    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Post event) {
        if (event.entityPlayer == null) return;
        
        CapeManager capeManager = CapeManager.getInstance();
        String playerName = event.entityPlayer.getName();
        
        if (capeManager.hasCape(playerName)) {
            String capeName = capeManager.getCapeName(playerName);
            ResourceLocation capeTexture = capeManager.getCapeTexture(capeName);
            
            if (capeTexture != null) {
                renderCape(event.entityPlayer, capeTexture, event.partialRenderTick);
            }
        }
    }
    
    private void renderCape(EntityPlayer player, ResourceLocation capeTexture, float partialTicks) {
        GlStateManager.pushMatrix();
        
        // Position cape behind player
        GlStateManager.translate(0.0F, 0.0F, -0.1F);
        
        // Calculate rotation
        float angle = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
        GlStateManager.rotate(angle, 0.0F, 1.0F, 0.0F);
        
        // Bind cape texture
        Minecraft.getMinecraft().getTextureManager().bindTexture(capeTexture);
        
        // Enable blending
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        // Draw cape using Tessellator like other code
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        
        wr.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX);
        wr.pos(-1.0, 0.0, -0.5).tex(0.0, 1.0).endVertex();
        wr.pos(1.0, 0.0, -0.5).tex(1.0, 1.0).endVertex();
        wr.pos(-1.0, 2.0, -0.5).tex(0.0, 0.0).endVertex();
        wr.pos(1.0, 2.0, -0.5).tex(1.0, 0.0).endVertex();
        tessellator.draw();
        
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
} 