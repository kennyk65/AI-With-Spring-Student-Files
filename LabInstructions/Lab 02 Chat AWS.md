## Lab 2 - Chat with AWS Bedrock

In this exercise you will create a simple Spring Boot application which can call chat models hosted in Amazon Bedrock.  Bedrock is a managed service which gives us access to many different chat and image generation [models](https://docs.aws.amazon.com/bedrock/latest/userguide/model-ids.html#model-ids-arns).  

Within the codebase you will find ordered *TODO* comments that describe what actions to take for each step.  By finding all files containing *TODO*, and working on the steps in numeric order, it is possible to complete this exercise without looking at the instructions.  If you do need guidance, you can always look at these instructions for full information.  Just be sure to perform the *TODO* steps in order.

Solution code is provided for you in a separate folder, so you can compare your work if needed.  For maximum benefit, try to complete the lab without looking at the solution.

Let's jump in.  

Estimated time: 20 minutes.

---
**Part 1 - Setup the Project**

The instructions below are for VS Code. If you wish to use IntelliJ or Eclipse and need assistance, see the "IDE Tips" document.

2. Open the _/student-files/lab02-chat-aws_ folder.  
    * Give the VSCode a moment to initialize its workspace, especially if this is the first time importing a Gradle/Maven project.
    * If you see a message about enabling null analysis for the project, you can select either enable or disable.
    * If you see a message about installing _Extension Pack for Java_, take the install option.
    * If you see a message _Do you trust the authors of the files in this folder_, check the checkbox and click the "trust" button.

    The project should be free of compiler errors at this point.  Address any issues you see before continuing.

1. Find the TODO instructions.

    * **Using VS Code:** Select "Edit" / "Find in Files..." and search for "TODO".
    * **Using IntelliJ:**  Select "Edit" / "Find" / "Find in Files..." and search for "TODO".  Use the "Open in Find Window" button to place the results in a tab for easy navigation.
    * **Using Eclipse:** Select "Window" / "Show View" / "Tasks".  Use the "Search" view to search for "TODO".  Find a filter icon on the top of this tab.  Check "Show all items", "TODOs".  For scope select "on elements in the selected projects".  The list of TODOs will appear in order!

    IMPORTANT: Work through the TODO instructions in order!   


---
**Part 2 - Obtain an AWS Account, Set Credentials, Enable Bedrock Models**

1. **TODO-01**: Establish an AWS account. Follow the instructions in the **[Lab Setup guide](https://github.com/kennyk65/AI-With-Spring-Student-Files/blob/main/LabInstructions/Lab%20Setup.md)** _Signup Process for Amazon / Bedrock_ section to setup an AWS Account, IAM User, set credentials in your local environment, and enable Bedrock models.


---
**Part 3 - Initial Configuration and Test**

4. **TODO-02**: Open the **pom.xml** file.  Add the dependency for Amazon Bedrock.  The groupId value will be `org.springframework.ai`.  The artifactId will be `spring-ai-starter-model-bedrock-converse`.  Save your work.

```
<dependency>
	<groupId>org.springframework.ai</groupId>
	<artifactId>spring-ai-starter-model-bedrock-converse</artifactId>
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
  application.name: Lab02 Chat AWS
  main.web-application-type: none     # Do not start a web server.
  ai:
    retry:
      max-attempts: 1  # Maximum retry attempts.
    model.chat: bedrock-converse
    bedrock:
      aws.region: us-west-2 # Adjust as needed.
      converse:
          options:
            model: us.amazon.nova-micro-v1:0  # Adjust as needed

```

  - The `spring.ai.model.chat: bedrock-converse` tells Spring AI which autoconfiguration class to use if multiple are on the classpath. In this case, you could omit this entry.
  - Adjust the [region](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-regions-availability-zones.html#concepts-regions) code to match the region where you enabled the Bedrock model.
  - The `bedrock.converse.chat.options.model` setting indicates the specific Bedrock model to use. This setting may change over time as new, improved models are introduced and older models are retired.  See the [latest model list](https://docs.aws.amazon.com/bedrock/latest/userguide/models-supported.html) for details.   
  - SpringAI applications can run as part of a web application, but these exercises are built to avoid that extra step.
  - Note: The retry settings will override the `ChatClient`'s default settings.  You are likely to experience errors while you learn the API's usage, and we don't want you to experience unnecessary delays or expense.
  - We could store the access key ID and secret key values in this file, but this would be a security risk if we were to ever distribute this file.  Setting these values in environment variables is safer.

7.  Save your work.  

---
**Part 4 - Try Spring AI's `ChatClient`**

Now we can use the ChatClient to make API calls to Amazon Bedrock and any of its hosted models.

8. **TODO-05**:  Open `src/main/java/com/example/client/AIClientImpl.java`.  
    - Use a stereotype annotation to mark this class as a Spring bean.  

```
@Component
public class AIClientImpl implements AIClient {
```

9. **TODO-06**: Define a private member variable of type ChatClient named client.
    - Create a constructor for this bean.  
    - Inject a ChatModel object into the constructor.  
    - Pass the model to the ChatClient.builder to build a ChatClient object.  Save the ChatClient object in the client field.
        - The `ChatClient.Builder` is automatically created by auto-configuration when it sees the AWS/Bedrock dependency on the classpath, and Spring will automatically inject it into your constructor on bean creation.  

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

14. **TODO-08**:  Open `src/test/java/com/example/client/AwsClientTests.java`.  Add an annotation to define the class as a Spring Boot test.  Annotate the class with the `@ActiveProfiles` annotation to activate the **aws** profile.

```
@SpringBootTest
@ActiveProfiles("aws")
public class AwsClientTests {
```

15. **TODO-09:** Use the `@Autowired` annotation to inject an instance of our `AIClient`.
    - The class we worked on in the last section implements `AIClient`, so we can inject the dependency by its interface.

```
    @Autowired AIClient client;
```

16.  **TODO-10:** Define a `@Test` method to test the `callApi()` method of the client.

  * Pass in a string that describes the response you wish to generate, such as the `samplePrompt` String defined in code above, or an equivalent.
  * Use AssertJ's Assertions.assertThat() method to ensure that the content is not null.
  * Use AssertJ's Assertions.assertThat() method to ensure that the content contains expected results.
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
17. **TODO-11:**  Organize your imports and save your work.  Run this test, it should pass.  Check the results in the console.

    * The test should run and produce a list of the five Great Lakes.  It is also very possible that you will encounter an error here, possibly due to setup, but also due to changes in Amazon Bedrock since the time of this writing.  Here are some troubleshooting tips.
    * If you have a compilation issue, be sure you have organized imports.  Compare your code to the solution code.
    * Be sure your access key and secret key are established in environment variables.
    * Be sure the region in your `application.yml` file is the same region where you enabled the Bedrock model.


**Part 6 - Summary**

At this point, you have integrated with one of Amazon Bedrock's hosted models from your own Spring Boot application.  If your were running a Spring Boot application on an AWS EC2 instance within a private network, this would be a natural combination to use rather than calling OpenAI or another hosted model directly.  Congratulations! 

