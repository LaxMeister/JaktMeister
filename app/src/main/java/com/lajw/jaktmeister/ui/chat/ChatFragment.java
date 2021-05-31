package com.lajw.jaktmeister.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lajw.jaktmeister.R;
import com.lajw.jaktmeister.authentication.activity.LoginActivity;
import com.lajw.jaktmeister.chat.adapter.UserAdapter;
import com.lajw.jaktmeister.entity.ChatRoom;
import com.lajw.jaktmeister.entity.User;

import java.util.ArrayList;
import java.util.List;


public class ChatFragment extends Fragment {

    private Button button;
    private TextView temp;

    private UserAdapter userAdapter;
    private RecyclerView recyclerView;
    private List<User> mUsers;
    private List<ChatRoom> chatRoomList;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private final String TAG = getClass().getName();

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.chatFragmentRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        chatRoomList = new ArrayList<>();
        mUsers = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), mUsers);
        recyclerView.setAdapter(userAdapter);

        getUserList();
        getChatList();


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void getUserList() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("ChatList").document(firebaseUser.getUid()).collection("chats")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("ChatFragment", "Listen failed.", error);
                        return;
                    }
                    chatRoomList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        if (doc != null) {
                            ChatRoom chatRoom = doc.toObject(ChatRoom.class);
                            Log.d("ChatFragment", "Snapshot changed and found");
                            chatRoomList.add(chatRoom);
                            userAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void getChatList() {
        FirebaseFirestore db1 = FirebaseFirestore.getInstance();
        db1.collection("users")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("ChatFragment", "Listen failed.", error);
                        return;
                    }
                    mUsers.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        if (doc != null) {
                            User user = doc.toObject(User.class);
                            if (user.getId() != null) {


                                String chatRoomId;
                                System.out.println("userid: " + user.getEmail() + " " + user.getId());
                                if (user.getId().compareToIgnoreCase(firebaseUser.getUid()) < firebaseUser.getUid().compareToIgnoreCase(user.getId())) {
                                    chatRoomId = user.getId() + "" + firebaseUser.getUid();
                                } else {
                                    chatRoomId = firebaseUser.getUid() + "" + user.getId();
                                }
                                for (ChatRoom chatRoom : chatRoomList) {
                                    if (chatRoom.getId().equals(chatRoomId)) {
                                        mUsers.add(user);
                                        userAdapter.notifyDataSetChanged();

                                    }
                                }
                                userAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }

    public void onClickLogout(View view) {
        startActivity(new Intent(getActivity(), LoginActivity.class));
    }
}