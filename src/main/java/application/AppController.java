package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.commons.vfs2.FileSystemException;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.feature.FloatFV;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.detection.DetectedFace;

import aifr.SupportVectorMachine;
import aifr.Constant;
import aifr.ExtractLocalLBPHistogram;
import aifr.HashMapSerializable;
import aifr.ManageFeatureVector;
import aifr.TraverseDirectory;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import libsvm.svm_model;

public class AppController implements Initializable {

	@FXML
	private AnchorPane scene;

	@FXML
	private Label title;

	@FXML
	private Label outClassLabel;

	@FXML
	private Label progressLabel;

	@FXML
	private Button reset;

	@FXML
	private Button browse;

	@FXML
	private Button recognize;

	@FXML
	private Button exitButton;

	@FXML
	private ProgressBar progressBar;

	@FXML
	private ImageView baseImage;

	@FXML
	private ImageView browseImage;

	@FXML
	private ImageView interestImage;

	@FXML
	private ImageView btnImage;

	@FXML
	private ImageView exitIcon;

	File fileLoad = null;
	VFSListDataset<MBFImage> testingSet = null;
	SupportVectorMachine classifier = new SupportVectorMachine();

	@Override
	public void initialize(URL url, ResourceBundle rB) {
		// TODO Auto-generated method stub
		browse.setDisable(true);
		recognize.setDisable(true);
		reset.setDisable(true);
		outClassLabel.setVisible(false);
		outClassLabel.setText("Not Found");
		progressBar.setProgress(0.0);
		progressBar.setVisible(true);
		progressLabel.setText("Initializing..");
		progressLabel.setVisible(true);
		browseImage.setImage(new Image("file:resources/ui/no_image.jpg"));
		interestImage.setImage(new Image("file:resources/ui/no_image.jpg"));

	}

	public void initClassifier() {
		Platform.runLater(() -> {
			progressLabel.setText("Initializing..");
		});
		classifier.setnClass(new TraverseDirectory(Constant.trainPath).getClassCount());
		if (!new File(Constant.classifierPathSVM).exists()) {
			Platform.runLater(() -> {
				progressLabel.setText("Fetching classifier.. Please Wait..");
			});
			ManageFeatureVector mF = new ManageFeatureVector();
			mF.setFeaturePath(ExtractLocalLBPHistogram.getFeaturePath());
			if (!new File(ExtractLocalLBPHistogram.getFeaturePath()).exists()) {
				// extract all features..
				Platform.runLater(() -> {
					progressLabel.setText("Extracting Features.. Please wait.");
				});
				HashMapSerializable<FloatFV, String> fv = mF.getFeatureVectorMap(new ExtractLocalLBPHistogram());
				Platform.runLater(() -> {
					progressLabel.setText("Dumping Features.. Please wait.");
				});
				if (mF.saveFeatureVector(fv)) {
					Platform.runLater(() -> {
						progressLabel.setText("Feature Vector dumped.");
					});
				} else {
					Platform.runLater(() -> {
						progressLabel.setText("Feature Vector dump failed.");
					});
				}
			}
			Platform.runLater(() -> {
				progressLabel.setText("Reading Features.. Please wait.");
			});
			HashMapSerializable<FloatFV, String> features = mF.readFeatureVector();
			Platform.runLater(() -> {
				progressLabel.setText("Training Classifier.. Please wait.");
			});
			classifier.train(features);
			Platform.runLater(() -> {
				progressLabel.setText("Dumping Classifier.. Please wait.");
			});
			if (classifier.save(Constant.classifierPathSVM)) {
				Platform.runLater(() -> {
					progressLabel.setText("Classifier dump successfull.");
				});
			} else {
				Platform.runLater(() -> {
					progressLabel.setText("Classifier dump failed.");
				});
			}
		}

		Platform.runLater(() -> {
			progressLabel.setText("Loading classifier..");
		});
		svm_model model = classifier.load_model(Constant.classifierPathSVM);
		classifier.svmModel = model;

		if (classifier.svmModel == null) {
			Platform.runLater(() -> {
				progressLabel.setText("Model loading error.");
			});
		} else {
			Platform.runLater(() -> {
				progressLabel.setText("Classifier loaded successfully.");
				progressLabel.setText("Browse an image..");
				browse.setDisable(false);
				reset.setDisable(false);
			});
		}

	}

