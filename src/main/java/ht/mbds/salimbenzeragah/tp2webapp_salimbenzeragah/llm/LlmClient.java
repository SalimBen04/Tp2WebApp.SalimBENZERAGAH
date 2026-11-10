package ht.mbds.salimbenzeragah.tp2webapp_salimbenzeragah.llm;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.google.genai.GoogleGenAiChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.enterprise.context.Dependent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;

@Dependent
public class LlmClient implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String systemRole;

    private transient Assistant assistant;
    private transient ChatMemory chatMemory;

    public LlmClient() {
        initialiserAssistant();
    }

    private void initialiserAssistant() {
        // 1. Chercher d'abord dans les variables d'environnement de votre PC
        String apiKey = System.getenv("GEMINI_KEY");

        // 2. Si non trouvée, chercher dans les propriétés JVM (-DGEMINI_KEY)
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getProperty("GEMINI_KEY");
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "La variable GEMINI_KEY est absente des variables d'environnement et des propriétés JVM."
            );
        }

        ChatModel model = GoogleGenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-3.5-flash-lite")
                .temperature(0.7)
                .build();

        chatMemory = MessageWindowChatMemory.withMaxMessages(20);

        assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(chatMemory)
                .build();
    }

    public void setSystemRole(String systemRole) {
        this.systemRole = systemRole;

        if (chatMemory != null) {
            chatMemory.clear();
            if (systemRole != null && !systemRole.isBlank()) {
                chatMemory.add(SystemMessage.from(systemRole));
            }
        }
    }

    public String chat(String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("La question ne peut pas être vide.");
        }
        return assistant.chat(question);
    }

    @Serial
    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject();
        initialiserAssistant();
        if (systemRole != null && !systemRole.isBlank()) {
            chatMemory.add(SystemMessage.from(systemRole));
        }
    }
}