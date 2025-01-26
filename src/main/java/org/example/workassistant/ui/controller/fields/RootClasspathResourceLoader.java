package org.example.workassistant.ui.controller.fields;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.util.ExtProperties;

import java.io.Reader;

/**
 * 从类路径的根路径开始查找
 *
 * @see org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
 * @see org.apache.velocity.runtime.resource.loader.StringResourceLoader
 * @see org.apache.velocity.runtime.resource.loader.FileResourceLoader
 * @see org.apache.velocity.runtime.resource.loader.URLResourceLoader
 * @see org.apache.velocity.runtime.resource.loader.DataSourceResourceLoader
 */
public class RootClasspathResourceLoader extends ClasspathResourceLoader {

    @Override
    public void init(ExtProperties configuration) {
        super.init(configuration);
    }

    @Override
    public Reader getResourceReader(String source, String encoding) throws ResourceNotFoundException {
        Reader reader = null;
        try {
            reader = super.getResourceReader(source, encoding);
        } catch (Exception exception) {
            // 默认的
        }
        return reader;
    }

    @Override
    public boolean isSourceModified(Resource resource) {
        return false;
    }

    @Override
    public long getLastModified(Resource resource) {
        return 0;
    }
}
