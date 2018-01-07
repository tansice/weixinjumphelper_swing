package com.tansice.weixinjump.service;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import com.tansice.weixinjump.guiext.ZPanel;
import com.tansice.weixinjump.model.RGBInfo;

/**
 * 参考知乎
 * 
 * @link <a href="https://zhuanlan.zhihu.com/p/32452473" rel=
 *       "external nofollow" rel=
 *       "external nofollow" target="_blank">https://zhuanlan.zhihu.com/p/
 *       32452473</a> 跳一跳辅助
 */
public class WeiXinJumpService implements Runnable {

	private volatile static WeiXinJumpService singleton;

	private WeiXinJumpService() {
	}

	public static WeiXinJumpService getInstance() {
		if (singleton == null) {
			synchronized (WeiXinJumpService.class) {
				if (singleton == null) {
					singleton = new WeiXinJumpService();
				}
			}
		}
		return singleton;
	}

	volatile ZPanel imagePanel;
	volatile JLabel imageLabel;

	public JLabel getImageLabel() {
		return imageLabel;
	}

	public void setImageLabel(JLabel imageLabel) {
		this.imageLabel = imageLabel;
	}

	public ZPanel getImagePanel() {
		return imagePanel;
	}

	public void setImagePanel(ZPanel imagePanel) {
		this.imagePanel = imagePanel;
	}

	private volatile boolean runningHook = true;

	public boolean isRunningHook() {
		return runningHook;
	}

	public void setRunningHook(boolean runningHook) {
		this.runningHook = runningHook;
	}

	private static final String IMAGE_NAME = "current.png";

	private static final String STORE_DIR = "d:/jump_screencapture";

	// 数量
	private static final int imageLengthLength = 5;

	// 存放图片的大小
	private static final long[] imageLength = new long[imageLengthLength];

	private final RGBInfo rgbInfo = new RGBInfo();

	private final String[] ADB_SCREEN_CAPTURE_CMDS = { "adb shell screencap -p /sdcard/" + IMAGE_NAME,
			"adb pull /sdcard/current.png " + STORE_DIR };

	// 截屏中游戏分数显示区域最下方的Y坐标，300是 1920x1080的值，根据实际情况修改
	private final int gameScoreBottomY = 300;

	// 按压的时间系数，可根据具体情况适当调节
	private final double pressTimeCoefficient = 1.35;

	// 按压的起始点坐标，也是再来一局的起始点坐标
	private final int swipeX = 550;

	private final int swipeY = 1580;

	// 二分之一的棋子底座高度
	private final int halfBaseBoardHeight = 20;

	// 棋子的宽度，从截屏中量取，自行调节
	private final int halmaBodyWidth = 74;

	// 游戏截屏里的两个跳板的中点坐标，主要用来计算角度，可依据实际的截屏计算，计算XY的比例
	private final int boardX1 = 813;

	private final int boardY1 = 1122;

	private final int boardX2 = 310;

	private final int boardY2 = 813;

	/**
	 * 获取跳棋以及下一块跳板的中心坐标
	 * 
	 * @return
	 * @throws IOException
	 * @update 2017年12月31日 下午12:18:22
	 */
	private int[] getHalmaAndBoardXYValue(File currentImage) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(currentImage);
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		System.out.println("图片信息[宽度：" + width + "，高度：" + height + "]");
		int halmaXSum = 0;
		int halmaXCount = 0;
		int halmaYMax = 0;
		int boardX = 0;
		int boardY = 0;
		// 从截屏从上往下逐行遍历像素点，以棋子颜色作为位置识别的依据，最终取出棋子颜色最低行所有像素点的平均值，即计算出棋子所在的坐标
		for (int y = gameScoreBottomY; y < height; y++) {
			for (int x = 0; x < width; x++) {
				processRGBInfo(bufferedImage, x, y);
				int rValue = this.rgbInfo.getRValue();
				int gValue = this.rgbInfo.getGValue();
				int bValue = this.rgbInfo.getBValue();
				// 根据RGB的颜色来识别棋子的位置，
				if (rValue > 50 && rValue < 60 && gValue > 53 && gValue < 63 && bValue > 95 && bValue < 110) {
					halmaXSum += x;
					halmaXCount++;
					// 棋子底行的Y坐标值
					halmaYMax = y > halmaYMax ? y : halmaYMax;
				}
			}
		}

