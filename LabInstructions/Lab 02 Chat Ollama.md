## Lab 2 - Chat with Ollama

In this exercise you will create a simple Spring Boot application which can call chat models hosted in locally in Ollama.

Within the codebase you will find ordered *TODO* comments that describe what actions to take for each step.  By finding all files containing *TODO*, and working on the steps in numeric order, it is possible to complete this exercise without looking at the instructions.  If you do need guidance, you can always look at these instructions for full information.  Just be sure to perform the *TODO* steps in order.

Solution code is provided for you in a separate folder, so you can compare your work if needed.  For maximum benefit, try to complete the lab without looking at the solution.

Let's jump in.

---
**Part 1 - Setup the Project**

The instructions below are for VS Code. If you wish to use IntelliJ or Eclipse and need assistance, see the "IDE Tips" document.

1. Open the _/student-files/lab02-chat-ollama_ folder.  
    * Give the VSCode a moment to initialize its workspace, especially if this is the first time importing a Gradle/Maven project.
    * If you see a message about enabling null analysis for the project, you can select either enable or disable.
    * If you see a message about installing _Extension Pack for Java_, take the install option.
    * If you see a message _Do you trust the authors of the files in this folder_, check the checkbox and click the "trust" button.

    The project should be free of compiler errors at this point.  Address any issues you see before continuing.

2. Find the TODO instructions.

    * **Using VS Code:** Select "Edit" / "Find in Files..." and search for "TODO".
    * **Using IntelliJ:**  Select "Edit" / "Find" / "Find in Files..." and search for "TODO".  Use the "Open in Find Window" button to place the results in a tab for easy navigation.
    * **Using Eclipse:** Select "Window" / "Show View" / "Tasks".  Use the "Search" view to search for "TODO".  Find a filter icon on the top of this tab.  Check "Show all items", "TODOs".  For scope select "on elements in the selected projects".  The list of TODOs will appear in order!

    IMPORTANT: Work through the TODO instructions in order!   


---
**Part 2 - Complete Ollama Local Setup**

