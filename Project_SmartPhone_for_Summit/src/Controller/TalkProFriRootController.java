package Controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import Model.Chatter;
import Model.Friends;
import Model.Room;
import Model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

public class TalkProFriRootController implements Initializable{

	@FXML private Label fssLblTime;	
	static final DateFormat CLOCK_FORMAT = new SimpleDateFormat("hh:mm:ss a");
	int count=0;

	static User user = TalkLoginRootController.user;
    User friend;
    User friendFromList;
	Friends friends;
	
	public Stage talkProFriStage;
	public Stage talkFriProfileStage;
	public Stage talkChattingRoomStage;
	public Stage talkSubEditUserStage;
	public Stage talkSubStageFindID;
	public Stage talkFindSubStage;
	public Stage talkSubChatListStage;
	
	@FXML private Button chLFriBtnFindFri;
	@FXML private Button chLFriBtnSettings;
	@FXML private HBox chLFriHBProf;
	@FXML private ImageView chLFriImgVUserPic;
	@FXML private Label chLFriLblUserNic;
	@FXML private ListView<User> chLFriListVFriend;
	@FXML private Button chLFriBtn2FriList;
	@FXML private Button chLFriBtn2ChatList;
	@FXML private Button chLFriBtnMakeGrp;
	
	UserDAO userDAO = new UserDAO();
	FriendsDAO friendsDAO = new FriendsDAO();
	RoomDAO roomDAO = new RoomDAO();
	static Chatter chatter=TalkLoginRootController.chatter;
	File file;
	ArrayList<User> joinner = new ArrayList<User>();
	ArrayList<User> groupChatJoinner = new ArrayList<User>();

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		setUpLocalTimeFSS();

		// 00. �α��ν� ������ ����
		user = userDAO.setUpUserInfo(user.getId());
		chatter = new Chatter(user);
		chatter.start();

		// 01. ����� �̹��� �г��� ����
		setUpUserPicNic();

		// 02. �ڽ��� �̹���, �г����� �ִ� HBox������ ������ ȸ������ ����â���� ����
		chLFriHBProf.setOnMouseClicked(eEditProfile -> {
			if (eEditProfile.getClickCount() == 2)
				handleHboxEditProfileAction();
		});

		// 03. ģ���߰�
		chLFriBtnFindFri.setOnAction(eFindFriend -> handleChLFriBtnFindFri());

		// 04. ����Ʈ�信 ģ�� ����
		setUpFriendList();

		chLFriListVFriend.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		// 05. ����Ʈ�信�� ����Ŭ���ϸ� ģ���� �������� ��
		chLFriListVFriend.setOnMouseClicked(eMakeNewChat -> {
			if (eMakeNewChat.getClickCount() == 2)
				handleClickedFriList();
		});

		// 06. ģ������� ������ ģ������Ʈ�� ����
		chLFriBtn2FriList.setOnAction(eGo2FriendList -> {
			setUpFriendList();
		});

		// 07. ä�ø���� ������ ä�� ����Ʈ�� �˾����� ���
		chLFriBtn2ChatList.setOnAction(eGo2ChattingList -> setUpChatList());

		// 08. ����Ʈ�信�� ���߼��� �� �游��� ��ư�� ������ ��ü���� �������
		chLFriBtnMakeGrp.setOnAction(eMakeJoinRoom -> handleMakeJoinRoom());

		// 09. �α׾ƿ�
		chLFriBtnSettings.setOnAction(eLogOut -> updateLogOutState());

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

	// 01. ����� �̹��� �г��� ����
	private void setUpUserPicNic() {
		chLFriLblUserNic.setText(user.getNicName());
		if (user.getImagePath() == null || user.getImagePath().equals("")) {
			//callAlert("�̹��� ���� : ��ϵ� ������ �����ϴ�");
		} else if (user.getImagePath() != null && !user.getImagePath().equals("")) {
			try {
				FileInputStream fileInputStream = new FileInputStream(user.getImagePath());
				chLFriImgVUserPic.setImage(new Image(fileInputStream));
			} catch (Exception e) {
				callAlert("�̹��� ��� ���� : �̹��� ��ΰ� �ùٸ��� �ʽ��ϴ�");
			}
		}
	}

