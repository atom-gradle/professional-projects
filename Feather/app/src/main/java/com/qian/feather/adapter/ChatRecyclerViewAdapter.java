package com.qian.feather.adapter;


import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.ContextMenu;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.qian.feather.item.Msg;
import com.qian.feather.item.ShareTarget;
import com.qian.feather.activity.PersonalInfoActivity;
import com.qian.feather.item.FixedMsg;
import com.qian.feather.R;
import com.qian.feather.item.User;

import java.util.ArrayList;
import java.util.List;

public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Msg> msgList;
    private LayoutInflater layoutInflater;
    private static final int TYPE_SYS_INFO = 0;
    private static final int TYPE_TEXT_SELF = 1;
    private static final int TYPE_TEXT_OPPO = 2;
    private static final int TYPE_IMAGE_SELF = 3;
    private static final int TYPE_IMAGE_OPPO = 4;
    private final View.OnCreateContextMenuListener contextMenuListener = new View.OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu,View view,ContextMenu.ContextMenuInfo menuInfo) {
            //menu.add(Menu.NONE,R.menu.context_menu,Menu.NONE,"");
        }
    };

    public ChatRecyclerViewAdapter(Context context,List<Msg> msgList) {
        this.context = context;
        this.msgList = msgList;
        layoutInflater = LayoutInflater.from(context);
    }
    public ChatRecyclerViewAdapter(Context context,List<Msg> msgList,User oppoUser) {
        this.context = context;
        this.msgList = msgList;
        layoutInflater = LayoutInflater.from(context);
    }

    class TextSelfViewHolder extends RecyclerView.ViewHolder {
        private ImageView head;
        private TextView text;
        private TextView textview_showTime;
        public TextSelfViewHolder(@NonNull View itemView) {
            super(itemView);
            head = itemView.findViewById(R.id.ImageView_head);
            text = itemView.findViewById(R.id.TextView_text);
            textview_showTime = itemView.findViewById(R.id.TextView_showTime);
        }
    }
    class TextOppoViewHolder extends RecyclerView.ViewHolder {
        private ImageView head;
        private TextView text;
        private TextView textview_showTime;
        public TextOppoViewHolder(@NonNull View itemView) {
            super(itemView);
            head = itemView.findViewById(R.id.ImageView_head);
            text = itemView.findViewById(R.id.TextView_text);
            textview_showTime = itemView.findViewById(R.id.TextView_showTime);
        }
    }
    class ImageSelfViewHolder extends RecyclerView.ViewHolder {
        private ImageView head;
        private ImageView image;
        public ImageSelfViewHolder(@NonNull View itemView) {
            super(itemView);
            head = itemView.findViewById(R.id.ImageView_head);
            image = itemView.findViewById(R.id.ImageView_image);
        }
    }
    class ImageOppoViewHolder extends RecyclerView.ViewHolder {
        private ImageView head;
        private ImageView image;
        public ImageOppoViewHolder(@NonNull View itemView) {
            super(itemView);
            head = itemView.findViewById(R.id.ImageView_head);
            image = itemView.findViewById(R.id.ImageView_image);
        }
    }
    class SysInfoViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout Layout_showSysInfo;
        private TextView textView_showSysInfo;
        public SysInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            Layout_showSysInfo = itemView.findViewById(R.id.Layout_showSysInfo);
            textView_showSysInfo = itemView.findViewById(R.id.TextView_showSysInfo);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 1) {
            View view = layoutInflater.inflate(R.layout.recyclerview_chat_text_self,parent,false);
            return new TextSelfViewHolder(view);
        } else if(viewType == 2) {
            View view = layoutInflater.inflate(R.layout.recyclerview_chat_text_oppo,parent,false);
            return new TextOppoViewHolder(view);
        } else if(viewType == 3) {
            View view = layoutInflater.inflate(R.layout.recyclerview_chat_image_self,parent,false);
            return new ImageSelfViewHolder(view);
        } else if(viewType == 4) {
            View view = layoutInflater.inflate(R.layout.recyclerview_chat_image_oppo,parent,false);
            return new ImageOppoViewHolder(view);
        } else if(viewType == 0) {
            View view = layoutInflater.inflate(R.layout.recyclerview_chat_sys_info,parent,false);
            return new SysInfoViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Msg msg = msgList.get(position);
        if (holder instanceof TextSelfViewHolder) {
            ((TextSelfViewHolder) holder).text.setText(msg.getContent());
            ((TextSelfViewHolder) holder).text.setLongClickable(true);
            ((TextSelfViewHolder) holder).text.setOnLongClickListener(view -> {
                //view.setOnCreateContextMenuListener(contextMenuListener);
                Intent intent = new Intent("DragSharePanel.SHOW");
                intent.putExtra("msg_to_share",msg);
                return false;
            });
            ((TextSelfViewHolder) holder).head.setOnClickListener(view -> {
                Intent intent = new Intent(context, PersonalInfoActivity.class);
                intent.putExtra("memoname","");
                context.startActivity(intent);
            });
            ((TextSelfViewHolder) holder).textview_showTime.setText(msg.getWhen());
        } else if (holder instanceof TextOppoViewHolder) {
            ((TextOppoViewHolder) holder).text.setText(msg.getContent());
            ((TextOppoViewHolder) holder).text.setLongClickable(true);
            /*
            ((TextOppoViewHolder) holder).text.setOnLongClickListener(view -> {
                    view.setOnCreateContextMenuListener(contextMenuListener);
                    return false;
            });
             */
            ((TextOppoViewHolder) holder).head.setOnClickListener(view -> {
                    Intent intent = new Intent(context, PersonalInfoActivity.class);
                    intent.putExtra("","");
                    context.startActivity(intent);
            });
            ((TextOppoViewHolder) holder).textview_showTime.setText(msg.getWhen());
        } else if(holder instanceof ImageSelfViewHolder) {
            //Glide.with(context).load().placeHolder(R.drawable.file_place_holder).into(((ImageSelfViewHolder) holder).image);
            ((ImageSelfViewHolder) holder).image.setImageBitmap(BitmapFactory.decodeFile(msg.getContent()));
            ((ImageSelfViewHolder) holder).head.setOnClickListener(view -> {
                    Intent intent = new Intent(context, PersonalInfoActivity.class);
                    intent.putExtra("memoname", User.currentUser.getName());
                    context.startActivity(intent);
            });
            ((ImageSelfViewHolder) holder).image.setOnClickListener(view -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/*");
                //intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(msgList.get(position).getStringContent()));
                context.startActivity(Intent.createChooser(intent,"分享"));
            });
        } else if(holder instanceof ImageOppoViewHolder) {
            ((ImageOppoViewHolder) holder).image.setImageBitmap(BitmapFactory.decodeFile(msg.getContent()));
            ((ImageOppoViewHolder) holder).head.setOnClickListener(view -> {
                    Intent intent = new Intent(context, PersonalInfoActivity.class);
                    intent.putExtra("memoname","chatObj");
                    context.startActivity(intent);
            });
        } else if(holder instanceof SysInfoViewHolder) {
            ((SysInfoViewHolder)holder).textView_showSysInfo.setText(msg.getContent());
        }
        // 在RecyclerView的ViewHolder中设置长按启动拖拽
        holder.itemView.setOnLongClickListener(v -> {
            ClipData.Item item = new ClipData.Item("msg_" + position);
            ClipData data = new ClipData("MSG", new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);

            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                v.startDragAndDrop(data, shadowBuilder, null, 0);
            } else {
                v.startDrag(data, shadowBuilder, null, 0);
            }

            // 显示分享目标面板
            showShareTargets(position, v);
            Toast.makeText(context,"a",Toast.LENGTH_SHORT).show();
            return true;
        });
    }
    private void showShareTargets(int position, View dragView) {
        // 获取被拖拽的消息
        Msg msg = msgList.get(position);

        // 创建并显示分享面板PopupWindow
        View sharePanel = LayoutInflater.from(context).inflate(R.layout.share_panel, null);
        PopupWindow shareWindow = new PopupWindow(sharePanel,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        shareWindow.setOutsideTouchable(true);
        shareWindow.setTouchable(true);
        shareWindow.setFocusable(true);

        // 设置分享面板位置（跟随拖拽位置）
        int[] location = new int[2];
        dragView.getLocationOnScreen(location);
        shareWindow.showAtLocation(null, Gravity.NO_GRAVITY,
                location[0], location[1] - sharePanel.getHeight());

        // 设置拖拽监听
        GridView gridView = sharePanel.findViewById(R.id.app_grid);
        ShareAdapter shareAdapter = new ShareAdapter(context, getShareApps(msg));
        gridView.setAdapter(shareAdapter);

        // 设置拖拽目标
        gridView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DROP:
                        // 获取拖拽位置对应的应用
                        float x = event.getX();
                        float y = event.getY();
                        int pos = gridView.pointToPosition((int)x, (int)y);

                        if (pos != GridView.INVALID_POSITION) {
                            ShareTarget app = shareAdapter.getItem(pos);
                            shareMessageToApp(msg, app);
                        }
                        shareWindow.dismiss();
                        return true;

                    case DragEvent.ACTION_DRAG_ENDED:
                        if (!event.getResult()) {
                            shareWindow.dismiss();
                        }
                        return true;
                }
                return true;
            }
        });
    }
    private void shareMessageToApp(Msg msg, ShareTarget app) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        if(msg.getType() == Msg.TEXT) {
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, msg.getContent());
        } else if(msg.getType() == Msg.FILE) {

        }
        intent.setClassName(app.packageName, app.className);
        context.startActivity(intent);
    }

    private List<ShareTarget> getShareApps(Msg msg) {
        // 获取可以分享消息的应用列表
        List<ShareTarget> apps = new ArrayList<>(20);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if(msg.getType() == Msg.TEXT) {
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, msg.getContent());
        } else if(msg.getType() == Msg.FILE) {
            shareIntent.setType("");
            shareIntent.putExtra(Intent.EXTRA_STREAM,msg.getContent());
        }

        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(shareIntent, 0);
        for (ResolveInfo info : activities) {
            apps.add(new ShareTarget(
                    info.loadIcon(pm),
                    info.loadLabel(pm).toString(),
                    info.activityInfo.packageName,
                    info.activityInfo.name
            ));
        }
        return apps;
    }
    @Override
    public int getItemViewType(int position) {
        Msg msg = msgList.get(position);
        if(msg.getState() == Msg.TO_SEND) {
            if(msg.getType() == FixedMsg.TYPE_TEXT) {
                return TYPE_TEXT_SELF;
            } else if(msg.getType() == FixedMsg.TYPE_IMAGE) {
                return TYPE_IMAGE_SELF;
            }
        } else if(msg.getState() == Msg.TO_RECEIVE) {
            if(msg.getType() == FixedMsg.TYPE_TEXT) {
                return TYPE_TEXT_OPPO;
            } else if(msg.getType() == FixedMsg.TYPE_IMAGE) {
                return TYPE_IMAGE_OPPO;
            }
        } else {
            return TYPE_SYS_INFO;
        }
        return 1;
    }
    @Override
    public int getItemCount() {
        return msgList.size();
    }
    public void updateData(List<Msg> msgList) {
        this.msgList = msgList;
        notifyDataSetChanged();
    }
}