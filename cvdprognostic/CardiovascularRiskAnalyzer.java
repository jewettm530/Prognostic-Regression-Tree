
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Main user interface for the Cardiovascular Risk Analysis System
 */
public class CardiovascularRiskAnalyzer {
    private static PrognosticRegressionTree regressionTree;
    private static DataLoader dataLoader;
    private static Scanner scanner;
    
    public static void main(String[] args) {
        initializeSystem();
        runMainMenu();
    }
    
    // In the initializeSystem() method, replace the tree building with:
private static void initializeSystem() {
    System.out.println("\n" + "=".repeat(70));
    System.out.println("❤️  CARDIOVASCULAR DISEASE PROGNOSTIC SYSTEM");
    System.out.println("=".repeat(70));
    System.out.println("Version: 2.0 - DATA-DRIVEN Regression Tree");
    System.out.println("=".repeat(70));
    
    scanner = new Scanner(System.in);
    regressionTree = new PrognosticRegressionTree();
    dataLoader = new DataLoader(regressionTree);
    
    // Load data
    try {
        dataLoader.loadData("../heart_data.csv");
        
        // Get all patients for training
        List<PatientRecord> allPatients = dataLoader.getAllPatients();
        System.out.println("\n📊 Dataset size: " + allPatients.size() + " patients");
        
        // Split into training (80%) and testing (20%)
        Collections.shuffle(allPatients);
        int trainSize = (int)(allPatients.size() * 0.8);
        List<PatientRecord> trainPatients = allPatients.subList(0, trainSize);
        List<PatientRecord> testPatients = allPatients.subList(trainSize, allPatients.size());
        
        System.out.println("   Training set: " + trainPatients.size() + " patients");
        System.out.println("   Testing set: " + testPatients.size() + " patients");
        
        // Build tree from training data
        regressionTree.buildTreeFromData(trainPatients, 5, 20);
        
        // Test on unseen data
        double testError = 0;
        for (PatientRecord patient : testPatients) {
            double predicted = regressionTree.predictRisk(patient);
            double actual = patient.getPredictedRisk();
            testError += Math.pow(predicted - actual, 2);
        }
        testError /= testPatients.size();
        System.out.printf("\n📈 Test MSE: %.4f%n", testError);
        
    } catch (FileNotFoundException e) {
        System.out.println("❌ Error: heart_data.csv not found in parent directory.");
        System.out.println("Please ensure heart_data.csv is located in the 'Prognostic Regression Tree' folder.");
        System.exit(1);
    } catch (IOException e) {
        System.out.println("❌ Error loading data: " + e.getMessage());
        System.exit(1);
    }
}
    
    private static void runMainMenu() {
        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1:
                    analyzeSinglePatient();
                    break;
                case 2:
                    displayAllPatientsSummary();
                    break;
                case 3:
                    exportAllPatientsSummary();
                    break;
                case 4:
                    displayStatistics();
                    break;
                case 5:
                    comparePatients();
                    break;
                case 6:
                    searchByRiskLevel();
                    break;
                case 7:
                    exportPatientReport();
                    break;
                case 8:
                    System.out.println("\nThank you for using the CVD Prognostic System!");
                    System.out.println("Stay healthy! ❤️");
                    running = false;
                    break;
                default:
                    System.out.println("\n❌ Invalid choice. Please try again.");
            }
            