3. **TODO-01**:  Complete the instructions for running Ollama in your local environment.  See the "Setup Process for Docker" and "Setup Process for Ollama" section in the **[Lab Setup guide](https://github.com/kennyk65/AI-With-Spring-Student-Files/blob/main/LabInstructions/Lab%20Setup.md)**.  

    Make sure your Ollama container is running.  For this lab we will use the **mistral** model, but your can also try **llama2**.

---
**Part 3 - Initial Configuration and Test**

4. **TODO-02**: Open the **pom.xml** file.  Add the dependency for **Ollama**.  The groupId value will be `org.springframework.ai`.  The artifactId will be `spring-ai-ollama-spring-boot-starter`.  Save your work.

```
<dependency>
	<groupId>org.springframework.ai</groupId>
	<artifactId>spring-ai-ollama-spring-boot-starter</artifactId>
</dependency>
```
- Note: Some IDEs may require you to "refresh" your project after altering dependencies in Maven/Gradle.

5.  **TODO-03**: Open the main application class at `src/main/java/com/example/Application.java`.  Run the application.

    * **VS Code**: Right-click, Run Java.  Or open the file and click the “Run” option hovering over the main() method.
    * **IntelliJ**: Right-click, select “Run ‘Application.main()’”. 
    * **Eclipse**: Right-click, Select Run As / Java Application.

    We expect the application to start, then stop, without errors.  If you have any errors related to tooling, be sure to address them now before proceeding.

1.  **TODO-04**: Open the `src/main/resources/application.yml` file.  Establish the following entries:

```
spring:
  application.name: Lab02 Chat Ollama
  main.web-application-type: none     # Do not start a web server.
  ai:
    retry:
      max-attempts: 1           # Maximum number of retry attempts.
    ollama:
      base-url: http://localhost:11434  # Default base URL when you run Ollama from Docker
      chat:
        enabled: true
        options:
          model: mistral

```
  * The `base-url` setting assumes a locally running Docker container.  You can omit this if you have not altered any default settings.
  * The `model` setting should align with the Ollama model which you have pulled and run.  You may need to switch this to "llama2" or another model.
  * The `spring.ai.ollama.chat.enabled` setting tells Spring Boot to specifically autoconfigure objects supporting Ollama.
  * SpringAI applications can run as part of a web application, but these exercises are built to avoid that extra step.
  * Note: The retry settings will override the `ChatClient`'s default settings.  You are likely to experience errors while you learn the API's usage, and we don't want you to experience unnecessary delays.
  
7.  Save your work.  

---
**Part 4 - Try Spring AI's `ChatClient`**

Now we can use the ChatClient to make API calls to the Ollama-hosted models.

8. **TODO-05**:  Open `src/main/java/com/example/client/AIClientImpl.java`.  
    - Use a stereotype annotation to mark this class as a Spring bean.  

```
@Component
public class AIClientImpl implements AIClient {
```

9. **TODO-06**: Create a constructor for this bean.  
    - Inject a ChatModel object into the constructor.  
    - Pass the model to the ChatClient.builder to build a ChatClient object.  Save the ChatClient object in the client field.
        - The `ChatClient.Builder` is automatically created by auto-configuration when it sees the Ollama dependency on the classpath, and Spring will automatically inject it into your constructor on bean creation.  

```
    public AIClientImpl(ChatModel model) {
        client = ChatClient.builder(model).build();
    }
```

10. **TODO-07**:  Within the `callApi()` method, use the `client` object to call the API.
    *  Use the `.prompt()` method to create a prompt to pass to the Model
    *  Use `.user()` to set the "user" message. Pass the input String parameter.
    *  Use `.call()` to invoke the model.  It returns a CallResponse.
    *  Use `.content()` as a simple means of extracting String content from the response. 
    *  Have the method return the content of the response.
```
        return 
            client
                .prompt()
                .user(input)
                .call()
                .content();
```

12. Supply any imports needed to make the code compile.
    * **VS Code**: Type Alt-Shift-O.
    * **IntelliJ**: Type Ctrl-Alt-O.
    * **Eclipse**: Type Ctrl-Shift-O.
1. Save your work.

---
**Part 5 - Create a `@Test` class**

Anything we code, we should test.  We will make a `@Test` class to ensure our Client object works as expected.

13. **TODO-08**:  Open `src/test/java/com/example/client/OllamaClientTests.java`.  Add an annotation to define the class as a Spring Boot test.  Annotate the class with the `@ActiveProfiles` annotation to activate the **ollama** profile.

```
@SpringBootTest
@ActiveProfiles("ollama")
public class OllamaClientTests {
```

14. **TODO-09:** Use the `@Autowired` annotation to inject an instance of our `AIClient`.
    - The class we worked on in the last section implements `AIClient`, so we can inject the dependency by its interface.

```
    @Autowired AIClient client;
```

15.  **TODO-10:** Define a `@Test` method to test the `callApi()` method of the client.

  * Pass in a string that describes the response you wish to generate, such as the `samplePrompt` String defined in code above, or an equivalent.
  * Use AssertJ's `Assertions.assertThat()` method to ensure that the content is not null.
  * Use AssertJ's `Assertions.assertThat()` method to ensure that the content contains expected results.
  * Print the response string that is returned.

```
	@Test
	void callApiTest() {

        String response =
            client.callApi(samplePrompt);

        assertThat(response).isNotNull();
        assertThat(response).contains(sampleResults);
    
		//	Print the results
		System.out.println("The results of the call are: " + response);
    }
```
16. **TODO-11:**  Organize your imports and save your work.  Run this test, it should pass.  Check the results in the console.

    * The test should run and produce a list of the five Great Lakes.  It is also very possible that you will encounter an error here, possibly due to setup, but also due to changes in Ollama since the time of this writing.  Here are some troubleshooting tips.
        * The Ollama Docker container must be running.
        * Ollama must be running the model matching the model specified in application.yml
        * If you have a compilation issue, be sure you have organized imports.  Compare your code to the solution code.


**Part 6 - Summary**

At this point, you have integrated with a local Ollama hosted model from your own Spring Boot application.  Congratulations! 

