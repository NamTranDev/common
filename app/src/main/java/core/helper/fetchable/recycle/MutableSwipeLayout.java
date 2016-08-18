package core.helper.fetchable.recycle;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.example.commonframe.R;


public class MutableSwipeLayout extends SwipeRefreshLayout {

    private static final int MAX_ALPHA = 255;

    private static final int CIRCLE_DIAMETER = 40;

    private static final int CIRCLE_BG_LIGHT = 0xFFFAFAFA;
    private CircleImageView circle;
    private MaterialProgressDrawable progress;
    private Animation in;
    private Animation out;

    public MutableSwipeLayout(Context context) {
        super(context);
        init();
    }

    public MutableSwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return !isRefreshing() && super.onStartNestedScroll(child, target, nestedScrollAxes);
    }

    private void init() {
        in = AnimationUtils.loadAnimation(getContext(), R.anim.refresh_in);
        out = AnimationUtils.loadAnimation(getContext(), R.anim.refresh_out);
        in.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                progress.start();
                circle.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                circle.clearAnimation();
            }

        });
        out.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                progress.stop();
                circle.clearAnimation();
                circle.setVisibility(View.GONE);
            }

        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        createProgressView();
    }

    @Override
    public void setColorSchemeColors(int... colors) {
        super.setColorSchemeColors(colors);
        progress.setColorSchemeColors(colors);
    }

    private void createProgressView() {
        RelativeLayout layout = new RelativeLayout(getContext());
        progress = new MaterialProgressDrawable(getContext(), this);
        progress.setBackgroundColor(CIRCLE_BG_LIGHT);
        progress.setAlpha(MAX_ALPHA);
        circle = new CircleImageView(getContext(), CIRCLE_BG_LIGHT, CIRCLE_DIAMETER / 2);
        circle.setImageDrawable(progress);
        circle.setVisibility(View.GONE);

        View original = getChildAt(1);
        removeViewAt(1);
        layout.addView(original, 0);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layout.addView(circle, 1, params);
        addView(layout, 1);

        // disable refresh progress when initialized
        View refreshProgress = getChildAt(0);
        if (refreshProgress != null)
            refreshProgress.setAlpha(0);
    }


    public void setEnableRefreshProgress(boolean enable) {
        View refreshProgress = getChildAt(0);
        if (refreshProgress != null)
            refreshProgress.setAlpha(enable ? 1 : 0);
    }

    public void startLoadingAnimation() {
        if (circle != null && circle.getVisibility() == View.GONE)
            circle.startAnimation(in);
    }

    public void stopLoadingAnimation() {
        if (circle != null && circle.getVisibility() == View.VISIBLE)
            circle.startAnimation(out);
    }
}
