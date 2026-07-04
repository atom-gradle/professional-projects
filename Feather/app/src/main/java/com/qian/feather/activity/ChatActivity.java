package com.qian.feather.activity;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.*;
import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.*;
import android.text.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.qian.feather.adapter.ShareAdapter;
import com.qian.feather.fragment.ImageChooserFragment;
import com.qian.feather.Helper.HttpHelper;
import com.qian.feather.item.Msg;
import com.qian.feather.item.ShareTarget;
import com.qian.feather.item.User;
import com.qian.feather.manager.ThreadManager;
import com.qian.feather.adapter.ChatRecyclerViewAdapter;
import com.qian.feather.NioClient;
import com.qian.feather.R;
import com.qian.feather.Utils;

import java.io.*;
import java.util.*;

import okhttp3.OkHttpClient;

/**
 * @author Qian atom-gradle
 */

public class ChatActivity extends AppCompatActivity {
    private RecyclerView chatRecyclerView;
    private List<Msg> msgList = new ArrayList<>();
    private ChatRecyclerViewAdapter chatRecyclerViewAdapter;
    private Handler handler;
    private static final String featherID = User.currentUser.getFeatherID();
    private String chatObjName;
    private EditText userInput;
    private Context context = this;
    private NotificationManager notificationManager;
    private ChatBroadcastReceiver chatBroadcastReceiver;
    private IntentFilter intentFilter;
    private Fragment imageChooserFragment;
    public static Boolean isDraft = false;
    private static NotificationChannel notificationChannel;
    private static volatile int notificationId = 0;
    private NioClient nioClient = null;
    private PopupWindow shareWindow;
    private WindowManager windowManager;
    private ItemTouchHelper touchHelper;
    private HttpHelper httpHelper = new HttpHelper(new OkHttpClient());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Feather_ChatActivity);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        userInput = findViewById(R.id.EditText_user_input);
        Intent intent0 = getIntent();
        chatObjName = intent0.getStringExtra("chatObjName");
        String draft = null;
        if(intent0.getStringExtra("draft") != null) {
            isDraft = true;
            draft = intent0.getStringExtra("draft");
            userInput.setText(draft);
            userInput.requestFocus();
        }

        Button returnToPrevious = findViewById(R.id.Button_return);
        Button chat_info = findViewById(R.id.Button_more_chat_info);
        Button add_more = findViewById(R.id.Button_add_more);
        Button send = findViewById(R.id.Button_send);
        Button emoji = findViewById(R.id.Button_emoji);
        TextView title = findViewById(R.id.TextView_name_of_chat_object);
        title.setText(chatObjName);
        add_more.setVisibility(View.VISIBLE);
        send.setVisibility(View.INVISIBLE);

        chatRecyclerView = findViewById(R.id.chat_RecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        initData();
        chatRecyclerViewAdapter = new ChatRecyclerViewAdapter(this,msgList);
        chatRecyclerView.setAdapter(chatRecyclerViewAdapter);
        chatRecyclerView.setLayoutManager(layoutManager);


        touchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.UP | ItemTouchHelper.DOWN,0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }
            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    showShareTargets(viewHolder.getAdapterPosition());
                }
                if(actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
                    Toast.makeText(context,"拖拽释放",Toast.LENGTH_SHORT).show();
                    if(shareWindow != null) {
                        if (shareWindow.isShowing()) {
                            shareWindow.dismiss();
                        }
                    }
                }
            }
        });
        touchHelper.attachToRecyclerView(chatRecyclerView);

        //register notification channel
        notificationChannel = Utils.createNotificationChannel("deafultMsgChannel","defaultMsgCahnnel");
        //更新chatRecycleView,UI组件更新
        handler = new Handler(Looper.myLooper()) {
            public void handleMessage(Message message) {
                chatRecyclerViewAdapter.updateData(msgList);
                if(message.obj == "msg_to_send") {
                    chatRecyclerView.scrollToPosition(msgList.size()-1);
                    send.setVisibility(View.INVISIBLE);
                    add_more.setVisibility(View.VISIBLE);
                    userInput.setText("");
                } else if(message.obj == "msg_received") {
                    Msg msg = msgList.get(msgList.size() - 1);
                    PendingIntent pendingIntent = PendingIntent.getActivity(ChatActivity.this,1,new Intent(ChatActivity.this, ChatActivity.class),PendingIntent.FLAG_MUTABLE);
                    Notification notification = Utils.createNotificationBuilder(ChatActivity.this,"defaultMsgCahnnel",msg,pendingIntent);
                    notificationManager.notify(notificationId,notification);
                    notificationId++;
                }
            }
        };

        ThreadManager.getInstance().execute(() -> {
            try {
                nioClient = NioClient.getInstance();
            } catch (IOException e) {
                Log.e("",e.toString());
                nioClient = null;
            }
        });
        if(nioClient != null) {
            nioClient.setMsgListener(new NioClient.MsgListener() {
                @Override
                public void onMsgReceived(Msg msg) {
                    if(msg != null) {
                        sendMessageToHandler(handler,"msg_received");
                        User.currentUser.msgList.add(msg);
                        msgList.add(msg);
                    }
                }
                @Override
                public void onConnectionStatusChanged(boolean connected) {
                    Log.d("","");
                }
                @Override
                public void onException(Exception exception) {
                    Log.e("caused from NioClient", exception.toString());
                }
            });
        }
        ThreadManager.getInstance().execute(() -> {
            try {
                NioClient.getInstance().handle();
            } catch (IOException e) {
                Log.d("ChatActivity#connect failed",e.toString());
            }
        });

        returnToPrevious.setOnClickListener(view -> {
            finish();
        });
        chat_info.setOnClickListener(view -> {
            Intent intent = new Intent(ChatActivity.this, ChatInfoActivity.class);
            intent.putExtra("chatObjName",chatObjName);
            Bundle bundle = new Bundle();
            bundle.putSerializable("msgList", (Serializable) msgList);
            intent.putExtra("msgListBundle",bundle);
            startActivity(intent);
        });
        add_more.setOnClickListener(view -> {
            checkPermission();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            imageChooserFragment = ImageChooserFragment.newInstance();
            ft.replace(R.id.ChatActivity_switchableFrameLayout,imageChooserFragment,null);
            ft.addToBackStack(null);
            ft.commit();
        });
        send.setOnClickListener(view -> {
            Msg msg = new Msg(featherID, chatObjName,userInput.getText().toString(),Msg.TEXT);
            ThreadManager.getInstance().execute(() -> {
                try {
                    NioClient.getInstance().sendMsg(msg);
                    User.currentUser.msgList.add(msg);
                    msgList.add(msg);
                } catch (Exception e) {
                    Looper.prepare();
                    Toast.makeText(context, "IO错误，已断开与服务器连接，请重试", Toast.LENGTH_SHORT).show();
                } finally {
                    User.currentUser.msgsToSend.add(msg);
                    sendMessageToHandler(handler, "msg_to_send");
                }
                //httpHelper.sendPostRequest(msg);
            });
        });
        userInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                if(charSequence.toString().equals("")) {
                    send.setVisibility(View.INVISIBLE);
                    add_more.setVisibility(View.VISIBLE);
                    userInput.setWidth(270);
                }
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                add_more.setVisibility(View.INVISIBLE);
                send.setVisibility(View.VISIBLE);
                userInput.setWidth(230);
                if(charSequence.toString().equals("")) {
                    send.setVisibility(View.INVISIBLE);
                    add_more.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                add_more.setVisibility(View.INVISIBLE);
                if("".equals(userInput.getText().toString())) {
                    userInput.setWidth(270);
                    send.setVisibility(View.INVISIBLE);
                    title.setText(chatObjName);
                    add_more.setVisibility(View.VISIBLE);
                }
            }
        });
        emoji.setOnClickListener(view -> {
            Msg msg = new Msg(featherID,chatObjName,"FastBoot(+_+)",Msg.TEXT);
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        //registerForContextMenu(chatRecyclerView);
        chatBroadcastReceiver = new ChatBroadcastReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("ImageChooserFragment.IMAGES_TO_SEND");
        intentFilter.addAction("ImageChooserFragment.FINISH_SELF");
        registerReceiver(chatBroadcastReceiver,intentFilter);
        if(notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        onMyBackPressed(true, new Runnable() {
            @Override
            public void run() {
                String draft = userInput.getText().toString();
                if(isDraft) {
                    Intent intent = new Intent("ChatAcitvity.UPDATE_CHATITEM_LIST");
                    intent.putExtra("whose_draft",chatObjName);
                    intent.putExtra("what_draft",draft);
                    sendBroadcast(intent);
                } else if(!"".equals(draft)) {
                    Intent intent = new Intent("ChatAcitvity.UPDATE_CHATITEM_LIST");
                    intent.putExtra("whose_draft",chatObjName);
                    intent.putExtra("what_draft",draft);
                    sendBroadcast(intent);
                }
                finish();
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onStop() {
        super.onStop();
        unregisterForContextMenu(chatRecyclerView);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    class ChatBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!(context instanceof Activity) || intent == null || intent.getAction() == null) {
                return;
            }
            if(("ImageChooserFragment.IMAGES_TO_SEND").equals(intent.getAction())) {
                List<String> imagesToSend = intent.getStringArrayListExtra("imagesToSend");
                for(String path : imagesToSend) {
                    Msg msg = new Msg(User.currentUser.getName(), chatObjName,path,Msg.FILE);
                }
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.remove(imageChooserFragment);
                fragmentTransaction.commit();
            } else if("ImageChooserFragment.FINISH_SELF".equals(intent.getAction())) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.remove(imageChooserFragment);
                fragmentTransaction.commit();
            } else if("DragSharePanel.SHOW".equals(intent.getAction())) {
                Msg msg = intent.getParcelableExtra("msg_to_share");
                Message message = new Message();
                message.what = 111;
                message.obj = msg;
                handler.sendMessage(message);
            }
        }
    }
    private void sendMessageToHandler(Handler handler,String obj) {
        Message message = new Message();
        message.obj = obj;
        handler.sendMessage(message);
    }
    private void initData() {
        Msg msg_sys_info = new Msg("初晨",chatObjName,"你好",Msg.SYS_INFO);
        msgList.add(msg_sys_info);
    }
    private void checkPermission() {
        final String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA};
        final int REQUEST_EXTERNAL_STORAGE = 1;
        for(int i = 0;i < PERMISSIONS_STORAGE.length;i++) {
            if(ActivityCompat.checkSelfPermission(this,PERMISSIONS_STORAGE[i]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ChatActivity.this, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        }
    }
    public void onMyBackPressed(Boolean isEnable,final Runnable callback) {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(isEnable) {
            @Override
            public void handleOnBackPressed() {
                callback.run();
            }
        });
    }
    private void showShareTargets(int position) {
        // 获取被拖拽的消息
        Msg msg = msgList.get(position);

        // 创建并显示分享面板PopupWindow
        View sharePanel = LayoutInflater.from(this).inflate(R.layout.share_panel, null);
        shareWindow = new PopupWindow(sharePanel,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        shareWindow.setOutsideTouchable(true);
        shareWindow.setTouchable(true);
        shareWindow.setFocusable(true);

        // 设置分享面板位置（跟随拖拽位置）
        int[] location = new int[2];
        shareWindow.showAtLocation(chatRecyclerView, Gravity.NO_GRAVITY,
                location[0], location[1] - sharePanel.getHeight());

        // 设置拖拽监听
        GridView gridView = sharePanel.findViewById(R.id.app_grid);
        ShareAdapter shareAdapter = new ShareAdapter(this, getShareApps(msg));
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
                            Toast.makeText(context,"aaa",Toast.LENGTH_SHORT).show();
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
        Msg.Builder builder = new Msg.Builder().addFrom("a").addTo("a");

        PackageManager pm = getPackageManager();
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
    private void shareMessageToApp(Msg msg, ShareTarget app) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        if(msg.getType() == Msg.TEXT) {
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, msg.getContent());
        } else if(msg.getType() == Msg.FILE) {

        }
        intent.setClassName(app.packageName, app.className);
        startActivity(intent);
    }
}
