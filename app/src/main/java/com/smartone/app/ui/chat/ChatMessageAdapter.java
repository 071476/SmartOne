package com.smartone.app.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.smartone.app.R;
import com.smartone.app.data.repository.ChatMessage;

public class ChatMessageAdapter
        extends ListAdapter<ChatMessage, ChatMessageAdapter.MessageViewHolder> {

    private static final DiffUtil.ItemCallback<ChatMessage> DIFF =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull ChatMessage a, @NonNull ChatMessage b) {
                    return a.id.equals(b.id);
                }
                @Override
                public boolean areContentsTheSame(
                        @NonNull ChatMessage a, @NonNull ChatMessage b) {
                    return a.content.equals(b.content) && a.role == b.role;
                }
            };

    public ChatMessageAdapter() {
        super(DIFF);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull MessageViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvMessage;
        private final TextView tvTime;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime    = itemView.findViewById(R.id.tvTime);
        }

        void bind(ChatMessage message) {
            tvMessage.setText(message.content);
            tvTime.setText(message.getFormattedTime());

            switch (message.role) {
                case USER:
                    tvMessage.setBackgroundResource(R.drawable.bg_bubble_user);
                    tvMessage.setTextColor(0xFFA8F0FF);
                    break;
                case ASSISTANT:
                    tvMessage.setBackgroundResource(R.drawable.bg_bubble_assistant);
                    tvMessage.setTextColor(0xFFC0C8D8);
                    break;
                case ERROR:
                    tvMessage.setBackgroundResource(R.drawable.bg_bubble_error);
                    tvMessage.setTextColor(0xFFFF8899);
                    break;
                case SYSTEM:
                    tvMessage.setBackgroundResource(R.drawable.bg_bubble_system);
                    tvMessage.setTextColor(0xFF6B7280);
                    tvTime.setVisibility(View.GONE);
                    break;
            }
        }
    }
}
