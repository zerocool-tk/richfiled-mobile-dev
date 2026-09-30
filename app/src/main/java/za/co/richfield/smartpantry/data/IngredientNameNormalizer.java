package za.co.richfield.smartpantry.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Normalises ingredient names so that the strict-matching rule survives the "simple real-world
 * messiness" the brief asks for, without pretending to be a natural language processor.
 *
 * <p>Three steps are applied to every name, for pantry items and recipe requirements alike:</p>
 * <ol>
 *     <li><b>Clean up</b> - lower case, trim, drop punctuation and collapse repeated spaces, and
 *         remove filler words such as "fresh" or "chopped".</li>
 *     <li><b>De-pluralise</b> - each word is reduced to a simple singular form, so "Tomatoes" and
 *         "tomato" produce the same key ("tomato"), and "eggs" matches "egg".</li>
 *     <li><b>Synonyms</b> - well known aliases are mapped onto one canonical name, for example
 *         "chili" and "chilli" both become "chilli", and "aubergine" becomes "brinjal".</li>
 * </ol>
 *
 * <p>The result is a comparison key such as {@code "tomato"} or {@code "mixed vegetable"}. The key
 * is used for matching only - the user always sees the name they typed.</p>
 */
public final class IngredientNameNormalizer {

    /** Filler words that never change which ingredient is meant. */
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "fresh", "freshly", "chopped", "sliced", "diced", "minced", "grated", "ground",
            "peeled", "crushed", "whole", "large", "small", "medium", "ripe", "raw", "cooked",
            "of", "a", "an", "the", "some", "any", "powdered", "dried", "frozen", "tinned",
            "canned", "plain", "extra", "virgin", "cold", "warm", "hot"));

    /** Alias groups: every spelling in a group normalises to the first (canonical) spelling. */
    private static final List<String[]> SYNONYM_GROUPS = Arrays.asList(
            new String[]{"chilli", "chili", "chile", "chillies", "fresh chilli"},
            // Note: chilli flakes are a DIFFERENT product from fresh chilli, so they get their own
            // group - mapping them together would wrongly let a fresh chilli satisfy a recipe that
            // asks for chilli flakes.
            new String[]{"chilli flakes", "chili flakes", "red chilli flakes", "chilli powder"},
            new String[]{"brinjal", "aubergine", "eggplant"},
            new String[]{"coriander", "cilantro"},
            new String[]{"maize meal", "pap", "mielie meal", "corn meal", "polenta"},
            new String[]{"baked bean", "baked beans", "tinned baked beans", "canned baked beans"},
            new String[]{"mixed vegetable", "mixed veg", "frozen mixed vegetables", "stir fry vegetables"},
            new String[]{"beef stock", "beef stock cube", "beef broth", "beef stock powder"},
            new String[]{"vegetable stock", "veg stock", "vegetable broth", "vegetable stock cube"},
            new String[]{"chicken stock", "chicken broth", "chicken stock cube"},
            new String[]{"mayonnaise", "mayo"},
            new String[]{"vegetable oil", "cooking oil", "sunflower oil", "oil"},
            new String[]{"olive oil", "extra virgin olive oil"},
            new String[]{"pasta", "macaroni", "penne", "fusilli", "shells", "macaroni pasta"},
            new String[]{"spaghetti", "spagetti"},
            new String[]{"potato", "potatoes", "baby potato", "baby potatoes"},
            new String[]{"tomato", "tomatoes", "roma tomato", "cherry tomato", "cherry tomatoes"},
            new String[]{"onion", "onions", "red onion", "white onion", "brown onion"},
            new String[]{"garlic", "garlic clove", "garlic cloves"},
            new String[]{"butternut", "butternut squash", "butternut pumpkin"},
            new String[]{"lentil", "lentils", "dhall", "dhal", "dal", "split peas"},
            new String[]{"carrot", "carrots"},
            new String[]{"pea", "peas", "green peas", "frozen peas"},
            new String[]{"egg", "eggs", "chicken egg", "chicken eggs"},
            new String[]{"rice", "white rice", "long grain rice", "basmati rice"},
            new String[]{"bread", "bread slice", "bread slices", "white bread", "brown bread", "toast"},
            new String[]{"cheese", "cheddar", "cheddar cheese", "grated cheese", "gouda"},
            new String[]{"milk", "full cream milk", "low fat milk", "fresh milk"},
            new String[]{"cream", "fresh cream", "cooking cream", "whipping cream"},
            new String[]{"butter", "margarine", "salted butter", "unsalted butter"},
            new String[]{"chicken", "chicken breast", "chicken fillet", "chicken pieces", "chicken breast fillet"},
            new String[]{"beef", "beef mince", "minced beef", "ground beef", "beef stewing meat", "stewing beef"},
            new String[]{"tuna", "tuna fish", "tinned tuna", "canned tuna"},
            new String[]{"flour", "cake flour", "white flour", "all purpose flour", "wheat flour"},
            new String[]{"salt", "table salt", "sea salt", "cooking salt"},
            new String[]{"black pepper", "pepper", "ground black pepper", "peppercorns"},
            new String[]{"curry powder", "curry masala", "masala", "rajah curry powder"},
            new String[]{"soy sauce", "soya sauce", "light soy sauce"},
            new String[]{"sugar", "white sugar", "castor sugar", "caster sugar", "brown sugar"});

    /** Fully prepared alias lookup: already-normalised alias -> canonical comparison key. */
    private static final Map<String, String> SYNONYMS = new HashMap<>();

    static {
        for (String[] group : SYNONYM_GROUPS) {
            // The first entry is the canonical name; normalise it without the synonym lookup so we
            // do not create a circular reference.
            String canonicalKey = baseNormalize(group[0]);
            SYNONYMS.put(canonicalKey, canonicalKey);
            for (int i = 1; i < group.length; i++) {
                // putIfAbsent keeps the table order-safe: an earlier group always wins, so a later
                // group can never re-point an existing key at a different ingredient.
                SYNONYMS.putIfAbsent(baseNormalize(group[i]), canonicalKey);
            }
        }
    }

    private IngredientNameNormalizer() {
        // Utility class - never instantiated.
    }

    /**
     * @param rawName whatever the user typed ("2 Fresh Tomatoes") or the recipe stores ("Tomato")
     * @return the comparison key used by the matching algorithm ("tomato")
     */
    public static String normalize(String rawName) {
        if (rawName == null) {
            return "";
        }
        String base = baseNormalize(rawName);
        String mapped = SYNONYMS.get(base);
        return mapped != null ? mapped : base;
    }

    /** Clean-up and de-pluralisation only, without the synonym step. */
    private static String baseNormalize(String rawName) {
        String cleaned = rawName.toLowerCase(Locale.US)
                .replaceAll("[^a-z0-9\\s]", " ")   // drop punctuation such as commas and brackets
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        StringBuilder key = new StringBuilder();
        for (String word : cleaned.split(" ")) {
            if (STOP_WORDS.contains(word)) {
                continue;
            }
            String singular = singularize(word);
            if (!singular.isEmpty()) {
                if (key.length() > 0) {
                    key.append(' ');
                }
                key.append(singular);
            }
        }
        // If every word was a stop word (for example the user literally typed "fresh"),
        // fall back to the cleaned input so the item is still usable.
        return key.length() > 0 ? key.toString() : cleaned;
    }

    /**
     * Very light singularisation. It deliberately only touches the endings that actually cause
     * false negatives in a pantry app, so that words like "salt", "oil" and "peas" all behave:
     * <ul>
     *     <li>"tomatoes" -> "tomato", "potatoes" -> "potato"</li>
     *     <li>"eggs" -> "egg", "carrots" -> "carrot"</li>
     *     <li>"loaves" -> "loaf", "knives" -> "knife"</li>
     *     <li>words ending in "ss"/"us"/"is" are left alone ("watercress", "couscous")</li>
     * </ul>
     */
    static String singularize(String word) {
        if (word.length() <= 3) {
            // Too short to strip safely ("gas", "rice", "oil" must stay intact).
            return word;
        }
        if (word.endsWith("ies") && word.length() > 4) {
            return word.substring(0, word.length() - 3) + "y";      // berries -> berry
        }
        if (word.endsWith("ves")) {
            return word.substring(0, word.length() - 3) + "f";      // loaves -> loaf
        }
        if (word.endsWith("oes")) {
            return word.substring(0, word.length() - 2);            // tomatoes -> tomato
        }
        if (word.endsWith("ss") || word.endsWith("us") || word.endsWith("is")) {
            return word;                                            // watercress, couscous, basis
        }
        if (word.endsWith("s")) {
            return word.substring(0, word.length() - 1);            // eggs -> egg, onions -> onion
        }
        return word;
    }

    /** True when two ingredient names refer to the same pantry ingredient. */
    public static boolean sameIngredient(String nameA, String nameB) {
        String keyA = normalize(nameA);
        return !keyA.isEmpty() && keyA.equals(normalize(nameB));
    }
}
