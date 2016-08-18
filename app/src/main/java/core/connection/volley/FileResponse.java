package core.connection.volley;

import java.util.List;
import java.util.Map;

import core.base.BaseResponse;
import core.util.RequestTarget;

public class FileResponse extends BaseResponse {

    private final RequestTarget target;
    private final String file;
    private final String url;

    public FileResponse(byte[] content, Map<String, String> headers, Map<String, List<String>> rawHeaders, RequestTarget target, String url, String file) {
        super(content, headers, rawHeaders);
        this.target = target;
        this.file = file;
        this.url = url;
    }

    /**
     * @return the target
     */
    public RequestTarget getRequestTarget() {
        return target;
    }

    public String getFile() {
        return file;
    }

    public String getUrl() {
        return url;
    }
}
