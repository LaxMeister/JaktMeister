package com.lajw.jaktmeister.chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lajw.jaktmeister.R;
import com.lajw.jaktmeister.chat.adapter.MessageAdapter;
import com.lajw.jaktmeister.entity.ChatMessage;
import com.lajw.jaktmeister.entity.ChatRoom;
import com.lajw.jaktmeister.entity.User;

import java.util.ArrayList;
import java.util.List;

public class PrivateMessageActivity extends AppCompatActivity {

    TextView firstname;
    TextView lastname;

    RecyclerView recyclerView;
    EditText messageText;
    ImageButton sendButton;

    FirebaseUser firebaseUser;
    FirebaseFirestore db;
    Intent intent;

    MessageAdapter messageAdapter;
    List<ChatMessage> mChatMessage;

    String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        //Widgets
        firstname = findViewById(R.id.textViewMessageFirstname);
        lastname = findViewById(R.id.textViewMessageLastname);
        messageText = findViewById(R.id.messageTextSend);
        sendButton = findViewById(R.id.messageButtonSend);

        //RecyclerView
        recyclerView = findViewById(R.id.recyclerViewMessage);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //Intent
        intent = getIntent();
        userid = intent.getStringExtra("userid");

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarMessage);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
//                Intent intent = new Intent(PrivateMessageActivity.this, ChatActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                finish();
//                startActivity(intent);
            }
        });


        //Authentication / Database
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(userid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User user = task.getResult().toObject(User.class);
                        firstname.setText(user.getFirstName());
                        lastname.setText(user.getLastName());
                    }
                });

        sendButton.setOnClickListener(v -> {
            String msg = messageText.getText().toString();
            if (!msg.equals("")) {
                sendMessage(firebaseUser.getUid(), userid, msg);
            } else {
                Toast.makeText(PrivateMessageActivity.this, "Meddelandet är tomt", Toast.LENGTH_SHORT).show();
            }
            messageText.setText("");
        });

        readMessage(firebaseUser.getUid(), userid);
        mChatMessage = new ArrayList<>();

        messageAdapter = new MessageAdapter(getApplicationContext(), mChatMessage);
        recyclerView.setAdapter(messageAdapter);
    }

    private void sendMessage(String sender, String receiver, String message) {
        String chatRoomId;
        ChatMessage chatMessage = new ChatMessage(sender, receiver, message);

        if (sender.compareToIgnoreCase(receiver) < receiver.compareToIgnoreCase(sender)) {
            chatRoomId = sender + "" + receiver;
        } else {
            chatRoomId = receiver + "" + sender;
        }

        Long date = System.currentTimeMillis();
        String dateString = date.toString();

        db.collection("chatrooms").document(chatRoomId).collection("messages").document(dateString)
                .set(chatMessage)
                .addOnSuccessListener(documentReference -> {
                    Log.d("MessageActivity", "Added object:" + chatMessage.toString());
                })
                .addOnFailureListener(e -> {
                    Log.w("MessageActivity", "Error adding message", e);
                });

        //Add to chat list
        ChatRoom chatRoom = new ChatRoom(chatRoomId);
        db.collection("ChatList").document(firebaseUser.getUid()).collection("chats").document(chatRoomId)
                .set(chatRoom)
                .addOnSuccessListener(documentReference -> {
                    Log.d("MessageActivity", "Added object:" + chatMessage.toString());
                })
                .addOnFailureListener(e -> {
                    Log.w("MessageActivity", "Error adding message", e);
                });

        //Also add for the other user
        db.collection("ChatList").document(userid).collection("chats").document(chatRoomId)
                .set(chatRoom)
                .addOnSuccessListener(documentReference -> {
                    Log.d("MessageActivity", "Added object:" + chatMessage.toString());
                })
                .addOnFailureListener(e -> {
                    Log.w("MessageActivity", "Error adding message", e);
                });
    }

    private void readMessage(String sender, String receiver) {
        String chatRoomId;

        if (sender.compareToIgnoreCase(receiver) < receiver.compareToIgnoreCase(sender)) {
            chatRoomId = sender + "" + receiver;
        } else {
            chatRoomId = receiver + "" + sender;
        }

        mChatMessage = new ArrayList<>();

        db.collection("chatrooms").document(chatRoomId).collection("messages")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("MessageActivity", "Listen failed.", error);
                        return;
                    }
                    mChatMessage.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        if (doc != null) {
                            ChatMessage chatMessage = doc.toObject(ChatMessage.class);
                            Log.d("MessageActivity", "Snapshot changed and found: " + chatMessage.getMessage() + "; " + chatMessage.getSender() + "; " + chatMessage.getReceiver());
                            mChatMessage.add(chatMessage);
                            messageAdapter.notifyDataSetChanged();
                            recyclerView.setAdapter(messageAdapter);
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        //Intent intent = new Intent(PrivateMessageActivity.this, ChatActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        //startActivity(intent);
    }

}

