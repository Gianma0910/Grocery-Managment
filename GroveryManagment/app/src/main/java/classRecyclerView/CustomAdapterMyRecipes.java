package classRecyclerView;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groverymanagment.R;

/**
 * Adapter that uses a Cursor object that contains all the recipes data stored into the local database.
 * In this adapter its items can be modified or deleted.
 * This adapter must be set fro MyRecipesActivity's RecyclerView.
 */
public class CustomAdapterMyRecipes extends RecyclerView.Adapter<MyRecipesViewHolder> {

    /**
     * Context object that represents where the object class has been instanced.
     */
    Context context;
    /**
     * Cursor object that contains all the recipes data stored into the local database.
     */
    Cursor cursor;
    /**
     * OnClickListener interface used to set up some methods for adapter's item.
     */
    OnClickListener onClickListener;

    /**
     * Integer that represents the position into the adapter of the selected item.
     */
    private int position;

    public CustomAdapterMyRecipes(Context context) {
        this.context = context;
        this.cursor = null;
    }

    @NonNull
    @Override
    public MyRecipesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View v = inflater.inflate(R.layout.card_my_recipe, parent, false);

        return new MyRecipesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyRecipesViewHolder holder, int position) {
        this.cursor.moveToPosition(position);

        holder.idRecipe.setText(cursor.getString(0));
        holder.textViewNameMyRecipe.setText(cursor.getString(1));

        byte[] imageByte = cursor.getBlob(3);

        if (imageByte != null) {
            Bitmap imageToDecode = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);


            holder.imageViewMyRecipe.setImageBitmap(imageToDecode);
        } else {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_error_image);
            holder.imageViewMyRecipe.setImageBitmap(bitmap);
        }

        holder.imageViewMyRecipe.setOnClickListener(view -> {
            if (onClickListener != null) {
                cursor.moveToPosition(position);
                onClickListener.onClickImage(position, cursor.getInt(0));
            }
        });

        holder.buttonEditRecipe.setOnClickListener(view -> {
            if (onClickListener != null) {
                cursor.moveToPosition(position);
                onClickListener.onEdit(position, cursor.getInt(0));
            }
        });

        holder.buttonDeleteRecipe.setOnClickListener(view -> {
            if (onClickListener != null) {
                cursor.moveToPosition(position);
                onClickListener.onDelete(position, cursor.getInt(0));
            }
        });
    }

    @Override
    public int getItemCount() {
        if (cursor == null) {
            return 0;
        }
        return cursor.getCount();
    }

    /**
     * Method to set the position value.
     * @param position Integer value used to set position value.
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Method used to get the position of adapter item.
     * @return
     */
    public int getPosition() {
        return position;
    }

    /**
     * Method used to set/upload the adapter cursor. If it isn't null than close and set/upload it.
     * @param cursor Cursor object which has to use to set the one of the adapter.
     */
    public void setCursor(Cursor cursor) {
        if (this.cursor != null) {
            this.cursor.close();
        }
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    /**
     * Interface that declares method for the adapter's items.
     */
    public interface OnClickListener{
        /**
         * Method used to check which item image has been clicked and then the method tries to launch an Intent to show the recipe details.
         * @param position Integer value that represents the position into the cursor adapter of the image item selected.
         * @param idRecipe Integer value that represents the ID of the image recipe has been selected.
         */
        void onClickImage(int position, int idRecipe);

        /**
         * Method used to check which item has been clicked and then the method tries to launch an Intent to modify the recipe details.
         * @param position Integer value that represents the position into the cursor adapter of the item selected.
         * @param idRecipe Integer value that representes the ID of the recipe selected. This value is used to modify the selected recipe.
         */
        void onEdit(int position, int idRecipe);

        /**
         * Method used to check which item has been clicked and the the method tries to delete the selected item.
         * @param position Integer value that represents the position into the cursor adapter of the item selected.
         * @param idRecipe Integer value used to represents the ID of the recipe selected. This value is used to delete the selected recipe.
         */
        void onDelete(int position, int idRecipe);
    }

    /**
     * Method used to set the OnClickListener of the adapter.
     * @param onClickListener OnClickListener used to set the one of the adapter.
     */
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
