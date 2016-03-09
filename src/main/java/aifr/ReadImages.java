package aifr;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;

public class ReadImages {

	public void print(String data) {
		System.out.println(data);
	}

	public void display(FImage img) {
		DisplayUtilities.display(img);
	}

	public MBFImage readImage(String img) {
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

	public MBFImage readImage(FImage img) {
		MBFImage image = null;
		image = img.toRGB();
		return image;
	}

	@SuppressWarnings("finally")
	public DetectedFace extractFace(MBFImage img) {

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
}
