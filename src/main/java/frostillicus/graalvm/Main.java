package frostillicus.graalvm;

import lotus.domino.NotesException;
import lotus.notes.addins.JavaServerAddin;
import lotus.notes.internal.MessageQueue;

public class Main extends JavaServerAddin {
	static {
		System.setProperty("java.library.path", "/opt/hcl/domino/notes/11000100/linux"); //$NON-NLS-1$ //$NON-NLS-2$
		System.loadLibrary("notes"); //$NON-NLS-1$
		System.loadLibrary("lsxbe"); //$NON-NLS-1$
	}
	
	// MessageQueue Constants
	public static final int MQ_MAX_MSGSIZE = 256;
	
	// MessageQueue errors:
	public static final int PKG_MISC = 0x0400;
	public static final int ERR_MQ_POOLFULL = PKG_MISC+94;
	public static final int ERR_MQ_TIMEOUT = PKG_MISC+95;
	public static final int ERR_MQSCAN_ABORT = PKG_MISC+96;
	public static final int ERR_DUPLICATE_MQ = PKG_MISC+97;
	public static final int ERR_NO_SUCH_MQ = PKG_MISC+98;
	public static final int ERR_MQ_EXCEEDED_QUOTA = PKG_MISC+99;
	public static final int ERR_MQ_EMPTY = PKG_MISC+100;
	public static final int ERR_MQ_BFR_TOO_SMALL = PKG_MISC+101;
	public static final int ERR_MQ_QUITTING = PKG_MISC+102;

	public static final String QUEUE_NAME = JavaServerAddin.MSG_Q_PREFIX + "GRAALVM-TEST"; //$NON-NLS-1$
	
	public static void main(String[] args) {
		new Main().start();
	}
	
	public Main() {
		setName("GraalVM Test");
	}
	
	@Override
	public void runNotes() throws NotesException {
		AddInLogMessageText("GraalVM Test initialized");
		int taskId = AddInCreateStatusLine(getName());
		try {
			
			MessageQueue mq = new MessageQueue();
			int status = mq.create(QUEUE_NAME, 0, 0);
			if(status != NOERROR) {
				throw new RuntimeException("Error during MQ creation: " + status); //$NON-NLS-1$
			}
			
			status = mq.open(QUEUE_NAME, 0);
			if(status != NOERROR) {
				throw new RuntimeException("Error during MQ open: " + status); //$NON-NLS-1$
			}
			
			try {
				AddInSetStatusLine(taskId, "Idle"); //$NON-NLS-1$
				StringBuffer buf = new StringBuffer();
				while(addInRunning() && status != ERR_MQ_QUITTING) {
					OSPreemptOccasionally();
					
					status = mq.get(buf, MQ_MAX_MSGSIZE, MessageQueue.MQ_WAIT_FOR_MSG, 500);
					switch(status) {
					case NOERROR: {
						String line = buf.toString();
						if(line != null && !line.isEmpty()) {
							AddInLogMessageText("Received message: " + line);
						}
						
						break;
					}
					case ERR_MQ_TIMEOUT:
					case ERR_MQ_EMPTY:
					case ERR_MQ_QUITTING:
						break;
					default:
						AddInLogErrorText("Received unexpected code while polling: " + status); //$NON-NLS-1$
						break;
					}
				}
				
			} finally {
				mq.close(0);
			}
		} catch(Throwable t) {
			t.printStackTrace();
		} finally {
			AddInDeleteStatusLine(taskId);
		}
	}

}
