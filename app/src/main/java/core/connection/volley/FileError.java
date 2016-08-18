package core.connection.volley;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

import core.util.RequestTarget;

public class FileError extends VolleyError {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final RequestTarget target;
    private final NetworkResponse response;
    private final String file;
    private final String url;

    public FileError(RequestTarget target, VolleyError error, String url, String file) {
        super(error);
        this.target = target;
        this.response = error.networkResponse;
        this.file = file;
        this.url = url;
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

    public String getFile() {
        return file;
    }

    public String getUrl() {
        return url;
    }
}
