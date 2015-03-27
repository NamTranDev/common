package com.example.commonframe.util;

import com.android.volley.Request;
import com.example.commonframe.R;

public class Constant {

	/* REQUEST SECTION */

	public enum RequestFormat {
		XML, JSON, STRING, DEFAULT,
	}

	public enum ReturnFormat {
		XML, JSON, STRING, FILE
	}

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
		OK {
			@Override
			public String toString() {
				return "1";
			}
		},
		ERR_SSL, ERR_UNKNOWN, ERR_PARSING, ERR_AUTH_FAIL, ERR_SERVER_FAIL, ERR_NO_CONNECTION, ERR_TIME_OUT
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

	public enum BackgroundRequestTarget {
		
		FILE {
			public String toString() {
				return "";
			}
		},
		DOCUMENT {
			@Override
			public String toString() {
				return "document.php";
			}
		},
		NOTIFICATION {
			@Override
			public String toString() {
				return "notification.php";
			}
		}
	}

	public enum RequestTarget {
		LOGIN {
			@Override
			public String toString() {
				return "login.php";
			}
		},
		REGISTER {
			@Override
			public String toString() {
				return "register.php";
			}
		},
		CATEGORY {
			@Override
			public String toString() {
				return "category.php";
			}
		},
		LOGOUT {
			@Override
			public String toString() {
				return "logout.php";
			}
		},
		FORGOT {
			@Override
			public String toString() {
				return "forgot.php";
			}
		},
		CURRENT {
			@Override
			public String toString() {
				return "current.php";
			}
		},
		VERSION {
			@Override
			public String toString() {
				return "version.php";
			}
		},
	}

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

	/* END REQUEST SECTION */

	/* DECLARE VARIABLES SECTION */

	/* COMMON VARIABLES*/
	public static final String NOTIFICATION_DEFINED = "Notification_Defined";
	
	/* END COMMON VARIABLES*/
	
	/* DEBUG */
	public static final boolean DEBUG = true;
	/* END DEBUG */
	
	/* SYSTEM */
	public static final String BLANK = "";
	public static final String EOF = System.getProperty("line.separator");
	public static final int INTERVAL_CLICK = 300; // 500ms
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
	public static final String SERVER_URL = "192.168.15.35/";
	public static final String KEY_STORE_PASSWORD = "ez24get";
	public static final int KEY_STORE_ID = R.raw.mystore;
	public static final int TIMEOUT_BACKGROUND_CONNECT = DEBUG ? 500 : 20000;
	public static final int TIMEOUT_CONNECT = DEBUG ? 500 : 10000;
	public static final int RETRY_CONNECT = DEBUG ? 0 : 2;
	public static final int RETRY_BACKGROUND_CONNECT = DEBUG ? 1 : 3;
	/* END NETWORK */
	
	/* END DECLARE VARIABLES SECTION */

}
