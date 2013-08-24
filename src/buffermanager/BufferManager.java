package buffermanager;

import java.lang.reflect.*;

import buffermanager.exceptions.BadFileException;
import buffermanager.exceptions.BadPageNumberException;
import buffermanager.exceptions.DBFileException;
import buffermanager.filesystem.FileSystem;
import buffermanager.policies.LRU;

public class BufferManager {
	public static int DEFAULT_BUFFER_SIZE = 10;
	public static String DEFAULT_POLICY = "LRU";

	private Frame[] bufferPool;
	private Policy policy;

	public BufferManager(int bufferSize, String policy)
			throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, SecurityException,
			InvocationTargetException, NoSuchMethodException {

		this.bufferPool = new Frame[bufferSize];

		//load the buffer replacement policy
		ClassLoader loader = BufferManager.class.getClassLoader();
		try {
			Class<Policy> policyClass = (Class<Policy>) loader
					.loadClass("buffermanager.policies." + policy);
			this.policy = policyClass.getConstructor(
					new Class[] { Frame[].class }).newInstance(bufferPool);
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
			System.err.println("Policy " + policy + " not found, using LRU.");
			this.policy = new LRU(bufferPool);
		}
	}

	public Frame pinPage(int pageId, String fileName) throws BadFileException,
			BadPageNumberException, DBFileException {
		Frame frame = null;
		
		//case 1: frame is in the buffer pool
		for (Frame f : bufferPool) {
			if ((f.getFilename() == fileName) && (f.getPageNum() == pageId)) {
				f.pin(fileName, pageId);
				frame = f;
				break;
			}
		}

		//case 2: frame is not in the buffer pool
		if (frame == null) {
			//choose a frame for replacement
			frame = policy.chooseFrame();
			
			//write the page that the frame contains if the dirty bit for replacement is on
			if (frame.isDirty()) {
				FileSystem.writePage(frame.getFilename(), frame.getPageNum(), frame.getPage());
			}
			
			//increment pin count
			frame.pin(fileName,pageId);
			//read requested page into replacement frame
			frame.setPage(FileSystem.readPage(fileName, pageId));
		}

		policy.pagePinned(frame);
		return frame;
	}

	public void unpinPage(int pageId, String fileName, boolean dirty) {
		for (Frame f : bufferPool) {
			if ((f.getFilename() == fileName) && (f.getPageNum() == pageId)) {
				f.unpin();
				f.setDirty(dirty);
				policy.pagePinned(f);
				break;
			}
		}

	}
}
