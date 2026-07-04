package com.qian.feather.fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.Toast;

import com.qian.feather.adapter.HomeRecyclerViewAdapter;
import com.qian.feather.IOUtils.ChatItemIO;
import com.qian.feather.item.ChatItem;
import com.qian.feather.OnRecyclerViewItemClickListener;
import com.qian.feather.item.User;
import com.qian.feather.Utils;
import com.qian.feather.R;

import java.util.*;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private Handler handler;
    private RecyclerView recyclerView;
    private static List<ChatItem> chatItemList;
    private Context context;
    HomeRecyclerViewAdapter homeRecyclerViewAdapter;
    private int unreadMsgNumber = 0;
    IntentFilter intentFilter;
    HomeFragmentBroadcastReceiver homeFragmentBroadcastReceiver = new HomeFragmentBroadcastReceiver();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    @NonNull
    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        initData();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(Looper.myLooper()) {
            public void handleMessage(Message message) {
                if(message.obj == "unreadMsgNumber++") {
                    Intent intent = new Intent("com.example.HomeFragment.UNREAD_MSG_NUMBER++");
                    context.sendBroadcast(intent);
                } else if(message.obj == "updateChatItem") {
                    homeRecyclerViewAdapter.updateData(chatItemList);
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        context = view.getContext();
        recyclerView = view.findViewById(R.id.home_RecyclerView);
        LinearLayoutManager lm = new LinearLayoutManager(context);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        homeRecyclerViewAdapter = new HomeRecyclerViewAdapter(context,chatItemList);
        homeRecyclerViewAdapter.setOnRecyclerViewItemClickListener(new OnRecyclerViewItemClickListener() {
            @Override
            public boolean onLongClick(View v) {
                System.out.println("aaa");
                return false;
            }
            @Override
            public void onItemClick(int position) {
                System.out.println(position);
                Toast.makeText(context,String.format("%d",position),Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(homeRecyclerViewAdapter);
        recyclerView.setLayoutManager(lm);
        recyclerView.addItemDecoration(new DividerItemDecoration(context,LinearLayoutManager.VERTICAL));
        registerForContextMenu(recyclerView);
        return view;
    }
    @Override
    public void onStart() {
        super.onStart();
        Log.i("at HomeFragment:","onStart()");
        registerForContextMenu(recyclerView);
        intentFilter = new IntentFilter("ChatAcitvity.UPDATE_CHATITEM_LIST");
        context.registerReceiver(homeFragmentBroadcastReceiver,intentFilter);
        System.out.println("has registered");
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.i("at HomeFragment:","onResume()");
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.i("at HomeFragment:","onStop()");
        unregisterForContextMenu(recyclerView);
        //context.unregisterReceiver(homeFragmentBroadcastReceiver);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        // 当 上下文菜单 创建时调用
        // 这里可以根据menuInfo来决定显示哪些菜单项
        //menu.add(菜单分组，菜单项id，菜单编号，标题).setIcon(R.drawable.xxx);
        super.onCreateContextMenu(menu,view,menuInfo);
        menu.add(0, 0, 0, "标为未读");
        menu.add(1, 1, 1, "置顶该聊天");
        menu.add(Menu.NONE, 2, Menu.NONE, "不显示该聊天");
        menu.add(Menu.NONE, 3, Menu.NONE, "删除该聊天");
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ContextMenu.ContextMenuInfo contextMenuInfo = item.getMenuInfo();
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) contextMenuInfo;
        //int position = menuInfo.position;
        //System.out.println("at homeFragment:position is:"+position);
        try {
           System.out.println(menuInfo.position);
        } catch (Exception e) {
            System.err.println("mistake");
        }
        switch (item.getItemId()) {
            case 0:
                chatItemList.get(chatItemList.size()-1).setHasRead(false);
                unreadMsgNumber++;
                Utils.sendMessageToHandler(handler,"unreadMsgNumber++");
                return true;
            case 1:
                ChatItem chatItem = chatItemList.get(chatItemList.size() - 1);
                chatItem.flipIsTop();
                Collections.sort(chatItemList);
                Utils.sendMessageToHandler(handler,"updateChatItem");
                return true;
            case 2:
                if(chatItemList.size() > 0)
                    chatItemList.remove(chatItemList.size() - 1);
                Utils.sendMessageToHandler(handler,"updateChatItem");
                return true;
            case 3:
                if(chatItemList.size() > 0)
                    chatItemList.remove(chatItemList.size() - 1);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("确定删除？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Utils.sendMessageToHandler(handler,"updateChatItem");
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
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    private void initData() {
        //TODO load chatItemList from local
        //chatItemList = ChatItemIO.recallChatItemFromNative(context, User.currentUser);
        chatItemList = new ArrayList<>(20);
        chatItemList.add(new ChatItem(R.drawable.feather,"Feather官方","Feather新版本更新内容~","上午8:00",true,true));
        chatItemList.add(new ChatItem(R.drawable.feather,"Feather传输助手","文件请发给我~","上午8:00",true,true));
        chatItemList.add(new ChatItem(R.drawable.kenan,"江户川柯南","真相永远只有一个!","下午5:47",false,true));
        chatItemList.add(new ChatItem(R.drawable.xiaowulang,"毛利小五郎","我怎么又睡着了...","下午2:12",false,true));
        chatItemList.add(new ChatItem(R.drawable.xiaolan,"毛利兰","柯南，一起出去玩吗","上午11:35",false,true));
        chatItemList.add(new ChatItem(R.drawable.xiaoai,"宫野志保","我可以确定就是她们，就是这种感觉","晚上11:35",false,true));
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        ChatItemIO.saveChatItemsToNative(context,chatItemList, User.currentUser);
    }
    class HomeFragmentBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == "ChatAcitvity.UPDATE_CHATITEM_LIST") {
                String whose_draft = intent.getStringExtra("whose_draft");
                String what_draft = intent.getStringExtra("what_draft");
                for(ChatItem chatItem : chatItemList) {
                    if(chatItem.getChatObjName().equals(whose_draft)) {
                        chatItem.setChatRecordOutline(what_draft);
                        chatItem.setIsDraft(true);
                        break;
                    }
                }
                homeRecyclerViewAdapter.updateData(chatItemList);
            }
        }
    }
}