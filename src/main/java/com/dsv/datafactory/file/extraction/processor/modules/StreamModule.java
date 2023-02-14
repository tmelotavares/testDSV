package com.dsv.datafactory.file.extraction.processor.modules;

import com.dsv.datafactory.file.extraction.processor.Config;
import com.dsv.datafactory.file.extraction.processor.ExtractionStream;
import com.dsv.datafactory.file.extraction.processor.util.ConfigurationLoader;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import org.apache.kafka.clients.ClientDnsLookup;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import javax.inject.Singleton;
import java.util.Properties;

public class StreamModule implements Module {

	@Override
	public void configure(Binder binder) {
	}

	@Provides
	@Singleton
	public KafkaStreams provideStream(Config config, ExtractionStream extractionStream) {
		final StreamsBuilder builder = new StreamsBuilder();
		extractionStream.createFrom(builder);

		final Topology topology = builder.build();

		Properties props = new Properties();

		props.put(StreamsConfig.APPLICATION_ID_CONFIG, config.kafkaClientId);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.kafkaAutoOffsetReset);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, config.kafkaGroupId);

		props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, config.kafkaCommitIntervalMs);
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.kafkaBootstrapServers);
		props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, ConfigurationLoader.getOrDefault("KAFKA_PROCESSING_GUARANTEE", "at_least_once"));

		props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, config.kakfaPollIntervalMs);
		props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, config.kafkaRequestTimeoutMs);

		props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, config.kafkaMaxRequestSize);
		props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, config.kafkaMaxRequestSize);
		props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, config.kafkaMaxRequestSize);
		props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, config.kafkaMaxRequestSize);
		props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, ConfigurationLoader.getOrDefault("KAFKA_ISOLATION_LEVEL", "read_committed"));

		if (ConfigurationLoader.getOrDefault("ENABLE_KAFKA_CLOUD", "true").equals("true")) {

			props.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, ConfigurationLoader.getOrDefault("KAFKA_TRANSACTION_TIMEOUT_MS", "600000"));

			// confluent kafka
			props.put(ConsumerConfig.CLIENT_DNS_LOOKUP_CONFIG, ClientDnsLookup.USE_ALL_DNS_IPS.toString());

			props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
			props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
			props.put(SaslConfigs.SASL_JAAS_CONFIG, String.format("org.apache.kafka.common.security.plain.PlainLoginModule required username='%s' password='%s';",
					ConfigurationLoader.getOrDefault("KAFKA_RBAC_USER", "", true),
					ConfigurationLoader.getOrDefault("KAFKA_RBAC_PW", "", true)));

			props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "JKS");
			props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
			props.put(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, ConfigurationLoader.getOrDefault("KAFKA_SSL_PROTOCOL", "TLSv1.2"));
			props.put(SslConfigs.SSL_CIPHER_SUITES_CONFIG, ConfigurationLoader.getOrDefault(
					"KAFKA_SSL_CIPHER_SUITE",
					"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA"));

			props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, ConfigurationLoader.getOrDefault("KAFKA_TRUSTSTORE_PATH", "") + '/' + ConfigurationLoader.getOrDefault("KAFKA_TRUSTSTORE_FILE", ""));
			props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, ConfigurationLoader.getOrDefault("KAFKA_TRUSTSTORE_PASSWORD", "", true));

			props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, ConfigurationLoader.getOrDefault("KAFKA_TRUSTSTORE_PATH", "") + '/' + ConfigurationLoader.getOrDefault("KAFKA_TRUSTSTORE_FILE", ""));
			props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, ConfigurationLoader.getOrDefault("KAFKA_TRUSTSTORE_PASSWORD", "", true));
			return new KafkaStreams(topology, props);
		}

		if (config.enableKafkaSSL.equals("true") && config.enableRBAC.equals("false")) {
          
          	props.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, ConfigurationLoader.getOrDefault("KAFKA_TRANSACTION_TIMEOUT_MS", "600000"));
        	props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, ConfigurationLoader.getOrDefault("MAX_POLL_RECORDS_CONFIG", "1"));

			props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");

			props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, config.kafkaTruststorePath + '/' + config.kafkaTruststoreFile);
			props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, config.kafkaTruststorePassword);
			props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, config.kafkaTruststorePath + '/' + config.kafkaTruststoreFile);
			props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, config.kafkaTruststorePassword);
			props.put(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, config.kafkaSLLProtocol);
			props.put(SslConfigs.SSL_CIPHER_SUITES_CONFIG, config.kafkaSSLCipher);
			props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");

		}

		if (config.enableKafkaSSL.equals("true") && config.enableRBAC.equals("true")){
          	
          	props.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, ConfigurationLoader.getOrDefault("KAFKA_TRANSACTION_TIMEOUT_MS", "600000"));
        	props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, ConfigurationLoader.getOrDefault("MAX_POLL_RECORDS_CONFIG", "1"));
          
			props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
			props.put(SaslConfigs.SASL_MECHANISM,"PLAIN");
			props.put(SaslConfigs.SASL_JAAS_CONFIG, String.format("org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";",
					ConfigurationLoader.getOrDefault("KAFKA_RBAC_USER","",true),
					ConfigurationLoader.getOrDefault("KAFKA_RBAC_PW","",true)));

			props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, config.kafkaTruststorePath + '/' + config.kafkaTruststoreFile);
			props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, config.kafkaTruststorePassword);
			props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, config.kafkaTruststorePath + '/' + config.kafkaTruststoreFile);
			props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, config.kafkaTruststorePassword);
			props.put(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, config.kafkaSLLProtocol);
			props.put(SslConfigs.SSL_CIPHER_SUITES_CONFIG, config.kafkaSSLCipher);
			props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
		}
		return new KafkaStreams(topology, props);

	}

}
