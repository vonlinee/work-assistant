package org.assistant.tools.doc.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import org.assistant.tools.doc.ApiGroup;
import org.assistant.tools.doc.ApiProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrator that scans Java source files and delegates to registered
 * {@link FrameworkApiParser} implementations.
 *
 * <h3>Extensibility</h3>
 * <p>
 * New framework support is added by:
 * </p>
 * <ol>
 * <li>Implementing {@link FrameworkApiParser}</li>
 * <li>Adding the instance to {@link #addParser(FrameworkApiParser)} or
 * the default list in {@link #getDefaultParsers()}</li>
 * </ol>
 */
public class WebApiScanner {

    private static final Logger log = LoggerFactory.getLogger(WebApiScanner.class);

    private final List<FrameworkApiParser> parsers = new ArrayList<>();

    public WebApiScanner() {
        // Register built-in parsers
        getDefaultParsers().forEach(this::addParser);
    }

    /**
     * Override or extend to provide additional parsers.
     */
    protected List<FrameworkApiParser> getDefaultParsers() {
        List<FrameworkApiParser> defaults = new ArrayList<>();
        defaults.add(new SpringMvcApiParser());
        defaults.add(new JaxRsApiParser());
        return defaults;
    }

    /**
     * Register an additional framework parser.
     */
    public void addParser(FrameworkApiParser parser) {
        this.parsers.add(parser);
    }

    /**
     * Scan a project and return all discovered API definitions.
     *
     * @param projectDir root directory of the project
     * @return the populated ApiProject
     */
    public ApiProject scan(Path projectDir) {
        ProjectParser projectParser = new ProjectParser();
        ProjectInfo projectInfo = projectParser.parse(projectDir);
        return scan(projectInfo);
    }

    /**
     * Scan from explicit source roots (useful when you have already parsed project
     * info).
     */
    public ApiProject scan(ProjectInfo projectInfo) {
        ApiProject apiProject = new ApiProject();
        apiProject.setProjectName(projectInfo.getProjectName());
        apiProject.setVersion(projectInfo.getVersion());
        apiProject.setDescription(projectInfo.getDescription());
        apiProject.setBasePath(projectInfo.getContextPath());

        JavaParser javaParser = new JavaParser();
        TypeResolver typeResolver = new TypeResolver(projectInfo.getSourceRoots());

        for (Path sourceRoot : projectInfo.getSourceRoots()) {
            scanSourceRoot(sourceRoot, javaParser, apiProject, typeResolver);
        }

        return apiProject;
    }

    private void scanSourceRoot(Path sourceRoot, JavaParser javaParser, ApiProject apiProject,
            TypeResolver typeResolver) {
        try {
            Files.walkFileTree(sourceRoot, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.toString().endsWith(".java")) {
                        processJavaFile(file, javaParser, apiProject, typeResolver);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    log.warn("Failed to visit file: {}", file, exc);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.error("Failed to walk source root: {}", sourceRoot, e);
        }
    }

    private void processJavaFile(Path file, JavaParser javaParser, ApiProject apiProject,
            TypeResolver typeResolver) {
        try {
            ParseResult<CompilationUnit> result = javaParser.parse(file);
            if (!result.isSuccessful() || result.getResult().isEmpty()) {
                log.debug("Failed to parse: {}", file);
                return;
            }

            CompilationUnit cu = result.getResult().get();

            for (FrameworkApiParser parser : parsers) {
                if (parser.supports(cu)) {
                    List<ApiGroup> groups = parser.parse(cu, typeResolver);
                    groups.forEach(apiProject::addGroup);
                    break; // Only one framework parser per file
                }
            }
        } catch (IOException e) {
            log.warn("Cannot read file: {}", file, e);
        }
    }
}
