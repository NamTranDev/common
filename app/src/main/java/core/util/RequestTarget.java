package core.util;

import android.net.Uri;
import android.util.Pair;

import core.base.BaseParser;
import core.util.Constant.RequestMethod;
import core.util.Constant.RequestType;

public enum RequestTarget {
    WEBSERVICE_REQUEST, BACKGROUND_REQUEST;

    public static BaseParser parser(RequestTarget target) {
        switch (target) {
            case WEBSERVICE_REQUEST:
                // return new WebServiceParser();
                return null;
            case BACKGROUND_REQUEST:
                // return new BackgroundServiceParser();
                return null;
            default:
                return null;
        }
    }

    public static String host(RequestTarget target) {
        switch (target) {
            case WEBSERVICE_REQUEST:
                return Constant.SERVER_URL;
            case BACKGROUND_REQUEST:
                return Constant.SERVER_URL;
            default:
                return Constant.SERVER_URL;
        }
    }

    public static RequestMethod method(RequestTarget target) {
        switch (target) {
            case WEBSERVICE_REQUEST:
                return RequestMethod.POST;
            case BACKGROUND_REQUEST:
                return RequestMethod.POST;
            default:
                return RequestMethod.GET;
        }
    }

    public static RequestType type(RequestTarget target) {
        switch (target) {
            case WEBSERVICE_REQUEST:
                return RequestType.HTTP;
            case BACKGROUND_REQUEST:
                return RequestType.HTTPS;
            default:
                return RequestType.HTTP;
        }
    }

    public static int timeout(RequestTarget target) {
        switch (target) {
            case WEBSERVICE_REQUEST:
                return 5000;
            case BACKGROUND_REQUEST:
                return 5000;
            default:
                return Constant.TIMEOUT_CONNECT;
        }
    }

    public static int retry(RequestTarget target) {
        switch (target) {
            case WEBSERVICE_REQUEST:
                return 1;
            case BACKGROUND_REQUEST:
                return 0;
            default:
                return Constant.RETRY_CONNECT;
        }
    }

    private static String addExtras(String url, Pair<String, String>... extras) {
        if (extras != null && extras.length > 0) {
            for (int i = 0; i < extras.length; ++i) {
                Pair pair = extras[i];
                url += (i == 0) ? "?" : "&" + Uri.encode(String.format("%1$s=%2$s", pair.first, pair.second));
            }
        }
        return url;
    }

    public static String build(RequestTarget target, Pair<String, String>... extras) {
        String url = "";
        switch (target) {
            case WEBSERVICE_REQUEST:
                url = "";
                break;
            default:
                break;
        }
        return addExtras(url, extras);
    }
}
