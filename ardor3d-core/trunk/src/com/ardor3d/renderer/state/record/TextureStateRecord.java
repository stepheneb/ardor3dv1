/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.renderer.state.record;

import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.HashMap;

import com.ardor3d.image.Texture;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.util.geom.BufferUtils;

public class TextureStateRecord extends StateRecord {

    public FloatBuffer eyePlaneS = BufferUtils.createFloatBuffer(4);
    public FloatBuffer eyePlaneT = BufferUtils.createFloatBuffer(4);
    public FloatBuffer eyePlaneR = BufferUtils.createFloatBuffer(4);
    public FloatBuffer eyePlaneQ = BufferUtils.createFloatBuffer(4);

    public HashMap<Integer, TextureRecord> textures;
    public TextureUnitRecord[] units;
    public int hint = -1;
    public int currentUnit = -1;

    /**
     * temporary rotation axis vector to flatline memory usage.
     */
    public final Vector3 tmp_rotation1 = new Vector3();

    /**
     * temporary matrix buffer to flatline memory usage.
     */
    public final FloatBuffer tmp_matrixBuffer = BufferUtils.createFloatBuffer(16);

    public TextureStateRecord() {
        textures = new HashMap<Integer, TextureRecord>();
        units = new TextureUnitRecord[TextureState.MAX_TEXTURES];
        for (int i = 0; i < units.length; i++) {
            units[i] = new TextureUnitRecord();
        }

        eyePlaneS.put(1.0f).put(0.0f).put(0.0f).put(0.0f);
        eyePlaneT.put(0.0f).put(1.0f).put(0.0f).put(0.0f);
        eyePlaneR.put(0.0f).put(0.0f).put(1.0f).put(0.0f);
        eyePlaneQ.put(0.0f).put(0.0f).put(0.0f).put(1.0f);
    }

    public TextureRecord getTextureRecord(final int textureId, final Texture.Type type) {
        TextureRecord tr = textures.get(textureId);
        if (tr == null) {
            tr = new TextureRecord();
            textures.put(textureId, tr);
        }
        return tr;
    }

    public void removeTextureRecord(final int textureId) {
        textures.remove(textureId);
        for (int i = 0; i < units.length; i++) {
            if (units[i].boundTexture == textureId) {
                units[i].boundTexture = -1;
            }
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        currentUnit = -1;
        hint = -1;
        final Collection<TextureRecord> texs = textures.values();
        for (final TextureRecord tr : texs) {
            tr.invalidate();
        }
        for (int i = 0; i < units.length; i++) {
            units[i].invalidate();
        }
    }

    @Override
    public void validate() {
        super.validate();
        final Collection<TextureRecord> texs = textures.values();
        for (final TextureRecord tr : texs) {
            tr.validate();
        }
        for (int i = 0; i < units.length; i++) {
            units[i].validate();
        }
    }
}
