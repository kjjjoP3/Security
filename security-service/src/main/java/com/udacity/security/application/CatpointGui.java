package com.udacity.security.application;

import com.udacity.security.data.PretendDatabaseSecurityRepositoryImpl;
import com.udacity.security.data.SecurityRepository;
import com.udacity.image.service.FakeImageService;
import com.udacity.security.service.SecurityService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * This is the primary JFrame for the application that contains all the
 * top-level JPanels.
 *
 * This class constructs all necessary dependencies and provides them to other
 * components.
 */
public class CatpointGui extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SecurityRepository repository = new PretendDatabaseSecurityRepositoryImpl();
	private FakeImageService imgService = new FakeImageService();
	private SecurityService secService = new SecurityService(repository, imgService);
	private DisplayPanel dispPanel = new DisplayPanel(secService);
	private SensorPanel sensPanel = new SensorPanel(secService);
	private ControlPanel ctrlPanel = new ControlPanel(secService, sensPanel);
	private ImagePanel imgPanel = new ImagePanel(secService);

	public CatpointGui() {
		setLocation(100, 100);
		setSize(600, 850);
		setTitle("Very Secure App");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel mainContainer = new JPanel();
		mainContainer.setLayout(new MigLayout());
		mainContainer.add(dispPanel, "wrap");
		mainContainer.add(imgPanel, "wrap");
		mainContainer.add(ctrlPanel, "wrap");
		mainContainer.add(sensPanel);

		getContentPane().add(mainContainer);
	}
}
