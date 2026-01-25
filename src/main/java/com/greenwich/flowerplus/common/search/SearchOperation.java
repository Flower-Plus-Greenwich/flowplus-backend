package com.greenwich.flowerplus.common.search;

public enum SearchOperation {
    EQUALITY, NEGATION, GREATER_THAN, LESS_THAN, LIKE, STARTS_WITH, ENDS_WITH, IN, NOT_IN, IS_NULL, IS_NOT_NULL, CONTAINS;

    public static final String[] OPERATIONS = {
            ":", "!", ">", "<", "~", "^", "$", "IN", "NOT IN", "IS NULL", "IS NOT NULL"
    };

    public static final String ZERO_OR_MORE_REGEX = "*";
    public static final String OR_PREDICATE_FLAG = "'";
    public static final String OR_OPERATOR = "OR";
    public static final String AND_OPERATOR = "AND";
    public static final String LEFT_PARENTHESIS = "(";
    public static final String RIGHT_PARENTHESIS = ")";

    public static SearchOperation getSimpleOperation(final char input) {
        return switch (input) {
            case ':' -> EQUALITY;
            case '!' -> NEGATION;
            case '>' -> GREATER_THAN;
            case '<' -> LESS_THAN;
            case '~' -> LIKE;
            case '^' -> STARTS_WITH;
            case '$' -> ENDS_WITH;
            default -> null;
        };
    }

}
