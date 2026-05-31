package com.smartone.app.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.smartone.app.R;
import com.smartone.app.data.local.HistoryEntry;

public class HistoryAdapter
        extends ListAdapter<HistoryEntry, HistoryAdapter.HistoryViewHolder> {

    public interface OnClickListener     { void onClick(HistoryEntry e); }
    public interface OnLongClickListener { void onLongClick(HistoryEntry e); }
    public interface OnFavoriteListener  { void onFavorite(HistoryEntry e); }

    private final OnClickListener     onClick;
    private final OnLongClickListener onLongClick;
    private final OnFavoriteListener  onFavorite;

    private static final DiffUtil.ItemCallback<HistoryEntry> DIFF =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull HistoryEntry a, @NonNull HistoryEntry b) {
                    return a.id == b.id;
                }
                @Override
                public boolean areContentsTheSame(
                        @NonNull HistoryEntry a, @NonNull HistoryEntry b) {
                    return a.title.equals(b.title)
                            && a.preview.equals(b.preview)
                            && a.isFavorite == b.isFavorite;
                }
            };

    public HistoryAdapter(
            OnClickListener onClick,
            OnLongClickListener onLongClick,
            OnFavoriteListener onFavorite) {
        super(DIFF);
        this.onClick     = onClick;
        this.onLongClick = onLongClick;
        this.onFavorite  = onFavorite;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.bind(getItem(position), onClick, onLongClick, onFavorite);
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {

        private final TextView    tvType;
        private final TextView    tvTitle;
        private final TextView    tvPreview;
        private final TextView    tvTime;
        private final ImageButton btnFavorite;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType      = itemView.findViewById(R.id.tvType);
            tvTitle     = itemView.findViewById(R.id.tvTitle);
            tvPreview   = itemView.findViewById(R.id.tvPreview);
            tvTime      = itemView.findViewById(R.id.tvTime);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }

        void bind(HistoryEntry entry,
                  OnClickListener onClick,
                  OnLongClickListener onLongClick,
                  OnFavoriteListener onFavorite) {

            tvType.setText(entry.type.toUpperCase());
            tvTitle.setText(entry.title);
            tvPreview.setText(entry.preview);
            tvTime.setText(entry.getRelativeTime());

            if (entry.isJson()) {
                tvType.setBackgroundResource(R.drawable.bg_badge_json);
                tvType.setTextColor(itemView.getContext().getColor(R.color.accent));
            } else {
                tvType.setBackgroundResource(R.drawable.bg_badge_chat);
                tvType.setTextColor(itemView.getContext().getColor(R.color.color_chat));
            }

            btnFavorite.setImageResource(entry.isFavorite
                    ? R.drawable.ic_star_filled
                    : R.drawable.ic_star_outline);

            itemView.setOnClickListener(v -> onClick.onClick(entry));
            itemView.setOnLongClickListener(v -> {
                onLongClick.onLongClick(entry);
                return true;
            });
            btnFavorite.setOnClickListener(v -> onFavorite.onFavorite(entry));
        }
    }
}
