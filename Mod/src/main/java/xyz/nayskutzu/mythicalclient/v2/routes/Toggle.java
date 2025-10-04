package xyz.nayskutzu.mythicalclient.v2.routes;

import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import net.minecraft.client.Minecraft;
import xyz.nayskutzu.mythicalclient.MessageBox;
import xyz.nayskutzu.mythicalclient.MythicalClientMod;
import xyz.nayskutzu.mythicalclient.hacks.BridgeHack;
import xyz.nayskutzu.mythicalclient.hacks.ChestESP;
import xyz.nayskutzu.mythicalclient.hacks.NearPlayer;
import xyz.nayskutzu.mythicalclient.hacks.NoGUI;
import xyz.nayskutzu.mythicalclient.hacks.NukeProcess;
import xyz.nayskutzu.mythicalclient.hacks.PlayerESP;
import xyz.nayskutzu.mythicalclient.hacks.PlayerHealth;
import xyz.nayskutzu.mythicalclient.hacks.TntTimer;
import xyz.nayskutzu.mythicalclient.hacks.Tracers;
import xyz.nayskutzu.mythicalclient.hacks.Trajectories;
import xyz.nayskutzu.mythicalclient.hacks.FireballDetector;
import xyz.nayskutzu.mythicalclient.hacks.BedESP;
import xyz.nayskutzu.mythicalclient.hacks.BowDetector;
import xyz.nayskutzu.mythicalclient.hacks.ResourceESP;
import xyz.nayskutzu.mythicalclient.hacks.ResourceGroundFinder;
import xyz.nayskutzu.mythicalclient.hacks.BanMe;
import xyz.nayskutzu.mythicalclient.v2.api.IResponse;

public class Toggle {
    public static Response render(IHTTPSession session) {
        Map<String, String> body = new HashMap<>();
        try {
            session.parseBody(body);
        } catch (Exception e) {
            MessageBox.error("ERROR", e.getMessage());
            return IResponse.sendManualResponse(500, "Internal Server Error", "Failed to parse body", false, null);

        }
        String modName = body.get("postData");
        if (modName == null) {
            return IResponse.sendManualResponse(400, "Bad Request", "Missing mod parameter", false, null);
        }
        modName = modName.trim();
        modName = body.get("postData");
        if (modName != null && modName.startsWith("{\"mod\":\"") && modName.endsWith("\"}")) {
            modName = modName.substring(8, modName.length() - 2);
        } else {
            return IResponse.sendManualResponse(400, "Bad Request", "Invalid mod parameter format", false, null);
        }
        System.out.println("Toggling mod: " + modName);

        if (modName.equals("BridgeHack")) {
            BridgeHack.main();
        }
        if (modName.equals("NukeProcess")) {
            NukeProcess.main();
        }
        if (modName.equals("PlayerESP")) {
            PlayerESP.main();
        }
        if (modName.equals("Tracers")) {
            Tracers.main();
        }
        if (modName.equals("BedESP")) {
            BedESP.main();
        }
        if (modName.equals("Trajectories")) {
            Trajectories.main();
        }
        if (modName.equals("KillGame")) {
            Minecraft.getMinecraft().shutdown();
        }
        if (modName.equals("ForceOP")) {
            MythicalClientMod.sendMessageToChat("&7[Server: Opped %player%]", true);
        }
        if (modName.equals("ChestESP")) {
            ChestESP.main();
        }
        if (modName.equals("PlayerHealth")) {
            PlayerHealth.main();
        }

        if (modName.equals("TntTimer")) {
            TntTimer.main();
        }
        if (modName.equals("NoGUI")) {
            NoGUI.main();
        }
        if (modName.equals("NearPlayer")) {
            NearPlayer.main();
        }
        if (modName.equals("FireballDetector")) {
            FireballDetector.main();
        }
        if (modName.equals("BowDetector")) {
            BowDetector.main();
        }
        if (modName.equals("ResourceESP")) {
            ResourceESP.render(session);
        }
        if (modName.equals("ResourceGroundFinder")) {
            ResourceGroundFinder.main();
        }
        if (modName.equals("BanMe")) {
            BanMe.main();
        }
        return IResponse.OK("Updated", null);
    }
}
