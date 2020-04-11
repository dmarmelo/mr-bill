package dev.estgp.is.utils.freemarker;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Freemarker template engine
 */
public class FreemarkerEngine {

    private Configuration cfg;

    public FreemarkerEngine(String pathname) {
        try {
            cfg = new Configuration(Configuration.VERSION_2_3_30);
            cfg.setDirectoryForTemplateLoading(new File(pathname));
            cfg.setDefaultEncoding("UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FreemarkerEngine(Class<?> resourceLoaderClass, String basePackagePath) {
        cfg = new Configuration(Configuration.VERSION_2_3_28);
        // To load the templates from the "templates" folder in the resources folder
        cfg.setClassForTemplateLoading(resourceLoaderClass, basePackagePath);
        cfg.setDefaultEncoding("UTF-8");
    }

    /**
     * Renders a model using a template.
     *
     * @param model the model
     * @param name  the template name
     * @return      String
     */
    public String render(Object model, String name) {
        try {
            Template template = cfg.getTemplate(name);
            Writer out = new StringWriter();
            template.process(model, out);
            return out.toString();
        } catch (TemplateException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
