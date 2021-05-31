package com.lajw.jaktmeister.entity;

import java.util.List;

public class User {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private List<HuntingTeam> huntingTeams;
    private HuntingTeam currentHuntingTeam;
    private boolean eulaAccepted;
    private boolean gdprAccepted;
    private boolean keepMeSignedIn;

    public User() {
    }

    public User(String firstName, String lastName, String email, String phoneNumber) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public User(String id, String firstName, String lastName, String email, String phoneNumber, boolean eulaAccepted, boolean gdprAccepted, boolean keepMeSignedIn) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.eulaAccepted = eulaAccepted;
        this.gdprAccepted = gdprAccepted;
        this.keepMeSignedIn = keepMeSignedIn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isEulaAccepted() {
        return eulaAccepted;
    }

    public void setEulaAccepted(boolean eulaAccepted) {
        this.eulaAccepted = eulaAccepted;
    }

    public boolean isGdprAccepted() {
        return gdprAccepted;
    }

    public void setGdprAccepted(boolean gdprAccepted) {
        this.gdprAccepted = gdprAccepted;
    }

    public boolean isKeepMeSignedIn() {
        return keepMeSignedIn;
    }

    public void setKeepMeSignedIn(boolean keepMeSignedIn) {
        this.keepMeSignedIn = keepMeSignedIn;
    }

    public List<HuntingTeam> getHuntingTeams() {
        return huntingTeams;
    }

    public void setHuntingTeams(List<HuntingTeam> huntingTeams) {
        this.huntingTeams = huntingTeams;
    }

    public HuntingTeam getCurrentHuntingTeam() {
        return currentHuntingTeam;
    }

    public void setCurrentHuntingTeam(HuntingTeam currentHuntingTeam) {
        this.currentHuntingTeam = currentHuntingTeam;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", huntingTeams=" + huntingTeams +
                ", currentHuntingTeam=" + currentHuntingTeam +
                ", eulaAccepted=" + eulaAccepted +
                ", gdprAccepted=" + gdprAccepted +
                ", keepMeSignedIn=" + keepMeSignedIn +
                '}';
    }
}

