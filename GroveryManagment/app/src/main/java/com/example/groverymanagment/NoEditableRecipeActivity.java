package com.example.groverymanagment;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import classRecyclerView.CustomAdapterMyRecipes;
import classRecyclerView.CustomAdapterNoEditableIngredient;
import database.GroveryManagmentDatabase;
import utilityClassDatabase.Recipe;

/**
 * Class that implements the logic of no_editable_recipe_gui GUI.
 */
public class NoEditableRecipeActivity extends AppCompatActivity {

    /**
     * GUI Toolbar
     */
    Toolbar toolbar;
    /**
     * GUI TextView that contains the description recipe.
     */
    TextView textViewDescriptionRecipe;
    /**
     * GUI TextView that contains the name recipe.
     */
    TextView textViewNameRecipe;
    /**
     * GUI ImageView that contains the image recipe.
     */
    ImageView imageViewNoEditableRecipe;
    /**
     * GUI Recycler view, used to contain CustomAdapterNoEditableIngredient object.
     */
    RecyclerView recyclerViewIngredient2;

    /**
     * Local database application.
     */
    private GroveryManagmentDatabase db;
    /**
     * Adapter used to show ingredients data. This adapter must be set for the recycler view of the GUI.
     */
    private CustomAdapterNoEditableIngredient adapter;
    /**
     * ID of recipe.
     */
    private int idRecipe;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.no_editable_recipe_gui);

        db = new GroveryManagmentDatabase(this);

        Intent intent = getIntent();

        idRecipe = intent.getIntExtra(MyRecipeActivity.ID_RECIPE_EXTRA, 0);

        Recipe recipe = db.getRecipeById(idRecipe);

        String nameRecipe = recipe.getNameRecipe();
        String descriptionRecipe = recipe.getDescriptionRecipe();
        Bitmap image = recipe.getImageRecipe();

        toolbar = findViewById(R.id.toolbar_no_editable_recipe);
        textViewNameRecipe = findViewById(R.id.name_no_editable_recipe);
        textViewDescriptionRecipe = findViewById(R.id.description_no_editable_recipe);
        imageViewNoEditableRecipe = findViewById(R.id.image_view_no_editable_recipe);
        recyclerViewIngredient2 = findViewById(R.id.recycler_view_no_editable_recipe);

        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Details recipe");
        imageViewNoEditableRecipe.setImageBitmap(image);
        textViewDescriptionRecipe.setText(descriptionRecipe);
        textViewNameRecipe.setText(nameRecipe);

        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            Cursor cur = db.getAllIngredientsOfARecipe(idRecipe);

            if (cur != null) {
                runOnUiThread(() -> {
                    adapter = new CustomAdapterNoEditableIngredient(this, cur);
                    recyclerViewIngredient2.setAdapter(adapter);
                    recyclerViewIngredient2.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
                });
            }

        });
        service.shutdown();
    }
}
