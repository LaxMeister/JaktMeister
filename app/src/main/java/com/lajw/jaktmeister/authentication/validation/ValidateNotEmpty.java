package com.lajw.jaktmeister.authentication.validation;


import android.util.Log;

import java.util.HashMap;

public class ValidateNotEmpty implements Validation {
    final String TAG = this.getClass().getName();

    @Override
    public boolean validate(HashMap<String, String> hashMap) {
        if(hashMap == null){
            Log.e(TAG, "HashMap is null, returns false");
            return false;
        }

        String firstname = hashMap.get("Firstname");
        String lastname = hashMap.get("Lastname");
        String phone = hashMap.get("Phone");
        System.out.println("ValidateNotEmpty \"Firstname \": "+ firstname);
        System.out.println("ValidateNotEmpty \"Lastname \": "+ lastname);
        System.out.println("ValidateNotEmpty \"Phone \": "+ phone);
        return !firstname.isEmpty();
    }

    @Override
    public boolean validate(String input, int minLength, int maxLength) {
        return false;
    }
}
