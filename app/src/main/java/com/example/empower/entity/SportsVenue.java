package com.example.empower.entity;

public class SportsVenue {

    private String venueID;
    private String name;
    private String address;
    private String suburb;
    private String postcode;
    private String state;
    private String businessCategory;
    private String lga;
    private String region;


    public SportsVenue (){

    }


    public SportsVenue(String venueID, String name, String address, String suburb, String postcode, String state, String businessCategory, String lga, String region) {
        this.venueID = venueID;
        this.name = name;
        this.address = address;
        this.suburb = suburb;
        this.postcode = postcode;
        this.state = state;
        this.businessCategory = businessCategory;
        this.lga = lga;
        this.region = region;
    }

    public String getVenueID() {
        return venueID;
    }

    public void setVenueID(String venueID) {
        this.venueID = venueID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSuburb() {
        return suburb;
    }

    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getBusinessCategory() {
        return businessCategory;
    }

    public void setBusinessCategory(String businessCategory) {
        this.businessCategory = businessCategory;
    }

    public String getLga() {
        return lga;
    }

    public void setLga(String lga) {
        this.lga = lga;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public String toString() {
        return "SportsVenue{" +
                "venueID='" + venueID + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", suburb='" + suburb + '\'' +
                ", postcode='" + postcode + '\'' +
                ", state='" + state + '\'' +
                ", businessCategory='" + businessCategory + '\'' +
                ", lga='" + lga + '\'' +
                ", region='" + region + '\'' +
                '}';
    }
}
