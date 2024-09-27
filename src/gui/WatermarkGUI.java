package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.awt.event.*;
import java.io.*;
import java.util.concurrent.*;


public class WatermarkGUI extends JFrame {
    private JTextField inputFileField;
    private JTextField outputFileField;
    private JTextField watermarkField;
    private JTextField keyField;
    private JButton embedButton;
    private JButton recognizeButton;

    public WatermarkGUI() {
        createUI();
    }

    private void createUI() {
        setTitle("Watermark Embedder & Recognizer");
        setSize(672, 406);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(6, 1, 10, 10));

        JPanel fileInputPanel = new JPanel();
        JLabel inputLabel = new JLabel("Input File:");
        inputFileField = new JTextField(20);
        JButton inputBrowse = new JButton("Browse");
        fileInputPanel.add(inputLabel);
        fileInputPanel.add(inputFileField);
        fileInputPanel.add(inputBrowse);

        JPanel fileOutputPanel = new JPanel();
        JLabel outputLabel = new JLabel("Output File:");
        outputFileField = new JTextField(20);
        JButton outputBrowse = new JButton("Browse");
        fileOutputPanel.add(outputLabel);
        fileOutputPanel.add(outputFileField);
        fileOutputPanel.add(outputBrowse);

        JPanel watermarkPanel = new JPanel();
        JLabel watermarkLabel = new JLabel("Watermark:");
        watermarkField = new JTextField(20);
        watermarkPanel.add(watermarkLabel);
        watermarkPanel.add(watermarkField);

        JPanel keyPanel = new JPanel();
        JLabel keyLabel = new JLabel("Key:");
        keyField = new JTextField(20);
        keyPanel.add(keyLabel);
        keyPanel.add(keyField);

        JPanel buttonPanel = new JPanel();
        embedButton = new JButton("Embed");
        recognizeButton = new JButton("Recognize");
        buttonPanel.add(embedButton);
        buttonPanel.add(recognizeButton);

        add(fileInputPanel);
        add(fileOutputPanel);
        add(watermarkPanel);
        add(keyPanel);
        add(buttonPanel);

        // Add functionality to buttons
        inputBrowse.addActionListener(e -> chooseFile(inputFileField));
        outputBrowse.addActionListener(e -> chooseFile(outputFileField));
        embedButton.addActionListener(e -> embedWatermark());
        recognizeButton.addActionListener(e -> recognizeWatermark());
    }

    private void chooseFile(JTextField textField) {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void embedWatermark() {
        try {
            String inputFile = inputFileField.getText();
            String outputFile = outputFileField.getText();
            String watermark = watermarkField.getText();
            String key = keyField.getText();

            // Example command, adjust according to your actual command structure
            String command = "java -jar -jar D:/ShellMarkDevelop/ShellMark/ShellMark.jar -E -A GTW -i " + inputFile + " -o " + outputFile + " -w " + watermark + " -k " + key;
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                JOptionPane.showMessageDialog(this, "Watermark embedded successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Error embedding watermark. Exit code: " + exitCode);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to execute embedding: " + e.getMessage());
        }
    }

    private void recognizeWatermark() {
        try {
            String inputFile = escapeSpaces(inputFileField.getText());
            String key = escapeSpaces(keyField.getText());

            // Ensure command line is correct with proper spaces and quotes
            String command = "java -jar D:/ShellMarkDevelop/ShellMark/ShellMark.jar -R -A GTW -i " + inputFile + " -k " + key;
            Process process = Runtime.getRuntime().exec(command);

            // Handle both streams in separate threads
            StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "OUTPUT");
            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "ERROR");

            // Start the gobblers
            outputGobbler.start();
            errorGobbler.start();

            // Wait for the process to end
            int exitCode = process.waitFor();
            outputGobbler.join(); // Ensure stream gobblers finish
            errorGobbler.join();

            String output = outputGobbler.getOutput();
            String errorOutput = errorGobbler.getOutput();

            //JOptionPane.showMessageDialog(this, output);

            if (exitCode == 0 && !output.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Watermark recognized successfully!\nOutput:" + output);
            } else if (!errorOutput.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Error recognizing watermark. Output:\n" + output + "\nError Output:\n" + errorOutput);
            } else {
                JOptionPane.showMessageDialog(this, "Watermark recognized successfully, but no output was generated.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to recognize watermark: " + e.getMessage());
        }
    }
    private String escapeSpaces(String path) {
        return path.contains(" ") ? "\"" + path + "\"" : path;
    }


    private String readOutput(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line + "\n");
        }
        return builder.toString();
    }





    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            WatermarkGUI ex = new WatermarkGUI();
            ex.setVisible(true);
        });
    }
}


class StreamGobbler extends Thread {
    private InputStream is;
    private String type;
    private StringBuilder output = new StringBuilder();

    public StreamGobbler(InputStream is, String type) {
        this.is = is;
        this.type = type;
    }

    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                output.append(line + "\n");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public String getOutput() {
        return output.toString();
    }
}
