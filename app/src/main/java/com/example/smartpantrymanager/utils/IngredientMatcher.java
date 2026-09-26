package com.example.smartpantrymanager.utils;

import com.example.smartpantrymanager.data.PantryItem;
import com.example.smartpantrymanager.data.RecipeIngredient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Decides whether a recipe can be made with what's in the pantry. This is the
 * app's most important rule: a recipe is only suggested if the pantry has EVERY
 * ingredient it needs, in AT LEAST the amount it needs. Having most of them isn't
 * enough.
 *
 * Comparing the pantry to a recipe isn't as simple as checking names match,
 * because people write the same thing in different ways. This class deals with
 * two of those:
 *  1. Names. "Tomato", "tomatoes" and " TOMATO " should all count as the same
 *     ingredient, so names are cleaned up before comparing.
 *  2. Units. 2 kg of flour should cover a recipe that needs 500 g, so amounts
 *     are converted to a shared unit before comparing.
 *
 * This code is kept separate from the screens so it can be tested on its own
 * (see IngredientMatcherTest).
 */
public final class IngredientMatcher {

    // Only holds helper methods, so nobody needs to create one.
    private IngredientMatcher() {
    }

    // Different names for the same ingredient, each pointing to the one name the
    // app uses. For example, someone who types "scallion" and a recipe that says
    // "green onion" are talking about the same thing.
    private static final Map<String, String> ALIASES = new HashMap<>();

    // This block runs once, the first time the class is used, and fills the list.
    static {
        ALIASES.put("scallion", "green onion");
        ALIASES.put("spring onion", "green onion");
        ALIASES.put("capsicum", "bell pepper");
        ALIASES.put("cilantro", "coriander");
        ALIASES.put("courgette", "zucchini");
        ALIASES.put("aubergine", "eggplant");
        ALIASES.put("beef mince", "ground beef");
        ALIASES.put("mince", "ground beef");
        ALIASES.put("tortilla", "tortilla wrap");
        ALIASES.put("wrap", "tortilla wrap");
    }

    /**
     * Cleans up an ingredient name so small differences in how it was typed don't
     * stop a match. It removes spaces at either end, makes everything lower case,
     * swaps in the standard name if there's an alias, and turns plurals into
     * singulars.
     *
     * Examples: "Tomato" -> "tomato", "Tomatoes" -> "tomato", " EGGS " -> "egg",
     * "Scallion" -> "green onion".
     */
    public static String normalizeName(String rawName) {
        if (rawName == null) {
            return ""; // treat a missing name as blank rather than crashing
        }
        String name = rawName.trim().toLowerCase();

        // Swap in the standard name if this is a known alias. This has to happen
        // before the plural step, because the alias list uses singular names.
        if (ALIASES.containsKey(name)) {
            name = ALIASES.get(name);
        }

        name = singularize(name);
        return name;
    }

    /**
     * Turns a plural into a singular using a few simple English spelling rules.
     * It isn't a full dictionary, but it covers the way common pantry ingredients
     * are pluralised. The rules are checked in order and the first one that fits
     * is used, so the more specific endings ("ies", "oes") come before plain "s".
     */
    private static String singularize(String word) {
        if (word.length() < 4) {
            return word; // words this short are left as-is (e.g. "egg", "ice")
        }
        if (word.endsWith("ies")) {
            return word.substring(0, word.length() - 3) + "y"; // berries -> berry
        }
        if (word.endsWith("oes")) {
            return word.substring(0, word.length() - 2); // tomatoes -> tomato, potatoes -> potato
        }
        if (word.endsWith("ches") || word.endsWith("shes")) {
            return word.substring(0, word.length() - 2); // sandwiches -> sandwich
        }
        if (word.endsWith("ss")) {
            return word; // e.g. "grass" - do not strip a trailing double 's'
        }
        if (word.endsWith("s")) {
            return word.substring(0, word.length() - 1); // eggs -> egg, onions -> onion
        }
        return word;
    }

    /**
     * The kinds of measurement. Units in the same kind can be compared (grams and
     * kilograms are both weight); units of different kinds can't (you can't tell
     * whether 200 g of garlic is more or less than 3 cloves).
     *  MASS   = weight (g, kg)
     *  VOLUME = liquid amount (ml, l, tsp, tbsp, cup)
     *  COUNT  = things you count (pieces, cloves, slices, cans, ...)
     */
    private enum UnitFamily {MASS, VOLUME, COUNT, UNKNOWN}

    /**
     * Converts an amount into the smallest unit of its kind, so two amounts can be
     * compared directly. Weights become grams, liquids become millilitres, and
     * counted things stay as they are.
     * Example: 2 kg becomes 2000 (grams).
     */
    private static double toBaseQuantity(double quantity, String rawUnit) {
        return quantity * conversionFactor(rawUnit);
    }

    // How many of the smallest unit fit into one of this unit.
    // For example, 1 kg is 1000 g, so the number for "kg" is 1000.
    private static double conversionFactor(String rawUnit) {
        switch (normalizeUnit(rawUnit)) {
            case "kg":
                return 1000; // to grams
            case "l":
                return 1000; // to millilitres
            case "tsp":
                return 5; // to millilitres
            case "tbsp":
                return 15; // to millilitres
            case "cup":
                return 250; // to millilitres
            default:
                return 1; // g, ml and count units are already their own base
        }
    }

