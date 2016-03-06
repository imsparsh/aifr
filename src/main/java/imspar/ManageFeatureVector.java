package imspar;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map.Entry;

import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.feature.FloatFV;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;

public class ManageFeatureVector {

	public ObjectOutputStream oos = null;
	public ObjectInputStream ois = null;
	public FileInputStream streamIn = null;
	public FileOutputStream streamOut = null;
	
	private String featurePath = null;
	private static String trainPath = Constant.trainPath;
	protected File trainingFile = null;
	protected VFSGroupDataset<FImage> trainingSet = null;

	public String getFeaturePath() {
		return featurePath;
	}

	public void setFeaturePath(String featurePath) {
		this.featurePath = featurePath;
	}

	public static String getTrainPath() {
		return trainPath;
	}

	public static void setTrainPath(String trainPath) {
		ManageFeatureVector.trainPath = trainPath;
	}

	public boolean saveFeatureVector(HashMapSerializable<FloatFV, String> hM) {
		if(featurePath == null){
			print("Feature path not specified.");
			System.exit(1);
		}
		boolean b = false;
		try {
			streamOut = new FileOutputStream(featurePath, true);
			streamOut.close();
			streamOut = new FileOutputStream(featurePath, true);
			oos = new ObjectOutputStream(streamOut);
		} catch (IOException exp) {
			exp.printStackTrace();
			System.out.println("File IO Error.");
		}
		try {
			oos.writeObject(hM);
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

	@SuppressWarnings("unchecked")
	public HashMapSerializable<FloatFV, String> readFeatureVector() {
		if(featurePath == null){
			System.err.println("Feature path not specified.");
			System.exit(1);
		}
		else if(!new File(featurePath).exists()){
			System.err.println("Feature Vector not available.");
			System.exit(1);
		}
		HashMapSerializable<FloatFV, String> fv = null;
		boolean entryFound = false;
		try {
			streamIn = new FileInputStream(featurePath);
			while (true) {
				try {
					ois = new ObjectInputStream(streamIn);
					fv =  (HashMapSerializable<FloatFV, String>) ois.readObject();
				} catch (EOFException exc) {
					ois.close();
					break;
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (fv instanceof HashMapSerializable) {
					entryFound = true;
				}
			}
			if (!entryFound) {
				System.out.println("No Entry Found.");
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			System.out.println("Feature Not Found exception occurred");
		} catch (IOException ex) {
			ex.printStackTrace();
			System.out.println("Feature read IO exception occurred");
		}
		return fv;
	}
	
	public HashMapSerializable<FloatFV, String> getFeatureVectorMap(ExtractFeature extractor){
		HashMapSerializable<FloatFV, String> hM = new HashMapSerializable<FloatFV, String>();
		TraverseDirectory tD = new TraverseDirectory(Constant.trainPath);
		HashMap<String, String> map = tD.getFileMap();
		ReadImages rI = new ReadImages();
		FloatFV fv = null;
		for (Entry<String, String> entry : map.entrySet()){
			System.out.println("Reading "+entry.getKey());
			MBFImage fImg = rI.readImage(entry.getKey());
			if(extractor instanceof ExtractLocalLBPHistogram){
				fv = extractor.extractFeature(rI.extractFace(fImg));
			}
			if (!(fv == null)){
				hM.put(fv, entry.getValue());
			}
		}
		return hM;
	}
	
	private void print(String string) {
		// TODO Auto-generated method stub
		System.out.println(string);
	}

}
