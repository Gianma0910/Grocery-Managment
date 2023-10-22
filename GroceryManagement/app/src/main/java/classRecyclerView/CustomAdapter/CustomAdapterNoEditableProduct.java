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

import classRecyclerView.ViewHolder.NoEditableProductViewHolder;

/**
 * Adapter that uses the Cursor object that contains all the products data of a specific shopping list stored into the local database.
 * In this adapter its items can't be modified or deleted.
 * This adapter must be set for NoEditableShoppingListActivity's RecyclerView.
 */
public class CustomAdapterNoEditableProduct extends RecyclerView.Adapter<NoEditableProductViewHolder> {

    /**
     * Context object that represents where the object class has been instanced.
     */
    Context context;
    /**
     * Cursor object that contains all the products data of a specific shopping list stored into the local database.
     */
    Cursor cursor;

    /**
     * CustomAdapterNoEditableProduct constructor.
     * @param context Context object that represents where the object class has been instanced.
     */
    public CustomAdapterNoEditableProduct(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    @NonNull
    @Override
    public NoEditableProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View v = layoutInflater.inflate(R.layout.no_editable_card_product, parent, false);

        return new NoEditableProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NoEditableProductViewHolder holder, int position) {
        this.cursor.moveToPosition(position);

        holder.textViewIdProduct.setText(cursor.getString(0));
        holder.textViewNameProduct.setText(cursor.getString(1));
        holder.textViewAmountProduct.setText(cursor.getString(2));

        int flagProductTaken = cursor.getInt(3);

        holder.checkProductTaken.setChecked(flagProductTaken == 1);

        holder.itemView.startAnimation(AnimationUtils.loadAnimation(
                holder.itemView.getContext(),
                android.R.anim.slide_in_left
        ));
    }

    @Override
    public int getItemCount() {
        if (this.cursor == null) {
            return 0;
        }
        return this.cursor.getCount();
    }
}
