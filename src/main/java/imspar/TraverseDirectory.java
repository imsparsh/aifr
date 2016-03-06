package imspar;

import java.io.File;
import java.io.Serializable;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

public class TraverseDirectory implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5694757228128629350L;
	private ArrayList<String> filePath = new ArrayList<String>();
	private ArrayList<String> fileClass = new ArrayList<String>();
	private HashMap<String, String> fileMap = new HashMap<String, String>();
	private HashMap<Integer, String> intToLabel = new HashMap<Integer, String>();
	private HashMap<String, Integer> labelToInt = new HashMap<String, Integer>();

	public ArrayList<String> getFilePath() {
		return filePath;
	}

	public void setFilePath(ArrayList<String> filePath) {
		this.filePath = filePath;
	}

	public ArrayList<String> getFileClass() {
		return fileClass;
	}

	public void setFileClass(ArrayList<String> fileClass) {
		this.fileClass = fileClass;
	}

	public HashMap<String, String> getFileMap() {
		return fileMap;
	}

	public void setFileMap(HashMap<String, String> fileMap) {
		this.fileMap = fileMap;
	}

	public HashMap<Integer, String> getIntToLabel() {
		return intToLabel;
	}

	public void setIntToLabel(HashMap<Integer, String> intToLabel) {
		this.intToLabel = intToLabel;
	}

	public HashMap<String, Integer> getLabelToInt() {
		return labelToInt;
	}

	public void setLabelToInt(HashMap<String, Integer> labelToInt) {
		this.labelToInt = labelToInt;
	}

	TraverseDirectory(String sdir) {

		String trainingFolderPath = sdir;

		File trainingFolder = new File(trainingFolderPath);
		String[] trainingSubfolderPaths = trainingFolder.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});

		ArrayList<String> filePathList = new ArrayList<String>();
		ArrayList<String> fileClassList = new ArrayList<String>();
		HashMap<String, String> map = new HashMap<String, String>();
		HashMap<Integer, String> intToLabelBase = new HashMap<Integer, String>();
		HashMap<String, Integer> labelToIntBase = new HashMap<String, Integer>();
		int ndx = 0;

		for (String SubfolderPath : trainingSubfolderPaths) {
			File[] files = new File(trainingFolderPath + "\\" + SubfolderPath).listFiles();

			for (File file : files) {
				filePathList.add(file.getAbsolutePath());
				fileClassList.add(SubfolderPath);
				map.put(file.getAbsolutePath(), SubfolderPath);
			}
			
			intToLabelBase.put(ndx, SubfolderPath);
			labelToIntBase.put(SubfolderPath, ndx);
			++ndx;
			
		}

		setFilePath(filePathList);
		setFileClass(fileClassList);
		setFileMap(map);
		setIntToLabel(intToLabelBase);
		setLabelToInt(labelToIntBase);

	}
	
	public int getClassCount(){
		return intToLabel.size();
	}

	public int getRecordCount(){
		return fileMap.size();
	}

}
