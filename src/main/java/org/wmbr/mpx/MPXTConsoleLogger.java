package org.wmbr.mpx;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.json.JSONObject;

public class MPXTConsoleLogger implements HttpResponseHandler<String> {
    private static final Logger LOGGER = Logger.getLogger(HttpGetPoller.class.getName());

    public void handle(HttpResponse<String> response) {

        try {
            JSONObject body = new JSONObject(response.body());
            List<Integer> mpxt = body.getJSONArray("mpxt")
                    .toList()
                    .stream()
                    .map((o) -> (Integer) o)
                    .collect(Collectors.toList());
            String vals = mpxt
                    .stream()
                    .map((l) -> l.toString())
                    .collect(Collectors.joining(", "));
            System.out.println(System.currentTimeMillis() + ", " + vals);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "could not parse MPXT data", e);
            throw e;
        }
    }

}
