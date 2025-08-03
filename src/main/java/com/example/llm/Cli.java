package com.example.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "llm", mixinStandardHelpOptions = true, version = "llm-java 1.0",
        description = "Access Large Language Models from the command-line.",
        subcommands = {Cli.EmbedCommand.class, Cli.AliasesCommand.class, Cli.LogCommand.class})
public class Cli implements Callable<Integer> {

    @Parameters(index = "0", description = "The prompt to execute.", arity = "0..1")
    private String prompt;

    @Option(names = {"-m", "--model"}, description = "The model to use.", required = true)
    private String model;

    @Option(names = {"-t", "--template"}, description = "The template to use.")
    private String templateName;

    @Option(names = {"-p", "--param"}, description = "Parameters for the template.", arity = "2", split = ",")
    private Map<String, String> params = new HashMap<>();

    @Option(names = {"--schema"}, description = "A JSON schema to use for the response.")
    private String schema;

    @Option(names = {"--tool"}, description = "A tool to make available to the model.")
    private String tool;

    @Option(names = {"-f", "--fragment"}, description = "A fragment to prepend to the prompt.")
    private String fragmentName;

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final Llm llm;
    private final TemplateManager templateManager;
    private final FragmentManager fragmentManager;

    public Cli(Llm llm, TemplateManager templateManager, FragmentManager fragmentManager) {
        this.llm = llm;
        this.templateManager = templateManager;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public Integer call() throws Exception {
        LlmChatModel chatModel = llm.getModel(model);

        String finalPrompt;
        if (templateName != null) {
            try {
                Template template = templateManager.loadTemplate(templateName);
                finalPrompt = template.render(params);
                if (prompt != null) {
                    finalPrompt = finalPrompt + "\n" + prompt;
                }
            } catch (FileNotFoundException e) {
                System.err.println("Error: " + e.getMessage());
                return 1;
            }
        } else {
            finalPrompt = prompt;
        }

        if (fragmentName != null) {
            try {
                String fragment = fragmentManager.loadFragment(fragmentName);
                if (finalPrompt != null) {
                    finalPrompt = fragment + "\n" + finalPrompt;
                } else {
                    finalPrompt = fragment;
                }
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
                return 1;
            }
        }

        if (finalPrompt == null) {
            System.err.println("Error: Prompt is required.");
            return 1;
        }

        List<Object> tools = new ArrayList<>();
        if (tool != null) {
            if ("stringLength".equals(tool)) {
                tools.add(new Tools());
            } else {
                System.err.println("Error: Unknown tool '" + tool + "'");
                return 1;
            }
        }

        LlmRequest llmRequest = new LlmRequest(finalPrompt, schema, tools);
        
        long startTime = System.currentTimeMillis();
        LlmResponse llmResponse = chatModel.chat(llmRequest);
        long durationMs = System.currentTimeMillis() - startTime;
        
        // Log the conversation if logging is enabled
        if (llm.isLoggingEnabled()) {
            llm.getLogManager().logConversation(
                model,
                finalPrompt,
                llmResponse.text(),
                llmResponse.promptTokens(),
                llmResponse.responseTokens(),
                llmResponse.totalTokens(),
                durationMs,
                schema,
                tools,
                null
            );
        }

        if (schema != null) {
            try {
                Object json = jsonMapper.readValue(llmResponse.text(), Object.class);
                System.out.println(jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
            } catch (JsonProcessingException e) {
                System.err.println("Error: The model did not return a valid JSON object.");
                System.err.println(llmResponse.text());
                return 1;
            }
        } else {
            System.out.println(llmResponse.text());
        }

        return 0;
    }

    @Command(name = "embed", description = "Generate an embedding for a given text.")
    static class EmbedCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "The text to embed.")
        private String text;

        @Option(names = {"-m", "--model"}, description = "The embedding model to use.", required = true)
        private String model;

        private final Llm llm;

        public EmbedCommand(Llm llm) {
            this.llm = llm;
        }

        @Override
        public Integer call() throws IOException {
            LlmEmbeddingModel llmEmbeddingModel = llm.getEmbeddingModel(model);
            List<Float> embedding = llmEmbeddingModel.embed(text);
            System.out.println(embedding);
            return 0;
        }
    }

    @Command(name = "aliases", description = "Manage model aliases.",
            subcommands = {AliasesCommand.ListCommand.class, AliasesCommand.SetCommand.class, AliasesCommand.RemoveCommand.class})
    static class AliasesCommand {
        private final Llm llm;

        public AliasesCommand(Llm llm) {
            this.llm = llm;
        }

        @Command(name = "list", description = "List all aliases.")
        static class ListCommand implements Callable<Integer> {
            private final Llm llm;

            public ListCommand(Llm llm) {
                this.llm = llm;
            }

            @Override
            public Integer call() throws IOException {
                Map<String, String> aliases = llm.getAliasManager().loadAliases();
                aliases.forEach((alias, model) -> System.out.println(alias + " -> " + model));
                return 0;
            }
        }

        @Command(name = "set", description = "Set an alias for a model.")
        static class SetCommand implements Callable<Integer> {
            @Parameters(index = "0", description = "The alias to set.")
            private String alias;

            @Parameters(index = "1", description = "The model name the alias should point to.")
            private String modelName;

            private final Llm llm;

            public SetCommand(Llm llm) {
                this.llm = llm;
            }

            @Override
            public Integer call() throws IOException {
                llm.getAliasManager().setAlias(alias, modelName);
                System.out.println("Alias '" + alias + "' set to '" + modelName + "'");
                return 0;
            }
        }

