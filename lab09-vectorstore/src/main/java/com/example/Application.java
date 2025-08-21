package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	//	TODO-02: Define a bean method named "vectorStore" of type VectorStore.
	//  The method should accept an EmbeddingModel parameter.
	//	Have it use the SimpleVectorStore's builder method to inject the given EmbeddingModel.
	//  Use @Profile to assign this bean to the "simple-vector-store" profile.

	
}
