package com.mcon152.recipeshare;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcon152.recipeshare.web.RecipeController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.web.servlet.function.RequestPredicates.contentType;

@WebMvcTest(RecipeController.class)
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = new ObjectMapper();
    }

    // Internal class for creation-related tests
    @Nested
    class CreationTests {
        @Test
        void testAddRecipe() throws Exception {

            ObjectNode json = mapper.createObjectNode();
            json.put("title", "Cake");
            json.put("description", "Delicious cake");
            // Change ingredients to a single String
            json.put("ingredients", "1 cup of flour, 1 cup of sugar, 3 eggs");
            json.put("instructions", "Mix and bake");
            String jsonString = mapper.writeValueAsString(json);
            mockMvc.perform(post("/api/recipes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Cake"))
                    .andExpect(jsonPath("$.description").value("Delicious cake"))
                    .andExpect(jsonPath("$.ingredients").value("1 cup of flour, 1 cup of sugar, 3 eggs"))
                    .andExpect(jsonPath("$.instructions").value("Mix and bake"))
                    .andExpect(jsonPath("$.id").isNumber());
        }

        @ParameterizedTest
        @CsvSource({
                "'Chocolate Cake','Rich chocolate cake','2 cups flour;1 cup cocoa;4 eggs','Bake at 350F for 30 min'",
                "'Pasta Salad','Fresh pasta salad','200g pasta;100g tomatoes;50g olives','Mix all ingredients'",
                "'Pancakes','Fluffy pancakes','1 cup flour;2 eggs;1 cup milk','Cook on skillet until golden'"
        })
        void parameterizedAddRecipeTest(String title, String description, String ingredients, String instructions) throws Exception {
            throw new UnsupportedOperationException("parameterizedAddRecipeTest");
        }
    }

    // Internal class for delete and get tests
    @Nested
    class DeleteAndGetTests {
        private List<Integer> recipeIds;

        @BeforeEach
        void createRecipes() throws Exception {
            recipeIds = new ArrayList<>();
            String[] recipes = {
                    "{\"title\":\"Pie\",\"description\":\"Apple pie\",\"ingredients\":\"Apples, Flour, Sugar\",\"instructions\":\"Mix and bake\"}",
                    "{\"title\":\"Soup\",\"description\":\"Tomato soup\",\"ingredients\":\"Tomatoes, Water, Salt\",\"instructions\":\"Boil and blend\"}"
            };
            for (String json : recipes) {
                String response = mockMvc.perform(post("/api/recipes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();
                int id = mapper.readTree(response).get("id").asInt();
                recipeIds.add(id);
            }
        }

        @Test
        void testGetAllRecipes() throws Exception {
            mockMvc.perform(get("/api/recipes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("Pie"))
                    .andExpect(jsonPath("$[1].title").value("Soup"));
        }

        @Test
        void testGetRecipe() throws Exception {
            int id = recipeIds.get(0);
            mockMvc.perform(get("/api/recipes/" + id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Pie"));
        }

        @Test
        void testDeleteRecipe() throws Exception{
            int id = recipeIds.get(0);
            mockMvc.perform(delete("/api/recipes/" + id))
                    .andExpect(status().isOk());
        }

        @Test
        void testPutRecipe()throws Exception {

            int id = recipeIds.get(0);
            String updatedRecipe = """
                        {
                        "id": "0",
                        "title": "Cake",
                        "description": "Delicious cake",
                        "ingredients": "1 cup of flour, 1 cup of sugar, 3 eggs",
                        "instructions": "Mix and bake"
                    }""";

            mockMvc.perform(put("/api/recipes/" + id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updatedRecipe))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Cake"))
                    .andExpect(jsonPath("$.description").value("Delicious cake"))
                    .andExpect(jsonPath("$.ingredients").value("1 cup of flour, 1 cup of sugar, 3 eggs"))
                    .andExpect(jsonPath("$.instructions").value("Mix and bake"))
                    .andExpect(jsonPath("$.id").isNumber());
        }
        @Test
        void testPatchRecipe() throws Exception {
            int id = recipeIds.get(0);
            String patchedRecipe = """
                        {
                        "description": "Delicious and scrumptious cake"
                    }""";

            mockMvc.perform(patch("/api/recipes/" + id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(patchedRecipe))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description").value("Delicious and scrumptious cake"));
        }
    }

    //Because in recipe controller I returned null it still returns 200 ok but content is null
    @Nested
    class NonExistingRecipeTests {

        @Test
        void testGetNonExistingRecipe() throws Exception {
           mockMvc.perform(get("/api/recipes/9999"))
                   .andExpect(status().isOk()).
                   andExpect(content().string(""));
        }

        @Test
        void testPutNonExistingRecipe() throws Exception {
            String updatedRecipe = """
                        {
                        "id": "12",
                        "title": "Cake",
                        "description": "Delicious cake",
                        "ingredients": "1 cup of flour, 1 cup of sugar, 3 eggs",
                        "instructions": "Mix and bake"
                    }""";
            mockMvc.perform(put("/api/recipes/9999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updatedRecipe))
                    .andExpect(status().isOk()).andExpect(content().string(""));
        }

        @Test
        void testPatchNonExistingRecipe() throws Exception {
            String update = """
            {
                    "description": "tasty"
            }""";

            mockMvc.perform(patch("/api/recipes/9999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(update))
        .andExpect(status().isOk()).andExpect(content().string(""));
        }

        @Test
        void testDeleteNonExistingRecipe() throws Exception {
            mockMvc.perform(delete("/api/recipes/9999" ))
                    .andExpect(status().isOk())
                            .andExpect(content().string("false"));
        }
    }



}