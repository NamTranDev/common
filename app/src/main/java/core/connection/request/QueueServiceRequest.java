package core.connection.request;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;

import java.util.Arrays;
import java.util.Map;

import core.base.BaseParser;
import core.base.Param;
import core.connection.QueueServiceRequester;
import core.connection.volley.QueueError;
import core.connection.volley.QueueResponse;
import core.util.Constant;
import core.util.Constant.RequestMethod;
import core.util.Constant.RequestType;
import core.util.RequestTarget;
import core.util.Utils;

/**
 * @author Tyrael
 * @version 1.0 <br>
 *          <br>
 *          <b>Class Overview</b> <br>
 *          <br>
 *          - Represents a class for forming a queue service request extends
 *          from Request<?> of Volley framework. This class will handle the
 *          type, method, url, time out, retry, headers, parameters and the
 *          return format of each request <br>
 *          - Every request will be marked with a tag which can be canceled if
 *          it is no longer needed <br>
 *          - The content of the result will be delivered to the
 *          QueueServiceRequester as a QueueResponse including the data, request
 *          target and headers
 * @since April 2014
 */

@SuppressWarnings("SameParameterValue")
public class QueueServiceRequest extends Request<QueueResponse> {

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
     * The parser for this request's response
     */
    private final BaseParser parser;

    /**
     * The request type for this request, either HTTP request or HTTPS request,
     * determined by Constant.RequestType
     */
    private final RequestType type;

    /**
     * The request method for this request, determined by Constant.RequestMethod
     */
    private final RequestMethod method;

    /**
     * The request url for this request, built by request type, server url and
     * target
     */
    private final String url;

    /**
     * The success result handler to integrate with Volley framework
     */
    private final Listener<QueueResponse> success;

    public QueueServiceRequest(String tag, RequestType type,
                               RequestMethod method, String address, RequestTarget target,
                               String api, Param content, BaseParser parser, QueueServiceRequester requester) {
        super(method.getValue(), type.toString() + address + api, requester);
        this.method = method;
        this.url = type.toString() + address + api;
        this.success = requester;
        this.target = target;
        this.content = content;
        this.parser = parser;
        this.type = type;
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
        return Priority.LOW;
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
        if (content != null) {
            return content.makeBodyContentType();
        }
        return super.getBodyContentType();
    }

    /**
     * @return the type
     */
    public RequestType getRequestType() {
        return type;
    }

    /**
     * @return the request method
     */
    public RequestMethod getRequestMethod() {
        return method;
    }

    /**
     * @return the request target
     */
    public RequestTarget getRequestTarget() {
        return target;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    @Override
    protected void deliverResponse(QueueResponse response) {
        success.onResponse(response);
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError error) {
        return new QueueError(target, parser, error);
    }

    @Override
    protected Response<QueueResponse> parseNetworkResponse(
            NetworkResponse response) {
        QueueResponse result = new QueueResponse(response.data, parser,
                response.headers, response.rawHeaders, target);
        return Response.success(result, getCacheEntry());
    }

    @Override
    public boolean equals(Object object) {
        if (object != null && object instanceof QueueServiceRequest) {
            QueueServiceRequest request = (QueueServiceRequest) object;
            return request.target == target
                    && request.method == method
                    && request.type == type
                    && request.url.equals(url)
                    && request.content.makeRequestHeaders().equals(
                    content.makeRequestHeaders())
                    && Arrays.equals(request.content.makeRequestBody(),
                    content.makeRequestBody());
        }
        return false;
    }
}
