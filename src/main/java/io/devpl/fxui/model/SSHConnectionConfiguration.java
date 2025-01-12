package io.devpl.fxui.model;

import lombok.Getter;
import lombok.Setter;

/**
 * SSH连接信息
 */
@Getter
@Setter
public class SSHConnectionConfiguration {
    private String lport;
    private String rport;
    private String sshPort;
    private String sshHost;
    private String sshUser;
    private String sshPassword;
    private String privateKeyPassword;
    private String privateKey;
}
