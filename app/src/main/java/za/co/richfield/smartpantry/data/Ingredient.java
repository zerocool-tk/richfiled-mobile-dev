package za.co.richfield.smartpantry.data;

/**
 * A single item in the user's pantry (one row of the "pantry_item" table).
 *
 * <p>Plain Java model object. The database layer ({@link DatabaseHelper}) maps Cursor rows
 * onto instances of this class so that the rest of the app never has to touch a Cursor
 * directly.</p>
 */
public class Ingredient {

    /** Primary key of the row. {@link #NO_ID} means "not saved to the database yet". */
    public static final long NO_ID = -1L;

    private long id;
    private String name;
    private double quantity;
    private String unit;
    /** Expiry date in ISO format (yyyy-MM-dd), or null when the user did not supply one. */
    private String expiryDate;

    public Ingredient() {
        this.id = NO_ID;
    }

    public Ingredient(String name, double quantity, String unit, String expiryDate) {
        this.id = NO_ID;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public Ingredient(long id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    /** True when this item has already been persisted and therefore has a row id. */
    public boolean isSaved() {
        return id != NO_ID;
    }

    /** "2.5 kg" style label used by the list adapter and the recipe detail screen. */
    public String getQuantityLabel() {
        return QuantityFormatter.format(quantity, unit);
    }

    @Override
    public String toString() {
        return name + " (" + getQuantityLabel() + ")";
    }
}
