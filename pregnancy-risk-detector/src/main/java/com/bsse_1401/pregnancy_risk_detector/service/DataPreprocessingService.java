package com.bsse_1401.pregnancy_risk_detector.service;

import com.bsse_1401.pregnancy_risk_detector.model.HealthData;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataPreprocessingService {

    private Map<String, Double> featureStats = new HashMap<>();
    private Map<String, Double> featureMeans = new HashMap<>();
    private Map<String, Double> featureStdDevs = new HashMap<>();

    /**
     * Load and preprocess CSV data from resources
     */
    public List<HealthData> loadAndPreprocessData(String csvFileName) throws IOException, CsvException {
        List<HealthData> healthDataList = new ArrayList<>();

        try {
            ClassPathResource resource = new ClassPathResource("data/" + csvFileName);
            InputStream inputStream = resource.getInputStream();
            CSVReader reader = new CSVReader(new InputStreamReader(inputStream));

            List<String[]> records = reader.readAll();

            if (records.isEmpty()) {
                throw new RuntimeException("CSV file is empty: " + csvFileName);
            }

            String[] headers = records.get(0);
            Map<String, Integer> headerIndexMap = createHeaderIndexMap(headers);

            // Process each record
            for (int i = 1; i < records.size(); i++) {
                String[] record = records.get(i);
                if (record.length == 0) continue;

                try {
                    HealthData healthData = parseRecord(record, headerIndexMap);
                    if (healthData != null) {
                        healthDataList.add(healthData);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing record " + i + ": " + e.getMessage());
                }
            }

            reader.close();

        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + csvFileName);
            throw e;
        }

        // Calculate statistics for normalization
        calculateFeatureStatistics(healthDataList);

        // Handle missing values and normalize
        return preprocessDataList(healthDataList);
    }

    /**
     * Create mapping of header names to column indices
     */
    private Map<String, Integer> createHeaderIndexMap(String[] headers) {
        Map<String, Integer> map = new HashMap<>();

        for (int i = 0; i < headers.length; i++) {
            String cleanHeader = headers[i].trim().toLowerCase()
                    .replace(" ", "")
                    .replace("_", "")
                    .replace("-", "");

            // Map various header formats to standard names
            switch (cleanHeader) {
                case "age":
                    map.put("age", i);
                    break;
                case "systolicbp":
                case "systolic":
                case "sbp":
                    map.put("systolicBP", i);
                    break;
                case "diastolicbp":
                case "diastolic":
                case "dbp":
                    map.put("diastolicBP", i);
                    break;
                case "bs":
                case "bloodsugar":
                case "glucose":
                case "bloodglucose":
                    map.put("bloodSugar", i);
                    break;
                case "bodytemp":
                case "temperature":
                case "temp":
                    map.put("bodyTemp", i);
                    break;
                case "bmi":
                    map.put("bmi", i);
                    break;
                case "previouscomplications":
                case "complications":
                    map.put("previousComplications", i);
                    break;
                case "preexistingdiabetes":
                case "diabetes":
                    map.put("preexistingDiabetes", i);
                    break;
                case "gestationaldiabetes":
                case "gdiabetes":
                    map.put("gestationalDiabetes", i);
                    break;
                case "mentalhealth":
                case "mental":
                    map.put("mentalHealth", i);
                    break;
                case "heartrate":
                case "hr":
                case "pulse":
                    map.put("heartRate", i);
                    break;
                case "risklevel":
                case "risk":
                case "target":
                case "output":
                    map.put("riskLevel", i);
                    break;
            }
        }

        return map;
    }

    /**
     * Parse a single CSV record into HealthData object
     */
    private HealthData parseRecord(String[] record, Map<String, Integer> headerMap) {
        HealthData healthData = new HealthData();

        try {
            // Parse numeric fields with error handling
            if (headerMap.containsKey("age") && headerMap.get("age") < record.length) {
                healthData.setAge(parseDoubleValue(record[headerMap.get("age")]));
            }

            if (headerMap.containsKey("systolicBP") && headerMap.get("systolicBP") < record.length) {
                healthData.setSystolicBP(parseDoubleValue(record[headerMap.get("systolicBP")]));
            }

            if (headerMap.containsKey("diastolicBP") && headerMap.get("diastolicBP") < record.length) {
                healthData.setDiastolicBP(parseDoubleValue(record[headerMap.get("diastolicBP")]));
            }

            if (headerMap.containsKey("bloodSugar") && headerMap.get("bloodSugar") < record.length) {
                healthData.setBloodSugar(parseDoubleValue(record[headerMap.get("bloodSugar")]));
            }

            if (headerMap.containsKey("bodyTemp") && headerMap.get("bodyTemp") < record.length) {
                healthData.setBodyTemp(parseDoubleValue(record[headerMap.get("bodyTemp")]));
            }

            if (headerMap.containsKey("bmi") && headerMap.get("bmi") < record.length) {
                healthData.setBmi(parseDoubleValue(record[headerMap.get("bmi")]));
            }

            if (headerMap.containsKey("heartRate") && headerMap.get("heartRate") < record.length) {
                healthData.setHeartRate(parseDoubleValue(record[headerMap.get("heartRate")]));
            }

            // Parse integer fields (binary indicators)
            if (headerMap.containsKey("previousComplications") && headerMap.get("previousComplications") < record.length) {
                healthData.setPreviousComplications(parseIntegerValue(record[headerMap.get("previousComplications")]));
            }

            if (headerMap.containsKey("preexistingDiabetes") && headerMap.get("preexistingDiabetes") < record.length) {
                healthData.setPreexistingDiabetes(parseIntegerValue(record[headerMap.get("preexistingDiabetes")]));
            }

            if (headerMap.containsKey("gestationalDiabetes") && headerMap.get("gestationalDiabetes") < record.length) {
                healthData.setGestationalDiabetes(parseIntegerValue(record[headerMap.get("gestationalDiabetes")]));
            }

            if (headerMap.containsKey("mentalHealth") && headerMap.get("mentalHealth") < record.length) {
                healthData.setMentalHealth(parseIntegerValue(record[headerMap.get("mentalHealth")]));
            }

            // Parse risk level
            if (headerMap.containsKey("riskLevel") && headerMap.get("riskLevel") < record.length) {
                healthData.setRiskLevel(normalizeRiskLevel(record[headerMap.get("riskLevel")]));
            }

            return healthData;

        } catch (Exception e) {
            System.err.println("Error parsing record: " + Arrays.toString(record) + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Parse double value with error handling
     */
    private Double parseDoubleValue(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null")) {
            return null;
        }

        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parse integer value with error handling
     */
    private Integer parseIntegerValue(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null")) {
            return null;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            // Try parsing as double and converting to int
            try {
                return (int) Math.round(Double.parseDouble(value.trim()));
            } catch (NumberFormatException e2) {
                return null;
            }
        }
    }

    /**
     * Normalize risk level strings to standard format
     */
    private String normalizeRiskLevel(String riskLevel) {
        if (riskLevel == null || riskLevel.trim().isEmpty()) {
            return null;
        }

        String normalized = riskLevel.trim().toLowerCase();

        // Handle different risk level formats
        switch (normalized) {
            case "high":
            case "high risk":
            case "2":
                return "HIGH";
            case "medium":
            case "mid":
            case "mid risk":
            case "medium risk":
            case "1":
                return "MEDIUM";
            case "low":
            case "low risk":
            case "0":
                return "LOW";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * Calculate feature statistics for normalization
     */
    private void calculateFeatureStatistics(List<HealthData> dataList) {
        if (dataList.isEmpty()) return;

        Map<String, List<Double>> featureValues = new HashMap<>();

        // Collect all feature values
        for (HealthData data : dataList) {
            addToFeatureMap(featureValues, "age", data.getAge());
            addToFeatureMap(featureValues, "systolicBP", data.getSystolicBP());
            addToFeatureMap(featureValues, "diastolicBP", data.getDiastolicBP());
            addToFeatureMap(featureValues, "bloodSugar", data.getBloodSugar());
            addToFeatureMap(featureValues, "bodyTemp", data.getBodyTemp());
            addToFeatureMap(featureValues, "bmi", data.getBmi());
            addToFeatureMap(featureValues, "heartRate", data.getHeartRate());
        }

        // Calculate means and standard deviations
        for (Map.Entry<String, List<Double>> entry : featureValues.entrySet()) {
            String feature = entry.getKey();
            List<Double> values = entry.getValue();

            if (!values.isEmpty()) {
                double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                double stdDev = Math.sqrt(values.stream()
                        .mapToDouble(v -> Math.pow(v - mean, 2))
                        .average().orElse(0.0));

                featureMeans.put(feature, mean);
                featureStdDevs.put(feature, stdDev == 0 ? 1.0 : stdDev); // Avoid division by zero
            }
        }
    }

    private void addToFeatureMap(Map<String, List<Double>> featureValues, String featureName, Double value) {
        if (value != null) {
            featureValues.computeIfAbsent(featureName, k -> new ArrayList<>()).add(value);
        }
    }

    /**
     * Preprocess data list: handle missing values and normalize
     */
    private List<HealthData> preprocessDataList(List<HealthData> dataList) {
        return dataList.stream()
                .map(this::handleMissingValues)
                .map(this::normalizeData)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Handle missing values using mean imputation for numeric features
     */
    private HealthData handleMissingValues(HealthData data) {
        if (data.getAge() == null && featureMeans.containsKey("age")) {
            data.setAge(featureMeans.get("age"));
        }
        if (data.getSystolicBP() == null && featureMeans.containsKey("systolicBP")) {
            data.setSystolicBP(featureMeans.get("systolicBP"));
        }
        if (data.getDiastolicBP() == null && featureMeans.containsKey("diastolicBP")) {
            data.setDiastolicBP(featureMeans.get("diastolicBP"));
        }
        if (data.getBloodSugar() == null && featureMeans.containsKey("bloodSugar")) {
            data.setBloodSugar(featureMeans.get("bloodSugar"));
        }
        if (data.getBodyTemp() == null && featureMeans.containsKey("bodyTemp")) {
            data.setBodyTemp(featureMeans.get("bodyTemp"));
        }
        if (data.getBmi() == null && featureMeans.containsKey("bmi")) {
            data.setBmi(featureMeans.get("bmi"));
        }
        if (data.getHeartRate() == null && featureMeans.containsKey("heartRate")) {
            data.setHeartRate(featureMeans.get("heartRate"));
        }

        // For binary features, use mode (most common value) - defaulting to 0
        if (data.getPreviousComplications() == null) {
            data.setPreviousComplications(0);
        }
        if (data.getPreexistingDiabetes() == null) {
            data.setPreexistingDiabetes(0);
        }
        if (data.getGestationalDiabetes() == null) {
            data.setGestationalDiabetes(0);
        }
        if (data.getMentalHealth() == null) {
            data.setMentalHealth(0);
        }

        return data;
    }

    /**
     * Normalize numeric features using z-score normalization
     */
    private HealthData normalizeData(HealthData data) {
        if (data.getAge() != null && featureMeans.containsKey("age")) {
            double normalized = (data.getAge() - featureMeans.get("age")) / featureStdDevs.get("age");
            data.setAge(normalized);
        }
        if (data.getSystolicBP() != null && featureMeans.containsKey("systolicBP")) {
            double normalized = (data.getSystolicBP() - featureMeans.get("systolicBP")) / featureStdDevs.get("systolicBP");
            data.setSystolicBP(normalized);
        }
        if (data.getDiastolicBP() != null && featureMeans.containsKey("diastolicBP")) {
            double normalized = (data.getDiastolicBP() - featureMeans.get("diastolicBP")) / featureStdDevs.get("diastolicBP");
            data.setDiastolicBP(normalized);
        }
        if (data.getBloodSugar() != null && featureMeans.containsKey("bloodSugar")) {
            double normalized = (data.getBloodSugar() - featureMeans.get("bloodSugar")) / featureStdDevs.get("bloodSugar");
            data.setBloodSugar(normalized);
        }
        if (data.getBodyTemp() != null && featureMeans.containsKey("bodyTemp")) {
            double normalized = (data.getBodyTemp() - featureMeans.get("bodyTemp")) / featureStdDevs.get("bodyTemp");
            data.setBodyTemp(normalized);
        }
        if (data.getBmi() != null && featureMeans.containsKey("bmi")) {
            double normalized = (data.getBmi() - featureMeans.get("bmi")) / featureStdDevs.get("bmi");
            data.setBmi(normalized);
        }
        if (data.getHeartRate() != null && featureMeans.containsKey("heartRate")) {
            double normalized = (data.getHeartRate() - featureMeans.get("heartRate")) / featureStdDevs.get("heartRate");
            data.setHeartRate(normalized);
        }

        return data;
    }

    /**
     * Normalize input data using pre-calculated statistics
     */
    public HealthData normalizeInputData(HealthData inputData) {
        HealthData normalized = new HealthData();

        // Copy and normalize numeric features
        if (inputData.getAge() != null && featureMeans.containsKey("age")) {
            double norm = (inputData.getAge() - featureMeans.get("age")) / featureStdDevs.get("age");
            normalized.setAge(norm);
        }
        if (inputData.getSystolicBP() != null && featureMeans.containsKey("systolicBP")) {
            double norm = (inputData.getSystolicBP() - featureMeans.get("systolicBP")) / featureStdDevs.get("systolicBP");
            normalized.setSystolicBP(norm);
        }
        if (inputData.getDiastolicBP() != null && featureMeans.containsKey("diastolicBP")) {
            double norm = (inputData.getDiastolicBP() - featureMeans.get("diastolicBP")) / featureStdDevs.get("diastolicBP");
            normalized.setDiastolicBP(norm);
        }
        if (inputData.getBloodSugar() != null && featureMeans.containsKey("bloodSugar")) {
            double norm = (inputData.getBloodSugar() - featureMeans.get("bloodSugar")) / featureStdDevs.get("bloodSugar");
            normalized.setBloodSugar(norm);
        }
        if (inputData.getBodyTemp() != null && featureMeans.containsKey("bodyTemp")) {
            double norm = (inputData.getBodyTemp() - featureMeans.get("bodyTemp")) / featureStdDevs.get("bodyTemp");
            normalized.setBodyTemp(norm);
        }
        if (inputData.getBmi() != null && featureMeans.containsKey("bmi")) {
            double norm = (inputData.getBmi() - featureMeans.get("bmi")) / featureStdDevs.get("bmi");
            normalized.setBmi(norm);
        }
        if (inputData.getHeartRate() != null && featureMeans.containsKey("heartRate")) {
            double norm = (inputData.getHeartRate() - featureMeans.get("heartRate")) / featureStdDevs.get("heartRate");
            normalized.setHeartRate(norm);
        }

        // Copy binary features as-is
        normalized.setPreviousComplications(inputData.getPreviousComplications());
        normalized.setPreexistingDiabetes(inputData.getPreexistingDiabetes());
        normalized.setGestationalDiabetes(inputData.getGestationalDiabetes());
        normalized.setMentalHealth(inputData.getMentalHealth());

        return normalized;
    }

    /**
     * Get feature statistics for model training
     */
    public Map<String, Double> getFeatureMeans() {
        return new HashMap<>(featureMeans);
    }

    public Map<String, Double> getFeatureStdDevs() {
        return new HashMap<>(featureStdDevs);
    }

    /**
     * Split data into training and testing sets
     */
    public Map<String, List<HealthData>> splitData(List<HealthData> data, double trainRatio) {
        Collections.shuffle(data, new Random(42)); // Fixed seed for reproducibility

        int trainSize = (int) (data.size() * trainRatio);
        List<HealthData> trainData = data.subList(0, trainSize);
        List<HealthData> testData = data.subList(trainSize, data.size());

        Map<String, List<HealthData>> split = new HashMap<>();
        split.put("train", new ArrayList<>(trainData));
        split.put("test", new ArrayList<>(testData));

        return split;
    }

    /**
     * Generate synthetic data for augmentation when dataset is small
     */
    public List<HealthData> generateSyntheticData(List<HealthData> originalData, int numSamples) {
        List<HealthData> syntheticData = new ArrayList<>();
        Random random = new Random(42);

        if (originalData.isEmpty()) return syntheticData;

        for (int i = 0; i < numSamples; i++) {
            // Select two random samples for interpolation
            HealthData sample1 = originalData.get(random.nextInt(originalData.size()));
            HealthData sample2 = originalData.get(random.nextInt(originalData.size()));

            // Create synthetic sample by interpolating between two samples
            HealthData synthetic = interpolateHealthData(sample1, sample2, random.nextDouble());
            syntheticData.add(synthetic);
        }

        return syntheticData;
    }

    private HealthData interpolateHealthData(HealthData data1, HealthData data2, double alpha) {
        HealthData synthetic = new HealthData();

        // Interpolate numeric features
        if (data1.getAge() != null && data2.getAge() != null) {
            synthetic.setAge(alpha * data1.getAge() + (1 - alpha) * data2.getAge());
        }
        if (data1.getSystolicBP() != null && data2.getSystolicBP() != null) {
            synthetic.setSystolicBP(alpha * data1.getSystolicBP() + (1 - alpha) * data2.getSystolicBP());
        }
        if (data1.getDiastolicBP() != null && data2.getDiastolicBP() != null) {
            synthetic.setDiastolicBP(alpha * data1.getDiastolicBP() + (1 - alpha) * data2.getDiastolicBP());
        }
        if (data1.getBloodSugar() != null && data2.getBloodSugar() != null) {
            synthetic.setBloodSugar(alpha * data1.getBloodSugar() + (1 - alpha) * data2.getBloodSugar());
        }
        if (data1.getBodyTemp() != null && data2.getBodyTemp() != null) {
            synthetic.setBodyTemp(alpha * data1.getBodyTemp() + (1 - alpha) * data2.getBodyTemp());
        }
        if (data1.getBmi() != null && data2.getBmi() != null) {
            synthetic.setBmi(alpha * data1.getBmi() + (1 - alpha) * data2.getBmi());
        }
        if (data1.getHeartRate() != null && data2.getHeartRate() != null) {
            synthetic.setHeartRate(alpha * data1.getHeartRate() + (1 - alpha) * data2.getHeartRate());
        }

        // For binary features, choose randomly between the two values
        synthetic.setPreviousComplications(Math.random() < 0.5 ? data1.getPreviousComplications() : data2.getPreviousComplications());
        synthetic.setPreexistingDiabetes(Math.random() < 0.5 ? data1.getPreexistingDiabetes() : data2.getPreexistingDiabetes());
        synthetic.setGestationalDiabetes(Math.random() < 0.5 ? data1.getGestationalDiabetes() : data2.getGestationalDiabetes());
        synthetic.setMentalHealth(Math.random() < 0.5 ? data1.getMentalHealth() : data2.getMentalHealth());

        // Use the more severe risk level
        if (data1.getRiskLevel() != null && data2.getRiskLevel() != null) {
            synthetic.setRiskLevel(getMoreSevereRiskLevel(data1.getRiskLevel(), data2.getRiskLevel()));
        }

        return synthetic;
    }

    private String getMoreSevereRiskLevel(String risk1, String risk2) {
        if ("HIGH".equals(risk1) || "HIGH".equals(risk2)) return "HIGH";
        if ("MEDIUM".equals(risk1) || "MEDIUM".equals(risk2)) return "MEDIUM";
        return "LOW";
    }
}