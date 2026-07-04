package com.qian.feather.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.*;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qian.feather.Helper.ImageHelper;
import com.qian.feather.Helper.MsgToImageHelper;
import com.qian.feather.IOUtils.MsgIO;
import com.qian.feather.item.Msg;
import com.qian.feather.item.Setting;
import com.qian.feather.R;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ChatInfoRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Setting> itemList;
    private LayoutInflater layoutInflater;
    private String chatObjName;
    private List<Msg> msgList;
    public static final int TYPE_ARROW = 1;
    public static final int TYPE_SLIDER = 2;
    public static final int TYPE_CHECKBOX = 3;
    public ChatInfoRecyclerViewAdapter(Context context,List<Setting> itemList,String chatObjName) {
        this.context = context;
        this.itemList = itemList;
        this.chatObjName = chatObjName;
        layoutInflater = LayoutInflater.from(context);
    }
    public ChatInfoRecyclerViewAdapter(Context context,List<Setting> itemList,String chatObjName,List<Msg> msgList) {
        this.context = context;
        this.itemList = itemList;
        this.chatObjName = chatObjName;
        this.msgList = msgList;
        layoutInflater = LayoutInflater.from(context);
    }
    class ArrowSettingItem extends RecyclerView.ViewHolder {
        private TextView itemDescription;
        public ArrowSettingItem(@NonNull View itemView) {
            super(itemView);
            itemDescription = itemView.findViewById(R.id.TextView_itemDescription);
        }
    }
    class SwitchSettingItem extends RecyclerView.ViewHolder {
        private TextView itemDescription;
        private Switch switch_;
        public SwitchSettingItem(@NonNull View itemView) {
            super(itemView);
            itemDescription = itemView.findViewById(R.id.TextView_itemDescription);
            switch_ = itemView.findViewById(R.id.Switch);
        }
    }
    class CheckBoxItem extends RecyclerView.ViewHolder {
        private CheckBox checkBox1;
        private CheckBox checkBox2;
        private CheckBox checkBox3;
        public CheckBoxItem(@NonNull View itemView) {
            super(itemView);
            checkBox1 = itemView.findViewById(R.id.CheckBox_1);
            checkBox2 = itemView.findViewById(R.id.CheckBox_2);
            checkBox3 = itemView.findViewById(R.id.CheckBox_3);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 1) {
            View view = layoutInflater.inflate(R.layout.universal_text_arrow_recycleritem,parent,false);
            return new ArrowSettingItem(view);
        } else if(viewType == 2) {
            View view = layoutInflater.inflate(R.layout.universal_text_slider_recycleritem,parent,false);
            return new SwitchSettingItem(view);
        } else if(viewType == 3) {
            View view = layoutInflater.inflate(R.layout.universal_checkbox_recycleritem,parent,false);
            return new CheckBoxItem(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String itemDescription = itemList.get(position).getItemName();
        if(holder instanceof ArrowSettingItem) {
            ((ArrowSettingItem) holder).itemDescription.setText(itemDescription);
            holder.itemView.setOnClickListener(view -> {
                if("清空聊天记录".equals(itemDescription)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("确定删除和"+chatObjName+"的聊天记录吗？");
                    builder.setPositiveButton("清空", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(MsgIO.removeMsgFromNative(context,chatObjName)) {
                                Toast.makeText(context,"已清空聊天记录",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else if("导出聊天记录为图片".equals(itemDescription)) {
                    if(msgList.size() < 1) {
                        Toast.makeText(context,"当前没有聊天记录可以导出",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                        Bitmap bitmap = MsgToImageHelper.convertMsgListToImage(msgList);
                        String filePath = "";
                        try {
                            filePath = ImageHelper.saveBitmapToJpg(bitmap);
                        } catch (IOException e) {
                            Toast.makeText(context,"导出失败",Toast.LENGTH_SHORT).show();
                        }
                        return filePath;
                    });
                    String filePath = null;
                    try {
                        filePath = future.get();
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        Toast.makeText(context,"isInterrupted",Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(context,"导出至："+filePath,Toast.LENGTH_SHORT).show();
                }
            });
        } else if(holder instanceof SwitchSettingItem) {
            ((SwitchSettingItem) holder).itemDescription.setText(itemDescription);
            ((SwitchSettingItem) holder).switch_.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if(isChecked) {

                    } else {

                    }
                }
            });
        }

    }
    @Override
    public int getItemViewType(int position) {
        Setting setting = itemList.get(position);
        return setting.getType();
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
    public void updateData(List<Setting> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }
}