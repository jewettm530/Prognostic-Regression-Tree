
import java.util.*;

/**
 * Node class for the Regression Tree data structure
 */
public class RegressionTreeNode {
    private int featureIndex;
    private double threshold;
    private double value;  // For leaf nodes
    private RegressionTreeNode left;
    private RegressionTreeNode right;
    private boolean isLeaf;
    private String featureName;
    
    // Constructor for leaf node
    public RegressionTreeNode(double value) {
        this.isLeaf = true;
        this.value = value;
    }
    
    // Constructor for decision node
    public RegressionTreeNode(int featureIndex, double threshold, String featureName) {
        this.featureIndex = featureIndex;
        this.threshold = threshold;
        this.featureName = featureName;
        this.isLeaf = false;
    }
    
    /**
     * Predict risk for a given patient
     */
    public double predict(Map<String, Object> features, List<String> featureOrder) {
        if (isLeaf) {
            return value;
        }
        
        double featureValue = getFeatureValue(features, featureOrder.get(featureIndex));
        
        if (featureValue <= threshold) {
            return left.predict(features, featureOrder);
        } else {
            return right.predict(features, featureOrder);
        }
    }
    
    /**
     * Predict using feature array (for data-driven approach)
     */
    public double predict(double[] features) {
        if (isLeaf) {
            return value;
        }
        
        if (features[featureIndex] <= threshold) {
            return left.predict(features);
        } else {
            return right.predict(features);
        }
    }
    
    private double getFeatureValue(Map<String, Object> features, String featureName) {
        Object value = features.get(featureName);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            return encodeCategorical(featureName, (String) value);
        }
        return 0.0;
    }
    
    private double encodeCategorical(String featureName, String value) {
        switch (featureName) {
            case "Gender":
                return value.equalsIgnoreCase("Male") ? 1.0 : 0.0;
            case "Exercise Habits":
                switch (value.toLowerCase()) {
                    case "low": return 0.0;
                    case "medium": return 1.0;
                    case "high": return 2.0;
                    default: return 1.0;
                }
            case "Smoking":
            case "Family Heart Disease":
            case "Diabetes":
            case "High Blood Pressure":
            case "Low HDL Cholesterol":
            case "High LDL Cholesterol":
                return value.equalsIgnoreCase("Yes") ? 1.0 : 0.0;
            case "Alcohol Consumption":
                switch (value.toLowerCase()) {
                    case "none": return 0.0;
                    case "low": return 1.0;
                    case "medium": return 2.0;
                    case "high": return 3.0;
                    default: return 1.0;
                }
            default:
                return 0.0;
        }
    }
    
    // Setters for tree construction
    public void setLeft(RegressionTreeNode node) { this.left = node; }
    public void setRight(RegressionTreeNode node) { this.right = node; }
    
    // Getters for tree traversal and debugging
    public boolean isLeaf() { return isLeaf; }
    public double getValue() { return value; }
    public String getFeatureName() { return featureName; }
    public double getThreshold() { return threshold; }
    public int getFeatureIndex() { return featureIndex; }
    public RegressionTreeNode getLeft() { return left; }
    public RegressionTreeNode getRight() { return right; }
    
    /**
     * Print tree structure for debugging
     */
    public void printTree(String indent) {
        if (isLeaf) {
            System.out.println(indent + "Leaf: risk = " + String.format("%.3f", value));
        } else {
            System.out.println(indent + "Node: " + featureName + " <= " + threshold);
            System.out.println(indent + "  Left:");
            left.printTree(indent + "    ");
            System.out.println(indent + "  Right:");
            right.printTree(indent + "    ");
        }
    }
}