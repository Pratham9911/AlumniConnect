package com.pratham.alumniconnect;

public class UserModel {
    public String uid;
    public String name;
    public String email;
    public String profileImageUrl;
    public String backgroundImageUrl;
    public String role;
    public String workplace;
    public String address;
    public String bio;

    public UserModel() {} // Needed for Firebase

    public UserModel(String uid, String name, String email, String profileImageUrl, String backgroundImageUrl,
                     String role, String workplace, String address, String bio) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.backgroundImageUrl = backgroundImageUrl;
        this.role = role;
        this.workplace = workplace;
        this.address = address;
        this.bio = bio;
    }
}

