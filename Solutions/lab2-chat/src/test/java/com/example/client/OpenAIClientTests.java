package com.example.client;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("openai")
public class OpenAIClientTests {

    @Autowired OpenAIClient openAIClient;

	@Test
	void quickChat() {

        String response = 
            openAIClient.callModel(
                "Generate the names of the five great lakes and their sizes in square miles.  Produce JSON output.",
                "gpt-3.5-turbo");

		assertThat(response).isNotNull();
		assertThat(response).contains("Huron", "Ontario", "Michigan", "Erie", "Superior");
		
		//	Print the results
		System.out.println("The results of the call are: " + response);

    }

}
