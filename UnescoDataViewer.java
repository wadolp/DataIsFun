import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * GUI application for displaying UNESCO World Heritage Sites data
 * with enhanced visualization features including charts and statistics
 */
public class UnescoDataViewer extends JFrame {
    private UnescoDataReader dataReader;
    private List<UnescoSite> currentSites;

    // Table components
    private JTable dataTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;

    // Filter components
    private JComboBox<String> filterCategoryComboBox;
    private JTextField countryFilterTextField;
    private JCheckBox dangerStatusCheckBox;
    private JCheckBox transboundaryCheckBox;
    private JButton applyFiltersButton;
    private JButton resetFiltersButton;

    // Status components
    private JLabel statusLabel;

    // Visualization panels
    private DisplayGraphics chartPanel;
    private DisplayStats statsPanel;

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
        setSize(1300, 800);
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
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create top panel with title and filters
        JPanel topPanel = createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Create table panel
        JPanel tablePanel = createTablePanel();

        // Create visualization panels
        chartPanel = new DisplayGraphics();
        statsPanel = new DisplayStats();


        // Create tabbed pane for right side
        JTabbedPane rightTabbedPane = new JTabbedPane();
        rightTabbedPane.addTab("Chart", null, chartPanel, "View data visualizations");
        rightTabbedPane.addTab("Statistics", null, statsPanel, "View statistical analysis");


