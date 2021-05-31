package com.lajw.jaktmeister.huntingteam;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lajw.jaktmeister.entity.HuntingTeam;
import com.lajw.jaktmeister.entity.User;
import com.lajw.jaktmeister.entity.UserHuntingTeam;
import com.lajw.jaktmeister.huntingteam.activity.HuntingTeamActivity;
import com.lajw.jaktmeister.utils.SharedPreferencesRepository;
import com.lajw.jaktmeister.utils.StringGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class CreateHuntingTeam {

    private final String TAG = getClass().getName();

    private ProgressBar progressBar;

    private FirebaseFirestore database;
    private FirebaseUser firebaseUser;

    AtomicReference<HuntingTeam> huntingTeam = new AtomicReference<>();
    private User currentUser;

    private Context context;

    public CreateHuntingTeam() {

    }

    public CreateHuntingTeam(Context context) {
        this.context = context;
        database = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUser = SharedPreferencesRepository.getCurrentUser(context);
    }


    public void create(String teamName) {
        String teamCode = StringGenerator.randomizeAlphanumericalString(8);

        if (validateInputs(teamName)) {
            checkAndUpdateConnectionCodeFlag(teamCode, teamName);
        }
    }

    //1
    private void checkAndUpdateConnectionCodeFlag(String connectCode, String teamName) {
        database.collection("HuntingTeam").whereEqualTo("connectCode", connectCode)
                .limit(1).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<HuntingTeam> test = task.getResult().toObjects(HuntingTeam.class);
                        if (test.size() > 0) {
                            String newConnectCode = StringGenerator.randomizeAlphanumericalString(8);
                            checkAndUpdateConnectionCodeFlag(newConnectCode, teamName);
                        } else {
                            for (HuntingTeam team : test) {
                                huntingTeam.set(team);
                            }
                            createTeam(connectCode, teamName);
                        }
                    } else {
                        Log.d(TAG, "Task was not successful");
                    }
                });
    }

    //2
    private void createTeam(String code, String name) {
        UserHuntingTeam userHuntingTeam = new UserHuntingTeam(currentUser);


        String id = database.collection("HuntingTeam").document().getId();

        List<UserHuntingTeam> adminList = new ArrayList<>();
        adminList.add(userHuntingTeam);
        HuntingTeam huntingTeam = new HuntingTeam(id, code, name, adminList);


        database.collection("HuntingTeam").document(id).set(huntingTeam)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "Jaktlaget har skapats", Toast.LENGTH_LONG).show();
                    getHuntingTeam(id);
                }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(context, "Något gick fel", Toast.LENGTH_LONG).show();
            Log.w(TAG, "Error adding document", e);
        });
    }

    //3
    private void getHuntingTeam(String id) {
        database.collection("HuntingTeam").whereEqualTo("id", id)
                .limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<HuntingTeam> list = Objects.requireNonNull(task.getResult()).toObjects(HuntingTeam.class);
                System.out.println("List size:" + list.size());
                for (HuntingTeam team : list) {
                    huntingTeam.set(team);
                    addHuntingTeamToUser();
                }
            }
        });
    }

    //4
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
                    createUpdatedActivity();
                }).addOnFailureListener(e -> {
            Toast.makeText(context, "Något gick fel", Toast.LENGTH_LONG).show();
        });
    }

    public boolean validateInputs(String name) {
        boolean temp = true;

        if (name.isEmpty() || name.length() < 2 || name.length() > 62) {
            Toast.makeText(context, "Namnet kräver 2-62 tecken. Endast A-Ö, 0-9, - och _ är tillåtna.", Toast.LENGTH_LONG).show();
            temp = false;
        }

        if (name.equals("Ej med i jaktlag") || name.equals("ej med i jaktlag")) {
            Toast.makeText(context, "Jaktlaget finns redan", Toast.LENGTH_LONG).show();
            temp = false;
        }

        if (name.length() > 62) {
            Toast.makeText(context, "Namnet kan inte ha mer än 62 tecken. Endast A-Ö, 0-9, - och _ är tillåtna.", Toast.LENGTH_LONG).show();
            temp = false;
        }

        if (!name.matches("[a-zA-Z0-9åäöÅÄÖ_\\s-]+")) {
            Toast.makeText(context, "Endast tecken A-Ö, 0-9, - och _ är tillåtna.", Toast.LENGTH_LONG).show();
            temp = false;
        }
        return temp;
    }

    private void createUpdatedActivity() {
        Intent intent = new Intent(context, HuntingTeamActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }
}