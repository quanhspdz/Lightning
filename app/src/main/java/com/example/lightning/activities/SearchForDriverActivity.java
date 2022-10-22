package com.example.lightning.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.lightning.R;
import com.example.lightning.models.Trip;
import com.example.lightning.tools.Const;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SearchForDriverActivity extends AppCompatActivity implements OnMapReadyCallback {
    
    private String tripId;
    private Trip trip;
    GoogleMap map;
    LatLng UET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_for_driver);

        init();

    }

    private void init() {
        UET = new LatLng(21.038902482537342, 105.78296809797327); //Dai hoc Cong Nghe Lat Lng

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment_maps);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        
        Intent intent = getIntent();
        if (intent.getStringExtra("tripId") == null) {
            Toast.makeText(this, "Can not get tripId!", Toast.LENGTH_SHORT).show();
        } else {
            tripId = intent.getStringExtra("tripId");
            getTripInfo(tripId);
        }
    }

    private void getTripInfo(String tripId) {
        FirebaseDatabase.getInstance().getReference().child("Trips")
                .child(tripId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        trip = snapshot.getValue(Trip.class);
                        if (trip != null) {
                            updateTripStatus(Const.searching, trip);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void updateTripStatus(String status, Trip trip) {
        FirebaseDatabase.getInstance().getReference().child("Activities").child("Trips")
                .child(status)
                .setValue(tripId);

        trip.setStatus(status);
        FirebaseDatabase.getInstance().getReference().child("Trips")
                .child(trip.getId())
                .setValue(trip);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(UET, 10));
    }
}