package com.example.smartpantrymanager.utils;

import com.example.smartpantrymanager.data.PantryItem;
import com.example.smartpantrymanager.data.RecipeIngredient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the app's most important business rule: a recipe is only "suggested"
 * if the pantry contains EVERY required ingredient at AT LEAST the required quantity.
 * No partial matches are ever allowed through.
 *
 * Two supporting problems are solved here:
 *  1. Ingredient NAME normalization, so "Tomato", "tomatoes" and " TOMATO " are
 *     treated as the same ingredient.
 *  2. Simple UNIT conversion, so "2 kg" of flour correctly satisfies a recipe
 *     that needs "500 g" of flour.
 */
public final class IngredientMatcher {

    private IngredientMatcher() {
    }

    // A small predefined list of common aliases, mapped to one canonical name.
    private static final Map<String, String> ALIASES = new HashMap<>();

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
     * Normalizes an ingredient name so simple casing, whitespace and singular/plural
     * differences do not stop two ingredients from being recognised as the same thing.
     *
     * Examples: "Tomato" -> "tomato", "Tomatoes" -> "tomato", " EGGS " -> "egg".
     */
    public static String normalizeName(String rawName) {
        if (rawName == null) {
            return "";
        }
        String name = rawName.trim().toLowerCase();

        // Apply a known alias first, if one exists for this exact word.
        if (ALIASES.containsKey(name)) {
            name = ALIASES.get(name);
        }

        name = singularize(name);
        return name;
    }

    /** Very small, rule-based singularizer - enough for common pantry ingredients. */
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

    /** The measurement "family" a unit belongs to, used to decide if two units are comparable. */
    private enum UnitFamily {MASS, VOLUME, COUNT, UNKNOWN}

    /**
     * Normalizes a unit string and converts a quantity into a common base unit for its
     * family (grams for mass, millilitres for volume, whole pieces for count) so that
     * quantities in different but compatible units can be compared directly.
     */
    private static double toBaseQuantity(double quantity, String rawUnit) {
        return quantity * conversionFactor(rawUnit);
    }

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
     * Normalizes a unit string to one canonical form. Mass (g/kg) and volume
     * (ml/l/tsp/tbsp/cup) units convert to a common base via conversionFactor().
     * Count-style units (piece, clove, slice, ...) are NOT interchangeable with
     * each other - a genuinely unrecognized unit is returned as-is (trimmed and
     * lowercased) rather than collapsed into "piece", so it only ever matches
     * an identical unrecognized string.
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
     * Checks whether the pantry, as a whole, holds enough of one required ingredient.
     * Pantry entries whose normalized name matches are summed together (a user might
     * have two separate pantry rows for the same ingredient); only pantry entries whose
     * unit is in the same family (mass/volume/count) as the required unit are counted.
     */
    private static boolean pantryHasEnough(RecipeIngredient required, List<PantryItem> pantryItems) {
        String requiredName = normalizeName(required.getIngredientName());
        UnitFamily requiredFamily = familyOf(required.getUnit());
        double requiredBaseQuantity = toBaseQuantity(required.getRequiredQuantity(), required.getUnit());

        double availableBaseQuantity = 0;
        for (PantryItem pantryItem : pantryItems) {
            if (!normalizeName(pantryItem.getIngredientName()).equals(requiredName)) {
                continue;
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

        return availableBaseQuantity >= requiredBaseQuantity;
    }

    /**
     * The strict matching algorithm: a recipe can only be made if EVERY required
     * ingredient is present in the pantry at a sufficient quantity. A single missing
     * or insufficient ingredient disqualifies the whole recipe.
     */
    public static boolean canMakeRecipe(List<RecipeIngredient> requiredIngredients, List<PantryItem> pantryItems) {
        if (requiredIngredients.isEmpty()) {
            return false; // a recipe with no defined ingredients can never be "makeable"
        }
        for (RecipeIngredient required : requiredIngredients) {
            if (!pantryHasEnough(required, pantryItems)) {
                return false;
            }
        }
        return true;
    }
}
