package com.qian.feather.CustomizedView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NewDividerItemDecoration extends RecyclerView.ItemDecoration {
    Context context;
    int orientation;
    Paint paint = new Paint();
    int mLeftOffset = 100;
    int mRightOffset = 100;
    int mDividerHeight = 2;
    public NewDividerItemDecoration(Context context,int orientation) {
        this.context = context;
        this.orientation = orientation;
        paint.setColor(Color.argb(255,246,246,246));
        paint.setStrokeWidth(0.5f);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(0, 0, 0, mDividerHeight);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (orientation == LinearLayoutManager.VERTICAL) {
            drawVerticalDivider(c, parent);
        }
    }

    //Draw item dividing line
    private void drawVerticalDivider(Canvas canvas, RecyclerView parent) {
        // mLeftOffset 为自己设置的左边偏移量
        final int left = parent.getPaddingLeft() + mLeftOffset;
        // mRightOffset 为设置的右边偏移量
        final int right = parent.getMeasuredWidth() - parent.getPaddingRight() + mRightOffset;
        final int childSize = parent.getChildCount();
        if (childSize <= 0) {
            return;
        }
        // 从第一个 item 开始绘制
        //int first = mStart;
        int first = 1;
        // 到第几个 item 绘制结束
        //int last = childSize - mEnd - (mIsShowLastDivider ? 0 : 1);
        int last = childSize;
        if (last <= 0) {
            return;
        }
        for (int i = first; i < last; i++) {
            drawableVerticalDivider(canvas, parent, left, right, i, mDividerHeight);
        }

    }

    private void drawableVerticalDivider(Canvas canvas, RecyclerView parent, int left, int right, int i, int dividerHeight) {
        final View child = parent.getChildAt(i);
        if (child == null) {
            return;
        }
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
        final int top = child.getBottom() + layoutParams.bottomMargin;
        final int bottom = top + dividerHeight;
/*
        // 适配 drawable
        if (mDivider != null) {
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(canvas);
        }
 */
        // 适配分割线
        if (paint != null) {
            canvas.drawRect(left, top, right, bottom, paint);
        }
    }
}
