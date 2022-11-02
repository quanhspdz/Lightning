package com.example.lightning.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lightning.R;
import com.example.lightning.models.CurrentPosition;
import com.example.lightning.models.Driver;
import com.example.lightning.models.Trip;
import com.example.lightning.models.Vehicle;
import com.example.lightning.tools.DecodeTool;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

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
    private FusedLocationProviderClient fusedLocationClient;

    public static Driver driver;
    public static Trip trip;
    public static CurrentPosition currentPosition;
    public static Vehicle vehicle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_pick_up);

        init();
        hideInfo();
        listener();
    }

    private void getDataFromFirebase() {
        Intent intent = getIntent();
        String tripId = intent.getStringExtra("tripId");
        String driverId = intent.getStringExtra("driverId");
        
        getTripInfo(tripId);
        getDriverInfo(driverId);
        getStatusUpdate(driverId);
    }

    private void getStatusUpdate(String driverId) {
        if (driverId != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("CurrentPosition")
                    .child("Driver")
                    .child(driverId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            currentPosition = snapshot.getValue(CurrentPosition.class);
                            if (currentPosition != null) {
                                setStatusView();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    private void setStatusView() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (currentPosition == null) {

                }
                while (trip == null) {

                }

                while (currentPosition.getSpeed() == null) {

                }

                while (currentPosition.getSpeed().trim().isEmpty()) {

                }

                final float[] distance = {(float) DecodeTool.calculateDistance(
                        DecodeTool.getLatLngFromString(trip.getPickUpLocation()),
                        DecodeTool.getLatLngFromString(currentPosition.getPosition())
                )};

                distance[0] = distance[0] / 1000;

                float speed = Float.parseFloat(currentPosition.getSpeed());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (speed != 0) {
                            float time = (float) ((distance[0] / speed));
                            time *= 60;
                            textTimeLeft.setText(String.format("%s min", Math.round(time)));
                        } else {
                            textTimeLeft.setText("N/A");
                        }
                        distance[0] = Math.round(distance[0] * 10);
                        distance[0] /= 10;
                        textDistanceLeft.setText(String.format("%s km", distance[0]));

                    }});

            }
        }).start();
    }

    private void getDriverInfo(String driverId) {
        if (driverId != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("Drivers")
                    .child(driverId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            driver = snapshot.getValue(Driver.class);
                            if (driver != null) {
                                setDriverInfoView(driver);
                                getVehicleInfo(driver.getVehicleId());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    private void getVehicleInfo(String vehicleId) {
        if (vehicleId != null) {
            FirebaseDatabase.getInstance().getReference()
                    .child("Vehicles")
                    .child(vehicleId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            vehicle = snapshot.getValue(Vehicle.class);
                            if (vehicle != null) {
                                setVehicleView(vehicle);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    private void setVehicleView(Vehicle vehicle) {
        textVehicleName.setText(vehicle.getName());
        textPlate.setText(vehicle.getPlateNumber());
    }

    private void setDriverInfoView(Driver driver) {
        Picasso.get().load(driver.getDriverImageUrl()).resize(1000, 1000)
                .centerCrop().placeholder(R.drawable.user_blue).into(imgDriver);
        textDriverName.setText(driver.getName());
    }

    private void getTripInfo(String tripId) {
        if (tripId != null) {
            FirebaseDatabase.getInstance().getReference().child("Trips")
                    .child(tripId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            trip = snapshot.getValue(Trip.class);
                            if (trip != null) {
                                setTripInfoView(trip);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    private void setTripInfoView(Trip trip) {

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment_maps);
        mapFragment.getMapAsync(this);
    }

    private void markCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        if (maps != null) {
                            maps.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                        } else {

                        }
                    }
                });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        maps = googleMap;
        maps.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        maps.getUiSettings().setRotateGesturesEnabled(false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        maps.setMyLocationEnabled(true);
        markCurrentLocation();
        getDataFromFirebase();
    }
}