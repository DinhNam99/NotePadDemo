package com.dell.noteapp.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.dell.noteapp.dao.NoteDao;
import com.dell.noteapp.entity.Note;

@Database(entities = {Note.class},version = 1)
public abstract class NoteDatabase extends RoomDatabase {

    public abstract NoteDao getNoteDao();

}
