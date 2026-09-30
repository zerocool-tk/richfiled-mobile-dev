package za.co.richfield.smartpantry.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import android.view.MenuItem;
import android.view.View;

import za.co.richfield.smartpantry.R;

/**
 * The single host Activity of the app.
 *
 * <p>It owns the navigation drawer, the toolbar and the container that the four main screens are
 * loaded into as Fragments. Only the two form-style screens (adding or editing an ingredient, and
 * viewing a recipe) are separate Activities, because they are pushed on top of the current screen
 * and need their own back behaviour - that is exactly the navigation pattern the module covers.</p>
 *
 * <p>Screens hosted here:</p>
 * <ol>
 *     <li>Pantry list (with search, edit and delete)</li>
 *     <li>Suggested recipes (strict matching only)</li>
 *     <li>Almost there (bonus, one ingredient short)</li>
 *     <li>Insights (counts and progress)</li>
 *     <li>Settings</li>
 * </ol>
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FloatingActionButton fabAddIngredient;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        fabAddIngredient = findViewById(R.id.fab_add_ingredient);

        // The drawer toggle wires the hamburger icon to the drawer and keeps the icon in sync.
        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_open_drawer, R.string.nav_open_drawer);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        fabAddIngredient.setOnClickListener(v -> openAddIngredientForm());
        registerBackPressHandler();

        if (savedInstanceState == null) {
            // First launch: open the pantry screen with the drawer item already ticked.
            navigationView.setCheckedItem(R.id.nav_pantry);
            showFragment(new PantryFragment(), getString(R.string.title_pantry));
            setTitle(getString(R.string.title_pantry));
        } else {
            // After a configuration change the FragmentManager restores the screen that was showing.
            updateFabVisibilityForCurrentScreen();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_pantry) {
            showFragment(new PantryFragment(), getString(R.string.title_pantry));
        } else if (itemId == R.id.nav_suggestions) {
            showFragment(new SuggestionsFragment(), getString(R.string.title_suggestions));
        } else if (itemId == R.id.nav_almost_there) {
            showFragment(new AlmostThereFragment(), getString(R.string.title_almost_there));
        } else if (itemId == R.id.nav_insights) {
            showFragment(new InsightsFragment(), getString(R.string.title_insights));
        } else if (itemId == R.id.nav_settings) {
            showFragment(new SettingsFragment(), getString(R.string.title_settings));
        } else {
            return false;
        }

        setTitle(item.getTitle());
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Replaces the container content with the given Fragment.
     *
     * <p>{@code replace} is used rather than {@code add} so that only one screen is ever visible and
     * each screen reloads fresh data from the database when it is opened - which is how a change made
     * on one screen shows up immediately on another.</p>
     */
    private void showFragment(Fragment fragment, String title) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, fragment.getClass().getSimpleName());
        transaction.commit();
        setTitle(title);
        // The FAB only makes sense on the pantry screen.
        fabAddIngredient.setVisibility(
                fragment instanceof PantryFragment ? View.VISIBLE : View.GONE);
    }

    /** Keeps the FAB consistent with whatever Fragment the FragmentManager restored. */
    private void updateFabVisibilityForCurrentScreen() {
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        fabAddIngredient.setVisibility(current instanceof PantryFragment ? View.VISIBLE : View.GONE);
    }

    /** Opens the Add Ingredient form - used by the FAB and by the empty-state buttons. */
    public void openAddIngredientForm() {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        startActivity(intent);
    }

    /** Jumps to the pantry screen from another Fragment (for example from an empty state). */
    public void showPantryScreen() {
        navigationView.setCheckedItem(R.id.nav_pantry);
        showFragment(new PantryFragment(), getString(R.string.title_pantry));
    }

    /** Jumps to the suggestions screen from another Fragment. */
    public void showSuggestionsScreen() {
        navigationView.setCheckedItem(R.id.nav_suggestions);
        showFragment(new SuggestionsFragment(), getString(R.string.title_suggestions));
    }

    /**
     * Standard drawer behaviour: the system back gesture closes an open drawer first and only then
     * leaves the screen. It uses the {@link OnBackPressedCallback} API because
     * {@code onBackPressed()} is deprecated from API 33 onwards and would otherwise raise a build
     * warning.
     */
    private void registerBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    // Hand the event back to the system so the Activity actually finishes.
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        });
    }
}
