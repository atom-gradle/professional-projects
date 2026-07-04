package com.qian.feather;

import android.view.View;

public interface OnRecyclerViewItemClickListener extends View.OnLongClickListener {
    boolean onLongClick(View v);
    void onItemClick(int position);
}
