package com.qian.feather.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.qian.feather.Utils;
import com.qian.feather.adapter.ImageChooserRecyclerViewAdapter;
import com.qian.feather.R;

import java.io.Serializable;
import java.util.*;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ImageChooserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImageChooserFragment extends Fragment {
    private RecyclerView recyclerView;
    private Context context;
    private ImageChooserRecyclerViewAdapter imageChooserRecyclerViewAdapter;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Boolean isLoading = false;
    private Spinner spinner;
    private ArrayAdapter<String> arrayAdapter;
    private Boolean firstCreated = false;
    private int currentSelection;
    public ImageChooserFragment() {
        // Required empty public constructor
    }

    public static ImageChooserFragment newInstance() {
        ImageChooserFragment fragment = new ImageChooserFragment();
        return fragment;
    }
    public static ImageChooserFragment newInstance(Map<String,List<String>> resultMap) {
        ImageChooserFragment fragment = new ImageChooserFragment();
        Bundle args = new Bundle();
        args.putSerializable("imagePathsMap", (Serializable) resultMap);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        onMyBackPressed(true, new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent("ImageChooserFragment.FINISH_SELF");
                context.sendBroadcast(intent);
            }
        });
    }
    public void onMyBackPressed(Boolean isEnable,final Runnable callback) {
        getActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(isEnable) {
            @Override
            public void handleOnBackPressed() {
                callback.run();
            }
        });
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
        View view = inflater.inflate(R.layout.fragment_image_chooser,container,false);
        Button Button_cancel = view.findViewById(R.id.Button_cancel);
        Button_cancel.setOnClickListener(view1 -> {
            Intent intent = new Intent("ImageChooserFragment.FINISH_SELF");
            context.sendBroadcast(intent);
        });
        Button button_sendImage = view.findViewById(R.id.Button_sendImage);
        button_sendImage.setOnClickListener(view2 -> {
            List<String> imagesToSend = imageChooserRecyclerViewAdapter.getImagesToSend();
            if(imagesToSend.size() == 0) {
                Toast.makeText(context,"未选择照片",Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent("ImageChooserFragment.IMAGES_TO_SEND");
                intent.putStringArrayListExtra("imagesToSend", (ArrayList<String>) imagesToSend);
                context.sendBroadcast(intent);
            }
        });

        Map<String,String> spinnerMap = Utils.getSpinnerMap();
        String[] spinnerArray = spinnerMap.keySet().toArray(new String[spinnerMap.size()]);
        spinner = view.findViewById(R.id.Spinner_imageSpinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context,R.layout.spinner_item_image_chooser,spinnerArray);
        arrayAdapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        firstCreated = true;
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                currentSelection = position;
                String selectedItem = adapterView.getItemAtPosition(position).toString();
                imageChooserRecyclerViewAdapter.updateData(Utils.getImagePathInFolder(spinnerMap.get(selectedItem)));
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        context = view.getContext();

        GridLayoutManager layoutManager = new GridLayoutManager(context,4);
        imageChooserRecyclerViewAdapter = new ImageChooserRecyclerViewAdapter(context);
        recyclerView = view.findViewById(R.id.imageChooser_RecyclerView);
        recyclerView.setAdapter(imageChooserRecyclerViewAdapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                //int totalItemCount = layoutManager.getItemCount();
                //int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

            }
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //continue loading images when scrolling is stopped
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Glide.with(context).resumeRequests();
                } else {
                    //stop loading when scrolling
                    Glide.with(context).pauseRequests();
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
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
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    private void loadMoreItems(List<String> imagePathsList) {
        // 这里添加加载数据的逻辑，例如从数据库或网络获取数据
        // 获取数据后，更新Adapter的数据集，并通知数据集改变
        List<String> list = imagePathsList.subList(0,40);
        imageChooserRecyclerViewAdapter.updateData(list);
        isLoading = false;
    }
}