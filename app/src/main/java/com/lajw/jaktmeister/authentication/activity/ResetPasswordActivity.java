package com.lajw.jaktmeister.authentication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.PatternsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.lajw.jaktmeister.R;

public class ResetPasswordActivity extends AppCompatActivity {
    //GUI
    private EditText editTextEmail;
    private ProgressBar progressBar;

    //Auth
    private FirebaseAuth mAuth;

    //In-class variables
    private final String TAG = this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        //Auth
        mAuth = FirebaseAuth.getInstance();

        //GUI
        editTextEmail = (EditText) findViewById(R.id.editTextResetEmail);
        progressBar = (ProgressBar) findViewById(R.id.progressBarReset);
    }



    private void resetPassword() {
        String email = editTextEmail.getText().toString().trim().toLowerCase();

        validateEmail(email);

        progressBar.setVisibility(View.VISIBLE);
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ResetPasswordActivity.this, "En länk har skickats till din epost för att återställa lösenordet", Toast.LENGTH_LONG).show();
                Log.d(TAG,"Password reset sent to email: "+email+" was successful");
                startActivity(new Intent(this, LoginActivity.class));
            } else {
                Toast.makeText(ResetPasswordActivity.this, "Epost-addressen hittades inte", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                Log.d(TAG,"Email: "+email+" was not found");
            }
        });
    }

    //Validation
    private void validateEmail(String email){
        if (email.isEmpty()) {
            editTextEmail.setError("Epost-address saknas");
            editTextEmail.requestFocus();
            return;
        }
        if (!PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Fel format på epost-addressen");
            editTextEmail.requestFocus();
            return;
        }
    }

    //On-click and Buttons
    public void onClickReset(View view) {
        resetPassword();
    }

    public void cancelButton(View view) {
        onBackPressed();
    }


}