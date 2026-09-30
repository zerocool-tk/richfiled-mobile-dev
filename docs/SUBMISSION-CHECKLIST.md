# Submission checklist

Everything the assignment brief asks for, where it is satisfied in this project, and what still has to
be done **by you** (screenshots, video, report and the GitHub repository itself).

---

## 1. Where each brief requirement is met

| Brief requirement | Where it lives in this project | Status |
|---|---|---|
| Built entirely in Java (not Kotlin) | All 21 main-source files are `.java` | done |
| Minimum 4 distinct screens | 7 screens: Pantry List, Add/Edit Ingredient, Suggested Recipes, Recipe Detail, Almost There, Insights, Settings | done |
| Correct use of Intents to navigate and pass data | `PantryFragment`, `SuggestionsFragment`, `AlmostThereFragment`, `RecipeDetailActivity` (recipe id + ingredient id extras) | done |
| RecyclerView with a custom Adapter | `PantryAdapter` + `RecipeAdapter` (two adapters) | done |
| Working navigation element | `NavigationView` drawer + toolbar + FAB in `MainActivity` | done |
| Input validation on data entry | `ValidationUtils` + `TextInputLayout` errors in `AddEditIngredientActivity` | done |
| Legible mobile layout | `ConstraintLayout` / `LinearLayout` / `CoordinatorLayout` + Material 3 theme (light and dark) | done |
| Pantry add / edit / delete | `DatabaseHelper.addIngredient`, `updateIngredient`, `deleteIngredient` | done |
| Pantry list bound to the database | `PantryFragment` + `PantryAdapter`, reloaded in `onResume()` | done |
| 15-20 recipes pre-loaded on first run | 23 recipes in `SeedData`, seeded by `DatabaseHelper.onCreate()` | done |
| Suggested Recipes screen | `SuggestionsFragment` + `RecipeMatcher.suggestedRecipes()` | done |
| Recipe detail screen | `RecipeDetailActivity` | done |
| Settings / profile screen | `SettingsFragment` (expiry alerts, window, units, bonus toggle, data tools) | done |
| Feedback when zero recipes match | Empty states in `fragment_pantry.xml`, `fragment_suggestions.xml`, `fragment_almost_there.xml` | done |
| Strict-matching rule | `RecipeMatcher.isCookable()` - one missing ingredient excludes the recipe | done |
| Robust to unit and plural differences | `UnitConverter` + `IngredientNameNormalizer` | done |
| Optional "Almost There" bonus list | `AlmostThereFragment` - separate screen, never mixed with strict matches | done |
| No maps / GPS / location | No location permission or mapping dependency anywhere; stated in the manifest comment and README | done |
| Full CRUD with real persistence | `DatabaseHelper` (insert, query, update, delete) on a real SQLite file | done |
| Extra: unit tests | 18 JUnit tests in `app/src/test/.../RecipeMatcherTest.java` | done |
| Extra: diagrams for the report | `docs/diagrams/screen-flow.png`, `docs/diagrams/er-diagram.png` | done |
| GitHub repository with history | **You must create it** - see section 2 | to do |
| 5-7 minute narrated video | **You must record it** - see section 3 | to do |
| Written report with screenshots | **You must write it** - see section 4 | to do |

---

## 2. GitHub repository

1. Create a **public** repository (for example `smart-pantry-manager`) on GitHub.
2. In the project folder:
   ```bash
   git init
   git add .
   git commit -m "Initial commit: project skeleton and readme"
   git branch -M main
   git remote add origin https://github.com/<your-username>/smart-pantry-manager.git
   git push -u origin main
   ```
3. **This part matters for your marks.** The brief states that the commit history is treated as
   evidence of your own development process and is checked against your video: a repository with all
   commits in a single sitting, or with no meaningful history, will be flagged. So do not push
   everything in one commit and stop there. As you work through the app to make it your own - and you
   will need to, to explain it in the video - keep committing in small, honest steps, for example:

   ```
   Add comments to RecipeMatcher explaining the strict rule
   Rename PantryAdapter fields for readability
   Add a "sort by expiry date" option to the pantry list
   Fix empty state text when the search finds nothing
   Add unit test for the tbsp/tsp conversion
   Update README with setup screenshots
   ```

   Each commit message should say what changed and why. Avoid `update`, `final`, or `fix stuff`.
4. Add the lecturer/marker as a collaborator if you make the repository private.

**Be able to explain every part of this code.** The assignment declaration states that work you cannot
explain when questioned is treated as academic dishonesty. Read through the files in the order shown in
the README's project structure section, and make your own changes as you go - that is also the fastest
way to prepare for the video.

---

## 3. Video demonstration (5-7 minutes, narrated)

Suggested shot list, mapped to the brief's required structure:

**Part 1 - GitHub walkthrough (about 1 minute)**
- Open the repository in a browser, scroll through the commits and explain how the app grew
  (data layer -> matching logic -> screens -> polish).

**Part 2 - Live app demonstration (2-3 minutes)**
1. Launch the app and show the empty pantry state.
2. Settings -> *Load a demo pantry*, then Suggested Recipes: "these are the recipes you can cook now".
3. Do a full CRUD cycle on screen: add an ingredient, edit its quantity, delete it, and show the list update.
4. **Prove the strict rule**: delete one ingredient (for example Eggs) and show the affected recipes
   disappear from Suggested Recipes and appear on Almost There; add it back and show them return.
5. Close the app completely (swipe it away from recents) and reopen it: the pantry is still there -
   that proves persistence.

