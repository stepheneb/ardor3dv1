/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.scenegraph;

import java.io.IOException;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.image.Texture2D;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.TextureRenderer;
import com.ardor3d.renderer.TextureRendererFactory;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.shape.Quad;
import com.ardor3d.util.export.Ardor3DExporter;
import com.ardor3d.util.export.Ardor3DImporter;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;

/**
 * <code>ImposterNode</code>
 */
public class ImposterNode extends Node {
    private static final double DEFAULT_DISTANCE = 10.0;

    private static final double DEFAULT_RATE = .05f;

    private static final long serialVersionUID = 1L;

    protected TextureRenderer tRenderer;

    protected Texture2D texture;

    protected Node quadScene;

    static int inode_val = 0;

    protected Quad standIn;

    protected double redrawRate;

    protected double elapsed;

    protected double cameraDistance = DEFAULT_DISTANCE;

    protected double cameraThreshold;

    protected double oldAngle;

    protected double lastAngle;

    protected boolean haveDrawn;

    protected boolean byCamera;

    protected boolean byTime;

    protected final Vector3 worldUpVector = new Vector3(0, 1, 0);

    public ImposterNode() {}

    public ImposterNode(final String name, final double size, final int twidth, final int theight,
            final Renderer renderer) {
        super(name);
        final DisplaySettings settings = new DisplaySettings(twidth, theight, 0, 0, 0, 8, 0, 0, false);
        tRenderer = TextureRendererFactory.INSTANCE.createTextureRenderer(settings, renderer,
                TextureRenderer.Target.Texture2D);

        tRenderer.getCamera().setLocation(new Vector3(0, 0, 75f));
        tRenderer.setBackgroundColor(new ColorRGBA(0, 0, 0, 0f));

        quadScene = new Node("imposter_scene_" + inode_val);
        quadScene.setCullHint(Spatial.CullHint.Never);

        standIn = new Quad("imposter_quad_" + inode_val);
        standIn.initialize(size, size);
        standIn.setModelBound(new BoundingBox());
        standIn.updateModelBound();
        standIn.setParent(this);
        standIn.updateGeometricState(0, true);

        inode_val++;
        resetTexture();
        redrawRate = elapsed = DEFAULT_RATE; // 20x per sec
        cameraThreshold = 0; // off
        haveDrawn = false;

    }

    /**
     * <code>draw</code> calls the onDraw method for each child maintained by this node.
     * 
     * @see com.ardor3d.scenegraph.Spatial#draw(com.ardor3d.renderer.Renderer)
     * @param r
     *            the renderer to draw to.
     */
    @Override
    public void draw(final Renderer r) {
        final Camera camera = ContextManager.getCurrentContext().getCurrentCamera();
        if (!haveDrawn || shouldDoUpdate(camera)) {
            updateCamera(camera.getLocation());
            if (byTime) {
                updateScene(redrawRate);
                elapsed -= redrawRate;
            } else if (byCamera) {
                updateScene(0);
            }
            renderTexture();
            haveDrawn = true;
        }
        standIn.onDraw(r);
    }

    /**
     * Force the texture camera to update its position and direction based on the given eyeLocation
     * 
     * @param eyeLocation
     *            The location the viewer is looking from in the real world.
     */
    public void updateCamera(final ReadOnlyVector3 eyeLocation) {
        final ReadOnlyVector3 center = standIn.getWorldTranslation();

        final double vDist = eyeLocation.distance(center);
        final double ratio = cameraDistance / vDist;
        final Vector3 newPos = eyeLocation.subtract(center, Vector3.fetchTempInstance()).multiplyLocal(ratio).addLocal(
                center);
        tRenderer.getCamera().setLocation(newPos);
        tRenderer.getCamera().lookAt(center, worldUpVector);
        Vector3.releaseTempInstance(newPos);
    }

    /**
     * Check to see if the texture needs updating based on the params set for redraw rate and camera threshold.
     * 
     * @param cam
     *            The camera we check angles against.
     * @return boolean
     */
    private boolean shouldDoUpdate(final Camera cam) {
        byTime = byCamera = false;
        if (redrawRate > 0 && elapsed >= redrawRate) {
            byTime = true;
            return true;
        }
        if (cameraThreshold > 0) {
            final double camChange = Math.abs(getCameraChange(cam));
            if (camChange >= cameraThreshold) {
                byCamera = true;
                oldAngle = lastAngle;
                return true;
            }
        }
        return false;
    }

    /**
     * Get the different in radians that the camera angle has changed since last update.
     * 
     * @param cam
     *            The camera we check angles against.
     * @return double
     */
    private double getCameraChange(final Camera cam) {
        // change is last camera angle - this angle
        final ReadOnlyVector3 eye = cam.getLocation();
        final ReadOnlyVector3 spot = standIn.getWorldTranslation();

        double opp = eye.getX() - spot.getX();
        final double adj = eye.getZ() - spot.getZ();
        if (adj == 0) {
            return 0;
        }
        lastAngle = Math.atan(opp / adj);
        opp = eye.getY() - spot.getY();
        lastAngle += Math.atan(opp / adj);

        return oldAngle - lastAngle;
    }

    /**
     * 
     * <code>attachChild</code> attaches a child to this node. This node becomes the child's parent. The current number
     * of children maintained is returned.
     * 
     * @param child
     *            the child to attach to this node.
     * @return the number of children maintained by this node.
     */
    @Override
    public int attachChild(final Spatial child) {
        return quadScene.attachChild(child);
    }

    /**
     * Set the Underlying texture renderer used by this imposter. Automatically calls resetTexture()
     * 
     * @param tRenderer
     *            TextureRenderer
     */
    public void setTextureRenderer(final TextureRenderer tRenderer) {
        this.tRenderer = tRenderer;
        resetTexture();
    }

