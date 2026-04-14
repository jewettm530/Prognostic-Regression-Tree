import java.io.*;
import java.util.*;

/**
 * Handles loading and managing patient data from CSV files
 * Implements missing value imputation strategies
 */
public class DataLoader {
    private Map<String, PatientRecord> patients;
    private PrognosticRegressionTree regressionTree;
    private Map<String, Integer> missingValueCounts;
    
    public DataLoader(PrognosticRegressionTree tree) {
        this.patients = new HashMap<>();
        this.regressionTree = tree;
        this.missingValueCounts = new HashMap<>();
    }
    
    /**
     * Load patient data from CSV file
     */
    public void loadData(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        boolean isFirstLine = true;
        String[] headers = null;
        
        System.out.println("Loading data from " + filename + "...");
        
        while ((line = reader.readLine()) != null) {
            if (isFirstLine) {
                headers = parseCSVLine(line);
                isFirstLine = false;
                continue;
            }
            
            String[] values = parseCSVLine(line);
            if (values.length < 2) continue;
            
            String patientId = values[0].trim();
            PatientRecord patient = new PatientRecord(patientId);
            
            // Map each column to features
            for (int i = 1; i < headers.length && i < values.length; i++) {
                String header = headers[i].trim();
                String value = values[i].trim();
                
                if (value.isEmpty()) {
                    // Handle missing value
                    value = imputeMissingValue(header);
                    missingValueCounts.merge(header, 1, Integer::sum);
                }
                
                Object convertedValue = convertValue(header, value);
                patient.getFeatures().put(header, convertedValue);
            }
            
            // Calculate risk and interventions
            regressionTree.predictRisk(patient);
            regressionTree.analyzeInterventions(patient);
            
            patients.put(patientId, patient);
        }
        
        reader.close();
        System.out.println("Loaded " + patients.size() + " patient records");
        
        if (!missingValueCounts.isEmpty()) {
            System.out.println("\nMissing values imputed:");
            for (Map.Entry<String, Integer> entry : missingValueCounts.entrySet()) {
                System.out.println("  " + entry.getKey() + ": " + entry.getValue() + " values");
            }
        }
    }
    
    private String[] parseCSVLine(String line) {
        // Simple CSV parser that handles quoted values
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentValue = new StringBuilder();
        
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(currentValue.toString());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }
        values.add(currentValue.toString());
        
        return values.toArray(new String[0]);
    }
    
    private String imputeMissingValue(String featureName) {
        // Simple imputation strategies based on feature type
        switch (featureName) {
            case "Age": return "50";
            case "Blood Pressure": return "120";
            case "Cholesterol Level": return "200";
            case "BMI": return "25";
            case "Exercise Habits": return "Medium";
            case "Alcohol Consumption": return "Medium";
            case "Gender": return "Female";
            case "Smoking":
            case "Family Heart Disease":
            case "Diabetes":
            case "High Blood Pressure":
            case "Low HDL Cholesterol":
            case "High LDL Cholesterol":
                return "No";
            default: return "Unknown";
        }
    }
    
    private Object convertValue(String header, String value) {
        // Numeric features
        if (header.equals("Age") || header.equals("Blood Pressure") || 
            header.equals("Cholesterol Level") || header.equals("BMI")) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        // Categorical features remain as strings
        return value;
    }
    
    public PatientRecord getPatient(String id) {
        return patients.get(id);
    }
    
    public boolean patientExists(String id) {
        return patients.containsKey(id);
    }
    
    public Map<String, PatientRecord> getPatientMap() {
        return patients;
    }
    
    public List<PatientRecord> getAllPatients() {
        return new ArrayList<>(patients.values());
    }
    
    public List<PatientRecord> getPatientsByRiskLevel(String riskLevel) {
        List<PatientRecord> result = new ArrayList<>();
        for (PatientRecord patient : patients.values()) {
            if (patient.getRiskLevel().equalsIgnoreCase(riskLevel)) {
                result.add(patient);
            }
        }
        return result;
    }
    
    public List<PatientRecord> getHighRiskPatients() {
        return getPatientsByRiskLevel("HIGH");
    }
    
    public List<PatientRecord> getModerateRiskPatients() {
        return getPatientsByRiskLevel("MODERATE");
    }
    
    public List<PatientRecord> getLowRiskPatients() {
        return getPatientsByRiskLevel("LOW");
    }
    
    public double getAverageRisk() {
        return patients.values().stream()
               .mapToDouble(PatientRecord::getPredictedRisk)
               .average()
               .orElse(0.0);
    }
    
    public Map<String, Integer> getRiskDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("HIGH", getHighRiskPatients().size());
        distribution.put("MODERATE", getModerateRiskPatients().size());
        distribution.put("LOW", getLowRiskPatients().size());
        return distribution;
    }
}