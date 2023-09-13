package utilityClassDatabase;

public class Ingredient {

    private int idIngredient;
    private String nameIngredient;
    private String doseIngredient;
    private int idRecipeForIngredient;

    public Ingredient (int id, String name, String dose, int idRecipe) {
        this.idIngredient = id;
        this.nameIngredient = name;
        this.doseIngredient = dose;
        this.idRecipeForIngredient = idRecipe;
    }

    public void setNameRecipe(int idRecipe) {
        this.idRecipeForIngredient = idRecipe;
    }

    public int getIdRecipe() {
        return idRecipeForIngredient;
    }

    public void setNameIngredient(String nameIngredient) {
        this.nameIngredient = nameIngredient;
    }

    public String getNameIngredient() {
        return nameIngredient;
    }

    public void setDoseIngredient(String doseIngredient) {
        this.doseIngredient = doseIngredient;
    }

    public String getDoseIngredient() {
        return doseIngredient;
    }

    public void setIdIngredient(int idIngredient) {
        this.idIngredient = idIngredient;
    }

    public int getIdIngredient() {
        return idIngredient;
    }
}

