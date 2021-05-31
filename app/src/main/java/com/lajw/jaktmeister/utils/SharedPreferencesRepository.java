package com.lajw.jaktmeister.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.lajw.jaktmeister.entity.HuntingTeam;
import com.lajw.jaktmeister.entity.User;

import static android.content.Context.MODE_PRIVATE;

public class SharedPreferencesRepository {

    public static void addCurrentUserToPreferences(Context context, User user){
        SharedPreferences prefs = context.getSharedPreferences("USER", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        editor.putString("UserObject", json);
        editor.commit();
    }

    public static User getCurrentUser(Context context){
        SharedPreferences prefs = context.getSharedPreferences("USER", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("UserObject","");
        User user = gson.fromJson(json, User.class);

        return user;
    }

    public static void addCurrentHuntingTeamToPreferences(Context context, HuntingTeam huntingTeam){
        SharedPreferences prefs = context.getSharedPreferences("HUNTINGTEAM", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(huntingTeam);
        editor.putString("HuntingTeamObject", json);
        editor.commit();
    }

    public static HuntingTeam getCurrentHuntingTeam(Context context){
        SharedPreferences prefs = context.getSharedPreferences("HUNTINGTEAM", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("HuntingTeamObject","");
        HuntingTeam huntingTeam = gson.fromJson(json, HuntingTeam.class);

        return huntingTeam;
    }
}
