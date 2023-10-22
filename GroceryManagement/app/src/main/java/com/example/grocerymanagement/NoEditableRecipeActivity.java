package com.example.grocerymanagement;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

import classRecyclerView.CustomAdapter.CustomAdapterNoEditableIngredient;
import database.GroveryManagmentDatabase;

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
    /** Name of recipe.*/
    private String nameRecipe;
    /** Description of recipe.*/
    private String descriptionRecipe;
    /**
     * Cursor used to initialize the adapter of recycler view.
     */
    private Cursor ingredientsCursor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.no_editable_recipe_gui);

        db = new GroveryManagmentDatabase(this);

        Intent intent = getIntent();

        idRecipe = intent.getIntExtra(MyRecipeActivity.ID_RECIPE_EXTRA, 0);
        nameRecipe = intent.getStringExtra(MyRecipeActivity.NAME_RECIPE_EXTRA);
        descriptionRecipe = intent.getStringExtra(MyRecipeActivity.DESCRIPTION_RECIPE_EXTRA);

        Bitmap imageRecipe = db.getImageRecipeById(idRecipe);

        toolbar = findViewById(R.id.toolbar_no_editable_recipe);
        textViewNameRecipe = findViewById(R.id.name_no_editable_recipe);
        textViewNameRecipe.startAnimation(AnimationUtils.loadAnimation(
                textViewNameRecipe.getContext(),
                android.R.anim.slide_in_left
        ));
        textViewDescriptionRecipe = findViewById(R.id.description_no_editable_recipe);
        textViewDescriptionRecipe.startAnimation(AnimationUtils.loadAnimation(
                textViewDescriptionRecipe.getContext(),
                android.R.anim.slide_in_left
        ));
        imageViewNoEditableRecipe = findViewById(R.id.image_view_no_editable_recipe);
        recyclerViewIngredient2 = findViewById(R.id.recycler_view_no_editable_recipe);

        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Details recipe");
        imageViewNoEditableRecipe.startAnimation(AnimationUtils.loadAnimation(
                imageViewNoEditableRecipe.getContext(),
                android.R.anim.fade_in
        ));
        if (imageRecipe != null) {
            imageViewNoEditableRecipe.setImageBitmap(imageRecipe);
        } else {
            imageViewNoEditableRecipe.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_error_image));
        }

        textViewDescriptionRecipe.setText(descriptionRecipe);
        textViewNameRecipe.setText(nameRecipe);

        ExecutorService setAdapterService = Executors.newSingleThreadExecutor();
        setAdapterService.submit(() -> {
            ingredientsCursor = db.getAllIngredientsOfARecipe(idRecipe);

            if (ingredientsCursor != null) {
                runOnUiThread(() -> {
                    adapter = new CustomAdapterNoEditableIngredient(this, ingredientsCursor);
                    recyclerViewIngredient2.setAdapter(adapter);
                    recyclerViewIngredient2.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
                });
            }

        });
        setAdapterService.shutdown();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ingredientsCursor.close();
    }
}