package za.co.richfield.smartpantry.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.List;

import za.co.richfield.smartpantry.R;
import za.co.richfield.smartpantry.data.DatabaseHelper;
import za.co.richfield.smartpantry.data.SettingsManager;
import za.co.richfield.smartpantry.util.DemoPantry;

/**
 * Settings screen: the preferences described in Section 2.2 of the brief (expiring-soon alerts and
 * a units preference), plus pantry maintenance actions that make the app easy to demonstrate.
 *
 * <p>Preferences live in SharedPreferences through {@link SettingsManager}; clearing or seeding the
 * pantry uses {@link DatabaseHelper} so that the screen touches both persistence mechanisms and
 * shows the difference between them.</p>
 */
public class SettingsFragment extends Fragment {

    private SettingsManager settingsManager;
    private DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        settingsManager = new SettingsManager(requireContext());
        databaseHelper = DatabaseHelper.getInstance(requireContext());

        MaterialSwitch switchExpiryAlerts = view.findViewById(R.id.switch_expiry_alerts);
        MaterialSwitch switchAlmostThere = view.findViewById(R.id.switch_almost_there);
        MaterialButtonToggleGroup groupWindow = view.findViewById(R.id.group_expiry_window);
        MaterialAutoCompleteTextView inputUnitSystem = view.findViewById(R.id.input_unit_system);
        MaterialButton buttonLoadDemo = view.findViewById(R.id.button_load_demo);
        MaterialButton buttonClearPantry = view.findViewById(R.id.button_clear_pantry);
        MaterialButton buttonResetSettings = view.findViewById(R.id.button_reset_settings);
        TextView textAbout = view.findViewById(R.id.text_about);

        // ---- expiring soon alerts -------------------------------------------------------
        switchExpiryAlerts.setChecked(settingsManager.isExpiryAlertsEnabled());
        switchExpiryAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setExpiryAlertsEnabled(isChecked);
            Toast.makeText(requireContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show();
        });

        // ---- alert window: 2, 3 or 7 days ------------------------------------------------
        updateWindowSelection(groupWindow, settingsManager.getExpiryWindowDays());
        groupWindow.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            int days = 3;
            if (checkedId == R.id.button_window_2) {
                days = 2;
            } else if (checkedId == R.id.button_window_7) {
                days = 7;
            }
            settingsManager.setExpiryWindowDays(days);
        });

        // ---- bonus list toggle -----------------------------------------------------------
        switchAlmostThere.setChecked(settingsManager.isAlmostThereEnabled());
        switchAlmostThere.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setAlmostThereEnabled(isChecked);
            Toast.makeText(requireContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show();
        });

        // ---- units preference ------------------------------------------------------------
        String[] unitSystems = new String[]{
                SettingsManager.UNIT_SYSTEM_METRIC,
                SettingsManager.UNIT_SYSTEM_ORIGINAL,
                SettingsManager.UNIT_SYSTEM_IMPERIAL};
        inputUnitSystem.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, unitSystems));
        inputUnitSystem.setText(settingsManager.getUnitSystem(), false);
        inputUnitSystem.setOnItemClickListener((parent, v, position, id) -> {
            settingsManager.setUnitSystem(unitSystems[position]);
            Toast.makeText(requireContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show();
        });

        // ---- pantry maintenance -----------------------------------------------------------
        buttonLoadDemo.setOnClickListener(v -> confirmDemoPantry());
        buttonClearPantry.setOnClickListener(v -> confirmClearPantry());
        buttonResetSettings.setOnClickListener(v -> {
            settingsManager.resetToDefaults();
            Toast.makeText(requireContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show();
            // Re-create the view so the controls show the restored defaults.
            switchExpiryAlerts.setChecked(settingsManager.isExpiryAlertsEnabled());
            switchAlmostThere.setChecked(settingsManager.isAlmostThereEnabled());
            updateWindowSelection(groupWindow, settingsManager.getExpiryWindowDays());
            inputUnitSystem.setText(settingsManager.getUnitSystem(), false);
        });

        // ---- about ------------------------------------------------------------------------
        textAbout.setText(getString(R.string.settings_about_summary,
                getString(R.string.app_version_name)) + "\n"
                + getString(R.string.settings_about) + ": "
                + databaseHelper.getRecipeCount() + " recipes, "
                + databaseHelper.getPantryItemCount() + " pantry items stored in SQLite.");
    }

    private void updateWindowSelection(MaterialButtonToggleGroup group, int days) {
        int buttonId = R.id.button_window_3;
        if (days == 2) {
            buttonId = R.id.button_window_2;
        } else if (days == 7) {
            buttonId = R.id.button_window_7;
        }
        group.check(buttonId);
    }

    private void confirmDemoPantry() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_demo_title)
                .setMessage(R.string.dialog_demo_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_load, (dialog, which) -> {
                    int added = databaseHelper.addIngredients(DemoPantry.items());
                    Toast.makeText(requireContext(),
                            getString(R.string.toast_demo_loaded, added), Toast.LENGTH_LONG).show();
                    if (getActivity() instanceof MainActivity) {
                        // Send the user to the list where the new items are visible.
                        ((MainActivity) getActivity()).showPantryScreen();
                    }
                })
                .show();
    }

    private void confirmClearPantry() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_clear_title)
                .setMessage(R.string.dialog_clear_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_confirm_delete, (dialog, which) -> {
                    databaseHelper.deleteAllIngredients();
                    Toast.makeText(requireContext(),
                            R.string.toast_pantry_cleared, Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}
