package com.qian.feather.adapter;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qian.feather.item.Device;
import com.qian.feather.R;

import java.util.List;

public class DeviceRecyclerViewAdapter extends RecyclerView.Adapter<DeviceRecyclerViewAdapter.LinearViewHolder> {

    private Context contexts;
    private List<Device> deviceList;
    private LayoutInflater layoutInflater;
    private final View.OnCreateContextMenuListener contextMenuListener = new View.OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu,View view,ContextMenu.ContextMenuInfo menuInfo) {
            //menu.add(Menu.NONE,R.menu.context_menu,Menu.NONE,"");
        }
    };

    public DeviceRecyclerViewAdapter(Context context, List<Device> deviceList) {
        this.contexts = context;
        this.deviceList = deviceList;
        layoutInflater = LayoutInflater.from(contexts);
    }

    class LinearViewHolder extends RecyclerView.ViewHolder {
        private TextView textView_name;
        private TextView textView_ip;
        public LinearViewHolder(View itemView) {
            super(itemView);
            textView_name = itemView.findViewById(R.id.TextView_showDeviceName);
            textView_ip = itemView.findViewById(R.id.TextView_showDeviceIP);
        }
    }

    @NonNull
    @Override
    public DeviceRecyclerViewAdapter.LinearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contexts).inflate(R.layout.recyclerview_home,parent,false);
        LinearViewHolder linearViewHolder = new LinearViewHolder(view);
        return linearViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceRecyclerViewAdapter.LinearViewHolder holder, int position) {
        Device device = deviceList.get(position);
        holder.textView_name.setText(device.getName());
        holder.textView_ip.setText(device.getIp());
        //holder.imageView_head.setImageResource(chatItem.getHeadSculpture());
        /*
        Glide.with(contexts)
                .load(chatItem.getHeadSculpture())
                .placeholder(R.drawable.feather)
                .transform(new RoundedCornersTransformation(12, 0, RoundedCornersTransformation.CornerType.ALL))
                //.override(100,100)
                .into(holder.imageView_head);
        holder.textView_chatObjName.setText(chatItem.getChatObjName());

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

         */
        /*
        holder.itemView.setOnLongClickListener(view -> {
            view.setOnCreateContextMenuListener(contextMenuListener);
            return false;
        });
         */

    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }
    public void updateData(List<Device> chatItemList) {
        this.deviceList = deviceList;
        notifyDataSetChanged();
    }
    public void upDateDraft(String whose_draft,String what_draft) {

    }
}
