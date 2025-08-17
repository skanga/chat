
# LLM Java CLI

A comprehensive command-line interface for accessing various Large Language Models (LLMs) from multiple providers. Built with Java 17 and LangChain4j, this tool provides a unified interface for interacting with models from OpenAI, Anthropic, Google Gemini, Azure OpenAI, Groq, Ollama, and more.

## Features

- **Multi-Provider Support**: Connect to OpenAI, Anthropic, Google Gemini, Azure OpenAI, Groq, Ollama, and HuggingFace models
- **Template System**: Use predefined templates for common prompt patterns
- **Conversation Management**: Continue conversations with context and history
- **Fragment Support**: Prepend reusable text fragments to prompts
- **Model Aliases**: Create shortcuts for frequently used models
- **Structured Output**: Support for JSON schema-based responses
- **Tool Integration**: Make tools available to compatible models
- **Embedding Models**: Generate embeddings for text content
- **Logging & History**: Track conversations and model interactions
- **H2 Database**: Local storage for conversations and logs

## Prerequisites

- **Java 17** or higher
- **Maven 3.6+** for building the project
- API keys for the LLM providers you want to use

## Installation

1. **Clone the repository:**
```bash
git clone <repository-url>
cd chat
```

2. **Build the project:**
```bash
mvn clean package
```

This creates an executable JAR file in the `target/` directory.

3. **Set up API keys:**

The application supports multiple providers. Set the relevant environment variables:

```bash
# Groq
export GROQ_API_KEY="your_groq_api_key"

# OpenAI
export OPENAI_API_KEY="your_openai_api_key"

# Anthropic
export ANTHROPIC_API_KEY="your_anthropic_api_key"

# Google Gemini
export GOOGLE_AI_GEMINI_API_KEY="your_gemini_api_key"

# Azure OpenAI
export AZURE_OPENAI_KEY="your_azure_key"
export AZURE_OPENAI_ENDPOINT="your_azure_endpoint"

# HuggingFace
export HUGGING_FACE_API_KEY="your_hf_api_key"
```

## Usage

### Basic Usage

Send a simple prompt to a model:
```bash
java -jar target/llm-java-1.0.jar -m groq/meta-llama/llama-4-scout-17b-16e-instruct "What is the capital of France?"
```

### Command Options

- `-m, --model`: **Required** - Specify the model to use
- `-t, --template`: Use a predefined template
- `-p, --param`: Parameters for templates (key-value pairs)
- `--schema`: JSON schema for structured responses
- `--tool`: Make a tool available to the model
- `-f, --fragment`: Prepend a text fragment to the prompt
- `-c, --continue`: Continue a previous conversation by ID
- `--context`: Number of previous messages to include (default: 10)

### Examples

**Using templates:**
```bash
java -jar target/llm-java-1.0.jar -m openai/gpt-4 -t code-review -p file "MyClass.java" -p language "Java"
```

**Continuing a conversation:**
```bash
java -jar target/llm-java-1.0.jar -m anthropic/claude-3-sonnet-20240229 -c conv_123 "Can you elaborate on that?"
```

**Using fragments:**
```bash
java -jar target/llm-java-1.0.jar -m groq/mixtral-8x7b-32768 -f expert-prompt "Explain quantum computing"
```

**Structured output with JSON schema:**
```bash
java -jar target/llm-java-1.0.jar -m openai/gpt-4 --schema person.json "Extract person info from this text"
```

### Subcommands

**Chat mode:**
```bash
java -jar target/llm-java-1.0.jar chat -m openai/gpt-4
```

**Generate embeddings:**
```bash
java -jar target/llm-java-1.0.jar embed -m openai/text-embedding-ada-002 "Text to embed"
```

**Manage aliases:**
```bash
java -jar target/llm-java-1.0.jar aliases list
java -jar target/llm-java-1.0.jar aliases set my-model openai/gpt-4
```

**View logs:**
```bash
java -jar target/llm-java-1.0.jar log list
java -jar target/llm-java-1.0.jar log show <conversation-id>
```

## Supported Models and Providers

### OpenAI
- GPT-4, GPT-4 Turbo, GPT-3.5 Turbo
- Text embedding models

### Anthropic
- Claude 3 (Opus, Sonnet, Haiku)
- Claude 2.1, Claude 2.0

### Google
- Gemini Pro, Gemini Pro Vision

### Azure OpenAI
- All OpenAI models via Azure endpoints

### Groq
- LLaMA models, Mixtral, Gemma

### Ollama
- Local models via Ollama

### HuggingFace
- Various open-source models

## Project Structure

```
src/main/java/com/example/llm/
├── Cli.java                    # Main CLI entry point
├── Llm.java                    # Core LLM interaction logic
├── ConversationManager.java    # Conversation handling
├── TemplateManager.java        # Template system
├── FragmentManager.java        # Text fragment management
├── AliasManager.java          # Model alias management
├── LogManager.java            # Logging and history
├── ModelRegistry.java         # Model configuration
└── provider/                  # Provider-specific implementations
    ├── OpenAiChatModelWrapper.java
    ├── AnthropicChatModelWrapper.java
    ├── GeminiChatModelWrapper.java
    └── ...
```

## Development

### Running Tests
```bash
mvn test
```

### Building from Source
```bash
mvn clean compile
mvn package
```

### Contributing
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Troubleshooting

**Maven dependency issues:** If you encounter Maven dependency conflicts, try:
```bash
mvn dependency:tree
mvn clean install -U
```

**API key errors:** Ensure your API keys are properly set as environment variables and have sufficient permissions.

**Model not found:** Check that the model identifier is correct and supported by the provider.
