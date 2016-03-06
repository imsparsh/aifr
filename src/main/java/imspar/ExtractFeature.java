package imspar;

import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FloatFV;
import org.openimaj.image.processing.face.detection.CLMDetectedFace;
import org.openimaj.image.processing.face.detection.DetectedFace;

public interface ExtractFeature extends FeatureExtractor<FloatFV, FloatFV> {

	FloatFV extractFeature(DetectedFace face);

	FloatFV extractFeature(CLMDetectedFace face);

}
