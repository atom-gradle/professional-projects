package com.qian.feather.activity;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;;
import android.util.Log;
import android.widget.*;
import android.content.*;

import com.qian.feather.item.Msg;
import com.qian.feather.NioClient;
import com.qian.feather.R;
import com.qian.feather.item.User;
import com.qian.feather.Utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * @author Qian atom-gradle
 */

public class LoginActivity extends AppCompatActivity {
    private Context context = this;
    private long second = -1L;
    private static final long div = 1_000_000_000;
    public static Set<String> bannedAccountSet = new HashSet<>(8);
    private static final String exitToastText = "Click one more time to exit";

    static {
        Collections.addAll(bannedAccountSet,"Admin","Administrator","Test","SuperUser","Test","User","root","System");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        EditText EditText_account = findViewById(R.id.EditText_account);
        EditText EditText_password = findViewById(R.id.EditText_password);

        Button login = findViewById(R.id.login_button);
        login.setOnClickListener(view -> {
            String account = EditText_account.getText().toString();
            String password = EditText_password.getText().toString();

            if(!Utils.isInfoValid(account, password)) {
                Toast.makeText(context,"Illegal Account",Toast.LENGTH_SHORT).show();
                return;
            }
            String hashPassword = String.format("%d",password.hashCode());
            User user = User.registerUser(account,hashPassword,"Snow");
            User.currentUser = user;
            new Thread(() -> {
                String content = null;
                try {
                    content = "LOGIN:"+user.getFeatherID()+";"+InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException e) {
                    Log.e("LoginActivity","getLocalAddress failed");
                    content = "LOGIN:"+user.getFeatherID();
                }
                Msg msg = new Msg(user.getFeatherID(),"SERVER",content,Msg.SYS_INFO);
                try {
                    NioClient client = NioClient.getInstance();
                    client.sendMsg(msg);
                } catch (IOException e) {
                    Log.e("Login Activity","Login Failed,probably bad network");
                }
            }).start();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        onMyBackPressed(true, new Runnable() {
            @Override
            public void run() {
                long secondNow = System.nanoTime() / div;
                if(second == -1) {
                    Toast.makeText(context, exitToastText, Toast.LENGTH_SHORT).show();
                    if((secondNow - second) < 5) {
                        finish();
                    } else {
                        Toast.makeText(context, exitToastText, Toast.LENGTH_SHORT).show();
                        second = secondNow;
                    }
                } else {
                    if((secondNow - second) < 5) {
                        finish();
                    } else {
                        Toast.makeText(context, exitToastText, Toast.LENGTH_SHORT).show();
                        second = secondNow;
                    }
                }
            }
        });
    }
    //处理退出时逻辑
    public void onMyBackPressed(Boolean isEnable,final Runnable callback) {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(isEnable) {
            @Override
            public void handleOnBackPressed() {
                callback.run();
            }
        });
    }
}
