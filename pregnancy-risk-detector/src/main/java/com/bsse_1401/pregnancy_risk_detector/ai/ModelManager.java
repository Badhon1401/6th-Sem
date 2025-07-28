package com.bsse_1401.pregnancy_risk_detector.ai;

import com.bsse_1401.pregnancy_risk_detector.model.HealthData;
import org.springframework.stereotype.Component;
import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.meta.Vote;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;

@Component
public class ModelManager {

    private Map<String, Classifier> trainedModels = new ConcurrentHashMap<>();
    private Map<String, Instances> modelDatasets = new ConcurrentHashMap<>();
    private Map<String, String[]> modelFeatures = new ConcurrentHashMap<>();
    private Map<String, Double> modelAccuracies = new ConcurrentHashMap<>();

    private static final String MODEL_SAVE_PATH = "models/";

    /**
     * Train multiple models for different feature combinations
     */
    public void trainModels(List<HealthData> trainingData) {
        System.out.println("🤖 Starting AI model training with " + trainingData.size() + " samples...");

        // Define different feature combinations for specialized models
        Map<String, String[]> featureCombinations = getFeatureCombinations();

        for (Map.Entry<String, String[]> entry : featureCombinations.entrySet()) {
            String modelKey = entry.getKey();
            String[] features = entry.getValue();

            try {
                System.out.println("Training model: " + modelKey + " with features: " + Arrays.toString(features));

                // Filter and prepare data for this feature combination
                List<HealthData> filteredData = filterDataByFeatures(trainingData, features);

                if (filteredData.size() < 10) {
                    System.out.println("⚠️  Insufficient data for model: " + modelKey + " (only " + filteredData.size() + " samples)");
                    continue;
                }

                // Create Weka dataset
                Instances dataset = createWekaDataset(filteredData, features, modelKey);
                modelDatasets.put(modelKey, dataset);
                modelFeatures.put(modelKey, features);

                // Train different algorithms and select the best one
                Classifier bestClassifier = trainAndSelectBestClassifier(dataset, modelKey);

                if (bestClassifier != null) {
                    trainedModels.put(modelKey, bestClassifier);
                    saveModel(bestClassifier, modelKey);
                    System.out.println("✅ Successfully trained model: " + modelKey);
                } else {
                    System.out.println("❌ Failed to train model: " + modelKey);
                }

            } catch (Exception e) {
                System.err.println("❌ Error training model " + modelKey + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("🎉 Model training completed! Trained " + trainedModels.size() + " models.");
    }

    /**
     * Define feature combinations for different specialized models
     */
    private Map<String, String[]> getFeatureCombinations() {
        Map<String, String[]> combinations = new LinkedHashMap<>();

        // Full feature model
        combinations.put("full_model", new String[]{
                "age", "systolicBP", "diastolicBP", "bloodSugar", "bodyTemp", "bmi",
                "previousComplications", "preexistingDiabetes", "gestationalDiabetes",
                "mentalHealth", "heartRate"
        });

        // Basic vitals model
        combinations.put("vitals_model", new String[]{
                "age", "systolicBP", "diastolicBP", "heartRate", "bodyTemp"
        });

        // Diabetes-focused model
        combinations.put("diabetes_model", new String[]{
                "age", "bloodSugar", "bmi", "preexistingDiabetes", "gestationalDiabetes"
        });

        // Cardiovascular model
        combinations.put("cardio_model", new String[]{
                "age", "systolicBP", "diastolicBP", "heartRate", "bmi", "previousComplications"
        });

        // Minimal model (for cases with very limited data)
        combinations.put("minimal_model", new String[]{
                "age", "systolicBP", "diastolicBP"
        });

        // Temperature-focused model
        combinations.put("temp_model", new String[]{
                "bodyTemp", "heartRate", "age"
        });

        // Mental health model
        combinations.put("mental_model", new String[]{
                "age", "mentalHealth", "previousComplications", "systolicBP", "diastolicBP"
        });

        return combinations;
    }

    /**
     * Filter training data to only include samples with required features
     */
    private List<HealthData> filterDataByFeatures(List<HealthData> data, String[] requiredFeatures) {
        List<HealthData> filtered = new ArrayList<>();

        for (HealthData sample : data) {
            boolean hasAllFeatures = true;

            for (String feature : requiredFeatures) {
                if (!hasFeature(sample, feature)) {
                    hasAllFeatures = false;
                    break;
                }
            }

            if (hasAllFeatures && sample.getRiskLevel() != null && !sample.getRiskLevel().equals("UNKNOWN")) {
                filtered.add(sample);
            }
        }

        return filtered;
    }

    private boolean hasFeature(HealthData data, String feature) {
        switch (feature) {
            case "age": return data.getAge() != null;
            case "systolicBP": return data.getSystolicBP() != null;
            case "diastolicBP": return data.getDiastolicBP() != null;
            case "bloodSugar": return data.getBloodSugar() != null;
            case "bodyTemp": return data.getBodyTemp() != null;
            case "bmi": return data.getBmi() != null;
            case "previousComplications": return data.getPreviousComplications() != null;
            case "preexistingDiabetes": return data.getPreexistingDiabetes() != null;
            case "gestationalDiabetes": return data.getGestationalDiabetes() != null;
            case "mentalHealth": return data.getMentalHealth() != null;
            case "heartRate": return data.getHeartRate() != null;
            default: return false;
        }
    }

    /**
     * Create Weka dataset from health data
     */
    private Instances createWekaDataset(List<HealthData> data, String[] features, String datasetName) {
        // Create attributes
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();

        for (String feature : features) {
            attributes.add(new Attribute(feature));
        }

        // Create class attribute (risk levels)
        Set<String> uniqueRiskLevels = new HashSet<>();
        for (HealthData sample : data) {
            if (sample.getRiskLevel() != null) {
                uniqueRiskLevels.add(sample.getRiskLevel());
            }
        }

        ArrayList<String> classValues = new ArrayList<>(uniqueRiskLevels);
        Collections.sort(classValues); // Ensure consistent ordering
        Attribute classAttribute = new Attribute("riskLevel", classValues);
        attributes.add(classAttribute);

        // Create dataset
        Instances dataset = new Instances(datasetName, attributes, data.size());
        dataset.setClassIndex(dataset.numAttributes() - 1);

        // Add instances
        for (HealthData sample : data) {
            double[] values = new double[features.length + 1]; // +1 for class

            for (int i = 0; i < features.length; i++) {
                values[i] = getFeatureValue(sample, features[i]);
            }

            // Set class value
            values[features.length] = classValues.indexOf(sample.getRiskLevel());

            Instance instance = new DenseInstance(1.0, values);
            dataset.add(instance);
        }

        return dataset;
    }

    private double getFeatureValue(HealthData data, String feature) {
        switch (feature) {
            case "age": return data.getAge() != null ? data.getAge() : 0.0;
            case "systolicBP": return data.getSystolicBP() != null ? data.getSystolicBP() : 0.0;
            case "diastolicBP": return data.getDiastolicBP() != null ? data.getDiastolicBP() : 0.0;
            case "bloodSugar": return data.getBloodSugar() != null ? data.getBloodSugar() : 0.0;
            case "bodyTemp": return data.getBodyTemp() != null ? data.getBodyTemp() : 0.0;
            case "bmi": return data.getBmi() != null ? data.getBmi() : 0.0;
            case "previousComplications": return data.getPreviousComplications() != null ? data.getPreviousComplications() : 0.0;
            case "preexistingDiabetes": return data.getPreexistingDiabetes() != null ? data.getPreexistingDiabetes() : 0.0;
            case "gestationalDiabetes": return data.getGestationalDiabetes() != null ? data.getGestationalDiabetes() : 0.0;
            case "mentalHealth": return data.getMentalHealth() != null ? data.getMentalHealth() : 0.0;
            case "heartRate": return data.getHeartRate() != null ? data.getHeartRate() : 0.0;
            default: return 0.0;
        }
    }

    /**
     * Train multiple algorithms and select the best performing one
     */
    private Classifier trainAndSelectBestClassifier(Instances dataset, String modelKey) throws Exception {
        Map<String, Classifier> algorithms = new HashMap<String,Classifier>();
        Map<String, Double> accuracies = new HashMap<>();

        // Random Forest
        RandomForest rf = new RandomForest();
        rf.setNumIterations(100);
        rf.setNumFeatures(Math.max(1, (int) Math.sqrt(dataset.numAttributes() - 1)));
        algorithms.put("RandomForest", rf);

        // Support Vector Machine
        SMO svm = new SMO();
        svm.setOptions(weka.core.Utils.splitOptions("-M"));
        algorithms.put("SVM", svm);

        // Neural Network
        MultilayerPerceptron mlp = new MultilayerPerceptron();
        mlp.setLearningRate(0.1);
        mlp.setMomentum(0.2);
        mlp.setTrainingTime(500);
        mlp.setHiddenLayers("a"); // Auto-select hidden layer size
        algorithms.put("NeuralNetwork", mlp);

        // Train and evaluate each algorithm
        String bestAlgorithm = null;
        double bestAccuracy = 0.0;
        Classifier bestClassifier = null;

        for (Map.Entry<String, Classifier> entry : algorithms.entrySet()) {
            String algName = entry.getKey();
            Classifier classifier = entry.getValue();

            try {
                // Train the classifier
                classifier.buildClassifier(dataset);

                // Evaluate using cross-validation
                double accuracy = evaluateClassifier(classifier, dataset);
                accuracies.put(algName, accuracy);

                System.out.println("  " + algName + " accuracy: " + String.format("%.2f%%", accuracy * 100));

                if (accuracy > bestAccuracy) {
                    bestAccuracy = accuracy;
                    bestAlgorithm = algName;
                    bestClassifier = classifier;
                }

            } catch (Exception e) {
                System.err.println("  Failed to train " + algName + ": " + e.getMessage());
            }
        }

        if (bestClassifier != null) {
            modelAccuracies.put(modelKey, bestAccuracy);
            System.out.println("  🏆 Best algorithm for " + modelKey + ": " + bestAlgorithm +
                    " (accuracy: " + String.format("%.2f%%", bestAccuracy * 100) + ")");
        }

        return bestClassifier;
    }

    /**
     * Evaluate classifier using cross-validation
     */
    private double evaluateClassifier(Classifier classifier, Instances dataset) throws Exception {
        weka.classifiers.Evaluation eval = new weka.classifiers.Evaluation(dataset);
        eval.crossValidateModel(classifier, dataset, 5, new Random(42));
        return eval.pctCorrect() / 100.0;
    }

    /**
     * Find the best model for given input features
     */
    public String findBestModel(HealthData inputData) {
        String[] availableFeatures = inputData.getAvailableFeatures();

        String bestModel = null;
        int maxMatchingFeatures = 0;
        double bestAccuracy = 0.0;

        for (Map.Entry<String, String[]> entry : modelFeatures.entrySet()) {
            String modelKey = entry.getKey();
            String[] modelFeatures = entry.getValue();

            // Count matching features
            int matchingFeatures = 0;
            for (String feature : modelFeatures) {
                for (String availableFeature : availableFeatures) {
                    if (feature.equals(availableFeature)) {
                        matchingFeatures++;
                        break;
                    }
                }
            }

            // Check if all required features are available
            if (matchingFeatures == modelFeatures.length) {
                double accuracy = modelAccuracies.getOrDefault(modelKey, 0.0);

                // Prefer model with higher accuracy, then more features
                if (accuracy > bestAccuracy ||
                        (accuracy == bestAccuracy && matchingFeatures > maxMatchingFeatures)) {
                    bestModel = modelKey;
                    maxMatchingFeatures = matchingFeatures;
                    bestAccuracy = accuracy;
                }
            }
        }

        return bestModel != null ? bestModel : "minimal_model"; // Fallback to minimal model
    }

    /**
     * Make prediction using the best available model
     */
    public Map<String, Object> predict(HealthData inputData, String selectedModel) throws Exception {
        System.out.println("🔧 [predict] Selected model: " + selectedModel);

        if (!trainedModels.containsKey(selectedModel)) {
            throw new RuntimeException("No suitable trained model found for the given features");
        }

        Classifier classifier = trainedModels.get(selectedModel);
        Instances dataset = modelDatasets.get(selectedModel);
        String[] features = modelFeatures.get(selectedModel);

        System.out.println("🧠 Model loaded: " + classifier.getClass().getSimpleName());
        System.out.println("📈 Features used: " + Arrays.toString(features));

        // Create input instance
        double[] values = new double[features.length + 1]; // +1 for missing class value

        for (int i = 0; i < features.length; i++) {
            values[i] = getFeatureValue(inputData, features[i]);
            System.out.println("   🔹 Feature [" + features[i] + "] = " + values[i]);
        }

        values[features.length] = Double.NaN; // Missing class

        Instance instance = new DenseInstance(1.0, values);
        instance.setDataset(dataset);

        System.out.println("📦 Instance prepared for prediction: " + instance);

        // Make prediction
        double prediction = classifier.classifyInstance(instance);
        double[] probabilities = classifier.distributionForInstance(instance);

        Attribute classAttribute = dataset.classAttribute();
        String predictedClass = classAttribute.value((int) prediction);

        System.out.println("✅ Prediction result: " + predictedClass);
        System.out.println("📊 Probability distribution:");
        for (int i = 0; i < probabilities.length; i++) {
            System.out.printf("   - %s: %.4f%n", classAttribute.value(i), probabilities[i]);
        }

        // Build result
        Map<String, Object> result = new HashMap<>();
        result.put("riskLevel", predictedClass);
        result.put("confidence", probabilities[(int) prediction]);
        result.put("modelUsed", selectedModel);
        result.put("features", String.join(", ", features));
        result.put("accuracy", modelAccuracies.getOrDefault(selectedModel, 0.0));

        Map<String, Double> probabilityMap = new HashMap<>();
        for (int i = 0; i < probabilities.length; i++) {
            probabilityMap.put(classAttribute.value(i), probabilities[i]);
        }
        result.put("probabilities", probabilityMap);

        return result;
    }


    /**
     * Save trained model to disk
     */
    private void saveModel(Classifier classifier, String modelKey) {
        try {
            File modelDir = new File(MODEL_SAVE_PATH);
            if (!modelDir.exists()) {
                modelDir.mkdirs();
            }

            String filename = MODEL_SAVE_PATH + modelKey + ".model";
            SerializationHelper.write(filename, classifier);
            System.out.println("💾 Model saved: " + filename);

        } catch (Exception e) {
            System.err.println("❌ Failed to save model " + modelKey + ": " + e.getMessage());
        }
    }

    /**
     * Load saved model from disk
     */
    public Classifier loadModel(String modelKey) {
        try {
            String filename = MODEL_SAVE_PATH + modelKey + ".model";
            return (Classifier) SerializationHelper.read(filename);
        } catch (Exception e) {
            System.err.println("❌ Failed to load model " + modelKey + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Load all saved models on startup
     */
    public void loadAllModels() {
        File modelDir = new File(MODEL_SAVE_PATH);
        if (!modelDir.exists()) {
            System.out.println("📁 Model directory doesn't exist, will be created when models are trained");
            return;
        }

        File[] modelFiles = modelDir.listFiles((dir, name) -> name.endsWith(".model"));
        if (modelFiles != null) {
            for (File modelFile : modelFiles) {
                String modelKey = modelFile.getName().replace(".model", "");
                Classifier classifier = loadModel(modelKey);
                if (classifier != null) {
                    trainedModels.put(modelKey, classifier);
                    System.out.println("📂 Loaded model: " + modelKey);
                }
            }
        }

        System.out.println("🔄 Loaded " + trainedModels.size() + " models from disk");
    }

    /**
     * Get model statistics
     */
    public Map<String, Object> getModelStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalModels", trainedModels.size());
        stats.put("modelAccuracies", new HashMap<>(modelAccuracies));
        stats.put("availableModels", new ArrayList<>(trainedModels.keySet()));

        Map<String, String[]> featureMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : modelFeatures.entrySet()) {
            featureMap.put(entry.getKey(), entry.getValue());
        }
        stats.put("modelFeatures", featureMap);

        return stats;
    }

    /**
     * Retrain specific model with new data
     */
    public boolean retrainModel(String modelKey, List<HealthData> newData) {
        try {
            if (!modelFeatures.containsKey(modelKey)) {
                System.err.println("❌ Model " + modelKey + " not found for retraining");
                return false;
            }

            String[] features = modelFeatures.get(modelKey);
            List<HealthData> filteredData = filterDataByFeatures(newData, features);

            if (filteredData.size() < 5) {
                System.err.println("❌ Insufficient data for retraining model: " + modelKey);
                return false;
            }

            Instances dataset = createWekaDataset(filteredData, features, modelKey);
            Classifier classifier = trainAndSelectBestClassifier(dataset, modelKey);

            if (classifier != null) {
                trainedModels.put(modelKey, classifier);
                modelDatasets.put(modelKey, dataset);
                saveModel(classifier, modelKey);
                System.out.println("✅ Successfully retrained model: " + modelKey);
                return true;
            }

        } catch (Exception e) {
            System.err.println("❌ Error retraining model " + modelKey + ": " + e.getMessage());
        }

        return false;
    }

    /**
     * Create ensemble prediction combining multiple models
     */
    public Map<String, Object> ensemblePredict(HealthData inputData) throws Exception {
        List<Map<String, Object>> predictions = new ArrayList<>();
        Map<String, Double> combinedProbabilities = new HashMap<>();
        Map<String, Integer> voteCounts = new HashMap<>();

        // Get predictions from all applicable models
        for (String modelKey : trainedModels.keySet()) {
            try {
                String[] modelFeatures = this.modelFeatures.get(modelKey);
                if (hasAllRequiredFeatures(inputData, modelFeatures)) {
                    Map<String, Object> prediction = predict(inputData, null);
                    predictions.add(prediction);

                    String predictedClass = (String) prediction.get("riskLevel");
                    voteCounts.put(predictedClass, voteCounts.getOrDefault(predictedClass, 0) + 1);

                    @SuppressWarnings("unchecked")
                    Map<String, Double> probs = (Map<String, Double>) prediction.get("probabilities");
                    for (Map.Entry<String, Double> entry : probs.entrySet()) {
                        combinedProbabilities.put(entry.getKey(),
                                combinedProbabilities.getOrDefault(entry.getKey(), 0.0) + entry.getValue());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error getting prediction from model " + modelKey + ": " + e.getMessage());
            }
        }

        if (predictions.isEmpty()) {
            throw new RuntimeException("No models available for the given input features");
        }

        // Normalize combined probabilities
        double totalWeight = predictions.size();
        for (Map.Entry<String, Double> entry : combinedProbabilities.entrySet()) {
            entry.setValue(entry.getValue() / totalWeight);
        }

        // Determine final prediction (majority vote with probability weighting)
        String finalPrediction = combinedProbabilities.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("UNKNOWN");

        double confidence = combinedProbabilities.getOrDefault(finalPrediction, 0.0);

        Map<String, Object> result = new HashMap<>();
        result.put("riskLevel", finalPrediction);
        result.put("confidence", confidence);
        result.put("modelUsed", "ensemble_" + predictions.size() + "_models");
        result.put("features", "combined_features");
        result.put("probabilities", combinedProbabilities);
        result.put("individualPredictions", predictions);
        result.put("voteCounts", voteCounts);

        return result;
    }

    private boolean hasAllRequiredFeatures(HealthData inputData, String[] requiredFeatures) {
        for (String feature : requiredFeatures) {
            if (!hasFeature(inputData, feature)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get model recommendations based on available features
     */
    public List<String> getRecommendedModels(HealthData inputData) {
        String[] availableFeatures = inputData.getAvailableFeatures();
        List<String> recommendations = new ArrayList<>();

        // Sort models by accuracy and feature match
        trainedModels.keySet().stream()
                .filter(modelKey -> {
                    String[] modelFeats = modelFeatures.get(modelKey);
                    return hasAllRequiredFeatures(inputData, modelFeats);
                })
                .sorted((m1, m2) -> {
                    double acc1 = modelAccuracies.getOrDefault(m1, 0.0);
                    double acc2 = modelAccuracies.getOrDefault(m2, 0.0);
                    return Double.compare(acc2, acc1); // Sort by accuracy descending
                })
                .forEach(recommendations::add);

        return recommendations;
    }

    /**
     * Validate model performance
     */
    public Map<String, Object> validateModel(String modelKey, List<HealthData> testData) {
        Map<String, Object> validation = new HashMap<>();

        if (!trainedModels.containsKey(modelKey)) {
            validation.put("error", "Model not found: " + modelKey);
            return validation;
        }

        try {
            Classifier classifier = trainedModels.get(modelKey);
            String[] features = modelFeatures.get(modelKey);

            List<HealthData> filteredTestData = filterDataByFeatures(testData, features);
            if (filteredTestData.isEmpty()) {
                validation.put("error", "No suitable test data for model: " + modelKey);
                return validation;
            }

            Instances testDataset = createWekaDataset(filteredTestData, features, modelKey + "_test");

            weka.classifiers.Evaluation eval = new weka.classifiers.Evaluation(testDataset);
            eval.evaluateModel(classifier, testDataset);

            validation.put("accuracy", eval.pctCorrect() / 100.0);
            validation.put("precision", eval.weightedPrecision());
            validation.put("recall", eval.weightedRecall());
            validation.put("fMeasure", eval.weightedFMeasure());
            validation.put("testSamples", filteredTestData.size());
            validation.put("confusionMatrix", eval.confusionMatrix());

        } catch (Exception e) {
            validation.put("error", "Validation failed: " + e.getMessage());
        }

        return validation;
    }
}
