package com.qian.feather.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.qian.feather.R;

import java.io.File;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ImageChooserRecyclerViewAdapter extends RecyclerView.Adapter<ImageChooserRecyclerViewAdapter.LinearViewHolder> {

    private Context context;
    private List<String> testList;
    private List<String> imagesToSend;
    private LayoutInflater layoutInflater;
    private BitmapFactory.Options options;
    private final ThreadPoolExecutor threadPoolExecutor;
    private static final int availableProcessors = Runtime.getRuntime().availableProcessors();
    private static final int maxPoolSize = availableProcessors + 1;
    private final LinkedBlockingQueue<Bitmap> bitmapQueue;
    public ImageChooserRecyclerViewAdapter(Context context) {
        this.context = context;
        testList = new ArrayList<>();
        imagesToSend = new ArrayList<>();
        layoutInflater = LayoutInflater.from(context);
        options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inSampleSize = 8;//将图像的宽高缩小为原始的1/8
        threadPoolExecutor = new ThreadPoolExecutor(availableProcessors,maxPoolSize,0L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),new ThreadPoolExecutor.DiscardOldestPolicy());
        bitmapQueue = new LinkedBlockingQueue<>(10);
    }
    class LinearViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageview;
        private CheckBox checkBox;
        public LinearViewHolder(View itemView) {
            super(itemView);
            imageview = itemView.findViewById(R.id.ImageView);
            checkBox = itemView.findViewById(R.id.CheckBox);
        }
    }

    @NonNull
    @Override
    public ImageChooserRecyclerViewAdapter.LinearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recyclerview_image_chooser3,parent,false);
        LinearViewHolder linearViewHolder = new LinearViewHolder(view);
        return linearViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageChooserRecyclerViewAdapter.LinearViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String imagePath = testList.get(position);
        /*
        threadPoolExecutor.execute(() -> {
            final Bitmap bitmap = BitmapFactory.decodeFile(imagePath,options);
            try {
                bitmapQueue.put(bitmap);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

         */
        Glide.with(context)
                .load(new File(imagePath))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(100,100)
                .format(DecodeFormat.PREFER_RGB_565)
                .skipMemoryCache(false)
                .into(holder.imageview);

        //解决RecyclerView视图复用，导致ChechBox已被选中的问题
        if(holder.checkBox.isChecked()) {
            holder.checkBox.setChecked(false);
        }
        holder.checkBox.setOnCheckedChangeListener((compoundButton,isChecked) -> {
            String path = testList.get(position);
            if(isChecked) {
                if(imagesToSend.size() < 10) {
                    synchronized (imagesToSend) {
                        imagesToSend.add(path);
                    }
                } else {
                    compoundButton.setChecked(false);
                    Toast.makeText(context,"最多选择10张照片",Toast.LENGTH_SHORT).show();
                }
            } else {
                if(imagesToSend.contains(path)) {
                    synchronized (imagesToSend) {
                        imagesToSend.remove(path);
                    }
                }
            }
        });
        holder.imageview.setOnClickListener(view -> {
            Toast.makeText(context,imagePath.substring(imagePath.length()-50),Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return testList.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull LinearViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }
    public void updateData(List<String> imagePathList) {
        this.testList = imagePathList;
        notifyDataSetChanged();
    }
    public List<String> getImagesToSend() {
        if(imagesToSend == null) {
            return new ArrayList<>();
        }
        return imagesToSend;
    }
}
