package com.qian.feather.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import com.qian.feather.adapter.ChatInfoRecyclerViewAdapter;
import com.qian.feather.item.Msg;
import com.qian.feather.item.Setting;
import com.qian.feather.R;

import java.util.ArrayList;
import java.util.List;

public class ChatInfoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatInfoRecyclerViewAdapter chatInfoRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.universal_info_page_model);

        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Button button_return = findViewById(R.id.Button_returnToPrevious);
        button_return.setOnClickListener(view -> {
            finish();
        });

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        Intent intent = getIntent();
        chatInfoRecyclerViewAdapter = new ChatInfoRecyclerViewAdapter(this,initData(),intent.getStringExtra("chatObjName"),(List<Msg>)intent.getBundleExtra("msgListBundle").getSerializable("msgList"));

        recyclerView = findViewById(R.id.RecyclerView);
        recyclerView.setAdapter(chatInfoRecyclerViewAdapter);
        recyclerView.setLayoutManager(lm);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,LinearLayoutManager.VERTICAL));
    }

    private List<Setting> initData() {
        List<Setting> list = new ArrayList<>();
        list.add(new Setting("查找聊天记录",Setting.TYPE_ARROW));
        list.add(new Setting("导出聊天记录为图片",Setting.TYPE_ARROW));
        list.add(new Setting("",Setting.TYPE_CHECKBOX));
        list.add(new Setting("消息免打扰",Setting.TYPE_SLIDER));
        list.add(new Setting("置顶聊天",Setting.TYPE_SLIDER));
        list.add(new Setting("设置当前聊天背景",Setting.TYPE_ARROW));
        list.add(new Setting("清空聊天记录",Setting.TYPE_ARROW));
        list.add(new Setting("投诉",Setting.TYPE_ARROW));
        return list;
    }
}
