package com.lajw.jaktmeister.authentication.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ValidationService implements Validation {

    private List<Validation> validations;
    private HashMap<String, String> hashMap;

    public ValidationService(HashMap<String, String> hashMap){
        validations = new ArrayList<>();
        this.hashMap = hashMap;
        this.validations.add(new ValidateEmail());
        this.validations.add(new ValidateNotEmpty());
//        this.validations.add(new ValidateStringLength());

        for(Validation v : validations){
            v.validate(hashMap);
        }

    }

    public boolean returnValidation(){

        return false;
    }

    @Override
    public boolean validate(HashMap<String, String> hashMap) {
        return false;
    }

    @Override
    public boolean validate(String input, int minLength, int maxLength) {
        return false;
    }

}
