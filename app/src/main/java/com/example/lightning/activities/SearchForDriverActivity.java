package com.example.lightning.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lightning.R;
import com.example.lightning.models.Trip;
import com.example.lightning.tools.Const;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchForDriverActivity extends AppCompatActivity implements OnMapReadyCallback {

    EditText edtPickUp, edtDes;
    ImageView imgVehicle;
    TextView textVehicleType, textTimeCost, textMoneyCost;

    public final int maxLength = 34;

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

    private void setTripInfo() {
        edtDes.setText(trip.getDropOffName());
        edtPickUp.setText(trip.getPickUpName());
        textTimeCost.setText(trip.getTimeCost());
        textMoneyCost.setText(trip.getCost());

        if (trip.getVehicleType().equals(Const.car)) {
            imgVehicle.setImageResource(R.drawable.car);
            textVehicleType.setText("Car");
        } else {
            imgVehicle.setImageResource(R.drawable.shipper);
            textVehicleType.setText("Motorbike");
        }
    }

    private void init() {
        edtDes = findViewById(R.id.edt_dropOfPos);
        edtPickUp = findViewById(R.id.edt_pickUpPos);
        textMoneyCost = findViewById(R.id.text_moneyCost);
        textTimeCost = findViewById(R.id.text_timeCost);
        textVehicleType = findViewById(R.id.text_vehicleType);
        imgVehicle = findViewById(R.id.imgVehicle);

        edtDes.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
        edtPickUp.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});

        UET = new LatLng(21.038902482537342, 105.78296809797327); //Dai hoc Cong Nghe Lat Lng

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment_maps);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        
        Intent intent = getIntent();
        if (intent.getStringExtra("tripId") == null) {
           getLastedTrip();
        } else {
            tripId = intent.getStringExtra("tripId");
            getTripInfo(tripId);
        }
    }

    private void getLastedTrip() {
        List<Trip> listTrips = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference().child("Trips")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Trip trip = dataSnapshot.getValue(Trip.class);
                            if (trip != null)
                                if (trip.getPassengerId().equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())) {
                                    listTrips.add(trip);
                                }
                        }
                        if (listTrips.size() > 0) {
                            trip = listTrips.get(listTrips.size() - 1);
                            setTripInfo();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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
                        setTripInfo();
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