package imspar;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.detection.CLMDetectedFace;
import org.openimaj.image.processing.face.detection.CLMFaceDetector;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;

public class ReadImages {
	/*
	 * private ArrayList<Path> trainImages = null;
	 * 
	 * ArrayList<Path> getTrainImages() { return trainImages; }
	 * 
	 * void setTrainImages(ArrayList<Path> trainImages) { this.trainImages =
	 * trainImages; }
	 * 
	 * private String trainDir = null;
	 * 
	 * String getTrainDir() { return trainDir; }
	 * 
	 * void setTrainDir(String trainDir) { this.trainDir = trainDir; }
	 * 
	 * 
	 * public void readDirectories(String root) { try { ArrayList<Path> aL = new
	 * ArrayList<Path>(); Files.find(Paths.get(root), 999, (p, bfa) ->
	 * bfa.isRegularFile()).forEach(aL::add); setTrainImages(aL); } catch
	 * (IOException e) { // TODO Auto-generated catch block print(
	 * "Directory Tracing Error"); e.printStackTrace(); } }
	 */

	public void print(String data) {
		System.out.println(data);
	}

	public void display(FImage img) {
		DisplayUtilities.display(img);
	}

	MBFImage readImage(String img) {
		MBFImage image = null;
		try {
			image = ImageUtilities.readMBF(new File(img));
			// print("Reading "+img.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return image;
	}

	MBFImage readImage(FImage img) {
		MBFImage image = null;
		image = img.toRGB();
		return image;
	}

	@SuppressWarnings("finally")
	DetectedFace extractFace(MBFImage img) {

		DetectedFace face = null;
		try {
			final FaceDetector<DetectedFace, FImage> fd = new HaarCascadeDetector(40);
			List<DetectedFace> faces = fd.detectFaces(Transforms.calculateIntensity(img));

			if (faces.size() > 0) {
				face = (DetectedFace) fd.detectFaces(Transforms.calculateIntensity(img)).get(0);
				// img.drawShape(face.getBounds(), RGBColour.BLACK);
				// DisplayUtilities.display(img);
			} else {
				// No Face Detected
				// print("No Face Found for " + img.toString());
			}

		} catch (Exception e) {
			print("Face Extraction Error");
			e.printStackTrace();
		} finally {
			return face;
		}
	}

	@SuppressWarnings("finally")
	CLMDetectedFace extractCLMFace(MBFImage img) {

		CLMDetectedFace face = null;
		try {
			final CLMFaceDetector fd = new CLMFaceDetector();
			List<CLMDetectedFace> faces = fd.detectFaces(Transforms.calculateIntensity(img));

			if (faces.size() > 0) {
				face = (CLMDetectedFace) fd.detectFaces(Transforms.calculateIntensity(img)).get(0);
			} else {
				// No Face Detected
				// print("No Face Found for " + img);
			}
		} catch (Exception e) {
			print("CLM Face Extraction Error");
			e.printStackTrace();
		} finally {
			return face;
		}
	}
}
