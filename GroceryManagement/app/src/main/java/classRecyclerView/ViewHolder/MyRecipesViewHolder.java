package classRecyclerView.ViewHolder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerymanagement.R;

/**
 * ViewHolder subclass that describes an item view of the MyRecipesActivity's RecyclerView.
 * This ViewHolder must be instanced by CustomAdapterMyRecipes class.
 */
public class MyRecipesViewHolder extends RecyclerView.ViewHolder {

    /**
     * ImageView of the item view that contains the image of recipe.
     */
    public ImageView imageViewMyRecipe;
    /**
     * TextView of the item view that contains the name of recipe.
     */
    public TextView textViewNameMyRecipe;
    /**
     * TextView of the item view that contains the ID of recipe.
     */
    public TextView idRecipe;
    /**
     * ImageButton of the item view that modifies the item view in the recycler view and in the local database.
     */
    public ImageButton buttonEditRecipe;
    /**
     * ImageButton of the item view that deletes the item view from the recycler view and from the local database.
     */
    public ImageButton buttonDeleteRecipe;

    /**
     * MyRecipesViewHolder constructor.
     * @param itemView View object that represents the single item of the recycler view.
     */
    public MyRecipesViewHolder(@NonNull View itemView) {
        super(itemView);

        idRecipe = itemView.findViewById(R.id.id_recipe);
        imageViewMyRecipe = itemView.findViewById(R.id.image_view_my_recipe);
        textViewNameMyRecipe = itemView.findViewById(R.id.text_view_name_my_recipe);
        buttonDeleteRecipe = itemView.findViewById(R.id.button_delete_recipe);
        buttonEditRecipe = itemView.findViewById(R.id.button_edit_recipe);
    }
}
