package Model;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import Controller.UserDAO;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Chatter extends Thread{

	public Socket socket; 
	User user;
	
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Chatter(User user) {
		this.user=user;
		startClient();
	}
	UserDAO userDAO = new UserDAO();
	
	private void startClient() {
		new Thread(() -> {
			socket = new Socket();
			try {
				socket.connect(new InetSocketAddress("192.168.0.209", 6114));
				//Platform.runLater(() -> callAlert("ä�ü��� ���� ���� : VaTalk Server�� �����Ͽ����ϴ�."));
				
				String ipPort = ""+socket.getLocalSocketAddress();
				String ip = ipPort.substring(ipPort.indexOf("/")+1, ipPort.indexOf(":"));
				String port = ipPort.substring(ipPort.indexOf(":")+1);
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						int updateIP = userDAO.updateIpPort(user.getId(), ip, port);
						if (updateIP == 1) {
							//callAlert("INSERT ���� : DB�� IP�� PORT�� ������Ʈ �Ǿ����ϴ�.");
						send("LOGIN$"+user.getId());
						}else
							callAlert("INSERT ���� : DB�� IP�� PORT ������Ʈ�� ���� �Ͽ����ϴ�.");
					}
				});
			
			} catch (IOException e) {
				Platform.runLater(() -> callAlert("ä�ü��� ���� ���� : VaTalk Server���� ���ῡ �����Ͽ����ϴ�."));
				if (!socket.isClosed())
					stopClient();
				return;
			}
		}).start();
	}
	private void send(String sendMessage) {
		try {
			PrintWriter pw = new PrintWriter(socket.getOutputStream());
			pw.println(sendMessage);
			pw.flush();
		} catch (IOException e) {
			e.printStackTrace();
			stopClient();
		}
	}
	public void stopClient() {
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
	public static void callAlert(String contentText) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("�˸�â");
		alert.setHeaderText(contentText.substring(0, contentText.lastIndexOf(":")));
		alert.setContentText(contentText.substring(contentText.lastIndexOf(":") + 1));
		alert.showAndWait();
	}
}
