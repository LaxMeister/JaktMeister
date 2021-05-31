package com.lajw.jaktmeister.huntingteam.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lajw.jaktmeister.R;
import com.lajw.jaktmeister.entity.HuntingTeam;
import com.lajw.jaktmeister.entity.User;
import com.lajw.jaktmeister.entity.UserHuntingTeam;
import com.lajw.jaktmeister.huntingteam.adapter.MemberAdapter;
import com.lajw.jaktmeister.utils.SharedPreferencesRepository;

import java.util.ArrayList;
import java.util.List;

public class TeamMembersActivity extends AppCompatActivity {

    private MemberAdapter adapter;

    private HuntingTeam huntingTeam;
    private User user;

    private boolean admin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_members);

        user = SharedPreferencesRepository.getCurrentUser(this);
        huntingTeam = SharedPreferencesRepository.getCurrentHuntingTeam(this);

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbarMemberActivity);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Medlemmar");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeamMembersActivity.this, HuntingTeamActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                startActivity(intent);
            }
        });

        List<UserHuntingTeam> huntingTeamList = new ArrayList<>();
        List<UserHuntingTeam> huntingTeamAdminList = new ArrayList<>();

        if (huntingTeam.getMembers() != null) {
            huntingTeamList = huntingTeam.getMembers();
        }
        if (huntingTeam.getAdmins() != null) {
            huntingTeamAdminList = huntingTeam.getAdmins();
        }
        //TODO: Clean it up
        for (UserHuntingTeam user1 : huntingTeamAdminList) {
            if (user1.getId().contains(user.getId())) {
                admin = true;
                break;
            }
        }

        huntingTeamAdminList.addAll(huntingTeamList);

        RecyclerView recyclerView = findViewById(R.id.huntingTeamMembersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MemberAdapter(this, huntingTeamAdminList, admin);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HuntingTeamActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        startActivity(intent);

    }
}