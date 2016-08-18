package core.helper.fetchable.recycle;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

public class MutableRecycleView extends RecyclerView implements MutableSwipeLayout.OnRefreshListener {

    private MutableSwipeLayout refresher;
    private LinearLayoutManager layoutManager;
    private OnScrollListener onScrollListener;
    private OnRefreshListener onRefreshListener;
    private OnLoadMoreListener onLoadMoreListener;
    private Adapter adapter;
    private boolean isLoadingMore;
    private boolean isRefreshing;
    private boolean isFirstLoadSkipped;

    public MutableRecycleView(Context context) {
        super(context);
        init();
    }

    public MutableRecycleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MutableRecycleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewParent parent = getParent();
                if (parent != null && parent instanceof RelativeLayout) {
                    ViewParent grand = parent.getParent();
                    if (grand != null && grand instanceof MutableSwipeLayout) {
                        refresher = (MutableSwipeLayout) grand;
                        refresher.setEnabled(false);
                        refresher.setNestedScrollingEnabled(true);
                        refresher.setOnRefreshListener(MutableRecycleView.this);
                        refresher.setEnableRefreshProgress(false);
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            }
        });
    }

    private void init() {
        setHasFixedSize(true);
        setLayoutManager(new LinearLayoutManager(getContext()));
        isRefreshing = false;
        isLoadingMore = false;
    }

    public void setColorScheme(final int... colors) {
        if (refresher != null) {
            refresher.setColorSchemeColors(colors);
        } else {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (refresher != null) {
                        refresher.setColorSchemeColors(colors);
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }

    @Override
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        super.setOnScrollListener(onScrollListener);
        this.onScrollListener = onScrollListener;
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        if (onRefreshListener != null) {
            this.onRefreshListener = onRefreshListener;
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (refresher != null) {
                        refresher.setEnableRefreshProgress(true);
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        } else {
            this.onRefreshListener = null;
            if (refresher != null) {
                refresher.setEnableRefreshProgress(false);
            }
        }
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        this.adapter = adapter;
    }

    public void setLayoutManager(LinearLayoutManager layoutManager) {
        super.setLayoutManager(layoutManager);
        this.layoutManager = layoutManager;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        isFirstLoadSkipped = true;
        return super.onTouchEvent(e);
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);

        if (onScrollListener != null)
            onScrollListener.onScrolled(this, dx, dy);

        if (refresher != null && layoutManager != null) {
            if (layoutManager.findFirstCompletelyVisibleItemPosition() <= 0) {
                refresher.setEnabled(true);
            } else {
                refresher.setEnabled(false);
            }
        }

        if (onLoadMoreListener != null && adapter != null && layoutManager != null && isFirstLoadSkipped) {
            int totalItemCount = adapter.getItemCount();
            int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition();
            boolean shouldLoadMore = (lastVisible + 1) >= totalItemCount;

            if (shouldLoadMore) {
                if (!isLoadingMore) {
                    if (isRefreshing) {
                        if (onLoadMoreListener.shouldOverrideRefreshProcess()) {
                            interruptRefresh();
                            isLoadingMore = true;
                            onLoadMoreListener.onLoadMore();
                            if (refresher != null)
                                refresher.startLoadingAnimation();
                            System.out.println("load more while refreshing");
                        }
                    } else {
                        interruptRefresh();
                        isLoadingMore = true;
                        onLoadMoreListener.onLoadMore();
                        if (refresher != null)
                            refresher.startLoadingAnimation();
                        System.out.println("load more");
                    }
                }
            }
        }
    }

    public void onLoadMoreComplete() {
        interruptLoadMore();
        if (adapter != null)
            adapter.notifyDataSetChanged();
        System.out.println("load more complete");
    }

    @Override
    public void onRefresh() {
        if (onRefreshListener != null && refresher != null) {
            if (!isRefreshing) {
                if (isLoadingMore) {
                    if (onRefreshListener.shouldOverrideLoadMoreProcess()) {
                        interruptLoadMore();
                        isRefreshing = true;
                        onRefreshListener.onRefresh();
                        System.out.println("refresh while loading more");
                    } else {
                        refresher.setRefreshing(false);
                    }
                } else {
                    interruptLoadMore();
                    isRefreshing = true;
                    onRefreshListener.onRefresh();
                    System.out.println("refresh");
                }
            }
        }
    }

    public void onRefreshComplete() {
        interruptRefresh();
        if (adapter != null)
            adapter.notifyDataSetChanged();
        System.out.println("refresh complete");
    }

    private void interruptRefresh() {
        if (refresher != null && refresher.isRefreshing()) {
            refresher.setRefreshing(false);
        }
        isRefreshing = false;
    }

    private void interruptLoadMore() {
        isLoadingMore = false;
        if (refresher != null)
            refresher.stopLoadingAnimation();
    }

    public interface OnRefreshListener {

        void onRefresh();

        boolean shouldOverrideLoadMoreProcess();
    }

    public interface OnLoadMoreListener {

        void onLoadMore();

        boolean shouldOverrideRefreshProcess();
    }
}