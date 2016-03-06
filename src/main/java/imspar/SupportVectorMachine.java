package imspar;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.feature.FloatFV;
import org.openimaj.image.FImage;
import org.openimaj.ml.annotation.Annotated;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class SupportVectorMachine implements ClassificationAlgorithm {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5259925839326987622L;
	TraverseDirectory tD = new TraverseDirectory(Constant.trainPath);
	svm_model svmModel = null;

	private int nClass = 0;
	private double[][] xtrain = null;
	private double[][] ytrain = null;

	public int getnClass() {
		return nClass;
	}

	public void setnClass(int nClass) {
		this.nClass = nClass;
	}

	public double[][] getXtrain() {
		return xtrain;
	}

	public void setXtrain(double[][] xtrain) {
		this.xtrain = xtrain;
	}

	public double[][] getYtrain() {
		return ytrain;
	}

	public void setYtrain(double[][] ytrain) {
		this.ytrain = ytrain;
	}

	/*
	 * public static void main(String[] args) { SVM svm = new SVM();
	 * svm.setXtrain(svm.xtrain); svm.setYtrain(svm.ytrain); svm_model m =
	 * svm.svmTrain();
	 * 
	 * double[] ypred = svmPredict(xtest, m);
	 * 
	 * for (int i = 0; i < xtest.length; i++) { System.out.println("(Actual:" +
	 * ytest[i][0] + " Prediction:" + ypred[i] + ")"); } }
	 */
	
	protected svm_problem getProblem(){
		xtrain = getXtrain();
		ytrain = getYtrain();
		svm_problem prob = new svm_problem();
		int recordCount = xtrain.length;
		int featureCount = xtrain[0].length;
		prob.y = new double[recordCount];
		prob.l = recordCount;
		prob.x = new svm_node[recordCount][featureCount];

		for (int i = 0; i < recordCount; i++) {
			double[] features = xtrain[i];
			prob.x[i] = new svm_node[features.length];
			for (int j = 0; j < features.length; j++) {
				svm_node node = new svm_node();
				node.index = j;
				node.value = features[j];
				prob.x[i][j] = node;
			}
			prob.y[i] = ytrain[i][0];
		}
		return prob;
	}
	
	protected svm_parameter getParameters(){
		svm_parameter param = new svm_parameter();
		param.probability = 1;
		param.gamma = 0.5;
		param.nu = 0.5;
		param.C = 1;
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.LINEAR;
		param.cache_size = 20000;
		param.eps = 0.0001;
		return param;
	}

	protected svm_model svmTrain() {
		
		svm_problem prob = this.getProblem();
		svm_parameter param = this.getParameters();

		svm_model model = svm.svm_train(prob, param);

		return model;
	}

	protected double[] svmPredict(double[][] xtest, svm_model model) {

		if (this.nClass < 2) {
			System.err.println("Nullable Class Vector. Set available no. of classes >= 2");
			System.exit(1);
		}

		double[] yPred = new double[xtest.length];

		for (int k = 0; k < xtest.length; k++) {

			double[] fVector = xtest[k];

			svm_node[] nodes = new svm_node[fVector.length];
			for (int i = 0; i < fVector.length; i++) {
				svm_node node = new svm_node();
				node.index = i;
				node.value = fVector[i];
				nodes[i] = node;
			}

			int totalClasses = getnClass();
			int[] labels = new int[totalClasses];
			svm.svm_get_labels(model, labels);

			double[] prob_estimates = new double[totalClasses];
			yPred[k] = svm.svm_predict_probability(model, nodes, prob_estimates);

		}

		return yPred;
	}

	@Override
	public ClassificationResult<String> classify(FImage object) {
		// TODO Auto-generated method stub
		FloatFV featureVector = getFeatureVector(object);
		String outcome = null;
		if (!(featureVector == null)) {
			double[] feature = featureVector.asDoubleVector();
			double[][] xtest = new double[1][];
			xtest[0] = feature;
			double[] ypred = svmPredict(xtest, svmModel);
			outcome = tD.getIntToLabel().get((int) ypred[0]);
			PrintableClassificationResult<String> result = new PrintableClassificationResult<String>(
					PrintableClassificationResult.BEST_RESULT);
			result.put(outcome, 1);
			return result;
		} else {
			return null;
		}
	}

	@Override
	public void train(List<? extends Annotated<FImage, String>> data) {
		// TODO Auto-generated method stub
	}
	
	protected void setXYParam(HashMapSerializable<FloatFV, String> readFeatureVector) {
		double[][] xtrain = new double[readFeatureVector.size()][];
		double[][] ytrain = new double[readFeatureVector.size()][1];
		int ndx = 0;

		for (Entry<FloatFV, String> entry : readFeatureVector.entrySet()) {

			double[] feature = entry.getKey().asDoubleVector();
			int intLabel = tD.getLabelToInt().get(entry.getValue());
			xtrain[ndx] = feature;
			ytrain[ndx][0] = intLabel;
			++ndx;
		}

		// set feature vectors
		setXtrain(xtrain);
		setYtrain(ytrain);
	}

	@Override
	public void train(HashMapSerializable<FloatFV, String> readFeatureVector) {
		this.setXYParam(readFeatureVector);
		// train the classifier
		svmModel = svmTrain();
	}

	public boolean save(String path) {
		boolean status = false;
		try {
			svm.svm_save_model(path, svmModel);
			status = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("IO Exception occurred in model save().");
		}
		return status;
	}

	public svm_model load_model(String path) {
		svm_model model = null;
		try {
			model = svm.svm_load_model(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("IO Exception occurred in model load().");
		}
		return model;
	}
	
	public void cross_validation(HashMapSerializable<FloatFV, String> readFeatureVector) {
		
		if (this.nClass < 2) {
			System.err.println("Nullable Class Vector. Set available no. of classes >= 2");
			System.exit(1);
		}

		this.setXYParam(readFeatureVector);
		svm_problem prob = this.getProblem();
		svm_parameter param = this.getParameters();
		svm.svm_cross_validation(prob, param, 3, new double[prob.l]);
	}

	/**
	 * Convert an image into a {@link DIMENSION} by {@link DIMENSION} feature
	 * vector.
	 * 
	 * @param image
	 * @return Flattened pixels of resized image
	 */
	protected FloatFV getFeatureVector(FImage image) {
		ExtractLocalLBPHistogram extractor = new ExtractLocalLBPHistogram();
		FloatFV feature = extractor.extractFeature(extractor.extractFace(extractor.readImage(image)));
		if (feature == null) {
			return null;
		} else {
			return feature;
		}
	}

}
