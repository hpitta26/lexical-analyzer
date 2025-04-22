import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.File;

public class Main {

    public static void main(String[] args) {
        String fileName;
        ArrayList<Lexeme> symbolTable = new ArrayList<Lexeme>();
        Scanner keyboard = new Scanner(System.in);
        fileName = "example.txt";
        

        try (Scanner scanner = new Scanner(new File(fileName))) {
            int count = 1;
            while (scanner.hasNextLine()) {
                System.out.println("Line " + count + " Reached");
                String line = scanner.nextLine();
                line.trim(); // remove leading and trailing whitespaces
                if (line.isEmpty()) {
                    continue; // skip empty lines
                }


                // Handle comments
                int c1 = line.indexOf("/*");
                int c2 = line.indexOf("//");
                if (c1 != -1 && c2 != -1) {
                    if (c1 < c2) {
                        handleMultiLineComment(line, scanner);
                    } else {
                        line = handleSingleLineComment(line);
                    }
                } else if (c1 != -1) {
                    handleMultiLineComment(line, scanner);
                } else if (c2 != -1) {
                    line = handleSingleLineComment(line);
                }
                System.out.println("Line: " + line);

                
                line.trim();
                if (line.equals("(") || line.equals(")") || line.equals("{") || line.equals("}") || line.equals("[") || line.equals("]")) {
                    continue; // skip brackets
                } 
                
                int curr = 0;
                while (curr != line.length()) {
                    if (line.charAt(curr) == ' ' || line.charAt(curr) == '\t') {
                        curr++;
                        continue;
                    }
                    if (line.charAt(curr) == '(' || line.charAt(curr) == '{' || line.charAt(curr) == '[' || line.charAt(curr) == ')' || line.charAt(curr) == '}' || line.charAt(curr) == ']') {
                        curr++;
                        continue;
                    }
                    
                    if (Character.isLetter(line.charAt(curr))) {
                        String[] res = identifyLetter(line.substring(curr)).split(" ");
                        Lexeme l = new Lexeme(res[0], res[1]);
                        if (!symbolTable.contains(l)) {
                            symbolTable.add(l);
                        }
                        curr += Integer.parseInt(res[2]);
                        System.out.println(res[0] + " " + res[1]);
                    } else if (Character.isDigit(line.charAt(curr))) {
                        String[] res = identifyDigit(line.substring(curr)).split(" ");
                        Lexeme l = new Lexeme(res[0], res[1]);
                        if (!symbolTable.contains(l)) {
                            symbolTable.add(l);
                        }
                        curr += Integer.parseInt(res[2]);
                        System.out.println(res[0] + " " + res[1]);
                    } else { // Symbol
                        System.out.println("Symbol reached");
                        String[] res = identifySymbol(line.substring(curr)).split(" ");
                        Lexeme l = new Lexeme(res[0], res[1]);
                        if (!symbolTable.contains(l)) {
                            symbolTable.add(l);
                        }
                        curr += Integer.parseInt(res[2]);
                        System.out.println(res[0] + " " + res[1]);
                    }
                }
                
                count++;
                System.out.println("\n");
            }
            scanner.close();
        } catch (FileNotFoundException E){
            E.printStackTrace();
        }

        System.out.println("\n\nSymbol Table\n");

        for (Lexeme l : symbolTable) {
            System.out.println(l);
        }

        keyboard.close();
    }
    
    public static String identifyLetter(String line) {
        String token;

        int lookahead = 1;
        for (int i=1; i<line.length(); i++) {
            if (Character.isLetterOrDigit(line.charAt(i))) {
                lookahead++;
            } else {
                break;
            }
        }

        String lexeme = line.substring(0, lookahead);
        
        switch (lexeme) {
            case "int": token = "TypeInteger"; break;
            case "bool": token = "TypeBoolean"; break;
            case "float": token = "TypeFloat"; break;
            case "char": token = "TypeCharacter"; break;
            case "if": token = "IfStatement"; break;
            case "else": token = "ElseStatement"; break;
            case "while": token = "WhileStatement"; break;
            case "true": token = "BooleanTrue"; break;
            case "false": token = "BooleanFalse"; break;
            default: token = "Identifier";
        }

        return token + " " + lexeme + " " + lookahead;
    }

