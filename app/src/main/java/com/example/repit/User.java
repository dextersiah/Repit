package com.example.repit;

public class User {

    private String firstName;
    private String lastName;
    private String imageURL;

    public User(){}

    public User(String firstName, String lastName, String imageURL) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.imageURL = imageURL;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getImageURL() {
        return imageURL;
    }

}