	// 02. �ڽ��� �̹���, �г����� �ִ� HBox������ ������ ȸ������ ����â���� ����
	private void handleHboxEditProfileAction() {
		try {
			Stage talkSubEditUserStage = new Stage(StageStyle.UTILITY);
			this.talkSubEditUserStage = talkSubEditUserStage;
			FXMLLoader subEditUserLoader = new FXMLLoader(getClass().getResource("../View/talk_profile_edit.fxml"));
			Parent talkSubEditUserRoot = subEditUserLoader.load();
			Scene talkSubEditUserScene = new Scene(talkSubEditUserRoot);
			talkSubEditUserScene.getStylesheets().add(getClass().getResource("../css/talk_login.css").toString());
			talkSubEditUserStage.setScene(talkSubEditUserScene);
			talkSubEditUserStage.setResizable(false);
			talkSubEditUserStage.setTitle("Va Talk PROFILE EDIT");
			talkSubEditUserStage.show();

			ImageView pfeImgVUserPic = (ImageView) talkSubEditUserRoot.lookup("#pfeImgVUserPic");
			TextField pfeTfUserId = (TextField) talkSubEditUserRoot.lookup("#pfeTfUserId");
			PasswordField pfePwfPut = (PasswordField) talkSubEditUserRoot.lookup("#pfePwfPut");
			PasswordField pfePwfCheck = (PasswordField) talkSubEditUserRoot.lookup("#pfePwfCheck");
			TextField pfeTfPhNo = (TextField) talkSubEditUserRoot.lookup("#pfeTfPhNo");
			TextField pfeTfUserNic = (TextField) talkSubEditUserRoot.lookup("#pfeTfUserNic");
			Label pfeLblState = (Label) talkSubEditUserRoot.lookup("#pfeLblState");
			Button pfeBtnEdit = (Button) talkSubEditUserRoot.lookup("#pfeBtnEdit");
			Button pfeBtnExit = (Button) talkSubEditUserRoot.lookup("#pfeBtnExit");
			Label pfeLblDeleteUser = (Label) talkSubEditUserRoot.lookup("#pfeLblDeleteUser");

			pfeTfUserId.setText(user.getId());
			pfeTfUserId.setDisable(true);
			pfePwfPut.setText(user.getPw());
			pfePwfCheck.setText(user.getPw());
			pfeTfPhNo.setText(user.getPhoneNo());
			pfeTfPhNo.setDisable(true);
			pfeTfUserNic.setText(user.getNicName());

			if (user.getImagePath() == null || user.getImagePath().equals("")) {
				//callAlert("�̹��� ���� : ��ϵ� ������ �����ϴ�");
			} else if (user.getImagePath() != null && !user.getImagePath().equals("")) {
				try {
					FileInputStream fileInputStream = new FileInputStream(user.getImagePath());
					pfeImgVUserPic.setImage(new Image(fileInputStream));
				} catch (Exception e) {
					callAlert("�̹��� ��� ���� : �̹��� ��ΰ� �ùٸ��� �ʽ��ϴ�");
				}
			}

			pfeImgVUserPic.setOnMouseClicked(eEidtPic -> {
				if (eEidtPic.getClickCount() == 2) {
					try {
						FileChooser fileChooser = new FileChooser();
						fileChooser.getExtensionFilters().add(new ExtensionFilter("JPG", "*.jpg"));
						fileChooser.getExtensionFilters().add(new ExtensionFilter("JPEG", "*.jpeg"));
						fileChooser.getExtensionFilters().add(new ExtensionFilter("PNG", "*.png"));
						fileChooser.getExtensionFilters().add(new ExtensionFilter("BMP", "*.bmp"));
						fileChooser.getExtensionFilters().add(new ExtensionFilter("GIF", "*.gif"));
						fileChooser.getExtensionFilters().add(new ExtensionFilter("TIF", "*.tif"));
						fileChooser.getExtensionFilters().add(new ExtensionFilter("�������", "*.*"));
						file = fileChooser.showOpenDialog(talkSubEditUserStage);
						if (file != null) {
							user.setImagePath(file.getPath());
							FileInputStream fileInputStream = new FileInputStream(user.getImagePath());
							pfeImgVUserPic.setImage(new Image(fileInputStream));
						}

					} catch (Exception e) {
						callAlert("�̹��� ���� �ε� ���� : �̹��� ������ Ȯ���ϼ���");
						e.printStackTrace();
					}
				}
			});

			talkSubEditUserStage.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent eEditSavePWNicImgPath) -> {
				if (eEditSavePWNicImgPath.getCode() == KeyCode.ENTER) {
					if (pfePwfPut.getText().trim().equals(null) || pfePwfCheck.getText().trim().equals(null)) {
						pfeLblState.setText("��й�ȣ�� �Է����� �����̽��ϴ�");
					} else if (!((pfePwfPut.getText().trim().length() >= 8 && pfePwfPut.getText().trim().length() <= 16)
							|| (pfePwfCheck.getText().trim().length() >= 8
									&& pfePwfCheck.getText().trim().length() <= 16))) {
						pfeLblState.setText("��й�ȣ�� 8�� �̻� 16�ڸ� �̳��� �Է��ϼ���");
					} else if (!pfePwfPut.getText().trim().equals(pfePwfCheck.getText().trim())) {
						pfeLblState.setText("��й�ȣ�� ��ġ���� �ʽ��ϴ�");
						pfePwfCheck.setText(null);
					} else if (pfeTfUserNic.getText().trim().equals(null)) {
						pfeLblState.setText("�г����� �Է����� �����̽��ϴ�");
					} else if (!pfeTfUserId.getText().trim().equals("") && !pfePwfPut.getText().trim().equals("")
							&& !pfePwfCheck.getText().trim().equals("") && !pfeTfPhNo.getText().trim().equals("")
							&& !pfeTfUserNic.getText().trim().equals("")) {

						user.setPw(pfePwfPut.getText().trim());
						user.setNicName(pfeTfUserNic.getText().trim());

						int flag = userDAO.updateUserEdit(user);
						if (flag == 1) {
							chatter.setUser(user);
							pfeLblState.setText("������ �����߽��ϴ�");
							//callAlert("DB �Է� ���� : ���� �� ������ DB�� �����߽��ϴ�");
						} else {
							pfeLblState.setText("���� ������ �ٽ� ���� ���ּ���");
							callAlert("DB �Է� ���� : ���� �� ������ DB�� �������� �� �߽��ϴ�");
						}
					}
				}
			});

