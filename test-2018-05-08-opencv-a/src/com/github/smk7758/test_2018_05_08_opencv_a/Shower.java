package com.github.smk7758.test_2018_05_08_opencv_a;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.videoio.VideoCapture;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Shower extends Thread implements Closeable {
	VideoCapture vc = null;
	ImageView iv = null;
	boolean can_do = false;

	public Shower(ImageView iv) {
		this.iv = iv;
		Path movie_file_path = Paths.get("E:\\OpenCV_Things\\IMG_1846.MOV");
		if (!Files.exists(movie_file_path)) {
			System.err.println("Movie do not exist.");
			return;
		}
		vc = new VideoCapture();
		vc.open(movie_file_path.toString());
		can_do = true;
		// Size size = new Size(vc.get(3), vc.get(4));
		// double fps = vc.get(5);
	}

	@Override
	public void run() {
		if (!can_do) {
			System.err.println("Cannot start.");
			return;
		}
		Mat mat = new Mat();
		Mat element = Mat.ones(3, 3, CvType.CV_8UC1); // 追加 3×3の行列で要素はすべて1
		// dilate処理に必要な行列
		MatOfByte byteMat = new MatOfByte();
		Image image = null;
		while (vc.read(mat) && !mat.empty()) {
			List<MatOfPoint> contours = new ArrayList<>();

			// Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2GRAY, 4); // グレースケール化
			// Imgproc.threshold(mat, mat, 80, 255, Imgproc.THRESH_BINARY); // 2値化
			Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2HSV); // 色変換(?)
			Core.inRange(mat, new Scalar(140, 120, 120), new Scalar(255, 250, 250), mat); // 色抽出(赤色)
			// Core.bitwise_not(mat, mat); // 色反転
			// Imgproc.dilate(mat, mat, element, new Point(-1, -1), 3); //塗りつぶし

			// 境界線を点としてとる
			Imgproc.findContours(mat, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_TC89_L1);

			// 最大面積をみつける
			double max_area = 0, area = 0;
			int max_area_contour = -1;
			for (int j = 0; j < contours.size(); j++) {
				// area = contours.get(j).size().area(); //コッチはその面積のため違う
				area = Imgproc.contourArea(contours.get(j)); // 境界点の面積
				if (max_area < area) {
					max_area = area;
					max_area_contour = j;
				}
			}

			// 重心を取得(投げてるver, 厳密な計算ver)
			Moments moments = Imgproc.moments(contours.get(max_area_contour));
			double a_x = moments.get_m10() / moments.get_m00();
			double a_y = moments.get_m01() / moments.get_m00();

			// // 重心を取得(非厳密)
			// double count = contours.get(max_area_contour).size().area();// 最大面積
			// double x = 0, y = 0;
			// double[] place = { 0, 0 };
			// for (int k = 0; k < count; k++) {
			// place = contours.get(max_area_contour).get(k, 0);
			// x += place[0];
			// y += place[1];
			// // System.out.println("面積: " + count + ", X: " + x + ", Y: " + y
			// // + " Place[0]: " + place[0] + ", Place[1]: " + place[1]);
			// }
			// x /= count;
			// y /= count;

			// 円描画
			Imgproc.circle(mat, new Point(a_x, a_y), 50, new Scalar(255, 0, 0), 3, 4, 0);

			// System.out.println("X: " + a_x + ", Y: " + a_y + ", AreaCount: " + "count" + ", MaxAC: " + max_area_contour
			// + "AreaSize: " + contours.size());

			Imgcodecs.imencode(".bmp", mat, byteMat); // mat → image
			image = new Image(new ByteArrayInputStream(byteMat.toArray()));
			iv.setImage(image); // set ImageView
		}
	}

	@Override
	public void interrupt() {
		close();
		super.interrupt();
	}

	@Override
	public void close() {
		System.out.println("Close. (Shower)");
		if (vc != null && vc.isOpened()) vc.release();
	}
}
