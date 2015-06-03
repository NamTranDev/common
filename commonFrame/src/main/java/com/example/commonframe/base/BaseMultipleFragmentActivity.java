package com.example.commonframe.base;

import java.util.NoSuchElementException;
import java.util.Stack;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.example.commonframe.R;
import com.example.commonframe.connection.BackgroundServiceRequester;
import com.example.commonframe.connection.Requester;
import com.example.commonframe.connection.WebServiceRequester;
import com.example.commonframe.connection.WebServiceRequester.WebServiceResultHandler;
import com.example.commonframe.dialog.AlertDialog;
import com.example.commonframe.dialog.AlertDialog.AlertDialogListener;
import com.example.commonframe.dialog.DecisionDialog;
import com.example.commonframe.dialog.DecisionDialog.DecisionDialogListener;
import com.example.commonframe.dialog.LoadingDialog;
import com.example.commonframe.dialog.Option;
import com.example.commonframe.dialog.OptionsDialog;
import com.example.commonframe.dialog.OptionsDialog.OptionsDialogListener;
import com.example.commonframe.model.base.Param;
import com.example.commonframe.util.CentralApplication;
import com.example.commonframe.util.Constant;
import com.example.commonframe.util.Constant.RequestTarget;
import com.example.commonframe.util.SingleClick;
import com.example.commonframe.util.SingleClick.SingleClickListener;
import com.example.commonframe.util.SingleTouch;
import com.example.commonframe.util.Utils;

/**
 * @author Tyrael
 * @since May 2015
 * @version 1.0 <br>
 * <br>
 *          <b>Class Overview</b> <br>
 * <br>
 *          Represents a class for essential fragment activity to be a super
 *          class of activities in project. It includes supportive method of
 *          showing, closing dialogs, making and canceling request. Those
 *          methods can be used in any derived class. <br>
 *          This class also supports multiple fragment containers for both
 *          tablet and phone with methods of add, remove, replace, back and
 *          clear fragments on a specific container. <br>
 *          The derived classes must implement <code>onCreateObject()</code>,
 *          <code>onBindView()</code>, <code>onResumeObject()</code>,
 *          <code>onFreeObject()</code> for the purpose of management.
 * 
 */
