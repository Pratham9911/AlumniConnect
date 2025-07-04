package com.pratham.alumniconnect;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.EntryViewHolder> {

    private final List<EntryModel> entryList;
    private final Context context;

    // ✨ Added long press listener support
    private final OnEntryActionListener actionListener;

    // ✨ Updated constructor to accept long press listener
    public EntryAdapter(List<EntryModel> entryList, Context context, OnEntryActionListener actionListener) {
        this.entryList = entryList;
        this.context = context;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.entry_item, parent, false);
        return new EntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        EntryModel entry = entryList.get(position);
        holder.entryTitle.setText(entry.getTitle());

        if (!TextUtils.isEmpty(entry.getSubtitle())) {
            holder.entrySubtitle.setText(entry.getSubtitle());
            holder.entrySubtitle.setVisibility(View.VISIBLE);
        } else {
            holder.entrySubtitle.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(entry.getImageUrl())) {
            holder.entryImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(entry.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.default_bg2)
                    .into(holder.entryImage);
        } else {
            holder.entryImage.setVisibility(View.GONE);
        }

        // ✨ Handle long press
        holder.itemView.setOnLongClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEntryLongPressed(entry);
            }
            return true;
        });

        holder.entryTitle.setOnClickListener(v -> {
            String url = entry.getLink();
            if (!TextUtils.isEmpty(url)) {
                if (Patterns.WEB_URL.matcher(url).matches()) {
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "https://" + url;
                    }
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        context.startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Invalid link", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return entryList.size();
    }

    static class EntryViewHolder extends RecyclerView.ViewHolder {
        TextView entryTitle, entrySubtitle;
        ImageView entryImage;

        public EntryViewHolder(@NonNull View itemView) {
            super(itemView);
            entryTitle = itemView.findViewById(R.id.entryTitle);
            entrySubtitle = itemView.findViewById(R.id.entrySubtitle);
            entryImage = itemView.findViewById(R.id.entryImage);
        }
    }

    // ✨ Interface for long-press actions
    public interface OnEntryActionListener {
        void onEntryLongPressed(EntryModel entry);
    }
}
