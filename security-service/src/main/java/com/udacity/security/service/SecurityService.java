package com.udacity.security.service;

import static com.udacity.security.data.AlarmStatus.ALARM;
import static com.udacity.security.data.AlarmStatus.NO_ALARM;
import static com.udacity.security.data.AlarmStatus.PENDING_ALARM;
import static com.udacity.security.data.ArmingStatus.ARMED_HOME;
import static com.udacity.security.data.ArmingStatus.DISARMED;

import com.udacity.image.service.ImageService;
import com.udacity.security.application.StatusListener;
import com.udacity.security.data.AlarmStatus;
import com.udacity.security.data.ArmingStatus;
import com.udacity.security.data.SecurityRepository;
import com.udacity.security.data.Sensor;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Service that receives information about changes to the security system.
 * Responsible for forwarding updates to the repository and making any decisions
 * about changing the system state.
 * <p>
 * This class contains most of the business logic for the system, and it is the
 * class you will be writing unit tests for.
 */
public class SecurityService {

	private final ImageService imageService;
	private final SecurityRepository securityRepository;
	private final Set<StatusListener> statusListeners = new HashSet<>();

	private boolean catDetectionEnabled = false;

	public SecurityService(SecurityRepository securityRepository, ImageService imageService) {
		this.securityRepository = securityRepository;
		this.imageService = imageService;
	}

	/**
	 * Updates the current arming status for the system. Changing the arming status
	 * may update both the alarm status.
	 *
	 * @param armingStatus
	 */
	public void setArmingStatus(ArmingStatus armingStatus) {
		if (catDetectionEnabled && armingStatus == ARMED_HOME) {
			setAlarmStatus(ALARM);
		}

		if (armingStatus == DISARMED) {
			setAlarmStatus(NO_ALARM);
		} else {
			ConcurrentSkipListSet<Sensor> sensors = new ConcurrentSkipListSet<>(getSensors());
			sensors.forEach(sensor -> changeSensorActivationStatus(sensor, false));
		}

		securityRepository.setArmingStatus(armingStatus);
		statusListeners.forEach(StatusListener::sensorStatusChanged);
	}

	/**
	 * Internal method that handles changes in alarm status based on cat detection.
	 *
	 * @param cat True if a cat is detected, otherwise false.
	 */
	private void handleCatDetection(Boolean cat) {
		catDetectionEnabled = cat;
		if (cat && getArmingStatus() == ARMED_HOME) {
			setAlarmStatus(AlarmStatus.ALARM);
		} else if (!cat && allSensorsInactive(false)) {
			setAlarmStatus(NO_ALARM);
		}

		statusListeners.forEach(listener -> listener.catDetected(cat));
	}

	/**
	 * Registers a StatusListener for updates from the SecurityService.
	 *
	 * @param statusListener
	 */
	public void addStatusListener(StatusListener statusListener) {
		statusListeners.add(statusListener);
	}

	public void removeStatusListener(StatusListener statusListener) {
		statusListeners.remove(statusListener);
	}

	/**
	 * Updates the alarm status of the system and notifies all listeners.
	 *
	 * @param status
	 */
	public void setAlarmStatus(AlarmStatus status) {
		securityRepository.setAlarmStatus(status);
		statusListeners.forEach(listener -> listener.notify(status));
	}

	/**
	 * Handles updates to alarm status when a sensor is activated.
	 */
	void handleSensorActivated() {
		ArmingStatus armingStatus = getArmingStatus();

		// If the system is disarmed, return without changing the alarm status.
		if (ArmingStatus.DISARMED.equals(armingStatus)) {
			return;
		}

		AlarmStatus currentStatus = getAlarmStatus();

		if (currentStatus == AlarmStatus.NO_ALARM) {
			setAlarmStatus(AlarmStatus.PENDING_ALARM);
		} else if (currentStatus == AlarmStatus.PENDING_ALARM) {
			setAlarmStatus(AlarmStatus.ALARM);
		}
	}

	/**
	 * Handles updates to alarm status when a sensor is deactivated.
	 */
	public void handleSensorDeactivated() {
		AlarmStatus currentStatus = getAlarmStatus();
		if (currentStatus == AlarmStatus.PENDING_ALARM) {
			setAlarmStatus(AlarmStatus.NO_ALARM);
		} else if (currentStatus == AlarmStatus.ALARM) {
			setAlarmStatus(AlarmStatus.PENDING_ALARM);
		}
	}

	/**
	 * Changes the activation status for the specified sensor and updates the alarm
	 * status if necessary.
	 *
	 * @param sensor
	 * @param active
	 */
	public void changeSensorActivationStatus(Sensor sensor, Boolean active) {
		if (!ALARM.equals(getAlarmStatus())) {
			if (active) {
				handleSensorActivated();
			} else if (sensor.getActive()) {
				handleSensorDeactivated();
			}
		}
		sensor.setActive(active);
		securityRepository.updateSensor(sensor);
	}

	/**
	 * Processes an image received from the camera for cat detection. The
	 * SecurityService uses its provided ImageService to analyze the image and
	 * update the alarm status as needed.
	 *
	 * @param currentCameraImage
	 */
	public void processImage(BufferedImage currentCameraImage) {
		handleCatDetection(imageService.imageContainsCat(currentCameraImage, 50.0f));
	}

	boolean allSensorsInactive(boolean state) {
		return getSensors().stream().allMatch(sensor -> sensor.getActive() == state);
	}

	public AlarmStatus getAlarmStatus() {
		return securityRepository.getAlarmStatus();
	}

	public Set<Sensor> getSensors() {
		return securityRepository.getSensors();
	}

	public void addSensor(Sensor sensor) {
		securityRepository.addSensor(sensor);
	}

	public void removeSensor(Sensor sensor) {
		securityRepository.removeSensor(sensor);
	}

	public ArmingStatus getArmingStatus() {
		return securityRepository.getArmingStatus();
	}
}
