package chatapp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Message implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static ConcurrentHashMap<Long, Message> messageList = new ConcurrentHashMap<Long, Message>();

	String content;
	User author;
	Date created;
	TAG status;

	transient ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		lock = new ReentrantReadWriteLock();
	}

	private static enum TAG {
		WITHDRAWN, DELETED, NEW, READ;
	}

	public Message(String content, User author) {
		this.content = content;
		this.author = author;
		created = new Date();
		status = TAG.NEW;
		addMessage(this);
	}

	public static void addMessage(Message msg) {
		messageList.put(msg.created.getTime(), msg);
	}

	public static Message getMessage(Long id) {
		return messageList.get(id);
	}

	public static void activateList(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		messageList = (ConcurrentHashMap<Long, Message>) ois.readObject();
		if (messageList == null) {
			messageList = new ConcurrentHashMap<Long, Message>();
		}
	}

	public static void passivate(ObjectOutputStream oos) throws IOException {
		oos.writeObject(messageList);
	}

	public void deleteMessage(User user) throws Exception {
		if (user.isAdmin() || user == author) {
			lock.writeLock().lock();
			try {
				if (status == TAG.NEW) {
					status = TAG.DELETED;
				} else {
					throw new Exception("Already read. Can't deleted. But it can be withdrawn");
				}
			} finally {
				lock.writeLock().unlock();
			}
		} else {
			throw new Exception("Not an author. Cannot delete");
		}
	}

	public void withdraw(User user) throws Exception {
		if (user.isAdmin() || user == author) {
			lock.writeLock().lock();
			try {
				if (status != TAG.DELETED) {
					status = TAG.WITHDRAWN;
				}
			} finally {
				lock.writeLock().unlock();
			}
		} else {
			throw new Exception("Not an author. Cannot withdraw");
		}
	}

	public void mark(User user) {
		if (user != author) {
			lock.writeLock().lock();
			try {
				if (status == TAG.NEW) {
					status = TAG.READ;
				}
			} finally {
				lock.writeLock().unlock();
			}
		}
	}

	public String read(User user) {
		String ret = content;
		if (user != author) {
			lock.readLock().lock();
			try {
				if (status == TAG.DELETED) {
					ret = "<DELETED>";
				} else if (status == TAG.WITHDRAWN) {
					ret = content + "<WITHDRAWN>";
				} else {
					ret = content;
				}
			} finally {
				lock.readLock().unlock();
			}
		}
		return ret;
	}
}
