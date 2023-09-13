package classRecyclerView;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groverymanagment.R;

/**
 * ViewHolder subclass that describes an item view of the SharedRecipesActivity's RecyclerView.
 * This ViewHolder must be instanced by CustomAdapterSharedRecipe class.
 */
public class SharedRecipeViewHolder extends RecyclerView.ViewHolder {

    /**
     * TextView of the item view that contains the name of the shared recipe.
     */
    TextView textViewNameSharedRecipe;
    /**
     * TextView of the item view that contains the description of the shared recipe.
     */
    TextView textViewDescriptionSharedRecipe;
    /**
     * ImageButton of the item view that saves the shared recipe into the local database.
     */
    ImageButton buttonSaveSharedRecipe;
    /**
     * RecyclerView that contains all the item view with ingredients data of a specific shared recipe.
     */
    RecyclerView recyclerViewIngredientsSharedRecipe;
    /**
     * Adapter that contains all ingredients data of a specific shared recipe.
     */
    CustomAdapterIngredientsSharedRecipe adapterIngredientsSharedRecipe;

    /**
     * SharedRecipeViewHolder constructor.
     * @param itemView View object that represents the single item view of the recycler view.
     */
    public SharedRecipeViewHolder(@NonNull View itemView) {
        super(itemView);

        textViewNameSharedRecipe = itemView.findViewById(R.id.text_view_name_shared_recipe);
        buttonSaveSharedRecipe = itemView.findViewById(R.id.button_save_shared_recipe);
        textViewDescriptionSharedRecipe = itemView.findViewById(R.id.text_view_description_shared_recipe);
        recyclerViewIngredientsSharedRecipe = itemView.findViewById(R.id.recycler_view_ingredients_shared_recipe);

        adapterIngredientsSharedRecipe = new CustomAdapterIngredientsSharedRecipe(itemView.getContext());
    }
}
