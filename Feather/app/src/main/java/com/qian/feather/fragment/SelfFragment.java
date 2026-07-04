package com.qian.feather.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qian.feather.adapter.SelfRecyclerViewAdapter;
import com.qian.feather.item.SelfItem;
import com.qian.feather.R;
import com.qian.feather.item.User;

import java.util.*;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SelfFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelfFragment extends Fragment {

    private RecyclerView recyclerView;
    private Context context;
    private SelfRecyclerViewAdapter selfRecyclerViewAdapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SelfFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SelfFragment newInstance(String param1, String param2) {
        SelfFragment fragment = new SelfFragment();
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
        View view = inflater.inflate(R.layout.fragment_me,container,false);
        context = view.getContext();
        recyclerView = view.findViewById(R.id.me_RecyclerView);
        LinearLayoutManager lm = new LinearLayoutManager(context);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        selfRecyclerViewAdapter = new SelfRecyclerViewAdapter(context,initData());
        recyclerView.setAdapter(selfRecyclerViewAdapter);
        recyclerView.setLayoutManager(lm);
        recyclerView.addItemDecoration(new DividerItemDecoration(context,LinearLayoutManager.VERTICAL));
        return view;
    }

    private List<SelfItem> initData() {
        List<SelfItem> itemList = new ArrayList<>();
        itemList.add(new SelfItem(1, User.currentUser));
        itemList.add(new SelfItem(2,"设置"));
        //itemList.add(new SelfItem(3,"朋友圈"));
        return itemList;
    }
}