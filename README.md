# Smart Pantry Manager (Panager)

A native Android application that helps users reduce food waste by tracking the
ingredients they already have at home and suggesting recipes they can make
**using only what is currently in their pantry**.

## Description

Panager lets a user maintain a list of pantry ingredients (name, quantity, unit,
expiry date) and automatically works out which of a library of stored recipes
they can cook right now. A recipe is only ever suggested if the pantry contains
**every** required ingredient at **at least** the required quantity — no
partial matches are ever shown.

## Features

- **Dashboard** with live summary counts (total pantry items, recipes you can
  make, items expiring soon) and quick-action shortcuts.
- **Pantry CRUD** — add, view, edit and delete ingredients, each with quantity,
  unit and an optional expiry date.
- **Strict recipe matching** — Suggested Recipes only ever shows recipes the
  user can fully make, computed fresh from the current pantry contents.
- **Recipe detail** screen with ingredients, quantities and numbered method
  steps.
- **Settings** screen for an expiring-soon alert toggle and a preferred unit
  system (Metric/Imperial), persisted with SharedPreferences.
- **Expiry indicators** — pantry items are colour-coded Expired (red),
  Expiring Soon (yellow, within 3 days) or Normal (blue).
- **20 seeded recipes**, inserted into Room once on first launch.
- Confirmation dialog before any delete, full input validation with clear
  error messages, and empty-state screens instead of blank UI.

## Technology used

- Java (no Kotlin)
- Android SDK, AndroidX, XML layouts
- Room (SQLite) for persistence
- RecyclerView + custom adapters for the pantry and recipe lists
- Activities + Intents for navigation (IDs passed through Intent extras, not
  whole objects)
- Gradle (Groovy DSL) build, runnable entirely from the command line

No Firebase, no external recipe API, no Google Maps/location/GPS, no payment
processing.

## Why Room

Room was chosen over talking to raw SQLite directly because it gives compile-time
verification of SQL queries, removes boilerplate cursor-handling code, and maps
query results straight onto plain Java objects (`PantryItem`, `Recipe`,
`RecipeIngredient`). It still sits on top of a real on-device SQLite database,
so pantry data and recipes genuinely persist after the app is closed and
reopened, which the assignment specifically requires. All database work is
dispatched to a shared background `ExecutorService`
(`AppDatabase.databaseWriteExecutor`) so it never touches the UI thread.

## Database structure

**SmartPantryDatabase** (Room, version 1), three tables:

| Table | Columns |
|---|---|
| `pantry_items` | id, ingredient_name, quantity, unit, expiry_date |
| `recipes` | id, name, description, instructions |
| `recipe_ingredients` | id, recipe_id, ingredient_name, required_quantity, unit |

`PantryDao` supports insert/getAll/getById/update/delete/deleteAll for pantry
items. `RecipeDao` supports inserting recipes and their ingredients, reading
all recipes, reading one recipe, reading a recipe's ingredients, and counting
recipes (used to decide whether the one-time seed has already run).

## Screens

1. **Home / Dashboard** (`MainActivity`)
2. **Pantry List** (`PantryActivity`)
3. **Add / Edit Ingredient** (`AddEditIngredientActivity`, one screen for both)
4. **Suggested Recipes** (`SuggestedRecipesActivity`)
5. **Recipe Detail** (`RecipeDetailActivity`)
6. **Settings** (`SettingsActivity`)

A shared `BottomNavigationView` (Home / Pantry / Recipes / Settings) links the
four top-level screens; Add/Edit Ingredient and Recipe Detail are opened via
`Intent`s carrying only an integer ID, with the full record then loaded from
Room.

## Strict matching logic

The core business rule — *a recipe may only appear in Suggested Recipes if the
pantry holds every required ingredient at a sufficient quantity* — is
implemented entirely in `utils/IngredientMatcher.java`, kept deliberately
separate from any Activity so it is easy to explain and to unit test:

1. **Name normalization** (`normalizeName`) trims whitespace, lowercases, and
   applies a small rule-based singularizer (`tomatoes` → `tomato`,
   `eggs` → `egg`) plus a short predefined alias list
   (`scallion` → `green onion`, etc.), so trivial wording differences never
   block a match.
2. **Unit handling**: each unit is normalized into one of three families —
   mass (g/kg), volume (ml/l), or count (pieces/cloves/slices/...) — and
   converted to a common base unit within its family (grams, millilitres, or
   whole pieces) so `1 kg` correctly satisfies a `500 g` requirement. Units
   from different families (e.g. grams vs. pieces) are treated as
   non-comparable, so they never falsely match.
3. **`canMakeRecipe(requiredIngredients, pantryItems)`** loops over every
   ingredient a recipe needs; for each one it sums the pantry's matching
   entries (by normalized name and compatible unit family) and checks the
   total against the required quantity. If **any single ingredient** is
   missing or insufficient, the whole recipe is rejected — there is no
   partial-match path.
4. `SuggestedRecipesActivity` calls this once per recipe against the current
   pantry and only adds recipes that return `true`.

## Testing

`app/src/test/java/.../IngredientMatcherTest.java` contains JUnit tests
(run on the local JVM, no emulator needed) covering the scenarios from the
assignment brief: all-ingredients-present, a missing ingredient, insufficient
quantity, sufficient quantity, singular/plural normalization, a recipe
becoming eligible the moment its last missing ingredient is added, and basic
g/kg unit conversion (both compatible and insufficient cases).

Run them with:

```
./gradlew testDebugUnitTest
```

## Installation / running the app

Requirements: Android SDK, a JDK the installed Gradle/AGP combination supports,
and either an emulator or a physical device with USB debugging enabled.

From the project root, using the Gradle wrapper (no Android Studio required):

```
# Build a debug APK
./gradlew assembleDebug

# Build and install it on a connected device/emulator
./gradlew installDebug

# Run the unit tests
./gradlew testDebugUnitTest
```

The resulting APK is written to `app/build/outputs/apk/debug/app-debug.apk`.
Launch "Panager" from the device's app drawer, or:

```
adb shell am start -n com.example.smartpantrymanager/.ui.MainActivity
```

## GitHub repository

This project is set up as its own Git repository (separate from any other
repo on the development machine) so it can be committed incrementally,
following the commit progression suggested in the assignment brief (project
setup → theme/colours → Room entities → DAOs → pantry CRUD → RecyclerView →
recipe seeding → strict matching → Suggested Recipes → Recipe Detail →
Settings → UI polish/validation → testing → documentation).
