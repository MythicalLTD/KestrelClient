package xyz.nayskutzu.mythicalclient.hacks;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xyz.nayskutzu.mythicalclient.MythicalClientMod;
import xyz.nayskutzu.mythicalclient.utils.ChatColor;
import xyz.nayskutzu.mythicalclient.utils.FriendlyPlayers;

public class PlayerHealth {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static boolean enabled;
    private static final PlayerHealth instance = new PlayerHealth();
    private static final int MAX_HEARTS_PER_ROW = 10;
    private static final String HEART_SYMBOL = "❤";
    private static final int DEFAULT_MAX_HEALTH = 20; // Default max health (10 hearts)

    public static void main() {
        try {
            System.out.println("PlayerHealth class booting up...");
            if (enabled) {
                enabled = false;
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(instance);
                MythicalClientMod.sendMessageToChat("&7Player health is now &cdisabled&7.", false);
            } else {
                enabled = true;
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(instance);
                MythicalClientMod.sendMessageToChat("&7Player health is now &aenabled&7.", false);
            }
        } catch (Exception e) {
            System.out.println("Failed to toggle PlayerHealth: " + e.getMessage());
        }
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Pre<?> event) {
        try {
            if (!enabled || mc == null || mc.thePlayer == null || event == null || event.entity == null) {
                return;
            }

            if (event.entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) event.entity;

                if (player == mc.thePlayer || player.isSpectator()) {
                    return;
                }

                String playerName = player.getName();
                if (playerName != null && !playerName.isEmpty() && !playerName.matches(".*[§&].*") && !playerName.matches(".*CIT-.*") && !FriendlyPlayers.isFriendly(playerName)) {
                    displayHealthUnderName(player);
                }
            }
        } catch (Exception e) {
            // Silently fail rather than crash
        }
    }

    private void displayHealthUnderName(EntityPlayer player) {
        GL11.glPushMatrix();
        try {
            if (player == null || mc == null || mc.getRenderManager() == null || mc.fontRendererObj == null) {
                return;
            }

            // Safely get health values with bounds checking
            float health;
            float maxHealth;
            try {
                health = Math.max(0, Math.min(player.getHealth(), Float.MAX_VALUE));
                maxHealth = Math.max(DEFAULT_MAX_HEALTH, Math.min(player.getMaxHealth(), Float.MAX_VALUE));
            } catch (Exception e) {
                return;
            }
            
            // Prevent division by zero and integer overflow
            if (health < 0 || maxHealth <= 0 || health > 1000 || maxHealth > 1000) {
                return;
            }
            
            // Calculate hearts with bounds checking
            int totalHearts = Math.min(500, (int) Math.ceil(health / 2.0));
            int maxHearts = Math.min(500, (int) Math.ceil(maxHealth / 2.0));
            int fullRows = Math.min(50, totalHearts / MAX_HEARTS_PER_ROW);
            int remainingHearts = totalHearts % MAX_HEARTS_PER_ROW;
            
            StringBuilder healthText = new StringBuilder();
            try {
                // Add full rows of hearts with length limit
                for (int row = 0; row < fullRows && row < 50; row++) {
                    for (int i = 0; i < MAX_HEARTS_PER_ROW && i < 50; i++) {
                        healthText.append(ChatColor.RED).append(HEART_SYMBOL);
                    }
                    if (row < fullRows - 1 || remainingHearts > 0) {
                        healthText.append("\n");
                    }
                }
                
                // Add remaining hearts with length limit
                if (remainingHearts > 0) {
                    for (int i = 0; i < remainingHearts && i < MAX_HEARTS_PER_ROW; i++) {
                        healthText.append(ChatColor.RED).append(HEART_SYMBOL);
                    }
                }
                
                // Add total hearts indicator with length checks
                if (maxHearts > 10 && healthText.length() < 1000) {
                    healthText.append(" ").append(ChatColor.GRAY).append("[")
                             .append(ChatColor.RED).append(Math.min(totalHearts, 999))
                             .append(ChatColor.GRAY).append("/")
                             .append(ChatColor.RED).append(Math.min(maxHearts, 999))
                             .append(ChatColor.GRAY).append("]");
                }
            } catch (Exception e) {
                // If string building fails, use a simple fallback
                healthText = new StringBuilder(ChatColor.RED + "❤");
            }

            // Safe GL state management
            try {
                // Store previous GL states
                boolean lightingEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
                boolean depthTestEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
                boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
                
                // Setup rendering with bounds checking
                float posX = (float) Math.max(-1000, Math.min(1000, player.posX - mc.getRenderManager().viewerPosX));
                float posY = (float) Math.max(-1000, Math.min(1000, player.posY - mc.getRenderManager().viewerPosY + player.height + 0.5F));
                float posZ = (float) Math.max(-1000, Math.min(1000, player.posZ - mc.getRenderManager().viewerPosZ));
                
                GL11.glTranslatef(posX, posY, posZ);
                GL11.glNormal3f(0.0F, 1.0F, 0.0F);
                
                // Make text always face the player
                try {
                    // Get the player's position relative to the entity
                    double deltaX = mc.thePlayer.posX - player.posX;
                    double deltaZ = mc.thePlayer.posZ - player.posZ;
                    float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX));
                    
                    // Calculate the rotation to face the player
                    float rotation = -yaw - 90f;
                    GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
                } catch (Exception e) {
                    // Fallback to default rotation if calculation fails
                    GL11.glRotatef(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
                    GL11.glRotatef(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
                }

                // Safe scale
                float scale = -0.025F;
                GL11.glScalef(scale, scale, scale);
                
                // Safely set GL states
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glDepthMask(false);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                try {
                    String[] lines = healthText.toString().split("\n");
                    for (int i = 0; i < lines.length && i < 50; i++) {
                        String line = lines[i];
                        if (line == null || line.isEmpty()) continue;
                        
                        try {
                            int width = Math.max(0, mc.fontRendererObj.getStringWidth(line));
                            int yOffset = (int) Math.max(-1000, Math.min(1000, i * 10 - (lines.length - 1) * 5));
                            
                            // Safe drawing with bounds checking
                            int xPos = (int) Math.max(-1000, Math.min(1000, -width / 2.0));
                            
                            // Draw outline safely
                            mc.fontRendererObj.drawString(line, xPos - 1, yOffset, 0x000000);
                            mc.fontRendererObj.drawString(line, xPos + 1, yOffset, 0x000000);
                            mc.fontRendererObj.drawString(line, xPos, yOffset - 1, 0x000000);
                            mc.fontRendererObj.drawString(line, xPos, yOffset + 1, 0x000000);
                            
                            // Draw actual text
                            mc.fontRendererObj.drawString(line, xPos, yOffset, 0xFFFFFF);
                        } catch (Exception e) {
                            continue; // Skip problematic line
                        }
                    }
                } catch (Exception e) {
                    // Fallback to simple display if text rendering fails
                    mc.fontRendererObj.drawString("❤", 0, 0, 0xFFFFFF);
                }

                // Safely restore GL states
                if (depthTestEnabled) GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glDepthMask(true);
                if (lightingEnabled) GL11.glEnable(GL11.GL_LIGHTING);
                if (!blendEnabled) GL11.glDisable(GL11.GL_BLEND);

            } catch (Exception e) {
                // Restore essential GL states if anything fails
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glDepthMask(true);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_BLEND);
            }
            
        } catch (Exception e) {
            // Silently fail rather than crash
        } finally {
            try {
                GL11.glPopMatrix();
            } catch (Exception e) {
                // Even if pop fails, don't crash
            }
        }
    }
}
