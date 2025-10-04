package xyz.nayskutzu.mythicalclient.v2.routes;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import xyz.nayskutzu.mythicalclient.MythicalClientMod;
import xyz.nayskutzu.mythicalclient.v2.api.IResponse;


public class SendChat {
    public static Response render(IHTTPSession session) {
        @SuppressWarnings("deprecation")
        Map<String, String> params = session.getParms();
        String message = params.get("postData");
        if (message == null) {
            return IResponse.sendManualResponse(400, "Bad Request", "Missing message parameter", false, null);
        }

        // Process the message (e.g., send it to a chat server, log it, etc.)
        // For demonstration, we'll just return the message back in the response
        System.out.println("Sending message: " + message);
        System.out.println("Sending message to chat");
        try {
            MythicalClientMod.sendMessageToChat(message,true);
            Response response = NanoHTTPD.newFixedLengthResponse(Response.Status.REDIRECT, NanoHTTPD.MIME_HTML, "<html><body>Redirecting...</body></html>");
            response.addHeader("Location", "/");
            return response;
        } catch (Exception e) {
            return IResponse.sendManualResponse(500, "Internal Server Error", "Failed to send message"+e.getMessage(), false, null);
        }
    }
}
