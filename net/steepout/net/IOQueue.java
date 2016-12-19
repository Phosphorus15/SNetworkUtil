package net.steepout.net;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A limited multi-thread IO task arranger <br/>
 * finishing a large sets of <b>parallel</b> transmission jobs with limited
 * threads <br/>
 * If you don't want your data transmission be alike with UDP , please use this
 * carefully
 * 
 * @author Phosphorus15
 *
 */
@Deprecated
public class IOQueue {
	List<IOThread> activeThread;

	class IOThread extends Thread {

		Sender sender;

		Object security = new Object();

		IOThread(Sender sender) {
			this.sender = sender;
			start();
			System.out.println("started");
		}

		public boolean finished() {
			return sender == null;
		}

		public void uploadJob(Sender sender) {
			System.out.println("job uploaded");
			if (this.sender == null) {
				this.sender = sender;
				System.out.println("upload successful");
			}
		}

		public void run() {
			while (!isInterrupted()) {
				if (sender == null)
					sender = next();
				if (sender != null) {
					sender.send();
					System.out.println("send " + sender.hashCode());
					sender = null;
				}
			}
		}
	}

	LinkedList<Sender> queue;

	int maximum = 3;

	public IOQueue() {
		this(3);
	}

	public IOQueue(int maximum) {
		this.maximum = maximum;
		activeThread = new ArrayList<IOThread>();
		queue = new LinkedList<Sender>();
	}

	/**
	 * Avoid duplicated single source sender
	 * 
	 * @param sender
	 */
	public synchronized void arrangeTask(Sender sender) {
		System.out.println(sender.hashCode());
		for (IOThread t : activeThread) {
			if (t.finished()) {
				synchronized (queue) {
					queue.add(sender);
				}
				return;
			}
		}
		System.out.println("233");
		if (activeThread.size() < maximum) {
			System.out.println("task arranged");
			activeThread.add(new IOThread(sender));
		} else {
			synchronized (queue) {
				queue.add(sender);
			}
		}
	}

	public void close(boolean force) {
		if (force) {
			for (IOThread t : this.activeThread)
				t.interrupt();
			queue.clear();
		} else {
			new Thread(() -> {
				while (queue.size() > 0)
					;
				for (IOThread th : this.activeThread) {
					th.interrupt();
				}
				queue.clear();
			}).start();
		}
	}

	protected synchronized Sender next() {
		synchronized (queue) {
			if (queue.size() > 0) {
				System.out.println("pop job" + queue.getFirst().hashCode());
				return queue.pop();
			} else
				return null;
		}
	}
}
