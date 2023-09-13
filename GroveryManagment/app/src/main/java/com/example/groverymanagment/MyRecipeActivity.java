package com.example.groverymanagment;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import classRecyclerView.CustomAdapterMyRecipes;
import database.GroveryManagmentDatabase;

/**
 * Class that implements the logic of my_recipes_gui GUI.
 */
public class MyRecipeActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * String that represents the name of the extra used to put an extra string for Intent.
     */
    public static final String ID_RECIPE_EXTRA = "IdRecipe";

    /**
     * GUI Toolbar.
     */
    Toolbar toolbar;
    /**
     * Recycler view of the GUI, used to contain CustomAdapterMyRecipes object.
     */
    RecyclerView recyclerViewMyRecipe;
    /**
     * GUI Button to add a new recipe.
     */
    Button buttonAddNewRecipe;

    /**
     * Adapter used to show recipes data. This adapter must be set for the recycler view of the GUI.
     */
    private CustomAdapterMyRecipes customAdapterMyRecipes;
    /**
     * Local database application.
     */
    private GroveryManagmentDatabase db;
    /**
     * Launcher to launch Intent for EditableRecipeActivity.class and AddRecipeActivity.class
     */
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_recipes_gui);

        toolbar = findViewById(R.id.toolbar_my_recipe);
        recyclerViewMyRecipe = findViewById(R.id.recycler_view_my_recipe);
        buttonAddNewRecipe = findViewById(R.id.button_add_new_recipe);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("My recipes");

        db = new GroveryManagmentDatabase(this);

        customAdapterMyRecipes = new CustomAdapterMyRecipes(this);

        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            Cursor cur = db.getAllRecipes();

            runOnUiThread(() -> {
                if (cur != null) {
                    customAdapterMyRecipes.setCursor(cur);
                }
            });
        });
        service.shutdown();

        recyclerViewMyRecipe.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerViewMyRecipe.setAdapter(customAdapterMyRecipes);

        customAdapterMyRecipes.setOnClickListener(new CustomAdapterMyRecipes.OnClickListener() {
            @Override
            public void onClickImage(int position, int idRecipe) {
                Intent intent = new Intent(MyRecipeActivity.this, NoEditableRecipeActivity.class);
                intent.putExtra(ID_RECIPE_EXTRA, idRecipe);
                startActivity(intent);
            }

            @Override
            public void onEdit(int position, int idRecipe) {
                Intent intent = new Intent(MyRecipeActivity.this, EditableRecipeActivity.class);
                intent.putExtra(ID_RECIPE_EXTRA, idRecipe);
                activityResultLauncher.launch(intent);
            }

            @Override
            public void onDelete(int position, int idRecipe) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MyRecipeActivity.this);
                builder.setTitle("Are you sure you want to delete this recipe?");
                builder.setIcon(R.drawable.ic_delete);
                builder.setPositiveButton("Yes", ((dialogInterface, i) -> {
                    ExecutorService service = Executors.newSingleThreadExecutor();
                    service.submit(() -> {
                        db.deleteRecipeById(idRecipe);
                        Cursor cursor1 = db.getAllRecipes();

                        runOnUiThread(() -> {
                            if (cursor1 != null) {
                                customAdapterMyRecipes.setCursor(cursor1);
                            }
                        });
                    });
                    service.shutdown();
                }));
                builder.setNegativeButton("No", ((dialogInterface, i) -> dialogInterface.cancel()));
                builder.show();
            }
        });

        this.activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            ExecutorService service1 = Executors.newSingleThreadExecutor();
                            service1.submit(() -> {
                                Cursor cur = db.getAllRecipes();

                                runOnUiThread(() -> {
                                    if (cur != null) {
                                        customAdapterMyRecipes.setCursor(cur);
                                    }
                                });
                            });
                            service1.shutdown();
                        }
                });

        buttonAddNewRecipe.setOnClickListener(this);
    }

    /**
     * Method used to check which view item has been clicked. The view must set OnClickListener.
     * @param view View object used to check which item has been clicked.
     */
    @Override
    public void onClick(View view) {
        if (view == buttonAddNewRecipe) {
            Intent intent = new Intent(this, AddRecipeActivity.class);
            activityResultLauncher.launch(intent);
        }
    }
}
