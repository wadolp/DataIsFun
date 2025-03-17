import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for displaying graphical charts of UNESCO World Heritage Sites data
 * using only Swing and AWT components
 */
public class DisplayGraphics extends JPanel {
    private List<UnescoSite> currentSites;
    private String chartType;
    private JPanel controlPanel;
    private ChartPanel chartPanel;

    // Color palette for charts
    private static final Color[] CHART_COLORS = {
            new Color(33, 150, 243),    // Blue
            new Color(76, 175, 80),     // Green
            new Color(244, 67, 54),     // Red
            new Color(255, 193, 7),     // Amber
            new Color(156, 39, 176),    // Purple
            new Color(0, 188, 212),     // Cyan
            new Color(255, 87, 34),     // Deep Orange
            new Color(233, 30, 99),     // Pink
            new Color(205, 220, 57),    // Lime
            new Color(63, 81, 181)      // Indigo
    };

    /**
     * Constructor for the DisplayGraphics panel
     */
    public DisplayGraphics() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Visualization"));

        // Initialize chart panel (center)
        chartPanel = new ChartPanel();
        add(chartPanel, BorderLayout.CENTER);

        // Initialize control panel (bottom)
        createControlPanel();
        add(controlPanel, BorderLayout.SOUTH);

        // Set default chart type
        this.chartType = "category";
    }

    /**
     * Create the control panel with chart type selector
     */
    private void createControlPanel() {
        controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JLabel label = new JLabel("Chart Type: ");
        JComboBox<String> chartTypeSelector = new JComboBox<>(new String[] {
                "Sites by Category", "Top 10 Countries", "Sites by Decade", "Danger Status"
        });

        // Add action listener to change chart type
        chartTypeSelector.addActionListener(e -> {
            String selected = (String) chartTypeSelector.getSelectedItem();
            if ("Sites by Category".equals(selected)) {
                chartType = "category";
            } else if ("Top 10 Countries".equals(selected)) {
                chartType = "country";
            } else if ("Sites by Decade".equals(selected)) {
                chartType = "year";
            } else if ("Danger Status".equals(selected)) {
                chartType = "danger";
            }

            updateVisualization(currentSites);
        });

        controlPanel.add(label);
        controlPanel.add(chartTypeSelector);
    }

    /**
     * Update the sites visualization based on the current chart type
     * @param sites List of UNESCO sites to visualize
     */
    public void updateVisualization(List<UnescoSite> sites) {
        this.currentSites = sites;

        if (sites == null || sites.isEmpty()) {
            chartPanel.setChartData(null, "No Data Available");
            return;
        }

        switch (chartType) {
            case "category":
                createCategoryChart(sites);
                break;
            case "country":
                createCountryChart(sites);
                break;
            case "year":
                createYearChart(sites);
                break;
            case "danger":
                createDangerChart(sites);
                break;
            default:
                createCategoryChart(sites);
        }
    }

    /**
     * Create a chart showing sites by category
     * @param sites List of UNESCO sites to visualize
     */
    private void createCategoryChart(List<UnescoSite> sites) {
        // Group sites by category and count
        Map<String, Long> categoryCounts = sites.stream()
                .collect(Collectors.groupingBy(UnescoSite::getCategory, Collectors.counting()));

        // Convert to chart data format
        List<ChartData> chartData = new ArrayList<>();
        int colorIndex = 0;

        for (Map.Entry<String, Long> entry : categoryCounts.entrySet()) {
            Color color = CHART_COLORS[colorIndex % CHART_COLORS.length];
            chartData.add(new ChartData(entry.getKey(), entry.getValue().doubleValue(), color));
            colorIndex++;
        }

        // Set chart data
        chartPanel.setChartData(chartData, "UNESCO Sites by Category");
        chartPanel.setChartType(ChartPanel.ChartType.PIE_CHART);
    }

    /**
     * Create a chart showing top 10 countries by number of sites
     * @param sites List of UNESCO sites to visualize
     */
    private void createCountryChart(List<UnescoSite> sites) {
        // Group sites by country and count
        Map<String, Long> countryCounts = sites.stream()
                .collect(Collectors.groupingBy(UnescoSite::getCountry, Collectors.counting()));

        // Sort by count and take top 10
        List<Map.Entry<String, Long>> topCountries = countryCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        // Convert to chart data format
        List<ChartData> chartData = new ArrayList<>();
        for (int i = 0; i < topCountries.size(); i++) {
            Map.Entry<String, Long> entry = topCountries.get(i);
            Color color = CHART_COLORS[i % CHART_COLORS.length];
            chartData.add(new ChartData(entry.getKey(), entry.getValue().doubleValue(), color));
        }

        // Set chart data
        chartPanel.setChartData(chartData, "Top 10 Countries by Number of UNESCO Sites");
        chartPanel.setChartType(ChartPanel.ChartType.BAR_CHART);
    }

    /**
     * Create a chart showing sites by inscription decade
     * @param sites List of UNESCO sites to visualize
     */
    private void createYearChart(List<UnescoSite> sites) {
        // Group sites by decade
        Map<String, Long> decadeCounts = sites.stream()
                .collect(Collectors.groupingBy(site -> {
                    int year = site.getInscriptionYear();
                    int decade = (year / 10) * 10;
                    return decade + "s";
                }, Collectors.counting()));

        // Sort by decade
        List<Map.Entry<String, Long>> sortedDecades = decadeCounts.entrySet().stream()
                .sorted(Comparator.comparing(entry -> {
                    // Extract decade number for sorting
                    String decade = entry.getKey();
                    return Integer.parseInt(decade.substring(0, decade.length() - 1));
                }))
                .collect(Collectors.toList());

        // Convert to chart data format
        List<ChartData> chartData = new ArrayList<>();
        Color lineColor = new Color(33, 150, 243); // Blue

        for (Map.Entry<String, Long> entry : sortedDecades) {
            chartData.add(new ChartData(entry.getKey(), entry.getValue().doubleValue(), lineColor));
        }

        // Set chart data
        chartPanel.setChartData(chartData, "UNESCO Sites by Inscription Decade");
        chartPanel.setChartType(ChartPanel.ChartType.LINE_CHART);
    }

    /**
     * Create a chart showing sites by danger status
     * @param sites List of UNESCO sites to visualize
     */
    private void createDangerChart(List<UnescoSite> sites) {
        // Count sites by danger status
        long sitesInDanger = sites.stream().filter(UnescoSite::isInDanger).count();
        long sitesNotInDanger = sites.size() - sitesInDanger;

        // Convert to chart data format
        List<ChartData> chartData = new ArrayList<>();
        chartData.add(new ChartData("In Danger", sitesInDanger, new Color(244, 67, 54))); // Red
        chartData.add(new ChartData("Not in Danger", sitesNotInDanger, new Color(76, 175, 80))); // Green

        // Set chart data
        chartPanel.setChartData(chartData, "UNESCO Sites by Danger Status");
        chartPanel.setChartType(ChartPanel.ChartType.PIE_CHART);
    }

    /**
     * Inner class for chart data representation
     */
    private static class ChartData {
        private String label;
        private double value;
        private Color color;

        public ChartData(String label, double value, Color color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }

        public String getLabel() { return label; }
        public double getValue() { return value; }
        public Color getColor() { return color; }
    }

    /**
     * Inner class for custom chart panel
     */
    private static class ChartPanel extends JPanel {
        private List<ChartData> chartData;
        private String chartTitle;
        private ChartType chartType;
        private final Font titleFont = new Font("SansSerif", Font.BOLD, 16);
        private final Font labelFont = new Font("SansSerif", Font.PLAIN, 12);

        public enum ChartType {
            PIE_CHART, BAR_CHART, LINE_CHART
        }

        public ChartPanel() {
            setBackground(Color.WHITE);
            this.chartType = ChartType.PIE_CHART;
        }

        public void setChartData(List<ChartData> chartData, String chartTitle) {
            this.chartData = chartData;
            this.chartTitle = chartTitle;
            repaint();
        }

        public void setChartType(ChartType chartType) {
            this.chartType = chartType;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw chart title
            if (chartTitle != null) {
                g2d.setFont(titleFont);
                FontMetrics fm = g2d.getFontMetrics();
                int titleWidth = fm.stringWidth(chartTitle);
                g2d.drawString(chartTitle, (getWidth() - titleWidth) / 2, 30);
            }

            // If no data, show message
            if (chartData == null || chartData.isEmpty()) {
                g2d.setFont(labelFont);
                String message = "No data available";
                FontMetrics fm = g2d.getFontMetrics();
                int messageWidth = fm.stringWidth(message);
                g2d.drawString(message, (getWidth() - messageWidth) / 2, getHeight() / 2);
                return;
            }

            // Draw appropriate chart type
            switch (chartType) {
                case PIE_CHART:
                    drawPieChart(g2d);
                    break;
                case BAR_CHART:
                    drawBarChart(g2d);
                    break;
                case LINE_CHART:
                    drawLineChart(g2d);
                    break;
            }
        }

        /**
         * Draw a pie chart
         * @param g2d Graphics2D object
         */
        private void drawPieChart(Graphics2D g2d) {
            int padding = 50;
            int legendWidth = 150;

            // Calculate total value for percentage
            double total = chartData.stream().mapToDouble(ChartData::getValue).sum();

            // Calculate pie chart dimensions
            int diameter = Math.min(getWidth() - padding * 2 - legendWidth, getHeight() - padding * 2);
            int x = (getWidth() - diameter - legendWidth) / 2;
            int y = padding + 20; // Add space for title

            // Draw pie slices
            double startAngle = 0;
            int legendY = y;

            for (ChartData data : chartData) {
                // Calculate slice angle
                double angle = 360.0 * (data.getValue() / total);

                // Draw slice
                g2d.setColor(data.getColor());
                g2d.fill(new Arc2D.Double(x, y, diameter, diameter, startAngle, angle, Arc2D.PIE));

                // Calculate center angle for label placement
                double centerAngle = startAngle + angle / 2;
                startAngle += angle;

                // Draw legend
                int legendX = x + diameter + 20;

                // Color box
                g2d.fillRect(legendX, legendY, 15, 15);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(legendX, legendY, 15, 15);

                // Label and percentage
                g2d.setFont(labelFont);
                String label = data.getLabel();
                String percentage = String.format("%.1f%%", (data.getValue() / total) * 100);
                g2d.drawString(label, legendX + 20, legendY + 12);
                g2d.drawString(percentage, legendX + 20 + 100, legendY + 12);

                legendY += 25;
            }
        }

        /**
         * Draw a bar chart
         * @param g2d Graphics2D object
         */
        private void drawBarChart(Graphics2D g2d) {
            int padding = 50;
            int chartWidth = getWidth() - padding * 2;
            int chartHeight = getHeight() - padding * 2 - 40; // Extra space for title
            int barSpacing = 10;

            // Calculate max value for scaling
            double maxValue = chartData.stream()
                    .mapToDouble(ChartData::getValue)
                    .max()
                    .orElse(1.0);

            // Calculate bar width
            int barWidth = (chartWidth - (chartData.size() - 1) * barSpacing) / chartData.size();

            // Draw axes
            g2d.setColor(Color.BLACK);
            g2d.drawLine(padding, getHeight() - padding, padding, padding + 20); // Y-axis
            g2d.drawLine(padding, getHeight() - padding, getWidth() - padding, getHeight() - padding); // X-axis

            // Draw Y-axis labels
            g2d.setFont(labelFont);
            FontMetrics fm = g2d.getFontMetrics();

            // Draw Y-axis scale (0, max/2, max)
            int scaleY = getHeight() - padding;
            g2d.drawString("0", padding - 25, scaleY);

            String halfLabel = String.format("%.0f", maxValue / 2);
            g2d.drawString(halfLabel, padding - 25, scaleY - chartHeight / 2);

            String maxLabel = String.format("%.0f", maxValue);
            g2d.drawString(maxLabel, padding - 25, scaleY - chartHeight);

            // Draw bars
            int barX = padding;

            for (ChartData data : chartData) {
                // Calculate bar height based on value
                int barHeight = (int) (chartHeight * (data.getValue() / maxValue));

                // Draw bar
                g2d.setColor(data.getColor());
                g2d.fillRect(barX, getHeight() - padding - barHeight, barWidth, barHeight);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(barX, getHeight() - padding - barHeight, barWidth, barHeight);

                // Draw label
                String label = data.getLabel();
                int labelWidth = fm.stringWidth(label);

                // Rotate label if too long
                if (labelWidth > barWidth + 10) {
                    AffineTransform oldTransform = g2d.getTransform();
                    g2d.rotate(-Math.PI / 4, barX + barWidth / 2, getHeight() - padding + 15);
                    g2d.drawString(label, barX + barWidth / 2 - labelWidth / 2, getHeight() - padding + 15);
                    g2d.setTransform(oldTransform);
                } else {
                    g2d.drawString(label, barX + barWidth / 2 - labelWidth / 2, getHeight() - padding + 15);
                }

                // Draw value on top of bar
                String valueLabel = String.format("%.0f", data.getValue());
                int valueLabelWidth = fm.stringWidth(valueLabel);
                g2d.drawString(valueLabel, barX + barWidth / 2 - valueLabelWidth / 2,
                        getHeight() - padding - barHeight - 5);

                // Move to next bar position
                barX += barWidth + barSpacing;
            }
        }

        /**
         * Draw a line chart
         * @param g2d Graphics2D object
         */
        private void drawLineChart(Graphics2D g2d) {
            int padding = 50;
            int chartWidth = getWidth() - padding * 2;
            int chartHeight = getHeight() - padding * 2 - 40; // Extra space for title

            // Calculate max value for scaling
            double maxValue = chartData.stream()
                    .mapToDouble(ChartData::getValue)
                    .max()
                    .orElse(1.0);

            // Draw axes
            g2d.setColor(Color.BLACK);
            g2d.drawLine(padding, getHeight() - padding, padding, padding + 20); // Y-axis
            g2d.drawLine(padding, getHeight() - padding, getWidth() - padding, getHeight() - padding); // X-axis

            // Draw Y-axis labels
            g2d.setFont(labelFont);
            FontMetrics fm = g2d.getFontMetrics();

            // Draw Y-axis scale (0, max/2, max)
            int scaleY = getHeight() - padding;
            g2d.drawString("0", padding - 25, scaleY);

            String halfLabel = String.format("%.0f", maxValue / 2);
            g2d.drawString(halfLabel, padding - 25, scaleY - chartHeight / 2);

            String maxLabel = String.format("%.0f", maxValue);
            g2d.drawString(maxLabel, padding - 25, scaleY - chartHeight);

            // Calculate point coordinates
            int pointSpacing = chartWidth / (chartData.size() - 1);
            if (chartData.size() == 1) {
                pointSpacing = 0; // Avoid division by zero
            }

            int[] xPoints = new int[chartData.size()];
            int[] yPoints = new int[chartData.size()];

            for (int i = 0; i < chartData.size(); i++) {
                ChartData data = chartData.get(i);
                xPoints[i] = padding + i * pointSpacing;
                yPoints[i] = getHeight() - padding - (int) (chartHeight * (data.getValue() / maxValue));
            }

            // Draw line segments
            g2d.setColor(chartData.get(0).getColor());
            g2d.setStroke(new BasicStroke(3));

            for (int i = 0; i < chartData.size() - 1; i++) {
                g2d.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
            }

            // Draw points
            for (int i = 0; i < chartData.size(); i++) {
                g2d.setColor(Color.WHITE);
                g2d.fillOval(xPoints[i] - 5, yPoints[i] - 5, 10, 10);
                g2d.setColor(chartData.get(0).getColor());
                g2d.drawOval(xPoints[i] - 5, yPoints[i] - 5, 10, 10);

                // Draw value above point
                g2d.setColor(Color.BLACK);
                String valueLabel = String.format("%.0f", chartData.get(i).getValue());
                int valueLabelWidth = fm.stringWidth(valueLabel);
                g2d.drawString(valueLabel, xPoints[i] - valueLabelWidth / 2, yPoints[i] - 10);

                // Draw x-axis label
                String xLabel = chartData.get(i).getLabel();
                int xLabelWidth = fm.stringWidth(xLabel);

                // Rotate label if too long or too many points
                if (xLabelWidth > pointSpacing - 10 || chartData.size() > 8) {
                    AffineTransform oldTransform = g2d.getTransform();
                    g2d.rotate(-Math.PI / 4, xPoints[i], getHeight() - padding + 15);
                    g2d.drawString(xLabel, xPoints[i] - xLabelWidth / 2, getHeight() - padding + 15);
                    g2d.setTransform(oldTransform);
                } else {
                    g2d.drawString(xLabel, xPoints[i] - xLabelWidth / 2, getHeight() - padding + 15);
                }
            }
        }
    }
}