## Lab 5 - Image Generation with ZhiPuAI

In this exercise you will create a simple Spring Boot application which can make calls to ZhiPuAI to generate images.  We will need an account on ZhiPuAI, then we will add code to a half-completed project structure.  Our code will create a client object for making image generation API calls, and as usual we will make sure we have test coverage.  Let's jump in.

Within the project code, you will find comments containing **TODO** instructions.  These instructions match the order of instructions in this lab document.  If you find it easier, you can complete this lab by following the embedded **TODO** comments; just make sure you follow them in order.

Be aware that [ZhiPu AI](https://open.bigmodel.cn/) is based in China.  When working with the API, error messages are presented in Chinese symbols which you will need to translate (try Google Translate) unless you are fluent in the language.  Token purchases cannot be made using standard payment techniques such as credit cards.

Estimated time: 25 minutes.


---
**Part 1 - Setup ZhiPuAI Account**

If you have not already done so, setup an account with ZhiPuAI.  The instructions are in the **[Lab Setup guide](https://github.com/kennyk65/AI-With-Spring-Student-Files/blob/main/LabInstructions/Lab%20Setup.md)**.  When you are finished, you should have an environment variable established with your API key.
* Restart your IDE after setting environment variables.


---
**Part 2 - Setup the Project**

A half-completed skeleton project has been created for you to relieve you from the image generation steps.  The instructions below are for VS Code. If you wish to use IntilliJ or Eclipse and need assistance, see the "IDE Tips" document if you need help.

1. From within VS Code, use the File menu and select "Open Folder". Select the _/student-files/lab05-images-zhipuai_ folder.  
    * See the **IDE Tips** document to troubleshoot any issues when opening this project.

1. **TODO-01**: Open the **pom.xml** file.  Notice that the  `spring-ai-zhipuai-spring-boot-starter` dependency has already been added.

1. **TODO-02**: Open `src/main/resources/application.yml` and establish the following entries:
    ```
    spring:
      application.name: Lab3 Images ZhiPuAI
      main.web-application-type: none     # Do not start a web server.

      ai:
        retry:
          max-attempts: 1           # Maximum number of retry attempts.
        model.image: zhipuai
    ```
    * SpringAI applications can run as part of a web application, but these exercises are built to avoid that extra step.
    * The retry* settings will override the `*ImageModel`'s default settings.  You are likely to experience errors while you learn the API's usage, and we don't want you to experience unnecessary expenses.  
    * Note that ZhiPu AI has far fewer image settings than competing models; this may change over time.
    
1. **TODO-03** Open the main application class in `src/main/java/com/example/Application.java`.  Run the application. We expect the application to start, then stop, without errors.  If you have any errors related to tooling, be sure to address them now before proceeding.


---
**Part 3 - Build an Image Generation Client**

Now we can create a client object to make the image generation API calls. 

5. **TODO-04**: Open `src/main/java/com/example/client/ZhiPuAIClient.java`. Establish the class as a Spring bean with the `@Component`annotation.  

    ```
    @Component
    public class ZhiPuAIAIClient implements AIClient {
    ```

1. **TODO-05**: Define a private final field named "model" of type `ImageModel`.  Define a constructor which dependency injects this field:

    ```
    private final ImageModel model;

    public StabilityAIClient(ImageModel model) {
        this.model = model;
    }
    ```

1. **TODO-06:** Within the `createImageUrl()` method, create a local variable of type `ImagePrompt` named _prompt_.  Use the `request` parameter and `DEFAULT_OPTIONS` object when defining the prompt.  Later you can experiment by creating your own custom-defined `ZhiPuAiImageOptions`.
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

1. **TODO-08:** Find the `return null;` line at the bottom of the method and remove it.  Find and return the base-64 encoded String in the `ImageResponse` returned from the previous call.  The String is nested within the `getResult()` and `getOutput()` methods.  

    ```
    return response.getResult().getOutput().getB64Json();
    ```
    * The API returns the image as a base-64 encoded value within a JSON document in the HTTP response.  The model parses the JSON response for us and provides the base64 encoded value.  Perhaps the method should have been named `getB64()`.  
1. Supply any imports needed to make the code compile.  Save your work.

---
**Part 4 - Create a `@Test` class**

Anything we code, we should test.  We will make a `@Test` method to ensure our Client object works as expected.

11. **TODO-09**: Open `src/test/java/com/example/client/ZhiPuAIClientTests.java`. Use `@SpringBootTest` to define this test class as a Spring Boot test.  

    ```
    @SpringBootTest
    public class ZhiPuAIClientTests {
    ```

1. **TODO-10:** Use the `@Autowired` annotation to inject an instance of `AIClient` into this test class.

    ```
    @Autowired AIClient client;
    ```

1. **TODO-11:** Define a new test method to test the client's `createImageB64()` method.  Pass in a string such as _"Crescent moon over the Shanghai skyline."_  Use `assertThat()` to ensure that the returned String is not blank.  Use the provided `testValidBase64Image()` method to check that the returned string appears to be a valid image, then save the image by calling `saveBase64Image()`. (Note that `Assertions.*` and `Utilities.*` are already statically imported for you.)

    ```
    @Test
    public void testCreateImageB64() {
        String imageB64 = client.createImageB64("Crescent moon over the Shanghai skyline.");
        assertThat(imageB64).isNotBlank();
        testValidBase64Image(imageB64);
        saveBase64Image(imageB64, "lab-image.png");
    }

    ```
1. Organize your imports and save your work.  Run this test.  Expect it to take a few moments to run, and it should pass.

1. The newly generated image should be found in the root of your project (you can provide a different file path to the `save*` method if you want).  Open the file in any image viewer / web browser and admire your handiwork!

---
**Part 5 - Summary**

In this lab you have integrated with ZhiPuAI's hosted models from your own Spring Boot application and used it to generate images using the _cogview-3_ model.  Congratulations! 

Feel free to experiment with the settings in this lab, but **be warned!** You can spend a LOT of money playing with AI image generation!
