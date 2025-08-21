
## Lab 09 - VectorStore

In this exercise you will create a Spring Boot application which utilizes a Vector Store.  You'll gain experience in loading a Vector Store with documents and performing semantic searches.  We will have a choice of which embedding model to use; Amazon Bedrock, OpenAI, or Ollama.

Within the codebase you will find ordered *TODO* comments that describe what actions to take for each step.  By finding all files containing *TODO*, and working on the steps in numeric order, it is possible to complete this exercise without looking at the instructions.  If you do need guidance, you can always look at these instructions for full information.  Just be sure to perform the *TODO* steps in order.

For simplicity, the lab will use an in-memory Vector Store.  There are optional steps to implement either Redis or PGVector-based Vector Stores if you have time and interest.

Solution code is provided for you in a separate folder, so you can compare your work if needed.  For maximum benefit, try to complete the lab without looking at the solution.

Let's jump in.

---
**Part 1 - Setup the Project**

1. Open the _/student-files/lab09-vectorstore_ project in your IDE.   The project should be free of compiler errors at this point.  Address any issues you see before continuing.

1. Find the TODO instructions.  Work through the TODO instructions in order!   

1. Open the **application.yml** file.

1. **TODO-01:** The lab project is already setup with dependencies for Bedrock, Ollama, and OpenAI.  However, you may need to adjust the settings based on your own accounts / environments.  Adjust the settings to correspond to the model(s) you plan on using:

    1. If you plan to use **Amazon Bedrock** :
        * Set spring.ai.model.embedding to bedrock-cohere.
        * Adjust the region setting if needed.  Use your previous lab settings for guidance.
        * Set the spring.ai.bedrock.cohere.embedding.model to **cohere.embed-english-v3**
        * Make sure you have followed the **[Lab Setup guide](https://github.com/kennyk65/AI-With-Spring-Student-Files/blob/main/LabInstructions/Lab%20Setup.md)** for AWS / Amazon Bedrock.
        
    1. If you plan to use **OpenAI**:
        * Set spring.ai.model.embedding to openai.
        * Set spring.ai.openai.embedding.options.model to **text-embedding-ada-002**, or see the **[latest list](https://platform.openai.com/docs/models/embeddings)**.
        * Make sure you have followed the **[Lab Setup guide](https://github.com/kennyk65/AI-With-Spring-Student-Files/blob/main/LabInstructions/Lab%20Setup.md)** for OpenAI.

    1. If you plan to use **Ollama**:
        * Set spring.ai.model.embedding to ollama.
        * Make sure you have followed the **[Lab Setup guide](https://github.com/kennyk65/AI-With-Spring-Student-Files/blob/main/LabInstructions/Lab%20Setup.md)** for Ollama.
        * Adjust the base-url if needed.  Use your previous lab settings for guidance.
        * Set spring.ai.ollama.embedding.options.model to **mxbai-embed-large**, or experiment with another **[embedding model](https://ollama.com/blog/embedding-models)**
        * Make sure your Ollama Docker container is running.
        * Pull the model if you have not already done so.


---
**Part 2 - Define a VectorStore Bean**

Next we will need to establish a VectorStore `@Bean`; the in-memory implementation that we want to use is not auto-configured.

5. Open the **application.yml** file.

1. **TODO-02:** Define a bean method named "vectorStore" of type `VectorStore`.
    * The method should accept an `EmbeddingModel` parameter
    * Have it use the SimpleVectorStore's builder method to inject the given `EmbeddingModel`.
	* Use `@Profile` to assign this bean to the "simple-vector-store" profile.
        * This will allow us to easily substitute this `@Bean` later if we switch to Redis or PGVector.

    ```        
        @Bean
        @Profile("simple-vector-store")
        public VectorStore vectorStore(EmbeddingModel embeddingModel) {
            return SimpleVectorStore.builder(embeddingModel).build();
        }        
    ```

1. Organize your imports, save your work.

---
**Part 3 - Define a ProductDaoImpl Bean**

Next we will focus on a `ProductDaoImpl` Bean.  This bean will be used to populate and search the Vector Store.

8. Open `src/main/java/com.example.dao.ProductDaoImpl.java`

1. **TODO-03:** Use a stereotype annotation to mark this DAO as a Spring bean.  Alter the class to implement the `ProductDao` interface.

    ```
    @Repository
    public class ProductDaoImpl implements ProductDao {
    ```

1. **TODO-04**: Within the class, define a member variable of type `VectorStore` and `@Autowire` it.

    ```
        @Autowired VectorStore vectorStore;
    ```

1. **TODO-05:** Define a public void `add()` method.
    * Define a single parameter of type `List<String>` containing product descriptions.
    * Convert the `List<String>` into a `List<Document>`, where each product description String is used to create a `Document`.
    * Call the vectorStore's `add()` method with the `List<Document>` to add the product descriptions to the VectorStore.

    ```
        public void add(List<String> products) {
            List<Document> documents = products.stream()
                .map(product -> new Document(product))
                .toList();
            vectorStore.add(documents);
        }
    ```

1. **TODO-06:** Define a `public List<String> findClosestMatches()` method.
    * Define two parameters: a `String` query and an `int` numberOfMatches.
    * Use the SearchRequest.builder() method to create a new SearchRequest.
    *   Use the builder's query() method to set the input query String.
    *   Use the builder's topK() method to limit the number of matches.
    * Call the vectorStore's `similaritySearch()` method with the `SearchRequest` to find the closest matches.
    * Capture the result in a `List<Document>`.
    * Convert the `List<Document>` into a `List<String>` and return.

    ```    
        public List<String> findClosestMatches(String query,int numberOfMatches) {

            SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(numberOfMatches)
                .build();
            List<Document> results = vectorStore.similaritySearch(request);
            return results.stream()
                .map((Document doc) -> doc.getText())
                .toList();
        }
    ```

1. **TODO-07:** Define a `public String findClosestMatch()` method.
    * Define a single parameter of type `String` containing a query.
    * Call the `findClosestMatches()` method with the query and 1 as the number of matches.
    
    ```
        public String findClosestMatch(String query) {
            return findClosestMatches(query, 1).get(0);            
        }
    ```

1. Organize your imports, save your work.

---
**Part 4 - Define a ProductServiceImpl Bean**

Next we will focus on a `ProductServiceImpl` Bean.  

15. Open `src/main/java/com.example.service.ProductServiceImpl.java`

1. **TODO-08:** Use a stereotype annotation to mark this service as a Spring bean.  Alter the class to implement the `ProductService` interface.

    ```
    @Service
    public class ProductServiceImpl implements ProductService {
    ```

1. **TODO-09:** Autowire the `ProductDao` bean we defined earlier.

    ```
        @Autowired ProductDao dao;
    ```

1. ** TODO-10:** Define a public void `save()` method which delegates to the DAO:
    * Define a single parameter of type `List<String>` containing product descriptions.
    * Call the dao's `add()` method with the `List<String>` to save the product descriptions.

    ```    
        public void save(List<String> products) {
            dao.add(products);
        }
    ```

1. **TODO-11:** Define a `public List<String> findClosestMatches()` method which delegates to the DAO:
    * Define a single parameter of type `String` containing a query.
    * Call the dao's `findClosestMatches()` method with the query and 5 as the number of matches.
    * Return the resulting `List<String>`.

    ```    
        public List<String> findClosestMatches(String query) {
            return dao.findClosestMatches(query,5);
        }
    ```

1. **TODO-12:** Define a `public String findClosestMatch()` method which delegates to the DAO:
    * Define a single parameter of type `String` containing a query.
    * Call the dao's `findClosestMatch()` method with the query.
    * Return the resulting `String`.

    ```
        public String findClosestMatch(String query) {
            return dao.findClosestMatch(query);
        }
    ```

---
**Part 5 - Define a Test Bean**

Anything that we code, we should test.  Create a test which loads the Vector Store with sample product descriptions.  They we will test semantic searches for a product idea, and expect to get a match.

21. Open `src/test/java/com.example.service.ProductServiceTests.java`

1. **TODO-13:** Define this test class as a Spring Boot test.  Use the `@ActiveProfiles` annotation to activate TWO profiles:
    1. The first profile will be **simple-vector-store**.
    2. The second profile will match the embedding model you plan to use:
        * For Amazon Bedrock,     use **aws-embedding**.
        * For standard OpenAI,    use **openai-embedding**.
        * For Ollama,             use **ollama-embedding**.    

    * Example:
    ```
    @SpringBootTest
    @ActiveProfiles({"simple-vector-store","aws-embedding"})
    public class ProductServiceTests {
    ```

1. **TODO-14:** Use the `@Autowired` annotation to inject an instance of the `ProductService`.

    ```
        @Autowired ProductService svc;
    ```

1. **TODO-15:** Write a `@Test` method to validate the ProductService.
    *   First, call the service's `save()` method with the `Utilities.products` list; this populates the test data.
        * `Utilities.products` has been created for you to provide sample product test data.
    * Next, call the service's `findClosestMatch()` method with the `Utilities.samplePrompt` String.
    * Use AssertJ's `assertThat()` to validate that the result starts with "Wireless Headphones:".
    * Finally, print the result to the console.

    ```
        @Test
        public void testFindClosestMatch() {
            svc.save(Utilities.products);
            String result = svc.findClosestMatch(Utilities.samplePrompt);
            assertThat(result).startsWith("Wireless Headphones:");
            System.out.println(result);
        }
    ```

1. **TODO-16:** Save all work.  Run this test, it should pass.

Congratulations!  You have successfully implemented an in-memory vector store, populated with embeddings of product descriptions, and performed semantic search to find matches.

---
**Part 6 (OPTIONAL) - Implement a Redis-based Vector Store**

The existing implementation uses an in-memory Vector Store, which is not appropriate for production use.  Follow these optional instructions to implement a Vector Store using Redis.

26. Open the **pom.xml** file.

1. **TODO-20** Replace the simple in-memory vector store with Redis.  Remove the comment to include the starter for Redis.  Follow the instructions in the **[Lab Setup guide](https://github.com/kennyk65/AI-With-Spring-Student-Files/blob/main/LabInstructions/Lab%20Setup.md)** to installing and run Redis as a Docker container

    ```
            <dependency>
                <groupId>org.springframework.ai</groupId>
                <artifactId>spring-ai-starter-vector-store-redis</artifactId>
            </dependency> 
    ```
1.  Save your work.  You may need to prompt your IDE to refresh your project.

1. Open the **application.yml** file.

1. **TODO-21**: Find the section of the file applicable to the `redis-vector-store` profile.  Set properties for the Redis vector store.
    * Note that these will only be used if the **redis-vector-store** profile is active.  This is done to prevent conflicts with the earlier lab steps.
    * Set `spring.ai.vectorstore.redis.uri` to redis://localhost:6379, unless your redis is running elsewhere.
    * Set `spring.ai.vectorstore.redis.initializeSchema` to true to create the necessary schema.  (You may not want to do this in a production environment.)

    ```    
    ai:
        vectorstore:
        redis:
            uri: redis://localhost:6379  # Default.
            index: default-index         # Default.
            prefix: "default:"           # Default.
            initializeSchema: true       # Potentially destructive.
    ```

1. Open `src/test/java/com.example.service.ProductServiceTests.java`

1. **TODO-22**:  Adjust the `@ActiveProfiles` annotation at the top of this class.  Replace "simple-vector-store" with "redis-vector-store".

    ```
    @ActiveProfiles({"redis-vector-store","aws-embedding"})
    ```
1. Save your work and run the test.  It should pass.

---
**Part 7 (OPTIONAL) - Implement a PGVector-based Vector Store**

Another alternative to the in-memory and Redis vector stores is the PGVector store, based on the Postgres database.  Follow these optional instructions to implement a Vector Store using PGVector.

34. Open the **pom.xml** file.

35. **TODO-24** Replace the simple in-memory vector store with PGVector.  Remove the comment to include the starter for PGVector.  Follow the instructions in the **[Lab Setup guide](https://github.com/kennyk65/AI-With-Spring-Student-Files/blob/main/LabInstructions/Lab%20Setup.md)** to installing and run PGVector as a Docker container

```
		<dependency>
			<groupId>org.springframework.ai</groupId>
			<artifactId>spring-ai-starter-vector-store-pgvector</artifactId>
		</dependency> 
```

36.  Save your work.  You may need to prompt your IDE to refresh your project.

1. Open the **application.yml** file.

1. **TODO-25**: Find the section of the file applicable to the `pg-vector-store` profile.Set properties for the Postgres vector store.
    * Note that these will only be used if the **pg-vector-store** profile is active.  This is done to prevent conflicts with the earlier lab steps.
    * Set `spring.datasource.url` to jdbc:postgresql://localhost:5432/postgres, unless your Postgres is running elsewhere.
    * Set `spring.datasource.username` and `spring.datasource.password` the username and password set when running the container.
    * Set the `spring.ai.vectorstore.pgvector.initialize-schema` to true to create the necessary schema. (You may not want to do this in a production environment.)
    * Set the `spring.ai.vectorstore.pgvector.index-type` to HNSW.
    * Set the `spring.ai.vectorstore.pgvector.distance-type` to COSINE_DISTANCE.
    * Set the `spring.ai.vectorstore.pgvector.dimensions` based on the the underlying model you are using
        * For Spring AI's internal embedding model, use 384.
        * For AWS/Bedrock/Cohere, use 1024
        * For OpenAI's text-embedding-ada-002, use 1536
        * If you make a mistake here, don't worry.  The error message will tell you the correct #.  Delete and restart the DB with the revised number.

```    
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: postgres
  ai:
    vectorstore:
      pgvector:
        initialize-schema: true
        index-type: HNSW                
        distance-type: COSINE_DISTANCE  
        # CRITICAL: The PGVectorStore database must be initialized to the # of dimensions in the model you are using.
        dimensions: ####
```

39. Open `src/test/java/com.example.service.ProductServiceTests.java`

40. **TODO-26**:  Adjust the `@ActiveProfiles` annotation at the top of this class.  Replace "simple-vector-store" / "redis-vector-store" with "pg-vector-store".


```
@ActiveProfiles({"pg-vector-store","aws-embedding"})
```
41. Save your work and run the test.  It should pass.


---
**Part 8 Summary**

At this point, you have successfully implemented an in-memory vector store, populated with embeddings of product descriptions, and performed semantic search to find matches.  If you have performed the optional sections, you have replaced the in-memory vector store with either Redis or PGVector.  Congratulations!