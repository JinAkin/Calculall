package com.trutech.calculall;

import java.util.ArrayList;

public class VRule {

    private String pattern;
    private int operation;
    private int dimension;
    private int firstOccurPosition;
    private VRuleSet vRuleSet;

    /**
     * @param pattern   String pattern to be replaced
     * @param operation Operation number
     */
    public VRule(String pattern, int operation, int dimension, VRuleSet vRuleSet) {
        this.pattern = pattern;
        this.operation = operation;
        this.dimension = dimension;
        this.vRuleSet = vRuleSet;
    }

    /**
     * @param expression Expression that needs to have rules applied to them
     * @return ArrayList<Token>
     */
    public ArrayList<Token> applyRule(ArrayList<Token> expression) {
        //Builds a string to represent the expression
        String stringExpression = buildString(expression);
        //Searches for a pattern in the expression
        BoyerMoore bm = new BoyerMoore(pattern);
        //Index of the first time the pattern happens is set to firstOccurPosition and is -1 if no pattern was found
        firstOccurPosition = bm.search(stringExpression);
        //Base case for recursive implementation or no further rule can be applied
        if (firstOccurPosition == -1) {
            vRuleSet.setAppliedRule();
            return expression;
        } else {
            return applyVectorOperation(expression);
        }
    }

    /**
     * N = Number, A = add, S = subtract, D = dot, C = cross, [ = open bracket, ] = closed bracket, , = comma
     *
     * @param expression An expression represented in a ArrayList of Token
     * @return String The given expression represented in a ArrayList of Token
     * as a String
     */
    public String buildString(ArrayList<Token> expression) {
        String stringExpression = "";
        for (int i = 0; i < expression.size(); i++) {
            if (expression.get(i) instanceof Number) {
                stringExpression = stringExpression + "N";
            } else if (expression.get(i) instanceof Operator) {
                if (((Operator) (expression.get(i))).getType() == Operator.ADD) {
                    stringExpression = stringExpression + "A";
                } else if (((Operator) (expression.get(i))).getType() == Operator.SUBTRACT) {
                    stringExpression = stringExpression + "S";
                } else if (((Operator) (expression.get(i))).getType() == Operator.DOT) {
                    stringExpression = stringExpression + "D";
                } else if (((Operator) (expression.get(i))).getType() == Operator.CROSS) {
                    stringExpression = stringExpression + "C";
                }
            } else if (expression.get(i) instanceof Bracket) {
                if (((Bracket) (expression.get(i))).getType() == Bracket.SQUAREOPEN) {
                    stringExpression = stringExpression + "[";
                } else if (((Bracket) (expression.get(i))).getType() == Bracket.SQUARECLOSED) {
                    stringExpression = stringExpression + "]";
                }
            } else if (expression.get(i).getSymbol() == ",") {
                stringExpression = stringExpression + ",";
            }
        }
        return stringExpression;
    }

    private ArrayList<Token> applyVectorOperation(ArrayList<Token> expression) {
        ArrayList<Token> tempExpression = new ArrayList<Token>();
        ArrayList<Token> numbers = new ArrayList<Token>();
        ArrayList<Token> operators = new ArrayList<Token>();
        //Load all the numbers in the pattern into numbers and the operator tokens in the pattern into operators
        for (int i = firstOccurPosition; i < firstOccurPosition + pattern.length(); i++) {
            if (expression.get(i) instanceof Number) {
                numbers.add(expression.get(i));
            } else if (expression.get(i) instanceof Operator) {
                operators.add(expression.get(i));
            }
        }
        double[] leftVector = new double[dimension], rightVector = new double[dimension];

        //Load all Numbers of each vector into an array of doubles to send for calculations
        for (Token n : numbers) {
            if (numbers.indexOf(n) < dimension) {
                leftVector[numbers.indexOf(n)] = ((Number) n).getValue();
            } else {
                rightVector[numbers.indexOf(n) - dimension] = ((Number) n).getValue();
            }
        }

        //Load Tokens that are before the pattern into tempExpression
        for (int i = 0; i < firstOccurPosition; i++) {
            tempExpression.add(expression.get(i));
        }


        // Add the new vector or number to the expression
        if (operation == VRuleSet.DOT) {
            tempExpression.add(new Number(Utility.calculateDotProduct(leftVector, rightVector)));
        } else if (operation == VRuleSet.CROSS) {
            ArrayList<Token> newVector = Utility.convertDoublesToVector(Utility.calculateCrossProduct(leftVector, rightVector));
            for (Token v : newVector) {
                tempExpression.add(v);
            }
        } else if (operation == VRuleSet.ADD) {
            ArrayList<Token> newVector = Utility.convertDoublesToVector(Utility.calculateAddOrSubtract(leftVector, rightVector, VRuleSet.ADD));
            for (Token v : newVector) {
                tempExpression.add(v);
            }
        } else if (operation == VRuleSet.SUBTRACT) {
            ArrayList<Token> newVector = Utility.convertDoublesToVector(Utility.calculateAddOrSubtract(leftVector, rightVector, VRuleSet.SUBTRACT));
            for (Token v : newVector) {
                tempExpression.add(v);
            }

            //Add the last bit of the expression to tempExpression
            for (int i = 0; i < expression.size() - pattern.length() - firstOccurPosition; i++) {
                tempExpression.add(expression.get(i + firstOccurPosition + pattern.length() - 1));
            }
        }
        return applyRule(tempExpression);
    }
}
