package com.dell.noteapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dell.noteapp.database.DBNoteHelper;
import com.dell.noteapp.entity.Note;

import java.util.Date;

public class NoteActivity extends AppCompatActivity {

    Toolbar toolbar;
    EditText edTitle, edNote;
    TextView tvDate;
    Note noteM = new Note();
    String option;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        init();
        setUpLoading();
    }

    private void init() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        edTitle = findViewById(R.id.edTitle);
        edNote = findViewById(R.id.edNote);
        tvDate = findViewById(R.id.tvDate);

    }

    public void setUpLoading(){
        Intent intent = getIntent();
        option = intent.getStringExtra("option");
        noteM = (Note) intent.getSerializableExtra("Note");
        if(noteM!=null) {
            edTitle.setText(noteM.getTitle());
            edNote.setText(noteM.getContent());
            tvDate.setText(noteM.getDate());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(option.equals("Add")){
            getMenuInflater().inflate(R.menu.menu_add,menu);
        }else if(option.equals("Update")) {
            getMenuInflater().inflate(R.menu.menu_note, menu);
            if(noteM.isFavorite()) {
                menu.getItem(3).setIcon(R.drawable.ic_favorite);
            }else{
                menu.getItem(3).setIcon(R.drawable.ic_favorite_border);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.menu_share:
                shareNote(noteM);
                break;
            case R.id.menu_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
                builder.setTitle("Are you sure?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteNote(noteM);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                AlertDialog ad = builder.create();
                ad.show();

                break;
            case R.id.menu_fav:
                if(noteM.isFavorite()){
                    item.setIcon(R.drawable.ic_favorite_border);
                    noteM.setFavorite(false);
                }else{
                    item.setIcon(R.drawable.ic_favorite);
                    noteM.setFavorite(true);
                }
                break;
            case R.id.menu_saveAdd:
                save(new Note());
                break;
            case R.id.menu_saveUpdate:
                save(noteM);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void save(final Note note) {
        final String title = edTitle.getText().toString();
        final String content = edNote.getText().toString();
        final Date date = new Date();

        if (title.isEmpty()) {
            edTitle.setError("Please enter title!");
            edTitle.requestFocus();
            return;
        }

        if (content.isEmpty()) {
            edNote.setError("Please enter content");
            edNote.requestFocus();
            return;
        }

        class SaveNote extends AsyncTask<Void, Void, Void>{

            @Override
            protected Void doInBackground(Void... voids) {
                note.setTitle(title);
                note.setContent(content);
                note.setDate(date.toString());
                if(option.equals("Update")){
                    Log.e("NOTE",title+"-"+content+"-"+date.toString());
                    DBNoteHelper.getInstance(getApplicationContext()).getNoteDatabase().getNoteDao().update(note);
                }else if(option.equals("Add")) {
                    DBNoteHelper.getInstance(getApplicationContext()).getNoteDatabase().getNoteDao().insert(note);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                finish();
                startActivity(new Intent(NoteActivity.this, MainActivity.class));
            }
        }
        SaveNote saveNote = new SaveNote();
        saveNote.execute();
    }
    private void deleteNote(final Note note){
        class DeleteNote extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                DBNoteHelper.getInstance(getApplicationContext()).getNoteDatabase()
                        .getNoteDao()
                        .delete(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_LONG).show();
                finish();
                startActivity(new Intent(NoteActivity.this, MainActivity.class));
            }
        }

        DeleteNote dt = new DeleteNote();
        dt.execute();
    }
    private void shareNote(Note note) {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        share.putExtra(Intent.EXTRA_SUBJECT, note.getTitle());
        share.putExtra(Intent.EXTRA_TEXT, note.getContent());

        startActivity(Intent.createChooser(share, "Share note!"));
    }
}
