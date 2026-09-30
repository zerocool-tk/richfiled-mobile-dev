package za.co.richfield.smartpantry.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import za.co.richfield.smartpantry.R;
import za.co.richfield.smartpantry.data.Ingredient;
import za.co.richfield.smartpantry.util.DateUtils;

/**
 * Custom RecyclerView adapter that binds a list of {@link Ingredient} objects - read from SQLite -
 * to the rows on the pantry screen.
 *
 * <p>The adapter is also where the expiry logic becomes visible to the user: when expiring-soon
 * alerts are enabled in Settings, an item that expires inside the alert window gets an amber warning
 * icon and a coloured expiry line.</p>
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /** Callbacks the hosting Fragment implements so the adapter stays free of Activity code. */
    public interface PantryItemListener {
        void onEditIngredient(Ingredient ingredient);

        void onDeleteIngredient(Ingredient ingredient);
    }

    private final List<Ingredient> items = new ArrayList<>();
    private final PantryItemListener listener;
    private final boolean expiryAlertsEnabled;
    private final int expiryWindowDays;

    public PantryAdapter(PantryItemListener listener, boolean expiryAlertsEnabled, int expiryWindowDays) {
        this.listener = listener;
        this.expiryAlertsEnabled = expiryAlertsEnabled;
        this.expiryWindowDays = expiryWindowDays;
    }

    /**
     * Replaces the data set and refreshes the list.
     *
     * <p>Called every time the Fragment reloads from the database, which is how a Create, Update or
     * Delete on another screen is reflected here.</p>
     */
    public void submitList(List<Ingredient> ingredients) {
        items.clear();
        if (ingredients != null) {
            items.addAll(ingredients);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Holds the views of one row so they are only looked up once per row. */
    class PantryViewHolder extends RecyclerView.ViewHolder {

        private final TextView textName;
        private final TextView textQuantity;
        private final TextView textExpiry;
        private final ImageView imageExpiryWarning;
        private final MaterialButton buttonEdit;
        private final MaterialButton buttonDelete;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_item_name);
            textQuantity = itemView.findViewById(R.id.text_item_quantity);
            textExpiry = itemView.findViewById(R.id.text_item_expiry);
            imageExpiryWarning = itemView.findViewById(R.id.image_expiry_warning);
            buttonEdit = itemView.findViewById(R.id.button_edit_item);
            buttonDelete = itemView.findViewById(R.id.button_delete_item);
        }

        void bind(final Ingredient ingredient) {
            textName.setText(ingredient.getName());
            textQuantity.setText(ingredient.getQuantityLabel());

            int daysUntilExpiry = DateUtils.daysUntil(ingredient.getExpiryDate());
            boolean hasExpiry = ingredient.getExpiryDate() != null
                    && !ingredient.getExpiryDate().trim().isEmpty();
            boolean expiringSoon = expiryAlertsEnabled && DateUtils
                    .isExpiringWithin(ingredient.getExpiryDate(), expiryWindowDays);

            if (!hasExpiry) {
                textExpiry.setVisibility(View.GONE);
                imageExpiryWarning.setVisibility(View.GONE);
            } else {
                textExpiry.setVisibility(View.VISIBLE);
                String label;
                int colour;
                if (DateUtils.isExpired(ingredient.getExpiryDate())) {
                    label = itemView.getContext().getString(R.string.expired) + " ("
                            + DateUtils.toDisplayFormat(ingredient.getExpiryDate()) + ")";
                    colour = itemView.getContext().getColor(R.color.red_700);
                } else if (expiringSoon) {
                    label = itemView.getContext()
                            .getString(R.string.expiring_soon, daysUntilExpiry)
                            + " - " + DateUtils.toDisplayFormat(ingredient.getExpiryDate());
                    colour = itemView.getContext().getColor(R.color.amber_700);
                } else {
                    label = itemView.getContext()
                            .getString(R.string.expiry_date_value,
                                    DateUtils.toDisplayFormat(ingredient.getExpiryDate()));
                    colour = itemView.getContext().getColor(R.color.grey_700);
                }
                textExpiry.setText(label);
                textExpiry.setTextColor(colour);
                imageExpiryWarning.setVisibility(expiringSoon ? View.VISIBLE : View.GONE);
            }

            buttonEdit.setOnClickListener(v -> listener.onEditIngredient(ingredient));
            buttonDelete.setOnClickListener(v -> listener.onDeleteIngredient(ingredient));
            itemView.setOnClickListener(v -> listener.onEditIngredient(ingredient));
        }
    }
}
