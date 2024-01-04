package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;

import static org.example.utils.GeneratorId.provideId;


@RestController
public class PetController {
    private final DynamicCodeExecutor codeExecutor;
    private final OpenAIService openAIService;

    @Autowired
    public PetController(OpenAIService openAIService, DynamicCodeExecutor codeExecutor) {
        this.openAIService = openAIService;
        this.codeExecutor = codeExecutor;
    }

    String getFieldsInfo() {
        Class<Pet> petClass = Pet.class;

        StringBuilder sb = new StringBuilder();
        Field[] fields = petClass.getDeclaredFields();

        for (Field field : fields) {
            sb.append("Field: ").append(field.getName())
                    .append(", Type: ").append(field.getType().getSimpleName());
        }

        return sb.toString();
    }

    @GetMapping("/api/pets")
    public String getAllPets() {
        String prompt = "Create a JSON response with a key 'java_code'. The value should be a Java class named 'DynamicClass' with a public method 'execute' that fetches all pets from a PostgreSQL database with address localhost:5433 name adoptpethd and table pet and returns the result in JSON format. Pet has following fields: " + getFieldsInfo() + "The class should be compilable and follow best coding practices.";

        try {
            String generatedCode = openAIService.getGeneratedCode(prompt);
            return codeExecutor.executeGeneratedCode(generatedCode);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error in generating or executing code";
        }
    }

    @PostMapping("/api/pets")
    public String createPet(@RequestParam String name, @RequestParam int age, @RequestParam String description) {
        Long id = provideId();
        String prompt = String.format(
                "Return a JSON object with a key 'java_code' containing a compilable Java class named 'DynamicClass' with a public method named 'execute' that creates a new pet with id %d, name '%s', age %d, and description '%s' in the PostgreSQL database with address localhost:5433 name adoptpethd and table pet. Provide only the Java source code without any additional explanations or comments. The class should be compilable and should follow best coding practices.",
                id, name, age, description
        );

        try {
            String generatedCode = openAIService.getGeneratedCode(prompt);
            return codeExecutor.executeGeneratedCode(generatedCode);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error in generating or executing code";
        }
    }

    @DeleteMapping("/api/pets/{id}")
    public String deletePet(@PathVariable Long id) {
        String prompt = String.format(
                "Return a JSON object with a key 'java_code' containing a compilable Java class named 'DynamicClass' with a public method named 'execute' that deletes pet with id %d in the PostgreSQL database with address localhost:5433 name adoptpethd and table pet. Provide only the Java source code without any additional explanations or comments. The class should be compilable and should follow best coding practices.",
                id
        );
        try {
            String generatedCode = openAIService.getGeneratedCode(prompt);
            return codeExecutor.executeGeneratedCode(generatedCode);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error in generating or executing code";
        }
    }
}
