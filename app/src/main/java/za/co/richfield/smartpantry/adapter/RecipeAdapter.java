package za.co.richfield.smartpantry.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import za.co.richfield.smartpantry.R;
import za.co.richfield.smartpantry.logic.RecipeMatch;

/**
 * Custom RecyclerView adapter for the Suggested Recipes and Almost There screens.
 *
 * <p>One adapter serves both lists because the row layout is identical; only the badge text and
 * badge colour differ, and those are decided from the {@link RecipeMatch.Status} of each item. That
 * is what keeps the strict suggestions visually distinct from the bonus "almost there" recipes.</p>
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    /** Implemented by the hosting Fragment to open the detail screen. */
    public interface RecipeClickListener {
        void onRecipeSelected(RecipeMatch match);
    }

    private final List<RecipeMatch> matches = new ArrayList<>();
    private final RecipeClickListener listener;

    public RecipeAdapter(RecipeClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<RecipeMatch> newMatches) {
        matches.clear();
        if (newMatches != null) {
            matches.addAll(newMatches);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        holder.bind(matches.get(position));
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    class RecipeViewHolder extends RecyclerView.ViewHolder {

        private final TextView textName;
        private final TextView textSummary;
        private final TextView textBadge;
        private final TextView textCategory;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_recipe_name);
            textSummary = itemView.findViewById(R.id.text_recipe_summary);
            textBadge = itemView.findViewById(R.id.text_recipe_badge);
            textCategory = itemView.findViewById(R.id.text_recipe_category);
        }

        void bind(final RecipeMatch match) {
            textName.setText(match.getRecipe().getName());
            textSummary.setText(match.getSummary());
            textCategory.setText(match.getRecipe().getCategory());

            if (match.isCookable()) {
                textBadge.setText(R.string.suggestions_badge);
                textBadge.setBackgroundResource(R.drawable.bg_badge_green);
                textBadge.setTextColor(itemView.getContext().getColor(R.color.green_900));
            } else {
                textBadge.setText(R.string.almost_badge);
                textBadge.setBackgroundResource(R.drawable.bg_badge_amber);
                textBadge.setTextColor(itemView.getContext().getColor(R.color.amber_700));
            }

            itemView.setOnClickListener(v -> listener.onRecipeSelected(match));
        }
    }
}
