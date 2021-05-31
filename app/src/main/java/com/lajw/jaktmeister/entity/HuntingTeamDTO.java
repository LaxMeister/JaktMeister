package com.lajw.jaktmeister.entity;

public class HuntingTeamDTO {

    private String id;
    private String name;
    private String connectCode;

    public HuntingTeamDTO() {
    }

    public HuntingTeamDTO(String id, String name, String connectCode) {
        this.id = id;
        this.name = name;
        this.connectCode = connectCode;
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

    public String getConnectCode() {
        return connectCode;
    }

    public void setConnectCode(String connectCode) {
        this.connectCode = connectCode;
    }
}
