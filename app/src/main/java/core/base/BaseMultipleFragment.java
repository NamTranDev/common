package core.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import core.connection.WebServiceRequester.WebServiceResultHandler;
import core.connection.queue.QueueElement;
import core.dialog.GeneralDialog.ConfirmListener;
import core.dialog.GeneralDialog.DecisionListener;
import core.util.ActionTracker;
import core.util.RequestTarget;
import core.util.SingleClick;
import core.util.SingleClick.SingleClickListener;
import core.util.SingleTouch;
import core.util.Utils;


@SuppressWarnings("unused")
public abstract class BaseMultipleFragment extends Fragment implements
        BaseInterface, SingleClickListener {

    /**
     * The flag to indicate all stack of fragments should resume when the host
     * activity is resuming. If true then all stacks will call resume, false
     * only the top fragment will call resume. Change true or false depends on
     * the behavior
     */
    private static final boolean isAllAttachedToActivityLifeCycle = false;

    /**
     * The single click to handle click action for this screen
     */
    private SingleClick singleClick = null;

    /**
     * Local active activity, in case the getActivity return null;
     */
    private BaseMultipleFragmentActivity activeActivity;

    /**
     * The unbinder of Butterknife to unbind views when the fragment view is destroyed
     */
    private Unbinder unbinder;

    /*
     * ANDROID LIFECYCLE
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onBaseCreate();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (view != null)
            view.setClickable(true);
        onBindView();
        onInitializeViewData();
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        if (activity instanceof BaseMultipleFragmentActivity) {
            activeActivity = (BaseMultipleFragmentActivity) activity;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAllAttachedToActivityLifeCycle) {
            // EventBus.getDefault().register(this);
            ActionTracker.enterScreen(getTag(), ActionTracker.Screen.FRAGMENT);
            onBaseResume();
        } else {
            resumeCurrentFragment();
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (getActivity() != null)
            getActivity().startActivityForResult(intent, requestCode);
        else if (getActiveActivity() != null)
            getActiveActivity().startActivityForResult(intent, requestCode);
        else
            activeActivity.startActivityForResult(intent, requestCode);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        if (getActivity() != null
                && getActivity() instanceof BaseMultipleFragmentActivity) {
            getActivity().startActivityForResult(intent, requestCode, options);
        } else if (getActiveActivity() != null
                && getActiveActivity() instanceof BaseMultipleFragmentActivity) {
            getActiveActivity().startActivityForResult(intent, requestCode, options);
        } else if (activeActivity != null) {
            activeActivity.startActivityForResult(intent, requestCode, options);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isAllAttachedToActivityLifeCycle) {
            onBasePause();
        } else {
            pauseCurrentFragment();
        }
    }

    @Override
    public void onDetach() {
        onBaseFree();
        Utils.nullViewDrawablesRecursive(getView());
        Utils.unbindDrawables(getView());
        super.onDetach();
        activeActivity = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        BaseApplication.getRefWatcher().watch(this);
    }

    /*
     * BASE INTERFACE
     */

    @Override
    public void onBindView() {
        unbinder = ButterKnife.bind(this, getView());
        /* Views are bind by Butterknife, override this for more actions on binding views */
    }

    @Override
    public String getResourceString(int id) {
        if (getActivity() != null
                && getActivity() instanceof BaseMultipleFragmentActivity)
            return ((BaseMultipleFragmentActivity) getActivity())
                    .getResourceString(id);
        else if (getActiveActivity() != null
                && getActiveActivity() instanceof BaseMultipleFragmentActivity)
            return ((BaseMultipleFragmentActivity) getActiveActivity())
                    .getResourceString(id);
        else
            return activeActivity.getResourceString(id);
    }

    @Override
    public final void registerSingleAction(View... views) {
        for (View view : views) {
            if (view != null) {
                if (!isExceptionalView(view)) {
                    view.setOnClickListener(getSingleClick());
                    view.setOnTouchListener(getSingleTouch());
                }
            }
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
    public final void showAlertDialog(Context context, int id, @LayoutRes int layout, @DrawableRes int icon,
                                      String title, String message, String confirm,
                                      Object onWhat, ConfirmListener listener) {
        if (getActivity() != null
                && getActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActivity()).showAlertDialog(
                    getActivity(), id, getGeneralDialogLayoutResource(), icon, title, message, confirm, onWhat, listener);
        else if (getActiveActivity() != null
                && getActiveActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActiveActivity())
                    .showAlertDialog(getActiveActivity(), id, getGeneralDialogLayoutResource(), icon,
                            title, message, confirm, onWhat, listener);
        else
            activeActivity.showAlertDialog(activeActivity, id, getGeneralDialogLayoutResource(), icon,
                    title, message, confirm, onWhat, listener);
    }

    @Override
    public final void showLoadingDialog(Context context, @LayoutRes int layout, String loading) {
        if (getActivity() != null
                && getActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActivity())
                    .showLoadingDialog(getActivity(), getLoadingDialogLayoutResource(), loading);
        else if (getActiveActivity() != null
                && getActiveActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActiveActivity())
                    .showLoadingDialog(getActiveActivity(), getLoadingDialogLayoutResource(), loading);
        else
            activeActivity.showLoadingDialog(activeActivity, getLoadingDialogLayoutResource(), loading);
    }

    @Override
    public final void showDecisionDialog(Context context, int id, @LayoutRes int layout, @DrawableRes int icon,
                                         String title, String message, String yes, String no, String cancel,
                                         Object onWhat, DecisionListener listener) {
        if (getActivity() != null
                && getActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActivity()).showDecisionDialog(
                    getActivity(), id, getGeneralDialogLayoutResource(), 0, title, message, yes, no,
                    null, onWhat, listener);
        else if (getActiveActivity() != null
                && getActiveActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActiveActivity())
                    .showDecisionDialog(getActiveActivity(), id, getGeneralDialogLayoutResource(), 0,
                            title, message, yes, no, null, onWhat, listener);
        else
            activeActivity.showDecisionDialog(activeActivity, id, getGeneralDialogLayoutResource(), 0,
                    title, message, yes, no, null, onWhat, listener);
    }

    @Override
    public final void closeLoadingDialog() {
        if (getActivity() != null
                && getActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActivity()).closeLoadingDialog();
        else if (getActiveActivity() != null
                && getActiveActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActiveActivity())
                    .closeLoadingDialog();
        else
            activeActivity.closeLoadingDialog();
    }

    @Override
    public boolean isExceptionalView(View view) {
        return BaseProperties.isExceptionalView(view);
    }

    @Override
    @SafeVarargs
    public final void makeFileRequest(String tag, String path, String name, String extension,
                                      RequestTarget target, Param content, Pair<String, String>... extras) {
        if (getActivity() != null
                && getActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActivity())
                    .makeFileRequest(tag, path, name, extension, target, content, extras);
        else if (getActiveActivity() != null
                && getActiveActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActiveActivity())
                    .makeFileRequest(tag, path, name, extension, target, content, extras);
        else
            activeActivity.makeFileRequest(tag, path, name, extension, target, content, extras);
    }

    @Override
    @SafeVarargs
    public final void makeRequest(String tag, boolean loading, Param content,
                                  WebServiceResultHandler handler, RequestTarget target,
                                  Pair<String, String>... extras) {
        if (getActivity() != null
                && getActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActivity()).makeRequest(tag,
                    loading, content, handler, target, extras);
        else if (getActiveActivity() != null
                && getActiveActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActiveActivity()).makeRequest(
                    tag, loading, content, handler, target, extras);
        else
            activeActivity.makeRequest(tag, loading, content, handler, target,
                    extras);
    }

    @Override
    @SafeVarargs
    public final void makeQueueRequest(String tag, QueueElement.Type type, Param content,
                                       RequestTarget target, Pair<String, String>... extras) {
        if (getActivity() != null
                && getActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActivity()).makeQueueRequest(tag, type, content, target, extras);
        else if (getActiveActivity() != null
                && getActiveActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActiveActivity()).makeQueueRequest(tag, type, content, target, extras);
        else
            activeActivity.makeQueueRequest(tag, type, content, target, extras);
    }

    @Override
    @SafeVarargs
    public final void makeParallelRequest(String tag, Param content, RequestTarget target, Pair<String, String>... extras) {
        if (getActivity() != null
                && getActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActivity()).makeParallelRequest(tag, content, target, extras);
        else if (getActiveActivity() != null
                && getActiveActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActiveActivity()).makeParallelRequest(tag, content, target, extras);
        else
            activeActivity.makeParallelRequest(tag, content, target, extras);
    }

    @Override
    public final void cancelWebServiceRequest(String tag) {
        if (getActivity() != null
                && getActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActivity())
                    .cancelWebServiceRequest(tag);
        else if (getActiveActivity() != null
                && getActiveActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActiveActivity())
                    .cancelWebServiceRequest(tag);
        else
            activeActivity.cancelWebServiceRequest(tag);
    }

    @Override
    public final void cancelBackgroundRequest(String tag) {
        if (getActivity() != null
                && getActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActivity())
                    .cancelBackgroundRequest(tag);
        else if (getActiveActivity() != null
                && getActiveActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActiveActivity())
                    .cancelBackgroundRequest(tag);
        else
            activeActivity.cancelBackgroundRequest(tag);
    }

    @Override
    public final SingleClick getSingleClick() {
        if (singleClick == null) {
            singleClick = new SingleClick();
            singleClick.setListener(this);
        }
        return singleClick;
    }

    @Override
    public final SingleTouch getSingleTouch() {
        if (getActivity() != null
                && getActivity() instanceof BaseMultipleFragmentActivity)
            return ((BaseMultipleFragmentActivity) getActivity())
                    .getSingleTouch();
        else if (getActiveActivity() != null
                && getActiveActivity() instanceof BaseMultipleFragmentActivity)
            return ((BaseMultipleFragmentActivity) getActiveActivity())
                    .getSingleTouch();
        else
            return activeActivity.getSingleTouch();
    }

    @LayoutRes
    @Override
    public int getGeneralDialogLayoutResource() {
        int layout;
        if (getActivity() != null
                && getActivity() instanceof BaseMultipleFragmentActivity)
            layout = ((BaseMultipleFragmentActivity) getActivity()).getGeneralDialogLayoutResource();
        else if (getActiveActivity() != null
                && getActiveActivity() instanceof BaseMultipleFragmentActivity)
            layout = ((BaseMultipleFragmentActivity) getActiveActivity()).getGeneralDialogLayoutResource();
        else
            layout = activeActivity.getGeneralDialogLayoutResource();

        return layout;
    }

    @LayoutRes
    @Override
    public int getLoadingDialogLayoutResource() {
        int layout;
        if (getActivity() != null
                && getActivity() instanceof BaseMultipleFragmentActivity)
            layout = ((BaseMultipleFragmentActivity) getActivity()).getLoadingDialogLayoutResource();
        else if (getActiveActivity() != null
                && getActiveActivity() instanceof BaseMultipleFragmentActivity)
            layout = ((BaseMultipleFragmentActivity) getActiveActivity()).getLoadingDialogLayoutResource();
        else
            layout = activeActivity.getLoadingDialogLayoutResource();

        return layout;
    }

    @AnimRes
    @Override
    public int getEnterInAnimation() {
        return -1;
    }

    @AnimRes
    @Override
    public int getBackInAnimation() {
        return -1;
    }

    @AnimRes
    @Override
    public int getEnterOutAnimation() {
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
        if (getActivity() != null
                && getActivity() instanceof BaseMultipleFragmentActivity)
            return ((BaseMultipleFragmentActivity) getActivity())
                    .getMainContainerId();
        else if (getActiveActivity() != null
                && getActiveActivity() instanceof BaseMultipleFragmentActivity)
            return ((BaseMultipleFragmentActivity) getActiveActivity())
                    .getMainContainerId();
        else
            return activeActivity.getMainContainerId();
    }

    public String getUniqueTag() {
        return getClass().getSimpleName();
    }

    protected void onBasePause() {
        // EventBus.getDefault().unregister(this);
        cancelWebServiceRequest(null);
        closeLoadingDialog();
    }

    protected final View findViewById(@IdRes int id) {
        if (getView() != null) {
            return getView().findViewById(id);
        }
        return null;
    }

    protected final void finish() {
        if (getView() != null && getView().getParent() != null) {
            int containerId = ((ViewGroup) getView().getParent()).getId();
            if (containerId != View.NO_ID && containerId >= 0) {
                if (getActivity() != null
                        && getActivity() instanceof BaseMultipleFragmentActivity)
                    ((BaseMultipleFragmentActivity) getActivity()).backStack(
                            containerId, null);
                else if (getActiveActivity() != null
                        && getActiveActivity() instanceof BaseMultipleFragmentActivity)
                    ((BaseMultipleFragmentActivity) getActiveActivity()).backStack(
                            containerId, null);
                else
                    activeActivity.backStack(containerId, null);
            }
        }
    }

    protected final void addMultipleFragments(@IdRes int containerId, BaseMultipleFragment... fragments) {
        if (getActivity() != null
                && getActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActivity()).addMultipleFragments(
                    containerId, fragments);
        else if (getActiveActivity() != null
                && getActiveActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActiveActivity()).addMultipleFragments(
                    containerId, fragments);
        else
            activeActivity.addMultipleFragments(containerId, fragments);
    }

    protected final void addFragment(@IdRes int containerId, BaseMultipleFragment fragment) {
        if (getActivity() != null
                && getActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActivity()).addFragment(
                    containerId, fragment);
        else if (getActiveActivity() != null
                && getActiveActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActiveActivity()).addFragment(
                    containerId, fragment);
        else
            activeActivity.addFragment(containerId, fragment);
    }

    protected final void replaceFragment(@IdRes int containerId,
                                         BaseMultipleFragment fragment, boolean clearStack) {
        if (getActivity() != null
                && getActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActivity()).replaceFragment(
                    containerId, fragment, clearStack);
        else if (getActiveActivity() != null
                && getActiveActivity() instanceof BaseMultipleFragmentActivity)
            ((BaseMultipleFragmentActivity) getActiveActivity())
                    .replaceFragment(containerId, fragment, clearStack);
        else
            activeActivity.replaceFragment(containerId, fragment,
                    clearStack);
    }

    private void pauseCurrentFragment() {
        if (getView() != null && getView().getParent() != null) {
            int containerId = ((ViewGroup) getView().getParent()).getId();
            if (getActivity() != null
                    && getActivity() instanceof BaseMultipleFragmentActivity) {
                BaseMultipleFragment top = ((BaseMultipleFragmentActivity) getActivity())
                        .getTopFragment(containerId);
                if (top != null && !Utils.isEmpty(top.getTag())
                        && getTag().equals(top.getTag())) {
                    top.onBasePause();
                }
            } else if (getActiveActivity() != null
                    && getActiveActivity() instanceof BaseMultipleFragmentActivity) {
                BaseMultipleFragment top = ((BaseMultipleFragmentActivity) getActiveActivity())
                        .getTopFragment(containerId);
                if (top != null && !Utils.isEmpty(top.getTag())
                        && getTag().equals(top.getTag())) {
                    top.onBasePause();
                }
            } else if (activeActivity != null) {
                BaseMultipleFragment top = activeActivity
                        .getTopFragment(containerId);
                if (top != null && !Utils.isEmpty(top.getTag())
                        && getTag().equals(top.getTag())) {
                    top.onBasePause();
                }
            }
        }
    }

    private void resumeCurrentFragment() {
        if (getView() != null && getView().getParent() != null) {
            int containerId = ((ViewGroup) getView().getParent()).getId();
            if (getActivity() != null
                    && getActivity() instanceof BaseMultipleFragmentActivity) {
                BaseMultipleFragment top = ((BaseMultipleFragmentActivity) getActivity())
                        .getTopFragment(containerId);
                if (top != null && !Utils.isEmpty(top.getTag())
                        && getTag().equals(top.getTag())) {
                    // EventBus.getDefault().register(this);
                    ActionTracker.enterScreen(getTag(), ActionTracker.Screen.FRAGMENT);
                    onBaseResume();
                }
            } else if (getActiveActivity() != null
                    && getActiveActivity() instanceof BaseMultipleFragmentActivity) {
                BaseMultipleFragment top = ((BaseMultipleFragmentActivity) getActiveActivity())
                        .getTopFragment(containerId);
                if (top != null && !Utils.isEmpty(top.getTag())
                        && getTag().equals(top.getTag())) {
                    // EventBus.getDefault().register(this);
                    ActionTracker.enterScreen(getTag(), ActionTracker.Screen.FRAGMENT);
                    onBaseResume();
                }
            } else if (activeActivity != null) {
                BaseMultipleFragment top = activeActivity
                        .getTopFragment(containerId);
                if (top != null && !Utils.isEmpty(top.getTag())
                        && getTag().equals(top.getTag())) {
                    // EventBus.getDefault().register(this);
                    ActionTracker.enterScreen(getTag(), ActionTracker.Screen.FRAGMENT);
                    onBaseResume();
                }
            }
        }
    }
}
