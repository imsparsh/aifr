package aifr;

import org.openimaj.feature.FloatFV;
import org.openimaj.image.processing.face.detection.DetectedFace;

public interface ExtractFeature {

	FloatFV extractFeature(DetectedFace face);

}
