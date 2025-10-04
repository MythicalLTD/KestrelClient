package xyz.nayskutzu.mythicalclient.v2.routes;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import xyz.nayskutzu.mythicalclient.MythicalClientMod;

public class Home {
    public static Response render(IHTTPSession session) {
        // Handle the default HTML page
        return renderHomePage(session);
    }

    private static Response renderHomePage(IHTTPSession session) {
        if (MythicalClientMod.injectDashboard) {
            InputStream inputStream = Home.class.getResourceAsStream("/html/index.html");
            if (inputStream == null) {
                return NanoHTTPD.newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "File not found");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                String html = stringBuilder.toString();
                return NanoHTTPD.newFixedLengthResponse(Status.OK, "text/html",
                        html.replace("%player_name%", MythicalClientMod.data.get("name").toString())
                                .replace("%version%", MythicalClientMod.data.get("version").toString()));
            } catch (IOException e) {
                return NanoHTTPD.newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "Error reading file");
            }
        } else {
            // Return a simple JSON reply for the home route if not injecting dashboard
            String json = "{\"status\":\"ok\",\"message\":\"Welcome to MythicalClient API\",\"version\":\""
                    + MythicalClientMod.data.get("version").toString() + "\"}";
            return NanoHTTPD.newFixedLengthResponse(Status.OK, "application/json", json);
        }
    }
}
