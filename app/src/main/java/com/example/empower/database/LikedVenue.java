package com.example.empower.database;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class LikedVenue {



    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "venueID")
    public String venueID;

    @ColumnInfo(name = "venue_name")
    public String name;

    @ColumnInfo(name = "venue_postcode")
    public String postcode;

    public LikedVenue(String venueID, String name, String postcode) {
        this.venueID = venueID;
        this.name = name;
        this.postcode = postcode;
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

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    @Override
    public String toString() {
        return "venueID=" + venueID + ';' + "name=" + name + ';' + "postcode=" + postcode;
    }
}
