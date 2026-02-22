package org.assistant.tools.doc.parser;

import com.github.javaparser.ast.CompilationUnit;
import org.assistant.tools.doc.ApiGroup;

import java.util.List;

/**
 * SPI interface for framework-specific API parsers.
 * <p>
 * Implementations detect and parse web API annotations from a particular
 * framework (e.g. Spring MVC, JAX-RS). New frameworks can be supported by
 * adding new implementations.
 * </p>
 *
 * <h3>Extensibility</h3>
 * To add support for a new web framework:
 * <ol>
 * <li>Create a new class implementing {@code FrameworkApiParser}</li>
 * <li>Register it in {@link WebApiScanner#getDefaultParsers()}</li>
 * </ol>
 */
public interface FrameworkApiParser {

    /**
     * A human-readable name for this parser (e.g. "Spring MVC", "JAX-RS").
     */
    String getFrameworkName();

    /**
     * Check whether the given compilation unit contains annotations belonging to
     * this framework.
     *
     * @param cu a parsed Java file
     * @return true if this parser should handle the file
     */
    boolean supports(CompilationUnit cu);

    /**
     * Parse all API endpoints from the given compilation unit.
     *
     * @param cu a parsed Java file that {@link #supports(CompilationUnit)} returned
     *           true for
     * @return list of API groups (typically one per controller class in the file)
     */
    List<ApiGroup> parse(CompilationUnit cu);

    /**
     * Parse all API endpoints, using a {@link TypeResolver} to resolve complex
     * parameter types into their field definitions.
     *
     * @param cu           a parsed Java file
     * @param typeResolver resolver for field-level info on DTOs/POJOs, may be null
     * @return list of API groups
     */
    default List<ApiGroup> parse(CompilationUnit cu, TypeResolver typeResolver) {
        return parse(cu);
    }
}
