package com.lajw.jaktmeister.huntingteam.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lajw.jaktmeister.R;
import com.lajw.jaktmeister.chat.activity.PrivateMessageActivity;
import com.lajw.jaktmeister.entity.HuntingTeam;
import com.lajw.jaktmeister.entity.User;
import com.lajw.jaktmeister.entity.UserHuntingTeam;
import com.lajw.jaktmeister.huntingteam.activity.TeamMembersActivity;
import com.lajw.jaktmeister.utils.SharedPreferencesRepository;

import java.util.List;
import java.util.Objects;


public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {

    private final String TAG = getClass().getName();

    private Context context;
    private List<UserHuntingTeam> mUsers;
    private boolean admin;

    private User user = new User();
    private HuntingTeam huntingTeam = new HuntingTeam();
    private FirebaseFirestore database;

    public MemberAdapter() {

    }

    public MemberAdapter(Context context, List<UserHuntingTeam> mUsers, boolean admin) {
        this.context = context;
        this.mUsers = mUsers;
        this.admin = admin;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.team_member_item,
                parent,
                false);

        user = SharedPreferencesRepository.getCurrentUser(view.getContext());
        huntingTeam = SharedPreferencesRepository.getCurrentHuntingTeam(view.getContext());
        database = FirebaseFirestore.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserHuntingTeam users = mUsers.get(position);
        String fullname = users.getFirstName() + " " + users.getLastName();
        if (holder.name != null) {
            holder.name.setText(fullname);
        }

        if (!admin) {
            holder.removeButton.setVisibility(View.INVISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, PrivateMessageActivity.class);
            i.putExtra("userid", users.getId());
            context.startActivity(i);
        });

        holder.removeButton.setOnClickListener(v -> {
            showDialogRemoveUser(users, fullname);
        });
    }

    private void showDialogRemoveUser(UserHuntingTeam users, String fullname) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Är du säker att du vill ta bort " + fullname + " från jaktlaget?")
                .setTitle("VARNING")
                .setPositiveButton(R.string.yes_button, (dialog, which) -> {
                    removeUserFromHuntingTeam(users);
                })
                .setNegativeButton(R.string.no_button, (dialog, which) -> {
                    //Needed(?) to show a no button
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void removeUserFromHuntingTeam(UserHuntingTeam user) {
        if (user != null && huntingTeam != null) {

            DocumentReference reference = database.collection("HuntingTeam").document(huntingTeam.getId());
            reference.update("members", FieldValue.arrayRemove(user));
            reference.update("admins", FieldValue.arrayRemove(user));
            Log.d(TAG, user.toString() + " was deleted from " + huntingTeam.toString());
            removeHuntingTeamFromUser(user);
        } else {
            Log.w(TAG, new NullPointerException());
        }
    }

    private void removeHuntingTeamFromUser(UserHuntingTeam user1) {
        DocumentReference reference = database.collection("users").document(user1.getId());
        reference.update("huntingTeams", FieldValue.arrayRemove(huntingTeam));
        Log.d(TAG, huntingTeam.toString() + " was deleted from " + user1.toString());

        //Update the current hunting list and then restart the activity to update the gui. Shit solution.
        database.collection("HuntingTeam").whereEqualTo("connectCode", huntingTeam.getConnectCode())
                .limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<HuntingTeam> list = Objects.requireNonNull(task.getResult()).toObjects(HuntingTeam.class);
                for (HuntingTeam team : list) {
                    SharedPreferencesRepository.addCurrentHuntingTeamToPreferences(context, team);
                    Intent intent = new Intent(context, TeamMembersActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageButton removeButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.textViewTeamMemberName);
            removeButton = itemView.findViewById(R.id.imageButtonTeamMemberItem);
        }
    }
}
