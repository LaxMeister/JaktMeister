package com.lajw.jaktmeister.huntingteam.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.lajw.jaktmeister.MainActivity;
import com.lajw.jaktmeister.R;
import com.lajw.jaktmeister.entity.HuntingTeam;
import com.lajw.jaktmeister.utils.SharedPreferencesRepository;

import java.util.List;
import java.util.Objects;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.ViewHolder> {

    private Context context;
    private List<HuntingTeam> mTeams;
    private FirebaseFirestore database;


    public TeamAdapter() {

    }

    public TeamAdapter(Context context, List<HuntingTeam> mTeams) {
        this.context = context;
        this.mTeams = mTeams;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.team_item_login,
                parent,
                false);

        database = FirebaseFirestore.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final HuntingTeam teams = mTeams.get(position);

        holder.name.setText(teams.getName());
        holder.itemView.setOnClickListener(v -> {

            database.collection("HuntingTeam").whereEqualTo("connectCode", teams.getConnectCode())
                    .limit(1).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<HuntingTeam> list = Objects.requireNonNull(task.getResult()).toObjects(HuntingTeam.class);
                    for (HuntingTeam team : list) {
                        SharedPreferencesRepository.addCurrentHuntingTeamToPreferences(context, team);
                        Intent i = new Intent(context, MainActivity.class);
                        context.startActivity(i);
                    }
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return mTeams.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textViewTeamItem);
        }
    }
}