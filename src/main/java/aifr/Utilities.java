package aifr;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FloatFV;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.feature.FImage2DoubleFV;
import org.openimaj.ml.annotation.ScoredAnnotation;

/**
 * Collection of generic utilities
 * 
 * @author imsparsh
 */
public class Utilities {

	final static String resultPath = Constant.resultPath;
	final static String testPath = Constant.testPath;

	/**
	 * Returns a new zero-meaned feature vector from an existing feature fector
	 * 
	 * @param feature
	 *            Existing feature vector
	 * @return Zero-meaned version of feature
	 */
	public static FloatFV zeroMean(DoubleFV feature) {
		FloatFV converted = new FloatFV(feature.values.length);

		double sum = 0;

		for (double value : feature.values) {
			sum += value;
		}

		double mean = sum / feature.values.length;

		for (int i = 0; i < feature.values.length; i++) {
			converted.values[i] = (float) (feature.values[i] - mean);
		}

		return converted;
	}

	/**
	 * Convienience method for zero-meaning an FImage
	 * 
	 * @param image
	 *            The image
	 * @return Zero mean feature vector
	 */
	public static FloatFV zeroMean(FImage image) {
		return Utilities.zeroMean(FImage2DoubleFV.INSTANCE.extractFeature(image));
	}

	/**
	 * Converts a list of scored annotations from an annotator to a
	 * classification result
	 * 
	 * @param annotations
	 *            The list of scored annotations
	 * @return The classification result
	 */
	public static <T> ClassificationResult<T> scoredListToResult(List<ScoredAnnotation<T>> annotations) {
		PrintableClassificationResult<T> result = new PrintableClassificationResult<T>();

		for (ScoredAnnotation<T> annotation : annotations) {
			result.put(annotation.annotation, annotation.confidence);
		}

		return result;
	}

	/**
	 * Convienience method for testing a classifier from the command line
	 * 
	 * @param classifier
	 *            The classifier
	 * @param classifierName
	 *            The name of the classifier for results filename
	 * @param classifierPath
	 *            The path of the classifier serialized file
	 */
	public static void runClassifier(ClassificationAlgorithm classifier, String classifierName, String classifierPath, Object extractor) {

		if (classifier == null || classifierName == null || classifierPath == null) {
			print("Invalid Parameters.");
			System.exit(1);
		}
		
		if(classifier instanceof SupportVectorMachine){
			((SupportVectorMachine) classifier).setnClass(new TraverseDirectory(Constant.trainPath).getClassCount());
		}

		PrintWriter writer = null;
		print("Opening results file.");
		try {
			writer = new PrintWriter(resultPath + classifierName + ".txt", "UTF-8");
		} catch (IOException e) {
			System.err.println("Can't open results file!");
			System.exit(1);
		}

		ManageClassifier mC = new ManageClassifier();
		mC.setClassifierPath(classifierPath);

		if (!new File(classifierPath).exists()) {
			ClassificationAlgorithm ca = null;
			if(!(extractor == null)){
				if(extractor instanceof ExtractLocalLBPHistogram)
					ca = mC.trainClassifier(classifier, ExtractLocalLBPHistogram.getFeaturePath());
			}
			else{
				ca = mC.trainClassifier(classifier);
			}
			if (mC.saveClassifier(ca)) {
				print("Classifier dumped : " + classifierPath);
			} else {
				print("Classifier dump failed.");
			}
		} else {
			print("Classifier already exists : " + classifier.getClass().getSimpleName());
		}

		print("Loading dataset...");
		ClassificationAlgorithm ca = mC.readClassifier();
		print("Completed.");

		File testingFile = new File(testPath);
		VFSListDataset<FImage> testingSet = null;

		try {
			testingSet = new VFSListDataset<FImage>(testingFile.getAbsolutePath(), ImageUtilities.FIMAGE_READER);
		} catch (FileSystemException e) {
			System.err.println("Couldn't load dataset: " + e.getMessage());
			System.exit(1);
		}
		
		// Classify testing set & write results
		if (!testingSet.isEmpty()) {
			System.out.println("Classifying Testing Set:");

			/*
			 * Iterate over each image in the group dataset and check that the
			 * classified result is the same as the group name
			 */
			int correct = 0, ndx = 0;
			String fullLabel = null;
			for (FImage image : testingSet) {
				ClassificationResult<String> classF = ca.classify(image);

				if (!(classF == null)) {

					fullLabel = testingSet.getID(ndx++);
					String res = fullLabel + " as " + classF;
					print("Classifying image " + ndx + ": " + res);
					writer.println(res);

					/*
					 * Iterate over results to find the one with the highest
					 * confidence
					 */
					double confidence = 0;
					String mostLikely = "unknown";
					for (String clazz : classF.getPredictedClasses()) {
						double conf = classF.getConfidence(clazz);
						if (conf > confidence) {
							mostLikely = clazz;
							confidence = conf;
						}
					}

					if (fullLabel.contains(mostLikely))
						correct++;
				}
			}

			/*
			 * Show percentage error
			 */
			String accuracy = "Accuracy: " + correct * 100f / ndx + "%";
			writer.println("  ======================  ");
			writer.println(accuracy);
			print(accuracy);

		} else {
			System.out.println("No images found in Testing Directory");
		}
		writer.close();

	}

	public static void print(String string) {
		// TODO Auto-generated method stub
		System.out.println(string);
	}

}
