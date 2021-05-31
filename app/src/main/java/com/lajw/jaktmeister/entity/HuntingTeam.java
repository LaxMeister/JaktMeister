package com.lajw.jaktmeister.entity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HuntingTeam {

    private String id;
    private String connectCode;
    private String name;
    private ChatRoom chatRoom;
    private List<UserHuntingTeam> admins;
    private List<UserHuntingTeam> members;
    private List<HuntingArea> huntingAreas;

    public enum VALUE{
        DEFAULT
    }

    public HuntingTeam(){

    }

    //Used for creating team
    public HuntingTeam(String id, String connectCode, String name, List<UserHuntingTeam> admins) {
        this.id = id;
        this.connectCode = connectCode;
        this.name = name;
        this.admins = admins;
    }

    public HuntingTeam(String connectCode, String name) {
        this.connectCode = connectCode;
        this.name = name;
        this.chatRoom = new ChatRoom();
        this.admins = new ArrayList<UserHuntingTeam>();
        this.members = new ArrayList<UserHuntingTeam>();
        this.huntingAreas = new ArrayList<HuntingArea>();
    }

    public HuntingTeam(String connectCode, String name, ChatRoom chatRoom, List<UserHuntingTeam> admins, List<UserHuntingTeam> members, List<HuntingArea> huntingAreas) {
        this.connectCode = connectCode;
        this.name = name;
        this.chatRoom = chatRoom;
        this.admins = admins;
        this.members = members;
        this.huntingAreas = huntingAreas;
    }

    public HuntingTeam(String id, String connectCode, String name, ChatRoom chatRoom, List<UserHuntingTeam> admins, List<UserHuntingTeam> members, List<HuntingArea> huntingAreas) {
        this.id = id;
        this.connectCode = connectCode;
        this.name = name;
        this.chatRoom = chatRoom;
        this.admins = admins;
        this.members = members;
        this.huntingAreas = huntingAreas;
    }

    public HuntingTeam(VALUE enums){
        this.id = "0";
        this.connectCode = "Ej med i jaktlag";
        this.name = "Ej med i jaktlag";
        this.chatRoom = new ChatRoom();
        this.admins = new ArrayList<>();
        this.members = new ArrayList<>();
        this.huntingAreas = new ArrayList<>();
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public List<UserHuntingTeam> getAdmins() {
        return admins;
    }

    public void setAdmins(List<UserHuntingTeam> admins) {
        this.admins = admins;
    }

    public List<UserHuntingTeam> getMembers() {
        return members;
    }

    public void setMembers(List<UserHuntingTeam> members) {
        this.members = members;
    }

    public List<HuntingArea> getHuntingAreas() {
        return huntingAreas;
    }

    public void setHuntingAreas(List<HuntingArea> huntingAreas) {
        this.huntingAreas = huntingAreas;
    }

    public String getConnectCode() {
        return connectCode;
    }

    public void setConnectCode(String connectCode) {
        this.connectCode = connectCode;
    }

    @NotNull
    @Override
    public String toString() {
        return "HuntingTeam{" +
                "id='" + id + '\'' +
                ", connectCode='" + connectCode + '\'' +
                ", name='" + name + '\'' +
                ", chatRoom=" + chatRoom +
                ", admins=" + admins +
                ", members=" + members +
                ", huntingAreas=" + huntingAreas +
                '}';
    }
}
