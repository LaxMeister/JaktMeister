package com.lajw.jaktmeister.huntingteam;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lajw.jaktmeister.entity.HuntingTeam;
import com.lajw.jaktmeister.entity.User;
import com.lajw.jaktmeister.entity.UserHuntingTeam;

public class HuntingTeamRepository {

    private final String TAG = getClass().getName();
    private final FirebaseFirestore database = FirebaseFirestore.getInstance();

    public void create(Context context, String teamName) {
        CreateHuntingTeam huntingTeam = new CreateHuntingTeam(context);
        huntingTeam.create(teamName);
    }

    public void join(Context context, String connectionCode) {
        JoinHuntingTeam huntingTeam = new JoinHuntingTeam(context);
        huntingTeam.join(connectionCode);
    }

    public boolean removeUserFromHuntingTeam(User currentUser, HuntingTeam currentHuntingTeam) {
        if (currentUser != null && currentHuntingTeam != null) {
            UserHuntingTeam userHuntingTeam = new UserHuntingTeam(currentUser);

            DocumentReference reference = database.collection("HuntingTeam").document(currentHuntingTeam.getId());
            reference.update("members", FieldValue.arrayRemove(userHuntingTeam));
            reference.update("admins", FieldValue.arrayRemove(userHuntingTeam));
            Log.d(TAG, currentUser.toString() + " was deleted from " + currentHuntingTeam.toString());
            return true;
        } else {
            return false;
        }
    }

    public boolean removeHuntingTeamFromUser(User currentUser, HuntingTeam currentHuntingTeam) {
        if (currentUser != null && currentHuntingTeam != null) {
            DocumentReference reference = database.collection("users").document(currentUser.getId());
            reference.update("huntingTeams", FieldValue.arrayRemove(currentHuntingTeam));
            Log.d(TAG, currentHuntingTeam.toString() + " was deleted from " + currentUser.toString());
            return true;
        } else {
            return false;
        }
    }
}
