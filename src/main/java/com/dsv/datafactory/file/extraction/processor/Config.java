package com.dsv.datafactory.file.extraction.processor;

import com.google.cloud.vision.v1.Feature;

public class Config {

	public String imageExtractionMetadataTopic;
	public Feature feature;

	public String extractedDocumentTopic;

	public String kafkaClientId;

	public String kafkaGroupId;

	public String kafkaAutoOffsetReset;

	public int kafkaCommitIntervalMs;

	public String enableKafkaSSL;

	public String kafkaBootstrapServers;

	public String kafkaMaxRequestSize;

	public String kafkaTruststorePath;

	public String kafkaTruststoreFile;

	public String kafkaTruststorePassword;

	public String kafkaSLLProtocol;

	public String kafkaSSLCipher;

	public int kakfaPollIntervalMs;

	public int kafkaRequestTimeoutMs;

    public String lineServiceUrl;

	public String startNumberOfClasses;

	public String enableRBAC;

	public String goodnessOfFit;

    public String googleCredPath;

    public String runGVInPararell;
}
