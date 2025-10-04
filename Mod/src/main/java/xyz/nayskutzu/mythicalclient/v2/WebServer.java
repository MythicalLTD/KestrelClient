package xyz.nayskutzu.mythicalclient.v2;

import fi.iki.elonen.NanoHTTPD;
import xyz.nayskutzu.mythicalclient.v2.routes.Home;
import xyz.nayskutzu.mythicalclient.v2.routes.Run;
import xyz.nayskutzu.mythicalclient.v2.routes.SendChat;
import xyz.nayskutzu.mythicalclient.v2.routes.Toggle;
import xyz.nayskutzu.mythicalclient.v2.routes.AddFriend;
import xyz.nayskutzu.mythicalclient.v2.routes.RemoveFriend;
import xyz.nayskutzu.mythicalclient.v2.routes.GetFriends;

public class WebServer extends NanoHTTPD {

    public WebServer(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            String uri = session.getUri();
            switch (uri) {
                case "/":
                    return Home.render(session);
                case "/toggle":
                    return Toggle.render(session);
                case "/run":
                    return Run.render(session);
                case "/sendchat":
                    return SendChat.render(session);
                case "/friends":
                    return GetFriends.render(session);
                case "/addfriend":
                    return AddFriend.render(session);
                case "/removefriend":
                    return RemoveFriend.render(session);
                case "/getfriends":
                    return GetFriends.render(session);
                default:
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found");
            }
        } catch (Exception e) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Internal Server Error "+e.getMessage());
        }
    }
}