package com.lajw.jaktmeister.authentication.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.PatternsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.lajw.jaktmeister.MainActivity;
import com.lajw.jaktmeister.R;
import com.lajw.jaktmeister.entity.HuntingTeam;
import com.lajw.jaktmeister.entity.User;
import com.lajw.jaktmeister.utils.SharedPreferencesRepository;

public class LoginActivity extends AppCompatActivity {
    //GUI
    private EditText editTextEmail, editTextPassword;
    private CheckBox keepSignedIn;
    private ProgressBar progressBar;
    private Button loginButton;

    //Auth
    private final int RC_SIGN_IN = 120;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    //Database
    private FirebaseFirestore db;

    //In-class variables
    private boolean keepSignedInState;
    private final String TAG = getClass().getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //GUI
        editTextEmail = (EditText) findViewById(R.id.editTextLoginEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextLoginPassword);
        progressBar = (ProgressBar) findViewById(R.id.progressBarLogin);
        keepSignedIn = (CheckBox) findViewById(R.id.checkBoxLoginKeepSignedIn);
        loginButton = (Button) findViewById(R.id.buttonLoginLogin);

        progressBar.setVisibility(View.INVISIBLE);
        //In-class variables
        keepSignedInState = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        //If user is signed in, skip this.login page and go straight to MainActivity
        if (mFirebaseUser != null) {
            editTextEmail.setText(mAuth.getCurrentUser().getEmail());
            DocumentReference docRef = db.collection("users").document(mFirebaseUser.getUid());
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (document.getBoolean("keepMeSignedIn")) {
                            addCurrentUserToSharedPreferences();
                            progressBar.setVisibility(View.VISIBLE);
                            editTextPassword.setText("skjutettdjur");
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        } else {
                            mAuth.signOut();
                        }
                    } else {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            });
        }
    }

    //Authentication
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser mUser = mAuth.getCurrentUser();

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference docRef = db.collection("users").document(mUser.getUid());
                        docRef.get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                DocumentSnapshot document = task1.getResult();
                                if (document.exists()) {
                                    addCurrentUserToSharedPreferences();
                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                    startActivity(new Intent(this, MainActivity.class));
                                } else {
                                    Log.d(TAG, "No such document");
                                    startActivity(new Intent(this, FirstLoginGoogleSignInActivity.class));
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task1.getException());
                            }
                        });
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                    }
                });
    }

    private void loginUser() {
        loginButton.setEnabled(false);
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        if (validateInput(email, password)) {
            progressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(
                    task -> {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (task.isSuccessful()) {
                            if (user != null) {
                                if (user.isEmailVerified()) {
                                    keepUserSignedIn(user.getUid(), keepSignedInState);
                                    SharedPreferencesRepository.addCurrentHuntingTeamToPreferences(LoginActivity.this, new HuntingTeam(HuntingTeam.VALUE.DEFAULT));
                                    addCurrentUserToSharedPreferences();

                                } else {
                                    user.sendEmailVerification();
                                    Toast.makeText(LoginActivity.this, "Du måste verifiera din email. Kolla din email efter en länk", Toast.LENGTH_LONG).show();
                                    loginButton.setEnabled(true);
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            }
                        } else {
                            editTextPassword.setError("Felaktigt lösenord");
                            editTextPassword.requestFocus();
                            progressBar.setVisibility(View.INVISIBLE);
                            //Disables the button for 2 seconds so the user can't spam the button while loading and for 2 seconds after its done loading
                            loginButton.postDelayed(() -> loginButton.setEnabled(true), 2000);

                        }
                    }
            );
        } else {
            loginButton.setEnabled(true);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void keepUserSignedIn(String userUid, boolean keepMeSignedIn) {
        DocumentReference sfDocRef = db.collection("users").document(userUid);
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            transaction.update(sfDocRef, "keepMeSignedIn", keepMeSignedIn);
            return null;
        }).addOnSuccessListener(aVoid -> Log.d(TAG, "Transaction success!"))
                .addOnFailureListener(e -> Log.w(TAG, "Transaction failure.", e));
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    //Validation
    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            editTextEmail.setError("Epost-address saknas");
            editTextEmail.requestFocus();
            return false;
        }
        if (!PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Fel format på epost-addressen");
            editTextEmail.requestFocus();
            return false;
        }
        if (password.isEmpty()) {
            editTextPassword.setError("Lösenord saknas");
            editTextPassword.requestFocus();
            return false;
        }
        return true;
    }

    //On-click and Buttons
    public void onClickGoogleSignIn(View view) {
        signIn();
    }

    public void loginOnClick(View view) {
        loginUser();
    }

    public void openRegisterActivity(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void openResetPasswordActivity(View view) {
        startActivity(new Intent(this, ResetPasswordActivity.class));
    }

    @SuppressLint("NonConstantResourceId")
    public void onCheckBoxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        setKeepSignedInState(checked);
    }

    //Getters and Setters
    public boolean isKeepSignedInState() {
        return keepSignedInState;
    }

    public void setKeepSignedInState(boolean keepSignedInState) {
        this.keepSignedInState = keepSignedInState;
    }

    private void addCurrentUserToSharedPreferences() {

        db.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User user = task.getResult().toObject(User.class);
                        SharedPreferencesRepository.addCurrentUserToPreferences(this, user);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }
                });

        Log.i("USER", "LOGINUSER:" + mAuth.getCurrentUser().getUid());
    }
}