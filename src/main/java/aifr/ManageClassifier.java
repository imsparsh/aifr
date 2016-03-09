package aifr;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.ml.annotation.AnnotatedObject;

public class ManageClassifier {

	public ObjectOutputStream oos = null;
	public ObjectInputStream ois = null;
	public FileInputStream streamIn = null;
	public FileOutputStream streamOut = null;
	
	private String classifierPath = null;
	private static String trainPath = Constant.trainPath;
	protected File trainingFile = null;
	protected VFSGroupDataset<FImage> trainingSet = null;

	public String getClassifierPath() {
		return classifierPath;
	}

	public void setClassifierPath(String classifierPath) {
		this.classifierPath = classifierPath;
	}

	public static String getTrainPath() {
		return trainPath;
	}

	public static void setTrainPath(String trainPath) {
		ManageClassifier.trainPath = trainPath;
	}

	public boolean saveClassifier(ClassificationAlgorithm ca) {
		if(classifierPath == null){
			print("Classifier path not specified.");
			System.exit(1);
		}
		boolean b = false;
		try {
			streamOut = new FileOutputStream(classifierPath, true);
			streamOut.close();
			streamOut = new FileOutputStream(classifierPath, true);
			oos = new ObjectOutputStream(streamOut);
		} catch (IOException exp) {
			exp.printStackTrace();
			System.out.println("File IO Error.");
		}
		try {
			oos.writeObject(ca);
			oos.flush();
			b = true;
		} catch (Exception e) {
			b = false;
			e.printStackTrace();
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException ex) {
					ex.printStackTrace();
					System.out.println("Connection close IO Error.");
				}
			}
		}
		return b;
	}

	public ClassificationAlgorithm trainClassifier(ClassificationAlgorithm classifier) {
		trainingFile = new File(trainPath);
		String classifierName = classifier.getClass().getSimpleName();
		try {
			trainingSet = new VFSGroupDataset<FImage>(trainingFile.getAbsolutePath(), ImageUtilities.FIMAGE_READER);
		} catch (FileSystemException e) {
			System.err.println("Couldn't load dataset: " + e.getMessage());
			System.exit(1);
		}

		// Train classifier
		System.out.println("Training classifier : " + classifierName);
		classifier.train(AnnotatedObject.createList(trainingSet));
		System.out.println("Training completed.");
		return classifier;

	}

	public ClassificationAlgorithm trainClassifier(ClassificationAlgorithm classifier, String featurePath) {
		trainingFile = new File(trainPath);
		String classifierName = classifier.getClass().getSimpleName();
		ManageFeatureVector mC = new ManageFeatureVector();
		mC.setFeaturePath(featurePath);
		
		// Train classifier
		System.out.println("Training classifier : " + classifierName);
		System.out.println("Reading Feature Vector..");
		classifier.train(mC.readFeatureVector());
		System.out.println("Features read.");
		System.out.println("Training completed.");
		return classifier;

	}

	public ClassificationAlgorithm readClassifier() {
		if(classifierPath == null){
			System.err.println("Classifier path not specified.");
			System.exit(1);
		}
		else if(!new File(classifierPath).exists()){
			System.err.println("Classifier Vector not available.");
			System.exit(1);
		}
		ClassificationAlgorithm ca = null;
		boolean entryFound = false;
		try {
			streamIn = new FileInputStream(classifierPath);
			while (true) {
				try {
					ois = new ObjectInputStream(streamIn);
					ca = (ClassificationAlgorithm) ois.readObject();
				} catch (EOFException exc) {
					ois.close();
					break;
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (ca instanceof ClassificationAlgorithm) {
					entryFound = true;
				}
			}
			if (!entryFound) {
				System.out.println("No Entry Found.");
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			System.out.println("Classifier Not Found exception occurred");
		} catch (IOException ex) {
			ex.printStackTrace();
			System.out.println("Classifier read IO exception occurred");
		}
		return ca;
	}
	
	private void print(String string) {
		// TODO Auto-generated method stub
		System.out.println(string);
		
	}

}
