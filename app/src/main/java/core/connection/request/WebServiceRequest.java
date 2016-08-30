package core.connection.request;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;

import java.util.Map;

import core.base.BaseParser;
import core.base.Param;
import core.connection.WebServiceRequester;
import core.connection.WebServiceRequester.WebServiceResultHandler;
import core.connection.volley.WebServiceError;
import core.connection.volley.WebServiceResponse;
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
 *          - Represents a class for forming a webservice request extends from
 *          Request<?> of Volley framework. This class will handle the type,
 *          method, url, time out, retry, headers, parameters and the return
 *          format of each request <br>
 *          - Every request will be marked with a tag which can be canceled if
 *          it is no longer needed <br>
 *          - The content of the result will be delivered to the
 *          WebServiceHandler as a WebServiceResponse including the data and
 *          headers
 * @since April 2014
 */

@SuppressWarnings("SameParameterValue")
public class WebServiceRequest extends Request<WebServiceResponse> {

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
     * The target for handling the result from webservice, the actual end result
     * will be delivered to this object (either success, unsuccessful or
     * failure)
     */
    private final WebServiceResultHandler handler;

    /**
     * The success result handler to integrate with Volley framework
     */
    private final Listener<WebServiceResponse> success;

    public WebServiceRequest(String tag, RequestType type,
                             RequestMethod method, String address, RequestTarget target,
                             String api, Param content, WebServiceRequester requester,
                             BaseParser parser, WebServiceResultHandler handler) {
        super(method.getValue(), type.toString() + address + api, requester);
        this.url = type.toString() + address + api;
        this.success = requester;
        this.method = method;
        this.handler = handler;
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
        return Priority.IMMEDIATE;
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
    public RequestType getRequestType() {
        return type;
    }

    /**
     * @return the handler
     */
    public WebServiceResultHandler getWebServiceResultHandler() {
        return handler;
    }

    /**
     * @return the request method
     */
    public RequestMethod getRequestMethod() {
        return method;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    @Override
    protected void deliverResponse(WebServiceResponse response) {
        success.onResponse(response);
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError error) {
        return new WebServiceError(target, parser, error);
    }

    @Override
    protected Response<WebServiceResponse> parseNetworkResponse(
            NetworkResponse response) {
        return Response.success(new WebServiceResponse(response.data, parser,
                response.headers, response.rawHeaders), getCacheEntry());
    }
}
