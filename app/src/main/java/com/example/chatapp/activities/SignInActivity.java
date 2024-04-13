package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivitySignInctivityBinding;
import com.example.chatapp.databinding.ActivitySignUpBinding;
import com.example.chatapp.firebase.MessagingService;
import com.example.chatapp.utilities.Constants;
import com.example.chatapp.utilities.PreferencesManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.C;

public class SignInActivity extends AppCompatActivity {
    private ActivitySignInctivityBinding binding;
    private MessagingService messagingService;
    private PreferencesManager preferencesManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInctivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferencesManager = new PreferencesManager(getApplicationContext());
        if (preferencesManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        setListeners();

    }

    private void setListeners() {
        binding.signUpLink.setOnClickListener(v ->
        startActivity(new Intent(SignInActivity.this,SignUpActivity.class)));
        binding.loginBtn.setOnClickListener(v -> {
            if (isValidSignInDetails()){
                signIn();
            }
        });
    }
    private void signIn(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null
                            && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferencesManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferencesManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferencesManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferencesManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        loading(false);
                        showToast("Unable sign in!");
                    }
                });
    }
    private void loading(Boolean isLoading){
        if (isLoading){
            binding.loginBtn.setVisibility(View.INVISIBLE);
            binding.progressBar1.setVisibility(View.VISIBLE);
        }else {
            binding.loginBtn.setVisibility(View.VISIBLE);
            binding.progressBar1.setVisibility(View.INVISIBLE);
        }
    }
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    private Boolean isValidSignInDetails(){
        if (binding.inputEmail.getText().toString().trim().isEmpty()){
            showToast("Enter email!");
            return false;
        }else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Enter valid email!");
            return false;
        }else if (binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Enter password");
            return false;
        }else {
            return true;
        }

    }
}