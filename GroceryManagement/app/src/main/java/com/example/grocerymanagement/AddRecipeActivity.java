package com.example.grocerymanagement;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import classRecyclerView.CustomAdapter.CustomAdapterIngredient;
import classRecyclerView.CustomAnimator.CustomAnimatorSlideInOut;
import database.GroveryManagmentDatabase;
import utilityClassDatabase.Ingredient;

/**
 * Class that implements the logic of editable_recipe_gui GUI. But in this case you insert a new recipe, you don't modify
 * an existing one.
 */
public class AddRecipeActivity extends AppCompatActivity implements View.OnClickListener {
    /**
     * GUI Toolbar
     */
    Toolbar toolbar;
    /**
     * GUI EditText where you have to insert the name of new recipe.
     */
    EditText editTextNameRecipe;
    /**
     * GUI ImageView where you have to put an image from your phone gallery.
     */
    ImageView imageViewRecipe;
    /**
     * GUI EditText where you have to insert the description of new recipe.
     */
    EditText editTextDescriptionRecipe;
    /**
     * GUI ImageButton to add a new ingredient to the recipe.
     */
    ImageButton buttonAddIngredient;
    /**
     * GUI RecyclerView that contains aCustomAdapterIngredient object.
     */
    RecyclerView recyclerViewIngredient;
    /**
     * GUI Button to save/submit the new recipe.
     */
    Button buttonSubmitRecipe;
    /**
     * GUI layout.
     */
    RelativeLayout relativeLayout;

    /**
     * Local database application.
     */
    private GroveryManagmentDatabase database;
    /**
     * Adapter used to show ingredients data of the recipe. This adapter must be set for the recycler view GUI.
     */
    private CustomAdapterIngredient customAdapterIngredient;
    /**
     * Launcher used to launch Intent ACTION_PICK, to handle the selection of image from your phone gallery.
     */
    private ActivityResultLauncher<Intent> activityResultLauncher;
    /**
     * String value that represents the unit of measure selected for the new ingredient.
     */
    private String unitDose;
    /**
     * Uri object that represents the uri of the selected image.
     */
    private Uri targetUri;
    /**
     * Bitmap object used to set the image for ImageView GUI.
     */
    private Bitmap bitmap;
    /**
     * Bitmap object that represents the image for ImageView GUI but in the right orientation.
     */
    private Bitmap bitmapRotate;

    private List<Ingredient> ingredientsRecipe;

