package za.co.richfield.smartpantry.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import za.co.richfield.smartpantry.logic.IngredientMatcher;

/**
 * SQLite persistence layer for the Smart Pantry Manager, built on {@link SQLiteOpenHelper}.
 *
 * <p><b>Why SQLite?</b> The pantry and the recipe collection are small, structured and completely
 * personal to one device: there is no need for cloud sync, no multi-device access and no login.
 * SQLite is therefore the simplest choice that satisfies the brief's requirement that data
 * "genuinely persist, not just live in memory". It also works offline, which matters in a kitchen
 * where signal is often poor, and it lets the strict-matching rule be tested with plain SQL
 * queries instead of network calls.</p>
 *
 * <p><b>Schema (three tables, one-to-many):</b></p>
 * <pre>
 *   pantry_item(_id, name, quantity, unit, expiry_date)
 *   recipe(_id, name, category, steps)
 *   recipe_ingredient(_id, recipe_id -> recipe(_id), name, quantity, unit)
 * </pre>
 *
 * <p>All CRUD operations required by the brief live in this class so that no Activity ever writes
 * SQL itself: create (insert), read (queries returning model objects), update and delete.</p>
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    public static final String DATABASE_NAME = "smart_pantry.db";
    /** Bump this and extend {@link #onUpgrade} whenever the schema changes. */
    public static final int DATABASE_VERSION = 1;

    // ---- pantry_item ----------------------------------------------------------------
    public static final String TABLE_PANTRY = "pantry_item";
    public static final String COL_ID = "_id";
    public static final String COL_NAME = "name";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_UNIT = "unit";
    public static final String COL_EXPIRY = "expiry_date";

    // ---- recipe ---------------------------------------------------------------------
    public static final String TABLE_RECIPE = "recipe";
    public static final String COL_CATEGORY = "category";
    public static final String COL_STEPS = "steps";

    // ---- recipe_ingredient ----------------------------------------------------------
    public static final String TABLE_RECIPE_INGREDIENT = "recipe_ingredient";
    public static final String COL_RECIPE_ID = "recipe_id";

    private static final String CREATE_PANTRY =
            "CREATE TABLE " + TABLE_PANTRY + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_NAME + " TEXT NOT NULL, "
                    + COL_QUANTITY + " REAL NOT NULL, "
                    + COL_UNIT + " TEXT NOT NULL, "
                    + COL_EXPIRY + " TEXT);";

    private static final String CREATE_RECIPE =
            "CREATE TABLE " + TABLE_RECIPE + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_NAME + " TEXT NOT NULL UNIQUE, "
                    + COL_CATEGORY + " TEXT, "
                    + COL_STEPS + " TEXT NOT NULL);";

    private static final String CREATE_RECIPE_INGREDIENT =
            "CREATE TABLE " + TABLE_RECIPE_INGREDIENT + " ("
                    + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COL_RECIPE_ID + " INTEGER NOT NULL, "
                    + COL_NAME + " TEXT NOT NULL, "
                    + COL_QUANTITY + " REAL NOT NULL, "
                    + COL_UNIT + " TEXT NOT NULL, "
                    + "FOREIGN KEY (" + COL_RECIPE_ID + ") REFERENCES " + TABLE_RECIPE + "(" + COL_ID + ")"
                    + " ON DELETE CASCADE);";

    private static final String CREATE_INDEX_PANTRY_NAME =
            "CREATE INDEX idx_pantry_name ON " + TABLE_PANTRY + "(" + COL_NAME + ");";
    private static final String CREATE_INDEX_RECIPE_INGREDIENT =
            "CREATE INDEX idx_recipe_ingredient_recipe ON " + TABLE_RECIPE_INGREDIENT + "(" + COL_RECIPE_ID + ");";

    private static DatabaseHelper instance;

    /** Single application-wide instance (a helper is expensive to create repeatedly). */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    /** Visible for tests: allows an in-memory or alternate-name database. */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Needed so that deleting a recipe also deletes its ingredient rows.
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PANTRY);
        db.execSQL(CREATE_RECIPE);
        db.execSQL(CREATE_RECIPE_INGREDIENT);
        db.execSQL(CREATE_INDEX_PANTRY_NAME);
        db.execSQL(CREATE_INDEX_RECIPE_INGREDIENT);
        seedRecipes(db);
        Log.i(TAG, "Database created and seeded with " + SeedData.recipes().size() + " recipes");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For this assignment a simple destructive upgrade is acceptable: the pantry is
        // disposable data and the recipe library is re-seeded straight afterwards.
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                + "; existing tables are recreated.");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    /** Pre-loads the recipe library the first time the app runs (the "seeded on first run" rule). */
    private void seedRecipes(SQLiteDatabase db) {
        for (Recipe recipe : SeedData.recipes()) {
            ContentValues recipeValues = new ContentValues();
            recipeValues.put(COL_NAME, recipe.getName());
            recipeValues.put(COL_CATEGORY, recipe.getCategory());
            recipeValues.put(COL_STEPS, recipe.getSteps());
            long recipeId = db.insert(TABLE_RECIPE, null, recipeValues);
            if (recipeId == -1) {
                Log.e(TAG, "Could not seed recipe " + recipe.getName());
                continue;
            }
            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                ContentValues ingredientValues = new ContentValues();
                ingredientValues.put(COL_RECIPE_ID, recipeId);
                ingredientValues.put(COL_NAME, ingredient.getName());
                ingredientValues.put(COL_QUANTITY, ingredient.getQuantity());
                ingredientValues.put(COL_UNIT, UnitConverter.canonicalUnit(ingredient.getUnit()));
                db.insert(TABLE_RECIPE_INGREDIENT, null, ingredientValues);
            }
        }
    }

    // ===================================================================================
    // CREATE
    // ===================================================================================

    /**
     * Inserts a new pantry item.
     *
     * @return the new row id, or -1 when the insert failed
     */
    public long addIngredient(Ingredient ingredient) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = toValues(ingredient);
        long newId = db.insert(TABLE_PANTRY, null, values);
        Log.d(TAG, "addIngredient(" + ingredient.getName() + ") -> id " + newId);
        return newId;
    }

    // ===================================================================================
    // READ
    // ===================================================================================

    /** @return every pantry item, alphabetically, as model objects. */
    @NonNull
    public List<Ingredient> getAllIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_PANTRY,
                null,
                null,
                null,
                null,
                null,
                COL_NAME + " COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) {
                ingredients.add(fromCursor(cursor));
            }
        }
        return ingredients;
    }

    /** Searches the pantry by (partial) name - used by the search field on the pantry screen. */
    @NonNull
    public List<Ingredient> searchIngredients(String searchTerm) {
        List<Ingredient> ingredients = new ArrayList<>();
        String term = searchTerm == null ? "" : searchTerm.trim();
        if (term.isEmpty()) {
            return getAllIngredients();
        }
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_PANTRY,
                null,
                COL_NAME + " LIKE ?",
                new String[]{"%" + term + "%"},
                null,
                null,
                COL_NAME + " COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) {
                ingredients.add(fromCursor(cursor));
            }
        }
        return ingredients;
    }

    /** @return a single pantry item, or {@code null} when the id no longer exists. */
    public Ingredient getIngredientById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_PANTRY, null, COL_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null)) {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        }
        return null;
    }

    /** @return how many rows are currently stored (shown on the pantry screen). */
    public int getPantryItemCount() {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PANTRY, null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    /** @return every recipe with its ingredient list attached, alphabetically. */
    @NonNull
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_RECIPE, null, null, null, null, null,
                COL_NAME + " COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) {
                Recipe recipe = new Recipe(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_STEPS)));
                loadIngredients(db, recipe);
                recipes.add(recipe);
            }
        }
        return recipes;
    }

    /** @return a single recipe including its ingredients, or {@code null} when it is missing. */
    public Recipe getRecipeById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_RECIPE, null, COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (cursor.moveToFirst()) {
                Recipe recipe = new Recipe(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_STEPS)));
                loadIngredients(db, recipe);
                return recipe;
            }
        }
        return null;
    }

    /** Reads the child rows of one recipe and attaches them to the model object. */
    private void loadIngredients(SQLiteDatabase db, Recipe recipe) {
        try (Cursor cursor = db.query(
                TABLE_RECIPE_INGREDIENT,
                new String[]{COL_NAME, COL_QUANTITY, COL_UNIT},
                COL_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipe.getId())},
                null, null, COL_ID + " ASC")) {
            while (cursor.moveToNext()) {
                recipe.addIngredient(new RecipeIngredient(
                        cursor.getString(0),
                        cursor.getDouble(1),
                        cursor.getString(2)));
            }
        }
    }

    /** @return total number of recipes stored (23 after first run). */
    public int getRecipeCount() {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_RECIPE, null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    /**
     * Every distinct ingredient name used anywhere in the recipe library.
     *
     * <p>Feeds the autocomplete list on the Add/Edit Ingredient form - if the user picks a spelling
     * the recipes already use, the strict-matching rule is far more likely to recognise it.</p>
     */
    @NonNull
    public List<String> getAllRecipeIngredientNames() {
        List<String> names = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT DISTINCT " + COL_NAME + " FROM " + TABLE_RECIPE_INGREDIENT
                        + " ORDER BY " + COL_NAME + " COLLATE NOCASE ASC", null)) {
            while (cursor.moveToNext()) {
                names.add(cursor.getString(0));
            }
        }
        if (names.isEmpty()) {
            // Fallback so the form still offers suggestions if the recipe library is ever empty.
            for (Recipe recipe : SeedData.recipes()) {
                for (RecipeIngredient ingredient : recipe.getIngredients()) {
                    if (!names.contains(ingredient.getName())) {
                        names.add(ingredient.getName());
                    }
                }
            }
        }
        return names;
    }

    // ===================================================================================
    // UPDATE
    // ===================================================================================

    /**
     * Saves changes to an existing pantry item.
     *
     * @return the number of rows updated (1 on success, 0 when the row is gone)
     */
    public int updateIngredient(Ingredient ingredient) {
        if (!ingredient.isSaved()) {
            throw new IllegalArgumentException("Cannot update an ingredient that has no row id");
        }
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.update(TABLE_PANTRY, toValues(ingredient),
                COL_ID + " = ?", new String[]{String.valueOf(ingredient.getId())});
        Log.d(TAG, "updateIngredient(" + ingredient.getName() + ") -> " + rows + " row(s)");
        return rows;
    }

    // ===================================================================================
    // DELETE
    // ===================================================================================

    /**
     * Deletes a pantry item.
     *
     * @return true when a row was actually removed
     */
    public boolean deleteIngredient(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_PANTRY, COL_ID + " = ?", new String[]{String.valueOf(id)});
        Log.d(TAG, "deleteIngredient(" + id + ") -> " + rows + " row(s)");
        return rows > 0;
    }

    /** Empties the pantry (offered on the Settings screen). */
    public void deleteAllIngredients() {
        getWritableDatabase().delete(TABLE_PANTRY, null, null);
    }

    /**
     * Adds a batch of pantry items in one transaction - used by the Settings screen's
     * "load a demo pantry" shortcut so that the strict-matching rule can be demonstrated quickly.
     *
     * @return the number of items inserted
     */
    public int addIngredients(List<Ingredient> ingredients) {
        SQLiteDatabase db = getWritableDatabase();
        int inserted = 0;
        db.beginTransaction();
        try {
            for (Ingredient ingredient : ingredients) {
                if (db.insert(TABLE_PANTRY, null, toValues(ingredient)) != -1) {
                    inserted++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        Log.d(TAG, "addIngredients(batch) -> " + inserted + " row(s)");
        return inserted;
    }

    /**
     * Subtracts the quantities a cooked recipe used from the pantry - the "I cooked this" action on
     * the recipe detail screen. It is the pantry half of the add / remove demonstration the brief
     * asks for in the video.
     *
     * <p>Quantities are converted first (so cooking a recipe that needs 500 g of chicken correctly
     * reduces a pantry holding 1 kg), and a pantry row is deleted once nothing is left of it.</p>
     *
     * @return the number of pantry rows that were updated or deleted
     */
    public int consumeIngredients(Recipe recipe) {
        SQLiteDatabase db = getWritableDatabase();
        List<Ingredient> pantry = getAllIngredients();
        int changed = 0;

        db.beginTransaction();
        try {
            for (RecipeIngredient required : recipe.getIngredients()) {
                Ingredient match = IngredientMatcher.findInPantry(required, pantry);
                if (match == null) {
                    continue;                       // Nothing in the pantry for this requirement.
                }
                double availableInRecipeUnit = UnitConverter.convert(
                        match.getQuantity(), match.getUnit(), required.getUnit());
                double remainingInRecipeUnit = availableInRecipeUnit - required.getQuantity();

                if (remainingInRecipeUnit <= 0.0001d) {
                    db.delete(TABLE_PANTRY, COL_ID + " = ?", new String[]{String.valueOf(match.getId())});
                    changed++;
                } else {
                    double remainingInStoredUnit = UnitConverter.convert(
                            remainingInRecipeUnit, required.getUnit(), match.getUnit());
                    ContentValues values = new ContentValues();
                    values.put(COL_QUANTITY, remainingInStoredUnit);
                    db.update(TABLE_PANTRY, values, COL_ID + " = ?",
                            new String[]{String.valueOf(match.getId())});
                    changed++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        Log.d(TAG, "consumeIngredients(" + recipe.getName() + ") -> " + changed + " row(s) changed");
        return changed;
    }

    // ===================================================================================
    // Mapping helpers
    // ===================================================================================

    private static ContentValues toValues(Ingredient ingredient) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, ingredient.getName());
        values.put(COL_QUANTITY, ingredient.getQuantity());
        values.put(COL_UNIT, UnitConverter.canonicalUnit(ingredient.getUnit()));
        values.put(COL_EXPIRY, ingredient.getExpiryDate());
        return values;
    }

    private static Ingredient fromCursor(Cursor cursor) {
        return new Ingredient(
                cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(COL_QUANTITY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_UNIT)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_EXPIRY)));
    }
}