**Part 3 - Concept explanation (2-3 minutes)** - pick three of the five, with your code on screen:
- the Activity/Fragment lifecycle in the app (`onCreate`/`onResume` reloads, `onSaveInstanceState`);
- how SQLite works end to end (`SQLiteOpenHelper` -> `onCreate` -> seeding -> CRUD -> `Cursor` -> model objects);
- how the RecyclerView and custom adapter display data (`onCreateViewHolder`, `onBindViewHolder`, `submitList`);
- how Intents move between screens (recipe id and ingredient id extras);
- how the strict-matching algorithm decides a recipe qualifies (`isCookable`, name normalising, unit conversion).

**Part 4 - Database justification (30-60 seconds)**
- Why SQLite rather than Firebase or PostgreSQL (see section 2 of the README - say it in your own words).

**Technical points:** record at 1080p, narrate throughout (a silent recording is not accepted), export
as MP4/H.264 and keep it small enough to fit the 50 MB ZIP limit together with the code.

---

## 4. Written report

Required sections, in this order:

1. **Cover page** - app name, your name, student number, module name, date.
2. **Table of contents.**
3. **Introduction** - the food-waste problem, who the app is for, what it does.
4. **System design** - paste `docs/diagrams/screen-flow.png` and `docs/diagrams/er-diagram.png`, and
   explain them in a paragraph each.
5. **Screenshots of every output** - take a real screenshot of **every** screen and function:
   empty pantry, pantry with items, add form, **validation error** (submit an invalid quantity),
   edit form, delete confirmation, suggested recipes with several matches, the zero-match empty state,
   almost there list, recipe detail for a cookable recipe, recipe detail for a recipe with a missing
   ingredient, settings, insights, and the pantry still populated after an app restart.
   Caption each one. **Do not use placeholders or mockups** - the brief explicitly penalises them.
6. **Key code snippets (3-5)** - suggested picks:
   - `RecipeMatcher.isCookable()` - the strict rule;
   - `DatabaseHelper.onCreate()` seeding plus `addIngredient`/`updateIngredient`/`deleteIngredient` - CRUD;
   - `PantryAdapter` / `RecipeAdapter` `onBindViewHolder` - adapter binding;
   - `AddEditIngredientActivity.saveIngredient()` - validation before persistence;
   - `IngredientNameNormalizer.normalize()` or `UnitConverter.convert()` - the "messiness" handling.
   Explain *what each does and why it was written that way*.
7. **Challenges and solutions (2-3 real problems)** - if you hit your own, use those. Otherwise some
   honest candidates you can describe after working through the code:
   - treating "tomato" and "tomatoes" as the same ingredient without over-normalising (the first
     version also collapsed "chilli" and "chilli flakes" into one, which wrongly made fresh chilli
     satisfy a recipe needing chilli flakes - fixed with ordered synonym groups);
   - comparing "0.5 kg" with "500 g" (solved by converting both to a base unit through `UnitConverter`);
   - recipes silently failing to match because of a stray word in the name ("fresh chopped tomatoes"),
     solved with a stop-word list in the normaliser;
   - keeping the RecyclerView in step with the database after an edit (solved by reloading in
     `onResume()` instead of caching the list in memory);
   - the bonus "almost there" list accidentally being shown as a suggestion, solved by classifying
     matches as CAN_COOK / ALMOST_THERE / NOT_YET and rendering the two lists from separate screens.
8. **Conclusion and reflection** - what you learned (Activity lifecycle, SQLite, adapters, normalising
   messy user input) and what you would improve (Recipe images, barcode scanning, shopping list export,
   Room instead of raw SQLite, ingredient categories, portion scaling).
9. **Reference list** - `docs/REFERENCES.md` is a starting list in Harvard style; add anything else you
   personally used, and delete what you did not.

**Formatting:** Times New Roman 12 pt, 1.5 line spacing, Harvard referencing, PDF for submission.

---

## 5. Packaging the ZIP

Name it exactly:

```
Studentnumber_Surname_MobileAppDev700_Assignment.zip
```

It must contain:

```
<your-student-number>_<Surname>_MobileAppDev700_Assignment.zip
├── SmartPantryManager/                 the complete project source (exclude build/ and .gradle/)
│   ├── app/src/...
│   ├── gradle/, gradlew, gradlew.bat
│   ├── build.gradle, settings.gradle, gradle.properties, .gitignore
│   └── README.md
├── Studentnumber_Surname_MobileAppDev700_Assignment.docx   (or .pdf) - the written report,
│                                                            containing the working GitHub link
└── Studentnumber_Surname_MobileAppDev700_Demo.mp4          the 5-7 minute video
```

Build the ZIP with the build output excluded, for example:

```bash
cd /path/to/parent
zip -r Studentnumber_Surname_MobileAppDev700_Assignment.zip \
    SmartPantryManager \
    Studentnumber_Surname_MobileAppDev700_Assignment.docx \
    Studentnumber_Surname_MobileAppDev700_Demo.mp4 \
    -x "SmartPantryManager/build/*" \
       "SmartPantryManager/.gradle/*" \
       "SmartPantryManager/app/build/*" \
       "SmartPantryManager/local.properties"
```

Then check the size:

```bash
du -h Studentnumber_Surname_MobileAppDev700_Assignment.zip     # must be under 50 MB
```

If it is too big, re-encode the video at a lower bitrate, for example:

```bash
ffmpeg -i demo.mp4 -vcodec libx264 -crf 28 -preset slow -vf scale=1280:-2 \
       -acodec aac -b:a 96k Studentnumber_Surname_MobileAppDev700_Demo.mp4
```

Finally: submit the single ZIP on Moodle, and tick the declaration of originality and the instruction
checkboxes on the cover page before you sign it.
