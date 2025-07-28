package com.bsse_1401.pregnancy_risk_detector.service;

import com.bsse_1401.pregnancy_risk_detector.ai.ModelManager;
import com.bsse_1401.pregnancy_risk_detector.model.HealthData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

@Service
public class PredictionService {

    @Autowired
    private ModelManager modelManager;

    @Autowired
    private DataPreprocessingService dataPreprocessingService;

    private boolean modelsInitialized = false;

    @PostConstruct
    public void initialize() {
        try {
            // Load existing models if available
            modelManager.loadAllModels();

            // Train models with sample data if no models exist
            if (modelManager.getModelStatistics().get("totalModels").equals(0)) {
                System.out.println("🔄 No trained models found. Training with sample data...");
                initializeWithSampleData();
            }

            modelsInitialized = true;
            System.out.println("✅ Prediction service initialized successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error initializing prediction service: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initialize models with sample data if no real data is available
     */
    private void initializeWithSampleData() {
        try {
            List<HealthData> sampleData = generateSampleData();
            modelManager.trainModels(sampleData);
        } catch (Exception e) {
            System.err.println("❌ Error creating sample data: " + e.getMessage());
        }
    }

    /**
     * Generate sample training data for initial model training
     */
    private List<HealthData> generateSampleData() {
        List<HealthData> sampleData = new ArrayList<>();
        Random random = new Random(42);

        // Generate 1500 sample records
        for (int i = 0; i < 1500; i++) {
            HealthData data = new HealthData();

            // Generate realistic health data
            data.setAge((double) (18 + random.nextInt(27))); // 18-45 years
            data.setSystolicBP((double) (90 + random.nextInt(70))); // 90-160 mmHg
            data.setDiastolicBP((double) (50 + random.nextInt(50))); // 50-100 mmHg
            data.setBloodSugar(4.0 + random.nextDouble() * 16.0); // 4.0-20.0 mmol/L
            data.setBodyTemp(96.0 + random.nextDouble() * 6.0); // 96-102°F
            data.setBmi(16.0 + random.nextDouble() * 24.0); // 16-40 BMI
            data.setPreviousComplications(random.nextInt(2)); // 0 or 1
            data.setPreexistingDiabetes(random.nextInt(2)); // 0 or 1
            data.setGestationalDiabetes(random.nextInt(2)); // 0 or 1
            data.setMentalHealth(random.nextInt(2)); // 0 or 1
            data.setHeartRate((double) (60 + random.nextInt(60))); // 60-120 bpm

            // Determine risk level based on conditions
            String riskLevel = determineRiskLevel(data, random);
            data.setRiskLevel(riskLevel);

            sampleData.add(data);
        }

        return sampleData;
    }

    private String determineRiskLevel(HealthData data, Random random) {
        int riskScore = 0;

        // Age factors
        if (data.getAge() > 35 || data.getAge() < 20) riskScore += 2;

        // Blood pressure factors
        if (data.getSystolicBP() > 140 || data.getDiastolicBP() > 90) riskScore += 3;
        if (data.getSystolicBP() > 160 || data.getDiastolicBP() > 100) riskScore += 2;

        // Blood sugar factors
        if (data.getBloodSugar() > 11.0) riskScore += 3;
        if (data.getBloodSugar() > 15.0) riskScore += 2;

        // Temperature factors
        if (data.getBodyTemp() > 101 || data.getBodyTemp() < 97) riskScore += 2;

        // BMI factors
        if (data.getBmi() > 30 || data.getBmi() < 18.5) riskScore += 1;

        // Previous conditions
        if (data.getPreviousComplications() == 1) riskScore += 2;
        if (data.getPreexistingDiabetes() == 1) riskScore += 2;
        if (data.getGestationalDiabetes() == 1) riskScore += 1;
        if (data.getMentalHealth() == 1) riskScore += 1;

        // Heart rate factors
        if (data.getHeartRate() > 100 || data.getHeartRate() < 60) riskScore += 1;

        // Add some randomness to avoid perfect correlations
        riskScore += random.nextInt(3) - 1; // -1, 0, or 1

        // Determine risk level
        if (riskScore >= 6) return "HIGH";
        else if (riskScore >= 3) return "MEDIUM";
        else return "LOW";
    }

    /**
     * Make pregnancy risk prediction
     */
    public Map<String, Object> predictRisk(HealthData inputData, String preferredAlgorithm) {
        System.out.println("🟢 [predictRisk] Starting prediction process...");
        System.out.println("🔍 Preferred algorithm: " + preferredAlgorithm);

        try {
            if (!modelsInitialized) {
                throw new RuntimeException("Models are not initialized yet. Please wait for initialization to complete.");
            }

            if (inputData == null) {
                throw new IllegalArgumentException("Input data cannot be null");
            }

            String[] availableFeatures = inputData.getAvailableFeatures();
            System.out.println("📊 Input features received: " + Arrays.toString(availableFeatures));

            if (availableFeatures.length == 0) {
                throw new IllegalArgumentException("No valid features provided in input data");
            }

            // Normalize input data
            HealthData normalizedData = dataPreprocessingService.normalizeInputData(inputData);
            System.out.println("✅ Input data normalized successfully");

            // Make prediction
            Map<String, Object> prediction;
            if ("ensemble".equals(preferredAlgorithm)) {
                System.out.println("🤖 Using ENSEMBLE method for prediction");
                prediction = modelManager.ensemblePredict(normalizedData);
            } else {
                System.out.println("🤖 Using " + preferredAlgorithm + " model for prediction");
                prediction = modelManager.predict(normalizedData, preferredAlgorithm);
            }

            // Post-processing
            prediction.put("inputFeatures", Arrays.asList(availableFeatures));
            prediction.put("timestamp", new Date());
            prediction.put("status", "success");

            // Interpretation
            String riskLevel = (String) prediction.get("riskLevel");
            prediction.put("interpretation", getRiskInterpretation(riskLevel));
            prediction.put("recommendations", getRiskRecommendations(riskLevel));

            System.out.println("✅ Prediction complete. Risk Level: " + riskLevel);
            System.out.println("📦 Prediction result: " + prediction);

            return prediction;

        } catch (Exception e) {
            System.err.println("❌ Error in predictRisk: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", new Date());
            return errorResponse;
        }
    }


    private String getRiskInterpretation(String riskLevel) {
        switch (riskLevel) {
            case "HIGH":
                return "High risk detected. Immediate medical attention recommended. This indicates potential complications that require urgent healthcare intervention.";
            case "MEDIUM":
                return "Moderate risk identified. Regular monitoring and healthcare consultation advised. Some risk factors present that should be addressed.";
            case "LOW":
                return "Low risk assessment. Continue regular prenatal care and maintain healthy lifestyle practices.";
            default:
                return "Risk assessment completed. Please consult with healthcare provider for personalized advice.";
        }
    }

    private List<String> getRiskRecommendations(String riskLevel) {
        List<String> recommendations = new ArrayList<>();

        switch (riskLevel) {
            case "HIGH":
                recommendations.add("Seek immediate medical consultation");
                recommendations.add("Monitor blood pressure and blood sugar regularly");
                recommendations.add("Follow strict dietary and exercise guidelines");
                recommendations.add("Consider hospitalization if symptoms worsen");
                recommendations.add("Frequent prenatal check-ups required");
                break;
            case "MEDIUM":
                recommendations.add("Schedule regular healthcare appointments");
                recommendations.add("Monitor vital signs weekly");
                recommendations.add("Maintain balanced diet and moderate exercise");
                recommendations.add("Take prescribed medications as directed");
                recommendations.add("Watch for warning signs and symptoms");
                break;
            case "LOW":
                recommendations.add("Continue regular prenatal care");
                recommendations.add("Maintain healthy lifestyle");
                recommendations.add("Take prenatal vitamins as recommended");
                recommendations.add("Stay hydrated and get adequate rest");
                recommendations.add("Monitor for any changes in symptoms");
                break;
            default:
                recommendations.add("Consult healthcare provider for personalized advice");
                break;
        }

        return recommendations;
    }

    /**
     * Get model information and statistics
     */
    public Map<String, Object> getModelInfo() {
        try {
            Map<String, Object> info = modelManager.getModelStatistics();
            info.put("serviceStatus", modelsInitialized ? "initialized" : "initializing");
            info.put("timestamp", new Date());
            return info;
        } catch (Exception e) {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            errorInfo.put("serviceStatus", "error");
            return errorInfo;
        }
    }

    /**
     * Get model recommendations for given input
     */
    public List<String> getModelRecommendations(HealthData inputData) {
        if (!modelsInitialized) {
            return Arrays.asList("Models not initialized");
        }

        try {
            return modelManager.getRecommendedModels(inputData);
        } catch (Exception e) {
            return Arrays.asList("Error getting recommendations: " + e.getMessage());
        }
    }

    /**
     * Retrain models with new data
     */
    public Map<String, Object> retrainModels(List<HealthData> newTrainingData) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (newTrainingData == null || newTrainingData.isEmpty()) {
                result.put("status", "error");
                result.put("message", "No training data provided");
                return result;
            }

            // Preprocess the new data
            List<HealthData> processedData = new ArrayList<>();
            for (HealthData data : newTrainingData) {
                if (data != null && data.getRiskLevel() != null) {
                    processedData.add(data);
                }
            }

            if (processedData.size() < 10) {
                result.put("status", "error");
                result.put("message", "Insufficient training data. At least 10 samples required.");
                return result;
            }

            // Retrain models
            modelManager.trainModels(processedData);

            result.put("status", "success");
            result.put("message", "Models retrained successfully");
            result.put("trainingSamples", processedData.size());
            result.put("timestamp", new Date());
            result.put("modelStats", modelManager.getModelStatistics());

        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Error retraining models: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Validate prediction accuracy with test data
     */
    public Map<String, Object> validateModels(List<HealthData> testData) {
        Map<String, Object> validation = new HashMap<>();

        try {
            if (!modelsInitialized) {
                validation.put("error", "Models not initialized");
                return validation;
            }

            Map<String, Object> modelStats = modelManager.getModelStatistics();
            @SuppressWarnings("unchecked")
            List<String> availableModels = (List<String>) modelStats.get("availableModels");

            Map<String, Object> modelValidations = new HashMap<>();

            for (String modelKey : availableModels) {
                Map<String, Object> modelValidation = modelManager.validateModel(modelKey, testData);
                modelValidations.put(modelKey, modelValidation);
            }

            validation.put("status", "success");
            validation.put("modelValidations", modelValidations);
            validation.put("testSamples", testData.size());
            validation.put("timestamp", new Date());

        } catch (Exception e) {
            validation.put("status", "error");
            validation.put("message", "Error validating models: " + e.getMessage());
        }

        return validation;
    }

    /**
     * Check if service is ready to make predictions
     */
    public boolean isReady() {
        return modelsInitialized && modelManager.getModelStatistics().get("totalModels") != null &&
                (Integer) modelManager.getModelStatistics().get("totalModels") > 0;
    }
}
