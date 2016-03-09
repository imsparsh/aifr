package aifr;

import org.openimaj.feature.FloatFV;

public class OrganizeLocalLBPHistogram {

	public static void fetchAndSave() {
		ExtractLocalLBPHistogram clm = new ExtractLocalLBPHistogram();
		ManageFeatureVector mC = new ManageFeatureVector();
		mC.setFeaturePath(ExtractLocalLBPHistogram.getFeaturePath());
		HashMapSerializable<FloatFV, String> fv = mC.getFeatureVectorMap(clm);
		if (mC.saveFeatureVector(fv)) {
			System.out.println("Feature Vector dumped : " + ExtractLocalLBPHistogram.getFeaturePath());
		} else {
			System.out.println("Feature Vector dump failed.");
		}
	}

	public static void main(String ar[]) {
		fetchAndSave();
	}

}
