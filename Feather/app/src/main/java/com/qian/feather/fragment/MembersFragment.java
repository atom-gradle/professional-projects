package com.qian.feather.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import android.os.*;
import android.view.*;
import android.widget.*;

import com.qian.feather.adapter.MembersRecyclerViewAdapter;
import com.qian.feather.item.Contact;
import com.qian.feather.R;

import java.util.*;

public class MembersFragment extends Fragment {
    private RecyclerView recyclerView;
    private Context context;
    private MembersRecyclerViewAdapter membersRecyclerViewAdapter;
    private Handler handler;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MembersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MembersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MembersFragment newInstance(String param1, String param2) {
        MembersFragment fragment = new MembersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_members,container,false);
        context = view.getContext();

        LinearLayoutManager lm = new LinearLayoutManager(context);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        membersRecyclerViewAdapter = new MembersRecyclerViewAdapter(context,loadContractsFromNative());
        recyclerView = view.findViewById(R.id.contracts_RecyclerView);
        recyclerView.setAdapter(membersRecyclerViewAdapter);
        recyclerView.setLayoutManager(lm);
        recyclerView.addItemDecoration(new DividerItemDecoration(context,LinearLayoutManager.VERTICAL));
        registerForContextMenu(recyclerView);

        return view;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //Toast.makeText(context,"onAttach()",Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onStart() {
        super.onStart();
        //Toast.makeText(context,"onStart()",Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onResume() {
        super.onResume();
        //Toast.makeText(context,"onResume()",Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onPause() {
        super.onPause();
        //Toast.makeText(context,"onPause()",Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onStop() {
        super.onStop();
        //Toast.makeText(context,"onStop()",Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onDetach() {
        super.onDetach();
        //Toast.makeText(context,"onDetach()",Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        // 当上下文菜单创建时调用
        // 这里可以根据menuInfo来决定显示哪些菜单项
        menu.add(Menu.NONE, 1, Menu.NONE, "设置备注和标签");
        menu.add(Menu.NONE, 2, Menu.NONE, "删除好友");
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                Toast.makeText(context,"设置了备注和标签",Toast.LENGTH_SHORT).show();
                return true;
            case 2:
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    private List<Contact> loadContractsFromNative() {
        List<Contact> contactsList = new ArrayList<>();
        contactsList.add(new Contact("赤井秀一",R.drawable.chocolate_bear));
        contactsList.add(new Contact("Feather官方",R.drawable.feather));
        contactsList.add(new Contact("文件传输助手",R.drawable.feather));
        contactsList.add(new Contact("铃木园子",R.drawable.bear_head_sculpture));
        contactsList.add(new Contact("朱蒂老师",R.drawable.chocolate_bear));
        contactsList.add(new Contact("安室透",R.drawable.bear_head_sculpture));
        contactsList.add(new Contact("江户川柯南",R.drawable.kenan));
        contactsList.add(new Contact("毛利兰",R.drawable.xiaolan));
        contactsList.add(new Contact("毛利小五郎",R.drawable.xiaowulang));
        contactsList.add(new Contact("宫野志保",R.drawable.xiaoai));
        Collections.sort(contactsList);
        return contactsList;
    }

}