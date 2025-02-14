/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.repository.conan.internal.orient.metadata;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.repository.storage.TempBlob;
import org.sonatype.repository.conan.internal.metadata.ConanManifest;

import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ConanManifestTest
    extends TestSupport
{

  @Mock
  TempBlob blob;

  @Test
  public void canParse() {
    when(blob.get()).thenAnswer( stream -> getClass().getResourceAsStream("conanmanifest.txt"));

    AttributesMap attributesMap = ConanManifest.parse(blob);

    assertTrue(attributesMap.get(".c_src/CMakeLists.txt").equals("9fd53df6571335b5080891a9b40e66b2"));
    assertTrue(attributesMap.get("conanfile.py").equals("9ea8083ad1c71182fa64ca0378bade18"));
  }
}
