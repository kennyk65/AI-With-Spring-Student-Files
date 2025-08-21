## Lab 5 - Image Generation with Azure OpenAI

In this exercise you will create a simple Spring Boot application which can make calls to Azure OpenAI to generate images.  We will begin by establishing an Azure account and permission to use Azure Open AI, which you may have from earlier labs.  Next we will open a half-completed project, create a client object for making image generation API calls, and as usual we will make sure we have test coverage.  Let's jump in.

Within the project code, you will find comments containing **TODO** instructions.  These instructions match the order of instructions in this lab document.  If you find it easier, you can complete this lab by following the embedded **TODO** comments; just make sure you follow them in order.

Estimated time: 35 minutes.


---
**Part 1 - Setup Azure Deployment**

If you have not already done so, Establish an Azure account.  Follow the instructions in the **[Lab Setup guide](https://github.com/kennyk65/AI-With-Spring-Student-Files/blob/main/LabInstructions/Lab%20Setup.md)** and find the _Signup Process for Azure OpenAI_ section.  Walk through these instructions to establish an Azure Account, OpenAI _resource_, Endpoint, Keys, and Deployment. Be sure that you have deployed the **dall-e-3** text-to-image model.

---
**Part 2 - Setup the Project**

1. From within VS Code, open the _/student-files/lab05-images-azure_ folder.  

1. **TODO-01**: Open the **pom.xml** file.  Notice that the  `spring-ai-starter-model-azure-openai` dependency has already been added.

1. **TODO-02**: Open `src/main/resources/application.yml` and establish the following entries:
    ```
    spring:
      application.name: Lab3 Images Azure
      main.web-application-type: none     # Do not start a web server.

      ai:
        retry:
          max-attempts: 1           # Maximum number of retry attempts.
        model.image: azure-openai
        azure:
          openai:
            endpoint: ENDPOINT-GOES-HERE
            image:
              options:
                deployment-name: DEPLOYMENT-NAME-GOES-HERE
                n: 1                  # number of images to generate.
                response_format: url  # or b64_json
                size: 1792x1024       # or 1024x1024, 1024x1792
                style: natural        # or vivid
    ```
    * SpringAI applications can run as part of a web application, but these exercises are built to avoid that extra step.
    * The retry* settings will override the `*ImageModel`'s default settings.  You are likely to experience errors while you learn the API's usage, and we don't want you to experience unnecessary expenses.  
    * If you forgot your _endpoint_, the value can be found in the [Azure Portal](https://portal.azure.com/) / _Azure OpenAI_. Open your resource and select _click here to view endpoints_.
    * The deployment name should match the model deployment name from the setup instructions, such as **dall-e-3** or a similar image generation model.
    * You can experiment with some of the image settings later.  Remember that larger, higher-quality images incur greater costs.  

1. **TODO-03** Open the main application class in `src/main/java/com/example/Application.java`.  Run the application. We expect the application to start, then stop, without errors.  If you have any errors related to tooling, be sure to address them now before proceeding.


---
**Part 3 - Build an Image Generation Client**

Now we can create a client object to make the image generation API calls. 

5. **TODO-04**: Open `src/main/java/com/example/client/AzureClient.java`. Establish the class as a Spring bean with the `@Component`annotation.  

    ```
    @Component
    public class AzureClient implements AIClient {
    ```

1. **TODO-05**: Define a private final field named "model" of type `ImageModel`.  Define a constructor which dependency injects this field:

    ```
    private final ImageModel model;

    public AzureClient(ImageModel model) {
        this.model = model;
    }
    ```

1. **TODO-06:** Within the `createImageUrl()` method, create a local variable of type `ImagePrompt` named _prompt_.  Use the `request` parameter and `DEFAULT_OPTIONS` object when defining the prompt.  Later you can experiment by creating your own custom-defined `AzureOpenAiImageOptions`.
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

11. **TODO-09**: Open `src/test/java/com/example/client/AzureClientTests.java`. Use `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)` to define this test class as a Spring Boot test without a web environment.  

    ```
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
    public class AzureClientTests {
    ```

1. **TODO-10:** Use the `@Autowired` annotation to inject an instance of `AIClient` into this test class.

    ```
    @Autowired AIClient client;
    ```

1. **TODO-11:** Define a `@Test` method.  Have it call the `client.createImageUrl()` method of the client, passing in a string such as _"A hand-glider soaring over Yosemite valley on a bright summer day."_ Use AssertJ's `assertThat()` method to ensure that the returned URL is not blank.  Print the URL that is returned.  (Note that `Assertions.*` is already statically imported for you.)

    ```
    @Test
    public void testCreateImageUrl() {
        String url = client.createImageUrl("A hand-glider soaring over Yosemite valley on a bright summer day.");
        assertThat(url).isNotBlank();
        System.out.println("URL: " + url);
    }
    ```
1. Supply any imports needed to make the code compile.  Save your work. Run this test.  Expect it to take a few moments to run, and it should pass.  Find the URL of the generated image and open it in a browser to see the result.

---
**Part 5 (Optional) - Save image to a downloaded file**

The previous solution resulted in an image hosted on a public URL.  Because we may wish to keep our image private, or to save it to a file, we need to provide an alternate solution that returns the image itself rather than a URL.

15. Return to `src/main/java/com/example/client/AzureClient.java`.

1. **TODO-13 (Optional):** Find the `createImageB64` method.  Within it, create an `AzureOpenAiImageOptions` object named `B64_OPTIONS`.  Use the builder pattern to set `responseFormat()` to **b64_json**.

    ```
    AzureOpenAiImageOptions B64_OPTIONS = 
        AzureOpenAiImageOptions.builder()
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

20. Return to `src/test/java/com/example/client/AzureClientTests.java`.

1. **TODO-17 (Optional):** Using your earlier `@Test` method as an example, define a new test method to test the client's `createImageB64()` method.  Pass in whatever image description you like.  As before, use `assertThat()` to ensure that the returned String is not blank.  Use the provided `testValidBase64Image()` method to check that the returned string appears to be a valid image, then save the image by calling `saveBase64Image()`. (Note that `Assertions.*` and `Utilities.*` are already statically imported for you.)

    ```
    @Test
    public void testCreateImageB64() {
        String imageB64 = client.createImageB64("A hand-glider soaring over Yosemite valley on a bright summer day.");
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

In this lab you have integrated with Azure's OpenAI hosted models from your own Spring Boot application and used it to generate images using the DALL-E-3 model.  If your were running a Spring Boot application on an Azure VM within a private network, this would be a natural combination to use rather than calling OpenAI directly.  Congratulations! 

Feel free to experiment with the settings in this lab, but **be warned!** You can spend a LOT of money playing with AI image generation!
