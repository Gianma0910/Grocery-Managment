package utilityClassDatabase;

import android.graphics.Bitmap;

public class Recipe {

    private int idRecipe;
    private String descriptionRecipe;
    private Bitmap imageRecipe;
    private String nameRecipe;
    private byte[] imageInByte;

    public Recipe(int idRecipe, String name, String description, Bitmap image) {
        this.idRecipe = idRecipe;
        this.nameRecipe = name;
        this.descriptionRecipe = description;
        this.imageRecipe = image;
    }

    public Recipe(int idRecipe, String name, String description) {
        this.idRecipe = idRecipe;
        this.nameRecipe = name;
        this.descriptionRecipe = description;
    }

    public void setDescriptionRecipe(String descriptionRecipe) {
        this.descriptionRecipe = descriptionRecipe;
    }

    public void setIdRecipe(int idRecipe) {
        this.idRecipe = idRecipe;
    }

    public int getIdRecipe() {
        return idRecipe;
    }

    public String getDescriptionRecipe() {
        return descriptionRecipe;
    }

    public void setImageRecipe(Bitmap imageRecipe) {
        this.imageRecipe = imageRecipe;
    }

    public Bitmap getImageRecipe() {
        return imageRecipe;
    }

    public void setNameRecipe(String nameRecipe) {
        this.nameRecipe = nameRecipe;
    }

    public String getNameRecipe() {
        return nameRecipe;
    }

    public void setImageInByte(byte[] imageInByte) {
        this.imageInByte = imageInByte;
    }

    public byte[] getImageInByte() {
        return imageInByte;
    }
}
