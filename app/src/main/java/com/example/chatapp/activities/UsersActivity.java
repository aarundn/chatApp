package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chatapp.R;
import com.example.chatapp.adapters.UserAdapter;
import com.example.chatapp.databinding.ActivityUsersBinding;
import com.example.chatapp.listeners.UserListener;
import com.example.chatapp.models.User;
import com.example.chatapp.utilities.Constants;
import com.example.chatapp.utilities.PreferencesManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity implements UserListener {
    ActivityUsersBinding binding;
    PreferencesManager preferencesManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListeners();
        preferencesManager = new PreferencesManager(getApplicationContext());
        getUsers();
    }
    private void setListeners(){
        binding.back.setOnClickListener(v -> onBackPressed());
    }

    private void getUsers(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferencesManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User>  users = new ArrayList<>();
                        for(QueryDocumentSnapshot snapshot: task.getResult()){
                            if (currentUserId.equals(snapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.name = snapshot.getString(Constants.KEY_NAME);
                            user.email = snapshot.getString(Constants.KEY_EMAIL);
                            user.Image = snapshot.getString(Constants.KEY_IMAGE);
                            user.token = snapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = snapshot.getId();
                            users.add(user);
                        }
                        if (users.size() > 0){
                            UserAdapter userAdapter = new UserAdapter(users, this);
                            binding.UsersRecyclerView.setAdapter(userAdapter);
                            binding.UsersRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }
    private void showErrorMessage(){
        binding.textError.setText(String.format("%s","no user available!"));
        binding.textError.setVisibility(View.VISIBLE);
    }
    private void loading(Boolean isLoading){
        if (isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}