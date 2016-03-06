package imspar;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;

import libsvm.svm_model;

public class ReloadedSVM {

	final static String resultPath = Constant.resultPath;
	final static String testPath = Constant.testPath;
	final static String classifierPath = "resources/classifier/SVM.ser";

	public static void main(String ar[]) {
		SupportVectorMachine classifier = new SupportVectorMachine();
		classifier.setnClass(new TraverseDirectory(Constant.trainPath).getClassCount());

		PrintWriter writer = null;
		print("Opening results file.");
		try {
			writer = new PrintWriter(resultPath + "ReloadedSVM.txt", "UTF-8");
		} catch (IOException e) {
			System.err.println("Can't open results file!");
			System.exit(1);
		}

		if (!new File(classifierPath).exists()) {
			ManageFeatureVector mF = new ManageFeatureVector();
			mF.setFeaturePath(ExtractLocalLBPHistogram.getFeaturePath());
			classifier.train(mF.readFeatureVector());
			if (classifier.save(classifierPath)) {
				System.out.println("Classifier dumped : " + classifierPath);
			} else {
				System.out.println("Classifier dump failed.");
			}
		} else {
			print("Classifier already exists : " + classifier.getClass().getSimpleName());
		}

		print("Loading dataset...");
		svm_model model = classifier.load_model(classifierPath);
		classifier.svmModel = model;
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
				ClassificationResult<String> classF = classifier.classify(image);

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

	private static void print(String string) {
		// TODO Auto-generated method stub
		System.out.println(string);
	}
}
