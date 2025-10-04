package xyz.nayskutzu.mythicalclient.hacks;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ResourceESP {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static volatile boolean enabled;
    private static final ResourceESP instance = new ResourceESP();
    public static volatile JFrame frame;
    private static volatile Thread updateThread;
    private static final AtomicBoolean isUpdating = new AtomicBoolean(false);

    public static void render(fi.iki.elonen.NanoHTTPD.IHTTPSession session) {
        try {
            System.out.println("ResourceESP window opening...");
            if (enabled) {
                safelyDisableESP();
            } else {
                safelyEnableESP();
            }
        } catch (Exception e) {
            System.out.println("Failed to toggle ResourceESP: " + e.getMessage());
            safelyDisableESP(); // Ensure cleanup on error
        }
    }

    private static void safelyEnableESP() {
        try {
            enabled = true;
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(instance);
            SwingUtilities.invokeLater(() -> {
                try {
                    displayWindow();
                } catch (Exception e) {
                    System.out.println("Error displaying window: " + e.getMessage());
                    safelyDisableESP();
                }
            });
        } catch (Exception e) {
            System.out.println("Error enabling ESP: " + e.getMessage());
            safelyDisableESP();
        }
    }

    private static void safelyDisableESP() {
        try {
            enabled = false;
            isUpdating.set(false);
            
            if (updateThread != null) {
                updateThread.interrupt();
                updateThread = null;
            }

            net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(instance);
            
            SwingUtilities.invokeLater(() -> {
                try {
                    if (frame != null) {
                        frame.dispose();
                        frame = null;
                    }
                } catch (Exception e) {
                    System.out.println("Error disposing window: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.out.println("Error disabling ESP: " + e.getMessage());
        }
    }

    private static void displayWindow() {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Bedwars Resources");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(250, 250);
            
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Main panel with gradient background
            JPanel mainPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    int w = getWidth(), h = getHeight();
                    GradientPaint gp = new GradientPaint(0, 0, new Color(20, 20, 30), 
                        w, h, new Color(35, 35, 45));
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, w, h);
                }
            };
            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

            // Resource panels
            JPanel resourcesPanel = new JPanel();
            resourcesPanel.setOpaque(false);
            resourcesPanel.setLayout(new GridLayout(5, 1, 5, 10));

            // Create labels for both inventory and ground items
            JLabel[] inventoryLabels = createResourceLabels();
            JLabel[] groundLabels = createResourceLabels();

            // Add labels to the grid
            String[] resources = {"Iron", "Gold", "Diamonds", "Emeralds", "Wool"};
            Color[] colors = {
                new Color(220, 220, 220), // Iron
                new Color(255, 215, 0),   // Gold
                new Color(0, 255, 255),   // Diamond
                new Color(50, 255, 50),   // Emerald
                new Color(255, 255, 255)  // Wool
            };

            for (int i = 0; i < resources.length; i++) {
                JPanel itemPanel = new JPanel();
                itemPanel.setOpaque(false);
                itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
                
                // Resource name with colored box
                JPanel namePanel = new JPanel();
                namePanel.setOpaque(false);
                namePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
                
                JPanel colorBox = new JPanel() {
                    @Override
                    public Dimension getPreferredSize() {
                        return new Dimension(12, 12);
                    }
                };
                colorBox.setBackground(colors[i]);
                colorBox.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
                
                JLabel nameLabel = new JLabel(resources[i]);
                nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
                nameLabel.setForeground(colors[i]);
                
                namePanel.add(colorBox);
                namePanel.add(nameLabel);
                namePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
                itemPanel.add(namePanel);
                
                // Counts panel
                JPanel countsPanel = new JPanel();
                countsPanel.setOpaque(false);
                countsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));
                
                inventoryLabels[i].setForeground(new Color(200, 200, 220));
                groundLabels[i].setForeground(new Color(200, 200, 220));
                
                countsPanel.add(inventoryLabels[i]);
                countsPanel.add(groundLabels[i]);
                countsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
                itemPanel.add(countsPanel);
                
                resourcesPanel.add(itemPanel);
            }

            mainPanel.add(resourcesPanel);
            frame.add(mainPanel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            startUpdateThread(inventoryLabels, groundLabels);
        });
    }

    private static JLabel[] createResourceLabels() {
        return new JLabel[] {
            createStyledLabel("0"),
            createStyledLabel("0"),
            createStyledLabel("0"),
            createStyledLabel("0"),
            createStyledLabel("0")
        };
    }

    private static JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(new Color(180, 180, 200));
        return label;
    }

    private static void startUpdateThread(JLabel[] inventoryLabels, JLabel[] groundLabels) {
        if (updateThread != null) {
            updateThread.interrupt();
        }

        updateThread = new Thread(() -> {
            isUpdating.set(true);
            while (enabled && frame != null && frame.isVisible() && isUpdating.get()) {
                try {
                    if (!SwingUtilities.isEventDispatchThread()) {
                        SwingUtilities.invokeLater(() -> {
                            try {
                                safelyUpdateResources(inventoryLabels, groundLabels);
                            } catch (Exception e) {
                                System.out.println("Error updating resources: " + e.getMessage());
                            }
                        });
                    }
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.out.println("Error in update thread: " + e.getMessage());
                }
            }
        });
        updateThread.setDaemon(true); // Ensure thread doesn't prevent game exit
        updateThread.start();
    }

    private static void safelyUpdateResources(JLabel[] inventoryLabels, JLabel[] groundLabels) {
        try {
            if (mc == null || mc.thePlayer == null || mc.theWorld == null) return;
            
            updateInventoryResources(inventoryLabels);
            updateGroundResources(groundLabels);
        } catch (Exception e) {
            System.out.println("Error updating resources: " + e.getMessage());
        }
    }

    private static void updateInventoryResources(JLabel[] labels) {
        try {
            if (mc.thePlayer == null || mc.thePlayer.inventory == null || labels == null) return;

            int[] counts = new int[5];
            
            for (ItemStack stack : mc.thePlayer.inventory.mainInventory) {
                try {
                    if (stack == null || stack.getItem() == null) continue;
                    
                    String itemName = stack.getItem().getUnlocalizedName().toLowerCase();
                    int count = Math.max(0, Math.min(stack.stackSize, 64)); // Prevent invalid stack sizes

                    updateCounts(counts, itemName, count);
                } catch (Exception e) {
                    continue; // Skip problematic items
                }
            }

            updateLabels(labels, counts, false);
        } catch (Exception e) {
            System.out.println("Error in inventory update: " + e.getMessage());
        }
    }

    private static void updateGroundResources(JLabel[] labels) {
        try {
            if (mc.theWorld == null || labels == null) return;

            int[] counts = new int[5];

            for (Object entity : mc.theWorld.loadedEntityList) {
                try {
                    if (!(entity instanceof EntityItem)) continue;
                    
                    EntityItem item = (EntityItem) entity;
                    ItemStack stack = item.getEntityItem();
                    
                    if (stack == null || stack.getItem() == null) continue;
                    
                    String itemName = stack.getItem().getUnlocalizedName().toLowerCase();
                    int count = Math.max(0, Math.min(stack.stackSize, 64));

                    updateCounts(counts, itemName, count);
                } catch (Exception e) {
                    continue; // Skip problematic entities
                }
            }

            updateLabels(labels, counts, true);
        } catch (Exception e) {
            System.out.println("Error in ground items update: " + e.getMessage());
        }
    }

    private static void updateCounts(int[] counts, String itemName, int count) {
        try {
            if (itemName.contains("iron")) counts[0] = safeAdd(counts[0], count);
            if (itemName.contains("gold")) counts[1] = safeAdd(counts[1], count);
            if (itemName.contains("diamond")) counts[2] = safeAdd(counts[2], count);
            if (itemName.contains("emerald")) counts[3] = safeAdd(counts[3], count);
            if (itemName.contains("wool") || 
                (itemName.contains("stained") && itemName.contains("cloth"))) {
                counts[4] = safeAdd(counts[4], count);
            }
        } catch (Exception e) {
            System.out.println("Error updating counts: " + e.getMessage());
        }
    }

    private static int safeAdd(int a, int b) {
        long result = (long) a + b;
        return (int) Math.min(result, Integer.MAX_VALUE);
    }

    private static void updateLabels(JLabel[] labels, int[] counts, boolean isGround) {
        try {
            if (labels == null || counts == null || labels.length != counts.length) return;

            for (int i = 0; i < labels.length; i++) {
                try {
                    if (labels[i] != null) {
                        labels[i].setText(counts[i] + (isGround ? " ⬇" : " ⬆"));
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (Exception e) {
            System.out.println("Error updating labels: " + e.getMessage());
        }
    }

    @SubscribeEvent
    public void onTick(net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent event) {
        // Empty as updates are handled by the separate thread
    }
} 
