package core.base;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.example.commonframe.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import core.connection.Requester;
import core.connection.WebServiceRequester;
import core.connection.WebServiceRequester.WebServiceResultHandler;
import core.connection.queue.QueueElement.Type;
import core.dialog.GeneralDialog;
import core.dialog.GeneralDialog.ConfirmListener;
import core.dialog.GeneralDialog.DecisionListener;
import core.dialog.LoadingDialog;
import core.util.ActionTracker;
import core.util.Constant;
import core.util.DLog;
import core.util.RequestTarget;
import core.util.SingleClick;
import core.util.SingleClick.SingleClickListener;
import core.util.SingleTouch;
import core.util.Utils;
import icepick.Icepick;
import icepick.State;

/**
 * @author Tyrael
 * @version 1.0 <br>
 *          <br>
 *          <b>Class Overview</b> <br>
 *          <br>
 *          Represents a class for essential fragment activity to be a super
 *          class of activities in project. It includes supportive method of
 *          showing, closing dialogs, making and canceling request. Those
 *          methods can be used in any derived class. <br>
 *          This class also supports multiple fragment containers for both
 *          tablet and phone with methods of add, remove, replace, back and
 *          clear fragments on a specific container. <br>
 *          The derived classes must implement <code>onBaseCreate()</code>,
 *          <code>onBindView()</code>, <code>onBaseResume()</code>,
 *          <code>onBaseFree()</code> for the purpose of management.
 * @since May 2015
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BaseMultipleFragmentActivity extends AppCompatActivity
        implements BaseInterface, SingleClickListener {

    /**
     * Tag of BaseFragmentActivity class for Log usage
     */
    private static String TAG = BaseMultipleFragmentActivity.class.getSimpleName();
    @State
    HashMap<Integer, ArrayList<String>> containers = new HashMap<>();
    /**
     * The flag indicating that the fragments are first initialized after the
     * activity created, this variable is only invoked once.
     */
    @State
    boolean isFragmentsInitialized = false;
    /**
     * The identification of the main fragment container, the default is the
     * first container added. Or it can be set by
     * <code>setMainContainerId()</code>. The id is used for default
     * <code>onBackPress()</code>, <code>onDeepLinking()</code>,
     * <code>onNotification()</code>, <code>onActivityResult()</code>
     */
    @State
    int mainContainerId = -1;
    /**
     * The flag indicating that the activity is finished and should free all of
     * resources at <code>onStop()</code> method
     */
    @State
    boolean isFinished = false;
    /**
     * The flag indicating that back stack will revert to the existing fragment. The flag can be
     * overriden by implement method <code>shouldBackIfFragmentExist</code>
     */
    @State
    boolean shouldBackIfFragmentExist = false;
    /**
     * The single click to handle click action for this screen
     */
    private SingleClick singleClick = null;
    /**
     * The unbinder of Butterknife to unbind views when the fragment view is destroyed
     */
    private Unbinder unbinder;

    /*
     * ABSTRACT METHODS
     */

    /**
     * This method is for initializing fragments used in the activity. This
     * method is called immediately after the <code>onResumeFragments()</code>
     * method of the activity and only called once when the activity is created,
     * it depends on the <code>isFragmentsInitialized</code>. Any first
     * fragments that used inside the activity should be initialized here for
     * the purpose of management.
     */
    protected abstract void onInitializeFragments();

    /**
     * This method is for handling the back stack event of the last fragment of
     * one container.
     *
     * @param containerId The container id of the last fragment when back stack event
     *                    called
     */
    protected abstract void onLastFragmentBack(@IdRes int containerId);

    /**
     * This method is for notifying when a new fragment or fragments are added to a container
     *
     * @param containerId The container id of the added fragment
     * @param tags        The added fragments tag
     */
    protected abstract void onFragmentAdded(@IdRes int containerId, String... tags);

    /**
     * This method is for notifying when a fragment or fragments are removed from a container
     *
     * @param containerId The container id of the removed fragment
     * @param tags        The removed fragments tag
     */
    protected abstract void onFragmentRemoved(@IdRes int containerId, String... tags);

    /*
     * ANDROID LIFECYCLE
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // This is to prevent multiple instances on release build (bug from
        // Android)
        if (!isTaskRoot()) {
            Intent intent = getIntent();
            String action = intent.getAction();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && action != null
                    && action.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        } else
            ActionTracker.openActionLog();
        TAG = getClass().getName();
        int enterAnim = getEnterInAnimation() == -1 ? Constant.DEFAULT_ADD_ANIMATION[0] : getEnterInAnimation();
        int exitAnim = getEnterOutAnimation() == -1 ? Constant.DEFAULT_ADD_ANIMATION[0] : getEnterOutAnimation();
        overridePendingTransition(enterAnim,
                exitAnim);
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
        onBaseCreate();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        unbinder = ButterKnife.bind(this);
        onBindView();
        onInitializeViewData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    protected void onResume() {
        TAG = getClass().getName();
        BaseProperties.wsRequester = WebServiceRequester
                .getInstance(BaseApplication.getContext());
        BaseApplication.setActiveActivity(this);
        // EventBus.getDefault().register(this);
        ActionTracker.enterScreen(getClass().getSimpleName(), ActionTracker.Screen.ACTIVITY);
        onBaseResume();
        super.onResume();
        onOutsideActionReceived();
        Utils.logHeap(TAG);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        // this code is to prevent using commitAllowStateLoss
        if (!isFragmentsInitialized) {
            isFragmentsInitialized = true;
            onInitializeFragments();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        BaseMultipleFragment fragment = getTopFragment(mainContainerId);
        if (fragment != null)
            fragment.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (BaseProperties.getSingleBackPress().onBackPressAllowed()) {
            // super.onBackPressed();
            backStack(mainContainerId, null);
        }
    }

    @Override
    public final boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Utils.closeSoftKeyboard(this, findViewById(android.R.id.content)
                    .getRootView());
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public final boolean onTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Utils.closeSoftKeyboard(this, findViewById(android.R.id.content)
                    .getRootView());
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public final void finish() {
        isFinished = true;
        super.finish();
        if (isTaskRoot())
            ActionTracker.closeActionLog();
        int enterAnim = getBackInAnimation() == -1 ? Constant.DEFAULT_BACK_ANIMATION[0] : getBackInAnimation();
        int exitAnim = getBackOutAnimation() == -1 ? Constant.DEFAULT_BACK_ANIMATION[1] : getBackOutAnimation();
        overridePendingTransition(enterAnim, exitAnim);
    }

    @Override
    protected void onPause() {
        // EventBus.getDefault().unregister(this);
        cancelWebServiceRequest(null);
        closeLoadingDialog();
        super.onPause();
        if (isFinished) {
            clearAllStacks();
        }
    }

    @Override
    protected void onStop() {
        if (isFinished) {
            ActionTracker.exitScreen(getClass().getSimpleName());
            onBaseFree();
            Utils.nullViewDrawablesRecursive(findViewById(android.R.id.content)
                    .getRootView());
            Utils.unbindDrawables(findViewById(android.R.id.content)
                    .getRootView());
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null)
            unbinder.unbind();
    }

    /*
     * BASE INTERFACE
     */

    @Override
    public void onBindView() {
        /* Views are bind by Butterknife, override this for more actions on binding views */
    }

    @Override
    public String getResourceString(int id) {
        try {
            return getResources().getString(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public final void registerSingleAction(View... views) {
        for (View view : views)
            if (view != null && !isExceptionalView(view)) {
                view.setOnClickListener(getSingleClick());
                view.setOnTouchListener(getSingleTouch());
            }
    }

    @Override
    public final void registerSingleAction(@IdRes int... ids) {
        for (int id : ids) {
            View view = findViewById(id);
            if (view != null && !isExceptionalView(view)) {
                view.setOnClickListener(getSingleClick());
                view.setOnTouchListener(getSingleTouch());
            }
        }
    }

    @Override
    public final Activity getActiveActivity() {
        return BaseApplication.getActiveActivity();
    }

    @Override
    public final Context getBaseContext() {
        return BaseApplication.getContext();
    }

    @Override
    public final void showDecisionDialog(Context context, int id, @LayoutRes int layout, @DrawableRes int icon,
                                         String title, String message, String yes, String no, String cancel,
                                         Object onWhat, DecisionListener listener) {
        if (BaseProperties.decisionDialog != null)
            BaseProperties.decisionDialog.dismiss();
        BaseProperties.decisionDialog = null;
        if (BaseProperties.decisionDialog == null)
            BaseProperties.decisionDialog = new GeneralDialog(context, id,
                    layout, icon, title, message, yes, no, cancel, listener, onWhat);

        if (BaseProperties.decisionDialog != null)
            BaseProperties.decisionDialog.show();
    }

    @Override
    public final void showAlertDialog(Context context, int id, @LayoutRes int layout, @DrawableRes int icon,
                                      String title, String message, String confirm,
                                      Object onWhat, ConfirmListener listener) {
        if (BaseProperties.alertDialog != null)
            BaseProperties.alertDialog.dismiss();
        BaseProperties.alertDialog = null;
        if (BaseProperties.alertDialog == null)
            BaseProperties.alertDialog = new GeneralDialog(context, id, layout, icon,
                    title, message, confirm, listener, onWhat);

        if (BaseProperties.alertDialog != null)
            BaseProperties.alertDialog.show();
    }

    @Override
    public final void showLoadingDialog(Context context, @LayoutRes int layout, String loading) {
        if (BaseProperties.loadingDialog != null)
            BaseProperties.loadingDialog.dismiss();
        BaseProperties.loadingDialog = null;
        if (BaseProperties.loadingDialog == null)
            BaseProperties.loadingDialog = new LoadingDialog(context, layout, loading);

        if (BaseProperties.loadingDialog != null)
            BaseProperties.loadingDialog.show();
    }

    @Override
    public final void closeLoadingDialog() {
        if (BaseProperties.loadingDialog != null)
            if (BaseProperties.loadingDialog.isShowing())
                BaseProperties.loadingDialog.dismiss();
    }

    @Override
    public boolean isExceptionalView(View view) {
        return BaseProperties.isExceptionalView(view);
    }

    @Override
    public final void makeFileRequest(String tag, String path, String name, String extension,
                                      RequestTarget target, Param content, Pair<String, String>... extras) {
        if (!Requester.startFileRequest(tag, target, content, path, name, extension, extras))
            DLog.d(TAG, "makeFileRequest failed with " + tag);
    }

    @Override
    public final void makeRequest(String tag, boolean loading, Param content,
                                  WebServiceResultHandler handler, RequestTarget target,
                                  Pair<String, String>... extras) {
        if (!Utils.isNetworkConnectionAvailable()) {
            closeLoadingDialog();
            showAlertDialog(
                    this,
                    -1, getGeneralDialogLayoutResource(),
                    -1,
                    getResourceString(R.string.error_internet_unavailable_title),
                    getResourceString(R.string.error_internet_unavailable_message), getResourceString(android.R.string.ok), null, null);
            return;
        }
        if (loading)
            showLoadingDialog(this, getLoadingDialogLayoutResource(), getString(R.string.loading_dialog_tv));
        if (!Requester.startWSRequest(tag, target, content, handler, extras)) {
            DLog.d(TAG, "makeRequest failed with " + tag);
            closeLoadingDialog();
        }
    }

    @Override
    public final void makeQueueRequest(String tag, Type type, Param content,
                                       RequestTarget target, Pair<String, String>... extras) {
        if (!Requester.startQueueRequest(tag, target, type, content, extras))
            DLog.d(TAG, "makeQueueRequest failed with " + tag);
    }

    @Override
    public final void makeParallelRequest(String tag, Param content, RequestTarget target, Pair<String, String>... extras) {
        if (!Requester.startParallelRequest(tag, target, content, extras))
            DLog.d(TAG, "makeParallelRequest failed with " + tag);
    }

    @Override
    public final void cancelWebServiceRequest(String tag) {
        if (BaseProperties.wsRequester != null) {
            BaseProperties.wsRequester.cancelAll(tag);
        } else {
            BaseProperties.wsRequester.cancelAll(null);
        }
    }

    @Override
    public final SingleTouch getSingleTouch() {
        return BaseProperties.getSingleTouch();
    }

    @Override
    public final SingleClick getSingleClick() {
        if (singleClick == null) {
            singleClick = new SingleClick();
            singleClick.setListener(this);
        }
        return singleClick;
    }

    @LayoutRes
    @Override
    public int getLoadingDialogLayoutResource() {
        return R.layout.loading_dialog;
    }

    @LayoutRes
    @Override
    public int getGeneralDialogLayoutResource() {
        return R.layout.general_dialog;
    }

    @AnimRes
    @Override
    public int getEnterInAnimation() {
        return -1;
    }

    @AnimRes
    @Override
    public int getEnterOutAnimation() {
        return -1;
    }

    @AnimRes
    @Override
    public int getBackInAnimation() {
        return -1;
    }

    @AnimRes
    @Override
    public int getBackOutAnimation() {
        return -1;
    }

    /*
     * BASE MULTIPLE FRAGMENT
     */

    @IdRes
    public final int getMainContainerId() {
        return this.mainContainerId;
    }

    public final void setMainContainerId(@IdRes int mainContainerId) {
        this.mainContainerId = mainContainerId;
    }

    public final void backStack(@IdRes int containerId, String toTag) {
        if (getSupportFragmentManager() != null) {
            ArrayList<String> tags = containers.get(containerId);
            if (tags != null) {
                if (tags.size() <= 1 && Utils.isEmpty(toTag)) {
                    onLastFragmentBack(containerId);
                } else {
                    FragmentTransaction transaction = getSupportFragmentManager()
                            .beginTransaction();
                    BaseMultipleFragment top = getTopFragment(containerId);
                    boolean areFragmentsRemoved = false;
                    ArrayList<String> removedTags = new ArrayList<>();
                    for (int i = tags.size() - 1; i > 0; --i) {
                        BaseMultipleFragment entry = (BaseMultipleFragment) getSupportFragmentManager().findFragmentByTag(tags.get(i));
                        if (entry != null) {
                            if (Utils.isEmpty(toTag)) {
                                areFragmentsRemoved = true;
                                removeAllChildFragments(entry.getView(), transaction);
                                entry.onBasePause();
                                tags.remove(i);
                                transaction.remove(entry);
                                removedTags.add(entry.getTag());
                                ActionTracker.exitScreen(entry.getTag());
                                break;
                            } else {
                                if (toTag.equals(entry.getTag()))
                                    break;
                                areFragmentsRemoved = true;
                                removeAllChildFragments(entry.getView(), transaction);
                                entry.onBasePause();
                                tags.remove(i);
                                transaction.remove(entry);
                                removedTags.add(entry.getTag());
                                ActionTracker.exitScreen(entry.getTag());
                            }
                        }
                    }
                    String[] removingTagsArray = new String[removedTags.size()];
                    removedTags.toArray(removingTagsArray);
                    if (areFragmentsRemoved && top != null && top.getView() != null)
                        animateBackOut(top.getView(), top.getBackOutAnimation(), transaction, containerId, removingTagsArray);
                    else {
                        transaction.commitNow();
                        onFragmentRemoved(containerId, removingTagsArray);
                    }
                    BaseMultipleFragment fragment = getTopFragment(containerId);
                    if (fragment != null) {
                        if (fragment.getView() != null) {
                            View view = fragment.getView();
                            animateBackIn(view, fragment.getBackInAnimation());
                        }
                        fragment.onResume();
                    }
                }
            }
        }
    }

    protected final void popAllBackStack(@IdRes int containerId) {
        if (getSupportFragmentManager() != null) {
            try {
                ArrayList<String> tags = containers
                        .get(containerId);
                if (tags != null) {
                    BaseMultipleFragment last = getTopFragment(containerId);
                    animateAddOut(last);
                    FragmentTransaction transaction = getSupportFragmentManager()
                            .beginTransaction();

                    ArrayList<BaseMultipleFragment> fragments = new ArrayList<>();

                    for (String tag : tags)
                        fragments.add((BaseMultipleFragment) getSupportFragmentManager().findFragmentByTag(tag));

                    for (BaseMultipleFragment fragment : fragments) {
                        removeAllChildFragments(fragment.getView(), transaction);
                        transaction.remove(fragment);
                    }
                    if (transaction != null) {
                        transaction.commitNow();
                        String[] removingTagsArray = new String[tags.size()];
                        tags.toArray(removingTagsArray);
                        onFragmentRemoved(containerId, removingTagsArray);
                    }
                    clearStack(containerId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public final BaseMultipleFragment getFragment(@IdRes int containerId, String uniqueTag) {
        ArrayList<String> tags = containers.get(containerId);
        if (tags != null && tags.size() > 0) {
            for (String tag : tags)
                if (tag.equals(uniqueTag))
                    return (BaseMultipleFragment) getSupportFragmentManager().findFragmentByTag(uniqueTag);
        }
        return null;
    }

    public final BaseMultipleFragment getTopFragment(@IdRes int containerId) {
        try {
            ArrayList<String> tags = containers.get(containerId);
            int size;
            if (tags != null && (size = tags.size()) > 0)
                return (BaseMultipleFragment) getSupportFragmentManager().findFragmentByTag(tags.get(size - 1));
        } catch (Exception e) {
            // ignore this exception
        }
        return null;
    }

    protected boolean shouldBackIfFragmentExist() {
        return shouldBackIfFragmentExist;
    }

    protected final void addMultipleFragments(@IdRes int containerId, BaseMultipleFragment... fragments) {
        if (getSupportFragmentManager() != null) {
            ArrayList<String> tags = containers.get(containerId);
            ArrayList<String> addingTags = new ArrayList<>();
            ArrayList<BaseMultipleFragment> addingFragments = new ArrayList<>();
            removeDuplicateFragments(fragments, addingTags, addingFragments);
            if (tags == null || tags.size() <= 0) {
                if (mainContainerId == -1)
                    mainContainerId = containerId;
                containers.put(containerId, tags = new ArrayList<>());
                tags.addAll(addingTags);
                FragmentTransaction transaction = getSupportFragmentManager()
                        .beginTransaction();
                for (BaseMultipleFragment fragment : addingFragments) {
                    int anim = fragment.getEnterInAnimation();
                    if (anim == -1) {
                        anim = Constant.DEFAULT_ADD_ANIMATION[0];
                    }
                    transaction
                            .setCustomAnimations(
                                    anim, 0, 0, 0) // add in animation
                            .add(containerId, fragment, fragment.getUniqueTag());
                }
                transaction.commitNow();
                if (addingTags.size() > 0) {
                    String[] addingTagsArray = new String[addingTags.size()];
                    addingTags.toArray(addingTagsArray);
                    onFragmentAdded(containerId, addingTagsArray);
                }
            } else {
                addingFragments = removeDuplicateFragmentsWithExisting(tags, addingTags, addingFragments);
                BaseMultipleFragment top = getTopFragment(containerId);
                if (top != null)
                    top.onPause();
                FragmentTransaction transaction = getSupportFragmentManager()
                        .beginTransaction();
                for (BaseMultipleFragment fragment : addingFragments) {
                    int anim = fragment.getEnterInAnimation();
                    if (anim == -1) {
                        anim = Constant.DEFAULT_ADD_ANIMATION[0];
                    }
                    transaction
                            .setCustomAnimations(
                                    anim, 0, 0, 0) // add in animation
                            .add(containerId, fragment, fragment.getUniqueTag());
                }
                transaction.commitNow();
                if (addingTags.size() > 0) {
                    String[] addingTagsArray = new String[addingTags.size()];
                    addingTags.toArray(addingTagsArray);
                    onFragmentAdded(containerId, addingTagsArray);
                }
            }
        }
    }

    protected final void addFragment(@IdRes int containerId, BaseMultipleFragment fragment) {
        addFragment(containerId, fragment, fragment.getUniqueTag());
    }

    protected final void replaceFragment(@IdRes int containerId,
                                         BaseMultipleFragment fragment, boolean clearStack) {
        replaceFragment(containerId, fragment, fragment.getUniqueTag(), clearStack);
    }

    protected final void removeFragment(@IdRes int containerId, String tag) {
        ArrayList<String> tags = containers.get(containerId);
        if (tags != null) {
            BaseMultipleFragment removed = getTopFragment(containerId);
            if (removed != null && removed.getTag().equals(tag)) {
                backStack(containerId, null);
            } else {
                for (int i = 0; i < tags.size(); ++i) {
                    removed = (BaseMultipleFragment) getSupportFragmentManager().findFragmentByTag(tags.get(i));
                    if (removed.getTag().equals(tag)) {
                        getSupportFragmentManager().beginTransaction()
                                .remove(removed).commitNow();
                        tags.remove(i);
                        onFragmentRemoved(containerId, tag);
                        break;
                    }
                }
            }
        }
    }

    private void onOutsideActionReceived() {
        if (getIntent() != null) {
            if (getIntent().getData() != null
                    && !Utils.isEmpty(getIntent().getData().getHost())
                    && (getIntent().getData().getHost()
                    .equals(getString(R.string.deep_linking_app_host)) || getIntent()
                    .getData().getHost()
                    .equals(getString(R.string.deep_linking_http_host)))) {
                onDeepLinking(new Intent(getIntent()));
                if (getTopFragment(mainContainerId) != null)
                    getTopFragment(mainContainerId).onDeepLinking(
                            new Intent(getIntent()));

                Intent resetDeepLinkIntent = new Intent(getIntent());
                resetDeepLinkIntent.setData(Uri.EMPTY);
                setIntent(resetDeepLinkIntent);
            } else if (getIntent().getExtras() != null
                    && getIntent().getBooleanExtra(
                    Constant.NOTIFICATION_DEFINED, false)) {

                int id = getIntent().getIntExtra(Constant.NOTIFICATION_ID, -1);
                if (id != -1) {
                    NotificationManager manager = (NotificationManager) getBaseContext()
                            .getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(id);
                    onNotification(new Intent(getIntent()));
                    if (getTopFragment(mainContainerId) != null)
                        getTopFragment(mainContainerId).onNotification(
                                new Intent(getIntent()));
                    Intent resetNotificationIntent = new Intent(getIntent());
                    resetNotificationIntent.putExtra(
                            Constant.NOTIFICATION_DEFINED, false);
                    setIntent(resetNotificationIntent);
                }
            }
        }
    }

    private void addFragment(@IdRes int containerId, BaseMultipleFragment fragment,
                             String tag) {
        if (getSupportFragmentManager() != null) {
            ArrayList<String> tags = containers.get(containerId);
            if (tags == null) {
                if (mainContainerId == -1)
                    mainContainerId = containerId;
                containers.put(containerId, tags = new ArrayList<>());
                tags.add(tag);
                FragmentTransaction transaction = getSupportFragmentManager()
                        .beginTransaction();
                int anim = fragment.getEnterInAnimation();
                if (anim == -1) {
                    anim = Constant.DEFAULT_ADD_ANIMATION[0];
                }
                transaction
                        .setCustomAnimations(anim,
                                0, 0, 0) // add in animation
                        .add(containerId, fragment, tag).commitNow();
                onFragmentAdded(containerId, tag);
            } else {
                for (String sTag : tags) {
                    if (!Utils.isEmpty(sTag)
                            && sTag.equals(tag)) {
                        if (shouldBackIfFragmentExist()) {
                            backStack(containerId, tag);
                            return;
                        } else {
                            return; // if the fragment exist, return
                        }
                    }
                }
                BaseMultipleFragment top = getTopFragment(containerId);
                if (top != null)
                    top.onPause();
                animateAddOut(top);
                tags.add(tag);
                FragmentTransaction transaction = getSupportFragmentManager()
                        .beginTransaction();
                int anim = fragment.getEnterInAnimation();
                if (anim == -1) {
                    anim = Constant.DEFAULT_ADD_ANIMATION[0];
                }
                transaction
                        .setCustomAnimations(
                                anim, 0, 0, 0) // add
                        // in
                        // animation
                        .add(containerId, fragment, tag).commitNow();
                onFragmentAdded(containerId, tag);
            }
        }
    }

    private void replaceFragment(@IdRes int containerId,
                                 BaseMultipleFragment fragment, String tag, boolean clearStack) {
        if (getSupportFragmentManager() != null) {
            ArrayList<String> tags = containers.get(containerId);
            if (tags != null) {
                if (clearStack) {
                    popAllBackStack(containerId);
                    addFragment(containerId, fragment, tag);
                } else {
                    boolean isExist = false;
                    for (int i = 0; i < tags.size(); ++i) {
                        BaseMultipleFragment entry = (BaseMultipleFragment) getSupportFragmentManager().findFragmentByTag(tags.get(i));
                        if (entry != null && entry.getTag().equals(tag)) {
                            isExist = true;
                            break;
                        }
                    }
                    if (isExist) {
                        if (tags.size() > 1) {
                            BaseMultipleFragment top = getTopFragment(containerId);
                            if (!(top != null && top.getTag().equals(tag))) {
                                backStack(containerId, tag);
                            }
                        }
                    } else {
                        if (tags.size() > 1) {
                            BaseMultipleFragment top = getTopFragment(containerId);
                            addFragment(containerId, fragment, tag);
                            if (top != null && !Utils.isEmpty(top.getTag()))
                                removeFragment(containerId, top.getTag());
                        } else {
                            popAllBackStack(containerId);
                            addFragment(containerId, fragment, tag);
                        }
                    }
                }
            } else {
                addFragment(containerId, fragment, tag);
            }
        }
    }

    private void clearStack(@IdRes int containerId) {
        ArrayList<String> tags = containers.get(containerId);
        if (tags != null)
            tags.clear();
    }

    private void clearAllStacks() {
        for (int i = 0; i < containers.size(); ++i) {
            ArrayList<String> stack = containers.get(i);
            if (stack != null)
                stack.clear();
        }
        containers.clear();
    }

    private void animateAddOut(BaseMultipleFragment previous) {
        if (previous != null) {
            final View view = previous.getView();
            if (view != null) {
                int anim = previous.getEnterOutAnimation();
                if (anim == -1) {
                    anim = Constant.DEFAULT_ADD_ANIMATION[1];
                }
                Animation animation = AnimationUtils.loadAnimation(this,
                        anim);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        view.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(animation);
            }
        }
    }

    private void animateBackIn(final View view, @AnimRes int anim) {
        if (view != null) {
            if (anim == -1) {
                anim = Constant.DEFAULT_BACK_ANIMATION[0];
            }
            Animation animation = AnimationUtils.loadAnimation(this,
                    anim);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    view.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.startAnimation(animation);
        }
    }

    private void animateBackOut(View view, @AnimRes int anim, final FragmentTransaction transaction, @IdRes final int containerId, final String[] removingTagsArray) {
        if (view != null) {
            if (anim == -1) {
                anim = Constant.DEFAULT_BACK_ANIMATION[1];
            }
            Animation animation = AnimationUtils.loadAnimation(this,
                    anim);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    transaction.commitNow();
                    onFragmentRemoved(containerId, removingTagsArray);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.startAnimation(animation);
        }
    }

    /**
     * The method is to remove all the child fragments in the parent fragment container
     * The removing transaction is same as the parent's transaction
     * BaseFragmentContainer must be use for the fragment container instead of normal FrameLayout
     */
    private void removeAllChildFragments(View parentFragment, FragmentTransaction transaction) {
        if (parentFragment != null) {
            ArrayList<View> subContainers = new ArrayList<>();
            parentFragment.findViewsWithText(subContainers, BaseFragmentContainer.TAG, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
            for (View container : subContainers) {
                if (container != null && container.getId() != View.NO_ID) {
                    ArrayList<String> subTags = containers.get(container.getId());

                    if (subTags != null) {
                        for (String subTag : subTags) {
                            BaseMultipleFragment subEntry = (BaseMultipleFragment) getSupportFragmentManager().findFragmentByTag(subTag);
                            transaction.remove(subEntry);
                        }
                        subTags.clear();
                    }
                }
            }
        }
    }

    private void removeDuplicateFragments(BaseMultipleFragment[] fromFragments, ArrayList<String> toTags, ArrayList<BaseMultipleFragment> toFragments) {

        if (fromFragments != null) {

            for (BaseMultipleFragment fragment : fromFragments)
                toTags.add(fragment.getUniqueTag());

            LinkedHashSet<String> tagSet = new LinkedHashSet<>(toTags);
            toTags.clear();
            toTags.addAll(tagSet);
            // remove duplicate tags

            for (String tag : toTags) {
                for (BaseMultipleFragment fragment : fromFragments) {
                    if (!Utils.isEmpty(tag) && tag.equals(fragment.getUniqueTag())) {
                        toFragments.add(fragment);
                        break;
                    }
                }
            }
        }
    }

    private ArrayList<BaseMultipleFragment> removeDuplicateFragmentsWithExisting(ArrayList<String> tags, ArrayList<String> addingTags, ArrayList<BaseMultipleFragment> addingFragments) {
        int originalTagSize = tags.size();
        LinkedHashSet<String> merge = new LinkedHashSet<>(tags);
        merge.addAll(addingTags);
        tags.clear();
        tags.addAll(merge);
        // remove all duplicate with the existing tags
        ArrayList<String> willAddTags = new ArrayList<>(tags.subList(originalTagSize - 1, tags.size() - 1));
        ArrayList<BaseMultipleFragment> willAddFragments = new ArrayList<>();

        for (String willAddTag : willAddTags) {
            for (BaseMultipleFragment fragment : addingFragments) {
                if (willAddTag.equals(fragment.getUniqueTag())) {
                    willAddFragments.add(fragment);
                    break;
                }
            }
        }
        return willAddFragments;
    }

}
