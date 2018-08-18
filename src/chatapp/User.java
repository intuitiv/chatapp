package chatapp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class User implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static ConcurrentHashMap<String, User> usersList = new ConcurrentHashMap<String, User>();
	static User admin = new User("admin", "Administrator", true);
	String id;
	String name;
	boolean adminUser;
	List<Topic> topics = new ArrayList<Topic>();

	public static void activateList(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		usersList = (ConcurrentHashMap<String, User>) ois.readObject();
		if(usersList == null) {
			usersList = new ConcurrentHashMap<String, User>();
			admin = new User("admin", "Administrator", true);
		}
	}

	public static void passivate(ObjectOutputStream oos) throws IOException {
		oos.writeObject(usersList);
	}

	public static User getUser(String id, String name) throws Exception {
		if (usersList.containsKey(id)) {
			return usersList.get(id);
		} else {
			return new User(id, name);
		}
	}

	User(String id, String name) throws Exception {
		this.id = id;
		this.name = name;
		addUser(this);
	}

	private User(String id, String name, boolean isAdmin) {
		this.id = id;
		this.name = name;
		try {
			addUser(this);
		} catch (Exception e) {
		}
		adminUser = isAdmin;
	}

	boolean isAdmin() {
		return adminUser;
	}

	public static void addUser(User newUser) throws Exception {
		if (!usersList.containsKey(newUser.id)) {
			usersList.put(newUser.id, newUser);
		} else {
			throw new Exception("Id already taken");
		}
	}

	public Topic createTopic(String name) throws Exception {
		Topic topic = new Topic(name, this);
		topics.add(topic);
		return topic;
	}

	public Topic getTopic(String name) throws Exception {
		return Topic.getTopic(name, this);
	}

	public Topic subscribeToATopic(String name) throws Exception {
		Topic topic = Topic.subscribeToATopic(name, this);
		topics.add(topic);
		return topic;
	}

	public ArrayList<Message> getAllMessages(String topicName) throws Exception {
		Topic topic = getTopic(topicName);
		return topic.getMessagesFrom(0, this);
	}

	public ArrayList<Message> getMessagesFrom(String topicName, int position) throws Exception {
		Topic topic = getTopic(topicName);
		return topic.getMessagesFrom(position, this);
	}
}
