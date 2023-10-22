package classRecyclerView.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerymanagement.R;

/**
 * ViewHolder subclass that describes an item view of the NoEditableRecipeActivity's RecyclerView and CustomAdapterSharedRecipe's RecyclerView.
 * This ViewHolder must be instanced by CustomAdapterNoEditableIngredient class and CustomAdapterIngredientsSharedRecipe class.
 */
public class NoEditableIngredientViewHolder extends RecyclerView.ViewHolder {

    /**
     * TextView of the item view that contains the name of the ingredient.
     */
    public TextView textViewNameIngredient2;
    /**
     * TextView of the item view that contains the dose of the ingredient.
     */
    public TextView textViewDoseIngredient2;

    /**
     * NoEditableIngredientViewHolder constructor.
     * @param itemView View object that represents the single item view of the recycler view.
     */
    public NoEditableIngredientViewHolder(@NonNull View itemView) {
        super(itemView);

        textViewNameIngredient2 = itemView.findViewById(R.id.text_view_name_ingredient2);
        textViewDoseIngredient2 = itemView.findViewById(R.id.text_view_dose_ingredient2);
    }
}