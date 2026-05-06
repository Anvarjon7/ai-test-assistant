package org.example.aitestassistant.ui;

import javax.swing.*;
import java.awt.*;

public class FailureToolWindowPanel {

    private final JPanel root;
    private final JTextArea outputArea;

    public FailureToolWindowPanel() {

        this.root = new JPanel(new BorderLayout());

        this.outputArea = new JTextArea("Waiting for test failures...");
        this.outputArea.setEditable(false);
        this.outputArea.setLineWrap(true);
        this.outputArea.setWrapStyleWord(true);
        this.outputArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JScrollPane scrollPane = new JScrollPane(outputArea);
        root.add(scrollPane, BorderLayout.CENTER);
    }

    public JComponent getContent(){
        return root;
    }

    public void showResult(String text) {
        SwingUtilities.invokeLater(() -> outputArea.setText(text));
    }

    public void showLoading() {
        SwingUtilities.invokeLater(() -> outputArea.setText("Analyzing failure..."));
    }

    public void showError(String error) {
        SwingUtilities.invokeLater(() -> outputArea.setText("Error: " + error));
    }
}
