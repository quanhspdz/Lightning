package com.example.lightning.models;

import com.google.android.gms.maps.model.LatLng;

public class Trip {
    private String id;
    private String passengerId;
    private String driverId;
    private String pickUpLocation;
    private String dropOffLocation;
    private String distance;
    private String cost;
    private String timeCost;
    private String paymentMethod;
    private String createTime;
    private String endTime;

    public Trip(String id, String passengerId, String driverId, String pickUpLocation, String dropOffLocation, String distance, String cost, String timeCost, String paymentMethod, String createTime, String endTime) {
        this.id = id;
        this.passengerId = passengerId;
        this.driverId = driverId;
        this.pickUpLocation = pickUpLocation;
        this.dropOffLocation = dropOffLocation;
        this.distance = distance;
        this.cost = cost;
        this.timeCost = timeCost;
        this.paymentMethod = paymentMethod;
        this.createTime = createTime;
        this.endTime = endTime;
    }

    public Trip() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getPickUpLocation() {
        return pickUpLocation;
    }

    public void setPickUpLocation(String pickUpLocation) {
        this.pickUpLocation = pickUpLocation;
    }

    public String getDropOffLocation() {
        return dropOffLocation;
    }

    public void setDropOffLocation(String dropOffLocation) {
        this.dropOffLocation = dropOffLocation;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getTimeCost() {
        return timeCost;
    }

    public void setTimeCost(String timeCost) {
        this.timeCost = timeCost;
    }

    @Override
    public String toString() {
        return "Trip{" +
                "id='" + id + '\'' +
                ", passengerId='" + passengerId + '\'' +
                ", driverId='" + driverId + '\'' +
                ", pickUpLocation='" + pickUpLocation + '\'' +
                ", dropOffLocation='" + dropOffLocation + '\'' +
                ", distance='" + distance + '\'' +
                ", cost='" + cost + '\'' +
                ", timeCost='" + timeCost + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", createTime='" + createTime + '\'' +
                ", endTime='" + endTime + '\'' +
                '}';
    }
}
