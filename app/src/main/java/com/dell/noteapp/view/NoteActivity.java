package com.dell.noteapp.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dell.noteapp.R;
import com.dell.noteapp.database.DBNoteHelper;
import com.dell.noteapp.entity.Note;

import java.util.ArrayList;
import java.util.Date;

public class NoteActivity extends AppCompatActivity {

    Toolbar toolbar;
    EditText edTitle, edNote;
    TextView tvDate;
    Note noteM = new Note();
    String option;
    byte[] b;

    // Request code for voice input
    private static final int REQUEST_CODE = 1234;

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
            case R.id.voice:
                Log.e("CLICK","v");
                startVoiceRecognitionActivity();
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
                //startActivity(new Intent(NoteActivity.this, MainActivity.class));
                finish();
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
                //startActivity(new Intent(NoteActivity.this, MainActivity.class));
                finish();
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



    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, R.string.voice_hint);
        startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * Handle the results from the voice recognition
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);

            if (matches.size() > 0) {
                if (edNote.getText().toString().length() == 0) {
                    edNote.setText(matches.get(0));
                    edNote.setSelection(edNote.getText().toString().length());
                } else {
                    Spanned spanText = (SpannedString) TextUtils.concat(edNote.getText(), " " + matches.get(0));
                    edNote.setText(spanText);
                    edNote.setSelection(edNote.getText().toString().length());
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void hideSoftKeyboard() {
        if (this.getCurrentFocus() != null) {
            try {
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getApplicationWindowToken(), 0);
            } catch (RuntimeException e) {
                //ignore
            }
        }
    }
}
