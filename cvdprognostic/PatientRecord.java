import java.util.*;

/**
 * Data structure for storing patient information and analysis results
 */
public class PatientRecord {
    private String id;
    private Map<String, Object> features;
    private Double predictedRisk;
    private List<InterventionResult> interventions;
    private Date analysisDate;
    
    public PatientRecord(String id) {
        this.id = id;
        this.features = new HashMap<>();
        this.interventions = new ArrayList<>();
        this.analysisDate = new Date();
    }
    
    // Getters
    public String getId() { return id; }
    public Map<String, Object> getFeatures() { return features; }
    public Double getPredictedRisk() { return predictedRisk; }
    public List<InterventionResult> getInterventions() { return interventions; }
    public Date getAnalysisDate() { return analysisDate; }
    
    // Setters
    public void setPredictedRisk(Double risk) { this.predictedRisk = risk; }
    public void addIntervention(InterventionResult ir) { interventions.add(ir); }
    public void setAnalysisDate(Date date) { this.analysisDate = date; }
    
    // Utility methods
    public String getRiskLevel() {
        if (predictedRisk == null) return "UNKNOWN";
        if (predictedRisk > 0.3) return "HIGH";
        if (predictedRisk > 0.15) return "MODERATE";
        return "LOW";
    }
    
    public String getRiskIcon() {
        switch (getRiskLevel()) {
            case "HIGH": return "🔴";
            case "MODERATE": return "🟡";
            case "LOW": return "🟢";
            default: return "⚪";
        }
    }
    
    public InterventionResult getTopIntervention() {
        if (interventions.isEmpty()) return null;
        return interventions.get(0);
    }
    
    @Override
    public String toString() {
        return String.format("Patient{id='%s', risk=%.2f, level=%s}", 
                            id, predictedRisk, getRiskLevel());
    }
}