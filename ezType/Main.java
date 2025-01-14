package ezType;

public class Main {
    public static void main(String[] args) {
        Expr expression = new Expr.Binary(
                new Expr.Binary(
                        new Expr.Literal(1),
                        new Token(TokenType.PLUS, "+", null, 0),
                        new Expr.Literal(2)
                ),
                new Token(TokenType.STAR, "*", null, 0),
                new Expr.Binary(
                        new Expr.Literal(4),
                        new Token(TokenType.MINUS, "-", null, 0),
                        new Expr.Literal(3)
                )
        );
        System.out.println(new AstPrinter().print(expression));
        System.out.println(new RpnPrinter().print(expression));
    }
}
