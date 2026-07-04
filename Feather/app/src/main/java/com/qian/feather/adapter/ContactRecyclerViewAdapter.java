package com.qian.feather.adapter;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qian.feather.item.Contact;
import com.qian.feather.activity.PersonalInfoActivity;
import com.qian.feather.R;

import java.util.List;
import java.util.Map;

public class ContactRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<Contact> contactList;
    private Map<String,Contact> contactMap;
    private LayoutInflater layoutInflater;

    public ContactRecyclerViewAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }
    public ContactRecyclerViewAdapter(Context context,List<Contact> contactList) {
        this.context = context;
        this.contactList = contactList;
        layoutInflater = LayoutInflater.from(context);
    }
    public ContactRecyclerViewAdapter(Context context,Map<String,Contact> contactMap) {
        this.context = context;
        this.contactMap = contactMap;
        layoutInflater = LayoutInflater.from(context);
    }
    class ShowFirstCharacter extends RecyclerView.ViewHolder {
        private TextView textview_character;
        public ShowFirstCharacter(@NonNull View itemView) {
            super(itemView);
            textview_character = itemView.findViewById(R.id.TextView_showFirstCharacter);
        }
    }
    class ShowContact extends RecyclerView.ViewHolder {
        private ImageView head;
        private TextView memoName;
        public ShowContact(@NonNull View itemView) {
            super(itemView);
            head = itemView.findViewById(R.id.ImageView_contact_head);
            memoName = itemView.findViewById(R.id.TextView_contact_memoname);
        }
    }
    class ShowContactsAmount extends RecyclerView.ViewHolder {
        private TextView textview_showContactAmount;
        public ShowContactsAmount(@NonNull View itemView) {
            super(itemView);
            textview_showContactAmount = itemView.findViewById(R.id.TextView_showContactAmount);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 1) {
            View view = layoutInflater.inflate(R.layout.recyclerview_contact_show_contact,parent,false);
            return new ShowContact(view);
        } else if(viewType == 2) {
            View view = layoutInflater.inflate(R.layout.recyclerview_contact_show_contacts_amount,parent,false);
            return new ShowContactsAmount(view);
        } else if(viewType == 0) {
            View view = layoutInflater.inflate(R.layout.recyclerview_contact_show_first_character,parent,false);
            return new ShowFirstCharacter(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(position == 0) {
            Contact contact = new Contact("置顶的人",R.drawable.contact_heart);
        } else if(position == 1) {
            Contact contact = new Contact("群聊/分组",R.drawable.contact_group);
        } else {

        }
        Contact contact = contactList.get(position);
        if (holder instanceof ShowFirstCharacter) {
            ((ShowFirstCharacter) holder).textview_character.setText("aaaaa");
        } else if (holder instanceof ShowContact) {
            /*
            Glide.with(context)
                    .load()
                    .placeholder(R.drawable.feather)
                    .into(((ShowContact)holder).head);
             */
            ((ShowContact) holder).memoName.setLongClickable(true);
            ((ShowContact) holder).memoName.setOnLongClickListener(view -> {
                return false;
            });
            ((ShowContact) holder).head.setOnClickListener(view -> {
                Intent intent = new Intent(context, PersonalInfoActivity.class);
                intent.putExtra("memoName","memoName");
                context.startActivity(intent);
            });
        } else if(holder instanceof ShowContactsAmount) {
            ((ShowContactsAmount)holder).textview_showContactAmount.setText(String.format("%d",11));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return  1;
    }
    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public void updateData(List<Contact> contactList) {
        this.contactList = contactList;
        notifyDataSetChanged();
    }
    public void updateData(Map<String,Contact> contactMap) {
        this.contactMap = contactMap;
        notifyDataSetChanged();
    }
}