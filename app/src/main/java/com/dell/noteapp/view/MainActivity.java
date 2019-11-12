package com.dell.noteapp.view;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dell.noteapp.R;
import com.dell.noteapp.adpater.NoteAdapter;
import com.dell.noteapp.database.DBNoteHelper;
import com.dell.noteapp.entity.Note;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Toolbar toolbar;
    ImageView back;
    TextView tvAllNotes, tvSearch, all, fv, cancle;
    EditText edSearch;
    RelativeLayout layout1, layout2;
    LinearLayout layoutoption;
    RecyclerView rcNote;
    FloatingActionButton fbAdd;
    NoteAdapter noteAdapter;
    InputMethodManager imm;
    List<Note> noteListM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        setUpListener();
        getAllNote();
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        removeNote();
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
        layoutoption = findViewById(R.id.layoutOption);
        fv = findViewById(R.id.fv);
        all = findViewById(R.id.all);
        cancle = findViewById(R.id.tvCancle);

        rcNote.setHasFixedSize(true);
        rcNote.setLayoutManager(new LinearLayoutManager(this));
    }

    public void setUpListener(){
        back.setOnClickListener(this);
        tvSearch.setOnClickListener(this);
        fbAdd.setOnClickListener(this);
        tvAllNotes.setOnClickListener(this);
        cancle.setOnClickListener(this);
        all.setOnClickListener(this);
        fv.setOnClickListener(this);
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
                    for (int i = 0; i < noteListM.size(); i++) {
                        final String text = noteListM.get(i).getTitle().toLowerCase();
                        if (text.contains(s)) {
                            filteredList.add(noteListM.get(i));
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
                        noteAdapter = new NoteAdapter(getApplicationContext(), (ArrayList<Note>) filteredList);
                        rcNote.setAdapter(noteAdapter);
                        noteAdapter.notifyDataSetChanged();  // data set changed
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
                noteAdapter = new NoteAdapter(getApplicationContext(),(ArrayList<Note>) noteList);
                rcNote.setAdapter(noteAdapter);
                noteListM = noteList;
            }
        }
        GetAllNote getAllNote = new GetAllNote();
        getAllNote.execute();
    }

    private void getNoteByFv(){
        class GetNoteByFv extends AsyncTask<Void, Void, List<Note>>{

            @Override
            protected List<Note> doInBackground(Void... voids) {
                List<Note>noteList = DBNoteHelper.getInstance(getApplicationContext()).getNoteDatabase().getNoteDao().getNoteByFavrite(true);
                return noteList;
            }

            @Override
            protected void onPostExecute(List<Note> noteList) {
                super.onPostExecute(noteList);
                noteAdapter = new NoteAdapter(getApplicationContext(),(ArrayList<Note>) noteList);
                rcNote.setAdapter(noteAdapter);
                noteListM = noteList;
            }
        }
        GetNoteByFv getNoteByFv = new GetNoteByFv();
        getNoteByFv.execute();
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
            case R.id.allnote:
                layoutoption.setVisibility(View.VISIBLE);
                break;
            case R.id.tvCancle:
                layoutoption.setVisibility(View.GONE);
                break;
            case R.id.all:
                tvAllNotes.setText("All Notes");
                setUpTextOption(all,cancle,fv);
                getAllNote();
                break;
            case R.id.fv:
                tvAllNotes.setText("Favorite");
                setUpTextOption(fv,all,cancle);
                getNoteByFv();
                break;
            case R.id.fbAdd:
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                intent.putExtra("option","Add");
                startActivity(intent);
                break;
        }
    }

    public void setUpLayoutToolbar(RelativeLayout layout1, RelativeLayout layout2){
        layout1.setVisibility(View.VISIBLE);
        layout2.setVisibility(View.GONE);
    }

    public void setUpTextOption(TextView tv1, TextView tv2, TextView tv3){
        layoutoption.setVisibility(View.GONE);
        tv1.setTextColor(getResources().getColor(R.color.colorPrimary));
        tv2.setTextColor(getResources().getColor(android.R.color.black));
        tv3.setTextColor(getResources().getColor(android.R.color.black));
    }

    private void removeNote(){

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                Drawable deleteDrawable = ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_menu_delete);
                int position = viewHolder.getAdapterPosition();
                Note note = noteListM.get(position);
                noteAdapter.removeItem(position);
                deleteNote(note);
            }


        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rcNote);
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
            }
        }

        DeleteNote dt = new DeleteNote();
        dt.execute();
    }
    @Override
    protected void onResume() {
        super.onResume();
        getAllNote();
    }
}