package xyz.nayskutzu.mythicalclient.v2.routes;

import java.util.Map;
import java.util.HashMap;
import com.google.gson.Gson;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import xyz.nayskutzu.mythicalclient.utils.FriendlyPlayers;

public class GetFriends {
    private static final Gson gson = new Gson();

    public static Response render(IHTTPSession session) {
        try {
            return NanoHTTPD.newFixedLengthResponse(Status.OK, "application/json", 
                gson.toJson(FriendlyPlayers.getFriendsList()));
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