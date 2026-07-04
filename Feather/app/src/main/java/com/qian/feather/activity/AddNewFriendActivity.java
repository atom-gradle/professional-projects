package com.qian.feather.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.os.Bundle;

import com.qian.feather.R;

public class AddNewFriendActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_friend);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        SearchView searchForUser = findViewById(R.id.SearchView_searchForUser);
        searchForUser.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                /*
                CompletableFuture<Msg> future = CompletableFuture.supplyAsync(() -> {
                    Msg msg = new Msg(User.currentUser.getName(),"SERVER","QUERY:SearchForUser:Account:"+query,Msg.SYS_INFO);
                    NioClient.getInstance().sendMsg(msg);
                });
                 */
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }
}