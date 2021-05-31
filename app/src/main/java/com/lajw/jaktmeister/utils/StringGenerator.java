package com.lajw.jaktmeister.utils;

public class StringGenerator {

    public static String randomizeAlphanumericalString(int numberOfCharacters){
        String AlphaNumericString ="ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        StringBuilder sb = new StringBuilder(numberOfCharacters);

        for(int i = 0; i < numberOfCharacters; i++){
            int index = (int)(AlphaNumericString.length()*Math.random());
            sb.append(AlphaNumericString.charAt(index));
        }

        return sb.toString();
    }
}
