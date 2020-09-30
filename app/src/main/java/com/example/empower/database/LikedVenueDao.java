package com.example.empower.database;

import androidx.lifecycle.LiveData;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

public interface LikedVenueDao {

    @Query("SELECT * FROM LikedVenue")
    LiveData<List<LikedVenue>> getAll();


    @Query("SELECT * FROM LikedVenue WHERE venueIndexID = :venueIndex LiMIT 1")
    LikedVenue findByID(int venueIndex);


    @Insert
    void insertALL(LikedVenue... likedVeneuses);

    @Insert
    long insert(LikedVenue likedVenue);

    @Delete
    void delete(LikedVenue likedVenue);

    @Update
    void updateLikedVenues(LikedVenue... likedVeneuses);

    @Query("DELETE FROM LikedVenue")
    void deleteAll();

}