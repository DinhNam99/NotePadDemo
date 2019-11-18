package com.dell.noteapp.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dell.noteapp.R;
import com.dell.noteapp.entity.Note;
import com.dell.noteapp.utils.UtilsHelper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

public class NoteActivity extends AppCompatActivity {

    Toolbar toolbar;
    EditText edTitle, edNote;
    TextView tvDate,create_edit;
    Note noteM = new Note();
    String option;
    UtilsHelper utilsHelper;

    // Request code for voice input
    private static final int REQUEST_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        utilsHelper = new UtilsHelper(this);
        init();
        try {
            setUpLoading();
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
        create_edit = findViewById(R.id.create_edit);

    }

    public void setUpLoading() throws ParseException {
        Intent intent = getIntent();
        option = intent.getStringExtra("option");
        noteM = (Note) intent.getSerializableExtra("Note");
        if(noteM!=null) {
            edTitle.setText(noteM.getTitle());
            edNote.setText(noteM.getContent());
            tvDate.setText(utilsHelper.setUpDate(noteM.getDate()));
        }
        if(option.equals("Add")){
            create_edit.setText("Create note");
        }else if(option.equals("Update")){
            create_edit.setText("Edit note");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(option.equals("Add")){
            getMenuInflater().inflate(R.menu.menu_add,menu);
        }else if(option.equals("Update")) {
            getMenuInflater().inflate(R.menu.menu_note, menu);
            if(noteM.isFavorite()) {
                menu.getItem(3).getIcon().setTint(getResources().getColor(R.color.colorPrimary));
            }else{
                menu.getItem(3).getIcon().setTint(getResources().getColor(R.color.textFormatWhite));
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        final String title = edTitle.getText().toString();
        final String content = edNote.getText().toString();
        final Date date = new Date();
        switch (id){
            case R.id.menu_share:
                shareNote(noteM);
                break;
            case R.id.menu_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
                builder.setTitle("Are you sure you want to delete this note?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        utilsHelper.deleteNote(noteM,"n");
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
                    item.getIcon().setTint(getResources().getColor(R.color.textFormatWhite));
                    noteM.setFavorite(false);
                    utilsHelper.updateFV(noteM);
                }else{
                    item.getIcon().setTint(getResources().getColor(R.color.colorPrimary));
                    noteM.setFavorite(true);
                    utilsHelper.updateFV(noteM);
                }
                break;
            case R.id.menu_saveAdd:
                if (title.isEmpty()) {
                    edTitle.setError("Please enter title!");
                    edTitle.requestFocus();
                    break;
                }

                if (content.isEmpty()) {
                    edNote.setError("Please enter content");
                    edNote.requestFocus();
                    break;
                }
                utilsHelper.save(new Note(),title,content,date,option);
                break;
            case R.id.menu_saveUpdate:
                if (title.isEmpty()) {
                    edTitle.setError("Please enter title!");
                    edTitle.requestFocus();
                    break;
                }

                if (content.isEmpty()) {
                    edNote.setError("Please enter content");
                    edNote.requestFocus();
                    break;
                }
                utilsHelper.save(noteM,title,content,date,option);
                break;
            case R.id.voice:
                Log.e("CLICK","v");
                startVoiceRecognitionActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
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
