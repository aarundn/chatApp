package com.example.chatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.databinding.UserItemRecentMessageBinding;
import com.example.chatapp.listeners.conversionListener;
import com.example.chatapp.models.ChatMessage;
import com.example.chatapp.models.User;

import java.util.List;


public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder>{
    private final List<ChatMessage> messages;
    private final conversionListener conversionListener;

    public RecentConversationsAdapter(List<ChatMessage> messages, conversionListener conversionListener) {
        this.messages = messages;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                UserItemRecentMessageBinding.inflate(LayoutInflater.from(parent.getContext())
                ,parent
                ,false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder{
        UserItemRecentMessageBinding binding;
        ConversionViewHolder(UserItemRecentMessageBinding itemRecentMessageBinding){
            super(itemRecentMessageBinding.getRoot());
            binding = itemRecentMessageBinding;
        }
        void setData(ChatMessage chatMessage){
            binding.userImageViewTv.setImageBitmap(getConversationImage(chatMessage.conversionImage));
            binding.userNameTv.setText(chatMessage.conversionName);
            binding.messageTv.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.id = chatMessage.conversionId;
                user.name = chatMessage.conversionName;
                user.Image = chatMessage.conversionImage;
                conversionListener.onConversionClicked(user);
            });
        }
    }
    private Bitmap getConversationImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
