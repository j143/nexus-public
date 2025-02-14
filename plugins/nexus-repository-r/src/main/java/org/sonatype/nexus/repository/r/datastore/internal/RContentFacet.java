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
package org.sonatype.nexus.repository.r.datastore.internal;

import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.repository.Facet.Exposed;
import org.sonatype.nexus.repository.content.facet.ContentFacetSupport;
import org.sonatype.nexus.repository.content.fluent.FluentAsset;
import org.sonatype.nexus.repository.content.fluent.FluentComponent;
import org.sonatype.nexus.repository.content.store.FormatStoreManager;
import org.sonatype.nexus.repository.r.RFormat;
import org.sonatype.nexus.repository.r.internal.AssetKind;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.TempBlob;

import org.apache.commons.lang3.StringUtils;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.r.internal.AssetKind.ARCHIVE;
import static org.sonatype.nexus.repository.r.internal.RAttributes.P_PACKAGE;
import static org.sonatype.nexus.repository.r.internal.RAttributes.P_VERSION;
import static org.sonatype.nexus.repository.r.internal.util.RDescriptionUtils.extractDescriptionFromArchive;
import static org.sonatype.nexus.repository.r.internal.util.RMetadataUtils.HASH_ALGORITHMS;
import static org.sonatype.nexus.repository.r.internal.util.RPathUtils.getBasePath;

/**
 * R content facet.
 *
 * @since 3.next
 */
@Exposed
@Named(RFormat.NAME)
public class RContentFacet
    extends ContentFacetSupport
{
  @Inject
  public RContentFacet(@Named(RFormat.NAME) final FormatStoreManager formatStoreManager)
  {
    super(formatStoreManager);
  }

  /**
   * Get an asset by path.
   *
   * @param assetPath the asset path.
   * @return the {@link FluentAsset} object.
   */
  public Optional<FluentAsset> getAsset(final String assetPath) {
    checkNotNull(assetPath);

    return assets().path(normalizeAssetPath(assetPath)).find();
  }

  /**
   * Store metadata with a given payload.
   *
   * @param payload   the payload which will be attached to the asset.
   * @param assetPath the asset path.
   * @param assetKind the asset kind.
   * @return the {@link FluentAsset} object.
   */
  public FluentAsset putMetadata(final Payload payload, final String assetPath, final AssetKind assetKind)
  {
    checkNotNull(payload);
    checkNotNull(assetKind);
    checkNotNull(assetPath);

    try (TempBlob tempBlob = blobs().ingest(payload, HASH_ALGORITHMS)) {
      return assets()
          .path(normalizeAssetPath(assetPath))
          .kind(assetKind.name())
          .blob(tempBlob)
          .save();
    }
  }

  /**
   * Store package with a given payload.
   *
   * @param payload   the payload which will be attached to the asset.
   * @param assetPath the path of an asset.
   * @return the {@link FluentAsset} object.
   */
  public FluentAsset putPackage(final Payload payload, final String assetPath) {
    checkNotNull(payload);
    checkNotNull(assetPath);

    try (TempBlob tempBlob = blobs().ingest(payload, HASH_ALGORITHMS)) {
      Map<String, String> attributes = extractDescriptionFromArchive(assetPath, tempBlob);
      String name = attributes.get(P_PACKAGE);
      String version = attributes.get(P_VERSION);
      String namespace = getBasePath(assetPath);
      FluentComponent component = findOrCreateComponent(name, namespace, version);

      return component
          .asset(normalizeAssetPath(assetPath))
          .kind(ARCHIVE.name())
          .attributes(RFormat.NAME, attributes)
          .blob(tempBlob)
          .save();
    }
  }

  private FluentComponent findOrCreateComponent(final String name, final String namespace, final String version) {
    return components()
        .name(name)
        .namespace(namespace)
        .version(version)
        .getOrCreate();
  }

  /**
   * Returns path with appended string on the beginning.
   *
   * @param path - Any path e.g. 'some/path/example'
   * @return - the path, e.g. '/some/path/example'
   */
  private static String normalizeAssetPath(final String path) {
    return StringUtils.prependIfMissing(path, "/");
  }
}
