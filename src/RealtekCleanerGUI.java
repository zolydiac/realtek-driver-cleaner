import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;

public class RealtekCleanerGUI {

    private JFrame frame;
    private JLabel statusLabel;
    private JTextArea driverListArea;
    private JButton deleteButton;
    private ArrayList<Driver> realtekDrivers;

    public static void main(String[] args) {
        if (!isRunningAsAdmin()) {
            showAdminRequiredDialog();
            return;
        }

        SwingUtilities.invokeLater(() -> {
            RealtekCleanerGUI gui = new RealtekCleanerGUI();
            gui.createAndShowGUI();
        });
    }

    // Check admin privileges using net session command
    private static boolean isRunningAsAdmin() {
        try {
            ProcessBuilder pb = new ProcessBuilder("net", "session");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                // Read output
            }

            int exitCode = process.waitFor();
            return exitCode == 0;

        } catch (Exception e) {
            return false;
        }
    }

    // Display error dialog if not running as admin
    private static void showAdminRequiredDialog() {
        JFrame errorFrame = new JFrame("Administrator Required");
        errorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        errorFrame.setSize(400, 150);
        errorFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel errorLabel = new JLabel("This program requires Administrator privileges.");
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(errorLabel);
        panel.add(Box.createVerticalStrut(10));

        JLabel instructionLabel = new JLabel("Please right-click and select 'Run as administrator'");
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(instructionLabel);
        panel.add(Box.createVerticalStrut(20));

        JButton okButton = new JButton("OK");
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        okButton.addActionListener(e -> System.exit(0));
        panel.add(okButton);

        errorFrame.add(panel);
        errorFrame.setVisible(true);
    }

    // Create and display main GUI window
    private void createAndShowGUI() {
        frame = new JFrame("Realtek Driver Cleaner");
        frame.setSize(650, 450);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Realtek Audio Driver Cleanup Tool");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(10));

        statusLabel = new JLabel("Ready to scan for Realtek drivers");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        driverListArea = new JTextArea(12, 50);
        driverListArea.setEditable(false);
        driverListArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(driverListArea);
        mainPanel.add(scrollPane);
        mainPanel.add(Box.createVerticalStrut(20));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JButton scanButton = new JButton("Scan for Drivers");
        scanButton.addActionListener(e -> performScan());
        buttonPanel.add(scanButton);

        deleteButton = new JButton("Delete All Drivers");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(e -> performDeletion());
        buttonPanel.add(deleteButton);

        mainPanel.add(buttonPanel);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    // Scan system for Realtek drivers in background thread
    private void performScan() {
        statusLabel.setText("Scanning for Realtek drivers...");
        driverListArea.setText("");
        deleteButton.setEnabled(false);

        new Thread(() -> {
            realtekDrivers = scanForRealtekDrivers();

            SwingUtilities.invokeLater(() -> {
                if (realtekDrivers.isEmpty()) {
                    statusLabel.setText("No Realtek drivers found");
                    driverListArea.setText("Your system has no Realtek drivers installed.\n\n" +
                            "This is good! You can now install your motherboard-specific drivers.");
                } else {
                    statusLabel.setText("Found " + realtekDrivers.size() + " Realtek driver(s)");

                    StringBuilder sb = new StringBuilder();
                    sb.append("The following Realtek drivers were found:\n\n");
                    for (Driver driver : realtekDrivers) {
                        sb.append("• ").append(driver.getPublishedName());
                        sb.append(" (").append(driver.getOriginalName()).append(")\n");
                        sb.append("  Provider: ").append(driver.getProviderName()).append("\n\n");
                    }
                    driverListArea.setText(sb.toString());
                    deleteButton.setEnabled(true);
                }
            });
        }).start();
    }

    // Delete all found drivers after confirmation
    private void performDeletion() {
        int result = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure you want to delete " + realtekDrivers.size() + " Realtek driver(s)?\n\n" +
                        "This will remove all Realtek audio drivers from your system.\n" +
                        "You will need to install your motherboard-specific drivers after this.\n\n" +
                        "This action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        statusLabel.setText("Deleting drivers...");
        deleteButton.setEnabled(false);
        driverListArea.setText("Deletion in progress...\n\n");

        new Thread(() -> {
            boolean success = deleteDrivers(realtekDrivers);

            SwingUtilities.invokeLater(() -> {
                if (success) {
                    statusLabel.setText("Deletion complete!");
                    driverListArea.setText("All Realtek drivers have been successfully deleted.\n\n" +
                            "IMPORTANT: You must restart your PC before installing new drivers.");

                    showCompletionDialog();
                } else {
                    statusLabel.setText("Some deletions failed");
                    driverListArea.append("\n\nSome drivers could not be deleted. Check console for details.");
                }
            });
        }).start();
    }

    // Offer motherboard detection and restart after successful deletion
    private void showCompletionDialog() {
        Object[] options = {
                "Find My Motherboard Drivers",
                "Restart Now",
                "Close"
        };

        int choice = JOptionPane.showOptionDialog(
                frame,
                "All Realtek drivers have been deleted successfully!\n\n" +
                        "NEXT STEPS:\n" +
                        "1. Restart your PC\n" +
                        "2. Install your motherboard-specific audio drivers\n\n" +
                        "Would you like help finding your motherboard's audio drivers?",
                "Deletion Complete",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            openMotherboardDriverSearch();
        } else if (choice == 1) {
            restartComputer();
        }
    }

    // Detect motherboard and open browser search for audio drivers
    private void openMotherboardDriverSearch() {
        String manufacturer = "";
        String model = "";

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "wmic", "baseboard", "get", "Manufacturer,Product"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;
            boolean headerSkipped = false;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }

                String[] parts = line.split("\\s{2,}");
                if (parts.length >= 2) {
                    manufacturer = parts[0].trim();
                    model = parts[1].trim();
                }
            }

            process.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame,
                    "Could not detect motherboard information.\n" +
                            "Please manually search for your motherboard's audio drivers.",
                    "Detection Failed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (manufacturer.isEmpty() || model.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "Could not detect motherboard information.\n" +
                            "Please manually search for your motherboard's audio drivers.",
                    "Detection Failed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                frame,
                "Detected Motherboard:\n" +
                        "Manufacturer: " + manufacturer + "\n" +
                        "Model: " + model + "\n\n" +
                        "This will open your browser to search for audio drivers\n" +
                        "for this specific motherboard.\n\n" +
                        "NOTE: Look for 'Audio' or 'Realtek Audio' drivers,\n" +
                        "NOT BIOS or other driver types.\n\n" +
                        "Continue?",
                "Motherboard Detected",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String searchQuery = manufacturer + " " + model + " audio drivers download";
                String encodedQuery = URLEncoder.encode(searchQuery, "UTF-8");
                String searchUrl = "https://www.google.com/search?q=" + encodedQuery;

                Desktop.getDesktop().browse(new URI(searchUrl));

                JOptionPane.showMessageDialog(frame,
                        "Browser opened with search for your motherboard's audio drivers.\n\n" +
                                "Download and install the audio drivers, then restart your PC.\n\n" +
                                "TIP: Look for downloads from the manufacturer's official website.",
                        "Driver Search Opened",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame,
                        "Could not open browser.\n\n" +
                                "Please manually search for:\n" +
                                manufacturer + " " + model + " audio drivers",
                        "Browser Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Restart computer with 10 second delay
    private void restartComputer() {
        int confirm = JOptionPane.showConfirmDialog(
                frame,
                "Your computer will restart in 10 seconds.\n\n" +
                        "Make sure to save any open work!\n\n" +
                        "Continue with restart?",
                "Confirm Restart",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Runtime.getRuntime().exec("shutdown -r -t 10");
                JOptionPane.showMessageDialog(frame,
                        "Restart initiated. Your PC will restart in 10 seconds.",
                        "Restarting",
                        JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame,
                        "Could not restart automatically.\n" +
                                "Please restart your PC manually.",
                        "Restart Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Use pnputil to enumerate and find Realtek drivers
    private static ArrayList<Driver> scanForRealtekDrivers() {
        ArrayList<Driver> drivers = new ArrayList<>();

        try {
            ProcessBuilder pb = new ProcessBuilder("pnputil", "/enum-drivers");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String currentPublishedName = null;
            String currentOriginalName = null;
            String currentProviderName = null;

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    if (currentProviderName != null && currentProviderName.contains("Realtek")) {
                        Driver driver = new Driver(
                                currentPublishedName,
                                currentOriginalName,
                                currentProviderName
                        );
                        drivers.add(driver);
                    }

                    currentPublishedName = null;
                    currentOriginalName = null;
                    currentProviderName = null;

                } else if (line.contains("Published Name:")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length > 1) {
                        currentPublishedName = parts[1].trim();
                    }

                } else if (line.contains("Original Name:")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length > 1) {
                        currentOriginalName = parts[1].trim();
                    }

                } else if (line.contains("Provider Name:")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length > 1) {
                        currentProviderName = parts[1].trim();
                    }
                }
            }

            process.waitFor();

        } catch (Exception e) {
            System.err.println("Error scanning for drivers:");
            e.printStackTrace();
        }

        return drivers;
    }

    // Delete drivers using pnputil with force uninstall
    private static boolean deleteDrivers(ArrayList<Driver> drivers) {
        boolean allSuccessful = true;

        for (Driver driver : drivers) {
            String filename = driver.getPublishedName();

            System.out.println("Deleting: " + filename);

            try {
                ProcessBuilder deletePb = new ProcessBuilder(
                        "pnputil",
                        "/delete-driver",
                        filename,
                        "/uninstall",
                        "/force"
                );
                deletePb.redirectErrorStream(true);

                Process deleteProcess = deletePb.start();

                BufferedReader deleteReader = new BufferedReader(
                        new InputStreamReader(deleteProcess.getInputStream())
                );

                String deleteLine;
                while ((deleteLine = deleteReader.readLine()) != null) {
                    System.out.println("  " + deleteLine);
                }

                int exitCode = deleteProcess.waitFor();
                if (exitCode == 0) {
                    System.out.println("  ✓ Successfully deleted " + filename);
                } else {
                    System.out.println("  ✗ Failed to delete " + filename +
                            " (exit code: " + exitCode + ")");
                    allSuccessful = false;
                }

            } catch (Exception e) {
                System.out.println("  ✗ Error deleting " + filename + ": " +
                        e.getMessage());
                allSuccessful = false;
            }
        }

        return allSuccessful;
    }
}

// Simple data class for driver information
class Driver {
    private String publishedName;
    private String originalName;
    private String providerName;

    public Driver(String publishedName, String originalName, String providerName) {
        this.publishedName = publishedName;
        this.originalName = originalName;
        this.providerName = providerName;
    }

    public String getPublishedName() {
        return publishedName;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getProviderName() {
        return providerName;
    }
}