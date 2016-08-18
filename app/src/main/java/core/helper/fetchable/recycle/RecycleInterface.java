package core.helper.fetchable.recycle;

import android.view.View;

public interface RecycleInterface<T> {

    void onItemClick(View view, T item, int position, int type);

}