        // Create split pane for main content
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePanel, rightTabbedPane);
        mainSplitPane.setDividerLocation(600);
        mainSplitPane.setOneTouchExpandable(true);
        mainPanel.add(mainSplitPane, BorderLayout.CENTER);

        // Create status panel at the bottom
        JPanel statusPanel = createStatusPanel();
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        // Add main panel to the frame
        setContentPane(mainPanel);
    }

    /**
     * Create the top panel with title and filters
     * @return the top panel
     */
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());

        // Create title label
        JLabel titleLabel = new JLabel("UNESCO World Heritage Sites Explorer");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        topPanel.add(titleLabel, BorderLayout.NORTH);

        // Create filter panel
        JPanel filterPanel = createFilterPanel();
        topPanel.add(filterPanel, BorderLayout.CENTER);

        return topPanel;
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
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Category filter
        gbc.gridx = 0;
        gbc.gridy = 0;
        filterPanel.add(new JLabel("Category:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        filterCategoryComboBox = new JComboBox<>(new String[]{"All", "Cultural", "Natural", "Mixed"});
        filterPanel.add(filterCategoryComboBox, gbc);

        // Country filter
        gbc.gridx = 2;
        gbc.weightx = 0.0;
        filterPanel.add(new JLabel("Country:"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 1.0;
        countryFilterTextField = new JTextField(15);
        filterPanel.add(countryFilterTextField, gbc);

        // Danger status filter
        gbc.gridx = 4;
        gbc.weightx = 0.5;
        dangerStatusCheckBox = new JCheckBox("In Danger");
        filterPanel.add(dangerStatusCheckBox, gbc);

        // Transboundary filter
        gbc.gridx = 5;
        gbc.weightx = 0.5;
        transboundaryCheckBox = new JCheckBox("Transboundary");
        filterPanel.add(transboundaryCheckBox, gbc);

        // Apply filters button
        gbc.gridx = 6;
        gbc.weightx = 0.5;
        applyFiltersButton = new JButton("Apply Filters");
        applyFiltersButton.addActionListener(e -> applyFilters());
        filterPanel.add(applyFiltersButton, gbc);

        // Reset filters button
        gbc.gridx = 7;
        gbc.weightx = 0.5;
        resetFiltersButton = new JButton("Reset Filters");
        resetFiltersButton.addActionListener(e -> resetFilters());
        filterPanel.add(resetFiltersButton, gbc);

        // Load data button
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        JButton loadDataButton = new JButton("Load UNESCO Data");
        loadDataButton.addActionListener(e -> selectAndLoadDataFile());
        filterPanel.add(loadDataButton, gbc);

        return filterPanel;
    }

    /**
     * Create the table panel with data table
     * @return panel with data table
     */
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("UNESCO Sites"));

        // Create table model with proper types
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

        // Create table
        dataTable = new JTable(tableModel);

        // Create row sorter AFTER table model is initialized
        tableSorter = new TableRowSorter<>(tableModel);
        dataTable.setRowSorter(tableSorter);

        // Disable auto-sorting to avoid unexpected behavior
        dataTable.setAutoCreateRowSorter(false);

        dataTable.setFillsViewportHeight(true);
        dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add selection listener
        dataTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    displaySelectedSiteDetails();
                }
            }
        });

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
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        // Create table controls panel
        JPanel tableControlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel sortLabel = new JLabel("Sort by: ");
        tableControlsPanel.add(sortLabel);

        // Create sort selector
        String[] sortOptions = {"Name", "Country", "Inscription Year", "Area"};
        JComboBox<String> sortSelector = new JComboBox<>(sortOptions);

        // Add radio buttons for sort direction
        JRadioButton ascendingButton = new JRadioButton("Ascending", true);
        JRadioButton descendingButton = new JRadioButton("Descending");
        ButtonGroup sortDirectionGroup = new ButtonGroup();
        sortDirectionGroup.add(ascendingButton);
        sortDirectionGroup.add(descendingButton);

        // Action to apply the current sort settings - with safety checks
        ActionListener sortActionListener = e -> {
            try {
                // Make sure there's data to sort
                if (tableModel.getRowCount() == 0) {
                    statusLabel.setText("No data to sort.");
                    return;
                }

                String selected = (String) sortSelector.getSelectedItem();
                int columnIndex = 0;
                switch (selected) {
                    case "Country":
                        columnIndex = 1;
                        break;
                    case "Inscription Year":
                        columnIndex = 2;
                        break;
                    case "Area":
                        columnIndex = 7;
                        break;
                    default:
                        columnIndex = 0; // Name
                }

                // Determine sort order
                SortOrder sortOrder = ascendingButton.isSelected() ? SortOrder.ASCENDING : SortOrder.DESCENDING;

                // Safely set sort keys - clear first then add new one
                tableSorter.setSortKeys(null);
                tableSorter.setMaxSortKeys(1);

                // Create and apply new sort key
                List<RowSorter.SortKey> sortKeys = new ArrayList<>();
                sortKeys.add(new RowSorter.SortKey(columnIndex, sortOrder));
                tableSorter.setSortKeys(sortKeys);

                // Update status message
                statusLabel.setText("Sorted by " + selected + " in " +
                        (sortOrder == SortOrder.ASCENDING ? "ascending" : "descending") +
                        " order. Displaying " + dataTable.getRowCount() + " sites.");

            } catch (Exception ex) {
                // Handle any exceptions gracefully
                statusLabel.setText("Error during sorting: " + ex.getMessage());
                ex.printStackTrace();
            }
        };

        // Add action listeners
        sortSelector.addActionListener(sortActionListener);
        ascendingButton.addActionListener(sortActionListener);
        descendingButton.addActionListener(sortActionListener);

        // Add controls to panel
        tableControlsPanel.add(sortSelector);
        tableControlsPanel.add(ascendingButton);
        tableControlsPanel.add(descendingButton);

        // Add a sort button for explicit sorting
        JButton sortButton = new JButton("Apply Sort");
        sortButton.addActionListener(sortActionListener);
        tableControlsPanel.add(sortButton);

        // Add controls panel to the table panel
        tablePanel.add(tableControlsPanel, BorderLayout.SOUTH);

        return tablePanel;
    }

    /**
     * Create the details panel for showing information about a selected site
     * @return panel with details information
     */
    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create text area for details
        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setText("Select a site in the table to see details.");

        // Add text area to scroll pane
        JScrollPane scrollPane = new JScrollPane(detailsArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create the status panel for showing status information
     * @return panel with status information
     */
    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        statusPanel.add(statusLabel, BorderLayout.WEST);

        return statusPanel;
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

        // Update data display
        updateData(filteredSites);
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

        // Update data display
        updateData(dataReader.getAllSites());
        statusLabel.setText("Displaying " + dataReader.getAllSites().size() + " sites");
    }

    /**
     * Display details for the currently selected site
     */
    private void displaySelectedSiteDetails() {
        try {
            int selectedRow = dataTable.getSelectedRow();
            if (selectedRow >= 0 && currentSites != null && tableModel.getRowCount() > 0) {
                // Convert view index to model index to get the correct site
                int modelRow = dataTable.convertRowIndexToModel(selectedRow);

                if (modelRow >= 0 && modelRow < tableModel.getRowCount()) {
                    // Get the name from the model to find the matching site
                    String siteName = (String) tableModel.getValueAt(modelRow, 0);

                    // Find the site in our currentSites list
                    UnescoSite selectedSite = currentSites.stream()
                            .filter(site -> site.getName().equals(siteName))
                            .findFirst()
                            .orElse(null);

                    if (selectedSite != null) {
                        statsPanel.displaySiteDetails(selectedSite);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error displaying site details: " + e.getMessage());
            e.printStackTrace();
        }
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
                List<UnescoSite> allSites = dataReader.getAllSites();
                updateData(allSites);
                statusLabel.setText("Loaded " + allSites.size() + " sites from " + selectedFile.getName());
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
            List<UnescoSite> allSites = dataReader.getAllSites();
            updateData(allSites);
            statusLabel.setText("Loaded " + allSites.size() + " sites");
        } catch (IOException ex) {
            statusLabel.setText("No data loaded. Use 'Load UNESCO Data' button to load a file.");
        }
    }

    /**
     * Update all data visualization components
     * @param sites list of sites to display
     */
    private void updateData(List<UnescoSite> sites) {
        this.currentSites = sites;
        updateTable(sites);
        chartPanel.updateVisualization(sites);
        statsPanel.updateStatistics(sites);
    }

    /**
     * Update the table with the provided sites
     * @param sites list of sites to display
     */
    private void updateTable(List<UnescoSite> sites) {
        try {
            // First, we need to disable sorting temporarily to prevent errors
            // when modifying the table data
            List<? extends RowSorter.SortKey> currentSortKeys = null;
            if (tableSorter != null) {
                currentSortKeys = tableSorter.getSortKeys();
                tableSorter.setSortKeys(null);
            }

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

            // Restore sort keys if they existed
            if (tableSorter != null && currentSortKeys != null && !currentSortKeys.isEmpty()) {
                tableSorter.setSortKeys(currentSortKeys);
            }
        } catch (Exception e) {
            // Print exception for debugging
            System.err.println("Error updating table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update the chart visualization
     * Called when the chart type is changed
     */
    public void updateCharts() {
        if (currentSites != null) {
            chartPanel.updateVisualization(currentSites);
        }
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