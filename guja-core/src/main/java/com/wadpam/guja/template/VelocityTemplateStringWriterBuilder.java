package com.wadpam.guja.template;

import com.google.inject.Inject;
import com.wadpam.guja.admintask.AdminTaskResource;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generate localised string writers based on velocity templates, locale and velocity contexts.
 *
 * @author mattiaslevin
 */
public class VelocityTemplateStringWriterBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(VelocityTemplateStringWriterBuilder.class);

  {
    // Initialize Velocity engine in singleton mode
    final Properties p = new Properties();
    p.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
    p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    p.setProperty("resource.loader", "class");
    p.setProperty("input.encoding", "UTF-8");
    p.setProperty("output.encoding", "UTF-8");
    Velocity.init(p);
  }

  private String templateName;
  private Locale locale;
  private VelocityContext vc;


  @Inject
  public VelocityTemplateStringWriterBuilder() {
    this(null, null, new VelocityContext());
  }
  public VelocityTemplateStringWriterBuilder(String templateName, Locale locale, VelocityContext vc) {
    this.templateName = templateName;
    this.locale = locale;
    this.vc = vc;
  }

  public static VelocityTemplateStringWriterBuilder withTemplate(String templateName) {
    return new VelocityTemplateStringWriterBuilder(templateName, null, new VelocityContext());
  }

  public VelocityTemplateStringWriterBuilder templateName(String name) {
    this.templateName = name;
    return this;
  }

  public VelocityTemplateStringWriterBuilder locale(Locale locale) {
    this.locale = locale;
    return this;
  }

  public VelocityTemplateStringWriterBuilder velocityContext(VelocityContext vc) {
    this.vc = vc;
    return this;
  }

  public VelocityTemplateStringWriterBuilder put(String key, Object value) {
    vc.put(key, value);
    return this;
  }

  public VelocityTemplateStringWriterBuilder put(Map<String, Object> map) {
    if (null != map) {
      for (Map.Entry<String, Object> entry : map.entrySet()) {
        put(entry.getKey(), entry.getValue());
      }
    }
    return this;
  }

  public StringWriter build() {
    checkNotNull(templateName);
    checkNotNull(vc);

    // Load template based on locale
    // Will throw an exception if the template is not found
    Template template = getTemplate(templateName);

    // Merge template and context
    StringWriter writer = new StringWriter();
    template.merge(vc, writer);

    return writer;
  }

  private Template getTemplate(String templateName) {

    final String localizedTemplateName = localizedTemplateName(templateName, locale);
    try {
      // Even if we catch the not-found error here and fix it below, this method will send an Error to the logger, which clutters
      // up the GAE log. This should be fixed somehow.
      return Velocity.getTemplate(localizedTemplateName);
    } catch (ResourceNotFoundException e) {
      // Fall back to default template name without any local postfix
      // If this also fails let the exception propagate
      // This will be logged for every user not in one of the translated locales, so don't do a Warning here, Info is enough
      LOGGER.info("Failed to load localized template [{}], fallback to default template [{}]", localizedTemplateName, templateName);
      return Velocity.getTemplate(templateName);
    }

  }

  private static String localizedTemplateName(String templateName, Locale locale) {

    if (null == locale) {
      return templateName;
    }

    String[] parts = templateName.split("\\.");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid velocity template name, must be in format name.vm");
    }

    return String.format("%s_%s.%s", parts[0], locale.getLanguage(), parts[1]);
	}

}
