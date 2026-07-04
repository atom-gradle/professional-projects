package com.qian.feather.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.qian.feather.item.SelfItem;
import com.qian.feather.R;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class SelfRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<SelfItem> itemList;
    private LayoutInflater layoutInflater;
    public SelfRecyclerViewAdapter(Context context,List<SelfItem> selfItemList) {
        this.context = context;
        this.itemList = selfItemList;
        layoutInflater = LayoutInflater.from(context);
    }
    class Header extends RecyclerView.ViewHolder {
        private ImageView imageview_head;
        private TextView textview_showMemoName;
        private TextView textview_showAccount;
        public Header(@NonNull View itemView) {
            super(itemView);
            imageview_head = itemView.findViewById(R.id.ImageView_head);
            textview_showMemoName = itemView.findViewById(R.id.TextView_showMemoName);
            textview_showAccount = itemView.findViewById(R.id.TextView_showAccount);
        }
    }

    class Body extends RecyclerView.ViewHolder {
        private TextView textview_itemDescription;
        private Button rightArrow;
        public Body(@NonNull View itemView) {
            super(itemView);
            textview_itemDescription = itemView.findViewById(R.id.TextView_itemDescription);
            rightArrow = itemView.findViewById(R.id.Button_rightArrow);
        }
    }

    class FriendCircle extends RecyclerView.ViewHolder {
        private TextView textview_itemDescription;
        private Button rightArrow;
        public FriendCircle(@NonNull View itemView) {
            super(itemView);
            textview_itemDescription = itemView.findViewById(R.id.TextView_itemDescription);
            rightArrow = itemView.findViewById(R.id.Button_rightArrow);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 1) {
            View view = layoutInflater.inflate(R.layout.recyclerview_personal_detail_header,parent,false);
            return new Header(view);
        } else if(viewType == 2) {
            View view = layoutInflater.inflate(R.layout.universal_text_arrow_recycleritem,parent,false);
            return new Body(view);
        } else if(viewType == 3) {
            //View view = layoutInflater
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SelfItem item = itemList.get(position);
        if (holder instanceof Header) {
            Glide.with(context).load(R.drawable.chocolate_bear)
                    .transform(new RoundedCornersTransformation(12, 0, RoundedCornersTransformation.CornerType.ALL))
                    .into(((Header) holder).imageview_head);
            ((Header) holder).textview_showMemoName.setText(item.getUser().getName());
            ((Header) holder).textview_showAccount.setText(item.getUser().getFeatherID());
        } else if(holder instanceof Body) {
            ((Body)holder).textview_itemDescription.setText(item.getItemDescription());
        } else if(holder instanceof FriendCircle) {

        }
    }

    @Override
    public int getItemViewType(int position) {
        SelfItem selfItem = itemList.get(position);
        return selfItem.getType();
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
    public void updateData(List<SelfItem> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }
}