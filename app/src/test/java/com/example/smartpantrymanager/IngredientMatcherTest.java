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
 * Automatic checks for the recipe matching rule in IngredientMatcher.
 *
 * Each method marked @Test builds a small made-up recipe and pantry, asks
 * IngredientMatcher whether the recipe can be made, and checks the answer.
 * assertTrue means "this should be a yes", assertFalse means "this should be a
 * no". If any answer is wrong, the test fails and names the method.
 *
 * The first five cover the scenarios from the assignment brief; the rest cover
 * unit conversion and other edge cases. They run on a normal computer with
 * ./gradlew testDebugUnitTest, no phone or emulator needed, because
 * IngredientMatcher doesn't use any Android features.
 */
public class IngredientMatcherTest {

    // Shortcuts that keep the tests short and readable.
    // required(...) makes one line of a recipe's ingredient list. The recipe ID
    // (1) doesn't matter here because nothing is saved to a database.
    private static RecipeIngredient required(String name, double quantity, String unit) {
        return new RecipeIngredient(1, name, quantity, unit);
    }

    // inPantry(...) makes one pantry item with no expiry date.
    private static PantryItem inPantry(String name, double quantity, String unit) {
        return new PantryItem(name, quantity, unit, null);
    }

    // Everything the recipe needs is in the pantry, so the answer should be yes.
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

    // Two out of three ingredients isn't enough. The missing chicken should rule it out.
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

    // The ingredient is there, but only 2 of the 3 needed.
    @Test
    public void test3_insufficientQuantity_recipeDoesNotAppear() {
        List<RecipeIngredient> required = List.of(required("tomato", 3, "piece"));
        List<PantryItem> pantry = List.of(inPantry("tomato", 2, "piece"));

        assertFalse(IngredientMatcher.canMakeRecipe(required, pantry));
    }

    // Having more than needed (5 when 3 are needed) is fine.
    @Test
    public void test4_sufficientQuantity_recipeAppears() {
        List<RecipeIngredient> required = List.of(required("tomato", 3, "piece"));
        List<PantryItem> pantry = List.of(inPantry("tomato", 5, "piece"));

        assertTrue(IngredientMatcher.canMakeRecipe(required, pantry));
    }

    // "tomatoes" in the pantry should count as "tomato" in the recipe.
    @Test
    public void test5_pluralPantryEntry_matchesAfterNormalization() {
        List<RecipeIngredient> required = List.of(required("tomato", 1, "piece"));
        List<PantryItem> pantry = List.of(inPantry("tomatoes", 1, "piece"));

        assertTrue(IngredientMatcher.canMakeRecipe(required, pantry));
    }

    // The same recipe is checked twice: no before the chicken is added,
    // yes straight after. This is what the user sees on the Recipes screen.
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

    // 1 kg is 1000 g, which covers a 500 g requirement.
    @Test
    public void unitConversion_kilogramsSatisfyGramsRequirement() {
        List<RecipeIngredient> required = List.of(required("flour", 500, "g"));
        List<PantryItem> pantry = List.of(inPantry("flour", 1, "kg"));

        assertTrue(IngredientMatcher.canMakeRecipe(required, pantry));
    }

    // 250 g is less than the 500 g needed.
    @Test
    public void unitConversion_insufficientGrams_recipeDoesNotAppear() {
        List<RecipeIngredient> required = List.of(required("flour", 500, "g"));
        List<PantryItem> pantry = List.of(inPantry("flour", 250, "g"));

        assertFalse(IngredientMatcher.canMakeRecipe(required, pantry));
    }

    // Grams and pieces can't be compared, so 5 pieces of rice doesn't count
    // toward 200 g of rice.
    @Test
    public void incompatibleUnits_doNotMatch() {
        List<RecipeIngredient> required = List.of(required("rice", 200, "g"));
        List<PantryItem> pantry = List.of(inPantry("rice", 5, "piece"));

        assertFalse(IngredientMatcher.canMakeRecipe(required, pantry));
    }

    // Different counted units don't mix: pieces or slices of garlic don't
    // count toward cloves.
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

    // Same counted unit on both sides, and enough of it.
    @Test
    public void countUnits_matchWhenIdentical() {
        List<RecipeIngredient> required = List.of(required("garlic", 2, "clove"));
        List<PantryItem> pantry = List.of(inPantry("garlic", 3, "clove"));

        assertTrue(IngredientMatcher.canMakeRecipe(required, pantry));
    }

    // "cloves" and "clove" are the same unit.
    @Test
    public void pluralCountUnitAlias_matchesSingularRequirement() {
        List<RecipeIngredient> required = List.of(required("garlic", 2, "clove"));
        List<PantryItem> pantry = List.of(inPantry("garlic", 3, "cloves"));

        assertTrue("Plural 'cloves' should normalize to the same unit as 'clove'",
                IngredientMatcher.canMakeRecipe(required, pantry));
    }

    // Spoons and cups are converted to millilitres before comparing.
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
