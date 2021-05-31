package com.lajw.jaktmeister.authentication.validation;

import android.util.Log;

import androidx.core.util.PatternsCompat;

import java.util.HashMap;

public class ValidateEmail implements Validation {
    final String TAG = this.getClass().getName();

    @Override
    public boolean validate(HashMap<String, String> hashMap) {
        if(hashMap == null){
            Log.e(TAG, "HashMap is null, returns false");
            return false;
        }

        String email = hashMap.get("Email");
        System.out.println("ValidateEmail \"Email\": "+email);
        return PatternsCompat.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public boolean validate(String input, int minLength, int maxLength) {
        return false;
    }
}
