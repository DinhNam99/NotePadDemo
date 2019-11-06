package com.dell.noteapp.adpater;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dell.noteapp.NoteActivity;
import com.dell.noteapp.R;
import com.dell.noteapp.entity.Note;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteHolder> {

    ArrayList<Note> noteList;
    Context context;
    LayoutInflater inflater;

    public NoteAdapter(Context context, ArrayList<Note> noteList){
        this.context = context;
        this.noteList = noteList;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_item, parent, false);
        return new NoteHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        final Note note = noteList.get(position);
        holder.tvTitle.setText(note.getTitle()+"");
        holder.tvContent.setText(note.getContent()+"");

        holder.layoutItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, NoteActivity.class);
                intent.putExtra("Note", note);
                intent.putExtra("option", "Update");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        //favorite
        if(note.isFavorite()== true){
            holder.imageFv.setVisibility(View.VISIBLE);
        }else{
            holder.imageFv.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public class NoteHolder extends RecyclerView.ViewHolder{

        TextView tvTitle, tvContent;
        RelativeLayout layoutItem;
        ImageView imageFv;
        public NoteHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            layoutItem = itemView.findViewById(R.id.layoutitem);
            imageFv = itemView.findViewById(R.id.imageFv);
        }
    }
}
