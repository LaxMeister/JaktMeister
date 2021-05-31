package com.lajw.jaktmeister.chat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lajw.jaktmeister.R;
import com.lajw.jaktmeister.chat.activity.PrivateMessageActivity;
import com.lajw.jaktmeister.entity.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private List<User> mUsers;

    public UserAdapter(){

    }
    public UserAdapter(Context context, List<User> mUsers) {
        this.context = context;
        this.mUsers = mUsers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item,
                parent,
                false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User users = mUsers.get(position);
        holder.firstname.setText(users.getFirstName());
        holder.lastname.setText(users.getLastName());

        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, PrivateMessageActivity.class);
            i.putExtra("userid", users.getId());
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView firstname;
        public TextView lastname;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            firstname = itemView.findViewById(R.id.textViewUserItem);
            lastname = itemView.findViewById(R.id.textViewUserItemLastname);
        }
    }
}
