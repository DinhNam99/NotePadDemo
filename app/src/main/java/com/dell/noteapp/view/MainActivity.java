package com.dell.noteapp.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dell.noteapp.R;
import com.dell.noteapp.adpater.NoteAdapter;
import com.dell.noteapp.database.DBNoteHelper;
import com.dell.noteapp.entity.Note;
import com.dell.noteapp.utils.UtilsHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    Toolbar toolbar;
    ImageView back,filter;
    TextView tvAllNotes, tvSearch, tvNotNote, tvNot;
    EditText edSearch;
    RelativeLayout layout1, layout2;
    LinearLayout layoutAllNote;
    RecyclerView rcNote;
    FloatingActionButton fbAdd;
    NoteAdapter noteAdapter;
    InputMethodManager imm;
    List<Note> noteListM;
    ArrayList<Note> notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        setUpListener();
        getAllNote();

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private void init() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");

        back = findViewById(R.id.back);
        tvAllNotes = findViewById(R.id.allnote);
        tvSearch = findViewById(R.id.tvSearch);
        edSearch = findViewById(R.id.edSearch);
        layout1 = findViewById(R.id.layout1);
        layout2 = findViewById(R.id.layout2);
        fbAdd = findViewById(R.id.fbAdd);
        rcNote = findViewById(R.id.rcNote);
        layoutAllNote = findViewById(R.id.layout_all_note);
        tvNotNote = findViewById(R.id.tvNotNote);
        tvNot = findViewById(R.id.tvNot);
        filter = findViewById(R.id.filter);
        rcNote.setHasFixedSize(true);
        rcNote.setLayoutManager(new LinearLayoutManager(this));

    }

    public void setUpListener(){
        back.setOnClickListener(this);
        tvSearch.setOnClickListener(this);
        fbAdd.setOnClickListener(this);
        layoutAllNote.setOnClickListener(this);
        filter.setOnClickListener(this);
        searchNote();
    }
    public void searchNote(){
        edSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                s = s.toString().toLowerCase();
                final List<Note> filteredList = new ArrayList<>();

                if(s!=null) {
                    for (int i = 0; i < notes.size(); i++) {
                        final String text = notes.get(i).getTitle().toLowerCase();
                        if (text.contains(s)) {
                            filteredList.add(notes.get(i));
                        }
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"Please enter query!!!",Toast.LENGTH_SHORT);
                }
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rcNote.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        noteAdapter = new NoteAdapter(MainActivity.this, (ArrayList<Note>) filteredList);
                        rcNote.setAdapter(noteAdapter);
                        noteAdapter.notifyDataSetChanged();  // data set changed
                        if(filteredList.size() == 0){
                            tvNotNote.setVisibility(View.VISIBLE);
                        }else if(filteredList.size() != 0){
                            tvNotNote.setVisibility(View.GONE);
                        }
                    }
                },2000);

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    private void getAllNote() {
        class GetAllNote extends AsyncTask<Void, Void, List<Note>>{

            @Override
            protected List<Note> doInBackground(Void... voids) {
                List<Note>noteList = DBNoteHelper.getInstance(getApplicationContext()).getNoteDatabase().getNoteDao().getAllNote();
                return noteList;
            }

            @Override
            protected void onPostExecute(List<Note> noteList) {
                super.onPostExecute(noteList);
                noteAdapter = new NoteAdapter(MainActivity.this,(ArrayList<Note>) noteList);
                rcNote.setAdapter(noteAdapter);
                noteListM = noteList;
                notes = (ArrayList<Note>) noteListM;
                setUpNotNote((ArrayList<Note>) noteList);
            }
        }
        GetAllNote getAllNote = new GetAllNote();
        getAllNote.execute();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.tvSearch:
                setUpLayoutToolbar(layout2,layout1);
                edSearch.requestFocus();
                imm.showSoftInput(edSearch, InputMethodManager.SHOW_IMPLICIT);
                fbAdd.hide();
                break;
            case R.id.back:
                setUpLayoutToolbar(layout1,layout2);
                imm.hideSoftInputFromWindow(edSearch.getWindowToken(), 0);
                fbAdd.show();
                break;
            case R.id.layout_all_note:
                PopupMenu menu = new PopupMenu(this,view);
                menu.getMenuInflater().inflate(R.menu.menu_option,menu.getMenu());
                menu.setOnMenuItemClickListener(this);
                menu.show();
                break;
            case R.id.fbAdd:
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                intent.putExtra("option","Add");
                startActivity(intent);
                break;
            case R.id.filter:
                PopupMenu menuTime = new PopupMenu(this,view);
                menuTime.getMenuInflater().inflate(R.menu.menu_time,menuTime.getMenu());
                menuTime.setOnMenuItemClickListener(this);
                menuTime.show();
                break;
        }
    }

    public void setUpLayoutToolbar(RelativeLayout layout1, RelativeLayout layout2){
        layout1.setVisibility(View.VISIBLE);
        layout2.setVisibility(View.GONE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        getAllNote();
    }
    public void setUpNotNote(ArrayList<Note> notes){
        if(notes.size()==0){
            tvNot.setVisibility(View.VISIBLE);
        }else{
            tvNot.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id){
            case R.id.menu_all:
                tvAllNotes.setText("All Notes");
                noteAdapter = new NoteAdapter(MainActivity.this,notes);
                rcNote.setAdapter(noteAdapter);
                break;
            case R.id.menu_favorite:
                tvAllNotes.setText("Favorite");
                ArrayList<Note> noteFV = new ArrayList<>();
                for (int i = 0; i < notes.size(); i++){
                    if(notes.get(i).isFavorite() == true){
                        noteFV.add(notes.get(i));
                    }
                }
                setUpNotNote(noteFV);
                noteAdapter = new NoteAdapter(MainActivity.this,noteFV);
                rcNote.setAdapter(noteAdapter);
                break;
            case R.id.today:
                ArrayList<Note> noteToday = new ArrayList<>();
                for(int i = 0; i<noteListM.size(); i++){
                    try {
                        if(UtilsHelper.TimeToNow(noteListM.get(i).getDate())==0){
                            noteToday.add(noteListM.get(i));
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                notes = noteToday;
                noteAdapter = new NoteAdapter(MainActivity.this,noteToday);
                rcNote.setAdapter(noteAdapter);
                setUpNotNote(noteToday);
                break;
            case R.id.yesterday:
                ArrayList<Note> noteYesterday = new ArrayList<>();
                for(int i = 0; i<noteListM.size(); i++){
                    try {
                        if(UtilsHelper.TimeToNow(noteListM.get(i).getDate())==1){
                            noteYesterday.add(noteListM.get(i));
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                notes = noteYesterday;
                noteAdapter = new NoteAdapter(MainActivity.this,noteYesterday);
                rcNote.setAdapter(noteAdapter);
                setUpNotNote(noteYesterday);
                break;
            case R.id.oneweek:
                ArrayList<Note> noteOneWeek = new ArrayList<>();
                for(int i = 0; i<noteListM.size(); i++){
                    try {
                        if(UtilsHelper.TimeToNow(noteListM.get(i).getDate())>1){
                            noteOneWeek.add(noteListM.get(i));
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                notes = noteOneWeek;
                noteAdapter = new NoteAdapter(MainActivity.this,noteOneWeek);
                rcNote.setAdapter(noteAdapter);
                setUpNotNote(noteOneWeek);
                break;
            case R.id.all_time:
                getAllNote();
                break;
        }
        return false;
    }
}
