import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.*;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Lab 8: Tag/Keyword Extractor
 * Lets the user choose a text file and a stop-word file, extracts tags,
 * displays their frequencies, and saves the output.
 */
public class TagExtractorFrame extends JFrame {

    private File sourceFile;
    private File stopWordFile;

    private final JLabel sourceFileLabel = new JLabel("Source file: None selected");
    private final JLabel stopFileLabel = new JLabel("Stop-word file: None selected");

    private final JButton chooseSourceButton = new JButton("Choose Text File");
    private final JButton chooseStopButton = new JButton("Choose Stop-Word File");
    private final JButton extractButton = new JButton("Extract Tags");
    private final JButton saveButton = new JButton("Save Output");
    private final JButton clearButton = new JButton("Clear");
    private final JButton quitButton = new JButton("Quit");

    private final JTextArea outputArea = new JTextArea(25, 50);

    public TagExtractorFrame() {
        setTitle("Tag / Keyword Extractor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        add(topPanel(), BorderLayout.NORTH);
        add(centerPanel(), BorderLayout.CENTER);
        add(bottomPanel(), BorderLayout.SOUTH);

        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        chooseSourceButton.addActionListener(e -> chooseSourceFile());
        chooseStopButton.addActionListener(e -> chooseStopWordFile());
        extractButton.addActionListener(e -> extractTags());
        saveButton.addActionListener(e -> saveOutput());
        clearButton.addActionListener(e -> clearOutput());
        quitButton.addActionListener(e -> System.exit(0));

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel topPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JPanel filePanel = new JPanel(new GridLayout(2, 1, 5, 5));
        filePanel.setBorder(new TitledBorder("Selected Files"));
        filePanel.add(sourceFileLabel);
        filePanel.add(stopFileLabel);

        JPanel buttonPanel1 = new JPanel(new FlowLayout());
        buttonPanel1.add(chooseSourceButton);
        buttonPanel1.add(chooseStopButton);

        JPanel buttonPanel2 = new JPanel(new FlowLayout());
        buttonPanel2.add(extractButton);
        buttonPanel2.add(saveButton);
        buttonPanel2.add(clearButton);
        buttonPanel2.add(quitButton);

        panel.add(filePanel);
        panel.add(buttonPanel1);
        panel.add(buttonPanel2);

        return panel;
    }

    private JPanel centerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Extracted Tags and Frequencies"));

        JScrollPane scrollPane = new JScrollPane(outputArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel bottomPanel() {
        JPanel panel = new JPanel();
        JLabel noteLabel = new JLabel("Choose both files, then click Extract Tags.");
        panel.add(noteLabel);
        return panel;
    }

    private void chooseSourceFile() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            sourceFile = chooser.getSelectedFile();
            sourceFileLabel.setText("Source file: " + sourceFile.getName());
        }
    }

    private void chooseStopWordFile() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            stopWordFile = chooser.getSelectedFile();
            stopFileLabel.setText("Stop-word file: " + stopWordFile.getName());
        }
    }

    private void extractTags() {
        if (sourceFile == null || stopWordFile == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please choose both a source text file and a stop-word file.",
                    "Missing File",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            Set<String> stopWords = loadStopWords(stopWordFile);
            Map<String, Integer> frequencyMap = scanTextFile(sourceFile, stopWords);

            StringBuilder sb = new StringBuilder();
            sb.append("Source File: ").append(sourceFile.getName()).append("\n");
            sb.append("Stop-Word File: ").append(stopWordFile.getName()).append("\n");
            sb.append("========================================\n");

            for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
                sb.append(String.format("%-20s %d%n", entry.getKey(), entry.getValue()));
            }

            outputArea.setText(sb.toString());
            outputArea.setCaretPosition(0);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error reading file: " + ex.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private Set<String> loadStopWords(File file) throws IOException {
        Set<String> stopWords = new TreeSet<>();

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String word = scanner.nextLine().trim().toLowerCase();
                if (!word.isEmpty()) {
                    stopWords.add(word);
                }
            }
        }

        return stopWords;
    }

    private Map<String, Integer> scanTextFile(File file, Set<String> stopWords) throws IOException {
        Map<String, Integer> frequencyMap = new TreeMap<>();

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().toLowerCase();

                String[] tokens = line.split("\\s+");

                for (String token : tokens) {
                    String cleaned = token.replaceAll("[^a-z]", "");

                    if (!cleaned.isEmpty() && !stopWords.contains(cleaned)) {
                        frequencyMap.put(cleaned, frequencyMap.getOrDefault(cleaned, 0) + 1);
                    }
                }
            }
        }

        return frequencyMap;
    }

    private void saveOutput() {
        if (outputArea.getText().isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "There is no output to save yet.",
                    "Nothing to Save",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JFileChooser chooser = new JFileChooser();
        int result = chooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File outFile = chooser.getSelectedFile();

            try (PrintWriter writer = new PrintWriter(outFile)) {
                writer.print(outputArea.getText());

                JOptionPane.showMessageDialog(
                        this,
                        "Output saved successfully."
                );
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Error saving file: " + ex.getMessage(),
                        "Save Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void clearOutput() {
        sourceFile = null;
        stopWordFile = null;
        sourceFileLabel.setText("Source file: None selected");
        stopFileLabel.setText("Stop-word file: None selected");
        outputArea.setText("");
    }
}