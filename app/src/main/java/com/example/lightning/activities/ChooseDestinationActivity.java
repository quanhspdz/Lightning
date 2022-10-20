package com.example.lightning.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.example.lightning.R;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class ChooseDestinationActivity extends AppCompatActivity {

    EditText edtPickUp, edtDestination;
    AppCompatButton buttonConfirm;
    private static final Integer CHOOSE_DES_REQUEST_CODE = 1;
    private static final Integer CHOOSE_PICK_UP_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_destination);

        init();
        setStatusBarColor();
        listener();

    }

    private void listener() {
        edtDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAutoCompleteDestination(CHOOSE_DES_REQUEST_CODE);
            }
        });
    }

    private void getAutoCompleteDestination(Integer requestCode) {
        List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS,
                Place.Field.LAT_LNG, Place.Field.NAME);

        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,
                fieldList).build(ChooseDestinationActivity.this);

        startActivityForResult(intent, requestCode);
    }

    private void init() {
        edtDestination = findViewById(R.id.edt_dropOfPos);
        edtPickUp = findViewById(R.id.edt_pickUpPos);

        Places.initialize(ChooseDestinationActivity.this, getResources().getString(R.string.MAPS_API_KEY));
    }

    private void setStatusBarColor() {
        Window window = this.getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blue_toolbar));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_DES_REQUEST_CODE && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            int maxLength = 33;
            edtDestination.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
            edtDestination.setText(place.getAddress());
        }

        if (requestCode == CHOOSE_PICK_UP_REQUEST_CODE && resultCode == RESULT_OK) {

        }
    }
}