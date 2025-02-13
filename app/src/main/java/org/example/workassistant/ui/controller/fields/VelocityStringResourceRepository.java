package org.example.workassistant.ui.controller.fields;

import org.apache.velocity.runtime.resource.util.StringResource;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Velocity要求所有模板需要预先定义好，然后通过指定的ResourceLoader进行加载
 * 字符串模板也是一样，需要预先指定一个模板名称，模板内容作为value，加载时通过模板名称进行加载
 */
public class VelocityStringResourceRepository implements StringResourceRepository {

    private final Map<String, StringResource> resourceMap = new ConcurrentHashMap<>();
    private final Map<Integer, StringResource> cachedStringResourceMap = new WeakHashMap<>();

    /**
     * @param name 字符串模板名称，或者字符串模板内容（运行时动态加载）
     * @return StringResource
     */
    @Override
    public StringResource getStringResource(String name) {
        if ("velocimacros.vtl".equals(name)) {
            return null;
        }
        StringResource resource = resourceMap.get(name);
        if (resource == null) {
            final int hashCode = name.hashCode();
            if ((resource = cachedStringResourceMap.get(hashCode)) == null) {
                resource = new StringResource(name, this.getEncoding());
                cachedStringResourceMap.put(hashCode, resource);
            }
        }
        return resource;
    }

    @Override
    public void putStringResource(String name, String body) {
        resourceMap.put(name, new StringResource(body, getEncoding()));
    }

    @Override
    public void putStringResource(String name, String body, String encoding) {
        resourceMap.put(name, new StringResource(body, encoding));
    }

    @Override
    public void removeStringResource(String name) {
        resourceMap.remove(name);
    }

    @Override
    public String getEncoding() {
        return StandardCharsets.UTF_8.name();
    }

    @Override
    public void setEncoding(String encoding) {
        // 固定UTF-8
    }
}
