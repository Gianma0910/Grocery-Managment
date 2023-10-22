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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import classRecyclerView.CustomAdapter.CustomAdapterIngredient2;
import classRecyclerView.CustomAnimator.CustomAnimatorSlideInOut;
import database.GroveryManagmentDatabase;
import utilityClassDatabase.Ingredient;

/**
 * Class that implements the logic of editable_recipe_gui GUI. In this case you modify an existing recipe.
 */
public class EditableRecipeActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * GUI layout.
     */
    RelativeLayout relativeLayout;
    /**
     * GUI Toolbar.
     */
    Toolbar toolbar;
    /**
     * GUI EditText where you have to insert the name of the recipe.
     */
    EditText editTextNameRecipe;
    /**
     * GUI ImageView where you have to put an image from your phone gallery.
     */
    ImageView imageViewRecipe;
    /**
     * GUI EditText where you have to insert the description of the recipe.
     */
    EditText editTextDescriptionRecipe;
    /**
     * GUI RecyclerView that contains CustomAdapaterIngredient2 object.
     */
    RecyclerView recyclerViewIngredient;
    /**
     * GUI Button used to add a new ingredient to the recipe.
     */
    ImageButton buttonAddIngredient;
    /**
     * GUI Button used to save/submit the modification of the recipe.
     */
    Button submitRecipeButton;

    /**
     * ID of the recipe to modify.
     */
    private int idRecipe;
    /**
     * Name of the recipe to modify.
     */
    private String nameRecipe;
    /** Description of the recipe to modify.*/
    private String descriptionRecipe;
    /**
     * Bitmap object that represents the image of the recipe to modify.
     */
    private Bitmap imageRecipe;
    /**
     * Bitmap object that represents the image of the recipe but in the right orientation.
     */
    private Bitmap imageRecipeRotate;
    /**
     * String value that represents the unit of measure of the new/modified ingredient*/
    private String unitDose;
    /**
     * Local database application.
     */
    private GroveryManagmentDatabase db;
    /**
     * Adapter used to show ingredients data. It must be set for the recycler view GUI.
     */
    private CustomAdapterIngredient2 customAdapterIngredient2;
    /**
     * Launcher used to launch an Intent to MyRecipes.class, so that last GUI can be uploaded.
     */
    private ActivityResultLauncher<Intent> activityResultLauncher;
    /**
     * Uri object that represents the uri of the selected image.
     */
    private Uri targetUri;

    @SuppressLint("Range")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editable_recipe_gui);

        db = new GroveryManagmentDatabase(this);

        Intent intent = getIntent();

        idRecipe = intent.getIntExtra(MyRecipeActivity.ID_RECIPE_EXTRA, 0);
        nameRecipe = intent.getStringExtra(MyRecipeActivity.NAME_RECIPE_EXTRA);
        descriptionRecipe = intent.getStringExtra(MyRecipeActivity.DESCRIPTION_RECIPE_EXTRA);
        imageRecipe = db.getImageRecipeById(idRecipe);
        imageRecipeRotate = imageRecipe;

        relativeLayout = findViewById(R.id.relative_layout);
        toolbar = findViewById(R.id.toolbar_add_recipe);
        toolbar.setTitle("Modify details recipe");
        setSupportActionBar(toolbar);

        editTextNameRecipe = findViewById(R.id.edit_text_name_recipe);
        editTextNameRecipe.setText(nameRecipe);

        imageViewRecipe = findViewById(R.id.image_view_recipe);

        if (imageRecipe == null) {
            imageViewRecipe.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_error_image));
        } else {
            imageViewRecipe.setImageBitmap(imageRecipe);
        }

        editTextDescriptionRecipe = findViewById(R.id.edit_text_description_recipe);
        editTextDescriptionRecipe.setText(descriptionRecipe);

        recyclerViewIngredient = findViewById(R.id.recycler_view_ingredient);

        ExecutorService setAdapterService = Executors.newSingleThreadExecutor();
        setAdapterService.submit(() -> {
            Cursor ingredientsCursor = db.getAllIngredientsOfARecipe(idRecipe);

            if (ingredientsCursor != null) {
                runOnUiThread(() -> {
                    customAdapterIngredient2 = new CustomAdapterIngredient2(this, ingredientsCursor);

                    customAdapterIngredient2.setOnClickListener(new CustomAdapterIngredient2.OnClickListener() {
                        @Override
                        public void onDelete(int position, int idIngredient) {
                            AlertDialog.Builder deleteIngredientDialog = new AlertDialog.Builder(EditableRecipeActivity.this);
                            deleteIngredientDialog.setTitle("Warning deleting message!");
                            deleteIngredientDialog.setMessage("Do you want delete this ingredient?");
                            deleteIngredientDialog.setIcon(R.drawable.ic_delete);
                            deleteIngredientDialog.setPositiveButton("Submit", (dialogInterface, i) -> {
                                ExecutorService deleteIngredientService = Executors.newSingleThreadExecutor();
                                deleteIngredientService.submit(() -> {
                                    db.deleteIngredientById(idIngredient);
                                    Cursor cur = db.getAllIngredientsOfARecipe(idRecipe);

                                    runOnUiThread(() -> {
                                        if (cur != null) {
                                            customAdapterIngredient2.setCursor(cur);
                                            customAdapterIngredient2.notifyItemRemoved(position);
                                            dialogInterface.cancel();
                                        }
                                    });
                                });
                                deleteIngredientService.shutdown();
                            });
                            deleteIngredientDialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
                            deleteIngredientDialog.show();
                        }

                        @Override
                        public void onEdit(int position, int idProduct, String nameIngredient, String doseIngredient) {
                            LayoutInflater inflater = LayoutInflater.from(EditableRecipeActivity.this);
                            View insertDataLayout = inflater.inflate(R.layout.insert_ingredient_data, null);

                            EditText editTextNameIngredient = insertDataLayout.findViewById(R.id.edit_text_name_ingredient);
                            EditText editTextDoseIngredient = insertDataLayout.findViewById(R.id.edit_text_dose_ingredient);
                            Spinner spinnerUnitDoseIngredient = insertDataLayout.findViewById(R.id.spinner_unit_dose_ingredient);

                            int indexSeparator = doseIngredient.indexOf(" ");
                            String dose = doseIngredient.substring(0, indexSeparator);

                            editTextNameIngredient.setText(nameIngredient);
                            editTextDoseIngredient.setText(dose);

                            ArrayList<String> itemSpinner = new ArrayList<>();
                            itemSpinner.add("g");
                            itemSpinner.add("kg");
                            itemSpinner.add("l");
                            itemSpinner.add("ml");

                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(EditableRecipeActivity.this, android.R.layout.simple_spinner_dropdown_item, itemSpinner);
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
                                public void onNothingSelected(AdapterView<?> adapterView) {}
                            });

                            AlertDialog.Builder editDataDialog = new AlertDialog.Builder(EditableRecipeActivity.this);
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
                                    Ingredient newIngredient = new Ingredient(idProduct, newName, newDose + " " + unitDose, 0);

                                    ExecutorService editDataService = Executors.newSingleThreadExecutor();
                                    editDataService.submit(() -> {
                                        boolean ingredientIsPresent = db.isIngredientInsideRecipeById(newName, idRecipe);

                                        if ((newName.equals(nameIngredient) && !newIngredient.getDoseIngredient().equals(doseIngredient))
                                                || !ingredientIsPresent) {
                                            db.updateIngredientOfARecipe(idRecipe, newIngredient);
                                            Cursor ingredientsCursor = db.getAllIngredientsOfARecipe(idRecipe);

                                            if (ingredientsCursor != null) {
                                                runOnUiThread(() -> {
                                                    customAdapterIngredient2.setCursor(ingredientsCursor);
                                                    customAdapterIngredient2.notifyItemChanged(position);
                                                    dialogInterface.cancel();
                                                });
                                            }
                                        } else {
                                            runOnUiThread(() -> showCustomSnackbar("The ingredient " + newName + " is already present in this recipe", R.drawable.ic_warning_error));
                                        }
                                    });
                                    editDataService.shutdown();
                                } else {
                                    showCustomSnackbar("You have to insert name and dose to edit the ingredient", R.drawable.ic_warning_error);
                                }
                            });
                            editDataDialog.setNegativeButton("Cancel", ((dialogInterface, i) -> dialogInterface.cancel()));
                            editDataDialog.show();
                        }
                    });

                    recyclerViewIngredient.setAdapter(customAdapterIngredient2);
                    recyclerViewIngredient.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
                    recyclerViewIngredient.setItemAnimator(new CustomAnimatorSlideInOut());
                });
            }

        });
        setAdapterService.shutdown();

        buttonAddIngredient = findViewById(R.id.button_add_ingredient);
        submitRecipeButton = findViewById(R.id.submit_recipe_button);

        buttonAddIngredient.setOnClickListener(this);
        submitRecipeButton.setOnClickListener(this);
        imageViewRecipe.setOnClickListener(this);

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
                                imageRecipe = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                                imageRecipeRotate = Bitmap.createBitmap(imageRecipe, 0, 0, imageRecipe.getWidth(), imageRecipe.getHeight(), matrix, true);

                                runOnUiThread(() -> {
                                    imageViewRecipe.startAnimation(AnimationUtils.loadAnimation(
                                            imageViewRecipe.getContext(),
                                            android.R.anim.fade_in
                                            ));
                                    imageViewRecipe.setImageBitmap(imageRecipeRotate);
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
     * Method used to check which item has been clicked. The items must be set OnClickListener.
     * @param view View object used to check which item has been clicked.
     */
    @Override
    public void onClick(View view) {
        if (view == submitRecipeButton) {
            String nameRecipe = editTextNameRecipe.getText().toString();
            String descriptionRecipe = editTextDescriptionRecipe.getText().toString();
            Cursor ingredientsCursor = db.getAllIngredientsOfARecipe(idRecipe);

            if (nameRecipe.length() == 0 || descriptionRecipe.length() == 0 || ingredientsCursor.getCount() == 0 || imageRecipeRotate == null) {
                showCustomSnackbar("Some data are missing", R.drawable.ic_warning_error);
                ingredientsCursor.close();
            } else {
                ExecutorService service = Executors.newSingleThreadExecutor();
                service.submit(() -> {
                    ingredientsCursor.close();
                    boolean isRecipePresent = db.isRecipeAlreadyPresentInDb(nameRecipe);

                    if (!isRecipePresent || nameRecipe.equals(this.nameRecipe)) {
                        db.updateRecipeData(idRecipe, nameRecipe, descriptionRecipe, imageRecipeRotate);

                        runOnUiThread(() -> {
                            setResult(Activity.RESULT_OK);
                            finish();
                        });
                    } else {
                        runOnUiThread(() -> showCustomSnackbar(nameRecipe + " is already present in your recipe", R.drawable.ic_warning_error));
                    }
                });
                service.shutdown();
            }
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
                ExecutorService insertNewIngredientService = Executors.newSingleThreadExecutor();
                insertNewIngredientService.submit(() -> {
                    String nameIngredient = editTextNameIngredient.getText().toString();
                    String doseIngredient = editTextDoseIngredient.getText().toString();

                    if (nameIngredient.length() == 0 || doseIngredient.length() == 0) {
                        showCustomSnackbar("You have to insert a name and a dose for the new ingredient", R.drawable.ic_warning_error);
                    } else {
                        boolean ingredientIsPresent = db.isIngredientInsideRecipeByName(nameIngredient, nameRecipe);

                        if (!ingredientIsPresent) {
                            db.insertIngredient(nameIngredient, editTextDoseIngredient.getText().toString() + " " + unitDose, nameRecipe);
                            Cursor ingredientsCursor = db.getAllIngredientsOfARecipe(idRecipe);

                            if (ingredientsCursor != null) {
                                ingredientsCursor.moveToLast();
                                runOnUiThread(() -> {
                                    customAdapterIngredient2.setCursor(ingredientsCursor);
                                    customAdapterIngredient2.notifyItemInserted(ingredientsCursor.getPosition());
                                });
                            }
                        } else {
                            runOnUiThread(() -> showCustomSnackbar("The ingredient " + nameIngredient + " is already present in this recipe", R.drawable.ic_warning_error));
                        }
                    }
                });
                insertNewIngredientService.shutdown();

                dialogInterface.cancel();
            });
            insertDataDialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
            insertDataDialog.show();
        } else if (view == imageViewRecipe) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intent);
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder closeActivityDialog = new AlertDialog.Builder(this);
        closeActivityDialog.setTitle("Warning!");
        closeActivityDialog.setMessage("Are you sure to close? Your modifies won't be saved!");
        closeActivityDialog.setIcon(R.drawable.ic_warning_error);
        closeActivityDialog.setPositiveButton("Yes!", ((dialogInterface, i) -> {
            setResult(RESULT_OK);
            finish();
        }));
        closeActivityDialog.setNegativeButton("No", ((dialogInterface, i) -> dialogInterface.cancel()));
        closeActivityDialog.show();
    }

    /**
     * Private method used to show a custom snackbar to notify the user aboutn error or successful operation.
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
