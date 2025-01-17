/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.deployment.model.internal.application;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.mule.runtime.api.util.Preconditions.checkArgument;
import static org.mule.runtime.api.util.Preconditions.checkState;
import static org.mule.runtime.module.artifact.api.classloader.ParentFirstLookupStrategy.PARENT_FIRST;

import org.mule.runtime.api.util.collection.SmallMap;
import org.mule.runtime.deployment.model.api.DeployableArtifactDescriptor;
import org.mule.runtime.deployment.model.api.application.Application;
import org.mule.runtime.deployment.model.api.application.ApplicationDescriptor;
import org.mule.runtime.deployment.model.api.builder.ApplicationClassLoaderBuilder;
import org.mule.runtime.deployment.model.api.builder.RegionPluginClassLoadersFactory;
import org.mule.runtime.deployment.model.api.plugin.ArtifactPluginDescriptor;
import org.mule.runtime.deployment.model.internal.AbstractArtifactClassLoaderBuilder;
import org.mule.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import org.mule.runtime.module.artifact.api.classloader.ClassLoaderLookupPolicy;
import org.mule.runtime.module.artifact.api.classloader.DeployableArtifactClassLoaderFactory;
import org.mule.runtime.module.artifact.api.classloader.LookupStrategy;
import org.mule.runtime.module.artifact.api.classloader.MuleDeployableArtifactClassLoader;
import org.mule.runtime.module.artifact.api.classloader.RegionClassLoader;
import org.mule.runtime.module.artifact.api.descriptor.ArtifactDescriptor;

import java.io.IOException;
import java.util.Map;

/**
 * {@link ArtifactClassLoader} builder for class loaders required by {@link Application} artifacts
 *
 * @since 4.0
 */
public class DefaultApplicationClassLoaderBuilder extends AbstractArtifactClassLoaderBuilder<DefaultApplicationClassLoaderBuilder>
    implements ApplicationClassLoaderBuilder {

  private final DeployableArtifactClassLoaderFactory artifactClassLoaderFactory;
  private ArtifactClassLoader domainArtifactClassLoader;

  /**
   * Creates a new builder for creating {@link Application} artifacts.
   * <p>
   * The {@code domainRepository} is used to locate the domain that this application belongs to and the
   * {@code artifactClassLoaderBuilder} is used for building the common parts of artifacts.
   *
   * @param artifactClassLoaderFactory factory for the classloader specific to the artifact resource and classes
   * @param pluginClassLoadersFactory  creates the class loaders for the plugins included in the application's region. Non null
   */
  public DefaultApplicationClassLoaderBuilder(DeployableArtifactClassLoaderFactory<ApplicationDescriptor> artifactClassLoaderFactory,
                                              RegionPluginClassLoadersFactory pluginClassLoadersFactory) {
    super(pluginClassLoadersFactory);

    this.artifactClassLoaderFactory = artifactClassLoaderFactory;
  }

  /**
   * Creates a new {@code MuleDeployableArtifactClassLoader} using the provided configuration. It will create the proper class
   * loader hierarchy and filters so application classes, resources, plugins and it's domain resources are resolve correctly.
   *
   * @return a {@code MuleDeployableArtifactClassLoader} created from the provided configuration.
   * @throws IOException exception cause when it was not possible to access the file provided as dependencies
   */
  @Override
  public MuleDeployableArtifactClassLoader build() {
    checkState(domainArtifactClassLoader != null, "Domain cannot be null");

    return (MuleDeployableArtifactClassLoader) super.build();
  }

  @Override
  protected ArtifactClassLoader createArtifactClassLoader(String artifactId, RegionClassLoader regionClassLoader) {
    return artifactClassLoaderFactory.create(artifactId, regionClassLoader, artifactDescriptor, artifactPluginClassLoaders);
  }

  @Override
  protected String getArtifactId(ArtifactDescriptor artifactDescriptor) {
    return getApplicationId(domainArtifactClassLoader.getArtifactId(), artifactDescriptor.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected ArtifactClassLoader getParentClassLoader() {
    return this.domainArtifactClassLoader;
  }

  /**
   * @param domain the domain artifact to which the application that is going to use this classloader belongs.
   * @return the builder
   */
  @Override
  public DefaultApplicationClassLoaderBuilder setDomainParentClassLoader(ArtifactClassLoader domainArtifactClassLoader) {
    this.domainArtifactClassLoader = domainArtifactClassLoader;
    return this;
  }

  /**
   * @param domainId      name of the domain where the application is deployed. Non empty.
   * @param applicationId id of the application. Non empty.
   * @return the unique identifier for the application in the container.
   */
  public static String getApplicationId(String domainId, String applicationId) {
    checkArgument(!isEmpty(domainId), "domainId cannot be empty");
    checkArgument(!isEmpty(applicationId), "applicationName cannot be empty");

    return domainId + "/app/" + applicationId;
  }

  @Override
  protected ClassLoaderLookupPolicy getParentLookupPolicy(ArtifactClassLoader parentClassLoader) {
    Map<String, LookupStrategy> lookupStrategies = new SmallMap<>();

    ArtifactDescriptor descriptor = parentClassLoader.getArtifactDescriptor();
    descriptor.getClassLoaderModel().getExportedPackages().forEach(p -> lookupStrategies.put(p, PARENT_FIRST));

    if (descriptor instanceof DeployableArtifactDescriptor) {
      for (ArtifactPluginDescriptor artifactPluginDescriptor : ((DeployableArtifactDescriptor) descriptor).getPlugins()) {
        artifactPluginDescriptor.getClassLoaderModel().getExportedPackages()
            .forEach(p -> lookupStrategies.put(p, PARENT_FIRST));
      }
    }

    return parentClassLoader.getClassLoaderLookupPolicy().extend(lookupStrategies);
  }
}
