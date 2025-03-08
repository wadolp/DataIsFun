import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * GUI application for displaying UNESCO World Heritage Sites data
 */
public class UnescoDataViewer extends JFrame {
    private UnescoDataReader dataReader;
    private JTable dataTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterCategoryComboBox;
    private JTextField countryFilterTextField;
    private JCheckBox dangerStatusCheckBox;
    private JCheckBox transboundaryCheckBox;
    private JButton resetFiltersButton;
    private JLabel statusLabel;

    // Column names for the table
    private static final String[] COLUMN_NAMES = {
            "Name", "Country", "Inscription Year", "In Danger", "Removal Date",
            "Latitude", "Longitude", "Area (ha)", "Category", "Transboundary"
    };

    /**
     * Constructor for the UNESCO Data Viewer
     */
    public UnescoDataViewer() {
        // Set up the main frame
        setTitle("UNESCO World Heritage Sites Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        // Initialize data reader
        dataReader = new UnescoDataReader();

        // Create UI components
        createUIComponents();

        // Load initial data
        loadData();

        // Make the frame visible
        setVisible(true);
    }

    /**
     * Create and arrange UI components
     */
    private void createUIComponents() {
        // Create main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Create filter panel at the top
        JPanel filterPanel = createFilterPanel();
        mainPanel.add(filterPanel, BorderLayout.NORTH);

        // Create table model and table
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Make table read-only
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 3:  // In Danger
                    case 9:  // Transboundary
                        return Boolean.class;
                    case 2:  // Inscription Year
                        return Integer.class;
                    case 5:  // Latitude
                    case 6:  // Longitude
                    case 7:  // Area
                        return Double.class;
                    default:
                        return String.class;
                }
            }
        };

        dataTable = new JTable(tableModel);
        dataTable.setAutoCreateRowSorter(true);
        dataTable.setFillsViewportHeight(true);
        
        // Set column widths
        dataTable.getColumnModel().getColumn(0).setPreferredWidth(200);  // Name
        dataTable.getColumnModel().getColumn(1).setPreferredWidth(100);  // Country
        dataTable.getColumnModel().getColumn(4).setPreferredWidth(100);  // Removal Date
        dataTable.getColumnModel().getColumn(5).setPreferredWidth(80);   // Latitude
        dataTable.getColumnModel().getColumn(6).setPreferredWidth(80);   // Longitude
        dataTable.getColumnModel().getColumn(7).setPreferredWidth(80);   // Area
        dataTable.getColumnModel().getColumn(8).setPreferredWidth(100);  // Category

        // Create scroll pane for the table
        JScrollPane tableScrollPane = new JScrollPane(dataTable);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Create status panel at the bottom
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Ready");
        statusPanel.add(statusLabel);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        // Add main panel to the frame
        setContentPane(mainPanel);
    }

    /**
     * Create the filter panel with controls for filtering the data
     * @return panel with filtering controls
     */
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Options"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Category filter
        gbc.gridx = 0;
        gbc.gridy = 0;
        filterPanel.add(new JLabel("Category:"), gbc);

        gbc.gridx = 1;
        filterCategoryComboBox = new JComboBox<>(new String[]{"All", "Cultural", "Natural", "Mixed"});
        filterPanel.add(filterCategoryComboBox, gbc);

        // Country filter
        gbc.gridx = 2;
        filterPanel.add(new JLabel("Country:"), gbc);

        gbc.gridx = 3;
        countryFilterTextField = new JTextField(15);
        filterPanel.add(countryFilterTextField, gbc);

        // Danger status filter
        gbc.gridx = 4;
        dangerStatusCheckBox = new JCheckBox("In Danger");
        filterPanel.add(dangerStatusCheckBox, gbc);

        // Transboundary filter
        gbc.gridx = 5;
        transboundaryCheckBox = new JCheckBox("Transboundary");
        filterPanel.add(transboundaryCheckBox, gbc);

        // Apply filters button
        gbc.gridx = 6;
        JButton applyFiltersButton = new JButton("Apply Filters");
        applyFiltersButton.addActionListener(e -> applyFilters());
        filterPanel.add(applyFiltersButton, gbc);

        // Reset filters button
        gbc.gridx = 7;
        resetFiltersButton = new JButton("Reset Filters");
        resetFiltersButton.addActionListener(e -> resetFilters());
        filterPanel.add(resetFiltersButton, gbc);

        // Load data button
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        JButton loadDataButton = new JButton("Load UNESCO Data");
        loadDataButton.addActionListener(e -> selectAndLoadDataFile());
        filterPanel.add(loadDataButton, gbc);

        // Statistics button
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        JButton statsButton = new JButton("Show Statistics");
        statsButton.addActionListener(e -> showStatistics());
        filterPanel.add(statsButton, gbc);

        return filterPanel;
    }

    /**
     * Apply the selected filters to the data
     */
    private void applyFilters() {
        List<UnescoSite> filteredSites = dataReader.getAllSites();
        
        // Apply category filter
        String selectedCategory = (String) filterCategoryComboBox.getSelectedItem();
        if (!"All".equals(selectedCategory)) {
            filteredSites = dataReader.getSitesByCategory(selectedCategory);
        }
        
        // Apply country filter
        String countryFilter = countryFilterTextField.getText().trim();
        if (!countryFilter.isEmpty()) {
            filteredSites = filteredSites.stream()
                    .filter(site -> site.getCountry().toLowerCase().contains(countryFilter.toLowerCase()))
                    .toList();
        }
        
        // Apply danger status filter
        if (dangerStatusCheckBox.isSelected()) {
            filteredSites = filteredSites.stream()
                    .filter(UnescoSite::isInDanger)
                    .toList();
        }
        
        // Apply transboundary filter
        if (transboundaryCheckBox.isSelected()) {
            filteredSites = filteredSites.stream()
                    .filter(UnescoSite::isTransboundary)
                    .toList();
        }
        
        // Update table with filtered data
        updateTable(filteredSites);
        statusLabel.setText("Displaying " + filteredSites.size() + " sites");
    }

    /**
     * Reset all filters to their default state
     */
    private void resetFilters() {
        filterCategoryComboBox.setSelectedItem("All");
        countryFilterTextField.setText("");
        dangerStatusCheckBox.setSelected(false);
        transboundaryCheckBox.setSelected(false);
        
        // Update table with all data
        updateTable(dataReader.getAllSites());
        statusLabel.setText("Displaying " + dataReader.getAllSites().size() + " sites");
    }

    /**
     * Show statistics about the UNESCO sites
     */
    private void showStatistics() {
        String statistics = dataReader.getStatistics();
        JTextArea textArea = new JTextArea(statistics);
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        
        JOptionPane.showMessageDialog(this, scrollPane, "UNESCO Sites Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Allow user to select a CSV file to load
     */
    private void selectAndLoadDataFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select UNESCO Sites CSV File");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
            }
            public String getDescription() {
                return "CSV Files (*.csv)";
            }
        });
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                dataReader.readCsvFile(selectedFile.getAbsolutePath());
                updateTable(dataReader.getAllSites());
                statusLabel.setText("Loaded " + dataReader.getAllSites().size() + " sites from " + selectedFile.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error loading file: " + ex.getMessage(), 
                        "File Load Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Error loading file");
            }
        }
    }

    /**
     * Load the initial data
     */
    private void loadData() {
        try {
            // Try to load default file
            dataReader.readCsvFile("allsites.csv");
            updateTable(dataReader.getAllSites());
            statusLabel.setText("Loaded " + dataReader.getAllSites().size() + " sites");
        } catch (IOException ex) {
            statusLabel.setText("No data loaded. Use 'Load UNESCO Data' button to load a file.");
        }
    }

    /**
     * Update the table with the provided sites
     * @param sites list of sites to display
     */
    private void updateTable(List<UnescoSite> sites) {
        // Clear the table
        tableModel.setRowCount(0);
        
        // Add data rows
        for (UnescoSite site : sites) {
            Object[] rowData = {
                site.getName(),
                site.getCountry(),
                site.getInscriptionYear(),
                site.isInDanger(),
                site.getRemovalDate() != null ? site.getRemovalDate().toString() : "N/A",
                site.getLatitude(),
                site.getLongitude(),
                site.getArea(),
                site.getCategory(),
                site.isTransboundary()
            };
            tableModel.addRow(rowData);
        }
        
        // Update status
        statusLabel.setText("Displaying " + sites.size() + " UNESCO World Heritage Sites");
    }

    /**
     * Main method to start the application
     */
    public static void main(String[] args) {
        // Use system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Start the application on the EDT
        SwingUtilities.invokeLater(() -> new UnescoDataViewer());
    }
}