package com.udacity.security.application;

import com.udacity.security.data.ArmingStatus;
import com.udacity.security.service.SecurityService;
import com.udacity.security.service.StyleService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JPanel containing the buttons to manipulate arming status of the system.
 */
public class ControlPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SecurityService securityService;
	private SensorPanel sensorPanel;
	private Map<ArmingStatus, JButton> buttonMapping;

	public ControlPanel(SecurityService securityService, SensorPanel sensorPanel) {
		super();
		setLayout(new MigLayout());
		this.setSecurityService(securityService);
		this.setSensorPanel(sensorPanel);

		JLabel headerLabel = new JLabel("System Control");
		headerLabel.setFont(StyleService.HEADING_FONT);

		add(headerLabel, "span 3, wrap");

		// Create a mapping of each status type to a corresponding JButton
		buttonMapping = Arrays.stream(ArmingStatus.values())
				.collect(Collectors.toMap(status -> status, status -> new JButton(status.getDescription())));

		// Add an action listener to each button that applies its arming status and
		// recolors all the buttons
		buttonMapping.forEach((status, button) -> {
			button.addActionListener(e -> {
				securityService.setArmingStatus(status);
				buttonMapping.forEach((s, b) -> b.setBackground(s == status ? s.getColor() : null));
				sensorPanel.sensorStatusChanged();
			});
		});

		// Loop through the enum in order to add buttons in the defined order
		Arrays.stream(ArmingStatus.values()).forEach(status -> add(buttonMapping.get(status)));

		ArmingStatus currentStatus = securityService.getArmingStatus();
		buttonMapping.get(currentStatus).setBackground(currentStatus.getColor());
	}

	public SecurityService getSecurityService() {
		return securityService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public SensorPanel getSensorPanel() {
		return sensorPanel;
	}

	public void setSensorPanel(SensorPanel sensorPanel) {
		this.sensorPanel = sensorPanel;
	}
}
