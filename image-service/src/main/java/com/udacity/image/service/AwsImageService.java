package com.udacity.image.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;

/**
 * Service for recognizing images that can identify the presence of cats. To
 * operate, AWS credentials must be configured in config.properties.
 * Instructions for setup: 1. Log into the AWS console and access IAM. 2. Add a
 * new user with programmatic access and attach the
 * 'AmazonRekognitionFullAccess' policy. 3. Save the 'Access key ID' and 'Secret
 * access key'. 4. Create a config.properties file in the src/main/resources
 * directory with the following: aws.id=[your access key id] aws.secret=[your
 * Secret access key] aws.region=[choose an AWS region, e.g., us-east-2]
 */
public class AwsImageService implements ImageService {

	private Logger log = LoggerFactory.getLogger(AwsImageService.class);

	// It's recommended to maintain a single instance of client objects
	private static RekognitionClient rekognitionClient;

	public AwsImageService() {
		Properties props = new Properties();
		try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties")) {
			props.load(is);
		} catch (IOException ioe) {
			log.error("Failed to initialize AWS Rekognition; properties file not found", ioe);
			return;
		}

		String awsId = props.getProperty("aws.id");
		String awsSecret = props.getProperty("aws.secret");
		String awsRegion = props.getProperty("aws.region");

		AwsCredentials awsCredentials = AwsBasicCredentials.create(awsId, awsSecret);
		rekognitionClient = RekognitionClient.builder()
				.credentialsProvider(StaticCredentialsProvider.create(awsCredentials)).region(Region.of(awsRegion))
				.build();
	}

	/**
	 * Checks if the provided image contains a cat.
	 * 
	 * @param image                The image to be scanned
	 * @param confidenceThreshhold Minimum confidence level for detecting a cat. For
	 *                             example, 90.0f requires at least 90% confidence.
	 * @return true if the image contains a cat; false otherwise
	 */
	public boolean imageContainsCat(BufferedImage image, float confidenceThreshhold) {
		Image awsImage;
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			ImageIO.write(image, "jpg", os);
			awsImage = Image.builder().bytes(SdkBytes.fromByteArray(os.toByteArray())).build();
		} catch (IOException ioe) {
			log.error("Failed to create image byte array", ioe);
			return false;
		}

		DetectLabelsRequest detectLabelsRequest = DetectLabelsRequest.builder().image(awsImage)
				.minConfidence(confidenceThreshhold).build();
		DetectLabelsResponse response = rekognitionClient.detectLabels(detectLabelsRequest);
		logLabelsForFun(response);

		return response.labels().stream().anyMatch(label -> label.name().toLowerCase().contains("cat"));
	}

	private void logLabelsForFun(DetectLabelsResponse response) {
		log.info(response.labels().stream().map(label -> String.format("%s (%.1f%%)", label.name(), label.confidence()))
				.collect(Collectors.joining(", ")));
	}
}
