package com.dell.noteapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.dell.noteapp.entity.Note;

import java.util.List;
@Dao
public interface NoteDao {

    @Query("Select * from Note")
    List<Note> getAllNote();

    @Query("Select * from Note where favorite = :isFv")
    List<Note> getNoteByFavrite(boolean isFv);

    @Insert
    void insert(Note note);

    @Delete
    void delete(Note note);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Note note);
}
