# Smart Pantry Manager

**A Java Android application that suggests recipes based strictly on the ingredients a user already has at home, to help cut household food waste.**

Mobile App Development 700 - Practical Assignment
Richfield Graduate Institute of Technology - Faculty of Information Technology

---

## 1. What the app does

The Smart Pantry Manager keeps a record of the ingredients a user actually has in their kitchen (the
"pantry") and suggests recipes they can cook **right now**, with no shopping trip required.

The whole app is built around one rule, and it is the most important piece of business logic in the
project:

> **A recipe may only be listed as suggested if every single ingredient it requires is currently in
> the user's pantry, in at least the required quantity.**

If a recipe needs five ingredients and the pantry holds four of them, that recipe is **not** shown in
the suggestions. Partial matches never appear there. Recipes that are exactly one ingredient short are
listed separately on a bonus **Almost There** screen, so they can be acted on without ever being
confused with a genuine match.

### Screens

| # | Screen | Type | Purpose |
|---|--------|------|---------|
| 1 | **My Pantry** | Fragment | Lists every pantry item in a RecyclerView, with live search, edit and delete |
| 2 | **Add / Edit Ingredient** | Activity | Form with full input validation; inserts or updates the SQLite row |
| 3 | **Suggested Recipes** | Fragment | Runs the strict-matching rule and lists only fully cookable recipes |
| 4 | **Recipe Detail** | Activity | Full ingredient checklist, method and live availability status |
| 5 | **Almost There** (bonus) | Fragment | Recipes missing exactly one ingredient, kept separate from the strict list |
| 6 | **Insights** | Fragment | Pantry, recipe and "can you cook it" counters |
| 7 | **Settings** | Fragment | Expiring-soon alerts, alert window, unit preference, demo pantry, clear pantry |

### Features

- **Pantry management** - add, edit and delete ingredients (name, quantity, unit, optional expiry date).
- **23 seeded recipes** - each with a category, a full ingredient list with quantities and units, and
  numbered preparation steps. Seeded into the database on first run.
- **Strict matching** - see above.
- **Robust matching** - `Tomatoes` matches `tomato`, `Juice` matches `juice`, `0.5 kg` of chicken
  satisfies a recipe that asks for `500 g`, and `7 tsp` of curry powder satisfies `2 tbsp`.
- **Expiring-soon alerts** - pantry items inside the alert window are flagged with an amber warning icon.
- **"I cooked this"** - subtracts the quantities a recipe uses from the pantry, so the pantry stays accurate.
- **Empty and error states** - every list explains itself rather than showing a blank screen.
- **Navigation drawer, toolbar and FAB** - consistent mobile navigation throughout.
- **Unit tests** - 18 JUnit tests covering the matching rule, unit conversion and name normalising.

### Explicitly out of scope

Following the brief, there is **no Google Maps, no mapping SDK and no GPS or location permission**
anywhere in this project. There is also no payment processing. The manifest requests no dangerous
permissions at all.

---

## 2. Database choice: SQLite (SQLiteOpenHelper)

**I chose SQLite, implemented locally on the device with `SQLiteOpenHelper`.**

The reasons, in the order that mattered most:

1. **The data is small, structured and personal.** A pantry holds dozens of rows, not millions. Its
   shape is fixed and relational (a recipe has many ingredients), which suits tables far better than a
   document store.
2. **No cloud, account or network is needed.** There is no login, no multi-device sync and no shared
   data, so Firebase or PostgreSQL would add a dependency and a failure mode (poor Wi-Fi in a kitchen)
   without solving a real problem.
3. **It satisfies the persistence requirement completely.** The brief requires that data "genuinely
   persist, not just live in memory" and survive the app being closed and reopened. SQLite files live
   in the app's private storage, so the pantry is still there after a restart, a force stop or a device
   reboot - no server uptime to worry about.
4. **It keeps the strict-matching rule testable.** Recipes and pantry items can be read in a couple of
   fast local queries, so the matching algorithm can be unit-tested without a network stub or emulator
   (see `app/src/test/.../RecipeMatcherTest.java`).
