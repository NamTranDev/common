package core.connection.volley;

import java.util.List;
import java.util.Map;

import core.base.BaseParser;
import core.base.BaseResponse;
import core.util.RequestTarget;

public class QueueResponse extends BaseResponse {

    private final RequestTarget target;

    private final BaseParser parser;

    public QueueResponse(byte[] content, BaseParser parser, Map<String, String> headers, Map<String, List<String>> rawHeaders,
                         RequestTarget target) {
        super(content, headers, rawHeaders);
        this.target = target;
        this.parser = parser;
    }

    public BaseParser getParser() {
        return parser;
    }

    /**
     * @return the target
     */
    public RequestTarget getRequestTarget() {
        return target;
    }
}
