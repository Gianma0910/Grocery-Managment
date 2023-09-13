package classRecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groverymanagment.R;

import java.util.ArrayList;
import java.util.List;

import utilityClassDatabase.Ingredient;

/**
 * Adapter that uses List of Ingredient object used to show ingredients data of recipes shared by the Bluetooth "server".
 * In this adapter its items can't be modified or deleted.
 * This adapter must be set into the SharedRecipeViewHolder.
 */
public class CustomAdapterIngredientsSharedRecipe extends RecyclerView.Adapter<NoEditableIngredientViewHolder> {

    /**
     * List of Ingredient objects of a shered recipe.
     */
    List<Ingredient> ingredients;
    /**
     * Context object that represents where the object class has been instanced.
     */
    Context context;

    /**
     * CustomAdapterIngredientsSharedRecipe constructor.
     * @param context Context object
     */
    public CustomAdapterIngredientsSharedRecipe(Context context) {
        this.context = context;
        this.ingredients = new ArrayList<>();
    }

    @NonNull
    @Override
    public NoEditableIngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.no_editable_card_ingredient, parent, false);

        return new NoEditableIngredientViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NoEditableIngredientViewHolder holder, int position) {
        Ingredient i = ingredients.get(position);

        holder.textViewNameIngredient2.setText(i.getNameIngredient());
        holder.textViewDoseIngredient2.setText(i.getDoseIngredient());
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    /**
     * Method used to set/upload the adapter list.
     * @param ingredients List of Ingredient objects which has to use to set the one of the adapter.
     */
    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
        notifyDataSetChanged();
    }
}
