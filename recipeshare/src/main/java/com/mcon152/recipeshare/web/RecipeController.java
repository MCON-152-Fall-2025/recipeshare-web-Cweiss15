package com.mcon152.recipeshare.web;

import com.mcon152.recipeshare.Recipe;
import com.mcon152.recipeshare.service.RecipeFactory;
import com.mcon152.recipeshare.service.RecipeService;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    private final RecipeService recipeService;
    private static final Logger logger = LoggerFactory.getLogger(RecipeController.class);

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    /**
     * Create a new recipe.
     * Returns 201 Created with Location header pointing to the new resource.
     */
    @PostMapping
    public ResponseEntity<Recipe> addRecipe(@RequestBody RecipeRequest recipeRequest) {
        try {
            logger.info("POST /api/recipes - Adding new recipe");
            Recipe toSave = RecipeFactory.createFromRequest(recipeRequest);
            Recipe saved = recipeService.addRecipe(toSave);
            logger.info("created recipe with id={}", saved.getId());
            logger.debug("Added recipe {}, type: {}", saved.getTitle(), saved.getRecipeType());
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()           // /api/recipes
                    .path("/{id}")                  // /{id}
                    .buildAndExpand(saved.getId())
                    .toUri();
            return ResponseEntity.created(location).body(saved);
        } catch (Exception e) {
            logger.error("Error occurred while adding recipe: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Retrieve all recipes. 200 OK.
     */
    @GetMapping
    public ResponseEntity<List<Recipe>> getAllRecipes() {
        logger.info("GET /api/recipes - Getting all recipes");
        List<Recipe> recipes = recipeService.getAllRecipes();
        logger.info("GET /api/recipes - Get all recipes successful");
        return ResponseEntity.ok(recipes);
    }

    /**
     * Retrieve a recipe by id. 200 OK or 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getRecipeById(@PathVariable long id) {
        logger.info("Get /api/recipes/{} - Getting recipe at id {}", id, id);

        return recipeService.getRecipeById(id)
                .map(recipe -> {
                    logger.info("Get /api/recipes/{} - Successfully got recipe at id {}", id, id);
                    return (ResponseEntity.ok(recipe));
                })
                .orElseGet(() -> {
                    logger.warn("Get /api/recipes/{} - No recipe found with id {}", id, id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Delete a recipe. 204 No Content if deleted, 404 Not Found otherwise.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable long id) {
        try {
            logger.info("DELETE /api/recipes/{} - Deleting recipe at id {}", id, id);
            boolean deleted = recipeService.deleteRecipe(id);
            if (deleted) {
                logger.info("DELETE /api/recipes/{} - Successfully deleted recipe at id {}", id, id);
                return ResponseEntity.noContent().build();
            } else {
                logger.warn("DELETE /api/recipes/{} - No recipe found with id {}", id, id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error occurred while deleting recipe: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Replace a recipe (full update). 200 OK with updated entity or 404 Not Found.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Recipe> updateRecipe(@PathVariable long id, @RequestBody RecipeRequest updatedRequest) {
        MDC.put("recipeName", updatedRequest.getTitle());
        logger.info("UPDATE /api/{} - Updating recipe at id {}", id, id);
        logger.debug("Updating recipe at id {} Title: {}, Type: {}", id, updatedRequest.getTitle(), updatedRequest.getType());
        Recipe updatedRecipe = RecipeFactory.createFromRequest(updatedRequest);

        return recipeService.updateRecipe(id, updatedRecipe)
                .map(recipe -> {
                    logger.info("Updated recipe {} successfully", id);
                    return ResponseEntity.ok(recipe);
                })
                .orElseGet(() -> {
                    logger.warn("Updated recipe {} - No recipe found with id {}", id, id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Partial update. 200 OK with updated entity or 404 Not Found.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Recipe> patchRecipe(@PathVariable long id, @RequestBody RecipeRequest partialRequest) {
        logger.info("PATCH /api/{} - Patching recipe at id {}", id, id);
        logger.debug("Patching recipe at id {}, Name: {}, Type: {}", id, partialRequest.getTitle(), partialRequest.getType());
        Recipe partialRecipe = RecipeFactory.createFromRequest(partialRequest);
        return recipeService.patchRecipe(id, partialRecipe)
                .map(recipe -> {
                    logger.info("Patched recipe {} successfully", id);
                    return ResponseEntity.ok(recipe);
                })
                .orElseGet(() -> {
                    logger.warn("Patched recipe {} - No recipe found with id {}", id, id);
                    return ResponseEntity.notFound().build();
                });
    }
}
