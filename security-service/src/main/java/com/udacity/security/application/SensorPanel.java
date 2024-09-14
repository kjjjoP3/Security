package com.udacity.security.application;

import com.udacity.security.data.AlarmStatus;
import com.udacity.security.data.Sensor;
import com.udacity.security.data.SensorType;
import com.udacity.security.service.SecurityService;
import com.udacity.security.service.StyleService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * Panel that allows users to add sensors to their system. Sensors can be
 * manually set to "active" or "inactive" to test the system functionality.
 */
public class SensorPanel extends JPanel implements StatusListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final SecurityService securityService;

	private JLabel headerLabel = new JLabel("Sensor Management");
	private JLabel nameLabel = new JLabel("Name:");
	private JLabel typeLabel = new JLabel("Sensor Type:");
	private JTextField nameField = new JTextField();
	private JComboBox<SensorType> typeDropdown = new JComboBox<>(SensorType.values());
	private JButton addSensorButton = new JButton("Add New Sensor");

	private JPanel sensorDisplayPanel;
	private JPanel sensorInputPanel;

	public SensorPanel(SecurityService securityService) {
		super();
		setLayout(new MigLayout());
		this.securityService = securityService;

		headerLabel.setFont(StyleService.HEADING_FONT);
		addSensorButton.addActionListener(e -> addSensor(
				new Sensor(nameField.getText(), SensorType.valueOf(typeDropdown.getSelectedItem().toString()))));

		sensorInputPanel = createSensorInputPanel();
		sensorDisplayPanel = new JPanel();
		sensorDisplayPanel.setLayout(new MigLayout());

		refreshSensorList(sensorDisplayPanel);

		add(headerLabel, "wrap");
		add(sensorInputPanel, "span");
		add(sensorDisplayPanel, "span");
	}

	/**
	 * Creates the panel with the form for adding a new sensor
	 */
	private JPanel createSensorInputPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout());
		panel.add(nameLabel);
		panel.add(nameField, "width 50:100:200");
		panel.add(typeLabel);
		panel.add(typeDropdown, "wrap");
		panel.add(addSensorButton, "span 3");
		return panel;
	}

	/**
	 * Retrieves the current list of sensors and updates the provided panel to
	 * display them. Sensors will be shown in the order of their creation.
	 * 
	 * @param panel The Panel to populate with the current list of sensors
	 */
	private void refreshSensorList(JPanel panel) {
		panel.removeAll();
		securityService.getSensors().stream().sorted().forEach(sensor -> {
			JLabel sensorInfoLabel = new JLabel(String.format("%s(%s): %s", sensor.getName(), sensor.getSensorType(),
					(sensor.getActive() ? "Active" : "Inactive")));
			JButton toggleButton = new JButton((sensor.getActive() ? "Deactivate" : "Activate"));
			JButton removeButton = new JButton("Remove Sensor");

			toggleButton.addActionListener(e -> toggleSensorActivity(sensor, !sensor.getActive()));
			removeButton.addActionListener(e -> deleteSensor(sensor));

			// Specify some sizes for buttons
			panel.add(sensorInfoLabel, "width 300:300:300");
			panel.add(toggleButton, "width 100:100:100");
			panel.add(removeButton, "wrap");
		});

		repaint();
		revalidate();
	}

	/**
	 * Requests the securityService to change a sensor's activation status and then
	 * refreshes the current sensor list.
	 * 
	 * @param sensor   The sensor to update
	 * @param isActive The desired activation status for the sensor
	 */
	private void toggleSensorActivity(Sensor sensor, Boolean isActive) {
		securityService.changeSensorActivationStatus(sensor, isActive);
		refreshSensorList(sensorDisplayPanel);
	}

	/**
	 * Adds a sensor to the securityService and refreshes the sensor list.
	 * 
	 * @param sensor The sensor to add
	 */
	private void addSensor(Sensor sensor) {
		if (securityService.getSensors().size() < 4) {
			securityService.addSensor(sensor);
			refreshSensorList(sensorDisplayPanel);
		} else {
			JOptionPane.showMessageDialog(null,
					"To add more than 4 sensors, please subscribe to our Premium Membership!");
		}
	}

	/**
	 * Removes a sensor from the securityService and refreshes the sensor list.
	 * 
	 * @param sensor The sensor to remove
	 */
	private void deleteSensor(Sensor sensor) {
		securityService.removeSensor(sensor);
		refreshSensorList(sensorDisplayPanel);
	}

	@Override
	public void notify(AlarmStatus status) {
		// Implementation to be defined
	}

	@Override
	public void catDetected(boolean catDetected) {
		// Implementation to be defined
	}

	@Override
	public void sensorStatusChanged() {
		refreshSensorList(sensorDisplayPanel);
	}
}
