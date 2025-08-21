## Lab 06 - Tool Calling

In this exercise you will configure SpringAI's ChatClient with locally defined "tools".  These tools will be used to embellish the response from the API with external knowledge not known to the model.

An example is combining general knowlege about a given company with its current stock price.  The model is trained with general information, but does not have access to current data.  The following is an example of a combination of the two:

> The stock ticker symbol 'TSLA' belongs to Tesla, Inc. Tesla is a renowned electric vehicle and clean energy company. The current stock price for Tesla is $174.11 

Estimated time: 25 minutes.


---
**Part 1 - Setup the Project**

1. Open the _/student-files/lab06-tool-use_ folder.  

    The project should be free of compiler errors at this point.  Address any issues you see before continuing.

1. Find the TODO instructions.

    * **Using VS Code:** Select "Edit" / "Find in Files..." and search for "TODO".
    * **Using IntelliJ:**  Select "Edit" / "Find" / "Find in Files..." and search for "TODO".  Use the "Open in Find Window" button to place the results in a tab for easy navigation.
    * **Using Eclipse:** Select "Window" / "Show View" / "Tasks".  Use the "Search" view to search for "TODO".  Find a filter icon on the top of this tab.  Check "Show all items", "TODOs".  For scope select "on elements in the selected projects".  The list of TODOs will appear in order!

    IMPORTANT: Work through the TODO instructions in order!   

2. Open the **pom.xml** file.

1. **TODO-01**: Observe that the dependencies Amazon Bedrock, OpenAI, or Azure OpenAI are already setup.  You can follow these lab instructions using any one of these backends.  There is nothing you need to do here:

    ```
		<dependency>
			<groupId>org.springframework.ai</groupId>
			<artifactId>spring-ai-starter-model-openai</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.ai</groupId>
			<artifactId>spring-ai-starter-model-azure-openai</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.ai</groupId>
			<artifactId>spring-ai-starter-model-bedrock-converse</artifactId>
		</dependency>
    ```

1.  Open `src/main/java/com/example/Application.java`.

1.  **TODO-02**: Run the application.

    * **VS Code**: Right-click, Run Java.  Or open the file and click the “Run” option hovering over the main() method.
    * **IntelliJ**: Right-click, select “Run ‘Application.main()’”. 
    * **Eclipse**: Right-click, Select Run As / Java Application.

    We expect the application to start, then stop, without errors.  If you have any errors related to tooling, be sure to address them now before proceeding.


