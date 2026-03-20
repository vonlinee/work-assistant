package org.assistant.jcef;

import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import me.friwi.jcefmaven.impl.progress.ConsoleProgressHandler;
import org.cef.CefApp;
import org.cef.CefSettings;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class JcefBootstrap {

	/**
	 * 运行时会下载CEF库，并解压到jcef-bundle目录下
	 *
	 * @return CefApp 全局的 CefApp 每个程序只能有一个，线程安全
	 */
	public static CefApp setup() {
		// Create a new CefAppBuilder instance
		CefAppBuilder builder = new CefAppBuilder();
		// Configure the builder instance
		// 设置 cef chrome实例的目录，关键，若不设置他会默认从网络中下载，国外网络可能下载不稳定导致失败
		// 我的 cef chrome 位于项目的chrome目录下 C:\Users\Administrator\Desktop\project\java\demo-desktop\chrome
		builder.setInstallDir(new File("jcef-bundle")); // Default
		// 由于是手动设置cef的chrome，我们要跳过ins检查，防止版本不一致导致从镜像站下载
		builder.setSkipInstallation(true);

		builder.setProgressHandler(new ConsoleProgressHandler()); // Default
		builder.addJcefArgs("--disable-gpu"); // Just an example
		CefSettings settings = builder.getCefSettings();
		settings.windowless_rendering_enabled = true; // Default - select OSR mode, window下不需要OSR
		// 设置日志级别（0=静默，1=错误，2=警告，3=信息，4=详细）
		settings.log_severity = CefSettings.LogSeverity.LOGSEVERITY_INFO;
		// 关闭 GPU 加速（可选，根据系统配置）
		settings.windowless_rendering_enabled = false;
		// Set an app handler. Do not use CefApp.addAppHandler(...), it will break your code on MacOSX!
		builder.setAppHandler(new MavenCefAppHandlerAdapter() {
			@Override
			public void stateHasChanged(CefApp.CefAppState state) {
				// 关闭应用时退出jvm运行
				if (state == CefApp.CefAppState.TERMINATED) System.exit(0);
			}
		});
		// Build a CefApp instance using the configuration above
		try {
			return builder.build();
		} catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}
	}

	public static void registerDisposeEventHandler(Window window) {
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// 关闭应用时要释放资源
				CefApp.getInstance().dispose();
				window.dispose();
				System.exit(0); // 0-正常退出，1-非正常退出
			}
		});
	}
}