			pfeBtnEdit.setOnAction(eEditSavePWNicImgPath -> {
				if (pfePwfPut.getText().trim().equals(null) || pfePwfCheck.getText().trim().equals(null)) {
					pfeLblState.setText("��й�ȣ�� �Է����� �����̽��ϴ�");
				} else if (!((pfePwfPut.getText().trim().length() >= 8 && pfePwfPut.getText().trim().length() <= 16)
						|| (pfePwfCheck.getText().trim().length() >= 8
								&& pfePwfCheck.getText().trim().length() <= 16))) {
					pfeLblState.setText("��й�ȣ�� 8�� �̻� 16�ڸ� �̳��� �Է��ϼ���");
				} else if (!pfePwfPut.getText().trim().equals(pfePwfCheck.getText().trim())) {
					pfeLblState.setText("��й�ȣ�� ��ġ���� �ʽ��ϴ�");
					pfePwfCheck.setText(null);
				} else if (pfeTfUserNic.getText().trim().equals(null)) {
					pfeLblState.setText("�г����� �Է����� �����̽��ϴ�");
				} else if (!pfeTfUserId.getText().trim().equals("") && !pfePwfPut.getText().trim().equals("")
						&& !pfePwfCheck.getText().trim().equals("") && !pfeTfPhNo.getText().trim().equals("")
						&& !pfeTfUserNic.getText().trim().equals("")) {

					user.setPw(pfePwfPut.getText().trim());
					user.setNicName(pfeTfUserNic.getText().trim());

					int flag = userDAO.updateUserEdit(user);
					if (flag == 1) {
						chatter.setUser(user);
						pfeLblState.setText("������ �����߽��ϴ�");
						//callAlert("DB �Է� ���� : ���� �� ������ DB�� �����߽��ϴ�");
					} else {
						pfeLblState.setText("���� ������ �ٽ� ���� ���ּ���");
						callAlert("DB �Է� ���� : ���� �� ������ DB�� �������� �� �߽��ϴ�");
					}
				}
			});

			pfeBtnExit.setOnAction(eBack2Profile -> talkSubEditUserStage.close());

