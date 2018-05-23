package com.github.smk7758.test_2018_05_08_opencv_a;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

public class Controller {
	public static int camera_number = 0;
	private Shower shower = null;

	@FXML
	ImageView iv;
	@FXML
	Button button_start, button_stop;

	public void initialize() {
	}

	@FXML
	public void onButtonStart() {
		System.out.println("start");
		if (shower == null) {
			shower = new Shower(iv);
			shower.setDaemon(true);
			shower.start();
		}
	}

	@FXML
	public void onButtonStop() {
		if (shower != null) shower.interrupt();
		System.out.println("Stop.(Controller)");
		System.exit(0);
	}
}
