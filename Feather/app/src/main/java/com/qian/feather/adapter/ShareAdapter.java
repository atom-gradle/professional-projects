package com.qian.feather.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.qian.feather.item.ShareTarget;
import com.qian.feather.R;

import java.util.List;

public class ShareAdapter extends BaseAdapter {
    private Context context;
    private List<ShareTarget> apps;

    public ShareAdapter(Context context, List<ShareTarget> apps) {
        this.context = context;
        this.apps = apps;
    }

    @Override
    public int getCount() {
        return apps.size();
    }

    @Override
    public ShareTarget getItem(int position) {
        return apps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false);
        }

        ShareTarget app = getItem(position);
        ImageView icon = convertView.findViewById(R.id.app_icon);
        TextView name = convertView.findViewById(R.id.app_name);

        icon.setImageDrawable(app.icon);
        name.setText(app.name);

        // 确保 item 根布局不拦截事件
        convertView.setClickable(false);
        convertView.setFocusable(false);
        return convertView;
    }
}