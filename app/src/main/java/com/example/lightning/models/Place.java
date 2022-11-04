package com.example.lightning.models;

public class Place {
    private String placeId;
    private String mainText;
    private String secondaryText;

    public Place(String placeId, String mainText, String secondaryText) {
        this.placeId = placeId;
        this.mainText = mainText;
        this.secondaryText = secondaryText;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getMainText() {
        return mainText;
    }

    public void setMainText(String mainText) {
        this.mainText = mainText;
    }

    public String getSecondaryText() {
        return secondaryText;
    }

    public void setSecondaryText(String secondaryText) {
        this.secondaryText = secondaryText;
    }

    @Override
    public String toString() {
        return "Place{" +
                "mainText='" + mainText + '\'' +
                ", secondaryText='" + secondaryText + '\'' +
                '}';
    }
}
