package com.lajw.jaktmeister;

public class Hunter {

    public String hunterName;
    public int standNumber;

    public Hunter() {
    }

    public Hunter(String hunterName, int standNumber) {
        this.hunterName = hunterName;
        this.standNumber = standNumber;
    }

    public String getHunterName() {
        return hunterName;
    }

    public void setHunterName(String hunterName) {
        this.hunterName = hunterName;
    }

    public int getStandNumber() {
        return standNumber;
    }

    public void setStandNumber(int standNumber) {
        this.standNumber = standNumber;
    }

    @Override
    public String toString() {
        return "Hunter{" +
                "hunterName='" + hunterName + '\'' +
                ", standNumber=" + standNumber +
                '}';
    }
}
