package org.assistant.jcef;

import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class JCEFSimpleBrowser {
	private static CefApp cefApp;
	private static CefClient cefClient;
	private static CefBrowser cefBrowser;

	public static void main(String[] args) {
		// 2. 初始化CefApp（全局唯一，必须先启动）
		cefApp = JcefBootstrap.setup();
		// 3. 创建CefClient（浏览器客户端，处理事件） 创建一个浏览器客户端实例
		cefClient = cefApp.createClient();
		// 4. 创建浏览器实例（参数：是否透明、初始URL）
		// 初始URL可以是本地文件（file:///D:/test.html）、网络地址、或空白（about:blank）
		// 第二个参数: useOSR
		cefBrowser = cefClient.createBrowser("https://www.baidu.com", false, false);

		// 5. 获取浏览器的Swing组件，嵌入窗口
		Component browserComponent = cefBrowser.getUIComponent();

		// 6. 创建Swing窗口并添加浏览器组件
		JFrame frame = new JFrame("JCEF 简单浏览器");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // 手动处理关闭
		frame.setLayout(new BorderLayout());
		frame.add(browserComponent, BorderLayout.CENTER);
		frame.setSize(1000, 700);
		frame.setLocationRelativeTo(null); // 窗口居中

		// 7. 处理窗口关闭事件（必须释放JCEF资源，否则会崩溃）
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// 关闭浏览器
				cefBrowser.close(true);
				// 释放Client
				cefClient.dispose();
				// 关闭CefApp并等待退出
				cefApp.dispose();
				// 退出程序
				System.exit(0);
			}
		});

		// 8. 显示窗口
		frame.setVisible(true);
		// 可选：注册JS和Java的通信路由（JS调用Java方法）

		// 添加一个方法用于html调用它，在html中执行 window.javaQuery({...})
		CefMessageRouter.CefMessageRouterConfig config = new CefMessageRouter.CefMessageRouterConfig();
		config.jsQueryFunction = "javaQuery";// 定义方法
		config.jsCancelFunction = "javaQueryCancel";// 定义取消方法
		CefMessageRouter messageRouter = CefMessageRouter.create(config);
		messageRouter.addHandler(new CefMessageRouterHandlerAdapter() {
			@Override
			public boolean onQuery(CefBrowser browser, CefFrame frame, long queryId, String request, boolean persistent, CefQueryCallback callback) {
				if ("my_call".equals(request)){
					// 返回影响成功的数据
					callback.success("get java data success!");
					browser.executeJavaScript("htmlFun('哈哈')",null,2);// 调用js中定义的方法
					// callback.failure();
					return true;
				}
				return false;
			}
		}, false);
		cefClient.addMessageRouter(messageRouter);

	}
}