import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.Year;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.OptionalDouble;

/**
 * Class for displaying statistics about UNESCO World Heritage Sites data
 */
public class DisplayStats extends JPanel {
    private JTable statsTable;
    private DefaultTableModel statsModel;
    private JTextArea detailsArea;
    private List<UnescoSite> currentSites;

    /**
     * Constructor for the DisplayStats panel
     */
    public DisplayStats() {
        setLayout(new BorderLayout());

        // Create statistics panel
        JPanel statsPanel = createStatsPanel();

        // Create details panel
        JPanel detailsPanel = createDetailsPanel();

        // Add components to split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, statsPanel, detailsPanel);
        splitPane.setDividerLocation(200);
        splitPane.setOneTouchExpandable(true);

        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * Create the statistics panel with a table for showing aggregate statistics
     * @return panel with statistics table
     */
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Statistics"));

        // Create table model for statistics
        String[] columnNames = {"Statistic", "Value"};
        statsModel = new DefaultTableModel(columnNames, 0);
        statsTable = new JTable(statsModel);
        statsTable.setAutoCreateRowSorter(false);
        statsTable.setFillsViewportHeight(true);
        statsTable.setRowHeight(25);

        // Make first column wider
        statsTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        statsTable.getColumnModel().getColumn(1).setPreferredWidth(150);

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(statsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Create the details panel for showing information about a selected site
     * @return panel with details text area
     */
    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Selected Site Details"));

        // Create text area for details
        detailsArea = new JTextArea();
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
     * Update the statistics based on the current list of sites
     * @param sites list of UNESCO sites to analyze
     */
    public void updateStatistics(List<UnescoSite> sites) {
        if (sites == null || sites.isEmpty()) {
            clearStatistics();
            return;
        }

        this.currentSites = sites;

        // Clear existing statistics
        statsModel.setRowCount(0);

        // Add general statistics
        addStatistic("Total number of sites", String.valueOf(sites.size()));

        // Count by category
        Map<String, Long> categoryCounts = sites.stream()
                .collect(Collectors.groupingBy(UnescoSite::getCategory, Collectors.counting()));

        addStatistic("Cultural sites", formatCount(categoryCounts.getOrDefault("Cultural", 0L), sites.size()));
        addStatistic("Natural sites", formatCount(categoryCounts.getOrDefault("Natural", 0L), sites.size()));
        addStatistic("Mixed sites", formatCount(categoryCounts.getOrDefault("Mixed", 0L), sites.size()));

        // Danger status
        long sitesInDanger = sites.stream().filter(UnescoSite::isInDanger).count();
        addStatistic("Sites in danger", formatCount(sitesInDanger, sites.size()));

        // Transboundary status
        long transboundarySites = sites.stream().filter(UnescoSite::isTransboundary).count();
        addStatistic("Transboundary sites", formatCount(transboundarySites, sites.size()));

        // Area statistics
        DoubleSummaryStatistics areaStats = sites.stream()
                .mapToDouble(UnescoSite::getArea)
                .summaryStatistics();

        addStatistic("Total area (hectares)", String.format("%.2f", areaStats.getSum()));
        addStatistic("Average area (hectares)", String.format("%.2f", areaStats.getAverage()));
        addStatistic("Smallest site (hectares)", String.format("%.2f", areaStats.getMin()));
        addStatistic("Largest site (hectares)", String.format("%.2f", areaStats.getMax()));

        // Inscription year statistics
        IntSummaryStatistics yearStats = sites.stream()
                .mapToInt(UnescoSite::getInscriptionYear)
                .summaryStatistics();

        int currentYear = Year.now().getValue();
        addStatistic("Oldest inscription", String.valueOf(yearStats.getMin()));
        addStatistic("Most recent inscription", String.valueOf(yearStats.getMax()));
        addStatistic("Average age (years)", String.format("%.1f", currentYear - yearStats.getAverage()));

        // Top country
        Map<String, Long> countryCounts = sites.stream()
                .collect(Collectors.groupingBy(UnescoSite::getCountry, Collectors.counting()));

        String topCountry = countryCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey() + " (" + entry.getValue() + " sites)")
                .orElse("None");

        addStatistic("Country with most sites", topCountry);
    }

    /**
     * Format count as "X (Y%)"
     * @param count count value
     * @param total total number for percentage calculation
     * @return formatted string
     */
    private String formatCount(long count, long total) {
        double percentage = (total > 0) ? (count * 100.0 / total) : 0.0;
        return count + " (" + String.format("%.1f", percentage) + "%)";
    }

    /**
     * Add a statistic to the table
     * @param name name of the statistic
     * @param value value of the statistic
     */
    private void addStatistic(String name, String value) {
        statsModel.addRow(new Object[]{name, value});
    }

    /**
     * Clear all statistics
     */
    private void clearStatistics() {
        statsModel.setRowCount(0);
        addStatistic("Total number of sites", "0");
        detailsArea.setText("No data available.");
    }

    /**
     * Display details for a selected site
     * @param site UNESCO site to display details for
     */
    public void displaySiteDetails(UnescoSite site) {
        if (site == null) {
            detailsArea.setText("No site selected.");
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("Site Name: ").append(site.getName()).append("\n\n");
        details.append("Country: ").append(site.getCountry()).append("\n");
        details.append("Category: ").append(site.getCategory()).append("\n");
        details.append("Inscription Year: ").append(site.getInscriptionYear()).append("\n");
        details.append("Area: ").append(String.format("%.2f", site.getArea())).append(" hectares\n");
        details.append("Coordinates: ").append(site.getLatitude()).append(", ").append(site.getLongitude()).append("\n");
        details.append("In Danger: ").append(site.isInDanger() ? "Yes" : "No").append("\n");
        details.append("Transboundary: ").append(site.isTransboundary() ? "Yes" : "No").append("\n");

        if (site.getRemovalDate() != null) {
            details.append("Removal Date: ").append(site.getRemovalDate()).append("\n");
        }

        // Add some contextual information if available
        if (currentSites != null && !currentSites.isEmpty()) {
            // Find rank by area
            List<UnescoSite> sitesByArea = currentSites.stream()
                    .sorted((s1, s2) -> Double.compare(s2.getArea(), s1.getArea()))
                    .collect(Collectors.toList());

            int areaRank = sitesByArea.indexOf(site) + 1;
            int totalSites = currentSites.size();

            if (areaRank > 0) {
                details.append("\nArea Rank: ").append(areaRank).append(" of ").append(totalSites);

                // Calculate percentile
                double percentile = 100.0 * (totalSites - areaRank) / totalSites;
                details.append(" (larger than ").append(String.format("%.1f", percentile)).append("% of sites)");
            }

            // Count sites in same country
            long sameCountrySites = currentSites.stream()
                    .filter(s -> s.getCountry().equals(site.getCountry()))
                    .count();

            if (sameCountrySites > 1) {
                details.append("\nSites in ").append(site.getCountry()).append(": ")
                        .append(sameCountrySites).append(" (")
                        .append(String.format("%.1f", sameCountrySites * 100.0 / totalSites))
                        .append("% of total)");
            }
        }

        detailsArea.setText(details.toString());
        detailsArea.setCaretPosition(0);
    }
}