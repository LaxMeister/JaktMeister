package com.lajw.jaktmeister.huntingteam;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.lajw.jaktmeister.entity.HuntingTeam;
import com.lajw.jaktmeister.entity.User;
import com.lajw.jaktmeister.entity.UserHuntingTeam;
import com.lajw.jaktmeister.huntingteam.activity.HuntingTeamActivity;
import com.lajw.jaktmeister.utils.SharedPreferencesRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class JoinHuntingTeam {


    private final String TAG = getClass().getName();

    private EditText connectionCode;

    private final FirebaseFirestore database;

    AtomicReference<HuntingTeam> huntingTeam = new AtomicReference<>();

    private final User currentUser;

    private final Context context;

    public JoinHuntingTeam(Context context) {
        this.context = context;
        database = FirebaseFirestore.getInstance();
        currentUser = SharedPreferencesRepository.getCurrentUser(context);
    }


    public void join(String connectionCode) {

        String validatedCode = connectionCode.trim().toUpperCase();
        if (validatedCode.isEmpty() || validatedCode.length() < 8 || !validatedCode.matches("[a-zA-Z0-9åäöÅÄÖ_\\s-]+")) {
            Toast.makeText(context, "En anslutningskod har 8 tecken, enbart bokstäver och siffror", Toast.LENGTH_SHORT).show();
            return;
        }
        afterGetCurrentUserGetHuntingTeam(validatedCode);

    }


    //1
    private void afterGetCurrentUserGetHuntingTeam(String connCode) {
        database.collection("HuntingTeam").whereEqualTo("connectCode", connCode)
                .limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<HuntingTeam> list = Objects.requireNonNull(task.getResult()).toObjects(HuntingTeam.class);
                if (list.size() < 1) {
                    //TODO: Fix this, it crashes
                    //connectionCode.setError("Hittade inget jaktlag. Fråga jaktledaren för anslutningskoden");
                    //connectionCode.requestFocus();
                    return;
                }
                for (HuntingTeam team : list) {
                    huntingTeam.set(team);
                    afterGetHuntingTeamAddUserToTeam();
                }
            }
        });
    }


    //2
    private void afterGetHuntingTeamAddUserToTeam() {
        boolean userAlreadyExists = false;
        UserHuntingTeam userHuntingTeam = new UserHuntingTeam(currentUser);

        HuntingTeam team = huntingTeam.get();
        List<UserHuntingTeam> list = new ArrayList<>();
        List<UserHuntingTeam> adminList = new ArrayList<>();

        if (team.getMembers() != null) {
            list = team.getMembers();

        }
        if (team.getAdmins() != null) {
            adminList = team.getAdmins();
        }
        for (UserHuntingTeam u : adminList) {
            if (u.getId().equals(currentUser.getId())) {
                if (u.getId().equals(currentUser.getId())) {
                    Toast.makeText(context, "Du är redan med i jaktlaget", Toast.LENGTH_LONG).show();
                    userAlreadyExists = true;
                    return;
                }
            }
        }

        for (UserHuntingTeam u : list) {
            if (u.getId().equals(currentUser.getId())) {
                Toast.makeText(context, "Du är redan med i jaktlaget", Toast.LENGTH_LONG).show();
                userAlreadyExists = true;
                return;
            }
        }
        if (!userAlreadyExists) {
            list.add(userHuntingTeam);
            team.setMembers(list);
            database.collection("HuntingTeam")
                    .document(team.getId())
                    .set(team)
                    .addOnSuccessListener(documentReference -> {
                        addHuntingTeamToUser();

                    }).addOnFailureListener(e -> {
                Toast.makeText(context, "Något gick fel", Toast.LENGTH_LONG).show();
            });
        }
    }

    //3
    private void addHuntingTeamToUser() {
        List<HuntingTeam> tempList;
        if (currentUser.getHuntingTeams() != null) {
            tempList = currentUser.getHuntingTeams();
        } else {
            tempList = new ArrayList<>();
        }
        tempList.add(huntingTeam.get());
        currentUser.setHuntingTeams(tempList);

        database.collection("users")
                .document(currentUser.getId())
                .set(currentUser)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "Du har gått med i jaktlaget", Toast.LENGTH_LONG).show();
                    SharedPreferencesRepository.addCurrentHuntingTeamToPreferences(context, huntingTeam.get());
                    Log.d(TAG, huntingTeam.get().toString());
                    createUpdatedActivity();
                }).addOnFailureListener(e -> {
            Toast.makeText(context, "Något gick fel", Toast.LENGTH_LONG).show();
        });
    }

    private void createUpdatedActivity() {
        Intent intent = new Intent(context, HuntingTeamActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

}