package classRecyclerView.CustomAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerymanagement.R;

import java.util.ArrayList;
import java.util.List;

import classRecyclerView.ViewHolder.SharedRecipeViewHolder;
import utilityClassDatabase.Ingredient;
import utilityClassDatabase.Recipe;

/**
 * Adapter that uses a List of Recipe objects and a List of Ingredient objects that contain all the shared recipes data with them ingredients.
 * In this adapter its items can't be modified or deleted.
 * This adapter must be set for SharedRecipesActivity's RecyclerView.
 */
public class CustomAdapterSharedRecipe extends RecyclerView.Adapter<SharedRecipeViewHolder> {

    /**
     * Context object that represents where the object class has been instanced.
     */
    Context context;
    /**
     * List of Recipe objects that contains all the shared recipes of another user.
     */
    List<Recipe> recipes;
    /**
     * OnClickListener interface used to set up some methods for adapter's item.
     */
    OnClickListener onClickListener;
    /**
     * List of Ingredient objects that contains all the ingredients data of the shared recipes.
     */
    List<Ingredient> ingredients;

    /**
     * Integer value that represents the position of the item into the two adapter lists.
     */
    private int position;

    /**
     * CustomAdapterSharedRecipe constructor.
     * @param context Context object that represents where the object class has been instanced.
     * @param ingredients List of Ingredient objects that contains all the ingredients data of the shared recipes.
     */
    public CustomAdapterSharedRecipe(Context context, List<Ingredient> ingredients) {
        this.context = context;
        this.recipes = new ArrayList<>();
        this.ingredients = ingredients;
    }

    @NonNull
    @Override
    public SharedRecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.card_shared_recipe, parent, false);

        return new SharedRecipeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SharedRecipeViewHolder holder, int position) {
        Recipe r = recipes.get(position);

        holder.textViewNameSharedRecipe.setText(r.getNameRecipe());
        holder.textViewDescriptionSharedRecipe.setText(r.getDescriptionRecipe());
        holder.adapterIngredientsSharedRecipe.setIngredients(getIngredientsOfARecipe(r.getIdRecipe()));
        holder.recyclerViewIngredientsSharedRecipe.setAdapter(holder.adapterIngredientsSharedRecipe);
        holder.recyclerViewIngredientsSharedRecipe.setLayoutManager(new LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false));

        holder.buttonSaveSharedRecipe.setOnClickListener(view -> {
            if (onClickListener != null) {
                setPosition(position);
                onClickListener.onClickSaveButton(recipes.get(this.position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    /**
     * Method used to set/upload the adapter list of shared recipes.
     * @param recipes
     */
    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }

    /**
     * Interface that declare a method for the adapter items.
     */
    public interface OnClickListener{
        /**
         * Method used to check which item has been clicked and then the method tries to save the item (shared recipe) into the local database.
         * @param recipeToSave Recipe object that represents the shared recipe to save into the local database.
         */
        void onClickSaveButton(Recipe recipeToSave);
    }

    /**
     * Method used to set the OnClickListener of the adapter.
     * @param onClickListener OnClickListener which has used to set the one of the adapter.
     */
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    /**
     * Method used to get a List of Ingredient objects that contains all the ingredients data of a specific recipe.
     * @param idRecipe Integer value that represents the ID of the recipe. This value is used to create the list of ingredients.
     * @return List of Ingredient objects that contains all the ingredients data of a specific recipe.
     */
    public List<Ingredient> getIngredientsOfARecipe(int idRecipe) {
        List<Ingredient> ingredientsOfARecipe = new ArrayList<>();

        for (Ingredient i : this.ingredients) {
            if (i.getIdRecipe() == idRecipe) {
                ingredientsOfARecipe.add(i);
            }
        }

        return ingredientsOfARecipe;
    }

    /**
     * Method used to set the position of the adapter item.
     * @param position Integer value used to set the position.
     */
    public void setPosition(int position) {
        this.position = position;
    }
}
