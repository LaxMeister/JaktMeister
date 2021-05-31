package com.lajw.jaktmeister.huntingteam.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lajw.jaktmeister.MainActivity;
import com.lajw.jaktmeister.R;
import com.lajw.jaktmeister.entity.HuntingTeam;
import com.lajw.jaktmeister.entity.User;
import com.lajw.jaktmeister.huntingteam.HuntingTeamRepository;
import com.lajw.jaktmeister.utils.SharedPreferencesRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class HuntingTeamActivity extends AppCompatActivity {

    private final String TAG = getClass().getName();

    private TextView textViewName, textViewConnectionCode;

    private String name, connectionCode;

    private User currentUser;
    private HuntingTeam currentHuntingTeam;

    private String huntingCodeName;

    private FirebaseFirestore database;
    private final HuntingTeamRepository huntingTeamRepository = new HuntingTeamRepository();
    private List<HuntingTeam> huntingTeams = new ArrayList<>();
    private final List<String> namesForLoadList = new ArrayList<>();
    private String teamName;

    private FirebaseUser firebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunting_team);

        //Auth & DB
        currentUser = SharedPreferencesRepository.getCurrentUser(this);
        currentHuntingTeam = SharedPreferencesRepository.getCurrentHuntingTeam(this);
        database = FirebaseFirestore.getInstance();

        //GUI
        textViewName = (TextView) findViewById(R.id.textViewHuntingTeamName);
        textViewConnectionCode = (TextView) findViewById(R.id.textViewHuntingTeamConnectionCode);

        name = currentHuntingTeam.getName();
        connectionCode = currentHuntingTeam.getConnectCode();

        textViewName.setText(name);
        textViewConnectionCode.setText(connectionCode);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        addCurrentUserToSharedPreferences();
        updateHuntingTeam();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onClickChangeTeam(View view) {
        changeTeam();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onClickJoinTeam(View view) {
        joinTeam();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onClickCreateTeam(View view) {
        createTeam();
    }

    public void onClickMembers(View view) {
        startActivity(new Intent(this, TeamMembersActivity.class));
    }

    public void onClickReturn(View view) {
        finish();
        startActivity(new Intent(HuntingTeamActivity.this, MainActivity.class));
    }

    public void onClickLeaveTeam(View view) {
        boolean success = huntingTeamRepository.removeUserFromHuntingTeam(currentUser, currentHuntingTeam);
        boolean success1 = huntingTeamRepository.removeHuntingTeamFromUser(currentUser, currentHuntingTeam);
        if (success && success1) {
            SharedPreferencesRepository.addCurrentHuntingTeamToPreferences(this, new HuntingTeam(HuntingTeam.VALUE.DEFAULT));
            Intent intent = new Intent(this, HuntingTeamActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(intent);
        } else {
            Log.e(TAG, "Remove user from hunting team success: " + success + ". Remove hunting team from user success: " + success1);
            Toast.makeText(this, "Något gick fel att gå ur laget, testa igen.", Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void createTeam() {
        //TODO: Open AlertDialog to enter team name and run line below
        //
        teamDialog(R.layout.create_team_dialog, "create");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void joinTeam() {
        //TODO: Open AlertDialog to enter connection code and run line below
        //huntingTeamRepository.join(this, "Insert Connection Code here");
        teamDialog(R.layout.join_team_dialog, "join");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void changeTeam() {
        //TODO: Open AlertDialog with a list of teams and run line below
        //SharedPreferencesRepository.addCurrentHuntingTeamToPreferences(this,insertHuntingTeamHere);
        changeTeamDialog();
    }

    private void updateHuntingTeam() {
        database.collection("HuntingTeam").whereEqualTo("connectCode", connectionCode)
                .limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<HuntingTeam> list = Objects.requireNonNull(task.getResult()).toObjects(HuntingTeam.class);
                for (HuntingTeam team : list) {
                    SharedPreferencesRepository.addCurrentHuntingTeamToPreferences(this, team);

                }
            }
        });
    }

    private void addCurrentUserToSharedPreferences() {
        database.collection("users")
                .document(firebaseUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User user = task.getResult().toObject(User.class);
                        SharedPreferencesRepository.addCurrentUserToPreferences(this, user);
                    }
                });
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("InflateParams")
    public void teamDialog(int team, String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(getResources().getLayout(team), null);
        EditText nametext = (EditText) view.findViewById(R.id.info_text);
        builder.setView(view);
//                .setTitle("Skriv in Lagkod");

        AlertDialog dialog = builder.create();
        dialog.show();
        view.findViewById(R.id.join_Btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                huntingCodeName = nametext.getText().toString();
                if (type.equals("join")) {
                    huntingTeamRepository.join(HuntingTeamActivity.this, huntingCodeName);
                } else if (type.equals("create")) {
                    huntingTeamRepository.create(HuntingTeamActivity.this, huntingCodeName);
                }
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.JoinCancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ResourceAsColor")
    public void changeTeamDialog() {
        User user = SharedPreferencesRepository.getCurrentUser(HuntingTeamActivity.this);
        if (user.getHuntingTeams() != null) {
            huntingTeams = user.getHuntingTeams();
        }
        for (int i = 0; i < huntingTeams.size(); i++) {
            namesForLoadList.add(huntingTeams.get(i).getName());
        }
        String[] stringArray = namesForLoadList.toArray(new String[namesForLoadList.size()]);
        String[] selectedString = new String[1];

//        LayoutInflater inflater = HuntingTeamActivity.this.getLayoutInflater();
//        View view = inflater.inflate(R.layout.activity_main, null);
//        NavigationView navigationView = (NavigationView) view.findViewById(R.id.nav_view);
//        View headerView = navigationView.getHeaderView(0);
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(HuntingTeamActivity.this);


        mBuilder.setTitle(R.string.first_dialog_title);
        mBuilder.setSingleChoiceItems(stringArray, -1, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedString[0] = stringArray[which].toString();
            }
        });
        mBuilder.setPositiveButton("Välj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < huntingTeams.size(); i++) {
                    if (huntingTeams.get(i).getName().equals(selectedString[0])) {
                        Log.i("TAG", "Name: " + huntingTeams.get(i));
                        SharedPreferencesRepository.addCurrentHuntingTeamToPreferences(HuntingTeamActivity.this, huntingTeams.get(i));
                        teamName = huntingTeams.get(i).getName();
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                finish();
//                                TextView navHuntingTeam = (TextView) headerView.findViewById(R.id.coolHuntingTeamName);
//                                navHuntingTeam.setText(teamName);
//                                headerView.findViewById(R.id.huntingTeamText).setVisibility(View.VISIBLE);
//                                navHuntingTeam.setVisibility(View.VISIBLE);
                                namesForLoadList.removeAll(namesForLoadList);
                                startActivity(new Intent(HuntingTeamActivity.this, HuntingTeamActivity.class));
                                // this code will be executed after 2 seconds
                            }
                        }, 700);
                    }
                }
            }
        });
        mBuilder.setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                namesForLoadList.removeAll(namesForLoadList);
            }
        });


        AlertDialog mDialog = mBuilder.create();
        mDialog.show();

        FrameLayout.LayoutParams layoutParams1 = new FrameLayout.LayoutParams(1000, 800);
        mDialog.getListView().setLayoutParams(layoutParams1);
        mDialog.getListView().setBackgroundColor(getColor(R.color.cool_light_green));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); //create a new one
        layoutParams.width = 700;
        layoutParams.gravity = Gravity.CENTER; //this is layout_gravity
        mDialog.getButton(mDialog.BUTTON_POSITIVE).setBackgroundColor(getColor(R.color.cool_yellow));
        mDialog.getButton(mDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.white));
        mDialog.getButton(mDialog.BUTTON_NEGATIVE).setBackgroundColor(getColor(R.color.cool_gray));
        mDialog.getButton(mDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.white));
        mDialog.getButton(mDialog.BUTTON_POSITIVE).setLayoutParams(layoutParams);
        mDialog.getButton(mDialog.BUTTON_NEGATIVE).setLayoutParams(layoutParams);

    }


}