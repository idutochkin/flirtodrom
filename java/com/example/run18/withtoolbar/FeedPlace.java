package com.example.run18.withtoolbar;

public class FeedPlace {

    private String place_id;
    private String name;
    private String placeAddress;
    private String placePicture;
    private String count_check_in;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getPlaceAddress() {
        return placeAddress;
    }
    public void setPlaceAddress(String placeAddress) {
        this.placeAddress = placeAddress;
    }

    public String getPlacePicture() {
        return placePicture;
    }
    public void setPlacePicture(String placePicture) {
        this.placePicture = placePicture;
    }

    public String getPlaceId() {
        return place_id;
    }
    public void setPlaceId(String place_id) {
        this.place_id = place_id;
    }

    public String getCountCheckIn() {
        return count_check_in;
    }
    public void setCountCheckIn(String count_check_in) {
        this.count_check_in = count_check_in;
    }
}