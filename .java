
package buffer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
public class GitStyleEditor {
   static Scanner sc = new Scanner(System.in);
   static List<String> file = new ArrayList<>();
   static List<ContributorLogEntry> contributorLog = new ArrayList<>();
   static String currentFilePath = "example.txt";
   static SnapshotArray snapshotArray = new SnapshotArray();
   static JTextArea textArea;
   static String currentContributor = "";
   static int contributionCounter = 1;
   static class SnapshotArray {
       private final Map<String, List<List<String>>> contributorSnapshots = new HashMap<>();
       private final Map<String, List<Integer>> snapshotIds = new HashMap<>();
       private final Map<String, Integer> contributorVersion = new HashMap<>();
       public void takeSnapshot(List<String> currentFile, String contributor) {
           contributorSnapshots.putIfAbsent(contributor, new ArrayList<>());
           snapshotIds.putIfAbsent(contributor, new ArrayList<>());
           contributorSnapshots.get(contributor).add(new ArrayList<>(currentFile));
           snapshotIds.get(contributor).add(contributionCounter++);
           contributorVersion.put(contributor, contributorSnapshots.get(contributor).size() - 1);
       }
       public List<String> undo(AccessLevel level, String contributor) {
           if (level != AccessLevel.WRITE && level != AccessLevel.BOTH) {
               return getCurrentSnapshot(contributor);
           }
           if (contributorVersion.getOrDefault(contributor, 0) > 0) {
               contributorVersion.put(contributor, contributorVersion.get(contributor) - 1);
               return new ArrayList<>(contributorSnapshots.get(contributor).get(contributorVersion.get(contributor)));
           } else {
               return getCurrentSnapshot(contributor);
           }
       }
       public List<String> redo(AccessLevel level, String contributor) {
           if (level != AccessLevel.WRITE && level != AccessLevel.BOTH) {
               return getCurrentSnapshot(contributor);
           }
           int version = contributorVersion.getOrDefault(contributor, 0);
           List<List<String>> snapshots = contributorSnapshots.getOrDefault(contributor, new ArrayList<>());
           if (version < snapshots.size() - 1) {
               contributorVersion.put(contributor, version + 1);
               return new ArrayList<>(snapshots.get(version + 1));
           } else {
               return getCurrentSnapshot(contributor);
           }
       }
       public List<String> getCurrentSnapshot(String contributor) {
           List<List<String>> snapshots = contributorSnapshots.get(contributor);
           int version = contributorVersion.getOrDefault(contributor, 0);
           if (snapshots != null && !snapshots.isEmpty()) {
               return new ArrayList<>(snapshots.get(version));
           }
           return new ArrayList<>();
       }
       public boolean hasSnapshots(String contributor) {
           return contributorSnapshots.containsKey(contributor);
       }
       public void goToSnapshot(String contributor, int id) {
           List<Integer> ids = snapshotIds.get(contributor);
           if (ids != null) {
               for (int i = 0; i < ids.size(); i++) {
                   if (ids.get(i) == id) {
                       contributorVersion.put(contributor, i);
                       return;
                   }
               }
           }
           JOptionPane.showMessageDialog(null, "Snapshot ID not found for contributor.");
       }
   }
   static class ContributorLogEntry {
       int id;
       String contributor;
       String action;
       String timestamp;
       public ContributorLogEntry(int id, String contributor, String action) {
           this.id = id;
           this.contributor = contributor;
           this.action = action;
           this.timestamp = new Date().toString();
       }
       @Override
       public String toString() {
           return "ID: " + id + " | " + timestamp + " - " + contributor + " performed: " + action;
       }
   }
   enum AccessLevel {
       READ, WRITE, BOTH, NONE
   }
   static boolean authenticate(String level) {
       String password = JOptionPane.showInputDialog(null, "Enter password for " + level + " access:");
       return password != null && password.equals("admin123");
   }
   public static void main(String[] args) {
       loadFile(currentFilePath);
       JFrame frame = new JFrame("Git Style Editor");
       frame.setSize(800, 600);
       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       frame.setLayout(new BorderLayout());
       textArea = new JTextArea();
       textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
       textArea.setEditable(true);
       JScrollPane scrollPane = new JScrollPane(textArea);
       frame.add(scrollPane, BorderLayout.CENTER);
       JPanel buttonPanel = new JPanel(new FlowLayout());
       JButton uploadFileButton = new JButton("Upload File");
       JButton saveFileButton = new JButton("Save to File");
       JButton applyChangesButton = new JButton("Commit Changes");
       JButton undoButton = new JButton("Undo");
       JButton redoButton = new JButton("Redo");
       JButton logButton = new JButton("Show Contributor Log");
       JButton goToSnapshotButton = new JButton("Go To Snapshot");
       JButton logoutButton = new JButton("Logout");
       buttonPanel.add(uploadFileButton);
       buttonPanel.add(saveFileButton);
       buttonPanel.add(applyChangesButton);
       buttonPanel.add(undoButton);
       buttonPanel.add(redoButton);
       buttonPanel.add(logButton);
       buttonPanel.add(goToSnapshotButton);
       buttonPanel.add(logoutButton);
       frame.add(buttonPanel, BorderLayout.SOUTH);
       String[] accessOptions = {"READ", "WRITE", "BOTH", "EXIT"};
       String accessChoice = (String) JOptionPane.showInputDialog(
           null, "Choose Access Level:", "Access Level",
           JOptionPane.PLAIN_MESSAGE, null, accessOptions, accessOptions[0]);
       if (accessChoice == null || accessChoice.equals("EXIT")) {
           JOptionPane.showMessageDialog(null, "Program terminated.");
           System.exit(0);
       }
       AccessLevel level = AccessLevel.valueOf(accessChoice);
       if (!authenticate(level.name())) {
           JOptionPane.showMessageDialog(null, "Authentication Failed.");
           System.exit(0);
       }
       currentContributor = JOptionPane.showInputDialog(null, "Enter your name (Contributor):");
       if (currentContributor == null || currentContributor.trim().isEmpty()) {
           JOptionPane.showMessageDialog(null, "Contributor name is required.");
           System.exit(0);
       }
       if (!snapshotArray.hasSnapshots(currentContributor)) {
           snapshotArray.takeSnapshot(file, currentContributor);
       } else {
           file = snapshotArray.getCurrentSnapshot(currentContributor);
       }
       Runnable updateDisplay = () -> {
           StringBuilder sb = new StringBuilder();
           for (String line : file) {
               sb.append(line).append("\n");
           }
           textArea.setText(sb.toString());
       };
       updateDisplay.run();
       uploadFileButton.addActionListener(e -> {
           JFileChooser fileChooser = new JFileChooser();
           int result = fileChooser.showOpenDialog(frame);
           if (result == JFileChooser.APPROVE_OPTION) {
               currentFilePath = fileChooser.getSelectedFile().getAbsolutePath();
               loadFile(currentFilePath);
               snapshotArray.takeSnapshot(file, currentContributor);
               updateDisplay.run();
           }
       });
       saveFileButton.addActionListener(e -> {
           saveFile(currentFilePath);
           JOptionPane.showMessageDialog(null, "File saved to " + currentFilePath);
       });
       applyChangesButton.addActionListener(e -> {
           if (level == AccessLevel.WRITE || level == AccessLevel.BOTH) {
               file.clear();
               String[] lines = textArea.getText().split("\n");
               file.addAll(Arrays.asList(lines));
               contributorLog.add(new ContributorLogEntry(contributionCounter, currentContributor, "Committed file changes."));
               snapshotArray.takeSnapshot(file, currentContributor);
               JOptionPane.showMessageDialog(null, "Changes committed with ID: " + (contributionCounter - 1));
           } else {
               JOptionPane.showMessageDialog(null, "Unauthorized option.");
           }
       });
       undoButton.addActionListener(e -> {
           file = snapshotArray.undo(level, currentContributor);
           updateDisplay.run();
      
       });
       redoButton.addActionListener(e -> {
           file = snapshotArray.redo(level, currentContributor);
           updateDisplay.run();
       
       });
       logButton.addActionListener(e -> showContributorLog());
       goToSnapshotButton.addActionListener(e -> {
           String input = JOptionPane.showInputDialog("Enter Contributor Name and Snapshot ID (e.g. Alice,3):");
           if (input != null && input.contains(",")) {
               String[] parts = input.split(",");
               if (parts.length == 2) {
                   String name = parts[0].trim();
                   int id = Integer.parseInt(parts[1].trim());
                   snapshotArray.goToSnapshot(name, id);
                   if (name.equals(currentContributor)) {
                       file = snapshotArray.getCurrentSnapshot(currentContributor);
                       updateDisplay.run();
                   }
               }
           }
       });
       logoutButton.addActionListener(e -> {
           int choice = JOptionPane.showConfirmDialog(frame, "Are you sure you want to log out?", "Log Out", JOptionPane.YES_NO_OPTION);
           if (choice == JOptionPane.YES_OPTION) {
               frame.dispose();
               main(args); // Restart
           }
       });
       frame.setVisible(true);
   }
   static void loadFile(String path) {
       file.clear();
       try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
           String line;
           while ((line = reader.readLine()) != null) {
               file.add(line);
           }
       } catch (IOException e) {
           JOptionPane.showMessageDialog(null, "File not found: " + path);
       }
   }
   static void saveFile(String path) {
       try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
           for (String line : file) {
               writer.write(line);
               writer.newLine();
           }
       } catch (IOException e) {
           JOptionPane.showMessageDialog(null, "Error saving file: " + e.getMessage());
       }
   }
   static void showContributorLog() {
       if (contributorLog.isEmpty()) {
           JOptionPane.showMessageDialog(null, "No contributions logged yet.");
           return;
       }
       String[] options = {"Show All", "Search by Name", "Cancel"};
       int choice = JOptionPane.showOptionDialog(null, "Choose an option:", "Contributor Log",
               JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
               options, options[0]);
       if (choice == 0) {
           StringBuilder sb = new StringBuilder("Contributor Log:\n");
           for (ContributorLogEntry entry : contributorLog) {
               sb.append(entry).append("\n");
           }
           JOptionPane.showMessageDialog(null, sb.toString());
       } else if (choice == 1) {
           JTextField searchField = new JTextField(15);
           Object[] message = {"Search by Contributor Name:", searchField};
           int searchOpt = JOptionPane.showConfirmDialog(null, message, "Search Contributor Log", JOptionPane.OK_CANCEL_OPTION);
           if (searchOpt == JOptionPane.OK_OPTION) {
               String searchTerm = searchField.getText().trim();
               if (searchTerm.isEmpty()) {
                   JOptionPane.showMessageDialog(null, "Please enter a name to search.");
                   return;
               }
               List<ContributorLogEntry> filteredLog = new ArrayList<>();
               for (ContributorLogEntry entry : contributorLog) {
                   if (entry.contributor.equalsIgnoreCase(searchTerm)) {
                       filteredLog.add(entry);
                   }
               }
               if (!filteredLog.isEmpty()) {
                   StringBuilder sb = new StringBuilder("Filtered Logs for '" + searchTerm + "':\n");
                   for (ContributorLogEntry entry : filteredLog) {
                       sb.append(entry).append("\n");
                   }
                   JOptionPane.showMessageDialog(null, sb.toString());
               } else {
                   JOptionPane.showMessageDialog(null, "No entries found for '" + searchTerm + "'.");
               }
           }
       }
   }
}