	public void browseImage(ActionEvent e) {

		// Set extension filter
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG | PNG", "*.JPG", "*.PNG");
		fileChooser.getExtensionFilters().addAll(extFilterJPG);

		// Show open file dialog
		try {
			this.fileLoad = fileChooser.showOpenDialog(null);
			BufferedImage bufferedImage = ImageIO.read(this.fileLoad);
			Image image = SwingFXUtils.toFXImage(bufferedImage, null);
			Platform.runLater(() -> {
				browseImage.setImage(image);
				recognize.setDisable(false);
				progressLabel.setText("Detect & Recognize.");
			});
		} catch (IOException ex) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void processImage() {
		new Thread() {
			public void run() {
				Platform.runLater(() -> {
					browse.setDisable(true);
					recognize.setDisable(true);
					reset.setDisable(true);
				});
				ExtractLocalLBPHistogram lbp = new ExtractLocalLBPHistogram();
				Platform.runLater(() -> {
					progressLabel.setText("Reading image..");
					progressBar.setProgress(0.01);
				});
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Platform.runLater(() -> {
					progressBar.setProgress(0.05);
				});
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Platform.runLater(() -> {
					progressBar.setProgress(0.10);
				});
				try {
					Thread.sleep(400);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				MBFImage img = lbp.readImage(fileLoad.getAbsolutePath());
				Platform.runLater(() -> {
					progressLabel.setText("Extracting face..");
					progressBar.setProgress(0.15);
				});
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Platform.runLater(() -> {
					progressBar.setProgress(0.20);
				});
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Platform.runLater(() -> {
					progressBar.setProgress(0.25);
				});
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				DetectedFace face = lbp.extractFace(img);
				if (!(face == null)) {
					Platform.runLater(() -> {
						progressLabel.setText("Aligning face..");
						progressBar.setProgress(0.30);
					});
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Platform.runLater(() -> {
						progressBar.setProgress(0.35);
					});
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Platform.runLater(() -> {
						progressBar.setProgress(0.40);
					});
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// FImage alignedFace = lbp.alignFace(face);
					// DisplayUtilities.display(alignedFace);

					Platform.runLater(() -> {
						progressLabel.setText("Extracting features..");
						progressBar.setProgress(0.45);
					});
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Platform.runLater(() -> {
						progressBar.setProgress(0.50);
					});
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Platform.runLater(() -> {
						progressBar.setProgress(0.55);
					});
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Platform.runLater(() -> {
						progressBar.setProgress(0.60);
					});
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Platform.runLater(() -> {
						progressBar.setProgress(0.65);
					});
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					FloatFV feature = lbp.extractFeature(face);
					Platform.runLater(() -> {
						progressLabel.setText("Classifying identity..");
						progressBar.setProgress(0.75);
					});
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Platform.runLater(() -> {
						progressBar.setProgress(0.80);
					});
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ClassificationResult<String> classF = classifier.classify(feature);
					Platform.runLater(() -> {
						progressBar.setProgress(0.84);
					});
					try {
						Thread.sleep(25);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Platform.runLater(() -> {
						progressBar.setProgress(0.88);
					});
					try {
						Thread.sleep(25);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Platform.runLater(() -> {
						progressBar.setProgress(0.92);
					});
					try {
						Thread.sleep(25);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Platform.runLater(() -> {
						progressBar.setProgress(0.96);
					});
					try {
						Thread.sleep(25);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (!(classF == null)) {
						try {
							testingSet = new VFSListDataset<MBFImage>(
									new File(Constant.trainPath + "/" + classF.toString()).getAbsolutePath(),
									ImageUtilities.MBFIMAGE_READER);
						} catch (FileSystemException e) {
							System.err.println("Couldn't load dataset: " + e.getMessage());
							System.exit(1);
						}

						MBFImage imagePath = testingSet.get(0);
						BufferedImage bufferedImage = ImageUtilities.createBufferedImage(imagePath);
						Image image = SwingFXUtils.toFXImage(bufferedImage, null);

						Platform.runLater(() -> {
							interestImage.setImage(image);

							progressLabel.setVisible(false);
							progressBar.setVisible(false);
							outClassLabel.setVisible(true);
							outClassLabel.setText(classF.toString());
						});

					} else {

						Platform.runLater(() -> {
							outClassLabel.setVisible(false);
							progressLabel.setText("Classification Error Occurred..");
						});

					}
				} else {
					Platform.runLater(() -> {
						progressLabel.setText("No Face Found..");
						progressBar.setProgress(1.0);
					});
				}
				Platform.runLater(() -> {
					reset.setDisable(false);
				});
			}
		}.start();
	}

	public void reset(ActionEvent e) {
		new Thread() {
			public void run() {
				Platform.runLater(() -> {
					outClassLabel.setVisible(false);
					outClassLabel.setText("Not Found");
					progressBar.setProgress(0.0);
					progressBar.setVisible(true);
					progressLabel.setText("Browse an image..");
					progressLabel.setVisible(true);
					recognize.setDisable(true);
					browse.setDisable(false);
					browseImage.setImage(new Image("file:resources/ui/no_image.jpg"));
					interestImage.setImage(new Image("file:resources/ui/no_image.jpg"));
				});
			}
		}.start();
	}

	public void exit(ActionEvent e) {
		new Thread() {
			public void run() {
				Platform.runLater(() -> {
					progressLabel.setText("Exiting application..");
				});
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.exit(0);
			}
		}.start();
	}

}
