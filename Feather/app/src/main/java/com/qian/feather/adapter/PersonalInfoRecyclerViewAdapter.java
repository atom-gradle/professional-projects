package com.qian.feather.adapter;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qian.feather.activity.ChatActivity;
import com.qian.feather.item.PersonalInfoItem;
import com.qian.feather.R;

import java.util.List;

public class PersonalInfoRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<PersonalInfoItem> personalInfoItemList;
    private LayoutInflater layoutInflater;
    public PersonalInfoRecyclerViewAdapter(Context context,List<PersonalInfoItem> itemList) {
        this.context = context;
        this.personalInfoItemList = itemList;
        layoutInflater = LayoutInflater.from(context);
    }
    class PersonalInfo1ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView showName;
        private TextView text2;
        private TextView text3;
        public PersonalInfo1ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.TextView_name);
            text2 = itemView.findViewById(R.id.TextView_showAccount);
        }
    }

    class PersonalInfo2ViewHolder extends RecyclerView.ViewHolder {
        private TextView itemDescription;
        private Button rightArrow;
        public PersonalInfo2ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemDescription = itemView.findViewById(R.id.TextView_itemDescription);
            rightArrow = itemView.findViewById(R.id.Button_rightArrow);
        }
    }
    class PersonalInfo3ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ImageView_head;
        private TextView itemDescription;
        public PersonalInfo3ViewHolder(@NonNull View itemView) {
            super(itemView);
            ImageView_head = itemView.findViewById(R.id.ImageView_head);
            itemDescription = itemView.findViewById(R.id.TextView_itemDescription);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 1) {
            View view = layoutInflater.inflate(R.layout.recyclerview_personal_detail_header,parent,false);
            return new PersonalInfo1ViewHolder(view);
        } else if(viewType == 2) {
            View view = layoutInflater.inflate(R.layout.universal_text_arrow_recycleritem,parent,false);
            return new PersonalInfo2ViewHolder(view);
        } else if(viewType == 3) {
            View view = layoutInflater.inflate(R.layout.universal_image_text_recyclerviewitem,parent,false);
            return new PersonalInfo3ViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(personalInfoItemList == null) {
            return;
        }
        PersonalInfoItem personalInfoItem = personalInfoItemList.get(position);
        if (holder instanceof PersonalInfo1ViewHolder) {
            ((PersonalInfo1ViewHolder) holder).showName.setText(personalInfoItem.getItemDescription());
            ((PersonalInfo1ViewHolder) holder).text2.setText(personalInfoItem.getItemDescription());
            ((PersonalInfo1ViewHolder) holder).text3.setText("中国");
        } else if(holder instanceof PersonalInfo2ViewHolder) {
            ((PersonalInfo2ViewHolder) holder).itemDescription.setText(personalInfoItem.getItemDescription());
        } else if (holder instanceof PersonalInfo3ViewHolder) {
            ((PersonalInfo3ViewHolder) holder).itemDescription.setText(personalInfoItem.getItemDescription());
            ((PersonalInfo3ViewHolder) holder).itemView.setOnClickListener(view -> {
                if("发消息".equals(personalInfoItem.getItemDescription())) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("chatObjName","初晨");
                    context.startActivity(intent);
                }
            });
        }
    }
    @Override
    public int getItemViewType(int position) {
        switch (personalInfoItemList.get(position).getType()) {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
        }
        return 1;
    }
    @Override
    public int getItemCount() {
        return personalInfoItemList.size();
    }
    public void updateData(List<PersonalInfoItem> itemList) {
        this.personalInfoItemList = itemList;
        notifyDataSetChanged();
    }
}