			talkSubEditUserStage.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent eClose) -> {
				if (eClose.getCode() == KeyCode.ESCAPE)
					talkSubEditUserStage.hide();
			});

			pfeLblDeleteUser.setOnMouseClicked(eDeleteUser -> {
				if (eDeleteUser.getClickCount() == 2) {
					pfeLblState.setText("���� Ż�� �Ͻðڽ��ϱ�?\nŻ�� �Ͻ÷��� Ż���ϱ⸦ �ѹ� �� ��������");
					pfeLblDeleteUser.setOnMouseClicked(eDelete -> {
						userDAO.deleteUser(user.getId());
						chatter.setSocket(null);
						chatter.setUser(null);
						try {
							Stage talkMainStage = new Stage(StageStyle.UTILITY);
							FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("../View/talk_login.fxml"));
							Parent talkMainRoot = loginLoader.load();
							TalkLoginRootController talkMainController = loginLoader.getController();
							talkMainController.talkLoginStage = talkMainStage;
							Scene talkMainScene = new Scene(talkMainRoot);
							talkMainScene.getStylesheets()
									.add(getClass().getResource("../css/talk_login.css").toString());
							talkMainStage.setScene(talkMainScene);
							talkMainStage.setResizable(false);
							talkMainStage.setTitle("Va Talk LOGIN");
							talkSubEditUserStage.close();
							talkMainStage.show();
							//callAlert("ȭ�� ��ȯ ���� : Va�� ȭ������ ��ȯ �Ǿ����ϴ�.");
						} catch (Exception e) {
							callAlert("ȭ�� ��ȯ ���� : Va�� ȭ�� ��ȯ�� ������ �ֽ��ϴ�. ����ٶ�");
							e.printStackTrace();
						}
					});
				}
			});

		} catch (Exception e) {
			callAlert("ȭ�� ��ȯ ���� : PROFILE EDIT ȭ�� ��ȯ�� ������ �ֽ��ϴ�. ����ٶ�");
			e.printStackTrace();
		}
	}

	// 03. ģ���߰�
	private void handleChLFriBtnFindFri() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../View/talk_select_howto_find.fxml"));
			Parent talkSubRoot = loader.load();
			Stage talkSubStage = new Stage(StageStyle.UNDECORATED);
			this.talkSubStageFindID = talkSubStage;
			talkSubStage.initModality(Modality.NONE);
			talkSubStage.initOwner(talkProFriStage);
			talkSubStage.setResizable(false);
			Scene talkSubScene = new Scene(talkSubRoot);
			talkSubScene.getStylesheets().add(getClass().getResource("../css/talk_login.css").toString());
			talkSubStage.setScene(talkSubScene);
			talkSubStage.show();

			AnchorPane imageAPane = (AnchorPane) talkSubRoot.lookup("#imageAPane");
			Button h2FindBtnAddrB = (Button) talkSubRoot.lookup("#h2FindBtnAddrB");
			Button h2FindBtnId = (Button) talkSubRoot.lookup("#h2FindBtnId");

			imageAPane.setOnMouseClicked(eExit -> {
				if (eExit.getClickCount() == 2)
					talkSubStage.close();
			});
			talkSubStage.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent eClose) -> {
				if (eClose.getCode() == KeyCode.ESCAPE)
					talkSubStage.close();
			});

			h2FindBtnAddrB.setOnAction(eFindByAddressBook -> {
				// ������ �ڵ��� DB���� ���� �����ͼ� ������ DB�� ��
			});

			h2FindBtnId.setOnAction(eFindById -> {
				talkSubStage.close();
				Parent talkFindSubRoot = null;
				try {
					FXMLLoader findLoader = new FXMLLoader(getClass().getResource("../View/talk_find_friend_id.fxml"));
					talkFindSubRoot = findLoader.load();
					Stage talkFindSubStage = new Stage(StageStyle.UNDECORATED);
					this.talkFindSubStage = talkFindSubStage;
					talkFindSubStage.initModality(Modality.NONE);
					talkFindSubStage.initOwner(talkSubStage);
					talkFindSubStage.setResizable(false);
					Scene talkFindSubScene = new Scene(talkFindSubRoot);
					talkFindSubScene.getStylesheets().add(getClass().getResource("../css/talk_login.css").toString());
					talkFindSubStage.setScene(talkFindSubScene);
					talkFindSubStage.show();

					AnchorPane findIdAPane = (AnchorPane) talkFindSubRoot.lookup("#findIdAPane");
					ImageView findFriImgVFri = (ImageView) talkFindSubRoot.lookup("#findFriImgVFri");
					Label findFriLblState = (Label) talkFindSubRoot.lookup("#findFriLblState");
					TextField findFriTfId = (TextField) talkFindSubRoot.lookup("#findFriTfId");
					Button findFriBtnFindId = (Button) talkFindSubRoot.lookup("#findFriBtnFindId");
					Button findFriBtnAddFri = (Button) talkFindSubRoot.lookup("#findFriBtnAddFri");

					findIdAPane.setOnMouseClicked(eFindExit -> {
						if (eFindExit.getClickCount() == 2) {
							talkSubStage.close();
							talkFindSubStage.close();
						}
					});

					talkFindSubStage.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent eFindClose) -> {
						if (eFindClose.getCode() == KeyCode.ESCAPE) {
							talkSubStage.close();
							talkFindSubStage.close();
						}
					});

					findFriTfId.setOnKeyPressed(eFindId -> {
						if (eFindId.getCode() == KeyCode.ENTER) {
							if (findFriTfId.getText().trim().equals("")) {
								findFriLblState.setText("�˻� �� ���̵� �Է��ϼ���");
							} else if (!(findFriTfId.getText().contains("@") && findFriTfId.getText().contains("."))) {
								findFriLblState.setText("Use Email Address\n�̸��� ������ �ƴմϴ�");
							} else {

								User userFriend = userDAO.selectFindFriend(findFriTfId.getText().trim());
								if (userFriend != null) {
									if (userFriend.getImagePath() == null || userFriend.getImagePath().equals("")) {
										try {
											FileInputStream fileInputStream = new FileInputStream(
											"D:\\JAVA_PROJECT\\Project_SmartPhone\\src\\icons\\27. ģ��\\ģ�� 00-1.png");
											findFriImgVFri.setImage(new Image(fileInputStream));
										} catch (Exception e) {
											callAlert("�̹��� ��� ���� : �̹��� ��ΰ� �ùٸ��� �ʽ��ϴ�");
										}
									} else if (userFriend.getImagePath() != null
											&& !userFriend.getImagePath().equals("")) {
										try {
											FileInputStream fileInputStream = new FileInputStream(
													userFriend.getImagePath());
											findFriImgVFri.setImage(new Image(fileInputStream));
										} catch (Exception e) {
											callAlert("�̹��� ��� ���� : �̹��� ��ΰ� �ùٸ��� �ʽ��ϴ�");
										}
									}
									Friends userFriendCheck = friendsDAO.searchAddAlready(user.getId(),
											userFriend.getId());
									if (userFriendCheck != null)
										findFriLblState.setText("�̹� ģ���� ȸ���Դϴ�\n�ٸ� ģ���� ã���ּ���");
									else {
										findFriLblState.setText("ã���ô� ģ���� ������ �߰��� �����ּ���");

										findFriBtnAddFri.setOnAction(eAddFriend -> {
											Friends friends = new Friends(user.getId(), userFriend.getId());
											int flag = friendsDAO.insertFriends(friends);
											if (flag == 1) {
												talkFindSubStage.close();
												//callAlert("ģ�� �߰� ���� : DB�� ģ���� �߰� �Ͽ����ϴ�");
												setUpFriendList();
												talkSubStage.close();
												talkFindSubStage.close();
											} else
												callAlert("ģ�� �߰� ���� : DB�� ģ���� �߰����� �� �Ͽ����ϴ�");

										});
									}
								} else {
									findFriLblState.setText("ã���ô� ȸ���� �����ϴ�");
									findFriTfId.clear();
								} // else �� �Է�o
							} // else �� �Է�x
						}
					});

					findFriBtnFindId.setOnAction(eFindId -> {

						if (findFriTfId.getText().trim().equals("")) {
							findFriLblState.setText("�˻� �� ���̵� �Է��ϼ���");
						} else if (!(findFriTfId.getText().contains("@") && findFriTfId.getText().contains("."))) {
							findFriLblState.setText("Use Email Address\n�̸��� ������ �ƴմϴ�");
						} else {

							User userFriend = userDAO.selectFindFriend(findFriTfId.getText().trim());
							if (userFriend != null) {
								if (userFriend.getImagePath() == null || userFriend.getImagePath().equals("")) {
									try {
										FileInputStream fileInputStream = new FileInputStream(
										"D:\\JAVA_PROJECT\\Project_SmartPhone\\src\\icons\\27. ģ��\\ģ�� 00-1.png");
										findFriImgVFri.setImage(new Image(fileInputStream));
									} catch (Exception e) {
										callAlert("�̹��� ��� ���� : �̹��� ��ΰ� �ùٸ��� �ʽ��ϴ�");
									}
								} else if (userFriend.getImagePath() != null && !userFriend.getImagePath().equals("")) {
									try {
										FileInputStream fileInputStream = new FileInputStream(
												userFriend.getImagePath());
										findFriImgVFri.setImage(new Image(fileInputStream));
									} catch (Exception e) {
										callAlert("�̹��� ��� ���� : �̹��� ��ΰ� �ùٸ��� �ʽ��ϴ�");
									}
								}
								
								Friends userFriendCheck = friendsDAO.searchAddAlready(user.getId(), userFriend.getId());
								
								if (userFriendCheck != null)
									findFriLblState.setText("�̹� ģ���� ȸ���Դϴ�\n�ٸ� ģ���� ã���ּ���");
								else {
									findFriLblState.setText("ã���ô� ģ���� ������ �߰��� �����ּ���");

									findFriBtnAddFri.setOnAction(eAddFriend -> {
										Friends friends = new Friends(user.getId(), userFriend.getId());
										int flag = friendsDAO.insertFriends(friends);
										if (flag == 1) {
											talkFindSubStage.close();
											//callAlert("ģ�� �߰� ���� : DB�� ģ���� �߰� �Ͽ����ϴ�");
											setUpFriendList();
											talkSubStage.close();
											talkFindSubStage.close();
										} else
											callAlert("ģ�� �߰� ���� : DB�� ģ���� �߰����� �� �Ͽ����ϴ�");
									});
								}
							} else {
								findFriLblState.setText("ã���ô� ȸ���� �����ϴ�");
								findFriTfId.clear();
							} // else �� �Է�o
						} // else �� �Է�x

					});
				} catch (Exception e) {
					callAlert("ȭ�� ��ȯ ���� : PROFILE EDIT ȭ�� ��ȯ�� ������ �ֽ��ϴ�. ����ٶ�");
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			callAlert("ȭ�� ��ȯ ���� : PROFILE EDIT ȭ�� ��ȯ�� ������ �ֽ��ϴ�. ����ٶ�");
			e.printStackTrace();
		}

	}

	// 04. ����Ʈ�信 ģ�� ���� // 06. ģ������� ������ ģ������Ʈ�� ����
	private void setUpFriendList() {
		ObservableList<User> obFriList = FXCollections.observableArrayList();
		for (User friend : friendsDAO.setUpFriendsList(user.getId())) {
			obFriList.add(new User(friend.getId(), friend.getNicName(), friend.getImagePath(), friend.getLogOn()));
		}
		chLFriListVFriend.setItems(obFriList);
		chLFriListVFriend.setCellFactory(new Callback<ListView<User>, ListCell<User>>() {
			@Override
			public ListCell<User> call(ListView<User> param) {
				return new ListCell<User>() {
					@Override
					protected void updateItem(User user, boolean empty) {
						super.updateItem(user, empty);

						if (user == null || empty) {
							setText(null);
							setGraphic(null);
						} else {

							HBox cellRoot = new HBox(10);
							cellRoot.setAlignment(Pos.CENTER_LEFT);
							cellRoot.setPadding(new Insets(5));

							FileInputStream fileInputStream;
							try {
								if (user.getImagePath() == null || user.getImagePath().equals("")) {
									fileInputStream = new FileInputStream(
									"D:\\JAVA_PROJECT\\Project_SmartPhone\\src\\icons\\27. ģ��\\ģ�� 00-1.png");
									ImageView imgProfilePic = new ImageView(new Image(fileInputStream));
									imgProfilePic.setFitHeight(50);
									imgProfilePic.setFitWidth(50);

									cellRoot.getChildren().add(imgProfilePic);

									cellRoot.getChildren().add(new Separator(Orientation.VERTICAL));

									VBox vBox = new VBox(5);
									vBox.setAlignment(Pos.CENTER_LEFT);
									vBox.setPadding(new Insets(5));

									vBox.getChildren().addAll(new Label("On/Off : " + user.getLogOn()),
											new Label("NicName : " + user.getNicName()));

									cellRoot.getChildren().add(vBox);

									setGraphic(cellRoot);
								} else if (user.getImagePath() != null && !user.getImagePath().equals("")) {
									fileInputStream = new FileInputStream(user.getImagePath());
									ImageView imgProfilePic = new ImageView(new Image(fileInputStream));
									imgProfilePic.setFitHeight(50);
									imgProfilePic.setFitWidth(50);

									cellRoot.getChildren().add(imgProfilePic);

									cellRoot.getChildren().add(new Separator(Orientation.VERTICAL));

									VBox vBox = new VBox(5);
									vBox.setAlignment(Pos.CENTER_LEFT);
									vBox.setPadding(new Insets(5));

									vBox.getChildren().addAll(new Label("On/Off : " + user.getLogOn()),
											new Label("NicName : " + user.getNicName()));

									cellRoot.getChildren().add(vBox);

									setGraphic(cellRoot);
								}
							} catch (FileNotFoundException e) {
								callAlert("�̹��� ��� ���� : �̹��� ��ΰ� �ùٸ��� �ʽ��ϴ�");
								e.printStackTrace();
							}
						}
					}
				};
			}
		});
	}

	// 05. ����Ʈ�信�� ����Ŭ���ϸ� ģ���� �������� ��
	private void handleClickedFriList() {
		friend = chLFriListVFriend.getSelectionModel().getSelectedItem();
		try {
			Stage talkFriProfileStage = new Stage(StageStyle.UTILITY);
			this.talkFriProfileStage = talkFriProfileStage;
			FXMLLoader friProfileLoader = new FXMLLoader(getClass().getResource("../View/talk_friend_profile.fxml"));
			Parent talkFriProfileRoot = friProfileLoader.load();
			Scene talkFriProfileScene = new Scene(talkFriProfileRoot);
			talkFriProfileScene.getStylesheets().add(getClass().getResource("../css/talk_login.css").toString());
			talkFriProfileStage.setScene(talkFriProfileScene);
			talkFriProfileStage.setResizable(false);
			talkFriProfileStage.setTitle("Va Talk FRIEND PROFILE");
			talkFriProfileStage.show();

			ImageView fpImgVFriPic = (ImageView) talkFriProfileRoot.lookup("#fpImgVFriPic");
			TextField fpTfFriId = (TextField) talkFriProfileRoot.lookup("#fpTfFriId");
			TextField fpTfFriPhNo = (TextField) talkFriProfileRoot.lookup("#fpTfFriPhNo");
			TextField fpTfFriNic = (TextField) talkFriProfileRoot.lookup("#fpTfFriNic");
			Label fpLblState = (Label) talkFriProfileRoot.lookup("#fpLblState");
			Button fpBtnChat = (Button) talkFriProfileRoot.lookup("#fpBtnChat");
			Button fpBtnExit = (Button) talkFriProfileRoot.lookup("#fpBtnExit");
			Label fpLblDeleteFri = (Label) talkFriProfileRoot.lookup("#fpLblDeleteFri");

			friend = userDAO.setUpUserInfo(friend.getId());
			fpTfFriId.setText(friend.getId());
			fpTfFriId.setDisable(true);
			fpTfFriPhNo.setText(friend.getPhoneNo());
			fpTfFriPhNo.setDisable(true);
			fpTfFriNic.setText(friend.getNicName());
			fpTfFriNic.setDisable(true);

			if (friend.getImagePath() == null || friend.getImagePath().equals("")) {
				//callAlert("�̹��� ���� : ��ϵ� ������ �����ϴ�");
			} else if (friend.getImagePath() != null && !friend.getImagePath().equals("")) {
				try {
					FileInputStream fileInputStream = new FileInputStream(friend.getImagePath());
					fpImgVFriPic.setImage(new Image(fileInputStream));
				} catch (Exception e) {
					callAlert("�̹��� ��� ���� : �̹��� ��ΰ� �ùٸ��� �ʽ��ϴ�");
				}
			}

			fpBtnChat.setOnAction(eGo2Chattingroom -> {
				try {
					Stage talkChattingStage = new Stage(StageStyle.UTILITY);
					this.talkChattingRoomStage = talkChattingStage;
					FXMLLoader ChattingLoader = new FXMLLoader(
							getClass().getResource("../View/talk_chatting_room.fxml"));
					Parent talkChattingRoot = ChattingLoader.load();
					TalkPrivChattingRoomController talkMainController = ChattingLoader.getController();
					talkMainController.setTalkChattingRoomStage(talkChattingStage);
					talkMainController.setFriend(friend);
					talkMainController.setFriendImagePath(friend.getImagePath());
					talkMainController.setFriendNicName(friend.getNicName());
					Scene talkChattingScene = new Scene(talkChattingRoot);
					talkChattingScene.getStylesheets()
							.add(getClass().getResource("../css/talk_chatting.css").toString());
					talkChattingStage.setScene(talkChattingScene);
					talkChattingStage.setResizable(false);
					talkChattingStage.setTitle("Va Talk CHATTING ROOM");
					talkFriProfileStage.close();
					talkChattingStage.show();
				} catch (Exception e) {
					e.printStackTrace();
				}

				Room room1 = roomDAO.searchRoomAlready(user.getId(), friend.getId());
				Room room2 = roomDAO.searchRoomAlready(friend.getId(), user.getId());
				if (room1 == null && room2 == null) {
					int makePrivRoom = roomDAO.makePrivRoom(user.getId(), friend.getId());
					if (makePrivRoom == 1) {
						//callAlert("�� ����� ���� : DB�� ä�ù��� ������Ʈ �Ǿ����ϴ�.");
						talkFriProfileStage.close();
					} else
						callAlert("�� ����� ���� : DB�� ä�ù��� ������Ʈ ���� �� �߽��ϴ�.");
				} else if (room1 != null && !room1.getRoomName().contains(",")) {
					int makePrivRoom = roomDAO.makePrivRoom(user.getId(), friend.getId());
					if (makePrivRoom == 1) {
						//callAlert("�� ����� ���� : DB�� ä�ù��� ������Ʈ �Ǿ����ϴ�.");
						talkFriProfileStage.close();
					} else
						callAlert("�� ����� ���� : DB�� ä�ù��� ������Ʈ ���� �� �߽��ϴ�.");
				} else if (room2 != null && !room2.getRoomName().contains(",")) {
					int makePrivRoom = roomDAO.makePrivRoom(user.getId(), friend.getId());
					if (makePrivRoom == 1) {
						//callAlert("�� ����� ���� : DB�� ä�ù��� ������Ʈ �Ǿ����ϴ�.");
						talkFriProfileStage.close();
					} else
						callAlert("�� ����� ���� : DB�� ä�ù��� ������Ʈ ���� �� �߽��ϴ�.");
				} else {
					callAlert("�� ����� ���� : �̹� ���� �� ���Դϴ�");
				}
			});

			fpBtnExit.setOnAction(eBack2Profile -> talkFriProfileStage.close());

			talkFriProfileStage.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent eClose) -> {
				if (eClose.getCode() == KeyCode.ESCAPE)
					talkFriProfileStage.hide();
			});

			fpLblDeleteFri.setOnMouseClicked(eDeleteUser -> {
				if (eDeleteUser.getClickCount() == 2) {
					fpLblState.setText("���� ģ���� ���� �Ͻðڽ��ϱ�?\nģ�������� �ѹ� �� ������ �����˴ϴ�");
					fpLblDeleteFri.setOnMouseClicked(eDelete -> {
						friendsDAO.deleteFriend(user.getId(), friend.getId());
						setUpFriendList();
						talkFriProfileStage.close();
					});
				}
			});
		} catch (Exception e) {
			callAlert("ȭ�� ��ȯ ���� : FRIEND PROFILE ȭ�� ��ȯ�� ������ �ֽ��ϴ�. ����ٶ�");
			e.printStackTrace();
		}
	}

	// 07. ä�ø���� ������ ä�� ����Ʈ�� �˾����� ���
	private void setUpChatList() {
		try {
			Stage talkSubChatListStage = new Stage(StageStyle.UTILITY);
			this.talkSubChatListStage = talkSubChatListStage;
			FXMLLoader subChatListLoader = new FXMLLoader(getClass().getResource("../View/talk_chat_list.fxml"));
			Parent talkSubChatListRoot = subChatListLoader.load();
			Scene talkSubChatListScene = new Scene(talkSubChatListRoot);
			talkSubChatListScene.getStylesheets().add(getClass().getResource("../css/talk_login.css").toString());
			talkSubChatListStage.setScene(talkSubChatListScene);
			talkSubChatListStage.setResizable(false);
			talkSubChatListStage.setTitle("Va Talk CHATLIST");
			talkSubChatListStage.show();
			ListView<String> chLFriListVChatter = (ListView) talkSubChatListRoot.lookup("#chLFriListVChatter");
			Button chLFriBtn2FriList = (Button) talkSubChatListRoot.lookup("#chLFriBtn2FriList");

			chLFriBtn2FriList.setOnAction(eClose -> talkSubChatListStage.close());

			ObservableList<String> obChatRoomList = FXCollections.observableArrayList();
			for (String room : roomDAO.setUpChatRoomList(user.getId())) {
				obChatRoomList.add(room);
			}

			chLFriListVChatter.setItems(obChatRoomList);
			chLFriListVChatter.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
				@Override
				public ListCell<String> call(ListView<String> param) {
					return new ListCell<String>() {
						@Override
						protected void updateItem(String room, boolean empty) {
							super.updateItem(room, empty);

							if (room == null || empty) {
								setText(null);
								setGraphic(null);
							} else {
								HBox cellRoot = new HBox(10);
								cellRoot.setAlignment(Pos.CENTER_LEFT);
								cellRoot.setPadding(new Insets(5));
								FileInputStream fileInputStream;
								try {
									fileInputStream = new FileInputStream(
									"D:\\JAVA_PROJECT\\Project_SmartPhone\\src\\icons\\13. message, ���� ��ƺ���,chatting\\message 03.png");
									ImageView imgProfilePic = new ImageView(new Image(fileInputStream));
									imgProfilePic.setFitHeight(50);
									imgProfilePic.setFitWidth(50);
									cellRoot.getChildren().add(imgProfilePic);
									cellRoot.getChildren().add(new Separator(Orientation.VERTICAL));
									VBox vBox = new VBox(5);
									vBox.setAlignment(Pos.CENTER_LEFT);
									vBox.setPadding(new Insets(5));
									String category = null;

									if (room.contains(","))
										category = "Private Room";
									else
										category = "Join Room";
									vBox.getChildren().addAll(new Label(category), new Label(room));

									cellRoot.getChildren().add(vBox);
									setGraphic(cellRoot);
								} catch (FileNotFoundException e) {
									callAlert("�̹��� ��� ���� : �̹��� ��ΰ� �ùٸ��� �ʽ��ϴ�");
									e.printStackTrace();
								}
							}
						}
					};
				}
			});
			
			chLFriListVChatter.setOnMouseClicked(eChatting -> {

				String roomName = chLFriListVChatter.getSelectionModel().getSelectedItem();

				if (roomName.contains(",")) {
					Friends friends = roomDAO.findPrivRoomJoinner(roomName);
					if (friends.getUserID().equals(user.getId())) {
						friendFromList = userDAO.setUpUserInfo(friends.getCounterID());
					} else {
						friendFromList = userDAO.setUpUserInfo(friends.getUserID());
					}

					try {
						Stage talkChattingStage = new Stage(StageStyle.UTILITY);
						this.talkChattingRoomStage = talkChattingStage;
						FXMLLoader ChattingLoader = new FXMLLoader(
								getClass().getResource("../View/talk_chatting_room.fxml"));
						Parent talkChattingRoot = ChattingLoader.load();
						TalkPrivChattingRoomController talkMainController = ChattingLoader.getController();
						talkMainController.talkChattingRoomStage = talkChattingStage;
						talkMainController.setFriend(friendFromList);
						talkMainController.setFriendImagePath(friendFromList.getImagePath());
						talkMainController.setFriendNicName(friendFromList.getNicName());
						Scene talkChattingScene = new Scene(talkChattingRoot);
						talkChattingScene.getStylesheets()
								.add(getClass().getResource("../css/talk_chatting.css").toString());
						talkChattingStage.setScene(talkChattingScene);
						talkChattingStage.setResizable(false);
						talkChattingStage.setTitle("Va Talk CHATTING ROOM");
						talkSubChatListStage.close();
						talkChattingStage.show();

					} catch (Exception e) {
						e.printStackTrace();
					}

				} else if (!roomName.contains(",")) {

					try {
						Stage talkChattingStage = new Stage(StageStyle.UTILITY);
						this.talkChattingRoomStage = talkChattingStage;
						FXMLLoader ChattingLoader = new FXMLLoader(
								getClass().getResource("../View/talk_chatting_room2.fxml"));
						Parent talkChattingRoot = ChattingLoader.load();
						TalkGroupChattingRoomController talkMainController = ChattingLoader.getController();
						talkMainController.talkChattingRoomStage = talkChattingStage;
						talkMainController.setRoomName(roomName);
						Scene talkChattingScene = new Scene(talkChattingRoot);
						talkChattingScene.getStylesheets()
								.add(getClass().getResource("../css/talk_chatting.css").toString());
						talkChattingStage.setScene(talkChattingScene);
						talkChattingStage.setResizable(false);
						talkChattingStage.setTitle("Va Talk CHATTING ROOM");
						talkSubChatListStage.close();
						talkChattingStage.show();

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 08. ����Ʈ�信�� ���߼��� �� �游��� ��ư�� ������ ��ü���� �������
	private void handleMakeJoinRoom() {

		/************ ��������� ��� ���� ��� �ߺ����� ���� ��������� ��� üũ�ϴ� ���� �߰��� �� *************/

		joinner.addAll(chLFriListVFriend.getSelectionModel().getSelectedItems());
		String groupRoomName = null;
		for (User joinner : joinner) {
			groupRoomName += joinner.getId() + " ";
		}
		groupRoomName = groupRoomName.substring(4) + user.getId();
		for (User joinner : joinner) {
			roomDAO.makeJoinRoom(user.getId(), groupRoomName, joinner.getId());
		}
	}

	// 09. �α׾ƿ�
	private void updateLogOutState() {

		handleSetLogOffAction();
		handleSetIpPortOffAction();

		talkProFriStage.close();
		if (this.talkFriProfileStage != null)
			talkFriProfileStage.close();
		if (this.talkChattingRoomStage != null)
			talkChattingRoomStage.close();
		if (this.talkSubEditUserStage != null)
			talkSubEditUserStage.close();
		if (this.talkSubStageFindID != null)
			talkSubStageFindID.close();
		if (this.talkFindSubStage != null)
			talkFindSubStage.close();
		if (this.talkSubChatListStage != null)
			talkSubChatListStage.close();
		chatter.stopClient();

	}

	private void handleSetLogOffAction() {
		int logOn = userDAO.updateUserLogOn(user, "off");
//		if (logOn == 1)
//			callAlert("�α׿��� ���� : DB�� �α׿��� ���·� ������Ʈ �Ǿ����ϴ�.");
//		else
//			callAlert("�α׿��� ���� : DB�� �α��� ���� ������� �ʾҽ��ϴ�.");
		if (logOn != 1)
			callAlert("�α׿��� ���� : DB�� �α��� ���� ������� �ʾҽ��ϴ�.");
	}

	private void handleSetIpPortOffAction() {
		int updateIP = userDAO.updateIpPort(user.getId(), null, null);
//		if (updateIP == 1)
//			callAlert("INSERT ���� : DB�� IP�� PORT�� ������Ʈ �Ǿ����ϴ�.");
//		else
//			callAlert("INSERT ���� : DB�� IP�� PORT ������Ʈ�� ���� �Ͽ����ϴ�.");
		if (updateIP != 1)
			callAlert("INSERT ���� : DB�� IP�� PORT ������Ʈ�� ���� �Ͽ����ϴ�.");
	}

	public static void callAlert(String contentText) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("�˸�â");
		alert.setHeaderText(contentText.substring(0, contentText.lastIndexOf(":")));
		alert.setContentText(contentText.substring(contentText.lastIndexOf(":") + 1));
		alert.showAndWait();
	}

}
