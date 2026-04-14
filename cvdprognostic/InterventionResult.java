/**
 * Data structure for storing intervention analysis results
 */
public class InterventionResult implements Comparable<InterventionResult> {
    private String name;
    private String description;
    private String category;
    private double currentRisk;
    private double newRisk;
    private double riskReduction;
    private double reductionPercentage;
    private int priority;
    
    public InterventionResult(String name, String description, String category, 
                             double currentRisk, double newRisk) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.currentRisk = currentRisk;
        this.newRisk = newRisk;
        this.riskReduction = currentRisk - newRisk;
        this.reductionPercentage = (riskReduction / currentRisk) * 100;
        this.priority = 0;
    }
    
    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public double getCurrentRisk() { return currentRisk; }
    public double getNewRisk() { return newRisk; }
    public double getRiskReduction() { return riskReduction; }
    public double getReductionPercentage() { return reductionPercentage; }
    public int getPriority() { return priority; }
    
    // Setter
    public void setPriority(int priority) { this.priority = priority; }
    
    @Override
    public int compareTo(InterventionResult other) {
        return Double.compare(other.riskReduction, this.riskReduction);
    }
    
    public String getEffectivenessBar(int width) {
        int filled = (int)((reductionPercentage / 100) * width);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < width; i++) {
            bar.append(i < filled ? "█" : "░");
        }
        bar.append("]");
        return bar.toString();
    }
    
    @Override
    public String toString() {
        return String.format("%s (%.1f%% reduction)", name, reductionPercentage);
    }
    
    public String toDetailedString() {
        return String.format(
            "\n%d. %s (%s)\n" +
            "   📝 %s\n" +
            "   📉 Risk Reduction: %.1f%% (from %.1f%% → %.1f%%)",
            priority, name, category, description, 
            reductionPercentage, currentRisk * 100, newRisk * 100
        );
    }
}