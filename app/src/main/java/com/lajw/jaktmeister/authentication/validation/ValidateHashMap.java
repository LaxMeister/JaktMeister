package com.lajw.jaktmeister.authentication.validation;

import java.util.HashMap;

public class ValidateHashMap implements Validation {
    @Override
    public boolean validate(HashMap<String, String> hashMap) {
        return false;
    }

    @Override
    public boolean validate(String input, int minLength, int maxLength) {
        return false;
    }
}
