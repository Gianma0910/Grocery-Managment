package classRecyclerView.CustomAnimator;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

/**
 * DefaultItemAnimator subclass used to animate the insertion and deletion of element from recycler view.
 * The animations that are used in this subclass are slide_in for insertion and slide_out for deletion.
 */
public class CustomAnimatorSlideInOut extends DefaultItemAnimator {

    @Override
    public boolean animateRemove(RecyclerView.ViewHolder holder) {
        Animation removeAnimation = AnimationUtils.loadAnimation(holder.itemView.getContext(), android.R.anim.slide_out_right);
        holder.itemView.startAnimation(removeAnimation);
        return super.animateRemove(holder);
    }

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        Animation addAnimation = AnimationUtils.loadAnimation(holder.itemView.getContext(), android.R.anim.slide_in_left);
        holder.itemView.startAnimation(addAnimation);
        return super.animateAdd(holder);
    }

    @Override
    public long getAddDuration() {
        return 800;
    }

    @Override
    public long getRemoveDuration() {
        return 800;
    }

    @Override
    public long getChangeDuration() {
        return 800;
    }
}
