package com.lajw.jaktmeister.chat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lajw.jaktmeister.R;
import com.lajw.jaktmeister.entity.ChatMessage;
import com.lajw.jaktmeister.entity.User;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context context;
    private List<ChatMessage> mChatMessage;

    FirebaseUser firebaseUser;

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    public MessageAdapter() {

    }

    public MessageAdapter(Context context, List<ChatMessage> mChatMessage) {
        this.context = context;
        this.mChatMessage = mChatMessage;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_LEFT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left,
                    parent,
                    false);
            return new ViewHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right,
                    parent,
                    false);
            return new ViewHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ChatMessage chatMessage = mChatMessage.get(position);

        holder.show_message.setText(chatMessage.getMessage());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(chatMessage.getSender())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if(holder.userNameAboveMessage != null){
                            User user = task.getResult().toObject(User.class);
                            holder.userNameAboveMessage.setText(user.getFirstName() +" "+ user.getLastName());
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return mChatMessage.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mChatMessage.get(position).getSender() != null) {
            if (mChatMessage.get(position).getSender().equals(firebaseUser.getUid())) {
                return MSG_TYPE_RIGHT;
            } else {
                return MSG_TYPE_LEFT;
            }
        }
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView show_message;
        public TextView userNameAboveMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.textViewShowMessage);
            userNameAboveMessage = itemView.findViewById(R.id.textViewChatItemUsername);
        }
    }
}