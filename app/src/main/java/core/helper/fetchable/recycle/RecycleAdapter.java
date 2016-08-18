package core.helper.fetchable.recycle;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import core.util.SingleClick;
import core.util.SingleTouch;


public abstract class RecycleAdapter<T> extends RecyclerView.Adapter implements SingleClick.SingleClickListener {

    public static final int HEADER_TYPE = 1;
    public static final int ITEM_TYPE = 2;

    protected final RecycleInterface<T> listener;
    protected final ArrayList<T> items;
    private final SingleClick singleClick;
    private final SingleTouch singleTouch;
    private final LayoutInflater inflater;

    public RecycleAdapter(LayoutInflater inflater, ArrayList<T> items, RecycleInterface<T> listener, SingleTouch singleTouch) {
        super();
        this.inflater = inflater;
        this.listener = listener;
        this.items = items;
        this.singleClick = new SingleClick();
        this.singleClick.setListener(this);
        this.singleTouch = singleTouch;
    }

    @Override
    public final ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder view = null;
        switch (viewType) {
            case HEADER_TYPE:
                view = new HeaderViewHolder<String>(inflater.inflate(getHeaderLayoutResource(), parent, false));
                break;
            case ITEM_TYPE:
                view = new ItemViewHolder<String>(inflater.inflate(getItemLayoutResource(), parent, false));
                break;
        }
        if (view != null && view.itemView != null) {
            view.setIsRecyclable(true);
            view.itemView.setOnClickListener(singleClick);
            view.itemView.setOnTouchListener(singleTouch);
            view.itemView.setTag(view);
        }
        return view;
    }

    @LayoutRes
    protected abstract int getHeaderLayoutResource();

    @LayoutRes
    protected abstract int getItemLayoutResource();

    protected abstract void bindHeaderView(HeaderViewHolder<T> holder, T data, int position);

    protected abstract void bindItemView(ItemViewHolder<T> holder, T data, int position);

    @Override
    public final void onBindViewHolder(ViewHolder holder, int position) {

        if (holder != null) {
            switch (holder.getItemViewType()) {
                case HEADER_TYPE:
                    HeaderViewHolder header = (HeaderViewHolder) holder;
                    bindHeaderView(header, items.get(position), position);
                    header.setData(items.get(position));
                    break;
                case ITEM_TYPE:
                    ItemViewHolder item = (ItemViewHolder) holder;
                    bindItemView(item, items.get(position), position);
                    item.setData(items.get(position));
                    break;
            }

        }
    }


    @Override
    public final void onSingleClick(View v) {
        Object holder = v.getTag();
        if (holder instanceof HeaderViewHolder) {
            listener.onItemClick(v, (T) ((HeaderViewHolder) holder).getData(), ((HeaderViewHolder) holder).getAdapterPosition(), HEADER_TYPE);
        } else if (holder instanceof ItemViewHolder) {
            listener.onItemClick(v, (T) ((ItemViewHolder) holder).getData(), ((ItemViewHolder) holder).getAdapterPosition(), ITEM_TYPE);
        }
    }

    protected class HeaderViewHolder<T> extends RecyclerView.ViewHolder {

        private T data;

        public HeaderViewHolder(View view) {
            super(view);
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

        public View findViewById(int id) {
            return itemView.findViewById(id);
        }
    }

    protected class ItemViewHolder<T> extends RecyclerView.ViewHolder {

        private T data;

        public ItemViewHolder(View view) {
            super(view);
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

        public View findViewById(int id) {
            return itemView.findViewById(id);
        }
    }
}