public abstract class BaseMultipleFragmentActivity extends FragmentActivity
		implements BaseInterface, SingleClickListener {
	/**
	 * Tag of BaseFragmentActivity class for Log usage
	 */
	protected static String TAG = "BaseMultipleFragmentActivity";

	/**
	 * The single click to handle click action for this screen
	 */
	private SingleClick singleClick = null;

	/**
	 * The flag indicating that the activity is finished and should free all of
	 * resources at <code>onStop()</code> method
	 */
	private boolean isFinished = false;

	/**
	 * The flag indicating that the fragments are first initialized after the
	 * activity created, this variable is only invoked once.
	 */
	private boolean isFragmentsInitialized = false;

	/**
	 * The identification of the main fragment container, the default is the
	 * first container added. Or it can be set by
	 * <code>setMainContainerId()</code>. The id is used for default
	 * <code>onBackPress()</code>, <code>onDeepLinking()</code>,
	 * <code>onNotification()</code>, <code>onActivityResult()</code>
	 */
	private int mainContaineId = -1;

	/**
	 * The array of fragment containers and all of its stacks. Each entry is
	 * defined by the id of the container.
	 */
	private final SparseArray<Stack<BaseMultipleFragment>> containers = new SparseArray<Stack<BaseMultipleFragment>>();

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
	 * @param containerId
	 *            The container id of the last fragment when back stack event
	 *            called
	 */
	protected abstract void onLastFragmentBack(int containerId);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		TAG = getClass().getName();
		overridePendingTransition(Constant.DEFAULT_ADD_ANIMATION[0],
				Constant.DEFAULT_ADD_ANIMATION[1]);
		super.onCreate(savedInstanceState);
		onCreateObject();

	}

	@Override
	protected void onResume() {
		TAG = getClass().getName();
		BaseProperties.wsRequester = WebServiceRequester
				.getInstance(CentralApplication.getContext());
		BaseProperties.bgRequester = BackgroundServiceRequester
				.getInstance(CentralApplication.getContext());
		CentralApplication.setActiveActivity(this);
		// EventBus.getDefault().register(this);
		onResumeObject();
		super.onResume();
		onOutsideActionReceived();
		Utils.logHeap(TAG);
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
				if (getTopFragment(mainContaineId) != null)
					getTopFragment(mainContaineId).onDeepLinking(
							new Intent(getIntent()));

				Intent resetDeepLinkIntent = new Intent(getIntent());
				resetDeepLinkIntent.setData(Uri.EMPTY);
				setIntent(resetDeepLinkIntent);
			} else if (getIntent().getExtras() != null
					&& getIntent().getBooleanExtra(
							Constant.NOTIFICATION_DEFINED, false)) {

				int id = getIntent().getIntExtra(
						Constant.NOTIFICATION_ID, -1);
				if (id != -1) {
					NotificationManager manager = (NotificationManager) getCentralContext()
							.getSystemService(Context.NOTIFICATION_SERVICE);
					manager.cancel(id);
					onNotification(new Intent(getIntent()));
					if (getTopFragment(mainContaineId) != null)
						getTopFragment(mainContaineId).onNotification(
								new Intent(getIntent()));
					Intent resetNotificationIntent = new Intent(getIntent());
					resetNotificationIntent.putExtra(
							Constant.NOTIFICATION_DEFINED, false);
					setIntent(resetNotificationIntent);
				}
			}
		}
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);
		onBindView();
		onInitializeViewData();
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
	protected void onStop() {
		if (isFinished) {
			onFreeObject();
			Utils.nullViewDrawablesRecursive(findViewById(android.R.id.content)
					.getRootView());
			Utils.unbindDrawables(findViewById(android.R.id.content)
					.getRootView());
		}
		super.onStop();
	}

	@Override
	public void finish() {
		isFinished = true;
		super.finish();
		overridePendingTransition(Constant.DEFAULT_BACK_ANIMATION[0],
				Constant.DEFAULT_BACK_ANIMATION[1]);
	}

	@Override
	protected void onPause() {
		// EventBus.getDefault().unregister(this);
		cancelRequest();
		closeLoadingDialog();
		super.onPause();
		if (isFinished) {
			clearAllStacks();
		}
	}

	@Override
	public void onBackPressed() {
		if (BaseProperties.getSingleBackPress().onBackPressAllowed()) {
			// super.onBackPressed();
			backStack(mainContaineId, null);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		BaseMultipleFragment fragment = getTopFragment(mainContaineId);
		if (fragment != null)
			fragment.onActivityResult(requestCode, resultCode, data);

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public View findViewById(int id) {
		View view = super.findViewById(id);
		if (view != null && !BaseProperties.isExceptionalView(view)) {
			view.setOnClickListener(getSingleClick());
			view.setOnTouchListener(getSingleTouch());
		}
		return view;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			Utils.closeSoftKeyboard(this, findViewById(android.R.id.content)
					.getRootView());
			return true;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void showDecisionDialog(Context context, int id, String title,
			String message, String yes, String no,
			DecisionDialogListener listener) {
		if (BaseProperties.questionDialog != null)
			BaseProperties.questionDialog.dismiss();
		BaseProperties.questionDialog = null;
		if (BaseProperties.questionDialog == null)
			BaseProperties.questionDialog = new DecisionDialog(context, id,
					title, message, yes, no, listener);

		if (BaseProperties.questionDialog != null)
			BaseProperties.questionDialog.show();
	}

	@Override
	public void showOptionsDialog(Context context, int id, String title,
			String message, int icon, Option[] options,
			OptionsDialogListener listener) {
		if (BaseProperties.optionsDialog != null)
			BaseProperties.optionsDialog.dismiss();
		BaseProperties.optionsDialog = null;

		if (BaseProperties.optionsDialog == null)
			BaseProperties.optionsDialog = new OptionsDialog(context, id,
					title, message, icon, options, listener);

		if (BaseProperties.optionsDialog != null)
			BaseProperties.optionsDialog.show();
	}

	@Override
	public void showAlertDialog(Context context, int id, String title,
			String message, int icon_id, AlertDialogListener listener) {
		if (BaseProperties.alertDialog != null)
			BaseProperties.alertDialog.dismiss();
		BaseProperties.alertDialog = null;
		if (BaseProperties.alertDialog == null)
			BaseProperties.alertDialog = new AlertDialog(context, id, title,
					message, icon_id, listener);

		if (BaseProperties.alertDialog != null)
			BaseProperties.alertDialog.show();
	}

	@Override
	public void showLoadingDialog(Context context) {
		if (BaseProperties.loadingDialog != null)
			BaseProperties.loadingDialog.dismiss();
		BaseProperties.loadingDialog = null;
		if (BaseProperties.loadingDialog == null)
			BaseProperties.loadingDialog = new LoadingDialog(context);

		if (BaseProperties.loadingDialog != null)
			BaseProperties.loadingDialog.show();
	}

	@Override
	public void closeLoadingDialog() {
		if (BaseProperties.loadingDialog != null)
			if (BaseProperties.loadingDialog.isShowing())
				BaseProperties.loadingDialog.dismiss();
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
	public Activity getActiveActivity() {
		return CentralApplication.getActiveActivity();
	}

	@Override
	public Context getCentralContext() {
		return CentralApplication.getContext();
	}

	public void cancelRequest() {
		if (BaseProperties.wsRequester != null)
			BaseProperties.wsRequester.cancelAll(null);
		BaseProperties.wsRequester = (null);
	}

	@Override
	public void cancelWebServiceRequest(String tag) {
		if (BaseProperties.wsRequester != null)
			BaseProperties.wsRequester.cancelAll(tag);
	}

	@Override
	public void cancelBackgroundRequest(String tag) {
		if (BaseProperties.bgRequester != null)
			BaseProperties.bgRequester.cancelAll(tag);
	}

	public void makeBackgroundRequest(String tag, RequestTarget target,
			String[] extras, Param content) {
		if (!Utils.isInternetAvailable()) {
			return;
		}
		if (!Requester.startBackgroundRequest(tag, target, extras, content))
			;
	}

	public void makeRequest(String tag, boolean loading, Param content,
			WebServiceResultHandler handler, RequestTarget target, String... extras) {
		if (!Utils.isInternetAvailable()) {
			closeLoadingDialog();
			showAlertDialog(
					this,
					-1,
					getResourceString(R.string.error_internet_unavailable_title),
					getResourceString(R.string.error_internet_unavailable_message),
					-1, null);
			return;
		}
		if (loading)
			showLoadingDialog(this);
		if (!Requester.startWSRequest(tag, target, extras, content, handler))
			closeLoadingDialog();
	}

	@Override
	public SingleTouch getSingleTouch() {
		return BaseProperties.getSingleTouch();
	}

	@Override
	public SingleClick getSingleClick() {
		if (singleClick == null) {
			singleClick = new SingleClick();
			singleClick.setListener(this);
		}
		return singleClick;
	}

	protected void setMainContainerId(int mainContainerId) {
		this.mainContaineId = mainContainerId;
	}

	public BaseMultipleFragment getTopFragment(int containerId) {
		try {
			Stack<BaseMultipleFragment> fragments = containers.get(containerId);
			if (fragments != null)
				return fragments.lastElement();
		} catch (NoSuchElementException e) {
		}
		return null;
	}

	private void clearStack(int containerId) {
		Stack<BaseMultipleFragment> fragments = containers.get(containerId);
		if (fragments != null)
			fragments.removeAllElements();
	}

	private void clearAllStacks() {
		for (int i = 0; i < containers.size(); ++i) {
			Stack<BaseMultipleFragment> stack = containers.get(i);
			if (stack != null)
				stack.removeAllElements();
		}
		containers.clear();
	}

	public void backStack(int containerId, String toTag) {
		if (getSupportFragmentManager() != null) {
			Stack<BaseMultipleFragment> fragments = containers.get(containerId);
			if (fragments != null) {
				if (fragments.size() <= 1 && Utils.isEmpty(toTag)) {
					onLastFragmentBack(containerId);
				} else {
					FragmentTransaction transaction = getSupportFragmentManager()
							.beginTransaction();
					for (int i = fragments.size() - 1; i > 0; --i) {
						BaseMultipleFragment entry = fragments.get(i);
						if (entry != null) {
							View view = entry.getView();
							if (Utils.isEmpty(toTag)) {
								animateBackOut(view);
								entry.onPauseObject();
								fragments.remove(i);
								transaction.remove(entry);
								break;
							} else {
								if (toTag.equals(entry.getTag()))
									break;
								animateBackOut(view);
								entry.onPauseObject();
								fragments.remove(i);
								transaction.remove(entry);
							}
						}
					}
					transaction.commit();
					getSupportFragmentManager().executePendingTransactions();
					BaseMultipleFragment fragment = getTopFragment(containerId);
					if (fragment != null) {
						if (fragment.getView() != null) {
							View view = fragment.getView();
							view.setVisibility(View.VISIBLE);
							animateBackIn(view);
						}
						fragment.onResume();
					}
				}
			}
		}
	}

	protected void popAllBackStack(int containerId) {
		if (getSupportFragmentManager() != null) {
			try {
				Stack<BaseMultipleFragment> fragments = containers
						.get(containerId);
				if (fragments != null) {
					BaseMultipleFragment last = getTopFragment(containerId);
					if (last != null) {
						animateAddOut(containerId);
					}
					FragmentTransaction transaction = null;
					for (BaseMultipleFragment fragment : fragments) {
						transaction = getSupportFragmentManager()
								.beginTransaction().remove(fragment);
					}
					if (transaction != null) {
						transaction.commit();
						getSupportFragmentManager()
								.executePendingTransactions();
					}
					clearStack(containerId);
				}
			} catch (Exception e) {
			}
		}
	}

	protected void addFragment(int containerId, BaseMultipleFragment fragment,
			String tag) {
		BaseMultipleFragment top = getTopFragment(containerId);
		if (top != null) {
			if (tag.equals(top.getTag()))
				return;
			top.onPause();
		}
		animateAddOut(containerId);
		if (getSupportFragmentManager() != null) {
			Stack<BaseMultipleFragment> fragments = containers.get(containerId);
			if (fragments == null) {
				if (mainContaineId == -1)
					mainContaineId = containerId;
				containers.append(containerId,
						fragments = new Stack<BaseMultipleFragment>());
				fragments.add(fragment);
				FragmentTransaction transaction = getSupportFragmentManager()
						.beginTransaction();
				transaction
						.setCustomAnimations(Constant.DEFAULT_ADD_ANIMATION[0],
								0, 0, 0) // add in animation
						.add(containerId, fragment, tag).commit();
				getSupportFragmentManager().executePendingTransactions();
			} else {
				boolean isExist = false;
				for (BaseMultipleFragment bf : fragments) {
					if (bf != null && !Utils.isEmpty(bf.getTag())
							&& bf.getTag().equals(tag)) {
						isExist = true;
						break;
					}
				}
				if (!isExist) {
					fragments.add(fragment);
					FragmentTransaction transaction = getSupportFragmentManager()
							.beginTransaction();
					transaction
							.setCustomAnimations(
									Constant.DEFAULT_ADD_ANIMATION[0], 0, 0, 0) // add
																				// in
																				// animation
							.add(containerId, fragment, tag).commit();
					getSupportFragmentManager().executePendingTransactions();
				} else {
					backStack(containerId, tag);
				}
			}
		}
	}

	protected void replaceFragment(int containerId,
			BaseMultipleFragment fragment, String tag, boolean clearStack) {
		if (getSupportFragmentManager() != null) {
			Stack<BaseMultipleFragment> fragments = containers.get(containerId);
			if (fragments != null) {
				if (clearStack) {
					popAllBackStack(containerId);
					addFragment(containerId, fragment, tag);
				} else {
					boolean isExist = false;
					for (int i = 0; i < fragments.size(); ++i) {
						BaseMultipleFragment entry = fragments.get(i);
						if (entry != null && entry.getTag().equals(tag)) {
							isExist = true;
							break;
						}
					}
					if (isExist) {
						if (fragments.size() > 1) {
							BaseMultipleFragment top = getTopFragment(containerId);
							if (!(top != null && top.getTag().equals(tag))) {
								backStack(containerId, tag);
							}
						}
					} else {
						if (fragments.size() > 1) {
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

	protected void removeFragment(int containerId, String tag) {
		Stack<BaseMultipleFragment> fragments = containers.get(containerId);
		if (fragments != null) {
			BaseMultipleFragment removed = getTopFragment(containerId);
			if (removed != null && removed.getTag().equals(tag)) {
				backStack(containerId, null);
			} else {
				for (int i = 0; i < fragments.size(); ++i) {
					removed = fragments.get(i);
					if (removed.getTag().equals(tag)) {
						getSupportFragmentManager().beginTransaction()
								.remove(removed).commit();
						getSupportFragmentManager()
								.executePendingTransactions();
						fragments.remove(i);
						break;
					}
				}
			}
		}
	}

	private void animateAddOut(int containerId) {
		BaseMultipleFragment previous = getTopFragment(containerId);
		if (previous != null) {
			final View view = previous.getView();
			if (view != null) {
				view.startAnimation(AnimationUtils.loadAnimation(this,
						Constant.DEFAULT_ADD_ANIMATION[1]));
			}
		}
	}

	private void animateBackIn(View view) {
		if (view != null) {
			view.startAnimation(AnimationUtils.loadAnimation(this,
					Constant.DEFAULT_BACK_ANIMATION[0]));
		}
	}

	private void animateBackOut(View view) {
		if (view != null) {
			view.startAnimation(AnimationUtils.loadAnimation(this,
					Constant.DEFAULT_BACK_ANIMATION[1]));
		}
	}
}