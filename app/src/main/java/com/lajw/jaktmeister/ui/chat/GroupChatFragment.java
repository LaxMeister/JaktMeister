package com.lajw.jaktmeister.ui.chat;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lajw.jaktmeister.MainActivity;
import com.lajw.jaktmeister.R;
import com.lajw.jaktmeister.chat.adapter.GroupMessageAdapter;
import com.lajw.jaktmeister.entity.ChatMessage;
import com.lajw.jaktmeister.entity.HuntingTeam;
import com.lajw.jaktmeister.entity.User;
import com.lajw.jaktmeister.entity.UserHuntingTeam;
import com.lajw.jaktmeister.notifaction.MyCoolSingleton;
import com.lajw.jaktmeister.utils.SharedPreferencesRepository;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GroupChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<User> mUsers;

    TextView firstname;
    TextView lastname;

    EditText messageText;
    ImageButton sendButton;

    FirebaseUser firebaseUser;
    FirebaseFirestore db;
    Intent intent;

    GroupMessageAdapter messageAdapter;
    List<ChatMessage> mChatMessage;
    List<User> adminList;
    HuntingTeam huntingTeam;

    String userid;
    //    String huntingTeamId = "F6HXu7XkMxTpZMdgZy7O";
    String huntingTeamId;

    String TAG = getClass().getName();

    public GroupChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_group_message, container, false);

        firstname = view.findViewById(R.id.textViewMessageFirstname);
        lastname = view.findViewById(R.id.textViewMessageLastname);
        messageText = view.findViewById(R.id.messageTextSend);
        sendButton = view.findViewById(R.id.messageButtonSend);

        recyclerView = view.findViewById(R.id.recyclerViewMessage);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        huntingTeam = SharedPreferencesRepository.getCurrentHuntingTeam(getContext());
        huntingTeamId = SharedPreferencesRepository.getCurrentHuntingTeam(getContext()).getId();
        if (!huntingTeamId.equals("0") && huntingTeamId != null) {

            List<UserHuntingTeam> adminList = new ArrayList<>();
            if (huntingTeam.getAdmins() != null) {
                adminList = huntingTeam.getAdmins();
            }

            mUsers = new ArrayList<>();
            messageAdapter = new GroupMessageAdapter(getContext(), mChatMessage, adminList);
            recyclerView.setAdapter(messageAdapter);

            intent = getActivity().getIntent();
            userid = intent.getStringExtra("userid");


            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            db = FirebaseFirestore.getInstance();


            sendButton.setOnClickListener(v -> {
                String msg = messageText.getText().toString();
                if (!msg.equals("")) {
                    sendMessage(firebaseUser.getUid(), huntingTeamId, msg);
                } else {
                    Toast.makeText(getContext(), "Meddelandet är tomt", Toast.LENGTH_SHORT).show();
                }
                messageText.setText("");
            });

            readMessage();
            mChatMessage = new ArrayList<>();
            messageAdapter = new GroupMessageAdapter(getContext(), mChatMessage, adminList);
            recyclerView.setAdapter(messageAdapter);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Information")
                    .setMessage("Du kan inte använda gruppchatten då du inte har valt ett jaktlag.")
                    .setPositiveButton("Stäng", (dialog, which) -> {
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        getActivity().finish();
                        startActivity(intent);
                    }).setCancelable(false)
                    .create()
                    .show();


        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void sendMessage(String sender, String reciever, String message) {
        ChatMessage chatMessage = new ChatMessage(sender, reciever, message);

        Long date = System.currentTimeMillis();
        String dateString = date.toString();

        db.collection("HuntingTeam").document(huntingTeamId).collection("ChatRoom").document(dateString)
                .set(chatMessage)
                .addOnSuccessListener(documentReference -> {
                    Log.d("MessageActivity", "Added object:" + chatMessage.toString());
                })
                .addOnFailureListener(e -> {
                    Log.w("MessageActivity", "Error adding message", e);
                });

        HuntingTeam currentHuntingTeam = SharedPreferencesRepository.getCurrentHuntingTeam(getContext());
        String NOTIFICATION_TITLE = SharedPreferencesRepository.getCurrentUser(getContext()).getFirstName() + " " + SharedPreferencesRepository.getCurrentUser(getContext()).getLastName();
        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        try {
            if (currentHuntingTeam != null) {
                notificationBody.put("title", NOTIFICATION_TITLE);
                notificationBody.put("message", message.toString());

                notification.put("data", notificationBody);
                notification.put("to", "/topics/" + SharedPreferencesRepository.getCurrentHuntingTeam(getContext()).getName().replace(" ", "") + "Stand");

            } else {

            }
        } catch (JSONException e) {
            Log.e(TAG, "onCreate: " + e.getMessage());
        }
        sendNotification(notification);
    }

    private void readMessage() {
        mChatMessage = new ArrayList<>();

        db.collection("HuntingTeam").document(huntingTeamId).collection("ChatRoom")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("MessageActivity", "Listen failed.", error);
                        return;
                    }
                    mChatMessage.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        if (doc != null) {
                            ChatMessage chatMessage = doc.toObject(ChatMessage.class);
                            Log.d("MessageActivity", "Snapshot changed and found: " + chatMessage.getMessage() + "; " + chatMessage.getSender());
                            mChatMessage.add(chatMessage);
                            messageAdapter.notifyDataSetChanged();
                            recyclerView.setAdapter(messageAdapter);
                        }
                    }
                });
    }

    private void sendNotification(JSONObject notification) {
        final String FCM_API = "https://fcm.googleapis.com/fcm/send";
        final String SERVER_KEY = "key=AAAAkuBCX_0:APA91bHOKQXvKqnS8l_1JIaDUZrkBdT-9h-yX5L5IFJobvaPeIw83DzihyBplR-T3Ha3-YPUUnLznfO3A9zocjkt4deXifW8NlV2lZfq-4TsBXtrZ649opiJUMrCqvJHX8oU5tV8sUhV";
        final String CONTENT = "application/json";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "onResponse: " + response.toString());

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "onErrorResponse: Didn't work");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", SERVER_KEY);
                params.put("Content-Type", CONTENT);
                return params;
            }
        };
        MyCoolSingleton.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);
    }
}