1.  Make sure you have followed the **[Lab Setup guide](https://github.com/kennyk65/AI-With-Spring-Student-Files/blob/main/LabInstructions/Lab%20Setup.md)** document for Bedrock, OpenAI, or Azure OpenAI depending on which instructions you wish to follow.

---
**Part 2 - Initial Configuration**

8. Open `src/main/resources/application.yml`.

1. **TODO-3**: While the lab project can run with either Bedrock, Azure, or OpenAI, the specific properties required by each vary.  You will need to adjust the settings based on your own accounts / environments corresponding to the model(s) you plan on using:

    1. If you plan to use **Amazon Bedrock**:
        * Scroll down to the 'aws' section to add these settings.
        * Set spring.ai.model.chat to bedrock-converse to tell SpringAI which autoconfigure class to use.
        * Adjust the region setting if needed.  Use your previous lab settings for guidance.
        * At present, this lab uses _us.amazon.nova-micro-v1:0_, but other models may work as well.

        ```
        spring:
            config.activate.on-profile: aws

            application.name: Lab06 Bedrock Tool Use
            ai:
                model.chat: bedrock-converse
                bedrock:
                aws.region: us-west-2 # Adjust as needed.
                converse:
                    chat:
                    options:
                        model: us.amazon.nova-micro-v1:0  # Adjust as needed.
        ```


    1. If you plan to use **OpenAI**:
        * Scroll down to the 'openai' section to add these settings.
        * Set spring.ai.model.chat to openai to tell SpringAI which autoconfigure class to use.
        * Adjust the model setting if needed.   You should be able to use the default.  Use your previous lab settings for guidance.

        ```
        spring:
        config.activate.on-profile: openai

        application.name: Lab06 OpenAI Tool Use
        ai:
            model.chat: openai
        ```

    1. If you plan to use **Azure OpenAI**:
        * Set spring.ai.model.chat to azure-openai to tell SpringAI which autoconfigure class to use.
        * Adjust the endpoint, deployment-name, and model settings as needed.  You should be able to use the default for the model. Use your previous lab settings for guidance.

        ```
        spring:
        config.activate.on-profile: azure

        application.name: Lab06 Azure OpenAI Tool Use
        ai:
            model.chat: azure-openai
            azure:
            openai:
                api_key: NEVER-PLACE-SECRET-KEY-IN-CONFIG-FILE
                endpoint: ENDPOINT-GOES-HERE
                chat:
                options:
                    deployment-name: DEPLOYMENT-NAME-GOES-HERE
                    model: gpt-35-turbo
        ```

1.  Save your work.  


---
**Part 3 - Service and Tool Objects**

The fascinating part of tool use is that we get to build our own local code that the model will use.  To keep our focus on Spring AI, the ChatClient, and the model behavior, we have already created the backend logic to be used by the model.

11. Open `src/main/java/com.example.service.StockService`.

1. **TODO-04:** Use an annotation to mark this class as a Spring bean.  

    ```
    @Service("stockService")
    public class StockService {
    ```
1. Take a moment to review the business logic.
    * Information on a handful of stocks has been hard-coded / generated.  In a real-world example, this code would access other services to dynamically determine stock prices and trading volumes, but we don't need to focus on that element.
    * Individual methods are made available to determine a given stock's price and volume.  A separate function is used to calculate an entire portfolio.
    * This POJO has nothing specific to Spring, Spring Boot, or Spring AI.
    
1. Open `src/main/java/com.example.service.StockTools`.

1. **TODO-05:** Use an annotation to mark this class as a Spring bean.  

    ```
    @Service
    public class StockTools {
    ```

1. Find the methods annotated with `@Tool`
    * These are the specific 'tools' made available to the model.
    * Spring AI will make the model aware that the Client has tools that it can use when it doesn't know the answer.  The model will know the names of the tools, their purpose, what parameters they need, and what they produce.
    * When the model is attempting to solve a problem requiring the use of a tool, it will send a response back to the client, asking it to invoke the tool on its behalf.  
        * The Client understands the response is not the final response, but a request to call the tool to provide more information.  
        * The client calls the tool method and obtains a result.
        * The client then sends another prompt to the model with the result from the tool.
        * The model and client repeat this process as needed until the model understands it can produce a final response.
        * The final response is returned to the caller.  *The caller does NOT observe the intermediate prompts and responses between the model and the Client.*
    * Notice that some of the tool methods have complex inputs or outputs.
        
---
**Part 4 - Create a Client Object**

17. Open `src/main/java/com.example.client.AIClientImpl`.

1. **TODO-06:** Use an annotation to mark this class as a Spring bean.  

    ```
    @Component
    public class AIClientImpl implements AIClient {
    ```

1. **TODO-07:** Within the constructor for this bean, pass the `ChatModel` to the `ChatClient.builder` to build a `ChatClient` object. Save the ChatClient object in the `client` field.
.
    ```
    client = ChatClient.builder(model).build();
    ```

1. **TODO-08:** Within the `callApi()` method, use the client object to call the API.
    * .prompt() method can be used to set the prompt using the input parameter.
	* The .tools() method will inject the tools bean so they are available to the model.
    * The .call() method will make the call to the model.
    * The .content() method will return the content of the response.
    * Have the method return the content of the response.
	
    ```
		return 
			client
				.prompt(input)
				.tools(tools)
				.call()
				.content();
    ```

1. Organize your imports, save your work.


---
**Part 5 - Create a `@Test` class**

Anything we code, we should test.  We will make a `@Test` class to ensure our Client object works as expected.

22. Open `src/test/java/com/example/client/AIClientTests.java`.

1. **TODO-09:** Define this test class as a Spring Boot test.  Activate one of the given profiles based on which backend you are using. 

    ```
    @SpringBootTest
    @ActiveProfiles("openai")
    //@ActiveProfiles("azure")
    //@ActiveProfiles("aws")
    public class AIClientTests {
    ```
        * Note: Only one of these profiles can be active at a time.  Otherwise, multiple ChatModels will be auto configured, and you will have to alter the `AIClientImpl` specific to the one you wish to inject.

1. **TODO-10:** Notice the AIClientUse is @Autowired.  This is the class under test.  The StockService is also @Autowired.  We will use it when asserting the test results.

    ```
    @Autowired AIClient client;
    @Autowired StockService stockService;

    ```

1. **TODO-11:** Define a `@Test` method to test the `callApi()` method of the client.
    * Pass in a string that describes the response you wish to generate, such as the provided "samplePrompt" String, or an equivalent.
    * Use AssertJ's `Assertions.assertThat()` method to ensure that the content is not null.
    * Use AssertJ's `Assertions.assertThat()` method to ensure that the content contains expected results.
    * Print the response string that is returned.

    ```
	@Test
	void testCallApi() {

        String response = client.callApi(samplePrompt);

		assertThat(response).isNotNull();
        assertThat(response).contains(sampleResults);

		//	Print the results
		System.out.println("The results of the call are: " + response);

    }
    ```

1. **TODO-12:** Organize imports and save all work.  Note that the `assertThat` functions are part of AssertJ.  You may need to manually add `import static org.assertj.core.api.Assertions.*;`.

1. Run this test, it should pass.  Notice the output contains specific data obtained from the StockService.  You should see a description of the output similar to this:

    > The company summary is: NVIDIA Corporation (NVDA) is a technology company known for its graphics processing units (GPUs) and chipsets. The stock price for NVDA is $607.37 USD.

1. TODO-13: For a more dramatic example of local tool use, examine the logic within the `testCalculatePortfolioValue()` test method.  The problem to solve is 
    > I have 100 shares of NVDA and 50 shares of AAPL. What is the total value of my portfolio?

    * To calculate the portfolio value, the model will need to use multiple tools.
        1. It will need to use our tool to get the stock price TWICE.
        2. It will use our tool to calculate the entire portfolio.
    * To test the results, our test logic will invoke the service functions directly to obtain the final portfolio value, which should appear in the results.

1. Remove the `@Disabled` annotation, run the test.  It should pass.

**Part 6 - Summary**
Our application now has an integration point with the foundational model API.  Spring's `ChatClient` instructs the API that local tools exist to provide missing information.  The generated response is embellished by the client by calling the designated functions. 