		if (halmaXSum != 0 && halmaXCount != 0) {
			// 棋子底行的X坐标值
			int halmaX = halmaXSum / halmaXCount;
			// 上移棋子底盘高度的一半
			int halmaY = halmaYMax - halfBaseBoardHeight;
			// 从gameScoreBottomY开始
			for (int y = gameScoreBottomY; y < height; y++) {
				processRGBInfo(bufferedImage, 0, y);
				int lastPixelR = this.rgbInfo.getRValue();
				int lastPixelG = this.rgbInfo.getGValue();
				int lastPixelB = this.rgbInfo.getBValue();
				// 只要计算出来的boardX的值大于0，就表示下个跳板的中心坐标X值取到了。
				if (boardX > 0) {
					break;
				}
				int boardXSum = 0;
				int boardXCount = 0;
				for (int x = 0; x < width; x++) {
					processRGBInfo(bufferedImage, x, y);
					int pixelR = this.rgbInfo.getRValue();
					int pixelG = this.rgbInfo.getGValue();
					int pixelB = this.rgbInfo.getBValue();
					// 处理棋子头部比下一个跳板还高的情况
					if (Math.abs(x - halmaX) < halmaBodyWidth) {
						continue;
					}

					// 从上往下逐行扫描至下一个跳板的顶点位置，下个跳板可能为圆形，也可能为方框，取多个点，求平均值
					if ((Math.abs(pixelR - lastPixelR) + Math.abs(pixelG - lastPixelG)
							+ Math.abs(pixelB - lastPixelB)) > 10) {
						boardXSum += x;
						boardXCount++;
					}
				}

				if (boardXSum > 0) {
					boardX = boardXSum / boardXCount;
				}
			}

			// 按实际的角度来算，找到接近下一个 board 中心的坐标
			boardY = (int) (halmaY
					- Math.abs(boardX - halmaX) * Math.abs(boardY1 - boardY2) / Math.abs(boardX1 - boardX2));
			if (boardX > 0 && boardY > 0) {
				int[] result = new int[4];
				// 棋子的X坐标
				result[0] = halmaX;
				// 棋子的Y坐标
				result[1] = halmaY;
				// 下一块跳板的X坐标
				result[2] = boardX;
				// 下一块跳板的Y坐标
				result[3] = boardY;
				return result;
			}
		}