    @SuppressLint("Range")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editable_recipe_gui);

        this.ingredientsRecipe = new ArrayList<>();

        relativeLayout = findViewById(R.id.relative_layout);

        toolbar = findViewById(R.id.toolbar_add_recipe);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Add recipe");

        editTextNameRecipe = findViewById(R.id.edit_text_name_recipe);

        imageViewRecipe = findViewById(R.id.image_view_recipe);
        imageViewRecipe.setOnClickListener(this);

        editTextDescriptionRecipe = findViewById(R.id.edit_text_description_recipe);

        buttonAddIngredient = findViewById(R.id.button_add_ingredient);
        buttonAddIngredient.setOnClickListener(this);

        database = new GroveryManagmentDatabase(this);

        recyclerViewIngredient = findViewById(R.id.recycler_view_ingredient);
        customAdapterIngredient = new CustomAdapterIngredient(this, ingredientsRecipe);
        recyclerViewIngredient.setAdapter(customAdapterIngredient);
        recyclerViewIngredient.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerViewIngredient.setItemAnimator(new CustomAnimatorSlideInOut());

        customAdapterIngredient.setOnClickListener(new CustomAdapterIngredient.OnClickListener() {
            @Override
            public void onDelete(int position) {
                AlertDialog.Builder deleteIngredientDialog = new AlertDialog.Builder(AddRecipeActivity.this);
                deleteIngredientDialog.setTitle("Warning deleting message!");
                deleteIngredientDialog.setMessage("Do you want to delete this ingredient?");
                deleteIngredientDialog.setIcon(R.drawable.ic_delete);
                deleteIngredientDialog.setPositiveButton("Submit", (dialogInterface, i) -> {
                        ingredientsRecipe.remove(position);
                        customAdapterIngredient.setIngredients(ingredientsRecipe);
                        customAdapterIngredient.notifyItemRemoved(position);

                        dialogInterface.cancel();
                });
                deleteIngredientDialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
                deleteIngredientDialog.show();
            }

            @Override
            public void onEdit(int position) {
                LayoutInflater inflater = LayoutInflater.from(AddRecipeActivity.this);
                View insertDataLayout = inflater.inflate(R.layout.insert_ingredient_data, null);

                EditText editTextNameIngredient = insertDataLayout.findViewById(R.id.edit_text_name_ingredient);
                EditText editTextDoseIngredient = insertDataLayout.findViewById(R.id.edit_text_dose_ingredient);
                Spinner spinnerUnitDoseIngredient = insertDataLayout.findViewById(R.id.spinner_unit_dose_ingredient);

                int indexSeparator = ingredientsRecipe.get(position).getDoseIngredient().indexOf(" ");
                String dose = ingredientsRecipe.get(position).getDoseIngredient().substring(0, indexSeparator);

                editTextNameIngredient.setText(ingredientsRecipe.get(position).getNameIngredient());
                editTextDoseIngredient.setText(dose);

                ArrayList<String> itemSpinner = new ArrayList<>();
                itemSpinner.add("g");
                itemSpinner.add("kg");
                itemSpinner.add("l");
                itemSpinner.add("ml");

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(AddRecipeActivity.this, android.R.layout.simple_spinner_dropdown_item, itemSpinner);
                spinnerUnitDoseIngredient.setAdapter(arrayAdapter);

                spinnerUnitDoseIngredient.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        switch (adapterView.getSelectedItemPosition()) {
                            case 0: {
                                unitDose = "g";
                                break;
                            }
                            case 1: {
                                unitDose = "kg";
                                break;
                            }
                            case 2: {
                                unitDose = "l";
                                break;
                            }
                            case 3: {
                                unitDose = "ml";
                                break;
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });

                AlertDialog.Builder editDataDialog = new AlertDialog.Builder(AddRecipeActivity.this);
                editDataDialog.setTitle("Modify ingredient's data");
                editDataDialog.setIcon(R.drawable.ic_edit);
                editDataDialog.setView(insertDataLayout);
                editDataDialog.setPositiveButton("Submit", (dialogInterface, i) -> {
                    String newName = editTextNameIngredient.getText().toString();
                    String newDose;

                    if (editTextDoseIngredient.getText().toString().length() == 0) {
                        newDose = String.valueOf(0);
                    } else {
                        newDose = editTextDoseIngredient.getText().toString();
                    }

                    if (newName.length() != 0 && !newDose.equals(String.valueOf(0))) {
                        Ingredient newIngredient = new Ingredient(0, newName, newDose + " " + unitDose, 0);

                        ExecutorService editProductDataService = Executors.newSingleThreadExecutor();
                        editProductDataService.submit(() -> {
                            boolean ingredientIsPresent = isIngredientAlreadyPresent(newIngredient);

                            if ((newIngredient.getNameIngredient().equals(ingredientsRecipe.get(position).getNameIngredient())
                                    && !newIngredient.getDoseIngredient().equals(ingredientsRecipe.get(position).getDoseIngredient()))
                                    || !ingredientIsPresent) {
                                ingredientsRecipe.remove(position);
                                ingredientsRecipe.add(position, newIngredient);

                                runOnUiThread(() -> {
                                    customAdapterIngredient.setIngredients(ingredientsRecipe);
                                    customAdapterIngredient.notifyItemChanged(position);
                                    dialogInterface.cancel();
                                });
                            } else {
                                runOnUiThread(() -> showCustomSnackbar("The ingredient " + newName + " is already present in this recipe", R.drawable.ic_warning_error));
                            }
                        });
                        editProductDataService.shutdown();
                    } else {
                        showCustomSnackbar("You have to insert name and dose to edit the ingredient", R.drawable.ic_warning_error);
                    }
                });
                editDataDialog.setNegativeButton("Cancel", ((dialogInterface, i) -> dialogInterface.cancel()));
                editDataDialog.show();
            }
        });

        buttonSubmitRecipe = findViewById(R.id.submit_recipe_button);
        buttonSubmitRecipe.setOnClickListener(this);

        this.activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        ExecutorService setImageRecipeService = Executors.newSingleThreadExecutor();
                        setImageRecipeService.submit(() -> {
                            assert result.getData() != null;
                            targetUri = result.getData().getData();
                            String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
                            Cursor cur = getContentResolver().query(targetUri, orientationColumn, null, null, null);
                            int orientation = 0;
                            if (cur != null && cur.moveToFirst()) {
                                orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
                                cur.close();
                            }
                            Matrix matrix = new Matrix();
                            matrix.postRotate(orientation);
                            try {
                                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                                bitmapRotate = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                                runOnUiThread(() -> {
                                    imageViewRecipe.startAnimation(AnimationUtils.loadAnimation(
                                            imageViewRecipe.getContext(),
                                            android.R.anim.fade_in
                                    ));
                                    imageViewRecipe.setImageBitmap(bitmapRotate);
                                    imageViewRecipe.setVisibility(View.VISIBLE);
                                });

                            } catch (IOException exception) {
                                showCustomSnackbar("Photo not found in the gallery", R.drawable.ic_warning_error);
                            }
                        });
                        setImageRecipeService.shutdown();
                    }
                }
        );
    }
    /**
     * Method used to check which item has been clicked. The items must set OnClickListener.
     * @param view View object used to check which item has been clicked.
     */
    @Override
    public void onClick(View view) {
        if (view == buttonSubmitRecipe) {
            String nameRecipe = editTextNameRecipe.getText().toString();
            String descriptionRecipe = editTextDescriptionRecipe.getText().toString();

            if (nameRecipe.length() == 0 || descriptionRecipe.length() == 0 || imageViewRecipe.getDrawable() == null || bitmapRotate == null || ingredientsRecipe.size() == 0) {
                showCustomSnackbar("Some data are missing", R.drawable.ic_warning_error);
            } else {
                ExecutorService insertNewRecipeService = Executors.newSingleThreadExecutor();
                insertNewRecipeService.submit(() -> {
                    boolean isRecipePresent = database.isRecipeAlreadyPresentInDb(nameRecipe);

                    if (!isRecipePresent) {
                        database.insertRecipe(nameRecipe, descriptionRecipe, bitmapRotate);

                        for (Ingredient i : ingredientsRecipe) {
                            database.insertIngredient(i.getNameIngredient(), i.getDoseIngredient(), nameRecipe);
                        }

                        runOnUiThread(() -> {
                            setResult(Activity.RESULT_OK);
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> showCustomSnackbar(nameRecipe + " is already present in your recipe", R.drawable.ic_warning_error));
                    }
                });
                insertNewRecipeService.shutdown();
            }
        } else if (view == imageViewRecipe) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intent);
        } else if (view == buttonAddIngredient) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View insertDataLayout = layoutInflater.inflate(R.layout.insert_ingredient_data, null);

            EditText editTextNameIngredient = insertDataLayout.findViewById(R.id.edit_text_name_ingredient);
            EditText editTextDoseIngredient = insertDataLayout.findViewById(R.id.edit_text_dose_ingredient);
            Spinner spinnerDose = insertDataLayout.findViewById(R.id.spinner_unit_dose_ingredient);

            ArrayList<String> itemSpinner = new ArrayList<>();
            itemSpinner.add("g");
            itemSpinner.add("kg");
            itemSpinner.add("l");
            itemSpinner.add("ml");

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(insertDataLayout.getContext(), android.R.layout.simple_spinner_dropdown_item, itemSpinner);
            spinnerDose.setAdapter(arrayAdapter);

            spinnerDose.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    switch (adapterView.getSelectedItemPosition()) {
                        case 0: {
                            unitDose = "g";
                            break;
                        }
                        case 1: {
                            unitDose = "kg";
                            break;
                        }
                        case 2: {
                            unitDose = "l";
                            break;
                        }
                        case 3: {
                            unitDose = "ml";
                            break;
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });

            AlertDialog.Builder insertDataDialog = new AlertDialog.Builder(this);
            insertDataDialog.setTitle("Add new ingredient to recipe");
            insertDataDialog.setIcon(R.drawable.ic_add_ingredient);
            insertDataDialog.setView(insertDataLayout);
            insertDataDialog.setPositiveButton("Submit", (dialogInterface, i) -> {
                    String nameIngredient = editTextNameIngredient.getText().toString();
                    String dose;

                    if (editTextDoseIngredient.getText().toString().length() == 0) {
                        dose = String.valueOf(0);
                    } else {
                        dose = editTextDoseIngredient.getText().toString();
                    }

                    if (nameIngredient.length() != 0 && !dose.equals(String.valueOf(0))) {
                        Ingredient ingredient = new Ingredient(0, nameIngredient, dose + " " + unitDose, 0);
                        boolean isAlreadyPresent = isIngredientAlreadyPresent(ingredient);

                        if (!isAlreadyPresent) {
                            ingredientsRecipe.add(ingredient);
                            customAdapterIngredient.setIngredients(ingredientsRecipe);
                            customAdapterIngredient.notifyItemInserted(ingredientsRecipe.indexOf(ingredient));
                            dialogInterface.cancel();
                        } else {
                            showCustomSnackbar("The ingredient " + nameIngredient + " is already present in this recipe", R.drawable.ic_warning_error);
                        }
                    } else {
                        showCustomSnackbar("You have to insert name and dose of the new ingredient", R.drawable.ic_warning_error);
                    }
            });
            insertDataDialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
            insertDataDialog.show();
        }
    }

    /**
     * Private method used to check if an ingredient is already present in the recipe with the same name of the new ingredient.
     * @param i Ingredient object used to check if there is another ingredient in the list with the same name.
     * @return True if there is another ingredient in the list with the same name, false otherwise.
     */
    private boolean isIngredientAlreadyPresent(Ingredient i) {
        for (Ingredient objIngredient : ingredientsRecipe) {
            if (objIngredient.getNameIngredient().equals(i.getNameIngredient())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder closeActivityDialog = new AlertDialog.Builder(this);
        closeActivityDialog.setTitle("Warning!");
        closeActivityDialog.setMessage("Are you sure to close? Your recipe won't be saved!");
        closeActivityDialog.setIcon(R.drawable.ic_warning_error);
        closeActivityDialog.setPositiveButton("Yes!", (dialogInterface, i) -> {
            dialogInterface.cancel();
            finish();
        });
        closeActivityDialog.setNegativeButton("No", ((dialogInterface, i) -> dialogInterface.cancel()));
        closeActivityDialog.show();
    }

    /**
     * Private method used to show a custom snackbar to notify the user about error or successful operation.
     * @param message String value that represents the message used to custom the snackbar.
     * @param icon Integer value that represents the icon used to custom the snackbar.
     */
    private void showCustomSnackbar(String message, int icon) {
        Snackbar snackbar = Snackbar.make(relativeLayout, message, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();

        TextView snackbarTextView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        snackbarTextView.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);

        snackbar.show();
    }
}
