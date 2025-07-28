package com.bsse_1401.pregnancy_risk_detector.controller;

import com.bsse_1401.pregnancy_risk_detector.model.HealthData;
import com.bsse_1401.pregnancy_risk_detector.service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Controller
@RequestMapping("/")
public class RiskPredictionController {

    @Autowired
    private PredictionService predictionService;

    /**
     * Home page - main UI
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("healthData", new HealthData());
        model.addAttribute("serviceReady", predictionService.isReady());
        return "index";
    }

    /**
     * Result page
     */
    @GetMapping("/result")
    public String result() {
        return "result";
    }

    /**
     * REST API - Make prediction
     */
    @PostMapping("/api/predict")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> predictRisk(@RequestBody Map<String, Object> request) {
        try {
            // Extract health data from request
            HealthData healthData = extractHealthData(request);
            String preferredAlgorithm = (String) request.getOrDefault("algorithm", "ensemble");

            // Validate input
            if (healthData.getAvailableFeatures().length == 0) {
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "No valid health data provided");
                return ResponseEntity.badRequest().body(error);
            }

            // Make prediction
            Map<String, Object> prediction = predictionService.predictRisk(healthData, preferredAlgorithm);

            return ResponseEntity.ok(prediction);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error processing prediction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Web form submission
     */
    @PostMapping("/predict")
    public String predictRiskForm(@ModelAttribute HealthData healthData,
                                  @RequestParam(value = "algorithm", defaultValue = "ensemble") String algorithm,
                                  Model model) {
        try {
            // ✅ Print all fields from HealthData
            System.out.println("----- Received Form Data -----");
            System.out.println("Age: " + healthData.getAge());
            System.out.println("BMI: " + healthData.getBmi());
            System.out.println("Systolic BP: " + healthData.getSystolicBP());
            System.out.println("Diastolic BP: " + healthData.getDiastolicBP());
            System.out.println("Heart Rate: " + healthData.getHeartRate());
            System.out.println("Blood Sugar: " + healthData.getBloodSugar());
            System.out.println("Body Temp: " + healthData.getBodyTemp());
            System.out.println("Previous Complications: " + healthData.getPreviousComplications());
            System.out.println("Pre-existing Diabetes: " + healthData.getPreexistingDiabetes());
            System.out.println("Gestational Diabetes: " + healthData.getGestationalDiabetes());
            System.out.println("Mental Health Concerns: " + healthData.getMentalHealth());

            // ✅ Print selected algorithm
            System.out.println("Algorithm: " + algorithm);
            System.out.println("--------------------------------");

            // Perform prediction
            Map<String, Object> prediction = predictionService.predictRisk(healthData, algorithm);

            model.addAttribute("prediction", prediction);
            model.addAttribute("healthData", healthData);
            model.addAttribute("success", true);

        } catch (Exception e) {
            model.addAttribute("error", "Error making prediction: " + e.getMessage());
            model.addAttribute("healthData", healthData);
            model.addAttribute("success", false);
        }

        return "result";
    }

    /**
     * Get model information
     */
    @GetMapping("/api/models")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getModelInfo() {
        Map<String, Object> info = predictionService.getModelInfo();
        return ResponseEntity.ok(info);
    }

    /**
     * Get model recommendations for input
     */
    @PostMapping("/api/models/recommend")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getModelRecommendations(@RequestBody Map<String, Object> request) {
        try {
            HealthData healthData = extractHealthData(request);
            List<String> recommendations = predictionService.getModelRecommendations(healthData);

            Map<String, Object> response = new HashMap<>();
            response.put("recommendations", recommendations);
            response.put("inputFeatures", Arrays.asList(healthData.getAvailableFeatures()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Upload CSV data for retraining
     */
    @PostMapping("/api/retrain")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> retrainModels(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "No file uploaded");
                return ResponseEntity.badRequest().body(error);
            }

            // TODO: Implement CSV parsing and model retraining
            // For now, return a placeholder response
            Map<String, Object> response = new HashMap<>();
            response.put("status", "info");
            response.put("message", "File upload received. CSV parsing and retraining will be implemented.");
            response.put("filename", file.getOriginalFilename());
            response.put("size", file.getSize());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error uploading file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/api/health")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", predictionService.isReady() ? "healthy" : "initializing");
        health.put("timestamp", new Date());
        health.put("service", "Pregnancy Risk Detection AI");
        health.put("version", "1.0.0");

        return ResponseEntity.ok(health);
    }

    /**
     * Get available algorithms
     */
    @GetMapping("/api/algorithms")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAvailableAlgorithms() {
        Map<String, Object> algorithms = new HashMap<>();

        List<Map<String, String>> algorithmList = new ArrayList<>();

        algorithmList.add(createAlgorithmInfo("ensemble", "Ensemble Model",
                "Combines multiple models for best accuracy"));
        algorithmList.add(createAlgorithmInfo("random_forest", "Random Forest",
                "Tree-based ensemble learning algorithm"));
        algorithmList.add(createAlgorithmInfo("neural_network", "Neural Network",
                "Deep learning multilayer perceptron"));
        algorithmList.add(createAlgorithmInfo("svm", "Support Vector Machine",
                "Statistical learning algorithm for classification"));

        algorithms.put("algorithms", algorithmList);
        algorithms.put("default", "ensemble");
        algorithms.put("recommended", "ensemble");

        return ResponseEntity.ok(algorithms);
    }

    private Map<String, String> createAlgorithmInfo(String id, String name, String description) {
        Map<String, String> info = new HashMap<>();
        info.put("id", id);
        info.put("name", name);
        info.put("description", description);
        return info;
    }

    /**
     * Batch prediction endpoint
     */
    @PostMapping("/api/predict/batch")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> batchPredict(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> batchData = (List<Map<String, Object>>) request.get("data");
            String algorithm = (String) request.getOrDefault("algorithm", "ensemble");

            if (batchData == null || batchData.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "No batch data provided");
                return ResponseEntity.badRequest().body(error);
            }

            List<Map<String, Object>> predictions = new ArrayList<>();
            List<Map<String, Object>> errors = new ArrayList<>();

            for (int i = 0; i < batchData.size(); i++) {
                try {
                    HealthData healthData = extractHealthData(batchData.get(i));
                    Map<String, Object> prediction = predictionService.predictRisk(healthData, algorithm);
                    prediction.put("index", i);
                    predictions.add(prediction);
                } catch (Exception e) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("index", i);
                    error.put("error", e.getMessage());
                    errors.add(error);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("predictions", predictions);
            response.put("errors", errors);
            response.put("totalProcessed", batchData.size());
            response.put("successCount", predictions.size());
            response.put("errorCount", errors.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Error processing batch prediction: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get prediction statistics
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            Map<String, Object> modelInfo = predictionService.getModelInfo();
            stats.put("modelStats", modelInfo);
            stats.put("serviceUptime", System.currentTimeMillis());
            stats.put("status", "active");

        } catch (Exception e) {
            stats.put("status", "error");
            stats.put("message", e.getMessage());
        }

        return ResponseEntity.ok(stats);
    }

    /**
     * API documentation endpoint
     */
    @GetMapping("/api/docs")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getApiDocs() {
        Map<String, Object> docs = new HashMap<>();

        List<Map<String, Object>> endpoints = new ArrayList<>();

        endpoints.add(createEndpointDoc("POST", "/api/predict",
                "Make a single risk prediction",
                "{ \"age\": 25, \"systolicBP\": 120, \"diastolicBP\": 80, \"algorithm\": \"ensemble\" }"));

        endpoints.add(createEndpointDoc("POST", "/api/predict/batch",
                "Make multiple risk predictions",
                "{ \"data\": [{ \"age\": 25, \"systolicBP\": 120 }], \"algorithm\": \"ensemble\" }"));

        endpoints.add(createEndpointDoc("GET", "/api/models",
                "Get model information and statistics", null));

        endpoints.add(createEndpointDoc("POST", "/api/models/recommend",
                "Get model recommendations for input features",
                "{ \"age\": 25, \"systolicBP\": 120 }"));

        endpoints.add(createEndpointDoc("GET", "/api/algorithms",
                "Get available prediction algorithms", null));

        endpoints.add(createEndpointDoc("GET", "/api/health",
                "Check service health status", null));

        endpoints.add(createEndpointDoc("GET", "/api/stats",
                "Get prediction and model statistics", null));

        docs.put("endpoints", endpoints);
        docs.put("version", "1.0.0");
        docs.put("description", "Pregnancy Health Risk Detection AI API");

        return ResponseEntity.ok(docs);
    }

    private Map<String, Object> createEndpointDoc(String method, String path, String description, String example) {
        Map<String, Object> endpoint = new HashMap<>();
        endpoint.put("method", method);
        endpoint.put("path", path);
        endpoint.put("description", description);
        if (example != null) {
            endpoint.put("example", example);
        }
        return endpoint;
    }

    /**
     * Extract HealthData from request map
     */
    private HealthData extractHealthData(Map<String, Object> request) {
        HealthData healthData = new HealthData();

        // Extract and set numeric fields
        if (request.containsKey("age")) {
            healthData.setAge(parseDouble(request.get("age")));
        }
        if (request.containsKey("systolicBP") || request.containsKey("systolic")) {
            Object value = request.getOrDefault("systolicBP", request.get("systolic"));
            healthData.setSystolicBP(parseDouble(value));
        }
        if (request.containsKey("diastolicBP") || request.containsKey("diastolic")) {
            Object value = request.getOrDefault("diastolicBP", request.get("diastolic"));
            healthData.setDiastolicBP(parseDouble(value));
        }
        if (request.containsKey("bloodSugar") || request.containsKey("bs") || request.containsKey("glucose")) {
            Object value = request.getOrDefault("bloodSugar",
                    request.getOrDefault("bs", request.get("glucose")));
            healthData.setBloodSugar(parseDouble(value));
        }
        if (request.containsKey("bodyTemp") || request.containsKey("temperature")) {
            Object value = request.getOrDefault("bodyTemp", request.get("temperature"));
            healthData.setBodyTemp(parseDouble(value));
        }
        if (request.containsKey("bmi")) {
            healthData.setBmi(parseDouble(request.get("bmi")));
        }
        if (request.containsKey("heartRate") || request.containsKey("pulse")) {
            Object value = request.getOrDefault("heartRate", request.get("pulse"));
            healthData.setHeartRate(parseDouble(value));
        }

        // Extract and set integer fields (binary indicators)
        if (request.containsKey("previousComplications")) {
            healthData.setPreviousComplications(parseInteger(request.get("previousComplications")));
        }
        if (request.containsKey("preexistingDiabetes") || request.containsKey("diabetes")) {
            Object value = request.getOrDefault("preexistingDiabetes", request.get("diabetes"));
            healthData.setPreexistingDiabetes(parseInteger(value));
        }
        if (request.containsKey("gestationalDiabetes")) {
            healthData.setGestationalDiabetes(parseInteger(request.get("gestationalDiabetes")));
        }
        if (request.containsKey("mentalHealth")) {
            healthData.setMentalHealth(parseInteger(request.get("mentalHealth")));
        }

        return healthData;
    }

    private Double parseDouble(Object value) {
        if (value == null) return null;

        try {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(Object value) {
        if (value == null) return null;

        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Error handler for the controller
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", e.getMessage());
        error.put("timestamp", new Date());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
