package com.dell.noteapp.adpater;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.dell.noteapp.R;
import com.dell.noteapp.entity.Note;
import com.dell.noteapp.utils.UtilsHelper;
import com.dell.noteapp.view.NoteActivity;

import java.text.ParseException;
import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteHolder> {

    ArrayList<Note> noteList;
    Context context;
    LayoutInflater inflater;
    UtilsHelper utilsHelper;

    public NoteAdapter(Context context, ArrayList<Note> noteList){
        this.context = context;
        this.noteList = noteList;
        inflater = LayoutInflater.from(context);
        utilsHelper = new UtilsHelper(context);
    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_item, parent, false);
        return new NoteHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NoteHolder holder, final int position) {
        final Note note = noteList.get(position);
        holder.tvTitle.setText(note.getTitle()+"");

        if(note.getContent().length()>50){
            String content = note.getContent().substring(0,40)+"...";
            holder.tvContent.setText(content);
        }else {
            holder.tvContent.setText(note.getContent() + "");
        }

        try {
            holder.tvDate.setText(utilsHelper.setUpDate(note.getDate()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

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

        holder.imageDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());
                builder.setTitle("Are you sure you want to delete this note?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        utilsHelper.deleteNote(note,"a");
                        removeItem(position);
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
            }
        });

        //favorite
        if(note.isFavorite()== true){
            DrawableCompat.setTint(holder.imageFv.getDrawable(), ContextCompat.getColor(context, R.color.colorPrimary));
        }else{
            DrawableCompat.setTint(holder.imageFv.getDrawable(), ContextCompat.getColor(context, R.color.textFormatWhite));
        }
        holder.imageFv.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                if(note.isFavorite()){
                    holder.imageFv.setColorFilter(context.getResources().getColor(R.color.textFormatWhite));
                    note.setFavorite(false);
                    utilsHelper.updateFV(note);
                }else{
                    holder.imageFv.setColorFilter(context.getResources().getColor(R.color.colorPrimary));
                    note.setFavorite(true);
                    utilsHelper.updateFV(note);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public void removeItem(int position) {
        noteList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, noteList.size());
    }

    public class NoteHolder extends RecyclerView.ViewHolder{

        TextView tvTitle, tvContent,tvDate;
        RelativeLayout layoutItem;
        ImageView imageFv, imageDel;
        CardView cardView;
        public NoteHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            layoutItem = itemView.findViewById(R.id.layoutitem);
            imageFv = itemView.findViewById(R.id.imageFv);
            cardView = itemView.findViewById(R.id.cardviewitem);
            tvDate = itemView.findViewById(R.id.tvDate);
            imageDel = itemView.findViewById(R.id.imageDel);
        }
    }
}
