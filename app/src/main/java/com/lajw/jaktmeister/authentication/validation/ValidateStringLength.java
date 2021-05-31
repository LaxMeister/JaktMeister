package com.lajw.jaktmeister.authentication.validation;

import android.util.Log;

import java.util.HashMap;

public class ValidateStringLength implements Validation {
    final String TAG = this.getClass().getName();

    @Override
    public boolean validate(HashMap<String, String> hashMap) {
        if(hashMap == null){
            Log.e(TAG, "HashMap is null, returns false");
            return false;
        }
        return false;
    }


    public boolean validate(String input, int minLength, int maxLength){
        return true;
    }
}
