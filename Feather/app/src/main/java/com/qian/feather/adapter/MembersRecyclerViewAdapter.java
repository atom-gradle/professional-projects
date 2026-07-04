package com.qian.feather.adapter;

import android.annotation.SuppressLint;
import android.content.*;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.qian.feather.item.Contact;
import com.qian.feather.activity.PersonalInfoActivity;
import com.qian.feather.R;
import java.util.*;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class MembersRecyclerViewAdapter extends RecyclerView.Adapter<MembersRecyclerViewAdapter.LinearViewHolder> {

    private Context context;
    private List<Contact> contactsList;
    private LayoutInflater layoutInflater;
    private Set<String> firstCharacterSet;
    private final View.OnCreateContextMenuListener contextMenuListener = new View.OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu,View view,ContextMenu.ContextMenuInfo menuInfo) {
            //menu.add(Menu.NONE, R.menu.context_menu,Menu.NONE,"");
        }
    };

    public MembersRecyclerViewAdapter(Context context,List<Contact> contactsList) {
        this.context = context;
        this.contactsList = contactsList;
        layoutInflater = LayoutInflater.from(context);
        firstCharacterSet = new LinkedHashSet<>(27);
    }

    class LinearViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout layout_nameAbbr;
        private ConstraintLayout layout_person;
        private ConstraintLayout layout_totalMembers;
        private ImageView ImageView_head;
        private TextView TextView_name;
        private TextView TextView_abbr;
        private TextView TextView_totalMembers;
        public LinearViewHolder(View itemView) {
            super(itemView);
            layout_nameAbbr = itemView.findViewById(R.id.Layout_nameAbbr);
            layout_person = itemView.findViewById(R.id.Layout_person);
            layout_totalMembers = itemView.findViewById(R.id.Layout_totalMembers);
            ImageView_head = itemView.findViewById(R.id.ImageView_head);
            TextView_name = itemView.findViewById(R.id.TextView_name);
            TextView_abbr = itemView.findViewById(R.id.TextView_nameAbbr);
            TextView_totalMembers = itemView.findViewById(R.id.TextView_totalMembers);
        }
    }

    @NonNull
    @Override
    public MembersRecyclerViewAdapter.LinearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recyclerview_members,parent,false);
        LinearViewHolder linearViewHolder = new LinearViewHolder(view);
        return linearViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MembersRecyclerViewAdapter.LinearViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if(position == 0) {
            Contact contact = new Contact("置顶的人", R.drawable.contact_heart);
            holder.layout_nameAbbr.setVisibility(View.GONE);
            holder.layout_totalMembers.setVisibility(View.GONE);
            holder.TextView_name.setText(contact.getNickname());
            //holder.ImageView_head.setImageResource(contact.getHead());
            Glide.with(context)
                    .load(contact.getHead())
                    .transform(new RoundedCornersTransformation(12, 0, RoundedCornersTransformation.CornerType.ALL))
                    .into(holder.ImageView_head);
        } else if(position == 1) {
            Contact contact = new Contact("群聊/分组",R.drawable.contact_group);
            holder.layout_nameAbbr.setVisibility(View.GONE);
            holder.layout_totalMembers.setVisibility(View.GONE);
            holder.TextView_name.setText(contact.getNickname());
            //holder.ImageView_head.setImageResource(contact.getHead());
            Glide.with(context)
                    .load(contact.getHead())
                    .transform(new RoundedCornersTransformation(12, 0, RoundedCornersTransformation.CornerType.ALL))
                    .into(holder.ImageView_head);
        } else if(position == (getItemCount()-1)) {
            holder.TextView_totalMembers.setText(String.format("%d",position-1)+"个联系人");
            holder.layout_totalMembers.setVisibility(View.VISIBLE);
        } else {
            Contact contact = contactsList.get(position-2);
            System.out.println(position + "  " +contact);
            //holder.ImageView_head.setImageResource(contact.getHead());
            Glide.with(context)
                    .load(contact.getHead())
                    .placeholder(R.drawable.feather)
                    .transform(new RoundedCornersTransformation(12, 0, RoundedCornersTransformation.CornerType.ALL))
                    .into(holder.ImageView_head);
            holder.layout_totalMembers.setVisibility(View.GONE);
            String firstCharacter = contact.getFirstPinyinCharacter();
            if(!firstCharacterSet.contains(firstCharacter)) {
                holder.TextView_abbr.setText("    "+firstCharacter);
                firstCharacterSet.add(firstCharacter);
            } else {
                holder.TextView_abbr.setVisibility(View.GONE);
            }
            holder.TextView_name.setText(contact.getNickname());
            holder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(context, PersonalInfoActivity.class);
                intent.putExtra("nickname",contact.getNickname());
                context.startActivity(intent);
            });
        }
        holder.itemView.setLongClickable(true);
        holder.itemView.setOnLongClickListener(view -> {
            view.setOnCreateContextMenuListener(contextMenuListener);
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return contactsList.size()+2;
    }

    public void updateData(List<Contact> contactsList) {
        this.contactsList = contactsList;
        notifyDataSetChanged();
    }
}