/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.framework;

public class DisplaySettings {
    private final int width;
    private final int height;
    private final int colorDepth;
    private final int frequency;
    private final int alphaBits;
    private final int depthBits;
    private final int stencilBits;
    private final int samples;
    private final boolean fullScreen;

    public DisplaySettings(final int width, final int height, final int colorDepth, final int frequency,
            final int alphaBits, final int depthBits, final int stencilBits, final int samples, final boolean fullScreen) {
        this.width = width;
        this.height = height;
        this.colorDepth = colorDepth;
        this.frequency = frequency;
        this.alphaBits = alphaBits;
        this.depthBits = depthBits;
        this.stencilBits = stencilBits;
        this.samples = samples;
        this.fullScreen = fullScreen;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getColorDepth() {
        return colorDepth;
    }

    public int getFrequency() {
        return frequency;
    }

    public int getAlphaBits() {
        return alphaBits;
    }

    public int getDepthBits() {
        return depthBits;
    }

    public int getStencilBits() {
        return stencilBits;
    }

    public int getSamples() {
        return samples;
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final DisplaySettings that = (DisplaySettings) o;

        if (colorDepth != that.colorDepth) {
            return false;
        }
        if (frequency != that.frequency) {
            return false;
        }
        if (fullScreen != that.fullScreen) {
            return false;
        }
        if (height != that.height) {
            return false;
        }
        if (width != that.width) {
            return false;
        }
        if (alphaBits != that.alphaBits) {
            return false;
        }
        if (depthBits != that.depthBits) {
            return false;
        }
        if (stencilBits != that.stencilBits) {
            return false;
        }
        if (samples != that.samples) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = 17;
        result = 31 * result + height;
        result = 31 * result + width;
        result = 31 * result + colorDepth;
        result = 31 * result + frequency;
        result = 31 * result + alphaBits;
        result = 31 * result + depthBits;
        result = 31 * result + stencilBits;
        result = 31 * result + samples;
        result = 31 * result + (fullScreen ? 1 : 0);
        return result;
    }
}
