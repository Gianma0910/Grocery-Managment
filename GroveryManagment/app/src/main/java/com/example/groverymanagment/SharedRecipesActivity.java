package com.example.groverymanagment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import classRecyclerView.CustomAdapterSharedRecipe;
import database.GroveryManagmentDatabase;
import utilityClassDatabase.Ingredient;
import utilityClassDatabase.Recipe;

/**
 * Class that implements the shared_recipes_activity GUI.
 */
public class SharedRecipesActivity extends AppCompatActivity {

    /**
     * GUI Toolbar
     */
    Toolbar toolbar;
    /**
     * GUI Recycler view, used to contains CustomAdapterSharedRecipe object.
     */
    RecyclerView recyclerViewSharedRecipes;
    /**
     * GUI layout.
     */
    CoordinatorLayout coordinatorLayout;

    private static final String TAG = "SharedRecipesActivity";

    /**
     * Local database application.
     */
    private GroveryManagmentDatabase database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shared_recipes_activity);

        Intent i = getIntent();
        String nameDeviceConnectedTo = i.getStringExtra(ConnectToDeviceActivity.NAME_DEVICE_CONNECTED_TO_EXTRA);
        String recipesReceived = i.getStringExtra(ConnectToDeviceActivity.RECIPES_EXTRA);
        String ingredientsReceived = i.getStringExtra(ConnectToDeviceActivity.INGREDIENTS_EXTRA);

        coordinatorLayout = findViewById(R.id.coordinator_layout);
        toolbar = findViewById(R.id.toolbar_shared_recipes);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(nameDeviceConnectedTo + "'s recipes");

        /**
         * List of recipes received from Bluetooth "server".
         */
        ArrayList<Recipe> recipes = getRecipeFromJson(recipesReceived);
        /**
         * List of ingredients received from Bluetooth "server".
         */
        ArrayList<Ingredient> ingredients = getIngredientsFromJson(ingredientsReceived);

        recyclerViewSharedRecipes = findViewById(R.id.recycler_view_shared_recipes);
        CustomAdapterSharedRecipe adapterSharedRecipe = new CustomAdapterSharedRecipe(this, ingredients);
        adapterSharedRecipe.setRecipes(recipes);
        recyclerViewSharedRecipes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerViewSharedRecipes.setAdapter(adapterSharedRecipe);

        database = new GroveryManagmentDatabase(this);

        adapterSharedRecipe.setOnClickListener(recipeToSave -> {
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.submit(() -> {
                Log.d(TAG, "Recipe to save: " + recipeToSave.getIdRecipe() + ", " + recipeToSave.getNameRecipe());

                boolean isAlreadyPresent = database.isRecipeAlreadyPresentInDb(recipeToSave.getIdRecipe());

                Log.d(TAG, "flag isAlreadyPresent: " + isAlreadyPresent);

                if (!isAlreadyPresent) {
                    database.insertSharedRecipe(recipeToSave.getIdRecipe(), recipeToSave.getNameRecipe(), recipeToSave.getDescriptionRecipe());

                    for (Ingredient objIngredient : ingredients) {
                        if (objIngredient.getIdRecipe() == recipeToSave.getIdRecipe()) {
                            database.insertIngredient(objIngredient.getNameIngredient(), objIngredient.getDoseIngredient(), recipeToSave.getNameRecipe());
                        }
                    }

                    runOnUiThread(() -> showCustomSnackbar("Recipe " + recipeToSave.getNameRecipe() + " saved successfully in your recipes", R.drawable.ic_success));
                } else {
                    runOnUiThread(() -> showCustomSnackbar("Recipe " + recipeToSave.getNameRecipe() + " is already saved in your recipes", R.drawable.ic_warning_error));
                }
            });
            service.shutdown();
        });
    }

    /**
     * Method used to convert Json String into a list of Recipe objects.
     * @param recipesJson Json String that represents the serialization of a list of Recipe objects, received from Bluetooth "server".
     * @return A list of Recipe objects.
     */
    private ArrayList<Recipe> getRecipeFromJson(String recipesJson) {
        ArrayList<Recipe> recipes;
        Gson gson = new GsonBuilder().create();
        Type listOfRecipes = new TypeToken<ArrayList<Recipe>>() {}.getType();

        recipes = gson.fromJson(recipesJson, listOfRecipes);

        return recipes;
    }

    /**
     * Method used to convert Json String into a list of Ingredient objects.
     * @param ingredientsJson Json String that represents the serialization of a list of Ingredient objects, received from Bluetooth "server".
     * @return A list of Ingredient objects.
     */
    private ArrayList<Ingredient> getIngredientsFromJson(String ingredientsJson) {
        ArrayList<Ingredient> ingredients;
        Gson gson = new GsonBuilder().create();
        Type listOfIngredients = new TypeToken<ArrayList<Ingredient>>() {}.getType();

        ingredients = gson.fromJson(ingredientsJson, listOfIngredients);

        return ingredients;
    }

    /**
     * Method used to show a custom snackbar, to notify the user about error or successful operation.
     * @param message String that represents the message used to custom the snackbar.
     * @param icon Integer that represents the icon used to custom the snackbar.
     */
    private void showCustomSnackbar(String message, int icon) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        TextView snackbarTextView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);

        snackbarTextView.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
        snackbar.show();

    }
}
