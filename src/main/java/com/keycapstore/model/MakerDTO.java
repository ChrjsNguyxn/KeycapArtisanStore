package com.keycapstore.model;

public class MakerDTO {

    private int makerId;
    private String name;
    private String origin;
    private String website;

    public MakerDTO() {}

    public MakerDTO(int makerId, String name, String origin, String website) {
        this.makerId = makerId;
        this.name = name;
        this.origin = origin;
        this.website = website;
    }

    public int getMakerId() { return makerId; }
    public void setMakerId(int makerId) { this.makerId = makerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    @Override
    public String toString() {
        return name;
    }
}