package chatapp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Topic implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static enum TAG {
		ACTIVE, BLOCKED;
	}

	String name;
	User admin;
	TAG status;
	HashSet<User> authenticatedUsers = new HashSet<User>();
	ConcurrentLinkedQueue<Message> messages = new ConcurrentLinkedQueue<Message>();

	Topic(String name, User admin) throws Exception {
		this.name = name;
		this.admin = admin;
		status = TAG.ACTIVE;
		authenticatedUsers.add(admin);
		addTopic(name, this);
	}

	private static ConcurrentHashMap<String, Topic> topicList = new ConcurrentHashMap<String, Topic>();

	public static void activateList(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		topicList = (ConcurrentHashMap<String, Topic>) ois.readObject();
		if(topicList == null) {
			topicList = new ConcurrentHashMap<String, Topic>();
		}
	}
	
	public static void passivate(ObjectOutputStream oos) throws IOException {
		oos.writeObject(topicList);
	}
	
	public static void addTopic(String name, Topic topic) throws Exception {
		if (!topicList.containsKey(name)) {
			topicList.put(name, topic);
		} else {
			throw new Exception("Topic already exists");
		}
	}

	public static Topic getTopic(String name, User user) throws Exception {
		Topic topic = topicList.get(name);
		if (topic.authenticatedUsers.contains(user)) {
			return topic;
		} else {
			throw new Exception("Not subscribed");
		}
	}

	public static Topic subscribeToATopic(String name, User user) throws Exception {
		Topic topic = topicList.get(name);
		if (topic != null) {
			topic.authenticatedUsers.add(user);
			return topic;
		} else {
			throw new Exception("No such topic");
		}
	}
	
	public void block(User user) throws Exception {
		if(user.isAdmin() || admin == user) {
			status = TAG.BLOCKED;
		} else {
			throw new Exception("Not an admin");
		}
	}

	public void addMessage(String content, User author) throws Exception {
		if (status == TAG.ACTIVE && authenticatedUsers.contains(author)) {
			Message msg = new Message(content, author);
			messages.add(msg);
		} else {
			throw new Exception("Cannot add message");
		}
	}

	public void mark(LinkedList<Message> cache) {
		if (cache.size() != messages.size()) {
			int pos = cache.size();
			Iterator<Message> iter = messages.iterator();
			while (pos != 0) {
				iter.next();
				pos--;
			}

			while (iter.hasNext()) {
				Message nextMsg = (Message) iter.next();
				cache.add(nextMsg);
			}
		}
	}

	public ArrayList<Message> getMessagesFrom(int position, User user) {
		ArrayList<Message> ret = new ArrayList<Message>();
		if (position < messages.size()) {
			int pos = position;
			Iterator<Message> iter = messages.iterator();
			while (pos != 0) {
				iter.next();
				pos--;
			}
			while (iter.hasNext()) {
				Message nextMsg = (Message) iter.next();
				nextMsg.read(user);
				ret.add(nextMsg);
			}
		}
		return ret;
	}
	
	
}
