package gilp.similarityMeasure;


import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/*
 * Seth Barker
 *  24 April 2019
 *
 *  A basic swing interface to get weight, algorithm, and input file selections
 *  from the user. The alignment algorithm will be run upon selection
 *  of an input file and using the current weights and algorithm.
 *  The result will be shown in the GUI as well as
 *  written to a .txt file named "Alignment Results" with the current time
 *
 *  Saved Results files can also be selected and displayed by the GUI
 */
public class GUI_Example {
    public static void main(String[] args) {

        JFrame frame = new JFrame("Local Sequence Alignment");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(600, 300));
        frame.setResizable(true);

        /*
         * Set layout manager Frame uses GridBagLayout, which separates the window into
         * a grid, and allows elements to span multiple grid cells the size of the rows
         * and columns in the grid is adjustable
         */
        GridBagLayout gbl = new GridBagLayout();
        frame.setLayout(gbl);

        // A text area that will display error messages and the alignment result
        JTextArea output = new JTextArea();
        GridBagConstraints f = new GridBagConstraints();
        f.gridx = 2;
        f.gridy = 0;
        f.gridwidth = 2;
        f.gridheight = 4;
        f.fill = GridBagConstraints.BOTH;
        output.setPreferredSize(new Dimension(200, 100));
        output.setEditable(false);
        output.setText("Output goes here");
        output.setAutoscrolls(true);
        frame.add(output, f);

        // ComboBox for selection of the alignment algorithm
        String[] choices = { "Smith-Waterson", "Brute Force", "Divide and Conquer", "Greedy" };
        JComboBox<String> algorithmSelect = new JComboBox<String>(choices);
        GridBagConstraints a = new GridBagConstraints();
        a.gridx = 0;
        a.gridy = 4;
        a.gridwidth = 2;
        frame.add(algorithmSelect, a);

        /*
         * Three ComboBoxes to select scoring weights
         */
        // Integer[] intChoices = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        // choices modified by QA
        Integer[] matchChoices = { 1, 2 };
        Integer[] missMatchChoices = { 0, 1 };
        Integer[] spaceChoices = { -2, -1 };
        JComboBox<Integer> matchWeight = new JComboBox<Integer>(matchChoices);
        GridBagConstraints b = new GridBagConstraints();
        b.gridx = 1;
        b.gridy = 1;
        b.anchor = GridBagConstraints.WEST;
        b.fill = GridBagConstraints.BOTH;
        frame.add(matchWeight, b);

        JComboBox mismatchWeight = new JComboBox(missMatchChoices);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        frame.add(mismatchWeight, c);

        JComboBox spaceWeight = new JComboBox(spaceChoices);
        GridBagConstraints d = new GridBagConstraints();
        d.gridx = 1;
        d.gridy = 3;
        d.anchor = GridBagConstraints.WEST;
        d.fill = GridBagConstraints.BOTH;
        frame.add(spaceWeight, d);

        JFileChooser fileChooser = new JFileChooser();

        // Code to be run upon selection of an input file
        fileChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get file
                File input = fileChooser.getSelectedFile();

