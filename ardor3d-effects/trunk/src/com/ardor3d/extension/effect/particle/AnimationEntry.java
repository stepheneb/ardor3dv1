/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.extension.effect.particle;

import java.io.IOException;

import com.ardor3d.util.export.Ardor3DExporter;
import com.ardor3d.util.export.Ardor3DImporter;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;
import com.ardor3d.util.export.Savable;

public class AnimationEntry implements Savable {
    protected double offset = 0.05; // 5% of life from previous entry
    protected double rate = 0.2; // 5 fps
    protected int[] frames = new int[1];

    public AnimationEntry() {}

    public AnimationEntry(final double offset) {
        this.offset = offset;
    }

    public int[] getFrames() {
        return frames;
    }

    public void setFrames(final int[] frames) {
        this.frames = frames;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(final double offset) {
        this.offset = offset;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(final double rate) {
        this.rate = rate;
    }

    public Class<? extends AnimationEntry> getClassTag() {
        return getClass();
    }

    public void read(final Ardor3DImporter im) throws IOException {
        final InputCapsule capsule = im.getCapsule(this);
        offset = capsule.readDouble("offsetMS", 0.05);
        rate = capsule.readDouble("rate", 0.2);
        frames = capsule.readIntArray("frames", null);
    }

    public void write(final Ardor3DExporter ex) throws IOException {
        final OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(offset, "offsetMS", 0.05);
        capsule.write(rate, "rate", 0.2);
        capsule.write(frames, "frames", null);
    }

    private static String makeText(final int[] frames) {
        if (frames == null || frames.length == 0) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        for (final int frame : frames) {
            sb.append(frame);
            sb.append(",");
        }
        return sb.substring(0, sb.length() - 1);
    }

    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder();

        builder.append("prev+");
        builder.append((int) (offset * 100));
        builder.append("% age...");

        builder.append("  rate: " + rate);

        builder.append("  sequence: " + makeText(frames));

        return builder.toString();
    }
}
