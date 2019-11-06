package com.dell.noteapp.database;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

public class DBNoteHelper {

    private Context context;
    private NoteDatabase noteDatabase;
    private static DBNoteHelper dbNoteHelper;

    public DBNoteHelper(Context context){
        this.context = context;
        noteDatabase = Room.databaseBuilder(context,NoteDatabase.class, "NOTE_DB").build();
    }

    public static synchronized DBNoteHelper getInstance(Context context){
        if(dbNoteHelper == null){
            dbNoteHelper = new DBNoteHelper(context);
        }
        return dbNoteHelper;
    }

    public NoteDatabase getNoteDatabase() {
        return noteDatabase;
    }
}
