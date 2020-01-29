package com.example.travelwithpurpose.models;

public class User {

    public String name, image, country, countryCode;

    public User(){

    }

    public User(String image, String name, String country, String countryCode) {
        this.image = image;
        this.name = name;
        this.country = country;
        this.countryCode = countryCode;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
