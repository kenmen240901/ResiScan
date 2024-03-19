package com.example.resiscan;

public class DataClass {
    private String title;
    private String firstName;
    private String lastName;
    private String wing;
    private String flatNumber;
    private String residentType;
    private String vehicleType;
    private String vehicleNumber;
    private String status;
    private String imageURL;
    private String key;

    public DataClass() {
    }

    public DataClass(String title, String firstName, String lastName, String wing, String flatNumber,
                     String residentType, String vehicleType, String vehicleNumber, String status,
                     String imageURL) {
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.wing = wing;
        this.flatNumber = flatNumber;
        this.residentType = residentType;
        this.vehicleType = vehicleType;
        this.vehicleNumber = vehicleNumber;
        this.status = status;
        this.imageURL = imageURL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getWing() {
        return wing;
    }

    public void setWing(String wing) {
        this.wing = wing;
    }

    public String getFlatNumber() {
        return flatNumber;
    }

    public void setFlatNumber(String flatNumber) {
        this.flatNumber = flatNumber;
    }

    public String getResidentType() {
        return residentType;
    }

    public void setResidentType(String residentType) {
        this.residentType = residentType;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}