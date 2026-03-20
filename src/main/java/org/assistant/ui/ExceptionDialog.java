package org.assistant.ui;

import org.assistant.util.SwingUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 通用的异常展示弹窗组件
 * 支持展示异常类型、消息、完整堆栈，支持复制异常信息
 */
public class ExceptionDialog extends JDialog {
	// 异常信息面板（折叠/展开切换）
	private JPanel stackTracePanel;
	// 堆栈文本区域
	private JTextArea stackTraceTextArea;
	// 异常类型+消息的富文本标签（区分颜色）
	private JTextPane exceptionInfoPane;

	/**
	 * 构造方法
	 *
	 * @param parent    父窗口（null则为无父窗口的弹窗）
	 * @param title     弹窗标题
	 * @param throwable 要展示的异常
	 */
	public ExceptionDialog(Frame parent, String title, Throwable throwable) {
		super(parent, title, true); // 模态弹窗
		initUI(throwable);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(800, 500); // 默认尺寸
		setMinimumSize(new Dimension(600, 400)); // 最小尺寸
		setLocationRelativeTo(parent); // 居中显示

		// 窗口关闭时释放资源
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
	}

	/**
	 * 简化构造方法（默认标题）
	 *
	 * @param parent    父窗口
	 * @param throwable 要展示的异常
	 */
	public ExceptionDialog(Frame parent, Throwable throwable) {
		this(parent, "系统异常信息", throwable);
	}

	/**
	 * 初始化UI组件
	 */
	private void initUI(Throwable throwable) {
		// 1. 主面板（边框布局，间距10px）
		JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		setContentPane(mainPanel);

		// 2. 顶部：异常基本信息（类型+消息）
		exceptionInfoPane = new JTextPane();
		exceptionInfoPane.setEditable(false);
		exceptionInfoPane.setBorder(BorderFactory.createTitledBorder("异常信息"));
		// 设置富文本样式（异常类型红色，消息黑色）
		setExceptionInfo(throwable);
		// 包装成滚动面板（避免内容过长）
		JScrollPane infoScrollPane = new JScrollPane(exceptionInfoPane);
		infoScrollPane.setPreferredSize(new Dimension(0, 80)); // 固定高度
		mainPanel.add(infoScrollPane, BorderLayout.NORTH);

		// 3. 中部：堆栈跟踪（默认折叠）
		stackTracePanel = new JPanel(new BorderLayout(10, 10));
		stackTracePanel.setBorder(BorderFactory.createTitledBorder("堆栈跟踪"));

		// 堆栈文本区域（等宽字体，只读）
		stackTraceTextArea = new JTextArea();
		stackTraceTextArea.setFont(new Font("Consolas", Font.PLAIN, 12));
		stackTraceTextArea.setEditable(false);
		stackTraceTextArea.setText(getStackTraceAsString(throwable));
		stackTraceTextArea.setLineWrap(false); // 代码不换行
		JScrollPane stackScrollPane = new JScrollPane(stackTraceTextArea);
		stackTracePanel.add(stackScrollPane, BorderLayout.CENTER);

		// 默认折叠堆栈面板
		mainPanel.add(stackTracePanel, BorderLayout.CENTER);

		// 4. 底部：操作按钮（展开/折叠、复制、关闭）
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

		// 展开/折叠按钮
		JButton toggleButton = new JButton("展开堆栈");
		toggleButton.addActionListener(e -> {
			boolean isVisible = stackTracePanel.isVisible();
			stackTracePanel.setVisible(!isVisible);
			toggleButton.setText(isVisible ? "展开堆栈" : "折叠堆栈");
			// 刷新窗口大小
			pack();
			setSize(Math.max(getWidth(), 600), Math.max(getHeight(), 400));
		});

		// 复制异常信息按钮
		JButton copyButton = new JButton("复制全部信息");
		copyButton.addActionListener(e -> copyExceptionInfo(throwable));

		// 关闭按钮
		JButton closeButton = new JButton("关闭");
		closeButton.addActionListener(e -> dispose());

		// 添加按钮到面板
		buttonPanel.add(toggleButton);
		buttonPanel.add(copyButton);
		buttonPanel.add(closeButton);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
	}

	/**
	 * 设置异常基本信息（富文本）
	 */
	private void setExceptionInfo(Throwable throwable) {
		StyledDocument doc = exceptionInfoPane.getStyledDocument();
		try {
			doc.remove(0, doc.getLength()); // 清空原有内容

			// 1. 异常类型（红色、加粗）
			SimpleAttributeSet typeStyle = new SimpleAttributeSet();
			StyleConstants.setForeground(typeStyle, Color.RED);
			StyleConstants.setBold(typeStyle, true);
			String typeText = "异常类型：" + throwable.getClass().getName() + "\n";
			doc.insertString(doc.getLength(), typeText, typeStyle);

			// 2. 异常消息（黑色、常规）
			SimpleAttributeSet msgStyle = new SimpleAttributeSet();
			StyleConstants.setForeground(msgStyle, Color.BLACK);
			String msgText = "异常消息：" + (throwable.getMessage() == null ? "无" : throwable.getMessage());
			doc.insertString(doc.getLength(), msgText, msgStyle);

			// 禁用编辑
			exceptionInfoPane.setEditable(false);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将堆栈跟踪转换为字符串
	 */
	private String getStackTraceAsString(Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		throwable.printStackTrace(pw);
		pw.flush();
		return sw.toString();
	}

	/**
	 * 复制异常全部信息到剪贴板
	 */
	private void copyExceptionInfo(Throwable throwable) {
		String sb = "异常类型：" + throwable.getClass().getName() + "\n" +
								"异常消息：" + throwable.getMessage() + "\n\n" +
								"堆栈跟踪：\n" + getStackTraceAsString(throwable);

		// 复制到剪贴板
		StringSelection selection = new StringSelection(sb);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
		// 提示复制成功
		JOptionPane.showMessageDialog(this, "异常信息已复制到剪贴板！", "提示", JOptionPane.INFORMATION_MESSAGE);
	}

	public static void run(JComponent parent, Runnable runnable) {
		try {
			runnable.run();
		} catch (Throwable throwable) {
			showException(SwingUtils.getParentJFrame(parent), throwable.getMessage(), throwable);
		}
	}

	public static void showError(JComponent parent, Throwable throwable) {
		showException(SwingUtils.getParentFrame(parent), throwable.getMessage(), throwable);
	}

	/**
	 * 静态便捷方法：快速展示异常弹窗
	 *
	 * @param parent    父窗口
	 * @param title     标题
	 * @param throwable 异常
	 */
	public static void showException(Frame parent, String title, Throwable throwable) {
		SwingUtilities.invokeLater(() -> {
			new ExceptionDialog(parent, title, throwable).setVisible(true);
		});
	}

	/**
	 * 静态便捷方法：快速展示异常弹窗（默认标题）
	 *
	 * @param parent    父窗口
	 * @param throwable 异常
	 */
	public static void showException(Frame parent, Throwable throwable) {
		showException(parent, "系统异常信息", throwable);
	}

	// 测试示例
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			// 模拟一个异常
			try {
				int a = 1 / 0; // 算术异常
			} catch (Throwable e) {
				// 展示异常弹窗
				ExceptionDialog.showException(null, "测试异常弹窗", e);
			}
		});
	}
}