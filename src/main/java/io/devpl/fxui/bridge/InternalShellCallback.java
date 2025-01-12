package io.devpl.fxui.bridge;

import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.exception.ShellException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringTokenizer;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * @see org.mybatis.generator.internal.DefaultShellCallback
 */
public class InternalShellCallback implements ShellCallback {

    protected Logger log = LoggerFactory.getLogger(InternalShellCallback.class);

    private final boolean overwrite;

    public InternalShellCallback(boolean overwrite) {
        super();
        this.overwrite = overwrite;
    }

    @Override
    public File getDirectory(String targetProject, String targetPackage) throws ShellException {
        // targetProject is interpreted as a directory that must exist
        //
        // targetPackage is interpreted as a subdirectory, but in package
        // format (with dots instead of slashes). The subdirectory will be
        // created if it does not already exist

        File project = new File(targetProject);

        Path targetProjectPath = Path.of(targetProject).normalize();

        if (!Files.exists(targetProjectPath)) {
            try {
                Files.createDirectories(targetProjectPath);
            } catch (IOException e) {
                log.error("创建目录失败:{}", targetProjectPath, e);
                throw new RuntimeException(e);
            }
        }

        // 如果目录实际不存在，那么Files.isDirectory返回false
        if (!Files.isDirectory(targetProjectPath)) {
            log.error("不是目录:{}", targetProjectPath);
            throw new ShellException(getString("Warning.9", targetProject));
        }

        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(targetPackage, ".");
        while (st.hasMoreTokens()) {
            sb.append(st.nextToken());
            sb.append(File.separatorChar);
        }
        File directory = new File(project, sb.toString());
        if (!directory.isDirectory()) {
            boolean rc = directory.mkdirs();
            if (!rc) {
                throw new ShellException(getString("Warning.10", directory.getAbsolutePath()));
            }
        }
        return directory;
    }

    @Override
    public boolean isOverwriteEnabled() {
        return overwrite;
    }
}
