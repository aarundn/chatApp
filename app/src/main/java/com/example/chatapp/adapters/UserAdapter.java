package com.example.chatapp.adapters;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.chatapp.databinding.UserItemBinding;
import com.example.chatapp.listeners.UserListener;
import com.example.chatapp.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final List<User> users;
    private final UserListener userListener;

    public UserAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        UserItemBinding userItemBinding = UserItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new UserViewHolder(userItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        UserItemBinding binding;
        public UserViewHolder(UserItemBinding userItemBinding) {
            super(userItemBinding.getRoot());
            binding = userItemBinding;
        }
        void setUserData(User user){
            binding.userNameTv.setText(user.name);
            binding.EmailTv.setText(user.email);
            binding.userImageViewTv.setImageBitmap(getUserImage(user.Image));
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
        }
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }

}
