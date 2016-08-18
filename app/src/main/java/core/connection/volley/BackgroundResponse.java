package core.connection.volley;

import java.util.List;
import java.util.Map;

import core.base.BaseResponse;
import core.util.RequestTarget;


@SuppressWarnings("unused")
public class BackgroundResponse extends BaseResponse {

    private final RequestTarget target;

    public BackgroundResponse(byte[] content, Map<String, String> headers, Map<String, List<String>> rawHeaders,
                              RequestTarget target) {
        super(content, headers, rawHeaders);
        this.target = target;
    }

    /**
     * @return the target
     */
    public RequestTarget getRequestTarget() {
        return target;
    }
}
