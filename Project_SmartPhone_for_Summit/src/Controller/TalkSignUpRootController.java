package Controller;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class TalkSignUpRootController implements Initializable{
	
	@FXML private Label fssLblTime;	
	static final DateFormat CLOCK_FORMAT = new SimpleDateFormat("hh:mm:ss a");
	int count=0;

	public Stage talkSignUpStage;
	
	@FXML private TextField suTfUserId;
	@FXML private Button suBtnFindId;
	@FXML private PasswordField suPwfPut;
	@FXML private PasswordField suPwfCheck;
	@FXML private TextField suTfPhNo;
	@FXML private Button suBtnFindPhNo;
	@FXML private TextField suTfUserNic;
	@FXML private Label suLblState;
	@FXML private Button suBtnSign;
	@FXML private Button suBtnCancel;
	@FXML private Label suLblFindId;
	@FXML private Label suLblFindPw;
	UserDAO userDAO = new UserDAO();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		setUpLocalTimeFSS();
		
		suPwfPut.setDisable(true);
		suPwfCheck.setDisable(true);
		suTfPhNo.setDisable(true);
		suBtnFindPhNo.setDisable(true);
		suTfUserNic.setDisable(true);

		// 01. ���̵� üũ ��ư
		suTfUserId.setOnKeyPressed(eFindId -> {
			if (eFindId.getCode() == KeyCode.ENTER)
				handleSuBtnFindIdAction();
		});
		suBtnFindId.setOnAction(eFindId -> handleSuBtnFindIdAction());

		// 02. ��ȭ��ȣ Ȯ��
		suTfPhNo.setOnKeyPressed(eFindPw -> {
			if (eFindPw.getCode() == KeyCode.ENTER)
				handleSuBtnFindPhNoAction();
		});
		suBtnFindPhNo.setOnAction(eFindPw -> handleSuBtnFindPhNoAction());

		// 03. ȸ������ ��ư. �׸� null�� Ȯ��, PWȮ��.
		suBtnSign.setOnAction(eSignUp -> handleSuBtnSignAction());
		suTfUserNic.setOnKeyPressed(eSignUp -> {
			if (eSignUp.getCode() == KeyCode.ENTER)
				handleSuBtnSignAction();
		});

		// 04. ���̵� ã�� �� hover
		suLblFindId.setOnMouseClicked(eSearchId -> handleSuLblFindIdAction());

		// 05. �н����� ã�� �� hover
		suLblFindPw.setOnMouseClicked(eSearchPw -> handleSuLblFindPwAction());

		// 06. ��ҹ�ư
		suBtnCancel.setOnAction(eGo2Login -> handleSuBtnCancelAction());

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

	// 01. ���̵� üũ ��ư
	private void handleSuBtnFindIdAction() {
		if (suTfUserId.getText().trim().equals("")) {
			suLblState.setText("�̸����� �Է��ϼ���");
			return;
		}
		String id = userDAO.searchExistedUserId(suTfUserId.getText());
		if (!(suTfUserId.getText().contains("@") && suTfUserId.getText().contains(".")))
			suLblState.setText("Use Email Address\n�̸��� ������ �ƴմϴ�");
		else if (id != null) {
			suLblState.setText("Please Use other ID\n�̹� ��� ���� ID�Դϴ�");
		} else {
			suPwfPut.setDisable(false);
			suPwfCheck.setDisable(false);
			suTfPhNo.setDisable(false);
			suBtnFindPhNo.setDisable(false);
			suTfUserNic.setDisable(true);
			suLblState.setText("We Checked Your ID\n��� ������ ID�Դϴ�");
		}
	}

	// 02. ��ȭ��ȣ Ȯ��
	private void handleSuBtnFindPhNoAction() {
		if (suTfPhNo.getText().trim().equals("")) {
			suLblState.setText("��ȭ��ȣ�� �Է��ϼ���");
			return;
		}
		String dbPhNo = userDAO.newSelectUserPhNo(suTfPhNo.getText());
		if (!(suTfPhNo.getText().contains("-") && (suTfPhNo.getText().length() == 13)))
			suLblState.setText("Use PhoneNo Format\n��ȭ��ȣ ������ ���� �ʽ��ϴ�");
		else if (suTfPhNo.getText().contains("-") && suTfPhNo.getText().length() == 13 && dbPhNo != null) {
			String[] dbPhoneNo = dbPhNo.split("-");
			String[] inputPhoneNo = suTfPhNo.getText().split("-");
			if ((inputPhoneNo[0] + inputPhoneNo[1] + inputPhoneNo[2])
					.equals(dbPhoneNo[0] + dbPhoneNo[1] + dbPhoneNo[2]))
				suLblState.setText("�̹� ���ԵǾ� �ִ� ��ȭ��ȣ �Դϴ�");
		} else {
			suLblState.setText("��밡���� ��ȭ��ȣ �Դϴ�");
			suPwfPut.setDisable(false);
			suPwfCheck.setDisable(false);
			suTfPhNo.setDisable(false);
			suBtnFindPhNo.setDisable(false);
			suTfUserNic.setDisable(false);
		}

	}

	// 03. ���Թ�ư. �׸� null�� Ȯ��, PWȮ��.
	private void handleSuBtnSignAction() {
		if (suTfUserId.getText().trim().equals(null)) {
			suLblState.setText("���̵� �Է����� �����̽��ϴ�");
		} else if (suPwfPut.getText().trim().equals(null) || suPwfCheck.getText().trim().equals(null)) {
			suLblState.setText("��й�ȣ�� �Է����� �����̽��ϴ�");
		} else if (!((suPwfPut.getText().trim().length() >= 8 && suPwfPut.getText().trim().length() <= 16)
				|| (suPwfCheck.getText().trim().length() >= 8 && suPwfCheck.getText().trim().length() <= 16))) {
			suLblState.setText("��й�ȣ�� 8�� �̻� 16�ڸ� �̳��� �Է��ϼ���");
		} else if (!suPwfPut.getText().trim().equals(suPwfCheck.getText().trim())) {
			suLblState.setText("��й�ȣ�� ��ġ���� �ʽ��ϴ�");
			suPwfCheck.setText(null);
		} else if (suTfPhNo.getText().trim().equals(null)) {
			suLblState.setText("��ȭ��ȣ�� �Է����� �����̽��ϴ�");
		} else if (!((suTfPhNo.getText().length() >= 11) && (suTfPhNo.getText().length() <= 13))) {
			suLblState.setText("��ȭ��ȣ ������ ���߾� �ּ���");
		} else if (suTfUserNic.getText().trim().equals(null)) {
			suLblState.setText("�г����� �Է����� �����̽��ϴ�");
		} else if (!suTfUserId.getText().trim().equals("") && !suPwfPut.getText().trim().equals("")
				&& !suPwfCheck.getText().trim().equals("") && !suTfPhNo.getText().trim().equals("")
				&& !suTfUserNic.getText().trim().equals("")) {

			User newUser = new User(suTfUserId.getText().trim(), suPwfPut.getText().trim(), suTfPhNo.getText().trim(),
					suTfUserNic.getText().trim(), null, null);
			int num = userDAO.insertSignUpUser(newUser);
			if (num == 1) {
				//callAlert("ȸ�����Կ� �����Ͽ����ϴ� : VaTalk ȸ���� �Ǽ̽��ϴ�.\n�α��� ���ּ���");
				try {
					Stage talkMainStage = new Stage(StageStyle.UTILITY);
					FXMLLoader loader = new FXMLLoader(getClass().getResource("../View/talk_login.fxml"));
					Parent talkMainRoot = loader.load();
					TalkLoginRootController talkMainController = loader.getController();
					talkMainController.talkLoginStage = talkMainStage;
					Scene talkMainScene = new Scene(talkMainRoot);
					talkMainScene.getStylesheets().add(getClass().getResource("../css/talk_login.css").toString());
					talkMainStage.setScene(talkMainScene);
					talkMainStage.setResizable(false);
					talkMainStage.setTitle("Va Talk LOGIN");
					talkSignUpStage.close();
					talkMainStage.show();
					//callAlert("ȭ�� ��ȯ ���� : Va�� ȭ������ ��ȯ �Ǿ����ϴ�.");
				} catch (Exception e) {
					callAlert("ȭ�� ��ȯ ���� : Va�� ȭ�� ��ȯ�� ������ �ֽ��ϴ�. ����ٶ�");
					e.printStackTrace();
				}
			} else if (num == 0) {
				suLblState.setText("ȸ�������� �ٽ� ���� ���ּ���");
				callAlert("ȸ������ ���� : ȸ�����Կ� ���� �Ͽ����ϴ�");
			}
		}
	}

	// 04. ���̵� ã�� �� hover
	private void handleSuLblFindIdAction() {
		Parent searchIdRoot = null;
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../View/talk_search_id.fxml"));
			searchIdRoot = loader.load();
			Stage srchIdStage = new Stage(StageStyle.UTILITY);
			srchIdStage.initModality(Modality.NONE);
			srchIdStage.initOwner(talkSignUpStage);
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

	// 05. �н����� ã�� �� hover
	private void handleSuLblFindPwAction() {
		Parent searchPwRoot = null;
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../View/talk_search_pw.fxml"));
			searchPwRoot = loader.load();
			Stage srchPwStage = new Stage(StageStyle.UTILITY);
			srchPwStage.initModality(Modality.NONE);
			srchPwStage.initOwner(talkSignUpStage);
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
			e.printStackTrace();
		}
	}

	// 06. ��ҹ�ư
	private void handleSuBtnCancelAction() {
		try {
			Stage talkMainStage = new Stage(StageStyle.UTILITY);
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../View/talk_login.fxml"));
			Parent talkMainRoot = loader.load();
			TalkLoginRootController talkMainController = loader.getController();
			talkMainController.talkLoginStage = talkMainStage;
			Scene talkMainScene = new Scene(talkMainRoot);
			talkMainScene.getStylesheets().add(getClass().getResource("../css/talk_login.css").toString());
			talkMainStage.setScene(talkMainScene);
			talkMainStage.setResizable(false);
			talkMainStage.setTitle("Va Talk LOGIN");
			talkSignUpStage.close();
			talkMainStage.show();
			//callAlert("ȭ�� ��ȯ ���� : Va�� ȭ������ ��ȯ �Ǿ����ϴ�.");
		} catch (Exception e) {
			callAlert("ȭ�� ��ȯ ���� : Va�� ȭ�� ��ȯ�� ������ �ֽ��ϴ�. ����ٶ�");
			e.printStackTrace();
		}
	}

	public static void callAlert(String contentText) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("�˸�â");
		alert.setHeaderText(contentText.substring(0, contentText.lastIndexOf(":")));
		alert.setContentText(contentText.substring(contentText.lastIndexOf(":") + 2));
		alert.showAndWait();
	}

}
