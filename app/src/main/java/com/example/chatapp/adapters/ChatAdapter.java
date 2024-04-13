package com.example.chatapp.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.databinding.ItemContainerSentMessageBinding;
import com.example.chatapp.databinding.ItemMessageRevievedBinding;
import com.example.chatapp.models.ChatMessage;

import java.util.Arrays;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Bitmap receiverImageProfile;
    private final List<ChatMessage> chatMessages;
    private final String senderId;

    public final int VIEW_TYPE_SENT = 1;
    public final int VIEW_TYPE_RECEIVE = 2;

    public ChatAdapter(Bitmap receiverImageProfile, List<ChatMessage> chatMessages, String senderId) {
        this.receiverImageProfile = receiverImageProfile;
        this.chatMessages = chatMessages;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            ItemContainerSentMessageBinding itemContainerSentMessageBinding = ItemContainerSentMessageBinding.inflate(
                    LayoutInflater.from(parent.getContext())
                    , parent
                    , false
            );
            return new sentMessageViewHolder(itemContainerSentMessageBinding);
        } else  {
            ItemMessageRevievedBinding itemMessageRevievedBinding = ItemMessageRevievedBinding.inflate(
                    LayoutInflater.from(parent.getContext())
                    , parent
                    , false
            );
            return new ReceiveMessageViewHolder(itemMessageRevievedBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT){
            ((sentMessageViewHolder) holder).setData(chatMessages.get(position));
        }else {
            ((ReceiveMessageViewHolder) holder).setDataR(chatMessages.get(position),receiverImageProfile);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }else {
            return VIEW_TYPE_RECEIVE;
        }
    }

    public static class sentMessageViewHolder extends RecyclerView.ViewHolder {
        private ItemContainerSentMessageBinding binding;
        public sentMessageViewHolder(ItemContainerSentMessageBinding sentMessageBinding) {
            super(sentMessageBinding.getRoot());
            binding = sentMessageBinding;
        }
        void setData(ChatMessage chatMessage){
            binding.textMessage.setText(decryptZigZag(chatMessage.message,3));
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }
    static class ReceiveMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemMessageRevievedBinding binding;
         ReceiveMessageViewHolder(ItemMessageRevievedBinding revievedBinding) {
            super(revievedBinding.getRoot());
            binding = revievedBinding;
        }
        void setDataR(ChatMessage chatMessage,Bitmap receiverProfileImage){
             binding.textMessage.setText(decryptZigZag(chatMessage.message,3));
             binding.textDateTime.setText(chatMessage.dateTime);
             binding.imageProfile.setImageBitmap(receiverProfileImage);
        }
    }
    public static String decryptZigZag(String cipher, int key)
    {

        // create the matrix to cipher plain text
        // key = rows , length(text) = columns
        char[][] rail = new char[key][cipher.length()];

        // filling the rail matrix to distinguish filled
        // spaces from blank ones
        for (int i = 0; i < key; i++)
            Arrays.fill(rail[i], '\n');

        // to find the direction
        boolean dirDown = true;

        int row = 0, col = 0;

        // mark the places with '*'
        for (int i = 0; i < cipher.length(); i++) {
            // check the direction of flow
            if (row == 0)
                dirDown = true;
            if (row == key - 1)
                dirDown = false;

            // place the marker
            rail[row][col++] = '*';

            // find the next row using direction flag
            if (dirDown)
                row++;
            else
                row--;
        }

        // now we can construct the fill the rail matrix
        int index = 0;
        for (int i = 0; i < key; i++)
            for (int j = 0; j < cipher.length(); j++)
                if (rail[i][j] == '*'
                        && index < cipher.length())
                    rail[i][j] = cipher.charAt(index++);

        StringBuilder result = new StringBuilder();

        row = 0;
        col = 0;
        for (int i = 0; i < cipher.length(); i++) {
            // check the direction of flow
            if (row == 0)
                dirDown = true;
            if (row == key - 1)
                dirDown = false;

            // place the marker
            if (rail[row][col] != '*')
                result.append(rail[row][col++]);

            // find the next row using direction flag
            if (dirDown)
                row++;
            else
                row--;
        }
        return result.toString();
    }
}
