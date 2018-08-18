package chatapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;

public class Main {
	public static void main(String[] args) throws Exception {
		File f = new File("ChatApp.ser");
		if (f.exists()) {
			FileInputStream is = new FileInputStream("ChatApp.ser");
			ObjectInputStream ois = new ObjectInputStream(is);
			Message.activateList(ois);
			User.activateList(ois);
			Topic.activateList(ois);
			ois.close();
			is.close();
		} else {
			Topic topic = User.admin.createTopic("F");
			topic.addMessage("Message 1", User.admin);
			topic.addMessage("Message 2", User.admin);
			topic.addMessage("Message 3", User.admin);
			topic.addMessage("Message 4", User.admin);
			topic.getMessagesFrom(2, User.admin).get(0).deleteMessage(User.admin);
			topic.getMessagesFrom(2, User.admin).get(1).withdraw(User.admin);
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("User credentials to login");
		String line = br.readLine();
		while (!line.equalsIgnoreCase("quit")) {
			if (line.startsWith("login")) {
				String parse[] = line.split(" ");
				String id = parse[1];
				String name = null;
				if (parse.length > 2) {
					name = parse[2];
				}
				try {
					User user = User.getUser(id, name);
					new ChatApp(user).run();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println("User credentials to login");
			line = br.readLine();
		}
		try {
			FileOutputStream fos = new FileOutputStream("ChatApp.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			Message.passivate(oos);
			User.passivate(oos);
			Topic.passivate(oos);
			System.out.println("Done");
			// closing resources
			oos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}

class ChatApp extends Thread {
	User user;

	ChatApp(User user) {
		this.user = user;
	}

	public void run() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line;
		try {
			System.out.println("enter command");
			line = br.readLine();
			while (!line.equalsIgnoreCase("logoff")) {
				try {
					if (line.startsWith("tcr")) {
						user.createTopic(line.split(" ")[1]);
					} else if (line.startsWith("tsub")) {
						user.subscribeToATopic(line.split(" ")[1]);
					} else if (line.startsWith("tam")) {
						user.getTopic(line.split(" ")[1]).addMessage(line.split(" ")[2], user);
					} else if (line.startsWith("tgm")) {
						Iterator<Message> msgs = user.getTopic(line.split(" ")[1]).getMessagesFrom(0, user).iterator();
						while (msgs.hasNext()) {
							Message message = (Message) msgs.next();
							System.out.println(message.author.name + "-->" + message.read(user));
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					System.out.println("enter command");
					line = br.readLine();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}