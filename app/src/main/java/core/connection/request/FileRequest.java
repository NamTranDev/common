package core.connection.request;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;

import java.util.Map;

import core.base.Param;
import core.connection.FileRequester;
import core.connection.volley.FileError;
import core.connection.volley.FileResponse;
import core.util.Constant;
import core.util.RequestTarget;
import core.util.Utils;


public class FileRequest extends Request<FileResponse> {

    /**
     * The name of the file will be stored after downloading.
     */
    private final String name;

    /**
     * The extension of the file will be stored after downloading.
     */
    private final String extension;

    /**
     * The path where the file will be stored after downloading.
     */
    private final String path;

    /**
     * The content parameters and headers for this request
     */
    private final Param content;

    /**
     * The target function of the service for this request, determined by
     * Constant.RequestTarget enum
     */
    private final RequestTarget target;

    /**
     * The request type for this request, either HTTP request or HTTPS request,
     * determined by Constant.RequestType
     */
    private final Constant.RequestType type;

    /**
     * The request method for this request, determined by Constant.RequestMethod
     */
    private final Constant.RequestMethod method;

    /**
     * The request url for this request, built by request type, server url and
     * target
     */
    private final String url;

    /**
     * The success result handler to integrate with Volley framework
     */
    private final Response.Listener<FileResponse> success;

    public FileRequest(String tag, Constant.RequestType type,
                       Constant.RequestMethod method, String address, RequestTarget target,
                       String api, Param content, FileRequester requester, String path, String name, String extension) {
        super(method.getValue(), type.toString() + address + api, requester);
        this.url = type.toString() + address + api;
        this.method = method;
        this.target = target;
        this.content = content;
        this.type = type;
        this.success = requester;
        this.name = name;
        this.path = path;
        this.extension = extension;
        setShouldCache(false);
        setTag(tag);
        setRetryPolicy(getRetryPolicy());
    }

    @Override
    public Request<?> setRetryPolicy(RetryPolicy retryPolicy) {
        if (target != null) {
            return super.setRetryPolicy(new DefaultRetryPolicy(
                    RequestTarget.timeout(target),
                    RequestTarget.retry(target),
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        }
        return super.setRetryPolicy(retryPolicy);
    }

    @Override
    public com.android.volley.Request.Priority getPriority() {
        return Priority.NORMAL;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        byte[] body = content.makeRequestBody();
        return (body == null || body.length == 0) ? super.getBody() : body;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = content.makeRequestHeaders();
        if (!Utils.isEmpty(content.makeBodyContentType())) {
            headers.remove(Constant.Header.CONTENT_TYPE.toString());
        }
        return headers;
    }

    @Override
    public String getBodyContentType() {
        if (content != null && !Utils.isEmpty(content.makeBodyContentType())) {
            return content.makeBodyContentType();
        }
        return super.getBodyContentType();
    }

    /**
     * @return the target
     */
    public RequestTarget getRequestTarget() {
        return target;
    }

    /**
     * @return the type
     */
    public Constant.RequestType getRequestType() {
        return type;
    }

    /**
     * @return the request method
     */
    public Constant.RequestMethod getRequestMethod() {
        return method;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }


    public String getFilePath() {
        return String.format("%s/%s.%s", path, name, extension);
    }

    @Override
    protected Response<FileResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(new FileResponse(response.data,
                response.headers, response.rawHeaders, target, url, getFilePath()), getCacheEntry());
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError error) {
        return new FileError(target, error, url, getFilePath());
    }

    @Override
    protected void deliverResponse(FileResponse response) {
        success.onResponse(response);
    }
}
