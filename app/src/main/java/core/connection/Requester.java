package core.connection;

import android.util.Pair;

import core.base.BaseApplication;
import core.base.BaseProperties;
import core.base.Param;
import core.connection.WebServiceRequester.WebServiceResultHandler;
import core.connection.queue.QueueElement;
import core.connection.request.BackgroundServiceRequest;
import core.connection.request.FileRequest;
import core.connection.request.ParallelServiceRequest;
import core.connection.request.QueueServiceRequest;
import core.connection.request.WebServiceRequest;
import core.util.DLog;
import core.util.RequestTarget;

@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "unused"})
public class Requester {

    private static final String TAG = "Requester";

    public static boolean startWSRequest(String tag, RequestTarget target, Param content, WebServiceResultHandler handler, Pair<String, String>... extras) {

        try {
            WebServiceRequest request;
            if (BaseProperties.wsRequester == null)
                BaseProperties.wsRequester = WebServiceRequester
                        .getInstance(BaseApplication.getContext());
            request = new WebServiceRequest(tag, RequestTarget.type(target),
                    RequestTarget.method(target), RequestTarget.host(target), target,
                    RequestTarget.build(target, extras), content,
                    BaseProperties.wsRequester, RequestTarget.parser(target), handler);
            BaseProperties.wsRequester.startRequest(request);
            DLog.d(TAG, request.getRequestMethod().name().toUpperCase()
                    + " >> " + request.getUrl());
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            DLog.d(TAG, "Request canceled!");
            return false;
        }
    }

    public static boolean startBackgroundRequest(String tag, RequestTarget target, Param content, Pair<String, String>... extras) {
        try {
            BackgroundServiceRequest request;
            if (BaseProperties.bgRequester == null)
                BaseProperties.bgRequester = BackgroundServiceRequester
                        .getInstance(BaseApplication.getContext());
            request = new BackgroundServiceRequest(tag, RequestTarget.type(target),
                    RequestTarget.method(target), RequestTarget.host(target), target,
                    RequestTarget.build(target, extras), content,
                    BaseProperties.bgRequester);
            BaseProperties.bgRequester.startRequest(request);
            DLog.d(TAG, request.getRequestMethod().name().toUpperCase()
                    + " >> " + request.getUrl());
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            DLog.d(TAG, "Background request canceled!");
            return false;
        }
    }

    public static boolean startQueueRequest(String tag, RequestTarget target,
                                            QueueElement.Type type, Param content, Pair<String, String>... extras) {
        try {
            QueueServiceRequest request;
            if (BaseProperties.queueRequester == null)
                BaseProperties.queueRequester = QueueServiceRequester
                        .getInstance(BaseApplication.getContext());
            request = new QueueServiceRequest(tag, RequestTarget.type(target),
                    RequestTarget.method(target), RequestTarget.host(target), target,
                    RequestTarget.build(target, extras), content, RequestTarget.parser(target),
                    BaseProperties.queueRequester);
            BaseProperties.queueRequester.addQueueRequest(new QueueElement(
                    request, type));
            DLog.d(TAG, request.getRequestMethod().name().toUpperCase()
                    + " >> " + request.getUrl());
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            DLog.d(TAG, "Queue request canceled!");
            return false;
        }
    }

    public static boolean startParallelRequest(String tag, RequestTarget target,
                                               Param content, Pair<String, String>... extras) {
        try {
            ParallelServiceRequest request;
            if (BaseProperties.parallelRequester == null)
                BaseProperties.parallelRequester = ParallelServiceRequester
                        .getInstance(BaseApplication.getContext());
            request = new ParallelServiceRequest(tag, RequestTarget.type(target),
                    RequestTarget.method(target), RequestTarget.host(target), target,
                    RequestTarget.build(target, extras), content, RequestTarget.parser(target),
                    BaseProperties.parallelRequester);
            ParallelServiceRequester.addRequest(request);
            DLog.d(TAG, request.getRequestMethod().name().toUpperCase()
                    + " >> " + request.getUrl());
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            DLog.d(TAG, "Parallel request canceled!");
            return false;
        }
    }

    public static boolean startFileRequest(String tag, RequestTarget target,
                                           Param content, String path, String name, String extension, Pair<String, String>... extras) {
        try {
            FileRequest request;
            if (BaseProperties.fileRequester == null)
                BaseProperties.fileRequester = FileRequester
                        .getInstance(BaseApplication.getContext());
            request = new FileRequest(tag, RequestTarget.type(target),
                    RequestTarget.method(target), RequestTarget.host(target), target,
                    RequestTarget.build(target, extras), content, BaseProperties.fileRequester, path, name, extension);
            FileRequester.addRequest(request);
            DLog.d(TAG, request.getRequestMethod().name().toUpperCase()
                    + " >> " + request.getUrl() + " >> " + request.getFilePath());
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            DLog.d(TAG, "File request canceled!");
            return false;
        }
    }
}