                if (input != null) {
                    String fileName = input.getName();
                    // fileExtention is the last four characters of the file name
                    String fileExtention = fileName.substring(fileName.length() - 4, fileName.length());

                    // Only accept files ending in .txt or .rtf
                    if (fileExtention.equals(".txt") || fileExtention.equals(".rtf")) {
                        try {
                            // Get the input strings from the file and pass them to the appropriate
                            // algorithm
                            Scanner fileScanner = new Scanner(input);

                            if (!fileScanner.hasNext()) {
                                output.setText("ERROR: Empty file.");// This is wrong
                            } else {

                                String sequence1 = fileScanner.nextLine();
                                sequence1 = sequence1.trim();
                                sequence1 = sequence1.toUpperCase();

                                // QA fixed accidental commented line
                                fileScanner.nextLine();// File will have an empty line in it, so we advance
                                // the scanner past it

                                String sequence2 = fileScanner.nextLine();
                                sequence2 = sequence2.trim();
                                sequence2 = sequence2.toUpperCase();

                                // Check sequences for invalid characters
                                // Valid chars are A C T and G only
                                boolean isSequenceValid = true;

                                for (int i = 0; i < sequence1.length(); i++) {
                                    char base = sequence1.charAt(i);
                                    if (base != 'A' && base != 'T' && base != 'C' && base != 'G') {
                                        output.setText("Invalid character in sequence");
                                        isSequenceValid = false;
                                        return; // added return here QA
                                    }
                                }
                                for (int i = 0; i < sequence2.length(); i++) {
                                    char base = sequence2.charAt(i);
                                    if (base != 'A' && base != 'T' && base != 'C' && base != 'G') {
                                        output.setText("Invalid character in sequence");
                                        isSequenceValid = false;
                                        return; // added return here QA
                                    }
                                }

                                // QA note: we prefer to have Sequence 1 to be the longest string
                                if (sequence1.length() < sequence2.length()) {
                                    String tempString = sequence2;
                                    sequence2 = sequence1;
                                    sequence1 = tempString;
                                }

                                // Get scoring weights from ComboBoxes
                                int match = matchChoices[matchWeight.getSelectedIndex()];
                                int mismatch = missMatchChoices[mismatchWeight.getSelectedIndex()];
                                int space = spaceChoices[spaceWeight.getSelectedIndex()];

                                // Pass the sequences and scoring weights to the alignment algorithm methods
                                String algorithm = (String) algorithmSelect.getSelectedItem();
                                LocalAlignmentAlgorithms align = new LocalAlignmentAlgorithms();

                                try {

                                    if (algorithm.equals("Smith-Waterson")) {
                                        output.setText(
                                                align.SmithWaterman(sequence1, sequence2, match, mismatch, space));
                                    } else if (algorithm.equals("Brute Force")) {
                                        output.setText(align.BruteForce(sequence1, sequence2, match, mismatch, space));
                                    } else if (algorithm.equals("Divide and Conquer")) {
                                        output.setText(
                                                align.DivideAndConquer(sequence1, sequence2, match, mismatch, space));
                                    } else if (algorithm.equals("Greedy")) {
                                        output.setText(align.greedy(sequence1, sequence2, match, mismatch, space));
                                    } else {
                                        output.setText("ERROR: Invalid algorithm selection.");
                                    }
                                } catch (Exception exception) {
                                    output.setText(exception.getLocalizedMessage());
                                }
                            }

                        } catch (FileNotFoundException e1) {
                            output.setText("ERROR: File not found.");
                        }
                    } else {
                        output.setText("ERROR: Invalid file format.");
                    }
                }
            }
        });

        // A buttons that opens a file chooser dialog
        JButton fileButton = new JButton("Choose File");
        GridBagConstraints e = new GridBagConstraints();
        e.gridx = 2;
        e.gridy = 4;
        frame.add(fileButton, e);
        fileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.showOpenDialog(fileButton);
            }
        });
        JFileChooser savedFileChooser = new JFileChooser();

        // Code to be run on selection of a results file

        JButton openSaved = new JButton("Open saved file");
        GridBagConstraints k = new GridBagConstraints();
        k.gridx = 3;
        k.gridy = 4;
        frame.add(openSaved, k);
        openSaved.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get file
                savedFileChooser.showOpenDialog(frame);
                File input = savedFileChooser.getSelectedFile();
                if (input != null) {
                    String fileName = input.getName();
                    // fileExtention is the last four characters of the file name
                    String fileExtention = fileName.substring(fileName.length() - 4, fileName.length());

                    // Only accept files ending in .txt or .rtf
                    if (fileExtention.equals(".txt") || fileExtention.equals(".rtf")) {
                        try {
                            Scanner fileScanner = new Scanner(input);

                            if (!fileScanner.hasNext()) {
                                output.setText("ERROR: Empty file.");
                            } else {
                                // Write contents of file to output
                                output.setText("Writing saved file... \n ");
                                while (fileScanner.hasNext()) {
                                    output.append(fileScanner.nextLine() + "\n");
                                }
                            }
                        } catch (FileNotFoundException e1) {
                            output.setText("ERROR: File not found.");
                        }
                    } else {
                        output.setText("ERROR: Invalid file format.");
                    }
                }
            }
        });

        JButton saveFile = new JButton("Save current output");
        GridBagConstraints l = new GridBagConstraints();
        l.gridx = 4;
        l.gridy = 4;
        frame.add(saveFile, l);
        saveFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String textToBeSaved = output.getText();
                if (savedFileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    try {
                        File file = savedFileChooser.getSelectedFile();
                        FileWriter fileWriter = new FileWriter(file);

                        fileWriter.write(textToBeSaved);
                        fileWriter.close();
                    } catch (IOException exception) {
                        // exception.printStackTrace();
                    }
                }
            }
        });

        /*
         *
         * JLabels that go next to their respective score selection boxes
         */

        JLabel weights = new JLabel("Enter Scoring Weights");
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        g.anchor = GridBagConstraints.CENTER;
        frame.add(weights, g);

        JLabel match = new JLabel("Match");
        GridBagConstraints h = new GridBagConstraints();
        h.gridx = 0;
        h.gridy = 1;
        h.anchor = GridBagConstraints.WEST;
        frame.add(match, h);

        JLabel mismatch = new JLabel("Mismatch");
        GridBagConstraints i = new GridBagConstraints();
        i.gridx = 0;
        i.gridy = 2;
        i.anchor = GridBagConstraints.WEST;
        frame.add(mismatch, i);

        JLabel gap = new JLabel("Gap");
        GridBagConstraints j = new GridBagConstraints();
        j.gridx = 0;
        j.gridy = 3;
        j.anchor = GridBagConstraints.WEST;
        frame.add(gap, j);

        // Set look and feel to system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (InstantiationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (UnsupportedLookAndFeelException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        frame.setVisible(true);

    }
}
