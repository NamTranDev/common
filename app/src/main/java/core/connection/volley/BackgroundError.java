package core.connection.volley;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

import core.util.RequestTarget;


@SuppressWarnings("unused")
public class BackgroundError extends VolleyError {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final RequestTarget target;
    private final NetworkResponse response;

    public BackgroundError(RequestTarget target, VolleyError error) {
        super(error);
        this.target = target;
        this.response = error.networkResponse;
    }

    /**
     * @return the target
     */
    public RequestTarget getRequestTarget() {
        return target;
    }

    /**
     * @return the response
     */
    public NetworkResponse getResponse() {
        return response;
    }


}
