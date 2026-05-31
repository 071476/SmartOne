package com.smartone.app.ui.viewer;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.smartone.app.R;
import com.smartone.app.parser.JsonParser;

public class JsonLineAdapter
        extends ListAdapter<JsonParser.JsonLine, JsonLineAdapter.LineViewHolder> {

    private static final DiffUtil.ItemCallback<JsonParser.JsonLine> DIFF =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull JsonParser.JsonLine a, @NonNull JsonParser.JsonLine b) {
                    return a.lineNumber == b.lineNumber;
                }
                @Override
                public boolean areContentsTheSame(
                        @NonNull JsonParser.JsonLine a, @NonNull JsonParser.JsonLine b) {
                    return a.content.equals(b.content) && a.hasError == b.hasError;
                }
            };

    public JsonLineAdapter() {
        super(DIFF);
    }

    @NonNull
    @Override
    public LineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_json_line, parent, false);
        return new LineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LineViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class LineViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvLineNumber;
        private final TextView tvContent;

        LineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLineNumber = itemView.findViewById(R.id.tvLineNumber);
            tvContent    = itemView.findViewById(R.id.tvLineContent);
        }

        void bind(JsonParser.JsonLine line) {
            tvLineNumber.setText(String.valueOf(line.lineNumber));
            if (line.hasError) {
                itemView.setBackgroundColor(Color.parseColor("#2A1020"));
                tvLineNumber.setTextColor(Color.parseColor("#FF4466"));
                tvContent.setTextColor(Color.parseColor("#FF4466"));
                tvContent.setText("→ " + line.content);
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT);
                tvLineNumber.setTextColor(Color.parseColor("#2A3045"));
                tvContent.setTextColor(Color.parseColor("#E8EAF0"));
                tvContent.setText(line.content);
            }
        }
    }
}
