package core.util;

import com.android.volley.Request;
import com.example.commonframe.R;

import core.base.BaseApplication;

@SuppressWarnings({"UnusedParameters", "PointlessBooleanExpression", "unused", "ConstantConditions"})
public class Constant {

    /* COMMON VARIABLES */
    public static final int[] DEFAULT_ADD_ANIMATION = {R.anim.slide_in_right,
            R.anim.slide_out_left};
    public static final int[] DEFAULT_BACK_ANIMATION = {R.anim.slide_in_left,
            R.anim.slide_out_right};
    public static final String NOTIFICATION_DEFINED = "Notification_Defined";
    public static final String NOTIFICATION_ID = "Notification_Id";
    /* END COMMON VARIABLES */

    /* DEBUG */
    public static final boolean DEBUG = Boolean.valueOf(BaseApplication.getContext().getString(R.string.DEBUG));
    /* END DEBUG */

    /* SYSTEM */
    public static final String BLANK = "";
    public static final String EOF = System.getProperty("line.separator");
    public static final int INTERVAL_CLICK = 500; // 500ms
    public static final int INTERVAL_BACK_PRESS = 300; // 300ms
    public static final boolean MEMORY_CACHE = true;
    public static final boolean DISC_CACHE = true;
    public static final int LRU_CACHE_SIZE = 20 * 1024 * 1024; // 20MB
    public static final int MEMORY_CACHE_SIZE = 20 * 1024 * 1024; // 20MB
    public static final int DISC_CACHE_SIZE = 100 * 1024 * 1024; // 100MB
    public static final int DISC_CACHE_COUNT = 200; // 200 files cached
    public static final int TINT_LEVEL = 0xFFaaaaaa; // 0xFFaaaaaa
    public static final float TINT_COLOR_LEVEL = 0.68f; // 0.68f
    /* END SYSTEM */

    /* NETWORK */
    public static final boolean NETWORK_ERROR_DATA_HANDLE = true;
    public static final String SERVER_URL = "";
    public static final String KEY_STORE_TYPE = "";
    public static final String KEY_STORE_PASSWORD = "";
    public static final int KEY_STORE_ID = 0;
    public static final boolean SSL_ENABLED = !(Utils.isEmpty(KEY_STORE_PASSWORD) && KEY_STORE_ID == 0);
    public static final int TIMEOUT_CONNECT = DEBUG ? 5000 : 10000;
    public static final int RETRY_CONNECT = DEBUG ? 0 : 2;
    /* GCM */
    public static final String SENDER_ID = "";

    public enum RequestType {
        HTTP {
            @Override
            public String toString() {
                return "http://";
            }
        },
        HTTPS {
            @Override
            public String toString() {
                return "https://";
            }
        }
    }

    public enum StatusCode {
        OK, ERR_SSL, ERR_UNKNOWN,
        ERR_PARSING, ERR_AUTH_FAIL,
        ERR_SERVER_FAIL, ERR_NO_CONNECTION,
        ERR_TIME_OUT, ERR_STORE_FILE,
        ERR_QUEUE_IN_REQUEST, ERR_REQUEST_POOL_FULL
    }

    public enum RequestMethod {
        GET, POST, DELETE, HEAD, OPTIONS, PATCH, PUT, TRACE;

        public int getValue() {
            switch (this) {
                case GET:
                    return Request.Method.GET;
                case POST:
                    return Request.Method.POST;
                case PUT:
                    return Request.Method.PUT;
                case DELETE:
                    return Request.Method.DELETE;
                case HEAD:
                    return Request.Method.HEAD;
                case PATCH:
                    return Request.Method.PATCH;
                case TRACE:
                    return Request.Method.TRACE;
                case OPTIONS:
                    return Request.Method.OPTIONS;
                default:
                    return -1;
            }
        }
    }

    /* END NETWORK */

    public enum Header {
        ACCEPT {
            @Override
            public String toString() {
                return "Accept";
            }
        },
        ACCEPT_CHARSET {
            @Override
            public String toString() {
                return "Accept-Charset";
            }
        },
        ACCEPT_ENCODING {
            @Override
            public String toString() {
                return "Accept-Encoding";
            }
        },
        ACCEPT_LANGUAGE {
            @Override
            public String toString() {
                return "Accept-Language";
            }
        },
        AUTHORIZATION {
            @Override
            public String toString() {
                return "Authorization";
            }
        },
        CACHE_CONTROL {
            @Override
            public String toString() {
                return "Cache-Control";
            }
        },
        CONNECTION {
            @Override
            public String toString() {
                return "Connection";
            }
        },
        CONTENT_LENGTH {
            @Override
            public String toString() {
                return "Content-Length";
            }
        },
        CONTENT_TYPE {
            @Override
            public String toString() {
                return "Content-Type";
            }
        },
        USER_AGENT {
            @Override
            public String toString() {
                return "User-Agent";
            }
        }
    }
    /* END GCM*/
}
