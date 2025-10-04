package xyz.nayskutzu.mythicalclient.v2.routes;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import xyz.nayskutzu.mythicalclient.v2.api.IResponse;

public class Run {
    public static Response render(IHTTPSession session) {
        return IResponse.OK("Updated", null);
    }
}
