# Panager (Smart Pantry Manager)

Panager is an Android app for keeping track of what's in your kitchen and
finding recipes you can cook with it. It only suggests a recipe when your pantry
has every ingredient the recipe needs, in at least the amount it needs. If you're
short on even one thing, the recipe doesn't show up.

## What it does

- A home dashboard shows how many items are in your pantry, how many recipes you
  can make right now, and how many items expire soon, with shortcuts to the main
  actions.
- You can add, view, edit and delete pantry items. Each one has a quantity, a unit
  and an optional expiry date.
- The Suggested Recipes screen is recalculated from the current pantry every time
  it opens, and only lists recipes you can fully make.
- Tapping a recipe opens its ingredient list, quantities and numbered steps.
- Settings has an on/off toggle for expiring-soon alerts and a Metric/Imperial
  choice. Both are saved with SharedPreferences.
- Pantry items are colour-coded: red for expired, yellow for anything expiring
  within 3 days, blue otherwise.
- 20 recipes are written into the database the first time the app launches(used generative ai for recipes).
- Deleting anything asks for confirmation first. Forms validate input and show an
  error message next to the bad field, and empty lists show a message instead of
  a blank screen.

## Built with

- Java (no Kotlin)
- Android SDK, AndroidX, XML layouts
- Room on top of SQLite
- RecyclerView with custom adapters for the pantry and recipe lists
- Activities and Intents for navigation. Only IDs go in Intent extras, never whole
  objects.
- Gradle (Groovy DSL). Everything builds and runs from the command line.

The app doesn't use Firebase, an external recipe API, maps or location, or
payments.

## Why Room instead of raw SQLite

Room checks SQL queries at compile time, gets rid of manual cursor handling, and
returns query results as plain Java objects (`PantryItem`, `Recipe`,
`RecipeIngredient`). Underneath it's still an on-device SQLite database, so pantry
data and recipes survive the app being closed and reopened, which the assignment
requires. All database calls run on a shared background executor
(`AppDatabase.databaseWriteExecutor`), never on the UI thread.

## Database

`SmartPantryDatabase` (Room, version 1) has three tables:

| Table | Columns |
|---|---|
| `pantry_items` | id, ingredient_name, quantity, unit, expiry_date |
| `recipes` | id, name, description, instructions |
| `recipe_ingredients` | id, recipe_id, ingredient_name, required_quantity, unit |

`PantryDao` handles insert, getAll, getById, update, delete and deleteAll for
pantry items. `RecipeDao` inserts recipes and their ingredients, reads all recipes
or a single one, reads a recipe's ingredients, and counts recipes. The count is
how the app knows whether the first-launch seed has already run.

## Screens

1. Home / Dashboard (`MainActivity`)
2. Pantry List (`PantryActivity`)
3. Add / Edit Ingredient (`AddEditIngredientActivity`, one screen for both)
4. Suggested Recipes (`SuggestedRecipesActivity`)
5. Recipe Detail (`RecipeDetailActivity`)
6. Settings (`SettingsActivity`)

A `BottomNavigationView` with Home, Pantry, Recipes and Settings connects the four
top-level screens. Add/Edit Ingredient and Recipe Detail are opened with an Intent
that carries an integer ID, and the screen loads the full record from Room itself.

## How recipe matching works

All of the matching logic lives in `utils/IngredientMatcher.java`, outside any
Activity, so it can be read on its own and unit tested without a device.

1. `normalizeName` trims whitespace, lowercases the name, and runs it through a
   simple rule-based singularizer (`tomatoes` becomes `tomato`, `eggs` becomes
   `egg`). A short alias list handles cases like `scallion` → `green onion`. This
   stops "Eggs" in the pantry from failing to match "egg" in a recipe.
2. Units are sorted into three families: mass (g, kg), volume (ml, l) and count
   (pieces, cloves, slices and so on). Within a family, amounts are converted to a
   base unit