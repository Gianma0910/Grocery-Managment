package com.example.grocerymanagement;

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

import classRecyclerView.CustomAdapter.CustomAdapterMyRecipes;
import classRecyclerView.CustomAnimator.CustomAnimatorFadeInOut;
import database.GroveryManagmentDatabase;

/**
 * Class that implements the logic of my_recipes_gui GUI.
 */
public class MyRecipeActivity extends AppCompatActivity implements View.OnClickListener {

    /** String that represents the name of the extra used to put the id of recipe as extra for an Intent.*/
    public static final String ID_RECIPE_EXTRA = "IdRecipe";
    /** String that represents the name of the extra used to put name of recipe as extra for an Intent.*/
    public static final String NAME_RECIPE_EXTRA = "NameRecipe";
    /** String that represents the name of the extra used to put description of recipe as extra for an Intent.*/
    public static final String DESCRIPTION_RECIPE_EXTRA = "DescriptionRecipe";

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
     * Launcher to launch Intent for activity AddRecipeActivity,
     */
    private ActivityResultLauncher<Intent> activityResultLauncherNewRecipe;
    /**
     * Launcher to launch Intent for activity EditableRecipeActivity,
     */
    private ActivityResultLauncher<Intent> activityResultLauncherEditRecipe;
    /** Integer value that represents the position of the item modified that belongs to recycler view.
     * This integer value is used to update the recycler view.
     */
    private int positionItemModified;

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

        ExecutorService setAdapterService = Executors.newSingleThreadExecutor();
        setAdapterService.submit(() -> {
            Cursor recipesCursor = db.getAllRecipes();

            if (recipesCursor != null) {
                runOnUiThread(() -> {
                    customAdapterMyRecipes = new CustomAdapterMyRecipes(this, recipesCursor);

                    customAdapterMyRecipes.setOnClickListener(new CustomAdapterMyRecipes.OnClickListener() {
                        @Override
                        public void onClickImage(int position, int idRecipe, String nameRecipe, String descriptionRecipe) {
                            Intent intent = new Intent(MyRecipeActivity.this, NoEditableRecipeActivity.class);
                            intent.putExtra(ID_RECIPE_EXTRA, idRecipe);
                            intent.putExtra(NAME_RECIPE_EXTRA, nameRecipe);
                            intent.putExtra(DESCRIPTION_RECIPE_EXTRA, descriptionRecipe);
                            startActivity(intent);
                        }

                        @Override
                        public void onEdit(int position, int idRecipe, String nameRecipe, String descriptionRecipe) {
                            positionItemModified = position;
                            Intent intent = new Intent(MyRecipeActivity.this, EditableRecipeActivity.class);
                            intent.putExtra(ID_RECIPE_EXTRA, idRecipe);
                            intent.putExtra(NAME_RECIPE_EXTRA, nameRecipe);
                            intent.putExtra(DESCRIPTION_RECIPE_EXTRA, descriptionRecipe);
                            activityResultLauncherEditRecipe.launch(intent);
                        }

                        @Override
                        public void onDelete(int position, int idRecipe) {
                            AlertDialog.Builder deleteRecipeDialog = new AlertDialog.Builder(MyRecipeActivity.this);
                            deleteRecipeDialog.setTitle("Warning deleting message!");
                            deleteRecipeDialog.setMessage("Do you want to delete this recipe?");
                            deleteRecipeDialog.setIcon(R.drawable.ic_delete);
                            deleteRecipeDialog.setPositiveButton("Yes", ((dialogInterface, i) -> {
                                ExecutorService service = Executors.newSingleThreadExecutor();
                                service.submit(() -> {
                                    db.deleteRecipeById(idRecipe);
                                    Cursor recipesCursor = db.getAllRecipes();

                                    runOnUiThread(() -> {
                                        if (recipesCursor != null) {
                                            customAdapterMyRecipes.setCursor(recipesCursor);
                                            customAdapterMyRecipes.notifyItemRemoved(position);
                                        }
                                    });
                                });
                                service.shutdown();
                            }));
                            deleteRecipeDialog.setNegativeButton("No", ((dialogInterface, i) -> dialogInterface.cancel()));
                            deleteRecipeDialog.show();
                        }
                    });

                    recyclerViewMyRecipe.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
                    recyclerViewMyRecipe.setAdapter(customAdapterMyRecipes);
                    recyclerViewMyRecipe.setItemAnimator(new CustomAnimatorFadeInOut());
                });
            }
        });
        setAdapterService.shutdown();

        this.activityResultLauncherNewRecipe = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        ExecutorService insertNewRecipeService = Executors.newSingleThreadExecutor();
                        insertNewRecipeService.submit(() -> {
                            Cursor recipesCursor = db.getAllRecipes();

                            if (recipesCursor != null) {
                                recipesCursor.moveToLast();
                                runOnUiThread(() -> {
                                    customAdapterMyRecipes.setCursor(recipesCursor);
                                    customAdapterMyRecipes.notifyItemInserted(recipesCursor.getPosition());
                                });
                            }

                        });
                        insertNewRecipeService.shutdown();
                    }
                });

        this.activityResultLauncherEditRecipe = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        ExecutorService editRecipeService = Executors.newSingleThreadExecutor();
                        editRecipeService.submit(() -> {
                           Cursor recipesCursor = db.getAllRecipes();

                           if (recipesCursor != null) {
                               runOnUiThread(() -> {
                                   customAdapterMyRecipes.setCursor(recipesCursor);
                                   customAdapterMyRecipes.notifyItemChanged(positionItemModified);
                               });
                           }
                        });
                        editRecipeService.shutdown();
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
            activityResultLauncherNewRecipe.launch(intent);
        }
    }
}
