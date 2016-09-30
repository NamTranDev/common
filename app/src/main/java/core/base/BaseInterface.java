package core.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.AnimRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.util.Pair;
import android.view.View;

import core.connection.WebServiceRequester.WebServiceResultHandler;
import core.connection.queue.QueueElement.Type;
import core.dialog.GeneralDialog.ConfirmListener;
import core.dialog.GeneralDialog.DecisionListener;
import core.util.RequestTarget;
import core.util.SingleClick;
import core.util.SingleTouch;


@SuppressWarnings("unused")
public interface BaseInterface {

    /**
     * This method is for initializing objects used in the activity. This method
     * is called immediately after the <code>onCreate()</code> method of the
     * activity and only called once when the activity is created. Any global
     * objects that used inside the activity should be initialized here for the
     * purpose of management.
     */
    void onBaseCreate();

    /**
     * This method is for handling the actions when user enter the application
     * from a deep link. This method is called immediately after
     * <code>onResume()</code> method and only called once when first action
     * received, afterward, the DeepLinking data will be removed. Any actions
     * and data from deep link sent to this activity must be handle in this
     * method. <br>
     *
     * @param data The intent data received from DeepLinking action
     */
    void onDeepLinking(Intent data);

    /**
     * This method is for handling the actions when user enter the application
     * from a notification. This method is called immediately after
     * <code>onResume()</code> method and only called once when when first
     * action received, afterward, the Notification data will be removed. Any
     * actions and data from notification sent to this activity must be handle
     * in this method. <br>
     *
     * @param data The intent data received from Notification action
     */
    void onNotification(Intent data);

    /**
     * This method is for attaching views to the object references used in the
     * activity. This method is called immediately after the
     * <code>setContentView()</code> method of the activity and only called once
     * when the activity is created. Any views defined in xml file should be
     * attached to object references in the activity here.
     */
    void onBindView();

    /**
     * This method is for initialization of data into views after binding from
     * layout. This method is called immediately after the
     * <code>onBindView()</code> method of the activity and only called once
     * when the activity is created. Any views had been binding in onBindView
     * file can set the data here.
     */
    void onInitializeViewData();

    /**
     * This method is for re-initiating objects after return from another
     * activity. This method is called immediately after the
     * <code>onResume()</code> method of the activity and called every time the
     * activity become visible on the screen (back from another activity or
     * return to the application from another application). Any object that
     * needs to be re-assigned should be here such as connection, observer,
     * listener.
     */
    void onBaseResume();

    /**
     * This method is for releasing objects after the activity is finished. This
     * method is called immediately after the <code>onStop()</code> method of
     * the activity and maybe called or not depending on the memory situation.
     * Any object that used in this activity should be released here.
     */
    void onBaseFree();

    /**
     * This method is to register views with single actions (single click and single touch).
     * Click actions will be handled in <code>onSingleClick</code> method. Some exceptional view will
     * not be registered such as EditText, ListView, etc. to avoid intercepting touch actions. The
     * customized exceptional views filtering are implemented in <code>isExceptionalView</code>
     *
     * @param views The view array to register with single actions
     */
    void registerSingleAction(View... views);

    /**
     * This method is to register views with single actions (single click and single touch) by the ids.
     * Click actions will be handled in <code>onSingleClick</code> method. Some exceptional view will
     * not be registered such as EditText, ListView, etc. to avoid intercepting touch actions. The
     * customized exceptional views filtering are implemented in <code>isExceptionalView</code>
     *
     * @param ids The view id array to register with single actions
     */
    void registerSingleAction(@IdRes int... ids);

    /**
     * This method is to show a decision dialog with defined values, only once
     * instance of this dialog will be allowed at a time. If there are more than
     * one, the previous will be dismissed. This dialog also auto close whenever
     * a decision made.
     *
     * @param context  The context which the dialog show on
     * @param layout   The layout xml to show
     * @param icon     The icon id resource for the dialog if present, -1 if not
     * @param title    The title of this dialog if present, null if not
     * @param message  The message of this dialog if present, null if not
     * @param yes      The text for accepting the decision if present, null if not
     * @param no       The text for rejecting the decision if present, null if not
     * @param cancel   The text for dismissing this dialog without making the
     *                 decision if present, null if not
     * @param listener The listener to handle the action from this dialog
     */
    void showDecisionDialog(Context context, int id, @LayoutRes int layout, @DrawableRes int icon, String title,
                            String message, String yes, String no, String cancel,
                            Object onWhat, DecisionListener listener);

