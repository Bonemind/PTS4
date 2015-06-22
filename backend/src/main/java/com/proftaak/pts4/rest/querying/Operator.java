package com.proftaak.pts4.rest.querying;

/**
 * Operators that can be used while querying, and the characters needed to use them
 */
public enum Operator {
    NE("!="),
    GE(">="),
    GT(">"),
    LE("<="),
    LT("<"),
    IN(":="),
    LIKE("%="),
    EQ("=");

    public final String symbol;

    Operator(String symbol) {
        this.symbol = symbol;
    }
}
