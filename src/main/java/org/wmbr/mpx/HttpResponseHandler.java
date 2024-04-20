package org.wmbr.mpx;

import java.net.http.HttpResponse;

public interface HttpResponseHandler<T> {
    public void handle(HttpResponse<T> response);
}
