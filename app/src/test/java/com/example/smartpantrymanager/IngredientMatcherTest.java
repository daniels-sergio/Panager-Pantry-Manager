package com.example.smartpantrymanager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.smartpantrymanager.data.PantryItem;
import com.example.smartpantrymanager.data.RecipeIngredient;
import com.example.smartpantrymanager.utils.IngredientMatcher;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for the strict recipe matching algorithm. These mirror the five
 * scenarios from the assignment brief and run on the local JVM (no Android
 * device required) since IngredientMatcher has no Android dependencies.
 */
public class IngredientMatcherTest {

    private static RecipeIngredient required(String name, double quantity, String unit) {
        return new RecipeIngredient(1, name, quantity, unit);
    }

    private static PantryItem inPantry(String name, double quantity, String unit) {
        return new PantryItem(name, quantity, unit, null);
    }

    @Test
    public void test1_allIngredientsPresent_recipeAppears() {
        List<RecipeIngredient> required = Arrays.asList(
                required("tomato", 1, "piece"),
                required("onion", 1, "piece"),
                required("chicken", 1, "piece"));

        List<PantryItem> pantry = Arrays.asList(
                inPantry("tomato", 2, "piece"),
                inPantry("onion", 1, "piece"),
                inPantry("chicken", 1, "piece"));

        assertTrue(IngredientMatcher.canMakeRecipe(required, pantry));
    }

    @Test
    public void test2_missingIngredient_recipeDoesNotAppear() {
        List<RecipeIngredient> required = Arrays.asList(
                required("tomato", 1, "piece"),
                required("onion", 1, "piece"),
                required("chicken", 1, "piece"));

        List<PantryItem> pantry = Arrays.asList(
                inPantry("tomato", 2, "piece"),
                inPantry("onion", 1, "piece"));
        // no chicken in the pantry

        assertFalse(IngredientMatcher.canMakeRecipe(required, pantry));
    }

    @Test
    public void test3_insufficientQuantity_recipeDoesNotAppear() {
        List<RecipeIngredient> required = List.of(required("tomato", 3, "piece"));
        List<PantryItem> pantry = List.of(inPantry("tomato", 2, "piece"));

        assertFalse(IngredientMatcher.canMakeRecipe(required, pantry));
    }

    @Test
    public void test4_sufficientQuantity_recipeAppears() {
        List<RecipeIngredient> required = List.of(required("tomato", 3, "piece"));
        List<PantryItem> pantry = List.of(inPantry("tomato", 5, "piece"));

        assertTrue(IngredientMatcher.canMakeRecipe(required, pantry));
    }

    @Test
    public void test5_pluralPantryEntry_matchesAfterNormalization() {
        List<RecipeIngredient> required = List.of(required("tomato", 1, "piece"));
        List<PantryItem> pantry = List.of(inPantry("tomatoes", 1, "piece"));

        assertTrue(IngredientMatcher.canMakeRecipe(required, pantry));
    }

    @Test
    public void addingMissingIngredientMakesRecipeEligible() {
        List<RecipeIngredient> required = Arrays.asList(
                required("tomato", 2, "piece"),
                required("onion", 1, "piece"),
                required("chicken", 200, "g"));

        List<PantryItem> pantry = new ArrayList<>(Arrays.asList(
                inPantry("tomato", 2, "piece"),
                inPantry("onion", 1, "piece")));

        assertFalse("Recipe should not be suggested before chicken is added", IngredientMatcher.canMakeRecipe(required, pantry));

        pantry.add(inPantry("chicken", 200, "g"));

        assertTrue("Recipe should become suggested once enough chicken is added", IngredientMatcher.canMakeRecipe(required, pantry));
    }

    @Test
    public void unitConversion_kilogramsSatisfyGramsRequirement() {
        List<RecipeIngredient> required = List.of(required("flour", 500, "g"));
        List<PantryItem> pantry = List.of(inPantry("flour", 1, "kg"));

        assertTrue(IngredientMatcher.canMakeRecipe(required, pantry));
    }

    @Test
    public void unitConversion_insufficientGrams_recipeDoesNotAppear() {
        List<RecipeIngredient> required = List.of(required("flour", 500, "g"));
        List<PantryItem> pantry = List.of(inPantry("flour", 250, "g"));

        assertFalse(IngredientMatcher.canMakeRecipe(required, pantry));
    }

    @Test
    public void incompatibleUnits_doNotMatch() {
        List<RecipeIngredient> required = List.of(required("rice", 200, "g"));
        List<PantryItem> pantry = List.of(inPantry("rice", 5, "piece"));

        assertFalse(IngredientMatcher.canMakeRecipe(required, pantry));
    }

    @Test
    public void countUnits_doNotConvertAcrossEachOther() {
        List<RecipeIngredient> required = List.of(required("garlic", 2, "clove"));
        List<PantryItem> pantryWithPieces = List.of(inPantry("garlic", 5, "piece"));
        List<PantryItem> pantryWithSlices = List.of(inPantry("garlic", 5, "slice"));

        assertFalse("A 'clove' requirement should not be satisfied by 'piece' entries",
                IngredientMatcher.canMakeRecipe(required, pantryWithPieces));
        assertFalse("A 'clove' requirement should not be satisfied by 'slice' entries",
                IngredientMatcher.canMakeRecipe(required, pantryWithSlices));
    }

    @Test
    public void countUnits_matchWhenIdentical() {
        List<RecipeIngredient> required = List.of(required("garlic", 2, "clove"));
        List<PantryItem> pantry = List.of(inPantry("garlic", 3, "clove"));

        assertTrue(IngredientMatcher.canMakeRecipe(required, pantry));
    }

    @Test
    public void pluralCountUnitAlias_matchesSingularRequirement() {
        List<RecipeIngredient> required = List.of(required("garlic", 2, "clove"));
        List<PantryItem> pantry = List.of(inPantry("garlic", 3, "cloves"));

        assertTrue("Plural 'cloves' should normalize to the same unit as 'clove'",
                IngredientMatcher.canMakeRecipe(required, pantry));
    }

    @Test
    public void volumeConversion_teaspoonsTablespoonsAndCupsConvertToMillilitres() {
        // 1 tbsp = 15ml, 1 cup = 250ml, 1 tsp = 5ml
        List<RecipeIngredient> required = List.of(required("milk", 250, "ml"));
        List<PantryItem> pantryWithCup = List.of(inPantry("milk", 1, "cup"));
        List<PantryItem> pantryWithTablespoons = List.of(inPantry("milk", 20, "tbsp")); // 300ml
        List<PantryItem> pantryWithTeaspoons = List.of(inPantry("milk", 20, "tsp")); // 100ml

        assertTrue("1 cup (250ml) should satisfy a 250ml requirement",
                IngredientMatcher.canMakeRecipe(required, pantryWithCup));
        assertTrue("20 tbsp (300ml) should satisfy a 250ml requirement",
                IngredientMatcher.canMakeRecipe(required, pantryWithTablespoons));
        assertFalse("20 tsp (100ml) should NOT satisfy a 250ml requirement",
                IngredientMatcher.canMakeRecipe(required, pantryWithTeaspoons));
    }
}
