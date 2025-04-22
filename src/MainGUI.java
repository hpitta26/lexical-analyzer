import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Scanner;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;

public class MainGUI {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LexicalAnalyzerGUI().createAndShowGUI();
        });
    }

    public static class LexicalAnalyzerGUI {
        private JTextArea inputTextArea;
        private JTextArea outputTextArea;
        private JTable symbolTable;
        private CardLayout cardLayout;
        private JPanel outputPanel;

        public void createAndShowGUI() {
            JFrame frame = new JFrame("Lexical Analyzer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            inputTextArea = new JTextArea();
            outputTextArea = new JTextArea();
            outputTextArea.setEditable(false);

            symbolTable = new JTable(new String[][]{}, new String[]{"Lexeme", "Token"});
            JScrollPane symbolTableScrollPane = new JScrollPane(symbolTable);

            JButton analyzeButton = new JButton("Analyze");
            analyzeButton.addActionListener(new AnalyzeButtonListener());

            JButton switchButton = new JButton("Switch to Symbol Table");
            switchButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cardLayout.next(outputPanel);
                    if (switchButton.getText().equals("Switch to Symbol Table")) {
                        switchButton.setText("Switch to Output.txt");
                    } else {
                        switchButton.setText("Switch to Symbol Table");
                    }
                }
            });

            JPanel inputPanel = new JPanel(new BorderLayout());
            inputPanel.add(new JLabel("Input.txt"), BorderLayout.NORTH);
            inputPanel.add(new JScrollPane(inputTextArea), BorderLayout.CENTER);

            outputPanel = new JPanel(cardLayout = new CardLayout());
            JPanel outputTextPanel = new JPanel(new BorderLayout());
            outputTextPanel.add(new JLabel("Output.txt"), BorderLayout.NORTH);
            outputTextPanel.add(new JScrollPane(outputTextArea), BorderLayout.CENTER);

            JPanel symbolTablePanel = new JPanel(new BorderLayout());
            symbolTablePanel.add(new JLabel("Symbol Table"), BorderLayout.NORTH);
            symbolTablePanel.add(symbolTableScrollPane, BorderLayout.CENTER);

            outputPanel.add(outputTextPanel, "Output.txt");
            outputPanel.add(symbolTablePanel, "Symbol Table");

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, outputPanel);
            splitPane.setDividerLocation(400);

            JPanel bottomPanel = new JPanel(new BorderLayout());
            JPanel analyzeButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            analyzeButtonPanel.add(analyzeButton);
            JPanel switchButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            switchButtonPanel.add(switchButton);

            bottomPanel.add(analyzeButtonPanel, BorderLayout.WEST);
            bottomPanel.add(switchButtonPanel, BorderLayout.EAST);

            frame.getContentPane().add(splitPane, BorderLayout.CENTER);
            frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

            frame.setVisible(true);
        }

        private class AnalyzeButtonListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inputText = inputTextArea.getText();
                ArrayList<Lexeme> symbolTableList = analyzeText(inputText);
                StringBuilder outputText = new StringBuilder();

                for (Lexeme lexeme : symbolTableList) {
                    outputText.append(lexeme).append("\n");
                }

                outputTextArea.setText(outputText.toString());

                try (FileWriter writer = new FileWriter("output.txt")) {
                    writer.write(outputText.toString());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }


                HashSet<Lexeme> set = new HashSet<>();
                ArrayList<Lexeme> list = new ArrayList<>();
                for (Lexeme l : symbolTableList) {
                    if (!set.contains(l)) {
                        set.add(l);
                        list.add(l);
                    }
                }
                String[][] data = new String[list.size()][2];
                for (int i = 0; i < list.size(); i++) {
                    Lexeme lexeme = list.get(i);
                    data[i][0] = lexeme.lexeme;
                    data[i][1] = lexeme.token;
                }


                // String[][] data = new String[symbolTableList.size()][2];
                // for (int i = 0; i < symbolTableList.size(); i++) {
                //     Lexeme lexeme = symbolTableList.get(i);
                //     data[i][0] = lexeme.lexeme;
                //     data[i][1] = lexeme.token;
                // }

                
                symbolTable.setModel(new javax.swing.table.DefaultTableModel(data, new String[]{"Lexeme", "Token"}));
            }
        }

        private ArrayList<Lexeme> analyzeText(String text) {
            ArrayList<Lexeme> symbolTable = new ArrayList<>();
            Scanner scanner = new Scanner(text);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue; // skip empty lines
                }

                if (line.contains("int main()")) {
                    continue; // skip main function
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
                        // if (!symbolTable.contains(l)) {
                        //     symbolTable.add(l);
                        // }
                        symbolTable.add(l);
                        curr += Integer.parseInt(res[2]);
                    } else if (Character.isDigit(line.charAt(curr))) {
                        String[] res = identifyDigit(line.substring(curr)).split(" ");
                        Lexeme l = new Lexeme(res[0], res[1]);
                        // if (!symbolTable.contains(l)) {
                        //     symbolTable.add(l);
                        // }
                        symbolTable.add(l);
                        curr += Integer.parseInt(res[2]);
                    } else { // Symbol
                        try {
                            String[] res = identifySymbol(line.substring(curr)).split(" ");
                            Lexeme l = new Lexeme(res[0], res[1]);
                            // if (!symbolTable.contains(l) && !res[0].equals("SKIP")) {
                            //     symbolTable.add(l);
                            // }
                            if (!res[0].equals("SKIP")) {
                                symbolTable.add(l);
                            }
                            curr += Integer.parseInt(res[2]);
                        } catch (Exception e) {
                            System.out.print("Character reached: ");
                            Lexeme l = new Lexeme("Character", ""+line.charAt(curr+3));
                            System.out.println(l);
                            // if (!symbolTable.contains(l)) {
                            //     symbolTable.add(l);
                            // }
                            symbolTable.add(l);
                            curr += 5;
                            continue;
                        }
                        
                    }
                }
            }

            scanner.close();
            return symbolTable;
        }
    }

    public static String identifyLetter(String line) {
        String token;

        int lookahead = 1;
        for (int i = 1; i < line.length(); i++) {
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
        for (int i = 1; i < line.length(); i++) {
            if (line.charAt(i) == '.') {
                lookahead++;
                continue;
            }
            if (!Character.isDigit(line.charAt(i))) {
                break;
            }
            lookahead++;
        }

        String token = line.contains(".") ? "Float" : "Integer";

        return token + " " + line.substring(0, lookahead) + " " + lookahead;
    }

    public static String identifySymbol(String token) {
        String result;

        int lookahead = 1;
        for (int i = 1; i < token.length(); i++) {
            if (!Character.isLetterOrDigit(token.charAt(i))) {
                lookahead++;
            } else {
                break;
            }
        }

        if (token.length() > 1 && (token.charAt(0) == '!' || token.charAt(0) == '-' && Character.isLetterOrDigit(token.charAt(1)))) {
            if (token.charAt(0) == '!') {
                result = "UnaryOperatorNot";
            } else {
                result = "UnaryOperatorNegation";
            }
        } else {
            String t = token.substring(0, lookahead).trim();
            if (t.equals(";")) {
                result = "Statement";
            } else if (t.equals("||")) {
                result = "OrExpression";
            } else if (t.equals("&&")) {
                result = "AndExpression";
            } else if (t.equals("=")) {
                result = "AssignmentOperator";
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
                result = "SKIP";
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
    }

    public static class Lexeme {
        String token;
        String lexeme;

        public Lexeme(String token, String lexeme) {
            this.token = token;
            this.lexeme = lexeme;
        }

        @Override
        public String toString() {
            return String.format("%-20s %s", lexeme, token);
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