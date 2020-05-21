package com.example.apapointment;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {
    String value;

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        value = parent.getItemAtPosition(position).toString();
        Log.d(TAG, "onItemSelected: "+value);


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
