package classRecyclerView.CustomAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerymanagement.R;

import java.util.ArrayList;
import java.util.List;

import classRecyclerView.ViewHolder.IngredientViewHolder;
import utilityClassDatabase.Ingredient;

public class CustomAdapterIngredient extends RecyclerView.Adapter<IngredientViewHolder> {
    /**
     * List of Ingredient objects of a recipe.
     */
    List<Ingredient> ingredients;
    /**
     * Context object that represents where the object class has been instanced.
     */
    Context context;
    /**
     * OnClickListener interface used to set up some methods for adapter's item.
     */
    OnClickListener onClickListener;

    /**
     * CustomerAdapterIngredient constructor.
     *
     * @param context Context object that represents where the object class has been instanced.
     */
    public CustomAdapterIngredient(Context context, List<Ingredient> ingredients) {
        this.context = context;
        this.ingredients = ingredients;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.card_ingredient_add_operation, parent, false);

        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);

        holder.textViewNameIngredient.setText(ingredient.getNameIngredient());
        holder.textViewDoseIngredient.setText(String.valueOf(ingredient.getDoseIngredient()));

        holder.buttonDeleteIngredient.setOnClickListener(view -> {
            if (onClickListener != null) {
                onClickListener.onDelete(holder.getAdapterPosition());
            }
        });

        holder.buttonEditIngredient.setOnClickListener(view -> {
            if (onClickListener != null) {
                onClickListener.onEdit(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    /**
     * Method used to set/upload the adapter list.
     *
     * @param ingredients List of Ingredient objects which has to use to set the adapter list.
     */
    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    /**
     * Interface that declares methods for the adapter's items.
     */
    public interface OnClickListener {
        /**
         * Method used to check which item has been clicked and then the method tries to delete the selected item.
         *
         * @param position Integer value that represents the position into the adapter list of the select item.
         */
        void onDelete(int position);

        /**
         * Method used to check which item has been clicked and then the method tries to launch an Intent to modify the data of the selected item.
         *
         * @param position Integer value that represents the position into the adapter list of the selected item.
         */
        void onEdit(int position);
    }

    /**
     * Method used to set the OnClickListener of the adapter.
     *
     * @param onClickListener OnClickListener use to set the one of the adapter.
     */
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}


