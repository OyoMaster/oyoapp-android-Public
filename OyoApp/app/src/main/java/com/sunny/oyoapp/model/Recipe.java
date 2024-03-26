package com.sunny.oyoapp.model;

public class Recipe {

    public int id;
    public String imageURL;
    public String title;
    public String ingredients;
    public String recipe;

    public Recipe(String title, String imageURL, String ingredients, String recipe) {
        this.title = title;
        this.imageURL = imageURL;
        this.ingredients = ingredients;
        this.recipe = recipe;
    }
}

