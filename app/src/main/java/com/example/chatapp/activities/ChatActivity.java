package com.example.chatapp.activities;


import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatapp.R;
import com.example.chatapp.adapters.ChatAdapter;
import com.example.chatapp.databinding.ActivityChatBinding;
import com.example.chatapp.models.ChatMessage;
import com.example.chatapp.models.User;
import com.example.chatapp.utilities.Constants;
import com.example.chatapp.utilities.PreferencesManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.C;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private User recieverUser;
    private AlertDialog encryptionDialog;
    private int keyEncryption ;
    private List<ChatMessage> chatMessage;
    private ChatAdapter chatAdapter;
    private PreferencesManager preferencesManager;
    private FirebaseFirestore database;
    private String conversationId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadReceiverDetails();
        setListeners();
        init();
        listenMessages();

    }
    private void init(){
        preferencesManager = new PreferencesManager(getApplicationContext());
        chatMessage = new ArrayList<>();
        chatAdapter = new ChatAdapter(getBitmapFromEncodedString(recieverUser.Image)
                ,chatMessage
                ,preferencesManager.getString(Constants.KEY_USER_ID));
        binding.chatRecycler.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage(){
        HashMap<String , Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferencesManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, recieverUser.id);
        message.put(Constants.KEY_MESSAGE, encryptZigZag(binding.inputMessage.getText().toString(),3));
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if (conversationId != null){
            updateConversion(binding.inputMessage.getText().toString());
        } else {
            HashMap<String , Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID, preferencesManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, preferencesManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE , preferencesManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID, recieverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME , recieverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE , recieverUser.Image);
            conversion.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            conversion.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(conversion);
        }
        binding.inputMessage.setText(null);
    }


    private void listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferencesManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, recieverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
              .whereEqualTo(Constants.KEY_SENDER_ID,recieverUser.id )
               .whereEqualTo(Constants.KEY_RECEIVER_ID, preferencesManager.getString(Constants.KEY_USER_ID))
             .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) ->{
        if (error != null){
            return;
        }
        if (value != null){
            int count = chatMessage.size();
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    ChatMessage chat = new ChatMessage();
                    chat.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chat.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chat.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chat.dateTime = getReadableDate(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chat.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chat.KeyEncryption = keyEncryption;
                    chatMessage.add(chat);
                }

            }
//            chatMessage.sort(Comparator.comparing(chatMessage::));
//           chatMessage.sort(Comparator.comparing(obj -> obj.dateObject));
            chatMessage.sort(Comparator.comparing(ChatMessage::getDateObject));

            if (count == 0){
                chatAdapter.notifyDataSetChanged();
            }else {
                chatAdapter.notifyItemRangeInserted(chatMessage.size(), chatMessage.size());
                binding.chatRecycler.smoothScrollToPosition(chatMessage.size() - 1);
            }
            binding.chatRecycler.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if (conversationId == null){
            CheckForConversion();
        }
    };


    private Bitmap getBitmapFromEncodedString(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
    private  void loadReceiverDetails(){
        recieverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.name.setText(recieverUser.name);
        binding.imageProfile1.setImageBitmap(getUserImage(recieverUser.Image));
    }
    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }
    private void setListeners(){
        binding.back.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> {
            if ( !binding.inputMessage.getText().toString().isEmpty()) {
                sendMessage();
            }
        });
        binding.encryptKey.setOnClickListener(v -> showEncryptionDialog());
    }

    private void showEncryptionDialog() {
        if (encryptionDialog == null){
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ChatActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.pop_up_layout,
                    (ViewGroup) findViewById(R.id.encryptionPopUp)
            );
            builder.setView(view);
            encryptionDialog = builder.create();
            if (encryptionDialog.getWindow() != null){
                encryptionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.okButton).setOnClickListener(v -> {
                EditText text = view.findViewById(R.id.keyEditText);
                if (!text.getText().toString().isEmpty()){
                    keyEncryption = Integer.parseInt(text.getText().toString());

                }else {
                    Toast.makeText(getApplicationContext(), "must not be empty enter the key please!", Toast.LENGTH_SHORT).show();
                }


            });
            view.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    encryptionDialog.dismiss();
                }
            });
        }
        encryptionDialog.show();
    }


    private String getReadableDate(Date date){
        return new SimpleDateFormat("MMMM dd,yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
    private void addConversion(HashMap<String, Object> conversion){
        database.collection(Constants.KEY_COLLECTIONS_CONVERSATION)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }
    private void updateConversion(String message){
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTIONS_CONVERSATION).document(conversationId);
        documentReference.update(Constants.KEY_LAST_MESSAGE , message,
                Constants.KEY_TIMESTAMP, new Date());
    }
    private void CheckForConversion(){
        if (chatMessage.size() != 0){
            CheckForConversionRemotely(
                    preferencesManager.getString(Constants.KEY_USER_ID),
                    recieverUser.id
            );
            CheckForConversionRemotely(
                    recieverUser.id,
                    preferencesManager.getString(Constants.KEY_USER_ID)
            );
        }
    }

    private void CheckForConversionRemotely(String senderId, String receiverId){
        database.collection(Constants.KEY_COLLECTIONS_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversationOnCompleteListener);
    }
    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };
   private   String encryptZigZag(String text, int key)
    {

        char[][] rail = new char[key][text.length()];
        for (int i = 0; i < key; i++)
            Arrays.fill(rail[i], '\n');
        boolean dirDown = false;
        int row = 0, col = 0;
        for (int i = 0; i < text.length(); i++) {
            if (row == 0 || row == key - 1)
                dirDown = !dirDown;
            rail[row][col++] = text.charAt(i);
            if (dirDown)
                row++;
            else
                row--;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < key; i++)
            for (int j = 0; j < text.length(); j++)
                if (rail[i][j] != '\n')
                    result.append(rail[i][j]);

        return result.toString();
    }
    public static String decryptZigZag(String cipher, int key)
    {
        char[][] rail = new char[key][cipher.length()];
        for (int i = 0; i < key; i++)
            Arrays.fill(rail[i], '\n');
        boolean dirDown = true;
        int row = 0, col = 0;
        for (int i = 0; i < cipher.length(); i++) {
            if (row == 0)
                dirDown = true;
            if (row == key - 1)
                dirDown = false;
            rail[row][col++] = '*';
            if (dirDown)
                row++;
            else
                row--;
        }
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
            if (row == 0)
                dirDown = true;
            if (row == key - 1)
                dirDown = false;
            if (rail[row][col] != '*')
                result.append(rail[row][col++]);
            if (dirDown)
                row++;
            else
                row--;
        }
        return result.toString();
    }
}