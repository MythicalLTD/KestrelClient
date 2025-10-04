package xyz.nayskutzu.mythicalclient.hacks.player;

import javax.swing.*;
import java.awt.*;

import xyz.nayskutzu.mythicalclient.MythicalClientMod;

public class Info {

    public static JFrame frame;

    public Info(net.minecraft.entity.player.EntityPlayer targetPlayer) {
        net.minecraft.entity.player.EntityPlayer selfPlayer = net.minecraft.client.Minecraft.getMinecraft().thePlayer;

        if (targetPlayer == selfPlayer) {
            MythicalClientMod.sendMessageToChat("&cYou can't get info about yourself.", false);
            return;
        }

        if (targetPlayer == null) {
            MythicalClientMod.sendMessageToChat("&cPlayer not found.", false);
            return;
        }

        Timer[] timer = new Timer[1];
        timer[0] = new Timer(1000, e -> {
            if (targetPlayer == null || targetPlayer.isDead || targetPlayer.getDistanceToEntity(selfPlayer) > 100) {
                timer[0].stop();
                frame.dispose();
                return;
            }

            // Update player position
            String updatedPlayerX = String.format("%.2f", targetPlayer.posX);
            String updatedPlayerY = String.format("%.2f", targetPlayer.posY);
            String updatedPlayerZ = String.format("%.2f", targetPlayer.posZ);

            // Update other player stats
            String updatedPlayerHealth = String.valueOf(targetPlayer.getHealth());
            String updatedPlayerArmor = String.valueOf(targetPlayer.getTotalArmorValue());
            String updatedPlayerFood = String.valueOf(targetPlayer.getFoodStats().getFoodLevel());
            String updatedPlayerSaturation = String.valueOf(targetPlayer.getFoodStats().getSaturationLevel());
            String updatedPlayerDimension = String.valueOf(targetPlayer.dimension);
            String updatedPlayerGameMode = targetPlayer.capabilities.isCreativeMode ? "Creative"
                    : targetPlayer.capabilities.isFlying ? "Spectator" : "Survival";
            String updatedPlayerFlying = targetPlayer.capabilities.isFlying ? "Yes" : "No";
            String updatedPlayerSneaking = targetPlayer.isSneaking() ? "Yes" : "No";
            String updatedPlayerSprinting = targetPlayer.isSprinting() ? "Yes" : "No";
            String updatedPlayerInvisible = targetPlayer.isInvisible() ? "Yes" : "No";

            // Update the UI components
            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
                Component[] components = frame.getContentPane().getComponents();
                for (Component component : components) {
                    if (component instanceof JPanel) {
                        JPanel panel = (JPanel) component;
                        Component[] labels = panel.getComponents();
                        for (int i = 0; i < labels.length; i++) {
                            if (labels[i] instanceof JLabel) {
                                JLabel label = (JLabel) labels[i];
                                switch (label.getText()) {
                                    case "Player Name:":
                                        ((JLabel) labels[i + 1]).setText(targetPlayer.getName());
                                        break;
                                    case "Player UUID:":
                                        ((JLabel) labels[i + 1]).setText(targetPlayer.getUniqueID().toString());
                                        break;
                                    case "Player Health:":
                                        ((JLabel) labels[i + 1]).setText(updatedPlayerHealth);
                                        break;
                                    case "Player Armor:":
                                        ((JLabel) labels[i + 1]).setText(updatedPlayerArmor);
                                        break;
                                    case "Player Food:":
                                        ((JLabel) labels[i + 1]).setText(updatedPlayerFood);
                                        break;
                                    case "Player Saturation:":
                                        ((JLabel) labels[i + 1]).setText(updatedPlayerSaturation);
                                        break;
                                    case "Player Dimension:":
                                        ((JLabel) labels[i + 1]).setText(updatedPlayerDimension);
                                        break;
                                    case "Player Location:":
                                        ((JLabel) labels[i + 1]).setText(
                                                updatedPlayerX + ", " + updatedPlayerY + ", " + updatedPlayerZ);
                                        break;
                                    case "Player Game Mode:":
                                        ((JLabel) labels[i + 1]).setText(updatedPlayerGameMode);
                                        break;
                                    case "Player Flying:":
                                        ((JLabel) labels[i + 1]).setText(updatedPlayerFlying);
                                        break;
                                    case "Player Sneaking:":
                                        ((JLabel) labels[i + 1]).setText(updatedPlayerSneaking);
                                        break;
                                    case "Player Sprinting:":
                                        ((JLabel) labels[i + 1]).setText(updatedPlayerSprinting);
                                        break;
                                    case "Player Invisible:":
                                        ((JLabel) labels[i + 1]).setText(updatedPlayerInvisible);
                                        break;
                                }
                            }
                        }
                    }
                }
            });
        });

        // Start the timer
        timer[0].start();

        // Display the window here
        SwingUtilities.invokeLater(() -> {
            String playerName = targetPlayer.getName();
            String playerUUID = targetPlayer.getUniqueID().toString();

            String playerHealth = String.valueOf(targetPlayer.getHealth());
            String playerArmor = String.valueOf(targetPlayer.getTotalArmorValue());
            String playerFood = String.valueOf(targetPlayer.getFoodStats().getFoodLevel());
            String playerSaturation = String.valueOf(targetPlayer.getFoodStats().getSaturationLevel());
            String playerDimension = String.valueOf(targetPlayer.dimension);

            String playerX = String.format("%.2f", targetPlayer.posX);
            String playerY = String.format("%.2f", targetPlayer.posY);
            String playerZ = String.format("%.2f", targetPlayer.posZ);

            String playerGameMode = targetPlayer.capabilities.isCreativeMode ? "Creative"
                    : targetPlayer.capabilities.isFlying ? "Spectator" : "Survival";
            String playerFlying = targetPlayer.capabilities.isFlying ? "Yes" : "No";
            String playerSneaking = targetPlayer.isSneaking() ? "Yes" : "No";
            String playerSprinting = targetPlayer.isSprinting() ? "Yes" : "No";
            String playerInvisible = targetPlayer.isInvisible() ? "Yes" : "No";

            frame = new JFrame("MythicalClient - Player Info (" + playerName + ")");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(400, 230);
            frame.setIconImage(new ImageIcon(MythicalClientMod.class.getResource("/icon16.png")).getImage());
            frame.setVisible(true);

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());

            // Create a modern look with bold labels and padding
            Font boldFont = new Font("Arial", Font.BOLD, 14);

            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.anchor = GridBagConstraints.WEST;

            String[][] labelsAndValues = {
                    { "Player Name:", playerName },
                    { "Player UUID:", playerUUID },
                    { "Player Health:", playerHealth },
                    { "Player Armor:", playerArmor },
                    { "Player Food:", playerFood },
                    { "Player Saturation:", playerSaturation },
                    { "Player Dimension:", playerDimension },
                    { "Player Location:", playerX + ", " + playerY + ", " + playerZ },
                    { "Player Game Mode:", playerGameMode },
                    { "Player Flying:", playerFlying },
                    { "Player Sneaking:", playerSneaking },
                    { "Player Sprinting:", playerSprinting },
                    { "Player Invisible:", playerInvisible }
            };

            for (int i = 0; i < labelsAndValues.length; i++) {
                gbc.gridx = 0;
                gbc.gridy = i;
                JLabel label = new JLabel(labelsAndValues[i][0]);
                label.setFont(boldFont);
                infoPanel.add(label, gbc);
                gbc.gridx = 1;
                JLabel valueLabel = new JLabel(labelsAndValues[i][1]);
                valueLabel.setName(labelsAndValues[i][0]); // Set the name for easy identification
                infoPanel.add(valueLabel, gbc);
            }

            panel.add(infoPanel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

            JButton refreshButton = new JButton("Refresh");
            refreshButton.setFont(boldFont);
            refreshButton.addActionListener(e -> {
                // Refresh logic here
            });
            buttonPanel.add(refreshButton);

            JButton closeButton = new JButton("Close");
            closeButton.setFont(boldFont);
            closeButton.addActionListener(e -> frame.dispose());
            buttonPanel.add(closeButton);

            JButton crashButton = new JButton("Crash");
            crashButton.setFont(boldFont);
            crashButton.addActionListener(e -> {
                targetPlayer.setInvisible(true);
            });
            buttonPanel.add(crashButton);

            JButton killButton = new JButton("Kill");
            killButton.setFont(boldFont);
            killButton.addActionListener(e -> {
                
            });
            buttonPanel.add(killButton);

            JButton spawnObj = new JButton("Spawn Obj");
            spawnObj.setFont(boldFont);
            spawnObj.addActionListener(e -> {
                for (int j = 0; j < 10; j++) {
                    net.minecraft.client.Minecraft.getMinecraft().theWorld.playSound(
                        targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ, 
                        "random.explode", 1.0F, 1.0F, false
                    );

                    for (int i = 0; i < 10; i++) {
                        double offsetX = (Math.random() - 0.5) * 2.0;
                        double offsetY = (Math.random() - 0.5) * 2.0;
                        double offsetZ = (Math.random() - 0.5) * 2.0;
                        net.minecraft.client.Minecraft.getMinecraft().effectRenderer.addEffect(
                            new net.minecraft.client.particle.EntityLargeExplodeFX.Factory().getEntityFX(
                                0, net.minecraft.client.Minecraft.getMinecraft().theWorld, 
                                targetPlayer.posX + offsetX, targetPlayer.posY + offsetY, targetPlayer.posZ + offsetZ, 
                                0, 0, 0, null
                            )
                        );
                    }
                }
            });
            buttonPanel.add(spawnObj);

            panel.add(buttonPanel, BorderLayout.SOUTH);

            frame.add(panel);
            frame.setSize(550, 600); // Increase the size to fit all data nicely

        });
    }
}
