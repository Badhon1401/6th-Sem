package com.bsse_1401.pregnancy_risk_detector.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HealthData {

    @JsonProperty("age")
    private Double age;

    @JsonProperty("systolicBP")
    private Double systolicBP;

    @JsonProperty("diastolicBP")
    private Double diastolicBP;

    @JsonProperty("bloodSugar")
    private Double bloodSugar;

    @JsonProperty("bodyTemp")
    private Double bodyTemp;

    @JsonProperty("bmi")
    private Double bmi;

    @JsonProperty("previousComplications")
    private Integer previousComplications;

    @JsonProperty("preexistingDiabetes")
    private Integer preexistingDiabetes;

    @JsonProperty("gestationalDiabetes")
    private Integer gestationalDiabetes;

    @JsonProperty("mentalHealth")
    private Integer mentalHealth;

    @JsonProperty("heartRate")
    private Double heartRate;

    @JsonProperty("riskLevel")
    private String riskLevel;

    // Default constructor
    public HealthData() {}

    // Constructor with all fields
    public HealthData(Double age, Double systolicBP, Double diastolicBP,
                      Double bloodSugar, Double bodyTemp, Double bmi,
                      Integer previousComplications, Integer preexistingDiabetes,
                      Integer gestationalDiabetes, Integer mentalHealth,
                      Double heartRate, String riskLevel) {
        this.age = age;
        this.systolicBP = systolicBP;
        this.diastolicBP = diastolicBP;
        this.bloodSugar = bloodSugar;
        this.bodyTemp = bodyTemp;
        this.bmi = bmi;
        this.previousComplications = previousComplications;
        this.preexistingDiabetes = preexistingDiabetes;
        this.gestationalDiabetes = gestationalDiabetes;
        this.mentalHealth = mentalHealth;
        this.heartRate = heartRate;
        this.riskLevel = riskLevel;
    }

    // Getters and setters
    public Double getAge() { return age; }
    public void setAge(Double age) { this.age = age; }

    public Double getSystolicBP() { return systolicBP; }
    public void setSystolicBP(Double systolicBP) { this.systolicBP = systolicBP; }

    public Double getDiastolicBP() { return diastolicBP; }
    public void setDiastolicBP(Double diastolicBP) { this.diastolicBP = diastolicBP; }

    public Double getBloodSugar() { return bloodSugar; }
    public void setBloodSugar(Double bloodSugar) { this.bloodSugar = bloodSugar; }

    public Double getBodyTemp() { return bodyTemp; }
    public void setBodyTemp(Double bodyTemp) { this.bodyTemp = bodyTemp; }

    public Double getBmi() { return bmi; }
    public void setBmi(Double bmi) { this.bmi = bmi; }

    public Integer getPreviousComplications() { return previousComplications; }
    public void setPreviousComplications(Integer previousComplications) {
        this.previousComplications = previousComplications;
    }

    public Integer getPreexistingDiabetes() { return preexistingDiabetes; }
    public void setPreexistingDiabetes(Integer preexistingDiabetes) {
        this.preexistingDiabetes = preexistingDiabetes;
    }

    public Integer getGestationalDiabetes() { return gestationalDiabetes; }
    public void setGestationalDiabetes(Integer gestationalDiabetes) {
        this.gestationalDiabetes = gestationalDiabetes;
    }

    public Integer getMentalHealth() { return mentalHealth; }
    public void setMentalHealth(Integer mentalHealth) { this.mentalHealth = mentalHealth; }

    public Double getHeartRate() { return heartRate; }
    public void setHeartRate(Double heartRate) { this.heartRate = heartRate; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    // Utility methods
    public String[] getAvailableFeatures() {
        java.util.List<String> features = new java.util.ArrayList<>();

        if (age != null) features.add("age");
        if (systolicBP != null) features.add("systolicBP");
        if (diastolicBP != null) features.add("diastolicBP");
        if (bloodSugar != null) features.add("bloodSugar");
        if (bodyTemp != null) features.add("bodyTemp");
        if (bmi != null) features.add("bmi");
        if (previousComplications != null) features.add("previousComplications");
        if (preexistingDiabetes != null) features.add("preexistingDiabetes");
        if (gestationalDiabetes != null) features.add("gestationalDiabetes");
        if (mentalHealth != null) features.add("mentalHealth");
        if (heartRate != null) features.add("heartRate");

        return features.toArray(new String[0]);
    }

    public double[] getFeatureVector() {
        java.util.List<Double> vector = new java.util.ArrayList<>();

        if (age != null) vector.add(age);
        if (systolicBP != null) vector.add(systolicBP);
        if (diastolicBP != null) vector.add(diastolicBP);
        if (bloodSugar != null) vector.add(bloodSugar);
        if (bodyTemp != null) vector.add(bodyTemp);
        if (bmi != null) vector.add(bmi);
        if (previousComplications != null) vector.add(previousComplications.doubleValue());
        if (preexistingDiabetes != null) vector.add(preexistingDiabetes.doubleValue());
        if (gestationalDiabetes != null) vector.add(gestationalDiabetes.doubleValue());
        if (mentalHealth != null) vector.add(mentalHealth.doubleValue());
        if (heartRate != null) vector.add(heartRate);

        return vector.stream().mapToDouble(Double::doubleValue).toArray();
    }

    @Override
    public String toString() {
        return "HealthData{" +
                "age=" + age +
                ", systolicBP=" + systolicBP +
                ", diastolicBP=" + diastolicBP +
                ", bloodSugar=" + bloodSugar +
                ", bodyTemp=" + bodyTemp +
                ", bmi=" + bmi +
                ", previousComplications=" + previousComplications +
                ", preexistingDiabetes=" + preexistingDiabetes +
                ", gestationalDiabetes=" + gestationalDiabetes +
                ", mentalHealth=" + mentalHealth +
                ", heartRate=" + heartRate +
                ", riskLevel='" + riskLevel + '\'' +
                '}';
    }
}

// PredictionRequest.java
class PredictionRequest {
    private HealthData healthData;
    private String preferredAlgorithm; // "ensemble", "random_forest", "neural_network", "svm"

    public PredictionRequest() {}

    public HealthData getHealthData() { return healthData; }
    public void setHealthData(HealthData healthData) { this.healthData = healthData; }

    public String getPreferredAlgorithm() { return preferredAlgorithm; }
    public void setPreferredAlgorithm(String preferredAlgorithm) { this.preferredAlgorithm = preferredAlgorithm; }
}

// PredictionResponse.java
class PredictionResponse {
    private String riskLevel;
    private double confidence;
    private String algorithmUsed;
    private String modelFeatures;
    private java.util.Map<String, Double> riskProbabilities;
    private String message;

    public PredictionResponse() {}

    public PredictionResponse(String riskLevel, double confidence, String algorithmUsed,
                              String modelFeatures, java.util.Map<String, Double> riskProbabilities, String message) {
        this.riskLevel = riskLevel;
        this.confidence = confidence;
        this.algorithmUsed = algorithmUsed;
        this.modelFeatures = modelFeatures;
        this.riskProbabilities = riskProbabilities;
        this.message = message;
    }

    // Getters and setters
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getAlgorithmUsed() { return algorithmUsed; }
    public void setAlgorithmUsed(String algorithmUsed) { this.algorithmUsed = algorithmUsed; }

    public String getModelFeatures() { return modelFeatures; }
    public void setModelFeatures(String modelFeatures) { this.modelFeatures = modelFeatures; }

    public java.util.Map<String, Double> getRiskProbabilities() { return riskProbabilities; }
    public void setRiskProbabilities(java.util.Map<String, Double> riskProbabilities) {
        this.riskProbabilities = riskProbabilities;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
