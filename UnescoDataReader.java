import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data model class for UNESCO World Heritage Sites
 */
class UnescoSite {
    private String name;
    private String country;
    private int inscriptionYear; // Changed from LocalDate to int
    private boolean inDanger;
    private LocalDate removalDate;
    private double latitude;
    private double longitude;
    private double area;
    private String category;
    private boolean transboundary;

    public UnescoSite(String name, String country, int inscriptionYear, boolean inDanger,
                      LocalDate removalDate, double latitude, double longitude, double area,
                      String category, boolean transboundary) {
        this.name = name;
        this.country = country;
        this.inscriptionYear = inscriptionYear; // Changed from LocalDate to int
        this.inDanger = inDanger;
        this.removalDate = removalDate;
        this.latitude = latitude;
        this.longitude = longitude;
        this.area = area;
        this.category = category;
        this.transboundary = transboundary;
    }

    // Getters and setters
    public String getName() { return name; }
    public String getCountry() { return country; }
    public int getInscriptionYear() { return inscriptionYear; } // Changed from LocalDate to int
    public boolean isInDanger() { return inDanger; }
    public LocalDate getRemovalDate() { return removalDate; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getArea() { return area; }
    public String getCategory() { return category; }
    public boolean isTransboundary() { return transboundary; }

    @Override
    public String toString() {
        return "UnescoSite{" +
                "name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", inscriptionYear=" + inscriptionYear + // Changed from LocalDate to int
                ", inDanger=" + inDanger +
                ", removalDate=" + (removalDate != null ? removalDate : "N/A") +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", area=" + area +
                ", category='" + category + '\'' +
                ", transboundary=" + transboundary +
                '}';
    }
}

/**
 * Main class that handles reading and processing UNESCO World Heritage Sites data
 */
public class UnescoDataReader {
    private List<UnescoSite> sites = new ArrayList<>();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy");

    /**
     * Read UNESCO data from a CSV file
     * @param filePath path to the CSV file
     * @throws IOException if file cannot be read
     */
    public void readCsvFile(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Skip header line
            String line = br.readLine();
            
            // Read data lines
            while ((line = br.readLine()) != null) {
                UnescoSite site = parseLine(line);
                if (site != null) {
                    sites.add(site);
                }
            }
        }
        
        System.out.println("Successfully loaded " + sites.size() + " UNESCO World Heritage Sites.");
    }
    
