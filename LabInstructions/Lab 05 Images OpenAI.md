## Lab 5 - Image Generation with OpenAI

In this exercise you will create a simple Spring Boot application which can use OpenAI to generate images.  We will need an account on OpenAI, then we will add code to a half-completed project structure.  Our code will create a client object for making image generation API calls, and as usual we will make sure we have test coverage.  Let's jump in.

Within the project code, you will find comments containing **TODO** instructions.  These instructions match the order of instructions in this lab document.  If you find it easier, you can complete this lab by following the embedded **TODO** comments; just make sure you follow them in order.

Estimated time: 35 minutes.


---
**Part 1 - Signup for OpenAI Account**

If you have not already done so, setup an account with OpenAI.  The instructions are in the **[Lab Setup guide](https://github.com/kennyk65/AI-With-Spring-Student-Files/blob/main/LabInstructions/Lab%20Setup.md)**. 

---
**Part 2 - Setup the Project**

A half-completed skeleton project has been created for you to relieve you from the image generation steps.  The instructions below are for VS Code. If you wish to use IntilliJ or Eclipse and need assistance, see the "IDE Tips" document if you need help.

1. From within VS Code, use the File menu and select "Open Folder". Select the _/student-files/lab05-images-openai_ folder.  
    * See the **IDE Tips** document to troubleshoot any issues when opening this project.


1. **TODO-01**: Open the **pom.xml** file.  Notice that the  `spring-ai-starter-model-openai` dependency has already been added.

1. **TODO-02**: Open `src/main/resources/application.yml` and establish the following entries:
    ```
    spring:
      application.name: Lab3 Images OpenAI
      main.web-application-type: none     # Do not start a web server.

    ai:
      retry:
        max-attempts: 1           # Maximum number of retry attempts.
      model.image: openai
      openai:
        image:
          options:
            n: 1                  # number of images to generate.
            response_format: url  # or b64_json
            size: 1792x1024       # or 1024x1024, 1024x1792
            style: natural        # or vivid

    ```
    * SpringAI applications can run as part of a web application, but these exercises are built to avoid that extra step.
    * The retry* settings will override the `*ImageModel`'s default settings.  You are likely to experience errors while you learn the API's usage, and we don't want you to experience unnecessary expenses.  
    * You can experiment with some of the image settings later.  Remember that larger, higher-quality images incur greater costs.  

1. **TODO-03** Open the main application class in `src/main/java/com/example/Application.java`.  Run the application. We expect the application to start, then stop, without errors.  If you have any errors related to tooling, be sure to address them now before proceeding.


---
**Part 3 - Build an Image Generation Client**

Now we can create a client object to make the image generation API calls. 

5. **TODO-04**: Open `src/main/java/com/example/client/OpenAIClient.java`. Establish the class as a Spring bean with the `@Component`annotation.  Provide an explicit bean name of "openAIClientBean" to avoid naming conflicts with internal SpringAI bean names.  

    ```
    @Component("openAIClientBean")
    public class OpenAIClient implements AIClient {
    ```

1. **TODO-05**: Define a private final field named "model" of type `ImageModel`.  Define a constructor which dependency injects this field:

    ```
    private final ImageModel model;

    public OpenAIClient(ImageModel model) {
        this.model = model;
    }
    ```

1. **TODO-06:** Within the `createImageUrl()` method, create a local variable of type `ImagePrompt` named _prompt_.  Use the `request` parameter and `DEFAULT_OPTIONS` object when defining the prompt.  Later you can experiment by creating your own custom-defined `OpenAiImageOptions`.
    * The `*ImageOptions` default values are set by Spring's `Environment` abstraction, in our case `application.yml` and environment variables.  Values set within code override these settings.

    ```
    ImagePrompt prompt = new ImagePrompt(
        request,
        DEFAULT_OPTIONS
    );
    ```

1. **TODO-07:** Use the dependency-injected `model` object to create an image. Define a local variable of type `ImageResponse` and set it by calling `model.call()`, passing the prompt you've just created.

    ```
    ImageResponse response = model.call(prompt);
    ```

1. **TODO-08:** Find the `return null;` line at the bottom of the method and remove it.  Find and return the URL String in the `ImageResponse` returned from the previous call.  The URL is nested within the `getResult()` and `getOutput()` methods.  

    ```
    return response.getResult().getOutput().getUrl();
    ```

1. Supply any imports needed to make the code compile.  Save your work.

---
**Part 4 - Create a `@Test` class**

Anything we code, we should test.  We will make a `@Test` method to ensure our Client object works as expected.

11. **TODO-09**: Open `src/test/java/com/example/client/OpenAIClientTests.java`. Use `@SpringBootTest` to define this test class as a Spring Boot test.  

    ```
    @SpringBootTest
    public class OpenAIClientTests {
    ```

1. **TODO-10:** Use the `@Autowired` annotation to inject an instance of `AIClient` into this test class.

    ```
    @Autowired AIClient client;
    ```

1. **TODO-11:** Define a `@Test` method.  Have it call the `client.createImageUrl()` method of the client, passing in a string such as _"Two golden retrievers playing tug-o-war in the snow."_ Use AssertJ's `assertThat()` method to ensure that the returned URL is not blank.  Print the URL that is returned.  (Note that `Assertions.*` is already statically imported for you.)

    ```
    @Test
    public void testCreateImageUrl() {
        String url = client.createImageUrl("Two golden retrievers playing tug-o-war in the snow.");
        assertThat(url).isNotBlank();
        System.out.println("URL: " + url);
    }
    ```
1. Supply any imports needed to make the code compile.  Save your work. Run this test.  Expect it to take a few moments to run, and it should pass.  Find the URL of the generated image and open it in a browser to see the result.

---
**Part 5 (Optional) - Save image to a downloaded file**

The previous solution resulted in an image hosted on a public URL.  Because we may wish to keep our image private, or to save it to a file, we need to provide an alternate solution that returns the image itself rather than a URL.

15. Return to `src/main/java/com/example/client/OpenAIClient.java`.

1. **TODO-13 (Optional):** Within the `createImageB64` method, create an `OpenAiImageOptions` object named `B64_OPTIONS`.  Use the builder pattern to set `responseFormat()` to **b64_json**.

    ```
    OpenAiImageOptions B64_OPTIONS = 
        OpenAiImageOptions.builder()
            .withResponseFormat("b64_json").build();
    ```

1. **TODO-14 (Optional):**  Similar to your earlier code, create an
`ImagePrompt` object using the `request` parameter and the `B64_OPTIONS`.

    ```
    ImagePrompt prompt = new ImagePrompt(
            request,
            B64_OPTIONS
    );
    ```
1. **TODO-15 (Optional):** Using your earlier code as a guide, create a variable of type `ImageResponse`.  Populate it by calling `model.call()`, passing the prompt you've just created.

    ```
    ImageResponse response = model.call(prompt);
    ```

1. **TODO-16 (Optional):** Remove the `return null;` line.  Using your earlier code as a guide, return the base-64 encoded String from the previous call.  Organize your imports and save your work.

    ```
    return response.getResult().getOutput().getB64Json();
    ```
    * The API returns the image as a base-64 encoded value within a JSON document in the HTTP response.  The model parses the JSON response for us and provides the base64 encoded value.  Perhaps the method should have been named `getB64()`.  

---
**Part 6 (Optional) - `@Test` the new method**

Finally let's add some code to test if the returned Base-64 image is an actual file.

20. Return to `src/test/java/com/example/client/OpenAIClientTests.java`.

1. **TODO-17 (Optional):** Using your earlier `@Test` method as an example, define a new test method to test the client's `createImageB64()` method.  Pass in whatever image description you like.  As before, use `assertThat()` to ensure that the returned String is not blank.  Use the provided `testValidBase64Image()` method to check that the returned string appears to be a valid image, then save the image by calling `saveBase64Image()`. (Note that `Assertions.*` and `Utilities.*` are already statically imported for you.)

    ```
    @Test
    public void testCreateImageB64() {
        String imageB64 = client.createImageB64("Two golden retrievers playing tug-o-war in the snow.");
        assertThat(imageB64).isNotBlank();
        testValidBase64Image(imageB64);
        saveBase64Image(imageB64, "image.png");
    }
    ```
    * Note: The `test*()` and `save*()` methods belong to the `Utilities` class. It is a statically imported helper class containing methods to make lab testing easier.  It is not part of SpringAI or any AI model.

1. Organize your imports and save your work.  Run this test.  Expect it to take a few moments to run, and it should pass.

1. The newly generated image should be found in the root of your project (you can provide a different file path to the `save*` method if you want).  Open the file in any image viewer / web browser and admire your handiwork!


---
**Part 7 - Summary**

In this lab we used OpenAI's DALL-E model for image generation.

Feel free to experiment with the settings in this lab, but **be warned!** You can spend a LOT of money playing with AI image generation!