    /**
     * Get the Underlying texture renderer used by this imposter.
     * 
     * @return TextureRenderer
     */
    public TextureRenderer getTextureRenderer() {
        return tRenderer;
    }

    /**
     * Get the distance we want the render camera to stay away from the render scene.
     * 
     * @return double
     */
    public double getCameraDistance() {
        return cameraDistance;
    }

    /**
     * Set the distance we want the render camera to stay away from the render scene.
     * 
     * @param cameraDistance
     *            double
     */
    public void setCameraDistance(final double cameraDistance) {
        this.cameraDistance = cameraDistance;
    }

    /**
     * Get how often (in seconds) we want the texture updated. example: .02 = every 20 ms or 50 times a sec. 0.0 = do
     * not update based on time.
     * 
     * @return double
     */
    public double getRedrawRate() {
        return redrawRate;
    }

    /**
     * Set the redraw rate (see <code>getRedrawRate()</code>)
     * 
     * @param rate
     *            double
     */
    public void setRedrawRate(final double rate) {
        redrawRate = rate;
        elapsed = rate;
    }

    /**
     * Get the Quad used as a standin for the scene being faked.
     * 
     * @return Quad
     */
    public Quad getStandIn() {
        return standIn;
    }

    /**
     * Set how much the viewers camera position has to change (in terms of angle to the imposter) before an update is
     * called.
     * 
     * @param threshold
     *            angle in radians
     */
    public void setCameraThreshold(final double threshold) {
        cameraThreshold = threshold;
        oldAngle = cameraThreshold + threshold;
    }

    /**
     * Get the camera threshold (see <code>setCameraThreshold()</code>)
     */
    public double getCameraThreshold() {
        return cameraThreshold;
    }

    /**
     * Resets and applies the texture, texture state and blend state on the standin Quad.
     */
    public void resetTexture() {
        if (texture == null) {
            texture = new Texture2D();
        }
        tRenderer.setupTexture(texture);
        final TextureState ts = new TextureState();
        ts.setEnabled(true);
        ts.setTexture(texture, 0);
        standIn.setRenderState(ts);

        // Add a blending mode... This is so the background of the texture is
        // transparent.
        final BlendState as1 = new BlendState();
        as1.setBlendEnabled(true);
        as1.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        as1.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        as1.setTestEnabled(true);
        as1.setTestFunction(BlendState.TestFunction.GreaterThan);
        as1.setEnabled(true);
        standIn.setRenderState(as1);
    }

    /**
     * Updates the scene the texture represents.
     * 
     * @param timePassed
     *            double
     */
    public void updateScene(final double timePassed) {
        quadScene.updateGeometricState(timePassed, true);
    }

    /**
     * force the underlying texture renderer to render the scene. Could be useful for imposters that do not use time or
     * camera angle to update the scene. (In which case, updateCamera and updateScene would likely be called prior to
     * calling this.)
     */
    public void renderTexture() {
        tRenderer.render(quadScene, texture);
    }

    /**
     * <code>updateWorldBound</code> merges the bounds of all the children maintained by this node. This will allow for
     * faster culling operations.
     * 
     * @see com.ardor3d.scenegraph.Spatial#updateWorldBound(boolean)
     */
    @Override
    public void updateWorldBound(final boolean recurse) {
        _worldBound = standIn.getWorldBound().clone(_worldBound);
        clearDirty(DirtyType.Bounding);
    }

    /**
     * 
     * <code>updateWorldData</code> updates the world transforms from the parent down to the leaf.
     * 
     * @param time
     *            the frame time.
     */
    @Override
    public void updateGeometricState(final double time, final boolean initiator) {
        super.updateGeometricState(time, initiator);
        standIn.updateGeometricState(time, false);
        elapsed += time;
    }

    /**
     * @return Returns the worldUpVector.
     */
    public Vector3 getWorldUpVector() {
        return worldUpVector;
    }

    /**
     * @param worldUpVector
     *            The worldUpVector to set.
     */
    public void setWorldUpVector(final Vector3 worldUpVector) {
        this.worldUpVector.set(worldUpVector);
    }

    @Override
    public void write(final Ardor3DExporter e) throws IOException {
        super.write(e);
        final OutputCapsule capsule = e.getCapsule(this);
        capsule.write(texture, "texture", null);
        capsule.write(quadScene, "quadScene", new Node("imposter_scene_" + inode_val));
        capsule.write(standIn, "standIn", new Quad("imposter_quad_" + inode_val));
        capsule.write(redrawRate, "redrawRate", DEFAULT_RATE);
        capsule.write(cameraDistance, "cameraDistance", DEFAULT_DISTANCE);
        capsule.write(cameraThreshold, "cameraThreshold", 0);
        capsule.write(worldUpVector, "worldUpVector", new Vector3(Vector3.UNIT_Y));
    }

    @Override
    public void read(final Ardor3DImporter e) throws IOException {
        super.read(e);
        final InputCapsule capsule = e.getCapsule(this);
        texture = (Texture2D) capsule.readSavable("texture", null);
        quadScene = (Node) capsule.readSavable("quadScene", new Node("imposter_scene_" + inode_val));
        standIn = (Quad) capsule.readSavable("standIn", new Quad("imposter_quad_" + inode_val));
        redrawRate = capsule.readDouble("redrawRate", DEFAULT_RATE);
        cameraDistance = capsule.readDouble("cameraDistance", DEFAULT_DISTANCE);
        cameraThreshold = capsule.readDouble("cameraThreshold", 0);
        worldUpVector.set((Vector3) capsule.readSavable("worldUpVector", new Vector3(Vector3.UNIT_Y)));
    }
}