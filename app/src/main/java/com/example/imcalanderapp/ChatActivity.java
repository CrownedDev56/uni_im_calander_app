package com.example.imcalanderapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class ChatActivity extends AppCompatActivity {

    private EditText editText;
    private ListView listView;
    private DatabaseReference databaseReference;
    private String sMessage;
    private Cipher cipher, decipher;
    private SecretKeySpec secretKey;

    //16 bit Encryption key
    private byte[] encryptKey = {9,115,51,6,105,4,-31,-23,-60,0,17,20,3,-106,119,-53};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();

        /// Date/time stored in seperate variables to be used in the chat activity and wearable notifications
        final String time = intent.getStringExtra(MainActivity.EXTRA_TIME);
        final String date = intent.getStringExtra(MainActivity.EXTRA_DATE);

        TextView textView = findViewById(R.id.tvDATE);
        textView.setText(date);

        final ListView listView = findViewById(R.id.messageLv);
        databaseReference = FirebaseDatabase.getInstance().getReference("Messages");

        try {
            // Advanced Encrypting Standard
            cipher = Cipher.getInstance("AES");
            decipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }


        secretKey = new SecretKeySpec(encryptKey, "AES");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // reads data from database and displays it on the activity list

                sMessage = dataSnapshot.getValue().toString();
                sMessage = sMessage.substring(1,sMessage.length()-1);

                String[] sMessageArray;
                sMessageArray = sMessage.split(", ");
                Arrays.sort(sMessageArray);
                String[] stringFinal = new String[sMessageArray.length * 2];

                try {
                    // loops through the message strings in the data base and displays them on the activity list
                    for (int i = 0; i < sMessageArray.length; i++) {
                        // splits the string into the date and message before displaying it
                        String[] stringK= sMessageArray[i].split("=", 2);
                        stringFinal[2 * i] = (String) android.text.format.DateFormat.format("dd-MM-yyyy hh:mm:ss", Long.parseLong(stringK[0]));
                        stringFinal[ 2*i+1] = AESDecrypt(stringK[1]);
                        listView.setAdapter(new ArrayAdapter<>(ChatActivity.this, android.R.layout.simple_list_item_1, stringFinal));
                    }
                } catch (NumberFormatException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void sendButton(View v) throws UnsupportedEncodingException {
        final EditText editText = findViewById(R.id.messageEt);
        Date date = new Date();
        // converts  time and the send edit text data into an encrypted String
        String eText =  AESEncrypt(editText.getText().toString());
        databaseReference.child(String.valueOf(date.getTime())).setValue(eText);
        editText.setText("");

        // notification for wearable

        //Intent i = new Intent(getApplicationContext(), ChatActivity.class);
        //PendingIntent pendingIntent = PendingIntent.getActivity(this,0,i,0);


//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "M_CH_ID")
  //              .setContentText("Message: ")
    //            .setContentText(AESDecrypt(eText))
      //          .setSmallIcon(R.drawable.common_full_open_on_phone)
        //        .setContentIntent(pendingIntent);
     //   Notification notification  = notificationBuilder.build();
 //       NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//      notificationManager.notify(1, notification);

    }

    private String AESEncrypt(String string){
        // converts the string into an encrypted byte format
        byte[] stringByte = string.getBytes();
        byte[] eByte = new byte[stringByte.length];

        try {
            //encrypts the string byte with the cipher
            cipher.init(Cipher.ENCRYPT_MODE,secretKey);
            eByte = cipher.doFinal(stringByte);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        String returnString = null;
        try{
            // Return string as ISO Latin Alphabet No.
            returnString = new String(eByte, StandardCharsets.ISO_8859_1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnString;
    }

    private String AESDecrypt(String string) throws UnsupportedEncodingException {
        //converts the encrypted byte into string  format
        byte[] eByte = string.getBytes("ISO_8859_1");
        String deString = string;
        byte[] decrypt;

        try {
            //decipers the encrypted byte and returns it as a string
            decipher.init(Cipher.DECRYPT_MODE, secretKey);
            decrypt = decipher.doFinal(eByte);
            deString = new String(decrypt);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return deString;
    }
}