    /**
     * Parse a CSV line into a UnescoSite object
     * @param line CSV line
     * @return UnescoSite object or null if parsing failed
     */
    private UnescoSite parseLine(String line) {
        try {
            String[] data = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"); // Handle commas in quoted fields
            
            // Clean up quoted fields
            for (int i = 0; i < data.length; i++) {
                if (data[i].startsWith("\"") && data[i].endsWith("\"")) {
                    data[i] = data[i].substring(1, data[i].length() - 1);
                }
            }
            
            if (data.length < 9) {
                System.err.println("Warning: Skipping incomplete line: " + line);
                return null;
            }
            
            String name = data[0].trim();
            String country = data[1].trim();
            
            // Parse inscription year
            int inscriptionYear = 1970; // Default year
            try {
                inscriptionYear = Integer.parseInt(data[2].trim());
            } catch (NumberFormatException e) {
                System.err.println("Warning: Invalid inscription year format in line: " + line);
            }
            
            // Parse danger status
            boolean inDanger = "yes".equalsIgnoreCase(data[3].trim()) || "true".equalsIgnoreCase(data[3].trim());
            
            // Parse removal date (if any)
            LocalDate removalDate = null;
            if (data[4] != null && !data[4].trim().isEmpty() && !data[4].trim().equalsIgnoreCase("n/a")) {
                try {
                    removalDate = LocalDate.parse(data[4].trim(), DATE_FORMATTER);
                } catch (DateTimeParseException e) {
                    // Leave as null if parsing fails
                }
            }
            
            // Parse coordinates
            double latitude = 0.0;
            double longitude = 0.0;
            try {
                if (data[5] != null && !data[5].trim().isEmpty()) {
                    latitude = Double.parseDouble(data[5].trim());
                }
                if (data[6] != null && !data[6].trim().isEmpty()) {
                    longitude = Double.parseDouble(data[6].trim());
                }
            } catch (NumberFormatException e) {
                System.err.println("Warning: Invalid coordinate format in line: " + line);
            }
            
            // Parse area
            double area = 0.0;
            if (data[7] != null && !data[7].trim().isEmpty()) {
                try {
                    area = Double.parseDouble(data[7].trim());
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Invalid area format in line: " + line);
                }
            }
            
            String category = data[8].trim();
            
            // Parse transboundary flag
            boolean transboundary = false;
            if (data.length > 9) {
                transboundary = "yes".equalsIgnoreCase(data[9].trim()) || "true".equalsIgnoreCase(data[9].trim());
            }
            
            return new UnescoSite(name, country, inscriptionYear, inDanger, removalDate, 
                                 latitude, longitude, area, category, transboundary);
        } catch (Exception e) {
            System.err.println("Error parsing line: " + line);
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get all UNESCO sites
     * @return list of UNESCO sites
     */
    public List<UnescoSite> getAllSites() {
        return new ArrayList<>(sites);
    }
    
    /**
     * Get sites filtered by category
     * @param category category to filter by
     * @return filtered list of sites
     */
    public List<UnescoSite> getSitesByCategory(String category) {
        return sites.stream()
                .filter(site -> category.equalsIgnoreCase(site.getCategory()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get sites filtered by country
     * @param country country to filter by
     * @return filtered list of sites
     */
    public List<UnescoSite> getSitesByCountry(String country) {
        return sites.stream()
                .filter(site -> country.equalsIgnoreCase(site.getCountry()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get sites filtered by danger status
     * @param inDanger danger status to filter by
     * @return filtered list of sites
     */
    public List<UnescoSite> getSitesByDangerStatus(boolean inDanger) {
        return sites.stream()
                .filter(site -> site.isInDanger() == inDanger)
                .collect(Collectors.toList());
    }
    
    /**
     * Get sites filtered by transboundary status
     * @param transboundary transboundary status to filter by
     * @return filtered list of sites
     */
    public List<UnescoSite> getSitesByTransboundaryStatus(boolean transboundary) {
        return sites.stream()
                .filter(site -> site.isTransboundary() == transboundary)
                .collect(Collectors.toList());
    }
    
    /**
     * Get sites inscribed after a given year
     * @param year year to filter by
     * @return filtered list of sites
     */
    public List<UnescoSite> getSitesInscribedAfter(int year) {
        return sites.stream()
                .filter(site -> site.getInscriptionYear() > year)
                .collect(Collectors.toList());
    }
    
    /**
     * Get sites sorted by area (largest first)
     * @return sorted list of sites
     */
    public List<UnescoSite> getSitesSortedByArea() {
        return sites.stream()
                .sorted(Comparator.comparing(UnescoSite::getArea).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * Get basic statistics about the UNESCO sites
     * @return string with statistics
     */
    public String getStatistics() {
        long totalSites = sites.size();
        long sitesInDanger = sites.stream().filter(UnescoSite::isInDanger).count();
        double averageArea = sites.stream().mapToDouble(UnescoSite::getArea).average().orElse(0.0);
        long transboundarySites = sites.stream().filter(UnescoSite::isTransboundary).count();
        
        // Count by category
        StringBuilder stats = new StringBuilder();
        stats.append("Total UNESCO World Heritage Sites: ").append(totalSites).append("\n");
        stats.append("Sites in danger: ").append(sitesInDanger).append(" (").append(String.format("%.1f", (sitesInDanger * 100.0 / totalSites))).append("%)\n");
        stats.append("Average area: ").append(String.format("%.2f", averageArea)).append(" hectares\n");
        stats.append("Transboundary sites: ").append(transboundarySites).append(" (").append(String.format("%.1f", (transboundarySites * 100.0 / totalSites))).append("%)\n");
        
        // Find country with most sites
        String topCountry = sites.stream()
                .collect(Collectors.groupingBy(UnescoSite::getCountry, Collectors.counting()))
                .entrySet().stream()
                .max(Comparator.comparing(e -> e.getValue()))
                .map(e -> e.getKey() + " (" + e.getValue() + " sites)")
                .orElse("None");
        stats.append("Country with most sites: ").append(topCountry).append("\n");
        
        return stats.toString();
    }

    /**
     * Console application to test the UNESCO data reader
     */
    public static void main(String[] args) {
        UnescoDataReader reader = new UnescoDataReader();
        
        try {
            // Change the file path as needed
            reader.readCsvFile("allsites.csv");
            
            List<UnescoSite> allSites = reader.getAllSites();
            
            // Display first item
            if (!allSites.isEmpty()) {
                System.out.println("\nFirst UNESCO site in the dataset:");
                System.out.println(allSites.get(0));
            }
            
            // Display 10th item (if available)
            if (allSites.size() >= 10) {
                System.out.println("\n10th UNESCO site in the dataset:");
                System.out.println(allSites.get(9));
            }
            
            // Display total number of entries
            System.out.println("\nTotal number of UNESCO sites: " + allSites.size());
            
            // Display statistics
            System.out.println("\nStatistics about UNESCO World Heritage Sites:");
            System.out.println(reader.getStatistics());
            
        } catch (IOException e) {
            System.err.println("Error reading UNESCO data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}