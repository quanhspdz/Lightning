package com.example.lightning.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.lightning.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import de.hdodenhof.circleimageview.CircleImageView;

public class WaitingPickUp extends AppCompatActivity implements OnMapReadyCallback {

    RelativeLayout layoutStatusUpdate, layoutBottom;
    TextView textTimeLeft, textDistanceLeft, textPlate, textVehicleName,
                textDriverName;
    CircleImageView imgFocus, imgDriver;
    RelativeLayout btnCall, btnMessage;
    AppCompatButton btnCancel;

    boolean infoIsHided = true;

    GoogleMap maps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_pick_up);

        init();
        hideInfo();
        listener();

    }

    private void listener() {
        layoutStatusUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideInfo();
            }
        });
    }

    private void hideInfo() {
        if (infoIsHided) {
            infoIsHided = false;
            layoutBottom.setVisibility(View.VISIBLE);
        } else {
            infoIsHided = true;
            layoutBottom.setVisibility(View.GONE);
        }
    }

    private void init() {
        layoutStatusUpdate = findViewById(R.id.layout_statusUpdate);
        layoutBottom = findViewById(R.id.layout_driverInfo);
        textTimeLeft = findViewById(R.id.text_timeLeft);
        textDistanceLeft = findViewById(R.id.text_distanceLeft);
        textPlate = findViewById(R.id.text_plateNumber);
        textVehicleName = findViewById(R.id.text_vehicleName);
        textDriverName = findViewById(R.id.textDriverName);
        btnCall = findViewById(R.id.layout_call);
        btnMessage = findViewById(R.id.layout_chat);
        btnCancel = findViewById(R.id.buttonCancel);
        imgFocus = findViewById(R.id.img_focusOnMe);
        imgDriver = findViewById(R.id.img_driver);


        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment_maps);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
}