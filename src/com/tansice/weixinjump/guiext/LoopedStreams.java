package com.tansice.weixinjump.guiext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class LoopedStreams {
	private PipedOutputStream pipedOS = new PipedOutputStream();
	private boolean keepRunning = true;
	private ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream() {
		public void close() {
			keepRunning = false;
			try {
				super.close();
				pipedOS.close();
			} catch (IOException e) {
				// 此处我们直接停止
				System.exit(1);
			}
		}
	};
	private PipedInputStream pipedIS = new PipedInputStream() {
		public void close() {
			keepRunning = false;
			try {
				super.close();
			} catch (IOException e) {
				// 此处我们直接停止
				System.exit(1);
			}
		}
	};

	public LoopedStreams() throws IOException {
		pipedOS.connect(pipedIS);
		startByteArrayReaderThread();
	}

	public InputStream getInputStream() {
		return pipedIS;
	}

	public OutputStream getOutputStream() {
		return byteArrayOS;
	}

	private void startByteArrayReaderThread() {
		new Thread(new Runnable() {
			public void run() {
				while (keepRunning) {
					if (byteArrayOS.size() > 0) {
						byte[] buffer = null;
						synchronized (byteArrayOS) {
							buffer = byteArrayOS.toByteArray();
							byteArrayOS.reset(); // 重置缓冲区
						}
						try {
							// 把提取到的数据发送给PipedOutputStream
							pipedOS.write(buffer, 0, buffer.length);
						} catch (IOException e) {
							// 此处我们直接停止
							System.exit(1);
						}
					} else { // 没罕有据可用，线程进入睡眠形态
						try {
							// 每隔1秒检查ByteArrayOutputStream新数据
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}).start();
	} // startByteArrayReaderThread()
} // LoopedStreams