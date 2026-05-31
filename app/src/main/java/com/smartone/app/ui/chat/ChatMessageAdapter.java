package com.smartone.app.ui.chat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
                public boolean areItemsTheSame(@NonNull ChatMessage a, @NonNull ChatMessage b) {
                    return a.id.equals(b.id);
                }
                @Override
                public boolean areContentsTheSame(@NonNull ChatMessage a, @NonNull ChatMessage b) {
                    return a.content.equals(b.content) && a.role == b.role;
                }
            };

    public ChatMessageAdapter() {
        super(DIFF);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        private final TextView    tvMessage;
        private final TextView    tvTime;
        private final FrameLayout bubbleContainer;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage       = itemView.findViewById(R.id.tvMessage);
            tvTime          = itemView.findViewById(R.id.tvTime);
            bubbleContainer = itemView.findViewById(R.id.bubbleContainer);
        }

        void bind(ChatMessage message) {
            tvMessage.setText(message.content);
            tvTime.setText(message.getFormattedTime());
            applyStyle(message);
            applyAlignment(message);
            setupLongPress(message);
        }

        private void applyStyle(ChatMessage message) {
            Context ctx = itemView.getContext();
            switch (message.role) {
                case USER:
                    tvMessage.setBackgroundResource(R.drawable.bg_bubble_user);
                    tvMessage.setTextColor(ContextCompat.getColor(ctx, R.color.bubble_user_text));
                    tvTime.setTextColor(ContextCompat.getColor(ctx, R.color.bubble_user_time));
                    break;
                case ASSISTANT:
                    tvMessage.setBackgroundResource(R.drawable.bg_bubble_assistant);
                    tvMessage.setTextColor(ContextCompat.getColor(ctx, R.color.bubble_assistant_text));
                    tvTime.setTextColor(ContextCompat.getColor(ctx, R.color.bubble_assistant_time));
                    break;
                case ERROR:
                    tvMessage.setBackgroundResource(R.drawable.bg_bubble_error);
                    tvMessage.setTextColor(ContextCompat.getColor(ctx, R.color.bubble_error_text));
                    tvTime.setTextColor(ContextCompat.getColor(ctx, R.color.bubble_error_time));
                    break;
                case SYSTEM:
                    tvMessage.setBackgroundResource(R.drawable.bg_bubble_system);
                    tvMessage.setTextColor(ContextCompat.getColor(ctx, R.color.bubble_system_text));
                    tvTime.setVisibility(View.GONE);
                    break;
            }
        }

        private void applyAlignment(ChatMessage message) {
            FrameLayout.LayoutParams params =
                    (FrameLayout.LayoutParams) tvMessage.getLayoutParams();
            ViewGroup.MarginLayoutParams containerParams =
                    (ViewGroup.MarginLayoutParams) bubbleContainer.getLayoutParams();
            int margin = dpToPx(64);
            if (message.isAlignedRight()) {
                params.gravity       = Gravity.END;
                containerParams.leftMargin  = margin;
                containerParams.rightMargin = dpToPx(8);
            } else {
                params.gravity       = Gravity.START;
                containerParams.leftMargin  = dpToPx(8);
                containerParams.rightMargin = margin;
            }
            tvMessage.setLayoutParams(params);
            bubbleContainer.setLayoutParams(containerParams);
        }

        private void setupLongPress(ChatMessage message) {
            tvMessage.setOnLongClickListener(v -> {
                Context ctx = v.getContext();
                ClipboardManager clipboard =
                        (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("mensaje", message.content);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ctx, "Mensaje copiado", Toast.LENGTH_SHORT).show();
                return true;
            });
        }

        private int dpToPx(int dp) {
            float density = itemView.getContext()
                    .getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }
}