    /**
     * This method is to show an alert dialog with defined values, only once
     * instance of this dialog will be allowed at a time. If there are more than
     * one, the previous will be dismissed. This dialog also auto close whenever
     * user clicks OK button.
     *
     * @param context  The context which the dialog show on
     * @param id       The id of this dialog in case many dialogs are shown
     * @param layout   The layout xml to show
     * @param icon     The icon id resource for this dialog if present, -1 if not
     * @param title    The title of this dialog if present, null if not
     * @param message  The message of this dialog if present, null if not
     * @param confirm  The confirm text of this dialog if present, null if not
     * @param listener The listener to listen the confirmed action, the dialog will
     *                 dismiss and the action will be fired immediately after confirm
     */
    void showAlertDialog(Context context, int id, @LayoutRes int layout, @DrawableRes int icon, String title,
                         String message, String confirm, Object onWhat, ConfirmListener listener);

    /**
     * This method is to show a loading dialog and stops user from interacting
     * with other views. Only once instance of this dialog will be allowed at a
     * time. If there are more than one, the previous will be dismissed. This
     * dialog also auto close whenever the application move out of the screen
     * (back to home).
     *
     * @param context The context which the dialog show on
     * @param layout  The layout xml to show
     * @param loading The loading text to show
     */
    void showLoadingDialog(Context context, @LayoutRes int layout, String loading);

    /**
     * This method is to dismiss the loading dialog if present when finish the
     * task and allow user to interact with other views.
     */
    void closeLoadingDialog();

    /**
     * This method is to get the customized loading dialog layout xml
     */
    @LayoutRes
    int getLoadingDialogLayoutResource();

    /**
     * This method is to get the customized alert dialog and decision dialog layout xml
     */
    @LayoutRes
    int getGeneralDialogLayoutResource();

    /**
     * This method is to find a string in the resource with a defined id.
     *
     * @param id The id of the string in resource
     * @return String from resource, null if the id not found
     */
    String getResourceString(int id);

    /**
     * This method is to retrieve the application context as general.
     *
     * @return The application context in BaseApplication
     */
    Context getBaseContext();

    /**
     * This method is to retrieve the active activity.
     *
     * @return The active activity
     */
    Activity getActiveActivity();

    /**
     * This method is to cancel all webservice requests with a specific tag if
     * any
     *
     * @param tag The web service requests will be canceled
     */
    void cancelWebServiceRequest(String tag);

    /**
     * This method is to cancel all background requests with a specific tag if
     * any
     *
     * @param tag The background requests will be canceled
     */
    void cancelBackgroundRequest(String tag);

    /**
     * This method is to retrieve the animation enter the screen of an activity or a fragment.
     *
     * @return The animation resource
     */
    @AnimRes
    int getEnterInAnimation();

    /**
     * This method is to retrieve the animation back to the screen of an activity or a fragment.
     *
     * @return The animation resource
     */
    @AnimRes
    int getBackInAnimation();

    /**
     * This method is to retrieve the animation move out of an activity or a fragment when another
     * activity or fragment enter the screen.
     *
     * @return The animation resource
     */
    @AnimRes
    int getEnterOutAnimation();

    /**
     * This method is to retrieve the animation move out of an activity.
     *
     * @return The animation resource
     */
    @AnimRes
    int getBackOutAnimation();

    /**
     * This method is for making a connection to server base on the target and
     * parameters defined in request. <br>
     * Every target must be defined in enum RequestTarget in Constant and
     * implement functions for returning type, method, host, parser and build. <br>
     * Every request content must be derived from Param and implements
     * <code>makeRequestParams()</code> for the parameters and
     * <code>makeRequestHeaders()</code> for the headers of the web-service.
     * This method can be used at any class implements BaseInterface. <br>
     * To receive the result from the request, the activity must implement
     * WebServiceResultHandler and re-assigned the requester with the activity
     * as the WebServiceResultHandler.
     *
     * @param tag     The activity starts this request
     * @param loading The flag indicate showing loading dialog
     * @param content The content of the request including parameters and headers
     * @param handler The handler for the result returned from the request
     * @param target  The function requested to the server
     * @param extras  The extra parameters to build api
     */
    void makeRequest(String tag, boolean loading, Param content,
                     WebServiceResultHandler handler, RequestTarget target,
                     Pair<String, String>... extras);

