package core.connection.volley;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

import core.base.BaseParser;
import core.util.RequestTarget;

public class ParallelError extends VolleyError {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final RequestTarget target;
    private final BaseParser parser;
    private final String tag;
    private final NetworkResponse response;

    public ParallelError(String tag, RequestTarget target, BaseParser parser, VolleyError error) {
        super(error);
        this.target = target;
        this.parser = parser;
        this.response = error.networkResponse;
        this.tag = tag;
    }

    public BaseParser getParser() {
        return parser;
    }

    public String getTag() {
        return tag;
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
