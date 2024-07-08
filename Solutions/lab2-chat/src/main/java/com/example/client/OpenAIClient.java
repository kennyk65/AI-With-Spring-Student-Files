package com.example.client;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

//  TODO-01: Follow the lab setup instructions for establishing an OpenAI account.
//  Once this is finished, move on to the next step.

@Component("openAIClientBean")
@Profile("openai")
public class OpenAIClient implements AIClient {

    private ChatClient client;

    //  TODO-05: Create a constructor for this bean.
    //  Inject a OpenAiChatModel object into the constructor.
    //  Pass the model to the ChatClient.builder to build a ChatClient object.
    //  Save the ChatClient object in the client field.
    public OpenAIClient(OpenAiChatModel model) {
        client = ChatClient.builder(model).build();
    }

    public String callApi(String input) {

        //  TODO-06: Define a new Prompt object using the user input.
        Prompt prompt = new Prompt(input);

        //  TODO-07: Use the client object to call the API.
        //  The .prompt() method can be used to set the prompt defined above.
        //  The .call() method will make the call to the model.
        //  The .content() method will return the content of the response.
        //  Have the method return the content of the response.
        return 
            client
                .prompt(prompt)
                .call()
                .content();
    }

}