package com.tansice.weixinjump.view;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import com.tansice.weixinjump.guiext.ConsoleTextArea;
import com.tansice.weixinjump.guiext.ZPanel;
import com.tansice.weixinjump.service.WeiXinJumpService;

/**
 * @since 2018年1月7日 05:14:48
 * @author Trial
 *
 */
public class StartFrame extends JFrame {

	private static final String VERSION = "V0.0.1";
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	volatile ZPanel imagePanel = new ZPanel();
	volatile JLabel imageLabel = new JLabel();
	ExecutorService executorService = Executors.newFixedThreadPool(1); // 创建一个执行任务的线程池

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					StartFrame frame = new StartFrame();
					frame.setVisible(true);
					frame.setResizable(false);
					int windowWidth = frame.getWidth(); // 获得窗口宽
					int windowHeight = frame.getHeight(); // 获得窗口高
					Toolkit kit = Toolkit.getDefaultToolkit(); // 定义工具包
					Dimension screenSize = kit.getScreenSize(); // 获取屏幕的尺寸
					int screenWidth = screenSize.width; // 获取屏幕的宽
					int screenHeight = screenSize.height; // 获取屏幕的高
					frame.setLocation(screenWidth / 2 - windowWidth / 2, screenHeight / 2 - windowHeight / 2);// 设置窗口居中显示
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public StartFrame() throws IOException {

		setIconImage(Toolkit.getDefaultToolkit()
				.getImage(StartFrame.class.getResource("/images/WeChat_64px_1194711_easyicon.net.png")));
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				executorService.shutdown();
				super.windowClosing(e);
			}
		});
		setTitle("微信跳一跳辅助-"+VERSION+" By Trial 2018.01.07 http://blog.tansice.com/");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 722, 463);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel panel = new JPanel();
		panel.setBounds(45, 97, 291, 45);
		contentPane.add(panel);
		panel.setLayout(null);

		JButton btnNewButton_1 = new JButton("开始");
		btnNewButton_1.setBackground(SystemColor.inactiveCaption);
		btnNewButton_1
				.setIcon(new ImageIcon(StartFrame.class.getResource("/images/start_16px_1169786_easyicon.net.png")));
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		btnNewButton_1.setBounds(30, 10, 96, 25);
		btnNewButton_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		btnNewButton_1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		panel.add(btnNewButton_1);

		JButton button = new JButton("停止");
		button.setBackground(SystemColor.inactiveCaption);
		button.setIcon(new ImageIcon(StartFrame.class.getResource("/images/Stop_16px_1191714_easyicon.net.png")));
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button.setBounds(167, 10, 81, 25);
		panel.add(button);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new CompoundBorder());
		panel_1.setBounds(5, 5, 280, 82);
		contentPane.add(panel_1);
		panel_1.setLayout(null);

		JLabel label = new JLabel("微信跳一跳辅助");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		label.setBounds(121, 25, 126, 47);
		panel_1.add(label);

		// 显示图片面板
		ZPanel imagePanel = new ZPanel();
		imagePanel.setBounds(461, 30, 216, 384);
		contentPane.add(imagePanel);

		imagePanel.add(imageLabel);

		JLabel label_1 = new JLabel("日志：");
		label_1.setBounds(5, 170, 54, 15);
		contentPane.add(label_1);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(5, 191, 426, 223);
		contentPane.add(scrollPane);

		ConsoleTextArea textArea = new ConsoleTextArea();
		scrollPane.setViewportView(textArea);

		JLabel label_2 = new JLabel("预览：");
		label_2.setBounds(461, 10, 54, 15);
		contentPane.add(label_2);
		textArea.setText(
				"只支持安卓，很任性地不支持苹果\n没有版权，因为我也是抄的\n使用方法：\n1.安装JAVA运行环境\n2.安装手机ADB驱动\n3.勾选开启USB调试\n欢迎逛逛 http://blog.tansice.com/");
		btnNewButton_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				WeiXinJumpService weiXinJumpService = WeiXinJumpService.getInstance();
				weiXinJumpService.setImagePanel(imagePanel);
				weiXinJumpService.setImageLabel(imageLabel);
				System.out.println("开始运行...");
				weiXinJumpService.setRunningHook(true);
				executorService.submit(weiXinJumpService); // 提交并执行任务
			}
		});
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				WeiXinJumpService.getInstance().setRunningHook(false);
			}
		});
	}
}
