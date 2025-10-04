package xyz.nayskutzu.mythicalclient.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CapeManager {
    private static final CapeManager INSTANCE = new CapeManager();
    private final Map<String, ResourceLocation> capeCache = new HashMap<>();
    private final Map<String, String> playerCapes = new HashMap<>();
    private final File capeDirectory;
    
    public static CapeManager getInstance() {
        return INSTANCE;
    }
    
    private CapeManager() {
        capeDirectory = new File(Minecraft.getMinecraft().mcDataDir, "mythicalclient/capes");
        if (!capeDirectory.exists()) {
            capeDirectory.mkdirs();
        }
        setupDefaultCapes();
    }
    
    private void setupDefaultCapes() {
        // Hardcode specific players with their capes
        playerCapes.put("ItzKevzyhhh", "mythical");
        playerCapes.put("test", "rainbow");
        playerCapes.put("admin", "admin");
        // Add more players here as needed
    }
    
    public boolean hasCape(String playerName) {
        return playerCapes.containsKey(playerName.toLowerCase());
    }
    
    public String getCapeName(String playerName) {
        return playerCapes.get(playerName.toLowerCase());
    }
    
    public ResourceLocation getCapeTexture(String capeName) {
        if (capeCache.containsKey(capeName)) {
            return capeCache.get(capeName);
        }
        
        // Try to load from local files
        File capeFile = new File(capeDirectory, capeName + ".png");
        if (capeFile.exists()) {
            try {
                BufferedImage image = ImageIO.read(capeFile);
                DynamicTexture texture = new DynamicTexture(image);
                ResourceLocation location = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("mythical_cape_" + capeName, texture);
                capeCache.put(capeName, location);
                return location;
            } catch (IOException e) {
                System.err.println("Failed to load cape texture: " + capeName);
            }
        }
        
        return null;
    }
    
    public File getCapeDirectory() {
        return capeDirectory;
    }
} 
