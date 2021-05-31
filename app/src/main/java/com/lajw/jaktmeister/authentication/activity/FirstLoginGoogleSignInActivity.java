package com.lajw.jaktmeister.authentication.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lajw.jaktmeister.MainActivity;
import com.lajw.jaktmeister.R;
import com.lajw.jaktmeister.authentication.EULADialog;
import com.lajw.jaktmeister.authentication.GDPRDialog;
import com.lajw.jaktmeister.entity.User;

public class FirstLoginGoogleSignInActivity extends AppCompatActivity {
    //GUI
    private TextView mName, mEmail, mPhone;
    private CheckBox checkBoxEULA, checkBoxGDPR;

    //In-class variables
    private boolean checkBoxEULAState, checkBoxGDPRState;
    private final String TAG = this.getClass().getName();

    //Auth
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;

    //Firestore
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_logged_in_user);

        //Auth
        mAuth = FirebaseAuth.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        //Sign in with Google
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //GUI
        mName = findViewById(R.id.textViewTempName);
        mEmail = findViewById(R.id.textViewTempEmail);
        mPhone = (EditText) findViewById(R.id.editTextTempPhone);
        checkBoxEULA = (CheckBox) findViewById(R.id.checkBoxGoogleSignInEULA);
        checkBoxGDPR = (CheckBox) findViewById(R.id.checkBoxGoogleSignInGDPR);

        //In-class variables
        checkBoxEULAState = false;
        checkBoxGDPRState = false;

        getGoogleCredentials();

    }


    //On-click and Buttons
    public void onClickUpdatePhonenumber(View view){
        if(!validatePhoneNumber(mPhone.getText().toString().trim())){
            return;
        }
        if(!validateCheckBoxes()){
            return;
        }
        String userUid = mUser.getUid();
        User user = createUser(mName.getText().toString(), mEmail.getText().toString(), mPhone.getText().toString());

        createDocumentWithUpdatedCredentials(userUid, user);

    }
    public void onClickLogout(View view) {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                task -> startActivity(new Intent(FirstLoginGoogleSignInActivity.this, LoginActivity.class)));

    }

    @SuppressLint("NonConstantResourceId")
    public void onCheckBoxClicked(View view){
        boolean checked = ((CheckBox) view).isChecked();

        switch(view.getId()){
            case R.id.checkBoxGoogleSignInEULA:
                if(checked){
                    openEulaDialog();
                    setCheckBoxEULAState(true);
                }else{
                    setCheckBoxEULAState(false);
                }break;
            case R.id.checkBoxGoogleSignInGDPR:
                if(checked){
                    openGdprDialog();
                    setCheckBoxGDPRState(true);
                }else{
                    setCheckBoxGDPRState(false);
                }break;
        }
    }

    //CRUD
    private void createDocumentWithUpdatedCredentials(String userUid, User user){
        DocumentReference docRef = db.collection("users").document(userUid);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                } else {
                    Log.d(TAG, "No such document, adding new document...");
                    db.collection("users").document(userUid).set(user);
                    Log.d(TAG, "...Document added with ID: " +userUid);
                }
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    public User createUser(String fullName, String email, String phoneNumber){
        User user = new User();
        //Google-accounts only handles a Full name (First + Last name) in one string, split into two
        String[] fullname = fullName.split(" ");
        user.setEmail(email);
        user.setFirstName(fullname[0].trim());
        user.setLastName(fullname[1].trim());
        user.setPhoneNumber(phoneNumber);
        user.setEulaAccepted(true);
        user.setGdprAccepted(true);
        return user;
    }

    //Validation
    public boolean validateCheckBoxes(){
        if(!checkBoxEULAState){
            checkBoxEULA.setError("Avtalet måste godkännas");
            checkBoxEULA.requestFocus();
            return false;
        }
        checkBoxEULA.setError(null);
        if(!checkBoxGDPRState){
            checkBoxGDPR.setError("Avtalet måste godkännas");
            checkBoxGDPR.requestFocus();
            return false;
        }
        checkBoxGDPR.setError(null);
        return true;
    }

    public boolean validatePhoneNumber(String phone){
        if (phone.isEmpty()) {
            mPhone.setError("Telefonnummer saknas");
            mPhone.requestFocus();
            return false;
        }
        return true;
    }

    //Other
    public void openEulaDialog(){
        EULADialog dialog = new EULADialog();
        dialog.show(getSupportFragmentManager(),"eula");
    }

    public void openGdprDialog(){
        GDPRDialog dialog = new GDPRDialog();
        dialog.show(getSupportFragmentManager(),"gdpr");
    }

    private void getGoogleCredentials(){
        mName.setText(mUser.getDisplayName());
        mEmail.setText(mUser.getEmail());
        mPhone.setText(mUser.getPhoneNumber());
    }

    //Getters and Setters
    public Boolean getCheckBoxEULAState() {
        return checkBoxEULAState;
    }

    public void setCheckBoxEULAState(boolean checkBoxEULAState) {
        this.checkBoxEULAState = checkBoxEULAState;
    }

    public Boolean getCheckBoxGDPRState() {
        return checkBoxGDPRState;
    }

    public void setCheckBoxGDPRState(boolean checkBoxGDPRState) {
        this.checkBoxGDPRState = checkBoxGDPRState;
    }
}