/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.example.pipeline;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ardor3d.example.ExampleBase;
import com.ardor3d.extension.shape.Torus;
import com.ardor3d.framework.FrameWork;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Image.Format;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.WireframeState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.shape.Quad;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.Timer;
import com.ardor3d.util.export.binary.BinaryExporter;
import com.ardor3d.util.export.binary.BinaryImporter;
import com.ardor3d.util.geom.BufferUtils;
import com.google.inject.Inject;

public class ExportImportExample extends ExampleBase {
    private static final Logger logger = Logger.getLogger(ExportImportExample.class.getName());

    private final Timer _timer;
    private final Matrix3 rotation = new Matrix3();

    private Node originalNode;
    private Node importedNode;

    public static void main(final String[] args) {
        start(ExportImportExample.class);
    }

    @Inject
    public ExportImportExample(final NativeCanvas canvas, final LogicalLayer layer, final FrameWork frameWork,
            final Timer timer) {
        super(canvas, layer, frameWork);
        _timer = timer;
    }

    @Override
    protected void updateExample(final double tpf) {
        final double time = _timer.getTimeInSeconds() * 0.5;

        originalNode.setRotation(rotation.fromAngles(time, time, time));
        importedNode.setRotation(rotation.fromAngles(time, time, time));
    }

    @Override
    protected void initExample() {
        _canvas.setTitle("TestExportImport");

        final Texture bg = TextureManager.load("images/ArdorLogo.jpg", Texture.MinificationFilter.Trilinear,
                Format.GuessNoCompression, true);
        final TextureState bgts = new TextureState();
        bgts.setTexture(bg);
        bgts.setEnabled(true);

        final TextureState ts = new TextureState();
        final Texture t0 = TextureManager.load("images/ArdorLogo.jpg", Texture.MinificationFilter.Trilinear,
                Format.GuessNoCompression, true);
        final Texture tex = TextureManager.load("images/ArdorLogo.jpg", Texture.MinificationFilter.Trilinear,
                Format.GuessNoCompression, true);
        tex.setEnvironmentalMapMode(Texture.EnvironmentalMapMode.SphereMap);
        ts.setTexture(t0, 0);
        ts.setTexture(tex, 1);
        ts.setEnabled(true);

        final Torus torus = new Torus("Torus", 50, 50, 10, 25);
        torus.updateModelBound();
        torus.setRenderState(ts);

        final Quad quad = new Quad("Quad");
        quad.initialize(150, 120);
        quad.updateModelBound();
        quad.setRenderState(bgts);

        final Mesh multiStrip = createMultiStrip();
        multiStrip.updateModelBound();
        multiStrip.setTranslation(0, 0, -30);

        originalNode = new Node("originalNode");
        originalNode.attachChild(torus);
        originalNode.attachChild(quad);
        originalNode.attachChild(multiStrip);

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            BinaryExporter.getInstance().save(originalNode, bos);
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "BinaryExporter failed to save file", e);
        }

        originalNode.setTranslation(new Vector3(-80, 0, -300));
        _root.attachChild(originalNode);

        final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        try {
            importedNode = (Node) BinaryImporter.getInstance().load(bis);
            importedNode.setTranslation(new Vector3(80, 0, -300));
            _root.attachChild(importedNode);
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "BinaryImporter failed to load file", e);
        }
    }

    private Mesh createMultiStrip() {
        final Mesh mesh = new Mesh();
        final MeshData meshData = mesh.getMeshData();

        final FloatBuffer vertexBuffer = BufferUtils.createVector3Buffer(16);

        vertexBuffer.put(-30).put(0).put(0);
        vertexBuffer.put(-40).put(0).put(0);
        vertexBuffer.put(-40).put(10).put(0);
        vertexBuffer.put(-30).put(10).put(0);

        vertexBuffer.put(-10).put(0).put(0);
        vertexBuffer.put(-20).put(0).put(0);
        vertexBuffer.put(-20).put(10).put(0);
        vertexBuffer.put(-10).put(10).put(0);

        vertexBuffer.put(10).put(0).put(0);
        vertexBuffer.put(20).put(0).put(0);
        vertexBuffer.put(20).put(10).put(0);
        vertexBuffer.put(10).put(10).put(0);

        vertexBuffer.put(30).put(0).put(0);
        vertexBuffer.put(40).put(0).put(0);
        vertexBuffer.put(40).put(10).put(0);
        vertexBuffer.put(30).put(10).put(0);

        meshData.setVertexBuffer(vertexBuffer);

        final IntBuffer indexBuffer = BufferUtils.createIntBuffer(18);

        // Strips
        indexBuffer.put(0).put(3).put(1).put(2);
        indexBuffer.put(4).put(7).put(5).put(6);

        // Quad
        indexBuffer.put(8).put(9).put(10).put(11);

        // Triangles
        indexBuffer.put(12).put(13).put(15);
        indexBuffer.put(13).put(15).put(14);

        meshData.setIndexBuffer(indexBuffer);

        // Setting sub primitive data
        final int[] indexLengths = new int[] { 4, 4, 4, 6 };
        meshData.setIndexLengths(indexLengths);

        final IndexMode[] indexModes = new IndexMode[] { IndexMode.TriangleStrip, IndexMode.TriangleStrip,
                IndexMode.Quads, IndexMode.Triangles };
        meshData.setIndexModes(indexModes);

        final WireframeState ws = new WireframeState();
        mesh.setRenderState(ws);
        mesh.updateModelBound();

        return mesh;
    }
}
