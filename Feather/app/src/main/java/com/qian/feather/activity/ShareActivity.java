package com.qian.feather.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.qian.feather.R;

public class ShareActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle onSavedInstance) {
        super.onCreate(onSavedInstance);
        setContentView(R.layout.activity_chat);

        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if(action != null) {
            if (action == Intent.ACTION_SEND) {
                if("image/*".equals(type)) {
                    Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    if (uri != null) {
                        //ImageView imageView = findViewById(R.id.imageView);
                    }
                }
            }
        }


    }

}
