package xyz.nayskutzu.mythicalclient.hacks;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xyz.nayskutzu.mythicalclient.MythicalClientMod;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.projectile.EntityArrow;
import xyz.nayskutzu.mythicalclient.utils.FriendlyPlayers;

public class BowDetector {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static boolean enabled;
    private static final BowDetector instance = new BowDetector();
    private static final double DETECTION_RANGE = 45.0;
    private static final int WARNING_COOLDOWN = 20; // ticks (1 second)
    private int ticksSinceLastWarning = WARNING_COOLDOWN;
    private static final int INDICATOR_DURATION = 60;
    private List<BowIndicator> activeBows = new ArrayList<>();
    
    private class BowIndicator {
        private final double distance;
        private final double deltaX;
        private final double deltaY;
        private final double deltaZ;
        private int ticksLeft;
        
        public BowIndicator(double distance, double deltaX, double deltaY, double deltaZ) {
            this.distance = distance;
            this.deltaX = deltaX;
            this.deltaY = deltaY;
            this.deltaZ = deltaZ;
            this.ticksLeft = INDICATOR_DURATION;
        }
    }

    public static void main() {
        try {
            if (enabled) {
                enabled = false;
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(instance);
                MythicalClientMod.sendMessageToChat("&7Bow detector is now &cdisabled&7.", false);
            } else {
                enabled = true;
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(instance);
                MythicalClientMod.sendMessageToChat("&7Bow detector is now &aenabled&7.", false);
            }
        } catch (Exception e) {
            System.out.println("Failed to toggle BowDetector: " + e.getMessage());
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        try {
            if (!enabled || mc.thePlayer == null || mc.theWorld == null) {
                return;
            }

            // Update active bows list
            activeBows.removeIf(indicator -> --indicator.ticksLeft <= 0);

            // Increment cooldown timer
            if (ticksSinceLastWarning < WARNING_COOLDOWN) {
                ticksSinceLastWarning++;
                return;
            }

            // Check for players holding bows
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (player != mc.thePlayer && player.isEntityAlive()) {
                    // Check if player is friendly first
                    if (FriendlyPlayers.isFriendly(player.getName())) {
                        continue; // Skip friendly players
                    }
                    
                    if (player.getCurrentEquippedItem() != null && 
                        player.getCurrentEquippedItem().getItem() instanceof ItemBow) {
                        
                        double distance = mc.thePlayer.getDistanceToEntity(player);
                        if (distance <= DETECTION_RANGE) {
                            // Calculate direction
                            double deltaX = player.posX - mc.thePlayer.posX;
                            double deltaY = player.posY - mc.thePlayer.posY;
                            double deltaZ = player.posZ - mc.thePlayer.posZ;
                            
                            String direction = getDirection(deltaX, deltaY, deltaZ);
                            
                            // Send warning message
                            String message = String.format(
                                "&c⚠ &e%s has a bow &c%.1f &eblocks %s! &c⚠",
                                player.getName(),
                                distance,
                                direction
                            );
                            
                            MythicalClientMod.sendMessageToChat(message, false);
                            
                            // Add to active bows for visual indicator
                            activeBows.add(new BowIndicator(distance, deltaX, deltaY, deltaZ));
                            
                            // Play warning sound
                            try {
                                mc.thePlayer.playSound("random.orb", 1.0F, 2.0F);
                            } catch (Exception e) {
                                // Silently fail if sound fails
                            }
                            
                            ticksSinceLastWarning = 0;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Silently fail rather than crash
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        try {
            if (!enabled || event.type != RenderGameOverlayEvent.ElementType.ALL || 
                mc.thePlayer == null || activeBows.isEmpty()) {
                return;
            }

            int screenWidth = event.resolution.getScaledWidth();
            int screenHeight = event.resolution.getScaledHeight();
            
            // Save GL state
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            
            try {
                for (BowIndicator bow : activeBows) {
                    // Calculate screen position
                    double angle = Math.atan2(bow.deltaZ, bow.deltaX);
                    float yaw = (float) Math.toDegrees(angle) - mc.thePlayer.rotationYaw;
                    float pitch = (float) Math.toDegrees(Math.atan2(bow.deltaY, 
                            Math.sqrt(bow.deltaX * bow.deltaX + bow.deltaZ * bow.deltaZ))) 
                            - mc.thePlayer.rotationPitch;
                    
                    // Convert to screen coordinates
                    float x = screenWidth / 2f + (float) Math.sin(Math.toRadians(yaw)) * 100;
                    float y = screenHeight / 2f - (float) Math.sin(Math.toRadians(pitch)) * 100;
                    
                    // Draw indicator
                    drawBowIndicator(x, y, bow.distance, bow.ticksLeft);
                }
            } finally {
                // Restore GL state
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glPopMatrix();
            }
        } catch (Exception e) {
            // Silently fail rather than crash
        }
    }

    private void drawBowIndicator(float x, float y, double distance, int ticksLeft) {
        try {
            // Calculate alpha based on remaining time
            float alpha = Math.min(1.0f, ticksLeft / 20.0f);
            
            // Draw arrow pointing to bow
            GL11.glColor4f(1.0f, 0.0f, 0.0f, alpha);
            GL11.glBegin(GL11.GL_TRIANGLES);
            GL11.glVertex2f(x, y - 10);
            GL11.glVertex2f(x - 5, y + 5);
            GL11.glVertex2f(x + 5, y + 5);
            GL11.glEnd();
            
            // Draw distance text
            String distanceText = String.format("%.1f", distance);
            mc.fontRendererObj.drawStringWithShadow(distanceText, 
                x - mc.fontRendererObj.getStringWidth(distanceText) / 2,
                y + 10, 
                ((int)(alpha * 255) << 24) | 0xFF0000);
            
        } catch (Exception e) {
            // Silently fail rather than crash
        }
    }

    private String getDirection(double deltaX, double deltaY, double deltaZ) {
        try {
            StringBuilder direction = new StringBuilder();
            
            // Vertical direction
            if (Math.abs(deltaY) > 2.0) {
                direction.append(deltaY > 0 ? "above" : "below");
            }
            
            // Horizontal direction
            if (Math.abs(deltaX) > 2.0 || Math.abs(deltaZ) > 2.0) {
                if (direction.length() > 0) {
                    direction.append(" and ");
                }
                
                // Calculate angle
                double angle = Math.toDegrees(Math.atan2(deltaZ, deltaX));
                angle = (angle + 360) % 360; // Normalize to 0-360
                
                // Convert angle to cardinal direction
                if (angle >= 337.5 || angle < 22.5) {
                    direction.append("east");
                } else if (angle < 67.5) {
                    direction.append("northeast");
                } else if (angle < 112.5) {
                    direction.append("north");
                } else if (angle < 157.5) {
                    direction.append("northwest");
                } else if (angle < 202.5) {
                    direction.append("west");
                } else if (angle < 247.5) {
                    direction.append("southwest");
                } else if (angle < 292.5) {
                    direction.append("south");
                } else {
                    direction.append("southeast");
                }
            }
            
            return direction.length() > 0 ? direction.toString() : "nearby";
            
        } catch (Exception e) {
            return "nearby"; // Fallback if direction calculation fails
        }
    }

    public void onUpdate(EntityArrow arrow) {
        if (arrow == null || arrow.shootingEntity == null) {
            return;
        }
        
        if (arrow.shootingEntity instanceof EntityPlayer) {
            EntityPlayer shooter = (EntityPlayer) arrow.shootingEntity;
            if (FriendlyPlayers.isFriendly(shooter.getName())) {
                return; // Skip detection for friendly players
            }
        }
        // ... rest of your bow detection code ...
    }
} 