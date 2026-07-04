package com.qian.feather.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ImageChooserSpinnerAdapter extends BaseAdapter {
    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }
    /*
    Context context;
    List<SpinnerBean> mDatas;
    public ImageChooserSpinnerAdapter(Context context, List<SpinnerBean> mDatas) {
        this.context = context;
        this.mDatas = mDatas;
    }
    @Override
    public int getCount() {
        return mDatas.size();
    }
    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int posiiton, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.spinner_item
                    holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.imageView2.setImageResource(mDatas.get(posiiton).getImageView());
        holder.name2.setText(mDatas.get(posiiton).getName());
        return convertView;
    }
    class ViewHolder {
        @BindView(R.id.spinner_item_2_imageView)
        ImageView imageView2;
        @BindView(R.id.spinner_item_2_name)
        TextView name2;
        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

     */
}