    public static String identifyDigit(String line) {
        int lookahead = 1;
        for (int i=1; i<line.length(); i++) {
            if (line.charAt(i) == '.') {
                lookahead++;
                continue;
            }
            if (!Character.isDigit(line.charAt(i))) {
                break;
            }
            lookahead++;
        }

        String token = line.contains(".")? "Float" : "Integer";
        
        return token + " " + line.substring(0, lookahead) + " " + lookahead;
    }


    public static String identifySymbol(String token) {
        String result;

        int lookahead = 1;
        for (int i=1; i<token.length(); i++) {
            if (!Character.isLetterOrDigit(token.charAt(i))) {
                lookahead++;
            } else {
                break;
            }
        }

        if (token.length() > 1 && token.charAt(0) == '!' || token.charAt(0) == '-' && Character.isLetterOrDigit(token.charAt(1))) {
            if (token.charAt(0) == '!') {
                result = "UnaryOperatorNot";
            } else {
                result = "UnaryOperatorNegation";
            }
        } else {
            String t = token.substring(0, lookahead).trim();
            // System.out.println(result + " " + token.substring(0, lookahead) + " " + lookahead);
            if (t.equals(";")) {
                result = "Statement";
            } else if (t.equals("||")) {
                result = "OrExpression";
            } else if (t.equals("&&")) {
                result = "AndExpression";
            } else if (t.equals("=")) {
                result = "RelativeOperatorEquals";
            } else if (t.equals("==")) {
                result = "EquivalencyOperatorEquals";
            } else if (t.equals("!=")) {
                result = "EquivalencyOperatorNotEquals";
            } else if (t.equals("<")) {
                result = "RelativeOperatorLessThan";
            } else if (t.equals("<=")) {
                result = "RelativeOperatorLessThanEquals";
            } else if (t.equals(">")) {
                result = "RelativeOperatorGreaterThan";
            } else if (t.equals(">=")) {
                result = "RelativeOperatorGreaterThanEquals";
            } else if (t.equals("+")) {
                result = "AdditionOperatorAdd";
            } else if (t.equals("-")) {
                result = "AdditionOperatorSubtract";
            } else if (t.equals("*")) {
                result = "MultiplicationOperatorMultiply";
            } else if (t.equals("/")) {
                result = "MultiplicationOperatorDivide";
            } else if (t.equals("%")) {
                result = "MultiplicationOperatorModulus";
            } else {
                result = "Character";
            }
        }

        return result.trim() + " " + token.substring(0, lookahead).trim() + " " + lookahead;
    }

    public static String handleSingleLineComment(String line) {
        System.out.println("// Comment Removed --> " + line.substring(line.indexOf("//")));
        line = line.substring(0, line.indexOf("//")); // remove comments

        return line;
    }

    public static void handleMultiLineComment(String line, Scanner scanner) {
        System.out.println("Multi-line Comment Removed");
        while (!line.contains("*/")) {
            line = scanner.nextLine(); // skip multi-line comments
        }
        // scanner.nextLine();
    }


    public static class Lexeme {
        String token;
        String lexeme;
    
        public Lexeme(String token, String lexeme){
            this.token = token;
            this.lexeme = lexeme;
        }
    
        @Override
        public String toString() {
            return lexeme + " " + token;
        }

        @Override
        public int hashCode() {
            return Objects.hash(token, lexeme);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Lexeme lexeme1 = (Lexeme) obj;
            return token.equals(lexeme1.token) && lexeme.equals(lexeme1.lexeme);
        }

    }
}
        