            if (running && choice != 8) {
                System.out.print("\nPress Enter to continue...");
                scanner.nextLine();
                scanner.nextLine();
            }
        }
    }
    
    private static void displayMainMenu() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("MAIN MENU");
        System.out.println("=".repeat(60));
        System.out.println("1. 🔍 Analyze Single Patient by ID");
        System.out.println("2. 📊 View All Patients Summary (Display)");
        System.out.println("3. 💾 Export All Patients Summary (Save to File)");
        System.out.println("4. 📈 View Database Statistics");
        System.out.println("5. 📉 Compare Two Patients");
        System.out.println("6. 🎯 Search Patients by Risk Level");
        System.out.println("7. 💾 Export Single Patient Report");
        System.out.println("8. 🚪 Exit");
        System.out.println("=".repeat(60));
    }
    
    private static void analyzeSinglePatient() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("PATIENT RISK ANALYSIS");
        System.out.println("=".repeat(60));
        
        String id = getStringInput("Enter Patient ID: ");
        
        if (dataLoader.patientExists(id)) {
            PatientRecord patient = dataLoader.getPatient(id);
            String report = RiskAnalyzerUtils.generatePatientReport(patient);
            System.out.println(report);
        } else {
            System.out.println("\n❌ Patient ID not found. Available IDs:");
            List<PatientRecord> allPatients = dataLoader.getAllPatients();
            for (int i = 0; i < Math.min(10, allPatients.size()); i++) {
                System.out.println("   " + allPatients.get(i).getId());
            }
            if (allPatients.size() > 10) {
                System.out.println("   ... and " + (allPatients.size() - 10) + " more");
            }
        }
    }
    
    private static void displayAllPatientsSummary() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("PATIENT SUMMARY DASHBOARD");
        System.out.println("=".repeat(80));
        System.out.printf("%-10s %-12s %-30s %-20s %-15s%n", "ID", "Risk Level", "Top Intervention", "Risk Reduction", "New Risk After Top Intervention");
        System.out.println("-".repeat(80));
        
        List<PatientRecord> patientList = dataLoader.getAllPatients();
        patientList.sort((p1, p2) -> Double.compare(p2.getPredictedRisk(), p1.getPredictedRisk()));
        
        for (PatientRecord patient : patientList) {
            double risk = patient.getPredictedRisk();
            String riskDisplay = patient.getRiskIcon() + " " + patient.getRiskLevel();
            
            String topIntervention = patient.getTopIntervention() != null ? 
                                    patient.getTopIntervention().getName() : "N/A";
            double topReduction = patient.getTopIntervention() != null ? 
                                 patient.getTopIntervention().getReductionPercentage() : 0;
            double newRisk = patient.getTopIntervention() != null ?
                            patient.getTopIntervention().getNewRisk() * 100 : risk * 100;
            
            System.out.printf("%-10s %-12s %-30s %-19.1f%% %-15.1f%%%n", 
                            patient.getId(), riskDisplay, topIntervention, topReduction, newRisk);
        }
        System.out.println("=".repeat(80));
    }
    
    private static void exportAllPatientsSummary() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("EXPORT ALL PATIENTS SUMMARY");
        System.out.println("=".repeat(60));
        
        try {
            // Create reports directory if it doesn't exist
            File reportsDir = new File("reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdir();
            }
            
            // Create filename with timestamp
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = "reports/all_patients_summary_" + timestamp + ".csv";
            
            PrintWriter writer = new PrintWriter(new FileWriter(filename));
            
            // Write CSV header
            writer.println("Patient ID,Age,Gender,Blood Pressure,Cholesterol Level,Exercise Habits,Smoking,Family Heart Disease,Diabetes,BMI,High Blood Pressure,Low HDL Cholesterol,High LDL Cholesterol,Alcohol Consumption,Current Risk (%),Risk Level,Top Intervention,Top Intervention Reduction (%),New Risk After Top Intervention (%)");
            
            List<PatientRecord> patientList = dataLoader.getAllPatients();
            patientList.sort((p1, p2) -> Double.compare(p2.getPredictedRisk(), p1.getPredictedRisk()));
            
            for (PatientRecord patient : patientList) {
                double risk = patient.getPredictedRisk();
                String riskLevel = patient.getRiskLevel();
                
                // Get patient features
                Map<String, Object> features = patient.getFeatures();
                
                // Get top intervention
                String topIntervention = patient.getTopIntervention() != null ? 
                                        patient.getTopIntervention().getName() : "N/A";
                double topReduction = patient.getTopIntervention() != null ? 
                                     patient.getTopIntervention().getReductionPercentage() : 0;
                double newRisk = patient.getTopIntervention() != null ?
                                patient.getTopIntervention().getNewRisk() * 100 : risk * 100;
                
                // Write CSV row
                writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%.1f,%s,%s,%.1f,%.1f%n",
                    patient.getId(),
                    features.getOrDefault("Age", "N/A"),
                    features.getOrDefault("Gender", "N/A"),
                    features.getOrDefault("Blood Pressure", "N/A"),
                    features.getOrDefault("Cholesterol Level", "N/A"),
                    features.getOrDefault("Exercise Habits", "N/A"),
                    features.getOrDefault("Smoking", "N/A"),
                    features.getOrDefault("Family Heart Disease", "N/A"),
                    features.getOrDefault("Diabetes", "N/A"),
                    features.getOrDefault("BMI", "N/A"),
                    features.getOrDefault("High Blood Pressure", "N/A"),
                    features.getOrDefault("Low HDL Cholesterol", "N/A"),
                    features.getOrDefault("High LDL Cholesterol", "N/A"),
                    features.getOrDefault("Alcohol Consumption", "N/A"),
                    risk * 100,
                    riskLevel,
                    topIntervention,
                    topReduction,
                    newRisk
                );
            }
            
            writer.close();
            System.out.println("\n✅ All patients summary exported successfully!");
            System.out.println("   File saved as: " + filename);
            System.out.println("   Location: " + new File(filename).getAbsolutePath());
            
            // Also create a formatted text version for easier reading
            String txtFilename = "reports/all_patients_summary_" + timestamp + ".txt";
            PrintWriter txtWriter = new PrintWriter(new FileWriter(txtFilename));
            
            txtWriter.println("=".repeat(100));
            txtWriter.println("CARDIOVASCULAR DISEASE RISK - ALL PATIENTS SUMMARY REPORT");
            txtWriter.println("=".repeat(100));
            txtWriter.println("Generated: " + new Date());
            txtWriter.println("Total Patients: " + patientList.size());
            txtWriter.println();
            txtWriter.println("-".repeat(100));
            txtWriter.printf("%-10s %-12s %-10s %-25s %-15s %-15s%n", 
                           "ID", "Risk Level", "Risk (%)", "Top Intervention", "Reduction (%)", "New Risk (%)");
            txtWriter.println("-".repeat(100));
            
            for (PatientRecord patient : patientList) {
                double risk = patient.getPredictedRisk();
                String riskLevel = patient.getRiskLevel();
                String riskIcon = patient.getRiskIcon();
                
                String topIntervention = patient.getTopIntervention() != null ? 
                                        patient.getTopIntervention().getName() : "N/A";
                double topReduction = patient.getTopIntervention() != null ? 
                                     patient.getTopIntervention().getReductionPercentage() : 0;
                double newRisk = patient.getTopIntervention() != null ?
                                patient.getTopIntervention().getNewRisk() * 100 : risk * 100;
                
                txtWriter.printf("%-10s %s %-10s %-10.1f %-25s %-15.1f %-15.1f%n", 
                               patient.getId(), riskIcon, riskLevel, risk * 100, 
                               topIntervention, topReduction, newRisk);
            }
            
            txtWriter.println("-".repeat(100));
            
            // Add statistics at the end
            txtWriter.println();
            txtWriter.println("SUMMARY STATISTICS:");
            txtWriter.println("-".repeat(50));
            txtWriter.printf("Average Risk: %.1f%%%n", dataLoader.getAverageRisk() * 100);
            txtWriter.printf("High Risk Patients (>30%%): %d%n", dataLoader.getHighRiskPatients().size());
            txtWriter.printf("Moderate Risk Patients (15-30%%): %d%n", dataLoader.getModerateRiskPatients().size());
            txtWriter.printf("Low Risk Patients (<15%%): %d%n", dataLoader.getLowRiskPatients().size());
            txtWriter.println("=".repeat(100));
            
            txtWriter.close();
            System.out.println("   Formatted text version also saved as: " + txtFilename);
            
        } catch (IOException e) {
            System.out.println("\n❌ Error exporting summary: " + e.getMessage());
        }
    }
    
    private static void displayStatistics() {
        Map<String, Integer> distribution = dataLoader.getRiskDistribution();
        double averageRisk = dataLoader.getAverageRisk();
        int totalPatients = dataLoader.getAllPatients().size();
        
        String stats = RiskAnalyzerUtils.generateStatisticsReport(distribution, averageRisk, totalPatients);
        System.out.println(stats);
    }
    
    private static void comparePatients() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("COMPARE TWO PATIENTS");
        System.out.println("=".repeat(60));
        
        String id1 = getStringInput("Enter first Patient ID: ");
        String id2 = getStringInput("Enter second Patient ID: ");
        
        if (!dataLoader.patientExists(id1)) {
            System.out.println("\n❌ Patient " + id1 + " not found!");
            return;
        }
        if (!dataLoader.patientExists(id2)) {
            System.out.println("\n❌ Patient " + id2 + " not found!");
            return;
        }
        
        PatientRecord p1 = dataLoader.getPatient(id1);
        PatientRecord p2 = dataLoader.getPatient(id2);
        
        String comparison = RiskAnalyzerUtils.generateComparisonReport(p1, p2);
        System.out.println(comparison);
    }
    
    private static void searchByRiskLevel() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SEARCH PATIENTS BY RISK LEVEL");
        System.out.println("=".repeat(60));
        System.out.println("1. 🔴 High Risk (>30%)");
        System.out.println("2. 🟡 Moderate Risk (15-30%)");
        System.out.println("3. 🟢 Low Risk (<15%)");
        
        int choice = getIntInput("Select risk level: ");
        List<PatientRecord> patients = null;
        String levelName = "";
        
        switch (choice) {
            case 1:
                patients = dataLoader.getHighRiskPatients();
                levelName = "HIGH RISK";
                System.out.println("\n🔴 HIGH RISK PATIENTS:");
                break;
            case 2:
                patients = dataLoader.getModerateRiskPatients();
                levelName = "MODERATE RISK";
                System.out.println("\n🟡 MODERATE RISK PATIENTS:");
                break;
            case 3:
                patients = dataLoader.getLowRiskPatients();
                levelName = "LOW RISK";
                System.out.println("\n🟢 LOW RISK PATIENTS:");
                break;
            default:
                System.out.println("Invalid choice");
                return;
        }
        
        System.out.println("-".repeat(50));
        if (patients.isEmpty()) {
            System.out.println("No patients found in this risk category.");
        } else {
            for (PatientRecord patient : patients) {
                System.out.printf("ID: %-10s Risk: %.1f%% | Top Intervention: %s%n",
                                 patient.getId(), 
                                 patient.getPredictedRisk() * 100,
                                 patient.getTopIntervention() != null ? 
                                 patient.getTopIntervention().getName() : "N/A");
            }
            System.out.println("\nTotal " + levelName + " patients: " + patients.size());
        }
    }
    
    private static void exportPatientReport() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("EXPORT SINGLE PATIENT REPORT");
        System.out.println("=".repeat(60));
        
        String id = getStringInput("Enter Patient ID to export: ");
        
        if (!dataLoader.patientExists(id)) {
            System.out.println("\n❌ Patient ID not found!");
            return;
        }
        
        try {
            // Create reports directory if it doesn't exist
            File reportsDir = new File("reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdir();
            }
            
            String filename = "reports/patient_report_" + id + "_" + 
                             System.currentTimeMillis() + ".txt";
            PrintWriter writer = new PrintWriter(new FileWriter(filename));
            
            PatientRecord patient = dataLoader.getPatient(id);
            String report = RiskAnalyzerUtils.generatePatientReport(patient);
            writer.print(report);
            writer.close();
            
            System.out.println("\n✅ Report exported successfully to: " + filename);
            
        } catch (IOException e) {
            System.out.println("\n❌ Error exporting report: " + e.getMessage());
        }
    }
    
        
    // Helper methods
    private static int getIntInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Please enter a number: ");
            scanner.next();
        }
        int input = scanner.nextInt();
        scanner.nextLine(); // consume newline
        return input;
    }
    
    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}