    /**
     * This method is for making a connection to server base on the target and
     * parameters defined in request. The requests are queued up if there is no
     * internet connection for the moment and can be resumed by calling
     * <code>QueueServiceRequester.startQueueRequest()</code></><br>
     * Every target must be defined in enum RequestTarget in Constant and
     * implement functions for returning type, method, host, parser and build. <br>
     * Every request content must be derived from Param and implements
     * <code>makeRequestParams()</code> for the parameters and
     * <code>makeRequestHeaders()</code> for the headers of the web-service.
     * This method can be used at any class implements BaseInterface. <br>
     * To receive the result from the request, the activity must be registered to
     * the QueueServiceRequest listeners' pool by <code>QueueServiceRequester.registerListener()</code>
     * and implement the <code>QueueServiceListener</code> interface.
     *
     * @param tag     The activity starts this request
     * @param type    The queue type (PASS, BLOCK, RETRY, STOP) for this request
     * @param content The content of the request including parameters and headers
     * @param target  The function requested to the server
     * @param extras  The extra parameters to build api
     */
    void makeQueueRequest(String tag, Type type, Param content,
                          RequestTarget target, Pair<String, String>... extras);

    /**
     * This method is for making multiple connections to server base on the target and
     * parameters defined in request. The requests are added up to a predefined number at
     * <code>ParallelServiceRequester.addRequest()</code></><br>
     * Every target must be defined in enum RequestTarget in Constant and
     * implement functions for returning type, method, host, parser and build. <br>
     * Every request content must be derived from Param and implements
     * <code>makeRequestParams()</code> for the parameters and
     * <code>makeRequestHeaders()</code> for the headers of the web-service.
     * This method can be used at any class implements BaseInterface. <br>
     * To receive the result from the request, the activity must be registered to
     * the ParallelServiceRequester listeners' pool by <code>ParallelServiceRequester.registerListener()</code>
     * and implement the <code>ParallelServiceListener</code> interface.
     *
     * @param tag     The activity starts this request
     * @param content The content of the request including parameters and headers
     * @param target  The function requested to the server
     * @param extras  The extra parameters to build api
     */
    void makeParallelRequest(String tag, Param content,
                             RequestTarget target, Pair<String, String>... extras);

    /**
     * This method is for downloading a file from server base on the
     * target and parameters defined in request. <br>
     * Every target must be defined in enum RequestTarget in Constant and
     * implement functions for returning type, method, host, parser and build. <br>
     * Every request content must be derived from Param and implements
     * <code>makeRequestParams()</code> for the parameters and
     * <code>makeRequestHeaders()</code> for the headers of the web-service.
     * This method can be used at any class implements BaseInterface. <br>
     * This method will return the result in background and it has the lowest
     * priority <br>
     * To handle the result of these request, implement the
     * <code>onResponse</code> and <code>onErrorResponse</code> in
     * <code>FileRequester</code>.
     *
     * @param tag       The string indicate the id of this request
     * @param path      The path where file will be stored
     * @param name      The file name
     * @param extension The file extension
     * @param target    The function requested to the server
     * @param content   The content of the request including parameters and headers
     * @param extras    The extra parameters to build api
     */
    void makeFileRequest(String tag, String path, String name, String extension, RequestTarget target,
                         Param content, Pair<String, String>... extras);

    /**
     * This method is to return the single instance of SingleTouch applying for
     * entire application.
     *
     * @return The SingleTouch instance
     */
    SingleTouch getSingleTouch();

    /**
     * This method is to return the single instance of SingleClick applying for
     * entire application.
     *
     * @return The SingleClick instance for each screen or fragment
     */
    SingleClick getSingleClick();

    /**
     * This method is to validate view type for setting on single touch and click listener. Any views
     * listed here are exceptional and will not be registered to single touch and click
     *
     * @return true if view should not be registered (ListView, EditText, etc.). Otherwise false
     */
    boolean isExceptionalView(View view);
}
