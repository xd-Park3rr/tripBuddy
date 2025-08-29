package com.example.tripbuddy.ui.gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripbuddy.R;
import com.example.tripbuddy.data.models.Memory;

import java.util.ArrayList;
import java.util.List;

public class MemoryAdapter extends RecyclerView.Adapter<MemoryAdapter.VH> {
    public interface OnItemClickListener { void onClick(Memory m, View sharedView); }
    private final LayoutInflater inflater;
    private final OnItemClickListener listener;
    private final Context context;
    private final List<Memory> items = new ArrayList<>();

    public MemoryAdapter(Context ctx, OnItemClickListener l) {
        this.context = ctx;
        this.listener = l;
        this.inflater = LayoutInflater.from(ctx);
    }

    public void submitList(List<Memory> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.item_memory, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        Memory m = items.get(position);
        h.title.setText(m.title != null ? m.title : "");
        String transitionName = "memory_" + m.id + "_image";
        ViewCompat.setTransitionName(h.image, transitionName);
        // Load image from res name or uri. For simplicity, use resource if prefixed with res:
        if (m.imageUri != null && m.imageUri.startsWith("res:")) {
            String resName = m.imageUri.substring(4);
            int resId = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
            if (resId == 0) resId = R.mipmap.ic_launcher;
            h.image.setImageResource(resId);
        } else if (m.imageUri != null) {
            h.image.setImageURI(android.net.Uri.parse(m.imageUri));
        } else {
            h.image.setImageResource(R.mipmap.ic_launcher);
        }
        h.itemView.setOnClickListener(v -> listener.onClick(m, h.image));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView image; TextView title;
        VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.iv_photo);
            title = itemView.findViewById(R.id.tv_title);
        }
    }
}
