
import java.util.*;

public class PrognosticRegressionTree {
    private RegressionTreeNode root;
    private List<String> featureOrder;
    @SuppressWarnings("unused")
    private Random random;
    
    // For tracking feature importance
    private Map<String, Double> featureImportance;
    
    public PrognosticRegressionTree() {
        this.featureOrder = Arrays.asList(
            "Age", "Gender", "Blood Pressure", "Cholesterol Level", 
            "Exercise Habits", "Smoking", "Family Heart Disease", "Diabetes",
            "BMI", "High Blood Pressure", "Low HDL Cholesterol", 
            "High LDL Cholesterol", "Alcohol Consumption"
        );
        this.random = new Random(42); // Seed for reproducibility
        this.featureImportance = new HashMap<>();
        
        // Initialize with a default tree (will be replaced if buildTreeFromData is called)
        buildDefaultTree();
    }
    
    /**
     * Build a default expert-driven tree (fallback if no data-driven training)
     */
    private void buildDefaultTree() {
        // Root node: Age split at 50
        root = new RegressionTreeNode(0, 50.0, "Age");
        
        // Left branch (Age <= 50)
        RegressionTreeNode leftBranch = new RegressionTreeNode(5, 0.5, "Smoking");
        leftBranch.setLeft(new RegressionTreeNode(0.15));  // Non-smoker
        leftBranch.setRight(new RegressionTreeNode(0.35)); // Smoker
        
        // Right branch (Age > 50)
        RegressionTreeNode rightBranch = new RegressionTreeNode(8, 25.0, "BMI");
        
        // BMI <= 25 branch
        RegressionTreeNode bmiLowBranch = new RegressionTreeNode(9, 0.5, "High Blood Pressure");
        bmiLowBranch.setLeft(new RegressionTreeNode(0.25));   // No HBP
        bmiLowBranch.setRight(new RegressionTreeNode(0.45));  // Has HBP
        
        // BMI > 25 branch
        RegressionTreeNode bmiHighBranch = new RegressionTreeNode(3, 200.0, "Cholesterol Level");
        
        // Cholesterol <= 200
        RegressionTreeNode cholLowBranch = new RegressionTreeNode(5, 0.5, "Smoking");
        cholLowBranch.setLeft(new RegressionTreeNode(0.35));
        cholLowBranch.setRight(new RegressionTreeNode(0.55));
        
        // Cholesterol > 200
        RegressionTreeNode cholHighBranch = new RegressionTreeNode(4, 1.0, "Exercise Habits");
        cholHighBranch.setLeft(new RegressionTreeNode(0.65));  // Low exercise
        cholHighBranch.setRight(new RegressionTreeNode(0.50)); // Med/High exercise
        
        bmiHighBranch.setLeft(cholLowBranch);
        bmiHighBranch.setRight(cholHighBranch);
        
        rightBranch.setLeft(bmiLowBranch);
        rightBranch.setRight(bmiHighBranch);
        
        root.setLeft(leftBranch);
        root.setRight(rightBranch);
    }
    
    /**
     * Build tree by learning from actual patient data
     */
    public void buildTreeFromData(List<PatientRecord> patients, int maxDepth, int minSamplesSplit) {
        System.out.println("\n🌲 Building Regression Tree from " + patients.size() + " patients...");
        System.out.println("   Max depth: " + maxDepth);
        System.out.println("   Min samples per split: " + minSamplesSplit);
        
        // Convert patients to feature vectors
        List<double[]> features = new ArrayList<>();
        double[] targets = new double[patients.size()];
        
        for (int i = 0; i < patients.size(); i++) {
            PatientRecord patient = patients.get(i);
            double[] featureVector = extractFeatureVector(patient);
            features.add(featureVector);
            targets[i] = patient.getPredictedRisk();
        }
        
        // Build tree recursively
        root = buildTreeRecursive(features, targets, 0, maxDepth, minSamplesSplit);
        
        // Normalize feature importance
        double total = featureImportance.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total > 0) {
            featureImportance.replaceAll((k, v) -> v / total);
        }
        
