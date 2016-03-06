package imspar;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.feature.FloatFV;
import org.openimaj.image.FImage;
import org.openimaj.knn.approximate.FloatNearestNeighboursKDTree;
import org.openimaj.ml.annotation.Annotated;
import org.openimaj.util.pair.IntFloatPair;

/**
 * K-Nearest-Neighbour classifier using scaled-down images as the method of
 * feature abstraction.
 * 
 * @author imsparsh
 */
public class KNearestNeighbor implements ClassificationAlgorithm {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3375331135281529470L;
	protected VFSGroupDataset<FImage> trainingSet;
	protected Map<FloatFV, String> annotatedFeatures;

	final public static int DIMENSION = 16;
	final public static int K_DEFAULT = 5;

	private int K = 1;

	/**
	 * public static void main(String[] args) throws FileSystemException,
	 * FileNotFoundException { Utilities.runClassifier(new KNearestNeighbor(),
	 * "KNN", args); }
	 **/

	/**
	 * Create a K-Nearest-Neighbor classifier with the default K value
	 */
	public KNearestNeighbor() {
		this.K = K_DEFAULT;
	}

	/**
	 * Create a K-Nearest-Neighbor classifier
	 * 
	 * @param k
	 *            Number of neighbors to classify against
	 */
	public KNearestNeighbor(int k) {
		this.K = k;
	}

	/**
	 * Train the classifier
	 * 
	 * @param data
	 *            The training set
	 */
	@Override
	public void train(List<? extends Annotated<FImage, String>> data) {
		annotatedFeatures = new HashMap<FloatFV, String>();

		// Project each image down to a small-scale feature vector
		for (Annotated<FImage, String> a : data) {
			FloatFV feature = getFeatureVector(a.getObject());

			// skip if face is not detected in the data image
			if (!(feature == null))
				annotatedFeatures.put(feature, a.getAnnotations().toArray(new String[1])[0]);
		}
	}

	public void train(HashMapSerializable<FloatFV, String> data) {
		annotatedFeatures = (HashMap<FloatFV, String>) data;
	}

	/**
	 * Classify an image
	 * 
	 * @param image
	 *            The image
	 * @return The classification result
	 */
	@Override
	public ClassificationResult<String> classify(FImage image) {
		float[][] converted = new float[annotatedFeatures.size()][(int) Math.pow(KNearestNeighbor.DIMENSION, 2)];

		int i = 0;

		for (Map.Entry<FloatFV, String> entry : annotatedFeatures.entrySet()) {
			converted[i] = entry.getKey().values;
			i++;
		}

		FloatNearestNeighboursKDTree.Factory factory = new FloatNearestNeighboursKDTree.Factory();
		FloatNearestNeighboursKDTree nn = factory.create(converted);

		// Find the K nearest neighbours
		FloatFV feature = getFeatureVector(image);

		if (!(feature == null)) {
			List<IntFloatPair> neighbours = nn.searchKNN(feature.values, K);

			// Create a frequency table of neighbours
			Hashtable<String, Integer> frequency = new Hashtable<String, Integer>(K);
			// List the total distances to the neighbours
			Hashtable<String, Float> distance = new Hashtable<String, Float>(K);
			float totalDist = 0f;

			for (IntFloatPair neighbour : neighbours) {
				String clazz = annotatedFeatures.get(new FloatFV(converted[neighbour.first]));

				// Add 1 to the frequency count
				Integer currentFreq = frequency.get(clazz);
				frequency.put(clazz, currentFreq == null ? 1 : ++currentFreq);

				// Add distance to the cumulative distance
				Float currentDist = distance.get(clazz);
				distance.put(clazz, currentDist == null ? neighbour.second : currentDist + neighbour.second);
				totalDist += neighbour.second;
			}

			// Find the most likely class by taking the average class of the
			// nearest
			// neighbours
			// Checks both frequency and average distance from point
			String clazz = "unknown";
			int count = 0;
			float dist = 0f;

			for (Entry<String, Integer> e : frequency.entrySet()) {
				float aveDist = distance.get(e.getKey()) / e.getValue();

				if (count < e.getValue() || count == e.getValue() && dist >= aveDist) {
					clazz = e.getKey();
					count = e.getValue();
					dist = aveDist;
				}
			}

			// Weighting function
			float weight = ((((float) count) / neighbours.size()) + (dist / totalDist)) / 2;

			PrintableClassificationResult<String> result = new PrintableClassificationResult<String>(
					PrintableClassificationResult.BEST_RESULT);
			result.put(clazz, weight);
			return result;
		} else {
			return null;
		}
	}

	/**
	 * Convert an image into a {@link DIMENSION} by {@link DIMENSION} feature
	 * vector.
	 * 
	 * @param image
	 * @return Flattened pixels of resized image
	 */
	protected FloatFV getFeatureVector(FImage image) {
		FloatFV feature = null;
		ExtractLocalLBPHistogram extractor = new ExtractLocalLBPHistogram();
		feature = extractor.extractFeature(extractor.extractFace(extractor.readImage(image)));
		/*
		ExtractCLMShapeFeature extractor = new ExtractCLMShapeFeature();
		feature = extractor.extractFeature(extractor.extractCLMFace(extractor.readImage(image)));
		*/
		if (feature == null) {
			return null;
		} else {
			return feature;
		}
	}
}