        @Command(name = "remove", description = "Remove an alias.")
        static class RemoveCommand implements Callable<Integer> {
            @Parameters(index = "0", description = "The alias to remove.")
            private String alias;

            private final Llm llm;

            public RemoveCommand(Llm llm) {
                this.llm = llm;
            }

            @Override
            public Integer call() throws IOException {
                llm.getAliasManager().removeAlias(alias);
                System.out.println("Alias '" + alias + "' removed.");
                return 0;
            }
        }
    }


    @Command(name = "log", description = "Manage conversation logs.",
            subcommands = {LogCommand.ListCommand.class, LogCommand.ViewCommand.class})
    static class LogCommand {
        private final Llm llm;

        public LogCommand(Llm llm) {
            this.llm = llm;
        }

        @Command(name = "list", description = "List conversation logs.")
        static class ListCommand implements Callable<Integer> {
            @Option(names = {"-l", "--limit"}, description = "Number of conversations to show", defaultValue = "10")
            private int limit;

            @Option(names = {"-o", "--offset"}, description = "Number of conversations to skip", defaultValue = "0")
            private int offset;

            private final Llm llm;

            public ListCommand(Llm llm) {
                this.llm = llm;
            }

            @Override
            public Integer call() {
                if (!llm.isLoggingEnabled()) {
                    System.err.println("Logging is not enabled. Set LLM_LOG_TYPE environment variable to 'jsonl' or 'h2'.");
                    return 1;
                }

                List<Conversation> conversations = llm.getLogManager().getConversations(limit, offset);
                if (conversations.isEmpty()) {
                    System.out.println("No conversations found.");
                    return 0;
                }

                System.out.printf("%-5s %-20s %-30s %-50s%n", "ID", "Timestamp", "Model", "Prompt");
                System.out.println("-".repeat(105));
                
                for (Conversation conv : conversations) {
                    String prompt = conv.getPrompt();
                    if (prompt.length() > 47) {
                        prompt = prompt.substring(0, 44) + "...";
                    }
                    System.out.printf("%-5d %-20s %-30s %-50s%n", 
                            conv.getId(), 
                            conv.getTimestamp().toString().substring(0, 19), 
                            conv.getModel(), 
                            prompt);
                }
                
                return 0;
            }
        }

        @Command(name = "view", description = "View a specific conversation.")
        static class ViewCommand implements Callable<Integer> {
            @Parameters(index = "0", description = "The conversation ID to view")
            private long id;

            private final Llm llm;

            public ViewCommand(Llm llm) {
                this.llm = llm;
            }

            @Override
            public Integer call() {
                if (!llm.isLoggingEnabled()) {
                    System.err.println("Logging is not enabled. Set LLM_LOG_TYPE environment variable to 'jsonl' or 'h2'.");
                    return 1;
                }

                Conversation conv = llm.getLogManager().getConversation(id);
                if (conv == null) {
                    System.err.println("Conversation with ID " + id + " not found.");
                    return 1;
                }

                System.out.println("Conversation ID: " + conv.getId());
                System.out.println("Timestamp: " + conv.getTimestamp());
                System.out.println("Model: " + conv.getModel());
                System.out.println("Duration: " + conv.getDurationMs() + "ms");
                System.out.println("Tokens: " + conv.getPromptTokens() + " prompt + " + conv.getResponseTokens() + " response = " + conv.getTotalTokens() + " total");
                if (conv.getSchema() != null) {
                    System.out.println("Schema: " + conv.getSchema());
                }
                System.out.println();
                System.out.println("Prompt:");
                System.out.println(conv.getPrompt());
                System.out.println();
                System.out.println("Response:");
                System.out.println(conv.getResponse());
                
                return 0;
            }
        }
    }

    public static void main(String[] args) {
        Llm llm = new Llm();
        TemplateManager templateManager = new TemplateManager();
        FragmentManager fragmentManager = new FragmentManager();

        CommandLine commandLine = new CommandLine(new Cli(llm, templateManager, fragmentManager), 
                new AppFactory(llm, templateManager, fragmentManager));
        int exitCode = commandLine.execute(args);
        
        // Close log manager if it exists
        if (llm.getLogManager() != null) {
            llm.getLogManager().close();
        }
        
        System.exit(exitCode);
    }

    static class AppFactory implements CommandLine.IFactory {
        private final Llm llm;
        private final TemplateManager templateManager;
        private final FragmentManager fragmentManager;

        public AppFactory(Llm llm, TemplateManager templateManager, FragmentManager fragmentManager) {
            this.llm = llm;
            this.templateManager = templateManager;
            this.fragmentManager = fragmentManager;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <K> K create(Class<K> cls) throws Exception {
            if (cls == Cli.class) {
                return (K) new Cli(llm, templateManager, fragmentManager);
            } else if (cls == EmbedCommand.class) {
                return (K) new EmbedCommand(llm);
            } else if (cls == AliasesCommand.class) {
                return (K) new AliasesCommand(llm);
            } else if (cls == AliasesCommand.ListCommand.class) {
                return (K) new AliasesCommand.ListCommand(llm);
            } else if (cls == AliasesCommand.SetCommand.class) {
                return (K) new AliasesCommand.SetCommand(llm);
            } else if (cls == AliasesCommand.RemoveCommand.class) {
                return (K) new AliasesCommand.RemoveCommand(llm);
            } else if (cls == LogCommand.class) {
                return (K) new LogCommand(llm);
            } else if (cls == LogCommand.ListCommand.class) {
                return (K) new LogCommand.ListCommand(llm);
            } else if (cls == LogCommand.ViewCommand.class) {
                return (K) new LogCommand.ViewCommand(llm);
            } else {
                return cls.getDeclaredConstructor().newInstance();
            }
        }
    }
}
