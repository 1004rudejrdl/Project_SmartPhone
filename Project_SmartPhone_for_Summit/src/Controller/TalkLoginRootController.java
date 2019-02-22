package Controller;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import Model.Chatter;
import Model.User;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class TalkLoginRootController implements Initializable {

	@FXML private Label fssLblTime;	
	static final DateFormat CLOCK_FORMAT = new SimpleDateFormat("hh:mm:ss a");
	int count=0;

	public Stage talkLoginStage;
	@FXML public Button scsBtnWifi;	
	@FXML public Button scsBtnVolume;
	@FXML public Button scsBtnMemo;
	@FXML public Button scsBtnCamera;
	@FXML public Button scsBtnMusic;
	@FXML private ImageView logImgVLogo;
	@FXML private TextField logTfId;
	@FXML private PasswordField logPwf;
	@FXML private Label logLblState;
	@FXML private Button logBtnLogin;
	@FXML private Label logLblSignUp;
	@FXML private Label logLblFindID;
	@FXML private Label logLblFindPw;
	static Chatter chatter;
	UserDAO userDAO = new UserDAO();
	static User user=null;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		setUpLocalTimeFSS();

		// 00. ������ id,pw set
		scsBtnWifi.setOnAction(eAuto -> {
			logTfId.setText("test1@naver.com");
			logPwf.setText("asdf1234");
		});
		scsBtnVolume.setOnAction(eAuto -> {
			logTfId.setText("test2@naver.com");
			logPwf.setText("asdf1234");
		});
		scsBtnMemo.setOnAction(eAuto -> {
			logTfId.setText("test3@naver.com");
			logPwf.setText("asdf1234");
		});
		scsBtnCamera.setOnAction(eAuto -> {
			logTfId.setText("test4@naver.com");
			logPwf.setText("asdf1234");
		});
		scsBtnMusic.setOnAction(eAuto -> {
			logTfId.setText("test5@naver.com");
			logPwf.setText("asdf1234");
		});
		
		// 01. �α��� ��ư
		logBtnLogin.setOnAction(eLogin -> handleLogBtnLoginAction());
		logPwf.setOnKeyPressed(eFindId -> {
			if (eFindId.getCode() == KeyCode.ENTER)
				handleLogBtnLoginAction();
		});

		// 02. ȸ������ �� hover
		logLblSignUp.setOnMouseClicked(eSignUp -> handleLogLblSignUpAction());

		// 03. ���̵� ã�� �� hover
		logLblFindID.setOnMouseClicked(eSearchId -> handleLogLblFindIDAction());

		// 04. �н����� ã�� �� hover
		logLblFindPw.setOnMouseClicked(eSearchPw -> handleLogLblFindPWAction());

	}


	private void setUpLocalTimeFSS() {
		Task<Void> task = new Task<Void>() {
			
			@Override
			protected Void call() throws Exception {
				try {
					count = 0;
					while (true) {
						count++;
						SimpleDateFormat dy = new SimpleDateFormat("HH:mm:ss");
						String strDT = dy.format(new Date());
						Platform.runLater(() -> {
							fssLblTime.setText(strDT);
						});
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
					Platform.runLater(() -> {
						callAlert("�ð� ���� : ���� �ð��� �� �� �����ϴ�");
					});
				}
				return null;
			}
		};
		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
	}
	
	// 01. �α��� ��ư
	private void handleLogBtnLoginAction() {
		if (logTfId.getText().trim().equals("") || logPwf.getText().trim().equals("")) {
			logLblState.setText("���̵�, ��й�ȣ�� �Է��ϼ���");
		} else if (!(logTfId.getText().contains("@") && logTfId.getText().contains("."))) {
			logLblState.setText("Use Email Address\n�̸��� ������ �ƴմϴ�");
		} else if (logPwf.getText().trim().equals(null)) {
			logLblState.setText("��й�ȣ�� �Է����� �����̽��ϴ�");
		} else if (!((logPwf.getText().trim().length() >= 8 && logPwf.getText().trim().length() <= 16))) {
			logLblState.setText("��й�ȣ�� 8�� �̻� 16�ڸ� �̳��� �Է��ϼ���");
		} else {
			user = userDAO.selectLoginUser(logTfId.getText().trim(), logPwf.getText().trim());
			if (user != null) {
				int logOn = userDAO.updateUserLogOn(user, "on");
//				if (logOn == 1)
//					callAlert("�α��� ���� : DB�� �α��� ���·� ������Ʈ �Ǿ����ϴ�.");
//				else
//					callAlert("�α��� ���� : DB�� �α��� ���� ������� �ʾҽ��ϴ�.");
				if (logOn != 1)
					callAlert("�α��� ���� : DB�� �α��� ���� ������� �ʾҽ��ϴ�.");
				try {
					Stage talkMainStage = new Stage(StageStyle.UTILITY);
					FXMLLoader loader = new FXMLLoader(getClass().getResource("../View/talk_profile_friend.fxml"));
					Parent talkMainRoot = loader.load();
					TalkProFriRootController talkMainController = loader.getController();
					talkMainController.talkProFriStage = talkMainStage;
					Scene talkMainScene = new Scene(talkMainRoot);
					talkMainScene.getStylesheets().add(getClass().getResource("../css/talk_login.css").toString());
					talkMainStage.setScene(talkMainScene);
					talkMainStage.setTitle("PROFILE and FRIEND");
					talkMainStage.setResizable(false);
					talkLoginStage.close();
					talkMainStage.show();
					callAlert("Login Succeeded : " + user.getNicName() + " �� ȯ���մϴ�.");
				} catch (Exception e) {
					callAlert("Login Failed : ���̵�, �н����带 Ȯ���ϼ���");
					e.printStackTrace();
				}

			} else {
				logLblState.setText("�ùٸ� ���̵�, ��й�ȣ�� �Է��ϼ���");
				callAlert("Can't found : ȸ���� �ƴϽø� ȸ�������� ���ּ���");
				logPwf.clear();
			} // else �� �Է�o
		} // else �� �Է�x
	}// handlelogBtnLoginAction

	// 02. ȸ�������� ������ ��
	private void handleLogLblSignUpAction() {
		try {
			Stage talkMainStage = new Stage(StageStyle.UTILITY);
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../View/talk_sign_up.fxml"));
			Parent talkMainRoot = loader.load();
			TalkSignUpRootController talkMainController = loader.getController();
			talkMainController.talkSignUpStage = talkMainStage;
			Scene talkMainScene = new Scene(talkMainRoot);
			talkMainScene.getStylesheets().add(getClass().getResource("../css/talk_login.css").toString());
			talkMainStage.setScene(talkMainScene);
			talkMainStage.setResizable(false);
			talkMainStage.setTitle("Va Talk SIGN UP");
			talkLoginStage.close();
			talkMainStage.show();
			callAlert("Sign Up : VaTalk�� ȸ���� �Ǿ���� \nThank U for Join Us");
		} catch (Exception e) {
			callAlert("Connect Failed : ������ ���� �� �� �����ϴ�.");
			e.printStackTrace();
		}
	}

	// 03. ���̵� ã�� �� hover
	private void handleLogLblFindIDAction() {
		Parent searchIdRoot = null;
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../View/talk_search_id.fxml"));
			searchIdRoot = loader.load();
			Stage srchIdStage = new Stage(StageStyle.UTILITY);
			srchIdStage.initModality(Modality.NONE);
			srchIdStage.initOwner(talkLoginStage);
			srchIdStage.setTitle("ID SEARCH");
			srchIdStage.setResizable(false);
			Scene srchIdScene = new Scene(searchIdRoot);
			srchIdScene.getStylesheets().add(getClass().getResource("../css/talk_login.css").toString());
			srchIdStage.setScene(srchIdScene);
			srchIdStage.show();
			Label srchLblState = (Label) searchIdRoot.lookup("#srchLblState");
			TextField srchTfPhNo = (TextField) searchIdRoot.lookup("#srchTfPhNo");
			Button srchBtnFindId = (Button) searchIdRoot.lookup("#srchBtnFindId");
			Button srchBtnCancel = (Button) searchIdRoot.lookup("#srchBtnCancel");

			srchBtnFindId.setOnAction(ePhNoForId -> {
				if (srchTfPhNo.getText().trim().equals("")) {
					srchLblState.setText("��ȭ��ȣ�� �Է��ϼ���");
					return;
				}
				if (!(srchTfPhNo.getText().contains("-") && (srchTfPhNo.getText().length() == 13))) {
					srchLblState.setText("Use PhoneNo Format\n��ȭ��ȣ ������ ���� �ʽ��ϴ�");
					return;
				} else {
					String dbPhNo = userDAO.newSelectUserPhNo(srchTfPhNo.getText());
					if (dbPhNo != null) {
						String[] dbPhoneNo = dbPhNo.split("-");
						String[] inputPhoneNo = srchTfPhNo.getText().split("-");
						if ((inputPhoneNo[0] + inputPhoneNo[1] + inputPhoneNo[2])
								.equals(dbPhoneNo[0] + dbPhoneNo[1] + dbPhoneNo[2])) {
							String dbID = userDAO.searchUserIdByPhNo(srchTfPhNo.getText());
							if (dbID != null) {
								srchLblState.setText("ã���� ���̵��\n" + dbID + " �Դϴ�");
								callAlert("ȸ��Ȯ�� : �̹� ȸ���̽ʴϴ�");
							} else {
								srchLblState.setText("�ش� ���̵� �������� �ʽ��ϴ�");
							}
						}
					}
				}
			});
			srchTfPhNo.setOnKeyPressed(ePhNoForId -> {
				if (ePhNoForId.getCode() == KeyCode.ENTER) {
					if (srchTfPhNo.getText().trim().equals("")) {
						srchLblState.setText("��ȭ��ȣ�� �Է��ϼ���");
						return;
					}
					if (!(srchTfPhNo.getText().contains("-") && (srchTfPhNo.getText().length() == 13))) {
						srchLblState.setText("Use PhoneNo Format\n��ȭ��ȣ ������ ���� �ʽ��ϴ�");
						return;
					} else {
						String dbPhNo = userDAO.newSelectUserPhNo(srchTfPhNo.getText());
						if (dbPhNo != null) {
							String[] dbPhoneNo = dbPhNo.split("-");
							String[] inputPhoneNo = srchTfPhNo.getText().split("-");
							if ((inputPhoneNo[0] + inputPhoneNo[1] + inputPhoneNo[2])
									.equals(dbPhoneNo[0] + dbPhoneNo[1] + dbPhoneNo[2])) {
								String dbID = userDAO.searchUserIdByPhNo(srchTfPhNo.getText());
								if (dbID != null) {
									srchLblState.setText("ã���� ���̵��\n" + dbID + " �Դϴ�");
									callAlert("ȸ��Ȯ�� : �̹� ȸ���̽ʴϴ�");
								} else {
									srchLblState.setText("�ش� ���̵� �������� �ʽ��ϴ�");
								}
							}
						}
					} 
				}
			});

			srchBtnCancel.setOnAction(eClose -> srchIdStage.hide());
			srchBtnCancel.setOnKeyPressed(eClose -> {
				if (eClose.getCode() == KeyCode.ENTER)
					srchIdStage.hide();
			});
			srchIdStage.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent eClose) -> {
				if (eClose.getCode() == KeyCode.ESCAPE)
					srchIdStage.hide();
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 04. �н����� ã�� �� hover
	private void handleLogLblFindPWAction() {
		Parent searchPwRoot = null;
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../View/talk_search_pw.fxml"));
			searchPwRoot = loader.load();
			Stage srchPwStage = new Stage(StageStyle.UTILITY);
			srchPwStage.initModality(Modality.NONE);
			srchPwStage.initOwner(talkLoginStage);
			srchPwStage.setTitle("PW SEARCH");
			srchPwStage.setResizable(false);
			Scene srchPwScene = new Scene(searchPwRoot);
			srchPwScene.getStylesheets().add(getClass().getResource("../css/talk_login.css").toString());
			srchPwStage.setScene(srchPwScene);
			srchPwStage.show();
			Label srchLblState = (Label) searchPwRoot.lookup("#srchLblState");
			TextField srchTfId = (TextField) searchPwRoot.lookup("#srchTfId");
			Button srchBtnFindPW = (Button) searchPwRoot.lookup("#srchBtnFindPW");
			Button srchBtnCancel = (Button) searchPwRoot.lookup("#srchBtnCancel");

			srchBtnFindPW.setOnAction(eIdForPw -> {
				if (srchTfId.getText().trim().equals("")) {
					srchLblState.setText("�̸����� �Է��ϼ���");
					return;
				}
				if (!(srchTfId.getText().contains("@") && srchTfId.getText().contains("."))) {
					srchLblState.setText("Use Email Address\n�̸��� ������ �ƴմϴ�");
					return;
				} else {
					String dbId = userDAO.searchExistedUserId(srchTfId.getText().trim());

					if (dbId != null) { 
						String dbPW = userDAO.searchUserPwById(srchTfId.getText());
						if (dbPW != null) {
							srchLblState.setText("ã���� �н������ " + dbPW + " �Դϴ�");
						} else {
							/*********************** �̸��Ϸ� ��й�ȣ �������� ***************************/
							srchLblState.setText("�����ڸ� ���� ��й�ȣ�� �缳�� ���ּ���");
						}
					} else {
						srchLblState.setText("Can't find ID\n�ش� ���̵� �����ϴ�");
						return;
					}
				}
			});

			srchTfId.setOnKeyPressed(eIdForPw -> {
				if (eIdForPw.getCode() == KeyCode.ENTER) {
					if (srchTfId.getText().trim().equals("")) {
						srchLblState.setText("�̸����� �Է��ϼ���");
						return;
					}
					if (!(srchTfId.getText().contains("@") && srchTfId.getText().contains("."))) {
						srchLblState.setText("Use Email Address\n�̸��� ������ �ƴմϴ�");
						return;
					} else {
						String dbId = userDAO.searchExistedUserId(srchTfId.getText().trim());

						if (dbId != null) { 
							String dbPW = userDAO.searchUserPwById(srchTfId.getText());
							if (dbPW != null) {
								srchLblState.setText("ã���� �н������ " + dbPW + " �Դϴ�");
							} else {
								/*********************** �̸��Ϸ� ��й�ȣ �������� ***************************/
								srchLblState.setText("�����ڸ� ���� ��й�ȣ�� �缳�� ���ּ���");
							}
						} else {
							srchLblState.setText("Can't find ID\n�ش� ���̵� �����ϴ�");
							return;
						}
					}
				}
			});

			srchBtnCancel.setOnAction(eClose -> srchPwStage.hide());
			srchBtnCancel.setOnKeyPressed(eClose -> {
				if (eClose.getCode() == KeyCode.ENTER)
					srchPwStage.hide();
			});
			srchPwStage.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent eClose) -> {
				if (eClose.getCode() == KeyCode.ESCAPE)
					srchPwStage.hide();
			});

		} catch (Exception e) {
		}
	}

	// ��Ÿ ����ó�� �Լ� "�������� : ���� �� �޼���"
	public static void callAlert(String contentText) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("�˸�â");
		alert.setHeaderText(contentText.substring(0, contentText.lastIndexOf(":")));
		alert.setContentText(contentText.substring(contentText.lastIndexOf(":") + 2));
		alert.showAndWait();
	}
}
