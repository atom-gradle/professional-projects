package com.qian.feather.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.*;

import com.qian.feather.adapter.PersonalInfoRecyclerViewAdapter;
import com.qian.feather.item.PersonalInfoItem;
import com.qian.feather.R;

import java.util.*;

public class PersonalInfoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PersonalInfoRecyclerViewAdapter personalInfoRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        if(getIntent().hasExtra("memoname")) {
            personalInfoRecyclerViewAdapter = new PersonalInfoRecyclerViewAdapter(this,initData(getIntent().getStringExtra("memoname")));
        } else {
            personalInfoRecyclerViewAdapter = new PersonalInfoRecyclerViewAdapter(this,initData());
        }

        recyclerView = findViewById(R.id.personal_info_RecyclerView);
        recyclerView.setAdapter(personalInfoRecyclerViewAdapter);
        recyclerView.setLayoutManager(lm);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,LinearLayoutManager.VERTICAL));

        Button button_return = findViewById(R.id.Button_return);
        button_return.setOnClickListener(view -> {
            finish();
        });

    }

    private List<PersonalInfoItem> initData() {
        List<PersonalInfoItem> itemList = new ArrayList<>();
        itemList.add(new PersonalInfoItem(1,"示例昵称"));
        itemList.add(new PersonalInfoItem(2,"设置备注和标签"));
        itemList.add(new PersonalInfoItem(2,"朋友权限"));
        itemList.add(new PersonalInfoItem(2,"朋友圈"));
        itemList.add(new PersonalInfoItem(2,"更多信息"));
        itemList.add(new PersonalInfoItem(3,"发消息"));
        itemList.add(new PersonalInfoItem(3,"语音通话"));
        return itemList;
    }
    private List<PersonalInfoItem> initData(String nickname) {
        List<PersonalInfoItem> itemList = new ArrayList<>();
        itemList.add(new PersonalInfoItem(1,nickname));
        itemList.add(new PersonalInfoItem(2,"设置备注和标签"));
        itemList.add(new PersonalInfoItem(2,"朋友权限"));
        itemList.add(new PersonalInfoItem(2,"朋友圈"));
        itemList.add(new PersonalInfoItem(2,"更多信息"));
        itemList.add(new PersonalInfoItem(3,"发消息"));
        itemList.add(new PersonalInfoItem(3,"语音通话"));
        return itemList;
    }
}