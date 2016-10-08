package org.hcjf.io.net.http.view;

import org.hcjf.io.net.http.HttpResponse;
import org.hcjf.io.net.http.layered.LayeredResponse;

/**
 * @author Andrés Medina
 * @email armedina@gmail.com
 */
public class ViewResponse extends LayeredResponse {
    public ViewResponse(HttpResponse httpResponse) {
        super(httpResponse);
    }
}
