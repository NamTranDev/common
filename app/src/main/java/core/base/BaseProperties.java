package core.base;

import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

import core.connection.FileRequester;
import core.connection.ParallelServiceRequester;
import core.connection.QueueServiceRequester;
import core.connection.WebServiceRequester;
import core.dialog.GeneralDialog;
import core.dialog.LoadingDialog;
import core.util.SingleBackPress;
import core.util.SingleTouch;

public abstract class BaseProperties {

    /**
     * Loading dialog reference, this loading dialog will be applied for the
     * entire application
     */
    public static LoadingDialog loadingDialog = null;

    /**
     * Decision dialog reference, this decision dialog will be applied for the
     * entire application
     */
    public static GeneralDialog decisionDialog = null;

    /**
     * Alert dialog reference, this decision dialog will be applied for the
     * entire application
     */
    public static GeneralDialog alertDialog = null;

    /**
     * The web service requester to make the request to server and return the
     * result to the context
     */
    public static WebServiceRequester wsRequester = null;

    /**
     * The queue service requester to make the request to server and return the
     * result to the listeners
     */
    public static QueueServiceRequester queueRequester = null;

    /**
     * The file requester to make the file download requests to server and return the
     * result to the listeners
     */
    public static FileRequester fileRequester = null;

    /**
     * The parallel service requester to make the requests (asynchronously) to server and return the
     * result to the listeners
     */
    public static ParallelServiceRequester parallelRequester = null;

    /**
     * Single touch reference, this single touch will be applied for components
     * to ensure only one component touched at the same time
     */
    private static SingleTouch singleTouch = null;

    /**
     * Single back-press reference, the single back-press ensure only the
     * back-press event only execute once after a short period
     */
    private static SingleBackPress singleBackPress = null;

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isExceptionalView(View view) {
        return ((view instanceof AdapterView) || (view instanceof EditText)
                || (view instanceof SwipeRefreshLayout)
                || (view instanceof DrawerLayout) || (view instanceof ViewPager));
    }

    public static SingleTouch getSingleTouch() {
        if (singleTouch == null)
            singleTouch = new SingleTouch();
        return singleTouch;
    }

    public static SingleBackPress getSingleBackPress() {
        if (singleBackPress == null)
            singleBackPress = new SingleBackPress();
        return singleBackPress;
    }
}
