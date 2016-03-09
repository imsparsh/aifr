package aifr;

import libsvm.svm_model;

public class PlaySupportVectors {

	final static String resultPath = Constant.resultPath;
	final static String classifierPath = "resources/classifier/SVM.ser";
	
	public static void trainSVM() {
		SupportVectorMachine classifier = new SupportVectorMachine();
		ManageClassifier mC = new ManageClassifier();
		mC.setClassifierPath(classifierPath);
		ClassificationAlgorithm ca = mC.trainClassifier(classifier);
		if (mC.saveClassifier(ca)) {
			System.out.println("Classifier dumped : " + classifierPath);
		} else {
			System.out.println("Classifier dump failed.");
		}
	}

/*	public static void trainSVM(String featurePath) {
		SupportVectorMachine classifier = new SupportVectorMachine();
		classifier.setnClass(2);
		ManageClassifier mC = new ManageClassifier();
		mC.setClassifierPath(classifierPath);
		ClassificationAlgorithm ca = mC.trainClassifier(classifier, featurePath);
		if (mC.saveClassifier(ca)) {
			System.out.println("Classifier dumped : " + classifierPath);
		} else {
			System.out.println("Classifier dump failed.");
		}
	}

*/
	public static void trainSVM(String featurePath) {
		long startTime = System.nanoTime();
		SupportVectorMachine classifier = new SupportVectorMachine();
		if(classifier instanceof SupportVectorMachine){
			((SupportVectorMachine) classifier).setnClass(new TraverseDirectory(Constant.trainPath).getClassCount());
		}
		ManageClassifier mC = new ManageClassifier();
		ManageFeatureVector mF = new ManageFeatureVector();
		mC.setClassifierPath(classifierPath);
		mF.setFeaturePath(featurePath);
		classifier.train(mF.readFeatureVector());
		if (classifier.save(classifierPath)) {
			System.out.println("Classifier dumped : " + classifierPath);
		} else {
			System.out.println("Classifier dump failed.");
		}
/*		if (mC.saveClassifier(ca)) {
			System.out.println("Classifier dumped : " + classifierPath);
		} else {
			System.out.println("Classifier dump failed.");
		}
*/
		long endTime = System.nanoTime();
		System.out.println("Time elapsed: "+(endTime-startTime)+" secs.");
		}

	public static void crossValidate(String featurePath) {
		long startTime = System.nanoTime();
		SupportVectorMachine classifier = new SupportVectorMachine();
		ManageFeatureVector mF = new ManageFeatureVector();
		mF.setFeaturePath(featurePath);
		if(classifier instanceof SupportVectorMachine){
			((SupportVectorMachine) classifier).setnClass(new TraverseDirectory(Constant.trainPath).getClassCount());
		}
		classifier.cross_validation(mF.readFeatureVector());
		long endTime = System.nanoTime();
		System.out.println("Time elapsed: "+((double)(endTime-startTime)/1000000000)+" secs.");
	}

	public static svm_model loadModel() {
		System.out.println("Loading SVM model..");
		long startTime = System.nanoTime();
		SupportVectorMachine classifier = new SupportVectorMachine();
		svm_model model = classifier.load_model(classifierPath);
		long endTime = System.nanoTime();
		System.out.println("Loading completed.");
		System.out.println("Time elapsed: "+((double)(endTime-startTime)/1000000000)+" secs.");
		return model;
	}

	public static void main(String ar[]) {
		// Utilities.runClassifier(new SupportVectorMachine(), "SVM", classifierPath, new ExtractLocalLBPHistogram());
		// trainSVM(ExtractLocalLBPHistogram.getFeaturePath());
		// crossValidate(ExtractLocalLBPHistogram.getFeaturePath());
		// loadModel();
	}

}
