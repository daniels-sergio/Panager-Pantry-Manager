package com.example.smartpantrymanager.utils;

import com.example.smartpantrymanager.data.Recipe;
import com.example.smartpantrymanager.data.RecipeDao;
import com.example.smartpantrymanager.data.RecipeIngredient;

/**
 * Inserts a starter set of 20 recipes (with their ingredients) into the Room
 * database the first time the app runs. seedIfEmpty() checks the recipe count
 * first so recipes are never duplicated on later launches.
 *
 * This must be called from a background thread (see AppDatabase.databaseWriteExecutor)
 * because it performs real Room database writes.
 */
public final class DatabaseSeeder {

    private DatabaseSeeder() {
    }

    public static void seedIfEmpty(RecipeDao recipeDao) {
        if (recipeDao.getRecipeCount() > 0) {
            return; // already seeded, do nothing
        }

        // ===== South African classics =====

        addRecipe(recipeDao, "Bobotie",
                "A spiced minced beef bake with a golden egg topping, South Africa's best-known dish.",
                "1. Soak the bread in some of the milk.\n2. Fry the onion until soft.\n3. Add the mince and curry powder, and brown well.\n4. Mix in the soaked bread and press into a baking dish.\n5. Beat the remaining milk with the egg and pour over the top, then bake until set.",
                new String[]{"beef mince", "onion", "curry powder", "bread", "milk", "egg"},
                new double[]{500, 1, 2, 1, 300, 2},
                new String[]{"g", "piece", "tbsp", "slice", "ml", "piece"});

        addRecipe(recipeDao, "Boerewors Rolls",
                "Grilled farmhouse sausage served in a fresh bread roll.",
                "1. Coil the boerewors and grill until cooked through.\n2. Fry the sliced onion until soft.\n3. Slice the bread rolls open.\n4. Place a portion of boerewors in each roll.\n5. Top with the fried onion and tomato sauce.",
                new String[]{"boerewors", "bread roll", "onion", "tomato sauce"},
                new double[]{400, 4, 1, 60},
                new String[]{"g", "piece", "piece", "ml"});

        addRecipe(recipeDao, "Chakalaka",
                "A spicy vegetable relish traditionally served alongside pap.",
                "1. Fry the onion and pepper until soft.\n2. Add the grated carrot and cook briefly.\n3. Stir in the tomato and curry powder.\n4. Simmer until the vegetables soften.\n5. Stir through the baked beans and serve.",
                new String[]{"onion", "bell pepper", "carrot", "tomato", "curry powder", "baked beans"},
                new double[]{1, 1, 2, 3, 1, 1},
                new String[]{"piece", "piece", "piece", "piece", "tbsp", "can"});

        addRecipe(recipeDao, "Pap en Wors",
                "Maize porridge served with a rich tomato and onion sauce and grilled boerewors.",
                "1. Bring the water to the boil and stir in the maize meal.\n2. Cook, stirring often, until thick.\n3. Grill the boerewors until cooked through.\n4. Fry the onion and tomato into a sauce.\n5. Serve the pap with the wors and sauce.",
                new String[]{"maize meal", "water", "boerewors", "onion", "tomato"},
                new double[]{500, 1, 300, 1, 4},
                new String[]{"g", "l", "g", "piece", "piece"});

        addRecipe(recipeDao, "Potjiekos",
                "A slow-cooked meat and vegetable stew, traditionally made in a cast-iron pot.",
                "1. Brown the beef in the pot.\n2. Layer the onion, carrot and potato on top.\n3. Pour in the stock without stirring.\n4. Cover and simmer slowly for a few hours.\n5. Serve straight from the pot.",
                new String[]{"beef", "potato", "carrot", "onion", "beef stock"},
                new double[]{600, 4, 3, 1, 500},
                new String[]{"g", "piece", "piece", "piece", "ml"});

        addRecipe(recipeDao, "Malva Pudding",
                "A sweet, spongy baked pudding soaked in a warm caramel sauce.",
                "1. Cream the sugar, egg and jam together.\n2. Fold in the flour and milk to form a batter.\n3. Pour into a baking dish and bake until risen.\n4. Melt the butter with extra milk and sugar for the sauce.\n5. Pour the warm sauce over the pudding as soon as it comes out of the oven.",
                new String[]{"flour", "sugar", "egg", "apricot jam", "milk", "butter"},
                new double[]{250, 200, 2, 2, 250, 50},
                new String[]{"g", "g", "piece", "tbsp", "ml", "g"});

        addRecipe(recipeDao, "Vetkoek",
                "Deep-fried dough rolls, often filled with mince or jam.",
                "1. Mix the flour, yeast and sugar with the water into a soft dough.\n2. Leave to rise until doubled in size.\n3. Shape into rolls.\n4. Deep fry in hot oil until golden on all sides.\n5. Drain and serve warm, plain or filled.",
                new String[]{"flour", "yeast", "sugar", "water", "oil"},
                new double[]{500, 7, 1, 300, 500},
                new String[]{"g", "g", "tbsp", "ml", "ml"});

        addRecipe(recipeDao, "Bunny Chow",
                "A hollowed loaf of bread filled with curry, a Durban classic.",
                "1. Fry the onion until soft.\n2. Add the curry powder and cook until fragrant.\n3. Add the chicken and potato, and simmer until cooked through.\n4. Hollow out the loaf of bread.\n5. Spoon the curry into the bread and serve.",
                new String[]{"bread", "chicken", "onion", "curry powder", "potato"},
                new double[]{1, 400, 1, 2, 2},
                new String[]{"whole", "g", "piece", "tbsp", "piece"});

        addRecipe(recipeDao, "Sosaties",
                "Skewered, marinated meat traditionally grilled over coals.",
                "1. Mix the curry powder and vinegar into a marinade.\n2. Coat the lamb in the marinade and leave to rest.\n3. Thread the lamb, apricot and onion onto skewers.\n4. Grill over coals, turning often.\n5. Serve hot off the skewer.",
                new String[]{"lamb", "dried apricot", "onion", "curry powder", "vinegar"},
                new double[]{500, 8, 1, 1, 30},
                new String[]{"g", "piece", "piece", "tbsp", "ml"});

        addRecipe(recipeDao, "Melktert",
                "A creamy, cinnamon-dusted milk tart in a pastry crust.",
                "1. Heat the milk in a pot.\n2. Whisk the flour, sugar and egg together.\n3. Slowly stir in the hot milk to make a custard.\n4. Return to the heat and stir until thickened.\n5. Pour into the pastry case, dust with cinnamon and chill before serving.",
                new String[]{"milk", "flour", "sugar", "egg", "pastry case", "cinnamon"},
                new double[]{500, 50, 100, 2, 1, 1},
                new String[]{"ml", "g", "g", "piece", "whole", "pinch"});

        // ===== Cape Malay classics =====

        addRecipe(recipeDao, "Cape Malay Chicken Curry",
                "A fragrant, mildly spiced curry with Cape Malay roots.",
                "1. Fry the onion and garlic until soft.\n2. Stir in the curry powder and cook until fragrant.\n3. Add the chicken and brown lightly.\n4. Add the tomato and potato with a little water.\n5. Simmer until the chicken and potato are cooked through.",
                new String[]{"chicken", "onion", "garlic", "curry powder", "tomato", "potato"},
                new double[]{500, 1, 3, 2, 3, 2},
                new String[]{"g", "piece", "clove", "tbsp", "piece", "piece"});

        addRecipe(recipeDao, "Cape Malay Koesisters",
                "Spiced, syrup-coated plaited doughnuts rolled in coconut.",
                "1. Mix the flour, yeast, cinnamon and a little sugar with the water into a dough.\n2. Leave to rise, then plait into small shapes.\n3. Deep fry until golden.\n4. Dip immediately into a cold sugar syrup.\n5. Roll in coconut and leave to cool.",
                new String[]{"flour", "yeast", "cinnamon", "sugar", "water", "coconut"},
                new double[]{500, 7, 1, 300, 250, 100},
                new String[]{"g", "g", "tsp", "g", "ml", "g"});

        addRecipe(recipeDao, "Denningvleis",
                "A tangy Cape Malay lamb curry flavoured with tamarind and bay leaves.",
                "1. Brown the lamb with the onion and garlic.\n2. Add the bay leaf and nutmeg.\n3. Stir in the tamarind and a little water.\n4. Cover and simmer slowly until the lamb is tender.\n5. Serve with yellow rice.",
                new String[]{"lamb", "onion", "garlic", "tamarind", "bay leaf", "nutmeg"},
                new double[]{600, 1, 2, 30, 2, 1},
                new String[]{"g", "piece", "clove", "ml", "piece", "pinch"});

        addRecipe(recipeDao, "Cape Malay Lamb Bredie",
                "A slow-cooked lamb and tomato stew.",
                "1. Brown the lamb and onion in a large pot.\n2. Add the tomato and chilli.\n3. Cover and simmer slowly until the meat is tender.\n4. Add the potato and cook until soft.\n5. Serve hot with rice.",
                new String[]{"lamb", "tomato", "onion", "potato", "chilli"},
                new double[]{600, 6, 1, 3, 1},
                new String[]{"g", "piece", "piece", "piece", "piece"});

        addRecipe(recipeDao, "Cape Malay Breyani",
                "A layered, fragrantly spiced rice and meat dish.",
                "1. Fry the onion until deep golden and set half aside.\n2. Cook the chicken with the curry powder and remaining onion.\n3. Parboil the rice and potato separately.\n4. Layer the rice, chicken and potato in a pot.\n5. Top with the fried onion and boiled egg, then steam until fully cooked.",
                new String[]{"rice", "chicken", "onion", "curry powder", "potato", "egg"},
                new double[]{400, 500, 2, 2, 2, 2},
                new String[]{"g", "g", "piece", "tbsp", "piece", "piece"});

        addRecipe(recipeDao, "Cape Malay Samoosas",
                "Crisp, triangular pastries filled with spiced mince.",
                "1. Fry the onion and curry powder until fragrant.\n2. Add the mince and cook until done, then cool.\n3. Spoon the filling into the pastry and fold into triangles.\n4. Seal the edges with a little water.\n5. Deep fry until golden and crisp.",
                new String[]{"samoosa pastry", "beef mince", "onion", "curry powder", "oil"},
                new double[]{1, 300, 1, 1, 500},
                new String[]{"packet", "g", "piece", "tbsp", "ml"});

        addRecipe(recipeDao, "Cape Malay Pickled Fish",
                "Fried fish in a spiced, tangy vinegar and onion sauce, a Cape Malay Easter tradition.",
                "1. Fry the fish fillets until cooked and set aside.\n2. Soften the onion in a pot.\n3. Stir in the curry powder, vinegar, sugar and bay leaf.\n4. Simmer the sauce until slightly thickened.\n5. Pour the hot sauce over the fish and chill before serving.",
                new String[]{"fish fillet", "onion", "vinegar", "curry powder", "sugar", "bay leaf"},
                new double[]{4, 2, 250, 2, 50, 2},
                new String[]{"piece", "piece", "ml", "tbsp", "g", "piece"});

        addRecipe(recipeDao, "Cape Malay Roti",
                "A soft, layered flatbread often served alongside curry.",
                "1. Mix the flour, salt and water into a soft dough.\n2. Rest the dough, then divide into balls.\n3. Roll each ball flat and brush with oil.\n4. Fold and roll again to create layers.\n5. Cook on a hot pan until golden on both sides.",
                new String[]{"flour", "water", "oil", "salt"},
                new double[]{400, 200, 30, 1},
                new String[]{"g", "ml", "ml", "pinch"});

        addRecipe(recipeDao, "Cape Malay Curried Frikkadels",
                "Spiced meatballs simmered in a mild curry sauce.",
                "1. Mix the mince with half the onion, egg and a little curry powder.\n2. Roll into small meatballs.\n3. Fry the remaining onion and curry powder until fragrant.\n4. Add the tomato and a little water to make a sauce.\n5. Simmer the meatballs in the sauce until cooked through.",
                new String[]{"beef mince", "onion", "egg", "curry powder", "tomato"},
                new double[]{500, 1, 1, 2, 3},
                new String[]{"g", "piece", "piece", "tbsp", "piece"});

        addRecipe(recipeDao, "Cape Malay Melkkos",
                "A comforting milk-based dumpling porridge, sweetened with cinnamon sugar.",
                "1. Rub the butter into the flour to form crumbs.\n2. Bring the milk to a gentle simmer.\n3. Drop spoonfuls of the crumb mixture into the milk.\n4. Simmer until the dumplings are cooked through.\n5. Serve sprinkled with cinnamon sugar.",
                new String[]{"flour", "butter", "milk", "sugar", "cinnamon"},
                new double[]{250, 30, 1, 50, 1},
                new String[]{"g", "g", "l", "g", "tsp"});
    }

    private static void addRecipe(RecipeDao recipeDao, String name, String description, String instructions,
                                   String[] ingredientNames, double[] quantities, String[] units) {
        long recipeId = recipeDao.insertRecipe(new Recipe(name, description, instructions));
        for (int i = 0; i < ingredientNames.length; i++) {
            recipeDao.insertRecipeIngredient(
                    new RecipeIngredient((int) recipeId, ingredientNames[i], quantities[i], units[i]));
        }
    }
}
