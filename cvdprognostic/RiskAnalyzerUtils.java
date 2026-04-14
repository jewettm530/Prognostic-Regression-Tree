import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Utility class for formatting and helper functions
 */
public class RiskAnalyzerUtils {
    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Format risk percentage for display
     */
    public static String formatRiskPercentage(double risk) {
        return String.format("%.1f%%", risk * 100);
    }
    
    /**
     * Get risk level icon
     */
    public static String getRiskIcon(double risk) {
        if (risk > 0.3) return "🔴";
        if (risk > 0.15) return "🟡";
        return "🟢";
    }
    
    /**
     * Get risk level text
     */
    public static String getRiskLevelText(double risk) {
        if (risk > 0.3) return "HIGH";
        if (risk > 0.15) return "MODERATE";
        return "LOW";
    }
    
    /**
     * Create a visual bar for risk level
     */
    public static String createRiskBar(double risk, int width) {
        int filled = (int)(risk * width);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < width; i++) {
            if (i < filled) {
                bar.append("█");
            } else if (i < width * 0.3) {
                bar.append("░");
            } else if (i < width * 0.7) {
                bar.append("░");
            } else {
                bar.append("░");
            }
        }
        bar.append("]");
        return bar.toString();
    }
    
    /**
     * Format patient profile for display
     */
    public static String formatPatientProfile(PatientRecord patient) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n📋 PATIENT PROFILE:\n");
        sb.append("-".repeat(50)).append("\n");
        
        Map<String, Object> features = patient.getFeatures();
        for (Map.Entry<String, Object> entry : features.entrySet()) {
            sb.append(String.format("  %-25s: %s%n", entry.getKey(), entry.getValue()));
        }
        
        return sb.toString();
    }
    
    /**
     * Format interventions for display
     */
    public static String formatInterventions(List<InterventionResult> interventions, int maxToShow) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n💊 RECOMMENDED INTERVENTIONS (Ranked by Effectiveness):\n");
        sb.append("-".repeat(70)).append("\n");
        
        for (int i = 0; i < Math.min(maxToShow, interventions.size()); i++) {
            InterventionResult ir = interventions.get(i);
            sb.append(String.format("\n%d. %s (%s)%n", i+1, ir.getName(), ir.getCategory()));
            sb.append(String.format("   📝 %s%n", ir.getDescription()));
            sb.append(String.format("   📉 Risk Reduction: %.1f%% (from %s → %s)%n",
                ir.getReductionPercentage(),
                formatRiskPercentage(ir.getCurrentRisk()),
                formatRiskPercentage(ir.getNewRisk())));
            sb.append(String.format("   Effectiveness: %s %.1f%%%n",
                ir.getEffectivenessBar(50),
                ir.getReductionPercentage()));
        }
        
        return sb.toString();
    }
    
    /**
     * Format clinical summary
     */
    public static String formatClinicalSummary(PatientRecord patient) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n📝 CLINICAL SUMMARY:\n");
        sb.append("-".repeat(50)).append("\n");
        
        double risk = patient.getPredictedRisk();
        List<InterventionResult> interventions = patient.getInterventions();
        
        if (risk > 0.3) {
            sb.append("⚠️  HIGH RISK: Immediate intervention strongly recommended.\n");
            if (!interventions.isEmpty()) {
                sb.append("   Priority actions: ").append(interventions.get(0).getName());
                if (interventions.size() > 1) {
                    sb.append(" and ").append(interventions.get(1).getName());
                }
                sb.append("\n");
            }
        } else if (risk > 0.15) {
            sb.append("⚡ MODERATE RISK: Lifestyle modifications recommended.\n");
            if (!interventions.isEmpty()) {
                sb.append("   Focus on: ").append(interventions.get(0).getName()).append("\n");
            }
        } else {
            sb.append("✅ LOW RISK: Continue maintaining healthy habits.\n");
            if (!interventions.isEmpty()) {
                sb.append("   Preventive actions: ").append(interventions.get(0).getName()).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Generate a complete patient report
     */
    public static String generatePatientReport(PatientRecord patient) {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(70)).append("\n");
        sb.append("CARDIOVASCULAR DISEASE RISK ASSESSMENT REPORT\n");
        sb.append("=".repeat(70)).append("\n");
        sb.append("Patient ID: ").append(patient.getId()).append("\n");
        sb.append("Analysis Date: ").append(dateFormat.format(patient.getAnalysisDate())).append("\n");
        
        double risk = patient.getPredictedRisk();
        sb.append("\n🎯 PREDICTED CVD RISK: ").append(getRiskIcon(risk));
        sb.append(" ").append(formatRiskPercentage(risk));
        sb.append(" (").append(getRiskLevelText(risk)).append(" RISK)\n");
        
        sb.append(formatPatientProfile(patient));
        sb.append(formatInterventions(patient.getInterventions(), 5));
        sb.append(formatClinicalSummary(patient));
        sb.append("\n").append("=".repeat(70)).append("\n");
        
        return sb.toString();
    }
    
    /**
     * Generate comparison report between two patients
     */
    public static String generateComparisonReport(PatientRecord p1, PatientRecord p2) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(80)).append("\n");
        sb.append("COMPARISON REPORT\n");
        sb.append("=".repeat(80)).append("\n");
        
        sb.append(String.format("\n%-20s %-20s %-20s%n", "Metric", "Patient " + p1.getId(), "Patient " + p2.getId()));
        sb.append("-".repeat(60)).append("\n");
        
        sb.append(String.format("%-20s %-20s %-20s%n", "CVD Risk", 
            formatRiskPercentage(p1.getPredictedRisk()),
            formatRiskPercentage(p2.getPredictedRisk())));
        
        double riskDiff = Math.abs(p1.getPredictedRisk() - p2.getPredictedRisk()) * 100;
        sb.append(String.format("%-20s %-20s %-20.1f%%%n", "Risk Difference", "", riskDiff));
        
        sb.append(String.format("%-20s %-20s %-20s%n", "Risk Level", 
            p1.getRiskLevel(), p2.getRiskLevel()));
        
        InterventionResult top1 = p1.getTopIntervention();
        InterventionResult top2 = p2.getTopIntervention();
        
        sb.append(String.format("%-20s %-20s %-20s%n", "Top Intervention", 
            top1 != null ? top1.getName() : "N/A",
            top2 != null ? top2.getName() : "N/A"));
        
        sb.append(String.format("%-20s %-20.1f%% %-20.1f%%%n", "Potential Reduction",
            top1 != null ? top1.getReductionPercentage() : 0,
            top2 != null ? top2.getReductionPercentage() : 0));
        
        sb.append("\n💡 Recommendation: ").append(
            p1.getPredictedRisk() > p2.getPredictedRisk() ? 
            "Patient " + p1.getId() + " requires more urgent intervention" :
            "Patient " + p2.getId() + " requires more urgent intervention"
        ).append("\n");
        
        return sb.toString();
    }
    
    /**
     * Generate statistics report
     */
    public static String generateStatisticsReport(Map<String, Integer> riskDistribution, 
                                                   double averageRisk, int totalPatients) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(60)).append("\n");
        sb.append("DATABASE STATISTICS\n");
        sb.append("=".repeat(60)).append("\n");
        sb.append("Total Patients: ").append(totalPatients).append("\n");
        sb.append(String.format("Average CVD Risk: %.1f%%%n", averageRisk * 100));
        sb.append("\nRisk Distribution:\n");
        sb.append("  🔴 High Risk (>30%): ").append(riskDistribution.getOrDefault("HIGH", 0)).append("\n");
        sb.append("  🟡 Moderate Risk (15-30%): ").append(riskDistribution.getOrDefault("MODERATE", 0)).append("\n");
        sb.append("  🟢 Low Risk (<15%): ").append(riskDistribution.getOrDefault("LOW", 0)).append("\n");
        sb.append("=".repeat(60)).append("\n");
        
        return sb.toString();
    }
}