package com.qian.feather.adapter;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.qian.feather.item.FileInfo;
import com.qian.feather.R;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class FileDisplayRecyclerViewAdapter extends RecyclerView.Adapter<FileDisplayRecyclerViewAdapter.LinearViewHolder> {

    private Context contexts;
    private List<FileInfo> fileList;
    private LayoutInflater layoutInflater;

    public FileDisplayRecyclerViewAdapter(Context context, List<FileInfo> fileList) {
        this.contexts = context;
        this.fileList = fileList;
        layoutInflater = LayoutInflater.from(contexts);
    }

    class LinearViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageview_fileicon;
        private TextView textView_filename;
        private TextView textView_fileinfo;
        public LinearViewHolder(View itemView) {
            super(itemView);
            imageview_fileicon = itemView.findViewById(R.id.ImageView_fileIcon);
            textView_filename = itemView.findViewById(R.id.TextView_fileName);
            textView_fileinfo = itemView.findViewById(R.id.TextView_fileInfo);
        }
    }

    @NonNull
    @Override
    public FileDisplayRecyclerViewAdapter.LinearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(contexts).inflate(R.layout.recyclerview_item_display_file,parent,false);
        LinearViewHolder linearViewHolder = new LinearViewHolder(view);
        return linearViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FileDisplayRecyclerViewAdapter.LinearViewHolder holder, int position) {
        FileInfo fileInfo = fileList.get(position);
        holder.textView_filename.setText(fileInfo.getName());
        holder.textView_fileinfo.setText(fileInfo.getTime() + fileInfo.getSize());
        Glide.with(contexts)
                .load(R.drawable.file_send_outline)
                .placeholder(R.drawable.feather)
                .transform(new RoundedCornersTransformation(12, 0, RoundedCornersTransformation.CornerType.ALL))
                //.override(100,100)
                .into(holder.imageview_fileicon);
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }
    public void updateData(List<FileInfo> fileList) {
        this.fileList = fileList;
        notifyDataSetChanged();
    }
}

