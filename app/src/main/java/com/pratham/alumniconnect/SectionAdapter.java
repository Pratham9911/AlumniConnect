package com.pratham.alumniconnect;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.SectionViewHolder> {

    private final List<SectionModel> sectionList;
    private final Context context;
    private final OnSectionActionListener listener;
    private boolean isOwnProfile;

    public interface OnSectionActionListener {
        void onAddEntryClicked(SectionModel section);
        void onEditSection(SectionModel section);
        void onDeleteSection(SectionModel section);

        // ✨ New method for entry long press
        void onEntryLongPressed(EntryModel entry, SectionModel section);
    }

    public SectionAdapter(List<SectionModel> sectionList, Context context, boolean isOwnProfile, OnSectionActionListener listener) {
        this.sectionList = sectionList;
        this.context = context;
        this.listener = listener;
        this.isOwnProfile = isOwnProfile;
    }

    @NonNull
    @Override
    public SectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.section_item, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SectionViewHolder holder, int position) {
        SectionModel section = sectionList.get(position);

        holder.sectionTitle.setText(section.getTitle());
        if (!isOwnProfile) {
            holder.addEntryBtn.setVisibility(View.GONE);
            holder.editBtn.setVisibility(View.GONE);
            holder.deleteBtn.setVisibility(View.GONE);
        }

        holder.addEntryBtn.setOnClickListener(v -> listener.onAddEntryClicked(section));
        holder.editBtn.setOnClickListener(v -> listener.onEditSection(section));
        holder.deleteBtn.setOnClickListener(v -> listener.onDeleteSection(section));

        // ✨ Pass long press callback to EntryAdapter
        EntryAdapter entryAdapter = new EntryAdapter(section.getEntries(), context, entry -> {
            listener.onEntryLongPressed(entry, section);
        });

        holder.entryRecycler.setLayoutManager(new LinearLayoutManager(context));
        holder.entryRecycler.setAdapter(entryAdapter);
    }

    @Override
    public int getItemCount() {
        return sectionList.size();
    }

    static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView sectionTitle;
        ImageView addEntryBtn, editBtn, deleteBtn;
        RecyclerView entryRecycler;

        public SectionViewHolder(@NonNull View itemView) {
            super(itemView);
            sectionTitle = itemView.findViewById(R.id.sectionTitle);
            addEntryBtn = itemView.findViewById(R.id.addEntryBtn);
            editBtn = itemView.findViewById(R.id.editSectionBtn);
            deleteBtn = itemView.findViewById(R.id.deleteSectionBtn);
            entryRecycler = itemView.findViewById(R.id.entryRecyclerView);
        }
    }
}
