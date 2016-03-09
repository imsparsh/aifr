package aifr;

public class PlayNeighbors {

	final static String resultPath = Constant.resultPath;
	final static String classifierPath = "resources/classifier/KNN.ser";

	public static void trainKNN() {
		KNearestNeighbor classifier = new KNearestNeighbor();
		ManageClassifier mC = new ManageClassifier();
		mC.setClassifierPath(classifierPath);
		ClassificationAlgorithm ca = mC.trainClassifier(classifier);
		if (mC.saveClassifier(ca)) {
			System.out.println("Classifier dumped : " + classifierPath);
		} else {
			System.out.println("Classifier dump failed.");
		}
	}

	public static void trainKNN(String featurePath) {
		KNearestNeighbor classifier = new KNearestNeighbor();
		ManageClassifier mC = new ManageClassifier();
		mC.setClassifierPath(classifierPath);
		ClassificationAlgorithm ca = mC.trainClassifier(classifier, featurePath);
		if (mC.saveClassifier(ca)) {
			System.out.println("Classifier dumped : " + classifierPath);
		} else {
			System.out.println("Classifier dump failed.");
		}
	}

	public static void main(String ar[]) {
		Utilities.runClassifier(new KNearestNeighbor(), "KNN", classifierPath, new ExtractLocalLBPHistogram());
		// Utilities.runClassifier(new KNearestNeighbor(), "KNN", classifierPath, new ExtractCLMShapeFeature());
		// trainKNN(ExtractCLMShapeFeature.getFeaturePath());
	}

}
