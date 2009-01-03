/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

import com.ardor3d.image.Image;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture.MinificationFilter;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.util.export.Ardor3DExporter;
import com.ardor3d.util.export.Ardor3DImporter;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;
import com.ardor3d.util.export.Savable;
import com.ardor3d.util.resource.ResourceLocatorTool;

/**
 * <code>TextureKey</code> provides a way for the TextureManager to cache and retrieve <code>Texture</code> objects.
 */
final public class TextureKey implements Savable {

    protected URL location = null;
    protected boolean flipped;
    protected Texture.MinificationFilter minFilter = MinificationFilter.Trilinear;
    protected Image.Format format = Image.Format.Guess;
    protected String fileType;
    protected transient RenderContext context = null;

    public TextureKey() {}

    public TextureKey(final URL location, final boolean flipped, final Image.Format imageType,
            final Texture.MinificationFilter minFilter) {
        this.location = location;
        this.flipped = flipped;
        this.minFilter = minFilter;
        format = imageType;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof TextureKey)) {
            return false;
        }

        final TextureKey that = (TextureKey) other;
        if (location == null) {
            if (that.location != null) {
                return false;
            }
        } else if (!location.equals(that.location)) {
            return false;
        }
        if (context == null) {
            if (that.context != null) {
                return false;
            }
        } else if (!context.equals(that.context)) {
            return false;
        }

        if (flipped != that.flipped) {
            return false;
        }
        if (format != that.format) {
            return false;
        }
        if (fileType == null && that.fileType != null) {
            return false;
        } else if (fileType != null && !fileType.equals(that.fileType)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 17;

        result += 31 * result + (location != null ? location.hashCode() : 0);
        result += 31 * result + (fileType != null ? fileType.hashCode() : 0);
        result += 31 * result + minFilter.hashCode();
        result += 31 * result + format.hashCode();
        result += 31 * result + (flipped ? 1 : 0);
        result += 31 * result + (context != null ? context.hashCode() : 0);

        return result;
    }

    public Texture.MinificationFilter getMinificationFilter() {
        return minFilter;
    }

    public void setMinificationFilter(final Texture.MinificationFilter minFilter) {
        this.minFilter = minFilter;
    }

    public RenderContext getContext() {
        return context;
    }

    public void setContext(final RenderContext context) {
        this.context = context;
    }

    public Image.Format getFormat() {
        return format;
    }

    public void setFormat(final Image.Format format) {
        this.format = format;
    }

    /**
     * @return Returns the flipped.
     */
    public boolean isFlipped() {
        return flipped;
    }

    /**
     * @param flipped
     *            The flipped to set.
     */
    public void setFlipped(final boolean flipped) {
        this.flipped = flipped;
    }

    /**
     * @return Returns the location.
     */
    public URL getLocation() {
        return location;
    }

    /**
     * @param location
     *            The location to set.
     */
    public void setLocation(final URL location) {
        this.location = location;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(final String fileType) {
        this.fileType = fileType;
    }

    @Override
    public String toString() {
        final String x = "tkey: loc:" + location + " flip: " + flipped + " code: " + hashCode() + " imageType: "
                + format + " fileType: " + fileType;
        return x;
    }

    // /////////////////
    // Methods for Savable
    // /////////////////

    public Class<? extends TextureKey> getClassTag() {
        return this.getClass();
    }

    public void write(final Ardor3DExporter e) throws IOException {
        final OutputCapsule capsule = e.getCapsule(this);
        if (location != null) {
            capsule.write(location.getProtocol(), "protocol", null);
            capsule.write(location.getHost(), "host", null);
            capsule.write(location.getFile(), "file", null);
        }
        capsule.write(flipped, "flipped", false);
        capsule.write(format, "format", Image.Format.Guess);
        capsule.write(minFilter, "minFilter", MinificationFilter.Trilinear);
        capsule.write(fileType, "fileType", null);
    }

    public void read(final Ardor3DImporter e) throws IOException {
        final InputCapsule capsule = e.getCapsule(this);
        final String protocol = capsule.readString("protocol", null);
        final String host = capsule.readString("host", null);
        final String file = capsule.readString("file", null);
        if (file != null) {
            location = ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_TEXTURE, URLDecoder.decode(file,
                    "UTF-8"));
        }
        if (location == null && protocol != null && host != null && file != null) {
            location = new URL(protocol, host, file);
        }

        flipped = capsule.readBoolean("flipped", false);
        format = capsule.readEnum("format", Image.Format.class, Image.Format.Guess);
        minFilter = capsule.readEnum("minFilter", MinificationFilter.class, MinificationFilter.Trilinear);
        fileType = capsule.readString("fileType", null);
    }

}