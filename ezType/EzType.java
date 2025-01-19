package ezType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class EzType {

    static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: ezType [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    // takes path to source file and executes it
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // indicate an error in the exit code
        if (hadError) System.exit(65);
    }

    // REPL
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader((input));

        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            // reset error so REPL keeps running even after error occured
            hadError = false;
        }
    }

    // run source code
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();
        // Stop if there was a syntax error.
        if (hadError) return;
        System.out.println(new AstPrinter().print(expression));
    }

    // TODO: add character number
    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        }
        else {
            report(token.line, " at '" + token.lexeme + "'",
                    message);
        }
    }

    static class ParseError extends RuntimeException {}
}