5. **It matches the module content.** The persistent-data chapter of the module uses
   `SQLiteOpenHelper` with a helper class and a data source, which is exactly the structure used here.

`SharedPreferences` is used for the handful of user preferences on the Settings screen, because those
are single key/value values with no relationships. Keeping them out of the relational schema means the
schema only models things that genuinely have relationships.

### Schema

```
pantry_item(_id PK, name, quantity, unit, expiry_date)          -- user-owned data (full CRUD)

recipe(_id PK, name UNIQUE, category, steps)                    -- reference data, seeded once
recipe_ingredient(_id PK, recipe_id FK -> recipe(_id),          -- one row per recipe requirement
                  name, quantity, unit)
```

- `recipe_ingredient.recipe_id` is a foreign key with `ON DELETE CASCADE`, and foreign keys are
  enabled in `DatabaseHelper.onConfigure()`.
- `pantry_item` is indexed on `name` and `recipe_ingredient` on `recipe_id`, the columns the app
  queries most often.
- There is deliberately **no** foreign key between the pantry and the recipes: a recipe *requirement*
  is not the same thing as an *owned* ingredient, so they are modelled as two separate concepts that
  are compared in Java by the matching logic.

Full diagrams: [`docs/diagrams/er-diagram.png`](docs/diagrams/er-diagram.png) (data model) and
[`docs/diagrams/screen-flow.png`](docs/diagrams/screen-flow.png) (screen flow).

---

## 3. Project structure

```
app/src/main/java/za/co/richfield/smartpantry/
├── PantryApp.java                     Application class (context holder)
├── data/
│   ├── DatabaseHelper.java            SQLiteOpenHelper: schema, seeding, full CRUD
│   ├── Ingredient.java                Pantry item model
│   ├── Recipe.java / RecipeIngredient.java
│   ├── SeedData.java                  The 23 seeded recipes
│   ├── SettingsManager.java           SharedPreferences wrapper
│   ├── UnitConverter.java             g/kg, ml/l, tsp/tbsp, countable units -> base units
│   ├── IngredientNameNormalizer.java  plural handling, filler words, synonyms
│   └── QuantityFormatter.java         "2.5 kg" style labels
├── logic/
│   ├── RecipeMatcher.java             THE STRICT MATCHING RULE (isCookable / matchAll)
│   ├── IngredientMatcher.java         Compares one requirement with the pantry
│   └── RecipeMatch.java               Match result + status (CAN_COOK / ALMOST_THERE / NOT_YET)
├── adapter/
│   ├── PantryAdapter.java             RecyclerView adapter for pantry rows
│   └── RecipeAdapter.java             RecyclerView adapter for recipe rows
├── ui/
│   ├── MainActivity.java              Host Activity: drawer, toolbar, FAB, Fragments
│   ├── PantryFragment.java            Screen 1
│   ├── AddEditIngredientActivity.java Screen 2 (create / update / delete + validation)
│   ├── SuggestionsFragment.java       Screen 3 (strict suggestions)
│   ├── RecipeDetailActivity.java      Screen 4
│   ├── AlmostThereFragment.java       Screen 5 (bonus)
│   ├── InsightsFragment.java          Screen 6
│   └── SettingsFragment.java          Screen 7
└── util/
    ├── ValidationUtils.java           Every form validation rule in one place
    ├── DateUtils.java                 Expiry date parsing and "days until"
    └── DemoPantry.java                One-tap demo pantry for quick testing
```

### The matching rule, in one place

`logic/RecipeMatcher.java`:

```java
public static boolean isCookable(Recipe recipe, List<Ingredient> pantry) {
    if (pantry == null || pantry.isEmpty()) {
        return false;                       // Nothing in the pantry means nothing can be cooked.
    }
    for (RecipeIngredient required : recipe.getIngredients()) {
        if (!IngredientMatcher.compare(required, pantry).satisfied) {
            return false;                   // One short ingredient is enough to exclude the recipe.
        }
    }
    return true;
}
```

---