    /**
     * Turns the many ways of writing a unit into one standard spelling, so
     * "grams", "gram" and "g" are all treated as "g".
     *
     * Counted units are kept separate from each other: a clove is not a piece,
     * and a slice is not a can. A unit this method doesn't recognise is returned
     * as typed (tidied up), so it only matches the exact same unit.
     * A missing unit is treated as "piece".
     */
    private static String normalizeUnit(String rawUnit) {
        if (rawUnit == null) {
            return "piece";
        }
        String unit = rawUnit.trim().toLowerCase();
        switch (unit) {
            case "g":
            case "gram":
            case "grams":
                return "g";
            case "kg":
            case "kilogram":
            case "kilograms":
                return "kg";
            case "ml":
            case "milliliter":
            case "milliliters":
            case "millilitre":
            case "millilitres":
                return "ml";
            case "l":
            case "liter":
            case "liters":
            case "litre":
            case "litres":
                return "l";
            case "tsp":
            case "teaspoon":
            case "teaspoons":
                return "tsp";
            case "tbsp":
            case "tablespoon":
            case "tablespoons":
                return "tbsp";
            case "cup":
            case "cups":
                return "cup";
            case "piece":
            case "pieces":
            case "pc":
            case "pcs":
                return "piece";
            case "clove":
            case "cloves":
                return "clove";
            case "slice":
            case "slices":
                return "slice";
            case "pinch":
            case "pinches":
                return "pinch";
            case "sprig":
            case "sprigs":
                return "sprig";
            case "bunch":
            case "bunches":
                return "bunch";
            case "can":
            case "cans":
                return "can";
            case "packet":
            case "packets":
            case "pack":
            case "packs":
                return "packet";
            case "stick":
            case "sticks":
                return "stick";
            case "whole":
                return "whole";
            case "head":
            case "heads":
                return "head";
            case "bottle":
            case "bottles":
                return "bottle";
            case "block":
            case "blocks":
                return "block";
            default:
                return unit; // an unrecognized unit only ever matches an identical one
        }
    }

    // Works out which kind of measurement a unit is. Anything that isn't weight or
    // liquid counts as a counted unit.
    private static UnitFamily familyOf(String rawUnit) {
        String unit = normalizeUnit(rawUnit);
        if (unit.equals("g") || unit.equals("kg")) {
            return UnitFamily.MASS;
        }
        if (unit.equals("ml") || unit.equals("l") || unit.equals("tsp") || unit.equals("tbsp") || unit.equals("cup")) {
            return UnitFamily.VOLUME;
        }
        return UnitFamily.COUNT;
    }

    /**
     * Checks whether the pantry has enough of ONE ingredient a recipe needs.
     *
     * The user might have the same ingredient saved more than once (say, two bags
     * of flour added on different days), so every matching pantry entry is added
     * up. A pantry entry only counts if:
     *  - its cleaned-up name matches the recipe's, and
     *  - its unit is the same kind (weight, liquid or count), and
     *  - for counted units, it's the exact same unit (cloves only count toward
     *    cloves).
     *
     * Example: the recipe needs 500 g flour and the pantry has 300 g and 0.5 kg.
     * That's 300 + 500 = 800 g, which is enough.
     */
    private static boolean pantryHasEnough(RecipeIngredient required, List<PantryItem> pantryItems) {
        // Work out what we're looking for, with the amount in the smallest unit.
        String requiredName = normalizeName(required.getIngredientName());
        UnitFamily requiredFamily = familyOf(required.getUnit());
        double requiredBaseQuantity = toBaseQuantity(required.getRequiredQuantity(), required.getUnit());

        // Running total of how much the pantry has, in the same smallest unit.
        double availableBaseQuantity = 0;
        for (PantryItem pantryItem : pantryItems) {
            if (!normalizeName(pantryItem.getIngredientName()).equals(requiredName)) {
                continue; // a different ingredient, skip it
            }
            if (familyOf(pantryItem.getUnit()) != requiredFamily) {
                continue; // incompatible units (e.g. recipe needs grams, pantry has "pieces")
            }
            if (requiredFamily == UnitFamily.COUNT
                    && !normalizeUnit(pantryItem.getUnit()).equals(normalizeUnit(required.getUnit()))) {
                continue; // count units don't convert across each other (e.g. "clove" != "piece")
            }
            availableBaseQuantity += toBaseQuantity(pantryItem.getQuantity(), pantryItem.getUnit());
        }

        // Enough if the total is at least what the recipe asks for.
        return availableBaseQuantity >= requiredBaseQuantity;
    }

    /**
     * The main check, used by the Home and Recipes screens. Answers "can this
     * recipe be made with this pantry?"
     *
     * It goes through the recipe's ingredients one by one. The moment one is
     * missing or short, it answers no and stops looking. It only answers yes if
     * every ingredient passed.
     */
    public static boolean canMakeRecipe(List<RecipeIngredient> requiredIngredients, List<PantryItem> pantryItems) {
        if (requiredIngredients.isEmpty()) {
            return false; // a recipe with no defined ingredients can never be "makeable"
        }
        for (RecipeIngredient required : requiredIngredients) {
            if (!pantryHasEnough(required, pantryItems)) {
                return false; // one ingredient short is enough to rule the recipe out
            }
        }
        return true; // every ingredient was covered
    }
}