        printFeatureImportance();
    }
    
    private RegressionTreeNode buildTreeRecursive(List<double[]> features, double[] targets, 
                                                   int depth, int maxDepth, int minSamplesSplit) {
        // Check stopping conditions
        if (depth >= maxDepth || features.size() < minSamplesSplit || isPure(targets)) {
            double averageRisk = calculateAverage(targets);
            return new RegressionTreeNode(averageRisk);
        }
        
        // Find best split
        Split bestSplit = findBestSplit(features, targets);
        
        if (bestSplit == null || bestSplit.impurityReduction < 0.001) {
            double averageRisk = calculateAverage(targets);
            return new RegressionTreeNode(averageRisk);
        }
        
        // Track feature importance
        String featureName = featureOrder.get(bestSplit.featureIndex);
        featureImportance.put(featureName, 
            featureImportance.getOrDefault(featureName, 0.0) + bestSplit.impurityReduction);
        
        // Create node and split data
        RegressionTreeNode node = new RegressionTreeNode(bestSplit.featureIndex, 
                                                          bestSplit.threshold, 
                                                          featureName);
        
        List<double[]> leftFeatures = new ArrayList<>();
        List<double[]> rightFeatures = new ArrayList<>();
        List<Double> leftTargets = new ArrayList<>();
        List<Double> rightTargets = new ArrayList<>();
        
        for (int i = 0; i < features.size(); i++) {
            if (features.get(i)[bestSplit.featureIndex] <= bestSplit.threshold) {
                leftFeatures.add(features.get(i));
                leftTargets.add(targets[i]);
            } else {
                rightFeatures.add(features.get(i));
                rightTargets.add(targets[i]);
            }
        }
        
        // Convert to arrays
        double[] leftTargetsArray = leftTargets.stream().mapToDouble(Double::doubleValue).toArray();
        double[] rightTargetsArray = rightTargets.stream().mapToDouble(Double::doubleValue).toArray();
        
        // Recursively build children
        node.setLeft(buildTreeRecursive(leftFeatures, leftTargetsArray, depth + 1, maxDepth, minSamplesSplit));
        node.setRight(buildTreeRecursive(rightFeatures, rightTargetsArray, depth + 1, maxDepth, minSamplesSplit));
        
        return node;
    }
    
    private Split findBestSplit(List<double[]> features, double[] targets) {
        Split bestSplit = null;
        double bestImpurityReduction = 0;
        double currentImpurity = calculateMSE(targets);
        
        // Try each feature
        for (int featureIdx = 0; featureIdx < featureOrder.size(); featureIdx++) {
            // Get unique values for this feature
            Set<Double> uniqueValues = new TreeSet<>();
            for (double[] feature : features) {
                uniqueValues.add(feature[featureIdx]);
            }
            
            // Try potential split points
            List<Double> splitCandidates = getSplitCandidates(uniqueValues);
            
            for (double threshold : splitCandidates) {
                List<Double> leftTargets = new ArrayList<>();
                List<Double> rightTargets = new ArrayList<>();
                
                for (int i = 0; i < features.size(); i++) {
                    if (features.get(i)[featureIdx] <= threshold) {
                        leftTargets.add(targets[i]);
                    } else {
                        rightTargets.add(targets[i]);
                    }
                }
                
                if (leftTargets.isEmpty() || rightTargets.isEmpty()) {
                    continue;
                }
                
                double leftMSE = calculateMSE(leftTargets.stream().mapToDouble(Double::doubleValue).toArray());
                double rightMSE = calculateMSE(rightTargets.stream().mapToDouble(Double::doubleValue).toArray());
                
                double weightedImpurity = (leftTargets.size() * leftMSE + rightTargets.size() * rightMSE) / features.size();
                double impurityReduction = currentImpurity - weightedImpurity;
                
                if (impurityReduction > bestImpurityReduction) {
                    bestImpurityReduction = impurityReduction;
                    bestSplit = new Split(featureIdx, threshold, impurityReduction);
                }
            }
        }
        
        return bestSplit;
    }
    
    private List<Double> getSplitCandidates(Set<Double> uniqueValues) {
        List<Double> values = new ArrayList<>(uniqueValues);
        Collections.sort(values);
        
        // For large datasets, sample fewer split points for efficiency
        if (values.size() > 20) {
            List<Double> sampled = new ArrayList<>();
            for (int i = 0; i < Math.min(20, values.size()); i++) {
                int index = (i * values.size()) / 20;
                sampled.add(values.get(index));
            }
            return sampled;
        }
        
        // For small sets, try midpoints between values
        List<Double> candidates = new ArrayList<>();
        for (int i = 0; i < values.size() - 1; i++) {
            double mid = (values.get(i) + values.get(i + 1)) / 2;
            candidates.add(mid);
        }
        
        if (candidates.isEmpty()) {
            candidates.add(values.get(0));
        }
        
        return candidates;
    }
    
    private double[] extractFeatureVector(PatientRecord patient) {
        double[] vector = new double[featureOrder.size()];
        Map<String, Object> features = patient.getFeatures();
        
        for (int i = 0; i < featureOrder.size(); i++) {
            String featureName = featureOrder.get(i);
            Object value = features.get(featureName);
            
            if (value instanceof Number) {
                vector[i] = ((Number) value).doubleValue();
            } else if (value instanceof String) {
                vector[i] = encodeCategoricalForVector(featureName, (String) value);
            } else {
                vector[i] = 0;
            }
        }
        
        return vector;
    }
    
    private double encodeCategoricalForVector(String featureName, String value) {
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
    
    private double calculateMSE(double[] values) {
        if (values.length == 0) return 0;
        double mean = calculateAverage(values);
        double mse = 0;
        for (double v : values) {
            mse += Math.pow(v - mean, 2);
        }
        return mse / values.length;
    }
    
    private double calculateAverage(double[] values) {
        if (values.length == 0) return 0;
        double sum = 0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.length;
    }
    
    private boolean isPure(double[] targets) {
        if (targets.length == 0) return true;
        double first = targets[0];
        for (double t : targets) {
            if (Math.abs(t - first) > 0.01) {
                return false;
            }
        }
        return true;
    }
    
    public double predictRisk(PatientRecord patient) {
        double[] featureVector = extractFeatureVector(patient);
        double risk = predictRecursive(root, featureVector);
        patient.setPredictedRisk(risk);
        return risk;
    }
    
    private double predictRecursive(RegressionTreeNode node, double[] features) {
        if (node.isLeaf()) {
            return node.getValue();
        }
        
        if (features[node.getFeatureIndex()] <= node.getThreshold()) {
            return predictRecursive(node.getLeft(), features);
        } else {
            return predictRecursive(node.getRight(), features);
        }
    }
    
    public List<InterventionResult> analyzeInterventions(PatientRecord patient) {
        List<InterventionResult> results = new ArrayList<>();
        double currentRisk = patient.getPredictedRisk();
        
        // Define interventions
        List<Intervention> interventions = getAllInterventions();
        
        for (Intervention intervention : interventions) {
            PatientRecord modifiedPatient = applyIntervention(patient, intervention);
            double newRisk = predictRisk(modifiedPatient);
            
            InterventionResult result = new InterventionResult(
                intervention.name, intervention.description, intervention.category,
                currentRisk, newRisk
            );
            results.add(result);
        }
        
        Collections.sort(results);
        for (int i = 0; i < results.size(); i++) {
            results.get(i).setPriority(i + 1);
        }
        
        patient.getInterventions().clear();
        patient.getInterventions().addAll(results);
        
        return results;
    }
    
    private void printFeatureImportance() {
        System.out.println("\n📊 FEATURE IMPORTANCE (Learned from your data):");
        System.out.println("=".repeat(50));
        
        List<Map.Entry<String, Double>> sorted = new ArrayList<>(featureImportance.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        
        for (Map.Entry<String, Double> entry : sorted) {
            String icon = getImportanceIcon(entry.getValue());
            System.out.printf("%s %-25s: %.1f%%%n", icon, entry.getKey(), entry.getValue() * 100);
        }
        System.out.println("=".repeat(50));
    }
    
    private String getImportanceIcon(double importance) {
        if (importance > 0.15) return "🔴";
        if (importance > 0.10) return "🟠";
        if (importance > 0.05) return "🟡";
        return "🟢";
    }
    
    private List<Intervention> getAllInterventions() {
        List<Intervention> interventions = new ArrayList<>();
        interventions.add(new Intervention("Stop Smoking", "Quit smoking completely", "Lifestyle", "Smoking", 1.0, 0.0));
        interventions.add(new Intervention("Increase Exercise", "Increase exercise to High level", "Lifestyle", "Exercise Habits", "Medium", "High"));
        interventions.add(new Intervention("Reduce Alcohol", "Reduce alcohol consumption to Low level", "Lifestyle", "Alcohol Consumption", "High", "Low"));
        interventions.add(new Intervention("Lower BMI", "Reduce BMI by 10%", "Health", "BMI", true, 0.10));
        interventions.add(new Intervention("Control Blood Pressure", "Lower blood pressure by 15%", "Medical", "Blood Pressure", true, 0.15));
        interventions.add(new Intervention("Lower Cholesterol", "Lower cholesterol by 20%", "Medical", "Cholesterol Level", true, 0.20));
        interventions.add(new Intervention("Manage Diabetes", "Get diabetes under control", "Medical", "Diabetes", 1.0, 0.0));
        return interventions;
    }
    
    private PatientRecord applyIntervention(PatientRecord original, Intervention intervention) {
        PatientRecord modified = new PatientRecord(original.getId());
        modified.getFeatures().putAll(original.getFeatures());
        
        Object currentValue = modified.getFeatures().get(intervention.featureName);
        
        if (intervention.isPercentageReduction) {
            if (currentValue instanceof Number) {
                double newValue = ((Number) currentValue).doubleValue() * (1 - intervention.reductionValue);
                modified.getFeatures().put(intervention.featureName, newValue);
            }
        } else if (intervention.targetValue != null) {
            modified.getFeatures().put(intervention.featureName, intervention.targetValue);
        }
        
        return modified;
    }
    
    public void printTree() {
        System.out.println("\n=== Regression Tree Structure ===");
        root.printTree("");
        System.out.println("================================\n");
    }
    
    private static class Split {
        int featureIndex;
        double threshold;
        double impurityReduction;
        
        Split(int featureIndex, double threshold, double impurityReduction) {
            this.featureIndex = featureIndex;
            this.threshold = threshold;
            this.impurityReduction = impurityReduction;
        }
    }
    
    private static class Intervention {
        String name;
        String description;
        String category;
        String featureName;
        boolean isPercentageReduction;
        double reductionValue;
        Object targetValue;
        
        Intervention(String name, String description, String category, 
                    String featureName, Object targetValue, Object newValue) {
            this.name = name;
            this.description = description;
            this.category = category;
            this.featureName = featureName;
            this.isPercentageReduction = false;
            this.targetValue = newValue;
        }
        
        Intervention(String name, String description, String category,
                    String featureName, boolean isPercentage, double reductionValue) {
            this.name = name;
            this.description = description;
            this.category = category;
            this.featureName = featureName;
            this.isPercentageReduction = isPercentage;
            this.reductionValue = reductionValue;
        }
    }
}