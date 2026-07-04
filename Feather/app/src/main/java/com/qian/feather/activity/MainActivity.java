package com.qian.feather.activity;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.*;
import androidx.viewpager2.widget.ViewPager2;

import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import com.qian.feather.fragment.HomeFragment;
import com.qian.feather.fragment.MembersFragment;
import com.qian.feather.fragment.SelfFragment;
import com.qian.feather.adapter.MainViewPagerAdapter;
import com.qian.feather.R;
import com.qian.feather.Utils;

import java.util.*;

/**
 * @author Qian atom-gradle
 */

public class MainActivity extends AppCompatActivity {
    private Handler handler;
    Context context = this;
    private List<Fragment> fragmentList;
    private HomeFragment homeFragment;
    private MembersFragment contactsFragment;
    private SelfFragment selfFragment;
    private static final int fragmentChanged = 1;
    private static int currentFragmentIndex = 0;
    private TextView title;
    private MainBroadcastReceiver mainBroadcastReceiver = new MainBroadcastReceiver();
    private ImageButton home;
    private ImageButton contacts;
    private ImageButton self;
    private static int unreadMsgNumber = 0;
    private ViewPager2 viewPager2;
    public static boolean isNightMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Feather_MainActivity);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.MicrosoftBlue));
        }
         */
        /*
        new Thread(() -> {
            try {
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"account.txt");
                if(!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(account.getBytes());
                fos.write(password.getBytes());
                fos.close();
            } catch (IOException e) {
                Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
            }
        }).start();
         */
        /*
        FileOutputStream outputStream = null;
        try {
            File file = new File(getFilesDir(),"x.txt");
            if(!file.exists()) {
                file.createNewFile();
            }
            outputStream = new FileOutputStream(file);
            outputStream.write(account.getBytes());
            outputStream.write(password.getBytes());
            outputStream.close();
            Toast.makeText(this,"success7",Toast.LENGTH_LONG).show();
        } catch(Exception e) {
            Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
        }
         */
        /*
        try {
            outputStream = openFileOutput("x.txt", Context.MODE_PRIVATE | Context.MODE_APPEND);
            outputStream.write("aaa".getBytes());
            outputStream.flush();
            outputStream.close();
            Toast.makeText(this,"success5",Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
            }
        }
 */

        home = findViewById(R.id.ImageButton_home);
        contacts = findViewById(R.id.ImageButton_contacts);
        self = findViewById(R.id.ImageButton_self);
        View layout_head = findViewById(R.id.Layout_head);
        title = findViewById(R.id.TextView_title);

        fragmentList = new ArrayList<>();
        homeFragment = new HomeFragment();
        contactsFragment = new MembersFragment();
        selfFragment = new SelfFragment();

        fragmentList.add(homeFragment);
        fragmentList.add(contactsFragment);
        fragmentList.add(selfFragment);

        ImageButton imageButton_switchMode = findViewById(R.id.ImageButton_switch_mode);
        imageButton_switchMode.setOnClickListener(view -> {
            if(!isNightMode) {
                context.setTheme(R.style.Theme_Feather_MainActivity_Night);
                isNightMode = true;
            } else {
                context.setTheme(R.style.Theme_Feather_MainActivity);
            }
        });
        ImageButton ImageButton_add_friend = findViewById(R.id.ImageButton_add_friend);
        ImageButton_add_friend.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this,AddNewFriendActivity.class);
            startActivity(intent);
        });

        MainViewPagerAdapter pagerAdapter = new MainViewPagerAdapter(this,this,fragmentList);
        viewPager2 = findViewById(R.id.ViewPager_main);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 1:
                        currentFragmentIndex = 1;
                        setContactsFragmentStyle();
                        Utils.sendMessageToHandler(handler,"Contact",fragmentChanged);
                        break;
                    case 2:
                        currentFragmentIndex = 2;
                        Utils.sendMessageToHandler(handler,"Me",fragmentChanged);
                        setSelfFragmentStyle();
                        break;
                    default:
                        currentFragmentIndex = 0;
                        Utils.sendMessageToHandler(handler,"Feather",fragmentChanged);
                        setHomeFragmentStyle();
                        break;
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                switch (state) {
                    //空闲状态
                    case ViewPager2.SCROLL_STATE_IDLE:
                        break;
                    //用户手指拖动
                    case ViewPager2.SCROLL_STATE_DRAGGING:
                        break;
                    //自滑动
                    case ViewPager2.SCROLL_STATE_SETTLING:
                        break;
                }
            }
        });
        viewPager2.setAdapter(pagerAdapter);
        home.setScaleType(home.getScaleType());
        home.setImageResource(R.drawable.contact_plain);
        contacts.setImageResource(R.drawable.contact_plain);
        self.setImageResource(R.drawable.self_plain);

        home.setOnClickListener(view -> {
            setHomeFragmentStyle();
            currentFragmentIndex = 0;
            Utils.sendMessageToHandler(handler,"Feather",fragmentChanged);
            viewPager2.setCurrentItem(0);
        });
        contacts.setOnClickListener(view -> {
            setContactsFragmentStyle();
            currentFragmentIndex = 1;
            Utils.sendMessageToHandler(handler,"Contact",fragmentChanged);
            viewPager2.setCurrentItem(1);
        });
        self.setOnClickListener(view -> {
            setSelfFragmentStyle();
            currentFragmentIndex = 2;
            Utils.sendMessageToHandler(handler,"Me",fragmentChanged);
            viewPager2.setCurrentItem(2);
        });

        //更新UI组件
        handler = new Handler(Looper.myLooper()) {
            public void handleMessage(Message message) {
                if(message.what == fragmentChanged) {
                    if("Self".equals(message.obj) || "Me".equals(message.obj) || "我".equals(message.obj)) {
                        layout_head.setVisibility(View.INVISIBLE);
                    } else {
                        layout_head.setVisibility(View.VISIBLE);
                        title.setText((String)message.obj);
                    }
                }
            }
        };
        onMyBackPressed(true, new Runnable() {
            @Override
            public void run() {
                moveTaskToBack(true);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter("com.example.HomeFragment.UNREAD_MSG_NUMBER++");
        registerReceiver(mainBroadcastReceiver,filter);
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
        unregisterReceiver(mainBroadcastReceiver);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    public void onMyBackPressed(Boolean isEnable,final Runnable callback) {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(isEnable) {
            @Override
            public void handleOnBackPressed() {
                callback.run();
            }
        });
    }
    private void setHomeFragmentStyle() {
        home.setScaleType(home.getScaleType());
        home.setImageResource(R.drawable.home_colored);
        contacts.setImageResource(R.drawable.contact_plain);
        self.setImageResource(R.drawable.self_plain);
    }
    private void setContactsFragmentStyle() {
        contacts.setScaleType(contacts.getScaleType());
        contacts.setImageResource(R.drawable.contact_colored);
        home.setImageResource(R.drawable.home_plain);
        self.setImageResource(R.drawable.self_plain);
    }
    private void setSelfFragmentStyle() {
        self.setScaleType(self.getScaleType());
        self.setImageResource(R.drawable.self_colored);
        home.setImageResource(R.drawable.home_plain);
        contacts.setImageResource(R.drawable.contact_plain);
    }

    class MainBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == "com.example.HomeFragment.UNREAD_MSG_NUMBER++") {
                unreadMsgNumber++;
                title.setText(String.format("Feather(%d)",unreadMsgNumber));
            }
        }
    }
}