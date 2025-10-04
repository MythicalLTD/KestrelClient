package xyz.nayskutzu.mythicalclient.hacks;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import xyz.nayskutzu.mythicalclient.MythicalClientMod;
import xyz.nayskutzu.mythicalclient.hacks.player.Info;

import javax.swing.*;
import java.awt.*;

public class NearPlayer {

    private static final Minecraft mc = Minecraft.getMinecraft();
    public static boolean enabled;
    private static final NearPlayer instance = new NearPlayer();
    public static JFrame frame;

    public static void main() {
        try {
            System.out.println("NearPlayer class booting up...");
            if (enabled) {
                enabled = false;
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(instance);
                frame.dispose();
            } else {
                enabled = true;
                net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(instance);
                displayWindow();
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            System.out.println("Failed to toggle NearPlayer: " + errorMessage);
            // mc.crashed(new CrashReport(errorMessage, e));

        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onPlayerTick(net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent event) {
        try {
            if (event.player == mc.thePlayer && enabled) {
                java.util.List<String> currentNearbyPlayers = new java.util.ArrayList<>();
                for (net.minecraft.entity.player.EntityPlayer player : mc.theWorld.playerEntities) {
                    if (player != mc.thePlayer
                            && mc.thePlayer.getDistanceToEntity(player) <= mc.gameSettings.renderDistanceChunks * 16) {
                        if (player.getName().length() < 4) {
                            continue;
                        }
                        currentNearbyPlayers.add(player.getName());
                        DefaultListModel<String> listModel = (DefaultListModel<String>) ((JList<?>) ((JScrollPane) ((JPanel) frame
                                .getContentPane().getComponent(0)).getComponent(0)).getViewport().getView()).getModel();
                        if (!listModel.contains(player.getName())) {
                            String playerName = player.getName();
                            if (isStaff(player)) {
                                playerName += " [STAFF]";
                            } else if (isInvisible(player)) {
                                playerName += " [INVISIBLE]";
                            } else if (isSpectator(player)) {
                                playerName += " [SPECTATOR]";
                            } else if (isFlying(player)) {
                                playerName += " [FLYING]";
                            }
                            if (!listModel.contains(playerName)) {
                                listModel.addElement(playerName);
                            }
                        }
                    }
                }
                java.util.Enumeration<String> elements = ((DefaultListModel<String>) ((JList<?>) ((JScrollPane) ((JPanel) frame
                        .getContentPane().getComponent(0)).getComponent(0)).getViewport().getView()).getModel())
                        .elements();
                while (elements.hasMoreElements()) {
                    String playerName = elements.nextElement();
                    if (!currentNearbyPlayers.contains(playerName)) {
                        removeItem(playerName);
                    }
                }
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            System.out.println("Failed to update nearby players: " + errorMessage);
        }
    }

    private static boolean isStaff(net.minecraft.entity.player.EntityPlayer player) {
        return player.capabilities.isFlying && player.isInvisible();
    }

    private static boolean isInvisible(net.minecraft.entity.player.EntityPlayer player) {
        return player.isInvisible();
    }

    private static boolean isSpectator(net.minecraft.entity.player.EntityPlayer player) {
        return player.isSpectator();
    }

    public static boolean isFlying(net.minecraft.entity.player.EntityPlayer player) {
        return player.capabilities.isFlying;
    }

    private static void addDoubleClickListener(JList<String> itemList) {
        itemList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int index = itemList.locationToIndex(evt.getPoint());
                    if (index >= 0) {
                        String playerName = itemList.getModel().getElementAt(index);
                        net.minecraft.entity.player.EntityPlayer player = getPlayerByName(playerName);
                        if (player != null) {
                            new Info(player);
                        }
                    }
                }
            }
        });
    }

    private static net.minecraft.entity.player.EntityPlayer getPlayerByName(String playerName) {
        for (net.minecraft.entity.player.EntityPlayer player : mc.theWorld.playerEntities) {
            if (player.getName().equals(playerName)) {
                return player;
            }
        }
        return null;
    }

    private static void displayWindow() {
        System.out.println("Displaying the MythicalClient window...");
        // Display the window here
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("MythicalClient");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(400, 230);
            frame.setIconImage(new ImageIcon(MythicalClientMod.class.getResource("/icon16.png")).getImage());
            frame.setVisible(true);
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Main panel with modern design
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(new Color(45, 45, 45));

            // List model and JList
            DefaultListModel<String> listModel = new DefaultListModel<>();
            JList<String> itemList = new JList<>(listModel);
            itemList.setBackground(new Color(60, 63, 65));
            itemList.setForeground(Color.WHITE);
            itemList.setSelectionBackground(new Color(75, 110, 175));
            itemList.setSelectionForeground(Color.WHITE);

            // Scroll pane for the list
            JScrollPane scrollPane = new JScrollPane(itemList);
            mainPanel.add(scrollPane, BorderLayout.CENTER);

            addDoubleClickListener(itemList);

            frame.add(mainPanel);

        });
    }

    public static void addItem(String item) {
        SwingUtilities.invokeLater(() -> {
            if (frame != null && frame.isVisible()) {
                @SuppressWarnings("unchecked")
                DefaultListModel<String> listModel = (DefaultListModel<String>) ((JList<?>) ((JScrollPane) ((JPanel) frame
                        .getContentPane().getComponent(0)).getComponent(0)).getViewport().getView()).getModel();
                listModel.addElement(item);
            }
        });
    }

    public static void removeItem(String item) {
        SwingUtilities.invokeLater(() -> {
            if (frame != null && frame.isVisible()) {
                @SuppressWarnings("unchecked")
                DefaultListModel<String> listModel = (DefaultListModel<String>) ((JList<String>) ((JScrollPane) ((JPanel) frame
                        .getContentPane().getComponent(0)).getComponent(0)).getViewport().getView()).getModel();
                listModel.removeElement(item);
            }
        });
    }
}
