package com.lajw.jaktmeister;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.lajw.jaktmeister.authentication.activity.LoginActivity;
import com.lajw.jaktmeister.chat.activity.ChatActivity;
import com.lajw.jaktmeister.entity.HuntingTeam;
import com.lajw.jaktmeister.entity.User;
import com.lajw.jaktmeister.utils.SharedPreferencesRepository;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    private final String TAG = "Topic";
    //Test for dev, delete later
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;
    private FirebaseAuth mAuth;
    private int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        count++;
        //Test for dev, delete later
        mAuth = FirebaseAuth.getInstance();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        Log.i(TAG, "USER: " + SharedPreferencesRepository.getCurrentUser(MainActivity.this));
        User User = SharedPreferencesRepository.getCurrentUser(this);
        String userName = SharedPreferencesRepository.getCurrentUser(MainActivity.this).getFirstName() + " " + SharedPreferencesRepository.getCurrentUser(MainActivity.this).getLastName();
        TextView navUsername = (TextView) headerView.findViewById(R.id.coolUserName);
        navUsername.setText(userName);

        HuntingTeam currentHuntingTeam = SharedPreferencesRepository.getCurrentHuntingTeam(this);
        TextView navHuntingTeam = (TextView) headerView.findViewById(R.id.coolHuntingTeamName);

        if (currentHuntingTeam != null) {
            navHuntingTeam.setText(currentHuntingTeam.getName());
            String TOPIC = SharedPreferencesRepository.getCurrentHuntingTeam(this).getName().replace(" ", "");
            FirebaseMessaging.getInstance().subscribeToTopic(TOPIC.replace(" ", ""));
            FirebaseMessaging.getInstance().subscribeToTopic(TOPIC.replace(" ", "") + "Stand");
        } else {
            headerView.findViewById(R.id.huntingTeamText).setVisibility(View.INVISIBLE);
            navHuntingTeam.setVisibility(View.INVISIBLE);
        }


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    //Delete later
    public void onClickLogout() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                task -> startActivity(new Intent(MainActivity.this, LoginActivity.class)));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void goToGroupChatFromMapMenuTop(MenuItem item) {
        HuntingTeam huntingTeam = SharedPreferencesRepository.getCurrentHuntingTeam(this);
        if (huntingTeam.getName().equals("Ej med i jaktlag")) {
            Toast.makeText(MainActivity.this, "Gå med i eller skapa ett jaktlag för att använda denna funktion", Toast.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(this, ChatActivity.class);
            String id = huntingTeam.getId();
            intent.putExtra("huntingTeamId", id);
            startActivity(intent);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        HuntingTeam currentHuntingTeam = SharedPreferencesRepository.getCurrentHuntingTeam(this);
        if (currentHuntingTeam != null) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(SharedPreferencesRepository.getCurrentHuntingTeam(this).getName().replace(" ", ""));
        }
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}