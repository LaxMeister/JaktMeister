package com.lajw.jaktmeister.authentication.validation;

import java.util.HashMap;

public interface Validation {

    public boolean validate(HashMap<String, String> hashMap);
    public boolean validate(String input, int minLength, int maxLength);
}
