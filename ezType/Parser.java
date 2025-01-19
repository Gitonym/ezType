package ezType;

import javax.swing.*;
import java.util.List;

import static ezType.TokenType.*;

/*
GRAMMAR:

expression     ->  equality ;
equality       ->  comparison ( ( "!=" | "==" ) comparison )* ;
comparison     ->  term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term           ->  factor ( ( "-" | "+" ) factor )* ;
factor         ->  unary ( ( "/" | "*" ) unary )* ;
unary          ->  "!" | "-" ) unary
                   | primary ;
primary        ->  NUMBER | STRING | "true" | "false" | "nil"
                   | "(" expression ")" ;
*/

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        try {
            return expression();
        } catch (EzType.ParseError error) {
            return null;
        }
    }

    // expression -> equality ;
    private Expr expression() {
        return equality();
    }

    // equality -> comparison ( ( "!=" | "==" ) comparison )* ;
    private Expr equality() {
        Expr expression = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expression = new Expr.Binary(expression, operator, right);
        }

        return expression;
    }

    // comparison -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    private Expr comparison() {
        Expr expression = term();

        while (match(LESS, LESS_EQUAL, GREATER, GREATER_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expression = new Expr.Binary(expression, operator, right);
        }

        return expression;
    }


    // term -> factor ( ( "-" | "+" ) factor )* ;
    private Expr term() {
        Expr expression = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expression = new Expr.Binary(expression, operator, right);
        }

        return expression;
    }

    // factor -> unary ( ( "/" | "*" ) unary )* ;
    private Expr factor() {
        Expr expresssion = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expresssion = new Expr.Binary(expresssion, operator, right);
        }

        return expresssion;
    }

    // unary          -> "!" | "-" ) unary
    //                 | primary ;
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    // primary -> NUMBER | STRING | "true" | "false" | "nil"
    //          | "(" expression ")" ;
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);
        if (match(NUMBER, STRING)) return new Expr.Literal(previous().literal);

        if (match(LEFT_PAREN)) {
            Expr expression = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expression);
        }

        throw error(peek(), "Exprected expression.");
    }

    // returns true if the current token is of a specific type and eats the token
    // returns false otherwise and does not eat the token
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    // returns true if the current token is of the passed type, false otherwise
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    // eats and returns the current token;
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    // returns true if end of tokens reached, false otherwise
    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    // returns the current token without eating it
    private Token peek() {
        return tokens.get(current);
    }

    // returns the previous token;
    private Token previous() {
        return tokens.get(current-1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private EzType.ParseError error(Token token, String message) {
        EzType.error(token, message);
        return new EzType.ParseError();
    }

    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;
            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
            advance();
        }
    }
}
