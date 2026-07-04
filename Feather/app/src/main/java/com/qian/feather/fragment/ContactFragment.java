package com.qian.feather.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import android.view.*;

import com.qian.feather.adapter.ContactRecyclerViewAdapter;
import com.qian.feather.item.Contact;
import com.qian.feather.R;

import java.util.*;
public class ContactFragment extends Fragment {
    private RecyclerView recyclerView;
    private Context context;
    private ContactRecyclerViewAdapter contactRecyclerViewAdapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ContactFragment() {
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
    public static ContactFragment newInstance(String param1, String param2) {
        ContactFragment fragment = new ContactFragment();
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
        contactRecyclerViewAdapter = new ContactRecyclerViewAdapter(context,loadContactsFromNative());
        recyclerView = view.findViewById(R.id.contracts_RecyclerView);
        recyclerView.setAdapter(contactRecyclerViewAdapter);
        recyclerView.setLayoutManager(lm);
        recyclerView.addItemDecoration(new DividerItemDecoration(context,LinearLayoutManager.VERTICAL));
        registerForContextMenu(recyclerView);
        return view;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
    @Override
    public void onStart() {
        super.onStart();
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
    }
    @Override
    public void onDetach() {
        super.onDetach();
    }
    /*
    private List<Contact> initGivenContacts() {
        List<Contact> givenContactsList = new ArrayList<>();
        givenContactsList.add(new Contact("置顶的人",R.drawable.contact_heart));
        givenContactsList.add(new Contact("群聊/分组",R.drawable.contact_group));
        return givenContactsList;
    }
     */
    private List<Contact> loadContactsFromNative() {
        List<Contact> contactsList = new ArrayList<>();
        contactsList.add(new Contact("All falls down",R.drawable.chocolate_bear));
        contactsList.add(new Contact("The Show",R.drawable.chocolate_bear));
        contactsList.add(new Contact("Summer",R.drawable.bear_head_sculpture));
        contactsList.add(new Contact("Legends Never Die",R.drawable.chocolate_bear));
        contactsList.add(new Contact("If I die young",R.drawable.bear_head_sculpture));
        contactsList.add(new Contact("柯南",R.drawable.chocolate_bear));
        contactsList.add(new Contact("小兰",R.drawable.chocolate_bear));
        contactsList.add(new Contact("小五郎",R.drawable.chocolate_bear));
        contactsList.add(new Contact("灰原",R.drawable.chocolate_bear));
        Collections.sort(contactsList);
        return contactsList;
    }

}