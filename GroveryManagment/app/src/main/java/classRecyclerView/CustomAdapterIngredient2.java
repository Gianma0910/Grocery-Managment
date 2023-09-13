package classRecyclerView;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groverymanagment.R;

/**
 * Adapter that uses a Cursor which contains all the ingredient data of a specific recipe stored into the local database.
 * In this adapter its items can be modified or deleted.
 * This adapter must be set for the EditableReipceActivity's RecyclerView.
 */
public class CustomAdapterIngredient2 extends RecyclerView.Adapter<IngredientViewHolder> {


    /**
     * Context object that represents where the object class has been instanced.
     */
    Context context;
    /**
     * Cursor object of the adapter that contain all the ingredient data of a specific recipe stored into the local database.
     */
    Cursor cursor;
    /**
     *  OnClickListener use to set the one of the adapter.
     */
    OnClickListener onClickListener;

    /**
     * CustomAdapterIngredient2 constructor.
     * @param context Context object that represents where the object class has been instanced.
     */
    public CustomAdapterIngredient2(Context context) {
        this.context = context;
        this.cursor = null;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View v = layoutInflater.inflate(R.layout.card_ingredient_add_operation, parent, false);

        return new IngredientViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        this.cursor.moveToPosition(position);

        holder.textViewNameIngredient.setText(cursor.getString(1));
        holder.textViewDoseIngredient.setText(String.valueOf(cursor.getString(2)));

        holder.buttonEditIngredient.setOnClickListener(view -> {
            if (onClickListener != null) {
                cursor.moveToPosition(position);
                onClickListener.onEdit(position, cursor.getInt(0), cursor.getString(1), cursor.getString(2));
            }
        });

        holder.buttonDeleteIngredient.setOnClickListener(view -> {
            if (onClickListener != null) {
                cursor.moveToPosition(position);
                onClickListener.onDelete(position, cursor.getInt(0));
            }
        });
    }

    /**
     * Method used to set the adapter cursor. If the adapter cursor isn't null then close and upload it.
     * @param cursor Cursor object which has to use to set the adapter cursor.
     */
    public void setCursor(Cursor cursor) {
        if (this.cursor != null) {
            this.cursor.close();
        }
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (this.cursor == null) {
            return 0;
        }
        return this.cursor.getCount();
    }

    /**
     * OnClickListener interface that declare methods for the adapter's item.
     */
    public interface OnClickListener{
        /**
         * Method used to check which item has been clicked and then the method tries to delete the selected item.
         * @param position Integer that represents the position into adapter cursor of the selected item.
         * @param idIngredient Integer that represents the ID of the selected item. This value is used to delete the item/ingredient.
         */
        void onDelete(int position, int idIngredient);

        /**
         * Method used to check which item has been clicked and the the method tries to modify the selected item.
         * @param position Integer that represents the position into adapter cursor of the selected item.
         * @param idIngredient Integer that represents the ID of the selected item. This value is used to modify the item.
         * @param nameIngredient String that represents the name of the selected item to modify.
         * @param doseIngredient String that represents the dose of the selected item to modify.
         */
        void onEdit(int position, int idIngredient, String nameIngredient, String doseIngredient);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
