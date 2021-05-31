package com.lajw.jaktmeister.authentication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.PatternsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lajw.jaktmeister.R;
import com.lajw.jaktmeister.authentication.EULADialog;
import com.lajw.jaktmeister.authentication.GDPRDialog;
import com.lajw.jaktmeister.entity.User;
import com.lajw.jaktmeister.authentication.EULADialog;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    //GUI
    private EditText editTextFirstname, editTextLastname, editTextEmail, editTextPhoneNumber, editTextPassword, editTextRepeatPassword;
    private ProgressBar progressBar;
    private CheckBox checkBoxEULA, checkBoxGDPR;

    //In-class variables
    private boolean checkBoxEULAState, checkBoxGDPRState;
    private boolean emptyEmail;
    private final String TAG = getClass().getName();

    //Auth
    private FirebaseAuth mAuth;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Auth
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();

        //GUI
        editTextFirstname = (EditText) findViewById(R.id.editTextRegisterFirstName);
        editTextLastname = (EditText) findViewById(R.id.editTextRegisterLastName);
        editTextEmail = (EditText) findViewById(R.id.editTextRegisterEmail);
        editTextPhoneNumber = (EditText) findViewById(R.id.editTextRegisterPhoneNumber);
        editTextPassword = (EditText) findViewById(R.id.editTextRegisterPassword);
        editTextRepeatPassword = (EditText) findViewById(R.id.editTextRegisterRepeatPassword);
        progressBar = (ProgressBar) findViewById(R.id.progressBarRegisterUser);

        //CheckBox
        checkBoxEULA = (CheckBox) findViewById(R.id.checkBoxRegisterEULA);
        checkBoxGDPR = (CheckBox) findViewById(R.id.checkBoxRegisterGDPR);
        checkBoxEULAState = false;
        checkBoxGDPRState = false;

    }

    //On-click and Buttons
    public void onClickRegisterUser(View view) {
        String firstname = editTextFirstname.getText().toString().trim();
        String lastname = editTextLastname.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim().toLowerCase();
        String phone = editTextPhoneNumber.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String repeatPassword = editTextRepeatPassword.getText().toString().trim();

        HashMap<String, String> map = new HashMap<>();
        map.put("Firstname", firstname);
        map.put("Lastname", lastname);
        map.put("Email", email);
        map.put("Phone", phone);
        map.put("Password", password);
        map.put("RepeatPassword", repeatPassword);

        //ValidationService validationService = new ValidationService(map);

        if (validateUserInputs(firstname, lastname, email, phone, password, repeatPassword)) {
            if (!validateCheckBoxes()) {
                return;
            }
            checkIfEmailExists(email);
            if (emptyEmail) {
                editTextEmail.setError("Epost-addressen finns redan");
                editTextEmail.requestFocus();
                setEmptyEmail(false);
            } else {
                progressBar.setVisibility(View.VISIBLE);
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                User user = new User(mAuth.getUid(), firstname, lastname, email, phone, true, true, false);
                                database.collection("users").document(mAuth.getUid())
                                        .set(user)
                                        .addOnSuccessListener(documentReference -> {
                                            Toast.makeText(RegisterActivity.this, "Kontot har skapats", Toast.LENGTH_LONG).show();
                                            Log.d(TAG, "DocumentSnapshot added with ID: " + mAuth.getUid());
                                            progressBar.setVisibility(View.GONE);
                                            returnToLoginActivity();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(RegisterActivity.this, "Något gick fel", Toast.LENGTH_LONG).show();
                                            Log.w(TAG, "Error adding document", e);
                                            progressBar.setVisibility(View.GONE);
                                        });
                            }
                        });
            }
        }

    }

    public void cancelButton(View view) {
        onBackPressed();
        finish();
    }

    public void onCheckBoxClicked(@NotNull View view) {
        boolean checked = ((CheckBox) view).isChecked();

        switch (view.getId()) {
            case R.id.checkBoxRegisterEULA:
                if (checked) {
                    openEulaDialog();
                    setCheckBoxEULAState(true);
                } else {
                    setCheckBoxEULAState(false);
                }
                break;
            case R.id.checkBoxRegisterGDPR:
                if (checked) {
                    openGdprDialog();
                    setCheckBoxGDPRState(true);
                } else {
                    setCheckBoxGDPRState(false);
                }
                break;
        }
    }


    //Validation
    public boolean validateUserInputs(String firstname, String lastname, String email, String phone, String password, String repeatPassword) {
        boolean temp = true;

        if (firstname.isEmpty()) {
            editTextFirstname.setError("Förnamn saknas");
            editTextFirstname.requestFocus();
            temp = false;
        }
        if (lastname.isEmpty()) {
            editTextLastname.setError("Efternamn saknas");
            editTextLastname.requestFocus();
            temp = false;

        }
        if (email.isEmpty()) {
            editTextEmail.setError("Epost-address saknas");
            editTextEmail.requestFocus();
            temp = false;
        }
        if (!PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Fel format på epost-addressen");
            editTextEmail.requestFocus();
            temp = false;
        }
        if (phone.isEmpty() || !phone.matches("^(([+]46)\\s*(7)|07)[02369]\\s*(\\d{4})\\s*(\\d{3})$")) {
            editTextPhoneNumber.setError("Felaktigt telefonnummer. Enbart följande format är godkänt: +46701234567 / 0701234567");
            editTextPhoneNumber.requestFocus();
            temp = false;
        }
        if (password.length() < 6) {
            editTextPassword.setError("Lösenordet måste vara minst 6 tecken");
            editTextPassword.requestFocus();
            temp = false;
        }
        if (!password.contentEquals(repeatPassword) || repeatPassword.isEmpty()) {
            editTextRepeatPassword.setError("Lösenorden överensstämmer inte med varandra");
            editTextRepeatPassword.requestFocus();
            temp = false;
        }
        if (!temp) {
            return false;
        }
        return true;
    }

    public boolean validateCheckBoxes() {
        if (!checkBoxEULAState) {
            checkBoxEULA.setError("Avtalet måste godkännas");
            checkBoxEULA.requestFocus();
            return false;
        }
        checkBoxEULA.setError(null);
        if (!checkBoxGDPRState) {
            checkBoxGDPR.setError("Avtalet måste godkännas");
            checkBoxGDPR.requestFocus();
            return false;
        }
        //Needed to remove the error after successful validation
        checkBoxGDPR.setError(null);
        return true;
    }

    private void checkIfEmailExists(String email) {
        database.collection("users").whereEqualTo("email", email)
                .limit(1).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        emptyEmail = Objects.requireNonNull(task.getResult()).isEmpty();
                        if (emptyEmail) {
                            Log.d(TAG, "Email:" + email + " doesn't already exists");
                            setEmptyEmail(true);
                        } else {
                            Toast.makeText(RegisterActivity.this, "Epost-addressen finns redan", Toast.LENGTH_LONG).show();
                            editTextEmail.setError("Epost-addressen finns redan");
                            editTextEmail.requestFocus();
                            progressBar.setVisibility(View.GONE);
                            Log.d(TAG, "Email:" + email + " already exists");
                            setEmptyEmail(false);
                        }
                    }
                });
    }

    //Other
    private void returnToLoginActivity() {
        //User seems to signin when creating account, so need to sign out before going to LoginActivity
        mAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void openEulaDialog() {
        EULADialog dialog = new EULADialog();
        dialog.show(getSupportFragmentManager(), "eula");
    }

    public void openGdprDialog() {
        GDPRDialog dialog = new GDPRDialog();
        dialog.show(getSupportFragmentManager(), "gdpr");
    }

    //Getters and Setters
    public boolean isEmptyEmail() {
        return emptyEmail;
    }

    public void setEmptyEmail(boolean emptyEmail) {
        this.emptyEmail = emptyEmail;
    }

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