package classRecyclerView;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groverymanagment.R;

/**
 * ViewHolder subclass that describes an item view of AddRecipeActivity's RecyclerView and EditableRecipeActivity's RecyclerView.
 * This ViewHolder must be instanced by CustomAdapterIngredient class and CustomAdapterIngredient2 class.
 */
public class IngredientViewHolder extends RecyclerView.ViewHolder {

    /**
     * TextView of the item view that contains the string: "Name:"
     */
    TextView textNameIngredient;
    /**
     * TextView of the item view that contains the name of the ingredient.
     */
    TextView textViewNameIngredient;
    /**
     * TextView of the item view that contains the string: "Dose:".
     */
    TextView textDoseIngredient;
    /**
     * TextView of the item view that contains the dose of the ingredient.
     */
    TextView textViewDoseIngredient;
    /**
     * ImageButton of the item view that deletes the item view from the recycler view and from the local database.
     */
    ImageButton buttonDeleteIngredient;
    /**
     * ImageButton of the item view that modifies the item view in the recycler view and in the local database.
     */
    ImageButton buttonEditIngredient;

    /**
     * IngredientViewHolder constructor.
     * @param itemView View object that represents the single item view of the recycler view.
     */
    public IngredientViewHolder(@NonNull View itemView) {
        super(itemView);

        textNameIngredient = itemView.findViewById(R.id.text_name_ingredient);
        textViewNameIngredient = itemView.findViewById(R.id.text_view_name_ingredient);
        textDoseIngredient = itemView.findViewById(R.id.text_dose_ingredient);
        textViewDoseIngredient = itemView.findViewById(R.id.text_view_dose_ingredient);

        buttonDeleteIngredient = itemView.findViewById(R.id.button_delete_ingredient);
        buttonEditIngredient = itemView.findViewById(R.id.button_edit_ingredient);
    }
}