		return null;
	}

	/**
	 * 执行命令
	 * 
	 * @param command
	 * @update 2017年12月31日 下午12:13:39
	 */
	private void executeCommand(String command) {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(command);
			System.out.println("开始执行命令: " + command);
			process.waitFor();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String line = bufferedReader.readLine();
			if (line != null) {
				System.out.println(line);
			}
			System.out.println("执行命令结束");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (process != null) {
				process.destroy();
			}
		}
	}

	/**
	 * ADB获取安卓截屏
	 * 
	 * @update 2017年12月31日 下午12:11:42
	 */
	private void executeADBCaptureCommands() {
		for (String command : ADB_SCREEN_CAPTURE_CMDS) {
			executeCommand(command);
		}
	}

	/**
	 * 跳一下
	 * 
	 * @param distance
	 * @update 2017年12月31日 下午12:23:19
	 */
	private void doJump(double distance) {
		System.out.println("距离: " + distance);
		// 计算按压时间，最小200毫秒
		int pressTime = (int) Math.max(distance * pressTimeCoefficient, 200);
		System.out.println("按压时间: " + pressTime + "ms");
		// 执行按压操作
		String command = String.format("adb shell input swipe %s %s %s %s %s", swipeX, swipeY, swipeX, swipeY,
				pressTime);
		executeCommand(command);
	}

	/**
	 * 再来一局
	 * 
	 * @update 2017年12月31日 下午12:47:06
	 */
	private void replayGame() {
		String command = String.format("adb shell input tap %s %s", swipeX, swipeY);
		executeCommand(command);
	}

	/**
	 * 计算跳跃的距离，也即两个点之间的距离
	 * 
	 * @param halmaX
	 * @param halmaY
	 * @param boardX
	 * @param boardY
	 * @return
	 * @update 2017年12月31日 下午12:27:30
	 */
	private double computeJumpDistance(int halmaX, int halmaY, int boardX, int boardY) {
		return Math.sqrt(Math.pow(Math.abs(boardX - halmaX), 2) + Math.pow(Math.abs(boardY - halmaY), 2));
	}

	/**
	 * 检查是否需要重新开局
	 * 
	 * @update 2017年12月31日 下午1:39:18
	 */
	private void checkDoReplay() {
		if (imageLength[0] > 0 && imageLength[0] == imageLength[1] && imageLength[1] == imageLength[2]
				&& imageLength[2] == imageLength[3] && imageLength[3] == imageLength[4]) {
			// 此时表示已经连续5次图片大小一样了，可知当前屏幕处于再来一局
			Arrays.fill(imageLength, 0);
			// 模拟点击再来一局按钮重新开局
			replayGame();
		}
	}

	/**
	 * 获取指定坐标的RGB值
	 * 
	 * @param bufferedImage
	 * @param x
	 * @param y
	 * @update 2017年12月31日 下午12:12:43
	 */
	private void processRGBInfo(BufferedImage bufferedImage, int x, int y) {
		rgbInfo.reset();
		int pixel = bufferedImage.getRGB(x, y);
		// 转换为RGB数字
		rgbInfo.setRValue((pixel & 0xff0000) >> 16);
		rgbInfo.setGValue((pixel & 0xff00) >> 8);
		rgbInfo.setBValue((pixel & 0xff));
	}

	@Override
	public void run() {
		try {
			File storeDir = new File(STORE_DIR);
			if (!storeDir.exists()) {
				boolean flag = storeDir.mkdir();
				if (!flag) {
					System.err.println("创建图片存储目录失败");
					return;
				}
			}
			// 执行次数
			int executeCount = 0;
			while (runningHook) {
				// 执行ADB命令，获取安卓截屏
				executeADBCaptureCommands();
				File currentImage = new File(STORE_DIR, IMAGE_NAME);
				if (!currentImage.exists()) {
					System.out.println("图片不存在");
					continue;
				} else {
					ImageIcon icon = new ImageIcon(STORE_DIR + File.separator + IMAGE_NAME);
					icon.setImage(icon.getImage().getScaledInstance(216, 384, Image.SCALE_FAST));
					imageLabel.setIcon(icon);
				}

				long length = currentImage.length();
				imageLength[executeCount % imageLengthLength] = length;
				// 查看是否需要重新开局
				checkDoReplay();
				executeCount++;
				System.out.println("当前第" + executeCount + "次执行");
				// 获取跳棋和底板的中心坐标
				int[] result = getHalmaAndBoardXYValue(currentImage);
				if (result == null) {
					System.out.println("获取跳棋和底板的中心坐标失败，请检查是否已开启游戏!");
					continue;
				}
				int halmaX = result[0];
				int halmaY = result[1];
				int boardX = result[2];
				int boardY = result[3];
				// System.out.println("halmaX: " + halmaX + ", halmaY: " +
				// halmaY + ", boardX: " + boardX + ", boardY: " + boardY);
				// 计算跳跃的距离
				double jumpDistance = computeJumpDistance(halmaX, halmaY, boardX, boardY);
				doJump(jumpDistance);
				// 每次停留2.5秒
				Random r = new Random();
				double d = r.nextDouble() * 3 + 1;
				int stopTime = (int) (d * 1000);
				System.out.println("随机停留:" + stopTime + "ms");
				TimeUnit.MILLISECONDS.sleep(stopTime);
			}
			if (!runningHook) {
				System.out.println("停止运行成功！ ");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
}
