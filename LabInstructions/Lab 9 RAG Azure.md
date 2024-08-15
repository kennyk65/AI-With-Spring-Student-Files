## Lab 9 - Retrieval Augmented Generation - Azure

In this exercise you will create a Spring Boot application which implements retrieval augmented generation.  It will utilize most of the technologies seen in the previous labs; embeddings, vector stores, and semantic search.  To streamline the entire process, the ChatClient will use a QuestionAnswerAdvisor so our code can avoid working directly with embeddings, vector stores, or semantic searches.

This lab will feature the OpenAI chat models hosted in Azure.  It will also have optional steps to use Redis or PGVector as vector stores rather than an in-memory implementation. 

Within the codebase you will find ordered *TODO* comments that describe what actions to take for each step.  By finding all files containing *TODO*, and working on the steps in numeric order, it is possible to complete this exercise without looking at the instructions.  If you do need guidance, you can always look at these instructions for full information.  Just be sure to perform the *TODO* steps in order.

Solution code is provided for you in a separate folder, so you can compare your work if needed.  For maximum benefit, try to complete the lab without looking at the solution.

Let's jump in.

---
**Part 1 - Establish Azure Account, OpenAI _resource_, Endpoint, Keys, Deployment**

If you have not already done so, follow the instructions in the **Lab Setup guide** and find the _Signup Process for Azure OpenAI_ section.  Walk through these instructions to establish an Azure Account, OpenAI _resource_, Endpoint, Keys, and Deployment.

---
**Part 2 - Setup the Project**

1. Open the _/student-files/lab9-rag-azure_ project in your IDE.   The project should be free of compiler errors at this point.  Address any issues you see before continuing.

2. Find the TODO instructions.  Work through the TODO instructions in order!   

3. Open the **pom.xml** file.

4. **TODO-01**: Observe the dependency for `spring-ai-transformers-spring-boot-starter`.  This dependency defines Spring AI's built in TransformerEmbeddingModel.  We will use this model to create embeddings rather than use an external model.  There is nothing you need to do with this dependency.  If you are motivated, after you have completed the lab, feel free to replace this with a hosted FM for embeddings if you like.

5. **TODO-02:** Add the dependency for Amazon Azure OpenAI.  The groupId value will be `org.springframework.ai`, the artifactId will be `spring-ai-azure-openai-ai-spring-boot-starter`.  

```
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-azure-openai-spring-boot-starter</artifactId>
</dependency>
```
6. Save your work.

7. Open `src/main/resources/application.yml`.  

8. **TODO-03:**  Set the following properties:
* Set the ai.azure.openai.chat.enabled to true.  
* Set the ai.azure.openai.endpoint to the value you established during Azure setup. 
* Set the ai.azure.openai.chat.options.deployment-name to the value you establised during setup.  
* Set the ai.azure.openai.chat.options.model to "gpt-35-turbo", or whichever model you have enabled.

```
  ai:
    azure:
      openai:
        api_key: NEVER-PLACE-SECRET-KEY-IN-CONFIG-FILE
        endpoint: ENDPOINT-GOES-HERE
        chat:
          enabled: true
          options:
            deployment-name: DEPLOYMENT-NAME-GOES-HERE
            model: gpt-35-turbo
```

9. Open `src/main/java/com.example.Application`.

10. **TODO-04:** Define a bean method named "vectorStore" of type `VectorStore`. The method should accept an `EmbeddingModel` parameter.  Have it instantiate and return a `new SimpleVectorStore` injected with the given `EmbeddingModel`.  Use `@Profile` to assign this bean to the **simple-vector-store** profile.

```
	@Bean
	@Profile("simple-vector-store")
	public VectorStore vectorStore(EmbeddingModel embeddingModel) {
		return new SimpleVectorStore(embeddingModel);
	}
```
11.  Organize your imports.  Save your work.


---
**Part 3 - Create the `AIClientImpl` Bean**

12. Open `src/main/java/com.example.client.AIClientImpl`.  

13. **TODO-05:** Use the `@Service` annotation to define this class as a Spring Service.

```
@Service
public class AIClientImpl implements AIClient {
```

14. **TODO-06:**  Use the `@Autowired` annotation to inject an instance of the `VectorStore` bean we defined earlier.

```
    @Autowired VectorStore vectorStore;
```

15. **TODO-07:** Define a constructor for this bean.  Inject a `ChatModel` object into the constructor.  Pass this model to the `ChatClient.builder` to build a `ChatClient` object.  Use `defaultSystem()` to set the default system message to the `defaultSystemMessage` String variable defined above.  build() the ChatClient object and save it in the client field.