## 4. Setup and run instructions

### Requirements

| Tool | Version used | Notes |
|------|--------------|-------|
| Android Studio | Koala (2024.1.1) or newer | AGP 8.5.2 requires it; see the note below |
| JDK | 17 | Bundled with recent Android Studio |
| Gradle | 8.7 | Downloaded automatically by the Gradle wrapper |
| Android SDK | API 34 (`compileSdk` / `targetSdk` 34) | Install via the SDK Manager |
| Minimum device | API 24 (Android 7.0) | `minSdk 24` |

### Steps

1. **Install the SDK platform if needed.** In Android Studio: *Tools -> SDK Manager -> SDK Platforms*,
   tick **Android 14.0 (API 34)**, and on the *SDK Tools* tab make sure **Android SDK Build-Tools 34**
   and **Android SDK Platform-Tools** are installed.
2. **Open the project.** Android Studio -> *File -> Open* -> select the project **root** folder (the one
   containing `settings.gradle` and `app/`). Do not open the `app/` folder on its own.
3. **Let Gradle sync.** The first sync downloads Gradle 8.7 and the AndroidX libraries, so it needs an
   internet connection. `local.properties` (with your `sdk.dir`) is generated automatically - it is in
   `.gitignore` and must not be committed.
4. **Run the app.** Choose an emulator or a connected device and press **Run** (Shift+F10), or:
   ```bash
   ./gradlew installDebug
   ```
5. **Try it in 30 seconds.** Open the navigation drawer -> **Settings** -> **Load a demo pantry**.
   Twelve common ingredients are added, then the **Suggested Recipes** screen immediately shows which
   recipes are cookable.
6. **Run the unit tests** (no emulator required):
   ```bash
   ./gradlew test
   ```
   The HTML report is written to `app/build/reports/tests/testDebugUnitTest/index.html`.

### Using an older Android Studio?

`build.gradle` pins AGP 8.5.2, which needs Android Studio 2024.1.1 or newer and JDK 17. If your
Android Studio is older, change that single line to a version it supports, for example:

```groovy
id 'com.android.application' version '8.2.2' apply false
```

Everything else in the project is compatible with AGP 8.0+.

### Demonstrating the strict rule (useful for the video)

1. Load the demo pantry, then open **Suggested Recipes** and note the count.
2. Go to **My Pantry** and delete **Eggs** (or **Salt**).
3. Return to **Suggested Recipes**: the recipes that needed that ingredient have disappeared.
4. Open **Almost There**: those recipes now appear there, each showing the one ingredient that is missing.
5. Add the ingredient back: they return to the strict suggestions list immediately.

---

## 5. Testing

`app/src/test/java/za/co/richfield/smartpantry/logic/RecipeMatcherTest.java` contains 18 tests written
against the matching rules, including:

- a recipe with four of its five ingredients present is **not** suggested, and is reported as
  "almost there" instead;
- adding the missing ingredient makes it appear, and removing one makes it disappear again;
- a quantity that is too small does not satisfy a requirement (1 egg cannot cover 3 eggs);
- `0.6 kg` satisfies `500 g`, `7 tsp` satisfies `2 tbsp`, and `1 l` satisfies `0.5 l`;
- `Tomatoes` / `tomato` / `Fresh Chopped Tomatoes` all match, while `chilli` does **not** match
  `chilli flakes`;
- an empty or `null` pantry suggests nothing and does not crash;
- duplicate pantry rows are resolved in favour of the best match.

Because the matching logic is plain Java with no Android imports, it runs on the development machine in
seconds rather than needing an emulator.

---

## 6. References

See `docs/REFERENCES.md` for the documentation and tutorial sources consulted while building this app.

---

## 7. Author

**Name:** [Your full name]
**Student number:** [Your ITS number]
**Module:** Mobile App Development 700
**Qualification:** [Your qualification]
**Year / Semester:** [Year] / [Semester]
**GitHub repository:** [repository link]

Assignment submitted for assessment in accordance with the Richfield Graduate Institute of Technology
academic integrity policy.
