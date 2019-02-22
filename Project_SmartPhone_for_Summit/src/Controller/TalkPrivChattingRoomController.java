package Controller;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import Model.Room;
import Model.User;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class TalkPrivChattingRoomController implements Initializable{

	@FXML private Label fssLblTime;	
	static final DateFormat CLOCK_FORMAT = new SimpleDateFormat("hh:mm:ss a");
	int count=0;

	public Stage talkChattingRoomStage;
	public void setTalkChattingRoomStage(Stage talkChattingRoomStage) {
		this.talkChattingRoomStage = talkChattingRoomStage;
	}
	
	Socket socket = TalkProFriRootController.chatter.getSocket();
	@FXML private Button chRmBtn2Back;
	@FXML private Button chRmBtnDeletePrivRm;
	@FXML private ImageView chRmImgVCounterPic;
	@FXML private Label chRmLblCounterNic;
	@FXML private TextArea chRmTa;
	@FXML private Button chRmBtnGame;
	@FXML private Button chRmBtnSltPic;
	@FXML private Button chRmBtnSltFile;
	@FXML private Button chRmBtnSltAddr;
	@FXML private TextField chRmTf;
	@FXML private Button chRmBtnSend;
	
	private ExecutorService executorService;
	String friendImagePath;
	String friendNicName;
	UserDAO userDAO = new UserDAO();
	FriendsDAO friendsDAO = new FriendsDAO();
	RoomDAO roomDAO = new RoomDAO();
	User user = TalkProFriRootController.user;
	User friend;

	public User getFriend() {
		return friend;
	}

	public void setFriend(User friend) {
		this.friend = friend;
	}

	public void setFriendImagePath(String friendImagePath) {
		this.friendImagePath = friendImagePath;
	}

	public void setFriendNicName(String friendNicName) {
		this.friendNicName = friendNicName;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		setUpLocalTimeFSS();

		chRmLblCounterNic.setOnMouseClicked(eSetFriend->setUpFriendPicNic());		
		
		chRmTf.setOnKeyPressed(event -> {
			if (event.getCode().equals(KeyCode.ENTER)) {
				send("PRIVCHAT$"+friend.getId()+"#"+chRmTf.getText()+"&"+user.getNicName());
				chRmTf.clear();
			}
		});
		chRmBtnSend.setOnAction(event -> {
			send("PRIVCHAT$"+friend.getId()+"#"+chRmTf.getText()+"&"+user.getNicName());
			chRmTf.clear();
		});
		
		chRmBtn2Back.setOnAction(eClose->talkChattingRoomStage.close());
		
		chRmBtnDeletePrivRm.setOnAction(eDeleteGroupRoom->DeletePrivRoomAction());
		
		receive();
		
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
	
	private void setUpFriendPicNic() {
		chRmLblCounterNic.setText(friendNicName);
		if (friendImagePath == null || friendImagePath.equals("")) {
			//callAlert("�̹��� ���� : ��ϵ� ������ �����ϴ�");
		} else if (friendImagePath != null && !friendImagePath.equals("")) {
			try {
				FileInputStream fileInputStream = new FileInputStream(friendImagePath);
				chRmImgVCounterPic.setImage(new Image(fileInputStream));
			} catch (Exception e) {
				callAlert("�̹��� ��� ���� : �̹��� ��ΰ� �ùٸ��� �ʽ��ϴ�");
			}
		}
	}
	
	private void DeletePrivRoomAction() {
		
		Room room1 = roomDAO.searchRoomAlready(user.getId(), friend.getId());
		Room room2 = roomDAO.searchRoomAlready(friend.getId(), user.getId());
		
		if (room1!=null&&room1.getRoomName().contains(","))
			roomDAO.deleteUserFromRoom(user.getId()+","+ friend.getId());
		if (room2!=null&&room2.getRoomName().contains(","))
			roomDAO.deleteUserFromRoom(friend.getId()+","+ user.getId());
		
		talkChattingRoomStage.close();
	}

	private void stopClient() {
		if (!socket.isClosed() && socket != null) {
			try {
				socket.close();
				Platform.runLater(() -> callAlert("ä�ü��� �ݱ� ���� : VaTalk Server�� �ݾҽ��ϴ�."));
			} catch (IOException e) {
				Platform.runLater(() -> callAlert("ä�ü��� �ݱ� ���� : ���α׷��� �����մϴ�."));
				return;
			}
		}
	}

	private void send(String sendMessage) {
		try {
			PrintWriter pw = new PrintWriter(socket.getOutputStream());
			pw.println(sendMessage);
			pw.flush();
		} catch (IOException e) {
			Platform.runLater(() -> chRmTa
					.appendText("\n���۽��� : " + sendMessage.substring(sendMessage.lastIndexOf(":") + 2) + "\n"));
			stopClient();
		}
	}
	
	private void receive() {
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());		
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						String receiveMessege = br.readLine();
						
						if (receiveMessege.startsWith("PRIVCHAT$")) {
						
							String senderNic = receiveMessege.substring(receiveMessege.indexOf("&")+1);
								String msg = receiveMessege.substring(receiveMessege.indexOf("#")+1, receiveMessege.indexOf("&"));
								Platform.runLater(() -> chRmTa.appendText(senderNic + " : " +msg + "\n"));
							
						}else if (receiveMessege.startsWith("DISCONNECTED$"))
							throw new IOException();
						
					} catch (IOException e) {
						Platform.runLater(() -> chRmTa.appendText("������ ����� �Ұ����մϴ�"));
						stopClient();
						break;
					} // try-catch
				} // while
			}
		};
		executorService.submit(runnable);
	}
	
	public static void callAlert(String contentText) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("�˸�â");
		alert.setHeaderText(contentText.substring(0, contentText.lastIndexOf(":")));
		alert.setContentText(contentText.substring(contentText.lastIndexOf(":") + 1));
		alert.showAndWait();
	}

}
