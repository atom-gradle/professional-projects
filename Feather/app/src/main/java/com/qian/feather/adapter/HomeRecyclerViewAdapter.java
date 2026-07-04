package com.qian.feather.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.qian.feather.activity.ChatActivity;
import com.qian.feather.item.ChatItem;
import com.qian.feather.OnRecyclerViewItemClickListener;
import com.qian.feather.R;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewAdapter.LinearViewHolder> {

    private Context contexts;
    private List<ChatItem> chatItemList;
    private LayoutInflater layoutInflater;
    private OnRecyclerViewItemClickListener listener;

    public void setOnRecyclerViewItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.listener = listener;
    }
    private final View.OnCreateContextMenuListener contextMenuListener = new View.OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu,View view,ContextMenu.ContextMenuInfo menuInfo) {
            //menu.add(Menu.NONE,R.menu.context_menu,Menu.NONE,"");
        }
    };

    public HomeRecyclerViewAdapter(Context context, List<ChatItem> chatItemList) {
        this.contexts = context;
        this.chatItemList = chatItemList;
        layoutInflater = LayoutInflater.from(contexts);
    }

    class LinearViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView_head;
        private TextView textView_chatObjName;
        private TextView textView_chatRecordOutline;
        private TextView textView_chatTime;
        private ConstraintLayout layout_ChatItemBackground;
        public LinearViewHolder(View itemView) {
            super(itemView);
            imageView_head = itemView.findViewById(R.id.ImageView_head);
            textView_chatObjName = itemView.findViewById(R.id.TextView_chatObject);
            textView_chatRecordOutline = itemView.findViewById(R.id.TextView_chatRecordOutline);
            textView_chatTime = itemView.findViewById(R.id.TextView_chatTime);
            layout_ChatItemBackground = itemView.findViewById(R.id.Layout_ChatItemBackground);
        }
    }

    @NonNull
    @Override
    public HomeRecyclerViewAdapter.LinearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contexts).inflate(R.layout.recyclerview_home,parent,false);
        LinearViewHolder linearViewHolder = new LinearViewHolder(view);
        return linearViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull HomeRecyclerViewAdapter.LinearViewHolder holder, int position) {
        ChatItem chatItem = chatItemList.get(position);
        if(chatItem.getIsTop()) {
            holder.layout_ChatItemBackground.setBackgroundResource(R.color.main);
            holder.textView_chatObjName.setBackgroundResource(R.color.main);
            holder.textView_chatRecordOutline.setBackgroundResource(R.color.main);
            holder.textView_chatTime.setBackgroundResource(R.color.main);
        } else {
            holder.layout_ChatItemBackground.setBackgroundResource(R.color.white);
            holder.textView_chatObjName.setBackgroundResource(R.color.white);
            holder.textView_chatRecordOutline.setBackgroundResource(R.color.white);
            holder.textView_chatTime.setBackgroundResource(R.color.white);
        }
        //holder.imageView_head.setImageResource(chatItem.getHeadSculpture());
        Glide.with(contexts)
                .load(chatItem.getHeadSculpture())
                .placeholder(R.drawable.feather)
                .transform(new RoundedCornersTransformation(12, 0, RoundedCornersTransformation.CornerType.ALL))
                //.override(100,100)
                .into(holder.imageView_head);
        holder.textView_chatObjName.setText(chatItem.getChatObjName());
        if(chatItem.getIsDraft()) {
            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append("[草稿]");
            builder.setSpan(new ForegroundColorSpan(Color.RED),0,4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(chatItem.getChatRecordOutline());
            holder.textView_chatRecordOutline.setText(builder);
        } else {
            holder.textView_chatRecordOutline.setText(chatItem.getChatRecordOutline());
        }

        holder.textView_chatTime.setText(chatItem.getChatTime());
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(contexts, ChatActivity.class);
            intent.putExtra("chatObjName",chatItem.getChatObjName());
            if(chatItem.getIsDraft()) {
                intent.putExtra("draft",chatItem.getChatRecordOutline());
            }
            contexts.startActivity(intent);
        });
        holder.itemView.setLongClickable(true);
        /*
        holder.itemView.setOnLongClickListener(view -> {
            view.setOnCreateContextMenuListener(contextMenuListener);
            return false;
        });
         */
        holder.itemView.setOnLongClickListener(new OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(int position) {
                System.out.println("ccc");
                if(listener != null) {
                    listener.onItemClick(position);
                }
            }
            @Override
            public boolean onLongClick(View v) {
                System.out.println("bbb");
                v.setOnCreateContextMenuListener(contextMenuListener);
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return chatItemList.size();
    }
    public void updateData(List<ChatItem> chatItemList) {
        this.chatItemList = chatItemList;
        notifyDataSetChanged();
    }
    public void upDateDraft(String whose_draft,String what_draft) {

    }
}
