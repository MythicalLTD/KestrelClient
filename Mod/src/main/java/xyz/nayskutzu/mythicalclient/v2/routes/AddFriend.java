package xyz.nayskutzu.mythicalclient.v2.routes;

import java.util.Map;
import java.util.HashMap;
import com.google.gson.Gson;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import xyz.nayskutzu.mythicalclient.utils.FriendlyPlayers;

public class AddFriend {
    private static final Gson gson = new Gson();

    public static Response render(IHTTPSession session) {
        try {
            Map<String, String> body = new HashMap<>();
            session.parseBody(body);
            String postData = body.get("postData");
            
            if (postData != null) {
                @SuppressWarnings("unchecked")
                Map<String, String> jsonData = gson.fromJson(postData, Map.class);
                String playerName = jsonData.get("playerName");
                if (playerName != null) {
                    FriendlyPlayers.addFriend(playerName);
                    Map<String, Boolean> response = new HashMap<>();
                    response.put("success", true);
                    return NanoHTTPD.newFixedLengthResponse(Status.OK, "application/json", 
                        gson.toJson(response));
                }
            }
            return createErrorResponse("Invalid request");
        } catch (Exception e) {
            return createErrorResponse(e.getMessage());
        }
    }

    private static Response createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return NanoHTTPD.newFixedLengthResponse(Status.BAD_REQUEST, "application/json", 
            gson.toJson(error));
    }
} 