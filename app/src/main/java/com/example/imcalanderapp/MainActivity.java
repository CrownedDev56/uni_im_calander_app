package com.example.imcalanderapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity  implements DatePickerDialog.OnDateSetListener {
    public Button set;
    public Button chat;
    public static final String EXTRA_TIME = "TIME.EXTRA TEXT";
    public static final String EXTRA_DATE = "DATE.EXTRA TEXT";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        set = findViewById(R.id.button);


        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //uses the Datepicker fragment class to create a new calandar
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        // get the selected date and display it on the text view
        String currentDateString = DateFormat.getDateInstance((DateFormat.FULL)).format(c.getTime());
        TextView textView = findViewById(R.id.textView);
        textView.setText(currentDateString);

        // store the time and the date to be used for wearable group notifications
        Intent i = new Intent(MainActivity.this, ChatActivity.class);
        i.putExtra(EXTRA_TIME, c.getTime());
        i.putExtra(EXTRA_DATE, currentDateString);
        // start ChatActivity
        startActivity(i);
    }
}
