package imspar;

import java.util.ArrayList;
import java.util.ListIterator;

import org.openimaj.feature.FloatFV;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.alignment.ScalingAligner;
import org.openimaj.image.processing.face.detection.CLMDetectedFace;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.feature.LocalLBPHistogram;

public class ExtractLocalLBPHistogram extends ReadImages implements ExtractFeature {

	final static String trainPath = "resources/CACD_Training";
	static String featurePath = "resources/feature/LocalLBPHistogram.ser";
	
	final private LocalLBPHistogram.Extractor<DetectedFace> lbpHist = new LocalLBPHistogram.Extractor<DetectedFace>(
			new ScalingAligner<DetectedFace>());

	public static String getFeaturePath() {
		return featurePath;
	}

	public static void main(String ar[]) {
		TraverseDirectory tD = new TraverseDirectory(trainPath);
		ExtractLocalLBPHistogram lbpF = new ExtractLocalLBPHistogram();

		ArrayList<String> filePath = tD.getFilePath();
		// ArrayList<String> fileClass = tD.getFileClass();
		
		ListIterator<String> it = filePath.listIterator();
		while (it.hasNext()) {
			MBFImage fImg = lbpF.readImage(it.next().toString());
			DetectedFace face = lbpF.extractFace(fImg);
			FloatFV hist = lbpF.extractFeature(face);
			 lbpF.print(hist.toString());
		}
	}

	@Override
	public FloatFV extractFeature(DetectedFace face) {
		if (face == null) {
			return null;
		} else {
			LocalLBPHistogram feature = lbpHist.extractFeature(face);
			return feature.getFeatureVector();
		}
	}

	@Override
	public FloatFV extractFeature(CLMDetectedFace face) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FloatFV extractFeature(FloatFV object) {
		return object;
	}

}
