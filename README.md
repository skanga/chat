
1. Run a build

mvn clean install

This will create a JAR file in the target directory.

2. Set your API key:

The application expects the Groq API key to be set as an environment variable. You can set it like this:

export GROQ_API_KEY="YOUR_GROQ_API_KEY"

Replace "YOUR_GROQ_API_KEY" with your actual Groq API key.

3. Run the prompt:

Once the project is built and your API key is set, you can run a prompt using the CLI. The command would look like this:

java -jar target/llm-java-1.0-SNAPSHOT.jar -m groq "What is the capital of France?"

This command does the following:

    java -jar target/llm-java-1.0-SNAPSHOT.jar: This runs the compiled Java application.
    -m groq: This specifies that you want to use the Groq provider.
    "What is the capital of France?": This is the prompt you want to send to the model.

The application will then use the langchain4j-groq library to send the prompt to the Groq API and print the llmResponse to the console.

Please note that you would need to have Java 17 and Maven installed on your system to build and run the project. Also, as I mentioned in my commit message, I was unable to test the project due to a Maven dependency issue. You might need to resolve this issue in your own environment to
