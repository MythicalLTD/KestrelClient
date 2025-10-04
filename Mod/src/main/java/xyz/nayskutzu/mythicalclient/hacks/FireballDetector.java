package xyz.nayskutzu.mythicalclient.hacks;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import xyz.nayskutzu.mythicalclient.MythicalClientMod;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import xyz.nayskutzu.mythicalclient.utils.FriendlyPlayers;

public class FireballDetector {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static boolean enabled;
    private static final FireballDetector instance = new FireballDetector();
    private static final double DETECTION_RANGE = 45.0;
    private static final int WARNING_COOLDOWN = 20; // ticks (1 second)
    private int ticksSinceLastWarning = WARNING_COOLDOWN;
    private static final int INDICATOR_DURATION = 60; // 3 seconds (20 ticks/second)
    private List<FireballIndicator> activeFireballs = new ArrayList<>();
    
    private class FireballIndicator {
        private final double distance;
        private final double deltaX;
        private final double deltaY;
        private final double deltaZ;
        private int ticksLeft;
        
        public FireballIndicator(double distance, double deltaX, double deltaY, double deltaZ) {
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
                MythicalClientMod.sendMessageToChat("&7Fireball detector is now &cdisabled&7.", false);
            } else {
                enabled = true;
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(instance);
                MythicalClientMod.sendMessageToChat("&7Fireball detector is now &aenabled&7.", false);
            }
        } catch (Exception e) {
            System.out.println("Failed to toggle FireballDetector: " + e.getMessage());
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        try {
            if (!enabled || mc == null || mc.thePlayer == null || mc.theWorld == null) {
                return;
            }

            // Update active fireballs list
            activeFireballs.removeIf(indicator -> --indicator.ticksLeft <= 0);

            // Increment cooldown timer
            if (ticksSinceLastWarning < WARNING_COOLDOWN) {
                ticksSinceLastWarning++;
                return;
            }

            // Check for players holding fireballs
            for (Entity entity : mc.theWorld.loadedEntityList) {
                try {
                    if (entity instanceof net.minecraft.entity.player.EntityPlayer && entity != mc.thePlayer) {
                        net.minecraft.entity.player.EntityPlayer player = (net.minecraft.entity.player.EntityPlayer) entity;
                        
                        // Check if player is friendly first
                        if (FriendlyPlayers.isFriendly(player.getName())) {
                            continue; // Skip friendly players
                        }
                        
                        if (player.getHeldItem() != null && 
                            player.getHeldItem().getItem() instanceof net.minecraft.item.ItemFireball) {
                            
                            double distance = mc.thePlayer.getDistanceToEntity(player);
                            if (distance <= DETECTION_RANGE) {
                                // Calculate direction
                                double deltaX = player.posX - mc.thePlayer.posX;
                                double deltaY = player.posY - mc.thePlayer.posY;
                                double deltaZ = player.posZ - mc.thePlayer.posZ;
                                
                                String direction = getDirection(deltaX, deltaY, deltaZ);
                                
                                // Send warning message about player with fireball
                                String message = String.format(
                                    "&c⚠ &e%s has a fireball &c%s &eblocks %s! &c⚠",
                                    player.getName(),
                                    String.format("%.1f", distance),
                                    direction
                                );
                                
                                MythicalClientMod.sendMessageToChat(message, false);
                                
                                // Add to active fireballs for visual indicator
                                activeFireballs.add(new FireballIndicator(distance, deltaX, deltaY, deltaZ));
                                
                                // Play warning sound
                                try {
                                    mc.thePlayer.playSound("random.orb", 1.0F, 0.5F);
                                } catch (Exception e) {
                                    // Silently fail if sound fails
                                }
                                
                                ticksSinceLastWarning = 0;
                            }
                        }
                    }
                } catch (Exception e) {
                    continue; // Skip problematic entity
                }
            }

            // Check for flying fireballs
            for (Entity entity : mc.theWorld.loadedEntityList) {
                try {
                    if (entity instanceof EntityFireball) {
                        EntityFireball fireball = (EntityFireball) entity;
                        
                        // Check if the fireball was shot by a friendly player
                        if (fireball.shootingEntity instanceof EntityPlayer) {
                            EntityPlayer shooter = (EntityPlayer) fireball.shootingEntity;
                            if (FriendlyPlayers.isFriendly(shooter.getName())) {
                                continue; // Skip fireballs from friendly players
                            }
                        }
                        
                        double distance = mc.thePlayer.getDistanceToEntity(entity);
                        if (distance <= DETECTION_RANGE) {
                            // Calculate direction
                            double deltaX = entity.posX - mc.thePlayer.posX;
                            double deltaY = entity.posY - mc.thePlayer.posY;
                            double deltaZ = entity.posZ - mc.thePlayer.posZ;
                            
                            // Add to active fireballs
                            activeFireballs.add(new FireballIndicator(distance, deltaX, deltaY, deltaZ));
                            
                            // Get cardinal direction
                            String direction = getDirection(deltaX, deltaY, deltaZ);
                            
                            // Send warning message
                            String message = String.format(
                                "&c⚠ &eFireball detected %s blocks %s&e! &c⚠",
                                String.format("%.1f", distance),
                                direction
                            );
                            
                            MythicalClientMod.sendMessageToChat(message, false);
                            
                            // Play warning sound
                            try {
                                mc.thePlayer.playSound("random.orb", 1.0F, 1.0F);
                            } catch (Exception e) {
                                // Silently fail if sound fails
                            }
                            
                            // Reset cooldown
                            ticksSinceLastWarning = 0;
                            break;
                        }
                    }
                } catch (Exception e) {
                    continue;
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
                mc == null || mc.thePlayer == null || activeFireballs.isEmpty()) {
                return;
            }

            int screenWidth = event.resolution.getScaledWidth();
            int screenHeight = event.resolution.getScaledHeight();
            
            // Save GL state
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            
            try {
                for (FireballIndicator fireball : activeFireballs) {
                    // Calculate screen position
                    double angle = Math.atan2(fireball.deltaZ, fireball.deltaX);
                    float yaw = (float) Math.toDegrees(angle) - mc.thePlayer.rotationYaw;
                    float pitch = (float) Math.toDegrees(Math.atan2(fireball.deltaY, 
                            Math.sqrt(fireball.deltaX * fireball.deltaX + fireball.deltaZ * fireball.deltaZ))) 
                            - mc.thePlayer.rotationPitch;
                    
                    // Convert to screen coordinates
                    float x = screenWidth / 2f + (float) Math.sin(Math.toRadians(yaw)) * 100;
                    float y = screenHeight / 2f - (float) Math.sin(Math.toRadians(pitch)) * 100;
                    
                    // Draw indicator
                    drawFireballIndicator(x, y, fireball.distance, fireball.ticksLeft);
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

    private void drawFireballIndicator(float x, float y, double distance, int ticksLeft) {
        try {
            // Calculate alpha based on remaining time
            float alpha = Math.min(1.0f, ticksLeft / 20.0f);
            
            // Draw arrow pointing to fireball
            GL11.glColor4f(1.0f, 0.0f, 0.0f, alpha); // Red with fade-out
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
                ((int)(alpha * 255) << 24) | 0xFF0000); // Red text with fade-out
            
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

    public void onUpdate(EntityFireball fireball) {
        if (fireball == null || fireball.shootingEntity == null) {
            return;
        }
        
        if (fireball.shootingEntity instanceof EntityPlayer) {
            EntityPlayer shooter = (EntityPlayer) fireball.shootingEntity;
            if (FriendlyPlayers.isFriendly(shooter.getName())) {
                return; // Skip detection for friendly players
            }
        }
        // ... rest of your fireball detection code ...
    }
} 