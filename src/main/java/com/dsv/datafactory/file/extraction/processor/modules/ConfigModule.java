package com.dsv.datafactory.file.extraction.processor.modules;

import com.google.cloud.vision.v1.Feature;
import com.dsv.datafactory.file.extraction.processor.Config;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import static com.dsv.datafactory.file.extraction.processor.util.ConfigurationLoader.getOrDefault;
import static com.dsv.datafactory.file.extraction.processor.util.ConfigurationLoader.getOrFail;

public class ConfigModule implements Module {

	@Override
	public void configure(Binder binder) {
	}

	@Provides
	@Singleton
	public Config provideConfig() {
		Config config = new Config();

		config.kafkaClientId = getOrFail("KAFKA_CLIENT_ID");
		config.kafkaGroupId = getOrFail("KAFKA_GROUP_ID");
		config.enableKafkaSSL = getOrDefault("ENABLE_KAFKA_SSL","true");
		config.enableRBAC = getOrDefault("ENABLE_KAFKA_RBAC","false");
		config.runGVInPararell = getOrDefault("RUN_GV_PARALLEL","false");

		config.imageExtractionMetadataTopic = getOrFail("IMAGE_EXTRACTION_METADATA_TOPIC");

		config.googleCredPath = getOrDefault("GOOGLE_APPLICATION_CREDENTIALS","credentials/google-cred.json");
		config.goodnessOfFit = getOrDefault("GOODNESS_OF_FIT","0.999");
		config.startNumberOfClasses = getOrDefault("START_CLASSES","");

		config.extractedDocumentTopic = getOrFail("EXTRACTED_DOCUMENTS_TOPIC");
		config.lineServiceUrl = getOrDefault("JENKS_URI","http://aif-jenks:8005/jenks/clustering");
		config.kafkaCommitIntervalMs = Integer.parseInt(getOrDefault("KAFKA_COMMIT_INTERVAL_MS", "200"));
		config.kakfaPollIntervalMs = Integer.parseInt(getOrDefault("KAFKA_POLL_INTERVAL_MS", "1800000"));
		config.kafkaRequestTimeoutMs = Integer.parseInt(getOrDefault("REQUEST_TIMEOUT_MS_CONFIG", "60000"));
		config.kafkaAutoOffsetReset = getOrDefault("KAFKA_AUTO_OFFSET_RESET", "latest");

		config.feature = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();

		config.kafkaBootstrapServers = getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");

		config.kafkaMaxRequestSize = getOrDefault("KAFKA_MAX_REQUEST_SIZE", "104857600");

		config.kafkaTruststorePath = getOrDefault("KAFKA_TRUSTSTORE_PATH", "");
		config.kafkaTruststoreFile = getOrDefault("KAFKA_TRUSTSTORE_FILE", "");
		config.kafkaTruststorePassword = getOrDefault("KAFKA_TRUSTSTORE_PASSWORD", "", true);
		config.kafkaSLLProtocol = getOrDefault("KAFKA_SSL_PROTOCOL", "TLSv1.3,TLSv1.2");
		config.kafkaSSLCipher = getOrDefault("KAFKA_SSL_CIPHER_SUITE", "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA");


		return config;
	}
}
