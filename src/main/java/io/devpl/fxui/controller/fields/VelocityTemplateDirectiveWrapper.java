package io.devpl.fxui.controller.fields;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.ASTDirective;
import org.apache.velocity.runtime.parser.node.Node;

public class VelocityTemplateDirectiveWrapper extends VelocityTemplateDirective {

    /**
     * 实现了TemplateDirective接口，但是并未与任何模板引擎对应的指令实现类有继承关系
     */
    private TemplateDirective directive;
    private boolean provideScope = false;

    public VelocityTemplateDirectiveWrapper() {
    }

    public VelocityTemplateDirectiveWrapper(TemplateDirective directive) {
        this.directive = directive;
    }

    @Override
    public String getName() {
        return this.directive.getName();
    }

    /**
     * @param rs      RuntimeServices
     * @param context Context
     * @param node    ASTDirective
     * @throws TemplateInitException TemplateInitException
     * @see Directive#init(RuntimeServices, InternalContextAdapter, Node)
     */
    @Override
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node) throws TemplateInitException {
        this.rsvc = rs;
        if (this.directive == null) {
            if (node instanceof ASTDirective directiveNode) {
                String directiveName = directiveNode.getDirectiveName();
                Directive directiveInstance = rs.getDirective(directiveName);
                if (directiveInstance instanceof VelocityTemplateDirectiveWrapper tdw) {
                    this.directive = tdw.getDirective();
                }
            }
        }
        if (this.directive == null) {
            throw new TemplateException("cannot initialize template directive");
        }
        provideScope = rsvc.isScopeControlEnabled(getScopeName());
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return this.directive.getParameterTypes();
    }

    @Override
    public boolean isScopeProvided() {
        return this.provideScope;
    }

    @Override
    public String render(Object[] params) {
        return this.directive.render(params);
    }

    public final TemplateDirective getDirective() {
        return this.directive;
    }
}
