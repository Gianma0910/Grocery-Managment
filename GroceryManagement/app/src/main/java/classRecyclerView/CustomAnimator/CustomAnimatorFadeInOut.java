package classRecyclerView.CustomAnimator;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

/**
 * DefaultItemAnimator subclass used to animate the insertion and deletion from recycler view.
 * The animations that are used in this subclass are fade_in for insertion and fade_out for deletion.
 */
public class CustomAnimatorFadeInOut extends DefaultItemAnimator {

    @Override
    public boolean animateRemove(RecyclerView.ViewHolder holder) {
        Animation animationRemove = AnimationUtils.loadAnimation(holder.itemView.getContext(), android.R.anim.fade_out);
        holder.itemView.startAnimation(animationRemove);
        return super.animateRemove(holder);
    }

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        Animation animationAdd = AnimationUtils.loadAnimation(holder.itemView.getContext(), android.R.anim.fade_in);
        holder.itemView.startAnimation(animationAdd);
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
