/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.config.internal.model;

import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.util.LazyValue;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.runtime.ast.api.ComponentAst;

import java.util.Collection;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Allows internal Runtime components to customize the {@link ArtifactAst AST} of the artifact being deployed.
 * 
 * @since 4.5
 */
public interface ApplicationModelAstPostProcessor {

  public LazyValue<Iterable<ApplicationModelAstPostProcessor>> AST_POST_PROCESSORS =
      new LazyValue<>(() -> ServiceLoader.load(ApplicationModelAstPostProcessor.class,
                                               ApplicationModelAstPostProcessor.class.getClassLoader()));

  /**
   * Create a new {@link ArtifactAst} based on the provided one, with any required changes applied.
   * 
   * @param ast             the original AST to apply changes on.
   * @param extensionModels the extensions that are registered for this AST's artifact.
   * @return a newly created AST
   */
  ArtifactAst postProcessAst(ArtifactAst ast, Set<ExtensionModel> extensionModels);

  /**
   * Create a new set of root components to create Spring bean definitions for.
   * 
   * @param rootComponents  the root components of an AST.
   * @param extensionModels the extensions that are registered for this AST's artifact.
   * @return a new set of root components to use instead of the ones passed as parameter.
   */
  Set<ComponentAst> resolveRootComponents(Collection<ComponentAst> rootComponents, Set<ExtensionModel> extensionModels);

}
