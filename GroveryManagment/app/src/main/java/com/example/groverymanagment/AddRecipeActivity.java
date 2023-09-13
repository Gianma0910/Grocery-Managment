package com.example.groverymanagment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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

import classRecyclerView.CustomAdapterIngredient;
import database.GroveryManagmentDatabase;
import utilityClassDatabase.Ingredient;

/**
 * Class that implements the logi of editable_recipe_gui GUI. But in this case you insert a new recipe, you don't modify
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
     * Local list of Ingredient objects that have been added to this recipe.
     */
    private List<Ingredient> ingredientsRecipe;
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
        customAdapterIngredient = new CustomAdapterIngredient(this);
        recyclerViewIngredient.setAdapter(customAdapterIngredient);
        recyclerViewIngredient.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        buttonSubmitRecipe = findViewById(R.id.submit_recipe_button);
        buttonSubmitRecipe.setOnClickListener(this);

        customAdapterIngredient.setOnClickListener(new CustomAdapterIngredient.OnClickListener() {
            @Override
            public void onDelete(int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddRecipeActivity.this);
                builder.setTitle("Are you sure to delete this ingredient?");
                builder.setIcon(R.drawable.ic_delete);
                builder.setPositiveButton("Submit", (dialogInterface, i) -> {
                    ExecutorService service = Executors.newSingleThreadExecutor();
                    service.submit(() -> ingredientsRecipe.remove(position));
                    service.shutdown();

                    runOnUiThread(() -> {
                        customAdapterIngredient.setIngredients(ingredientsRecipe);
                        dialogInterface.cancel();
                    });
                });
                builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
                builder.show();
            }

            @Override
            public void onEdit(int position) {
                LayoutInflater inflater = LayoutInflater.from(AddRecipeActivity.this);
                View v = inflater.inflate(R.layout.insert_ingredient_data, null);

                EditText editTextNameIngredient = v.findViewById(R.id.edit_text_name_ingredient);
                EditText editTextDoseIngredient = v.findViewById(R.id.edit_text_dose_ingredient);
                Spinner spinnerUnitDoseIngredient = v.findViewById(R.id.spinner_unit_dose_ingredient);

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

                AlertDialog.Builder builder = new AlertDialog.Builder(AddRecipeActivity.this);
                builder.setTitle("Edit ingredient data");
                builder.setIcon(R.drawable.ic_edit);
                builder.setView(v);
                builder.setPositiveButton("Submit", (dialogInterface, i) -> {
                    String newName = editTextNameIngredient.getText().toString();
                    String newDose;

                    if (editTextDoseIngredient.getText().toString().length() == 0) {
                        newDose = String.valueOf(0);
                    } else {
                        newDose = editTextDoseIngredient.getText().toString();
                    }

                    if (newName.length() != 0 && !newDose.equals(String.valueOf(0))) {
                        Ingredient newIngredient = new Ingredient(0, newName, newDose + " " + unitDose, 0);

                        ExecutorService service = Executors.newSingleThreadExecutor();
                        service.submit(() -> {
                            boolean ingredientIsPresent = isIngredientAlreadyPresent(newIngredient);

                            if ((newIngredient.getNameIngredient().equals(ingredientsRecipe.get(position).getNameIngredient())
                                    && !newIngredient.getDoseIngredient().equals(ingredientsRecipe.get(position).getDoseIngredient()))
                                    || !ingredientIsPresent) {
                                ingredientsRecipe.remove(position);
                                ingredientsRecipe.add(position, newIngredient);

                                runOnUiThread(() -> {
                                    customAdapterIngredient.setIngredients(ingredientsRecipe);
                                    dialogInterface.cancel();
                                });
                            } else {
                                runOnUiThread(() -> showCustomSnackbar("The ingredient " + newName + " is already present in this recipe", R.drawable.ic_warning_error));
                            }
                        });
                        service.shutdown();
                    } else {
                        showCustomSnackbar("You have to insert name and dose to edit the ingredient", R.drawable.ic_warning_error);
                    }
                });
                builder.setNegativeButton("Cancel", ((dialogInterface, i) -> dialogInterface.cancel()));
                builder.show();
            }
        });

        this.activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        ExecutorService service = Executors.newSingleThreadExecutor();
                        service.submit(() -> {
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
                               Bitmap bitmapRotate = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                               runOnUiThread(() -> {
                                   imageViewRecipe.setImageBitmap(bitmapRotate);
                                   imageViewRecipe.setVisibility(View.VISIBLE);
                               });

                            } catch (IOException exception) {
                                showCustomSnackbar("Photo not found in the gallery", R.drawable.ic_warning_error);
                            }
                        });
                        service.shutdown();
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

            if (nameRecipe.length() == 0 || descriptionRecipe.length() == 0 || imageViewRecipe.getDrawable() == null || bitmap == null || ingredientsRecipe.size() == 0) {
                showCustomSnackbar("Some data are missing", R.drawable.ic_warning_error);
            } else {
                ExecutorService service = Executors.newSingleThreadExecutor();
                service.submit(() -> {
                    boolean isRecipePresent = database.isRecipeAlreadyPresentInDb(nameRecipe);

                    if (!isRecipePresent) {
                        database.insertRecipe(nameRecipe, descriptionRecipe, bitmap);

                        for (Ingredient i : ingredientsRecipe) {
                            database.insertIngredient(i.getNameIngredient(), i.getDoseIngredient(), nameRecipe);
                        }

                        runOnUiThread(() -> {
                            Intent i = new Intent(this, MyRecipeActivity.class);
                            setResult(Activity.RESULT_OK, i);
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> showCustomSnackbar(nameRecipe + " is already present in your recipe", R.drawable.ic_warning_error));
                    }
                });
            }
        } else if (view == imageViewRecipe) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intent);
        } else if (view == buttonAddIngredient) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View v = layoutInflater.inflate(R.layout.insert_ingredient_data, null);

            EditText editTextNameIngredient = v.findViewById(R.id.edit_text_name_ingredient);
            EditText editTextDoseIngredient = v.findViewById(R.id.edit_text_dose_ingredient);
            Spinner spinnerDose = v.findViewById(R.id.spinner_unit_dose_ingredient);

            ArrayList<String> itemSpinner = new ArrayList<>();
            itemSpinner.add("g");
            itemSpinner.add("kg");
            itemSpinner.add("l");
            itemSpinner.add("ml");

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(v.getContext(), android.R.layout.simple_spinner_dropdown_item, itemSpinner);
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

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Insert ingredient data");
            builder.setIcon(R.drawable.ic_add_ingredient);
            builder.setView(v);
            builder.setPositiveButton("Submit", (dialogInterface, i) -> {
                String nameIngredient = editTextNameIngredient.getText().toString();
                String dose;

                if (editTextDoseIngredient.getText().toString().length() == 0) {
                    dose = String.valueOf(0);
                } else {
                    dose = editTextDoseIngredient.getText().toString();
                }

                if (nameIngredient.length() != 0 && !dose.equals(String.valueOf(0))) {
                    Ingredient ingredient = new Ingredient(0, nameIngredient, dose + " " + unitDose, 0);

                    if (!isIngredientAlreadyPresent(ingredient)) {
                        ingredientsRecipe.add(ingredient);
                        customAdapterIngredient.setIngredients(ingredientsRecipe);
                        dialogInterface.cancel();
                    } else {
                        showCustomSnackbar("The ingredient " + nameIngredient + " is already present in this recipe", R.drawable.ic_warning_error);
                    }
                } else {
                    showCustomSnackbar("You have to insert name and dose of the new ingredient", R.drawable.ic_warning_error);
                }


            });
            builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
            builder.show();
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
