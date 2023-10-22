package classRecyclerView.CustomAdapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.grocerymanagement.R;

import classRecyclerView.ViewHolder.NoEditableIngredientViewHolder;

/**
 * Adapter that uses a Cursor object that contains all the ingredients data of a specific recipe stored into the local database.
 * In this adapter its items can't be modified or deleted.
 * This adapter must be set for NoEditableRecipeActivity's GUI.
 */
public class CustomAdapterNoEditableIngredient extends RecyclerView.Adapter<NoEditableIngredientViewHolder> {

    /**
     * Context object that represents where the object class has been instanced.
     */
    Context context;
    /**
     * Cursor object that contains all the ingredients data of a specific recipe.
     */
    Cursor cur;

    /**
     * CustomAdapterNoEditableIngredient constructor.
     * @param context Context object that represents where the object class has bene instanced.
     * @param cur Cursor object that contains all the ingredients data of a specific recipe.
     */
    public CustomAdapterNoEditableIngredient(Context context, Cursor cur) {
        this.context = context;
        this.cur = cur;
    }

    @NonNull
    @Override
    public NoEditableIngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View v = layoutInflater.inflate(R.layout.no_editable_card_ingredient, parent, false);

        return new NoEditableIngredientViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NoEditableIngredientViewHolder holder, int position) {
        this.cur.moveToPosition(position);

        holder.textViewNameIngredient2.setText(cur.getString(1));
        holder.textViewDoseIngredient2.setText(cur.getString(2));

        holder.itemView.startAnimation(AnimationUtils.loadAnimation(
                holder.itemView.getContext(),
                android.R.anim.slide_in_left
        ));
    }

    @Override
    public int getItemCount() {
        if (this.cur == null) {
            return 0;
        }
        return cur.getCount();
    }
}