```
    public AIClientImpl(ChatModel model) {
        client = ChatClient.builder(model)
                .defaultSystem(defaultSystemMessage)
                .build();
    }
```

* Spring AI's autoconfiguration will have created the `ChatModel` and `ChatClient.builder` beans automatically based on the dependency defined earlier.
* `defaultSystem()` defines the "system message" which defines the overall context, purpose, and rules to be followed by the Foundational Model.  It will be sent with every request made by this client.
* The system message tells the Foundational Model that it is a product expert that makes recommendations to customers.  Notice that it emphasizes that recommendations must include a summary and a URL; this will be important later when we test.

16.  **TODO-08:** Within the `save()` method, add code to convert the `List<String>` into a `List<Document>`.  The `Document`'s constructor takes a String, which you can use to supply the product description.  Call the vectorStore's `add()` method with the `List<Document>` to add these descriptions to the `VectorStore`.
* There are several ways to do this in Java.  One way is with a stream:

```
        List<Document> documents = products.stream()
            .map(product -> new Document(product))
                .toList();
        vectorStore.add(documents);
```


    @Override
    public String getProductRecommendationsText(String query) {

17.  **TODO-09:**  Within the `getProductRecommendations()` method, Define a `new QuestionAnswerAdvisor` object.  Inject it with the `VectorStore` object that was `@Autowired` earlier.
* The `QuestionAnswerAdvisor` is Spring AI's secret weapon for convenient RAG processing.  It will handle 1) converting the prompt to an embedding 2)  performing a similarity search on the vector store 3) embellishing the prompt with the relevant matching documents.

```
    RequestResponseAdvisor advisor = new QuestionAnswerAdvisor(vectorStore);
```

18. **TODO-10:** Use the client object to call the API:
* The `.prompt()` method can be used to define a prompt.
* The `.user()` method can be used to set the incoming `query` String parameter as the user message.  
* The `.advisors()` method can be used to register the QuestionAnswerAdvisor you just defined.
* The `.call()` method will make the call to the model.
* The `.content()` method will return the content of the response.
Have the method return the content of the response.

```
        return
            client
                .prompt().user(query)
                .advisors(advisor)
                .call()
                .content();
```

19.  Organize your imports.  Save your work.


---
**Part 4 - Create a @Test class for the AIClientImpl Bean**

Anything we code, we should test. We will make a `@Test` class to ensure our Client object works as expected.

20.  Open `src/test/java/com.example.client.AzureClientTest`.

21.  **TODO-11:** Define this test class as a Spring Boot test.  Use the `@ActiveProfiles` annotation to activate the **simple-vector-store** and **azure** profiles.

```
@SpringBootTest
@ActiveProfiles({"simple-vector-store","azure"})
public class AzureClientTests {
```

22.  **TODO-12:**  Use the `@Autowired` annotation to inject an instance of the `AIClient`.

```
    @Autowired AIClient client;
```

23.  **TODO-13:**  Write a `@Test` method to validate the `AIClient`.
* First, call the client's `save()` method passing the `Utilities.productCatalog` list; this populates the test data.
* Next, call the client's `getProductRecommendations()` method with the `Utilities.query` String.
* Use AssertJ's `Assertions.assertThat()` method to ensure that the content is not null.
* Use AssertJ's `Assertions.assertThat()` method to ensure that the content contains expected results.  `Utilities.sampleResults` array contains the expected results.
* Print the response string that is returned.

```
    @Test
    public void testGetProductRecommendations() {
        client.save(Utilities.productCatalog);
        String response = client.getProductRecommendations(Utilities.query);
        System.out.println(response);

        assertThat(response).isNotNull();
        assertThat(response).contains(Utilities.sampleResults);
    }
```

24.  **TODO-14:** Save all work.  Run this test, it should pass.

---
**Part 5 OPTIONAL - Replace the simple in-memory vector store with Redis**

Our existing implementation uses an in-memory vector store; fine for testing but not intended for production.  Redis is a popular data storage technology which can be used as a production-quality vector store.

Be sure you have completed the "Setup Process for Redis Docker Container" instructions found in the **Lab Setup** guide. 

25. Open the **pom.xml** file.

26. ***TODO-21:**  Replace the simple in-memory vector store with Redis by removing the commend on the `spring-ai-redis-store-spring-boot-starter` dependency.

```
		<dependency>
			<groupId>org.springframework.ai</groupId>
			<artifactId>spring-ai-redis-store-spring-boot-starter</artifactId>
		</dependency> 
```

27. Open `src/main/resources/application.yml`.  

28. **TODO-22:** Set properties for the Redis vector store.  Notice that these properties will only be used if the **redis-vector-store** profile is active.
* Set `spring.ai.vectorstore.redis.uri` to **redis://localhost:6379**, unless your redis is running elsewhere.
* Set `spring.ai.vectorstore.redis.initializeSchema` to **true** to create the necessary schema.  (You may not want to do this in a production environment.)

```
  ai:
    vectorstore:
      redis:
        uri: redis://localhost:6379  # Default.
        index: default-index         # Default.
        prefix: "default:"           # Default.
        initializeSchema: true       # Potentially destructive.
```

29.  Notice that there are autoconfigure instructions to exclude PgVectorStoreAutoConfiguration and DataSourceAutoConfiguration.  This is done to address the possibility that a student may enable both the redis-vector-store and pg-vector-store profiles.

30.  Open `src/test/java/com.example.client.AzureClientTest`.

31.  **TODO-23:**  Alter the `@ActiveProfiles` annotation at the top of this class.  Replace the **simple-vector-store** profile with **redis-vector-store**.

```
@SpringBootTest
@ActiveProfiles({"redis-vector-store","azure"})
//@ActiveProfiles({"simple-vector-store","azure"})
public class AzureClientTests {
```

32.  Save your work and run the test again.  It should pass.

---
**Part 6 OPTIONAL - Replace the simple in-memory vector store with PGVector**

Our existing implementation uses an in-memory vector store; fine for testing but not intended for production.  PGVector is a popular data storage technology based on the Postgres database.  

Be sure you have completed the "Setup Process for PostgreSQL Docker Container" and "Setup Process for PGVector Docker Container" instructions found in the **Lab Setup** guide. 

33. Open the **pom.xml** file.

34. **TODO-24** Replace the simple in-memory vector store with Redis by removing the commend on the `spring-ai-pgvector-store-spring-boot-starter` dependency.

```
		<dependency>
			<groupId>org.springframework.ai</groupId>
			<artifactId>spring-ai-pgvector-store-spring-boot-starter</artifactId>
		</dependency> 
```

35. Open `src/main/resources/application.yml`.  

36. **TODO-25**  Set properties for the Postgres vector store.  Note that these will only be used if the "pg-vector-store" profile is active.
* Set `spring.datasource.url` to **jdbc:postgresql://localhost:5432/postgres**, unless your Postgres is running elsewhere.
* Set `spring.datasource.username` and `spring.datasource.password` the username and password set when running the container.
* Set `spring.ai.vectorstore.pgvector.initialize-schema` to **true** to create the necessary schema. (You may not want to do this in a production environment.)
* Set `spring.ai.vectorstore.pgvector.index-type` to **HNSW**.
* Set `spring.ai.vectorstore.pgvector.distance-type` to **COSINE_DISTANCE**.
* Set `spring.ai.vectorstore.pgvector.dimensions` based on the the underlying model you are using
    * For AWS/Bedrock/Cohere, use 384
    * For OpenAI's text-embedding-ada-002, use 1536
    
If you make a mistake here, don't worry.  The error message will tell you the correct #.  Delete and restart the DB with the revised number.

```
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: postgres
  ai:
    vectorstore:
      pgvector:
        initialize-schema: true
        index-type: HNSW                # Hierarchical Navigable Small World (HNSW) algorithm, Alternatives: Annoy, LSH, KD-Tree, Ball Tree, Product Quantization, FAISS
        distance-type: COSINE_DISTANCE  # How it determines if one vector is similar to another.
        # CRITICAL: The PGVectorStore database must be initialized to the # of dimensions in the model you are using.
        dimensions: 1536                # OpenAI's text-embedding-ada-002 model uses 1536 dimensions.
        #dimensions: 384                 # AWS/Bedrock/Cohere uses 384 dimensions.
```

37.  Notice that there are autoconfigure instructions to exclude RedisVectorStoreAutoConfiguration.  This is done to address the possibility that a student may enable both the redis-vector-store and pg-vector-store profiles simultaneously.

38.  Open `src/test/java/com.example.client.AzureClientTest`.

39.  **TODO-26:** Alter the `@ActiveProfiles` annotation at the top of this class.  Replace the "simple-vector-store" profile with "pg-vector-store"

```
@SpringBootTest
@ActiveProfiles({"pg-vector-store","azure"})
//@ActiveProfiles({"simple-vector-store","azure"})
public class AzureClientTests {
```

40. Save your work and run the test again.  It should pass.