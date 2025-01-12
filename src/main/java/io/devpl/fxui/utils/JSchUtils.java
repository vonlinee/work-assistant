package io.devpl.fxui.utils;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.devpl.fxui.model.DatabaseInfo;
import io.devpl.sdk.util.StringUtils;
import io.devpl.fxui.fxtras.Alerts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ssh连接工具
 */
public class JSchUtils {

    static Logger log = LoggerFactory.getLogger(JSchUtils.class);

    private static final JSch jsch = new JSch();

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static volatile boolean portForwaring = false;
    private static final Map<Integer, Session> portForwardingSession = new ConcurrentHashMap<>();

    public static Session getSSHSession(DatabaseInfo databaseConfig) {
        if (StringUtils.isBlank(databaseConfig.getSshHost()) || StringUtils.isBlank(databaseConfig.getSshPort()) || StringUtils.isBlank(databaseConfig.getSshUser()) || (StringUtils.isBlank(databaseConfig.getPrivateKey()) && StringUtils.isBlank(databaseConfig.getSshPassword()))) {
            return null;
        }
        Session session = null;
        try {
            // Set StrictHostKeyChecking property to no to avoid UnknownHostKey issue
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            Integer port = NumberUtils.decode(databaseConfig.getSshPort(), 22);
            session = jsch.getSession(databaseConfig.getSshUser(), databaseConfig.getSshHost(), port);
            if (StringUtils.hasText(databaseConfig.getPrivateKey())) {
                // 使用秘钥方式认证
                jsch.addIdentity(databaseConfig.getPrivateKey(), StringUtils.whenBlank(databaseConfig.getPrivateKeyPassword(), null));
            } else {
                session.setPassword(databaseConfig.getSshPassword());
            }
            session.setConfig(config);
        } catch (JSchException e) {
            // Ignore
        }
        return session;
    }

    public static void shutdownPortForwarding(Session session) {
        portForwaring = false;
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    public static void engagePortForwarding(Session sshSession, DatabaseInfo config) {
        if (sshSession != null) {
            AtomicInteger assinged_port = new AtomicInteger();
            Future<?> result = executorService.submit(() -> {
                try {
                    Integer lport = NumberUtils.decode(config.getLport(), NumberUtils.parseInteger(config.getLport(), 3306));
                    Integer rport = NumberUtils.decode(config.getRport(), NumberUtils.parseInteger(config.getRport(), 3306));
                    Session session = portForwardingSession.get(lport);
                    if (session != null && session.isConnected()) {
                        String s = session.getPortForwardingL()[0];
                        String[] split = StringUtils.split(s, ":");
                        boolean portForwarding = String.format("%s:%s", split[0], split[1])
                            .equals(lport + ":" + config.getHost());
                        if (portForwarding) {
                            return;
                        }
                    }
                    sshSession.connect();
                    assinged_port.set(sshSession.setPortForwardingL(lport, config.getHost(), rport));
                    portForwardingSession.put(lport, sshSession);
                    portForwaring = true;
                    log.info("portForwarding Enabled, {}", assinged_port);
                } catch (JSchException e) {
                    log.error("Connect Over SSH failed", e);
                    if (e.getCause() != null && e.getCause()
                        .getMessage()
                        .equals("Address already in use: JVM_Bind")) {
                        throw new RuntimeException("Address already in use: JVM_Bind");
                    }
                    throw new RuntimeException(e.getMessage());
                }
            });
            try {
                result.get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                shutdownPortForwarding(sshSession);
                if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) e.getCause();
                }
                if (e instanceof TimeoutException) {
                    throw new RuntimeException("OverSSH 连接超时：超过5秒");
                }
                log.info("executorService isShutdown:{}", executorService.isShutdown());
                Alerts.error("OverSSH 失败，请检查连接设置:" + e.getMessage())
                    .showAndWait();
            }
        }
    }
}
