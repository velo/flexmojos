package org.sonatype.flexmojos.generator;

import java.io.File;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;

@Component( role = Generator.class, hint = "dummy" )
public class DummyGenerator
    implements Generator
{

    private GeneratorLogger logger;

    private Map<String, File> classes;

    private ClassLoader classLoader;

    private Map<String, String> extraOptions;

    private File persistentOutputFolder;

    private Map<String, String> template;

    private File transientOutputFolder;

    private String[] translators;

    public void generate( GenerationRequest request )
        throws GenerationException
    {
        this.logger = request.getLogger();
        this.classes = request.getClasses();
        this.classLoader = request.getClassLoader();
        this.extraOptions = request.getExtraOptions();
        this.persistentOutputFolder = request.getPersistentOutputFolder();
        this.template = request.getTemplates();
        this.transientOutputFolder = request.getTransientOutputFolder();
        this.translators = request.getTranslators();

        throw new GenerationException();
    }

    public GeneratorLogger getLogger()
    {
        return logger;
    }

    public Map<String, File> getClasses()
    {
        return classes;
    }

    public ClassLoader getClassLoader()
    {
        return classLoader;
    }

    public Map<String, String> getExtraOptions()
    {
        return extraOptions;
    }

    public File getPersistentOutputFolder()
    {
        return persistentOutputFolder;
    }

    public Map<String, String> getTemplate()
    {
        return template;
    }

    public File getTransientOutputFolder()
    {
        return transientOutputFolder;
    }

    public String[] getTranslators()
    {
        return translators;
    }

}
