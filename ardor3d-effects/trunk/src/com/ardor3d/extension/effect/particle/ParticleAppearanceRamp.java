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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadableColorRGBA;
import com.ardor3d.util.export.Ardor3DExporter;
import com.ardor3d.util.export.Ardor3DImporter;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;
import com.ardor3d.util.export.Savable;

public class ParticleAppearanceRamp implements Savable {

    protected List<RampEntry> entries = new ArrayList<RampEntry>();

    public void addEntry(final RampEntry entry) {
        entries.add(entry);
    }

    public void addEntry(final int index, final RampEntry entry) {
        entries.add(index, entry);
    }

    public void clearEntries() {
        entries.clear();
    }

    public Iterator<RampEntry> getEntries() {
        return entries.iterator();
    }

    public void removeEntry(final RampEntry entry) {
        entries.remove(entry);
    }

    public void removeEntry(final int index) {
        entries.remove(index);
    }

    public void getValuesAtAge(final double age, final double maxAge, final ColorRGBA store, final double[] fStore,
            final ParticleSystem particles) {
        double prevCAge = 0, prevMAge = 0, prevSiAge = 0, prevSpAge = 0;
        double nextCAge = maxAge, nextMAge = maxAge, nextSiAge = maxAge, nextSpAge = maxAge;
        double trAge = 0;
        RampEntry prevCEntry = null, prevMEntry = null, prevSiEntry = null, prevSpEntry = null;
        RampEntry nextCEntry = null, nextMEntry = null, nextSiEntry = null, nextSpEntry = null;
        for (int i = 0; i < entries.size(); i++) {
            final RampEntry entry = entries.get(i);
            trAge += entry.getOffset() * maxAge;
            // Color
            if (nextCEntry == null) {
                if (trAge > age) {
                    if (entry.hasColorSet()) {
                        nextCAge = trAge;
                        nextCEntry = entry;
                    }
                } else {
                    if (entry.hasColorSet()) {
                        prevCAge = trAge;
                        prevCEntry = entry;
                    }
                }
            }

            // mass
            if (nextMEntry == null) {
                if (trAge > age) {
                    if (entry.hasMassSet()) {
                        nextMAge = trAge;
                        nextMEntry = entry;
                    }
                } else {
                    if (entry.hasMassSet()) {
                        prevMAge = trAge;
                        prevMEntry = entry;
                    }
                }
            }

            // size
            if (nextSiEntry == null) {
                if (trAge > age) {
                    if (entry.hasSizeSet()) {
                        nextSiAge = trAge;
                        nextSiEntry = entry;
                    }
                } else {
                    if (entry.hasSizeSet()) {
                        prevSiAge = trAge;
                        prevSiEntry = entry;
                    }
                }
            }

            // spin
            if (nextSpEntry == null) {
                if (trAge > age) {
                    if (entry.hasSpinSet()) {
                        nextSpAge = trAge;
                        nextSpEntry = entry;
                    }
                } else {
                    if (entry.hasSpinSet()) {
                        prevSpAge = trAge;
                        prevSpEntry = entry;
                    }
                }
            }

        }

        // color
        {
            final float lifeCRatio = (float) ((age - prevCAge) / (nextCAge - prevCAge));
            final ReadableColorRGBA start = prevCEntry != null ? prevCEntry.getColor() : particles.getStartColor();
            final ReadableColorRGBA end = nextCEntry != null ? nextCEntry.getColor() : particles.getEndColor();
            ColorRGBA.lerp(start, end, lifeCRatio, store);
        }

        // mass
        {
            final double lifeMRatio = (age - prevMAge) / (nextMAge - prevMAge);
            final double start = prevMEntry != null ? prevMEntry.getMass() : particles.getStartMass();
            final double end = nextMEntry != null ? nextMEntry.getMass() : particles.getEndMass();
            fStore[Particle.VAL_CURRENT_MASS] = (1 - lifeMRatio) * start + lifeMRatio * end;
        }

        // Size
        {
            final double lifeSiRatio = (age - prevSiAge) / (nextSiAge - prevSiAge);
            final double start = prevSiEntry != null ? prevSiEntry.getSize() : particles.getStartSize();
            final double end = nextSiEntry != null ? nextSiEntry.getSize() : particles.getEndSize();
            fStore[Particle.VAL_CURRENT_SIZE] = (1 - lifeSiRatio) * start + lifeSiRatio * end;
        }

        // Spin
        {
            final double lifeSpRatio = (age - prevSpAge) / (nextSpAge - prevSpAge);
            final double start = prevSpEntry != null ? prevSpEntry.getSpin() : particles.getStartSpin();
            final double end = nextSpEntry != null ? nextSpEntry.getSpin() : particles.getEndSpin();
            fStore[Particle.VAL_CURRENT_SPIN] = (1 - lifeSpRatio) * start + lifeSpRatio * end;
        }
    }

    public Class<? extends ParticleAppearanceRamp> getClassTag() {
        return getClass();
    }

    public void read(final Ardor3DImporter im) throws IOException {
        final InputCapsule capsule = im.getCapsule(this);
        entries = capsule.readSavableList("entries", null);
        if (entries == null) {
            entries = new ArrayList<RampEntry>();
        }
    }

    public void write(final Ardor3DExporter ex) throws IOException {
        final OutputCapsule capsule = ex.getCapsule(this);
        capsule.writeSavableList(entries, "entries", null);
    }

}