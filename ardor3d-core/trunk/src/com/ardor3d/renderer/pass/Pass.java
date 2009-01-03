/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.renderer.pass;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.RenderState;
import com.ardor3d.scenegraph.Spatial;

/**
 * <code>Pass</code> encapsulates logic necessary for rendering one or more steps in a multipass technique.
 * 
 * Rendering:
 * 
 * When renderPass is called, a check is first made to see if the pass isEnabled(). Then any states set on this pass are
 * enforced via Spatial.enforceState(RenderState). This is useful for doing things such as causing this pass to be
 * blended to a previous pass via enforcing an BlendState, etc. Next, doRender(Renderer) is called to do the actual
 * rendering work. Finally, any enforced states set before this pass was run are restored.
 */
public abstract class Pass implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /** list of Spatial objects registered with this pass. */
    protected List<Spatial> spatials = new ArrayList<Spatial>();

    /** if false, pass will not be updated or rendered. */
    protected boolean enabled = true;

    /** offset params to use to differentiate multiple passes of the same scene in the zbuffer. */
    protected float zFactor;
    protected float zOffset;

    /**
     * RenderStates registered with this pass - if a given state is not null it overrides the corresponding state set
     * during rendering.
     */
    protected final EnumMap<RenderState.StateType, RenderState> passStates = new EnumMap<RenderState.StateType, RenderState>(
            RenderState.StateType.class);

    /** a place to internally save previous states setup before rendering this pass */
    protected final EnumMap<RenderState.StateType, RenderState> savedStates = new EnumMap<RenderState.StateType, RenderState>(
            RenderState.StateType.class);

    protected RenderContext context = null;

    /** if enabled, set the states for this pass and then render. */
    public final void renderPass(final Renderer r) {
        if (!enabled) {
            return;
        }
        context = ContextManager.getCurrentContext();
        applyPassStates();
        r.setPolygonOffset(zFactor, zOffset);
        doRender(r);
        r.clearPolygonOffset();
        resetOldStates();
        context = null;
    }

    /**
     * Enforce a particular state. In other words, the given state will override any state of the same type set on a
     * scene object. Remember to clear the state when done enforcing. Very useful for multipass techniques where
     * multiple sets of states need to be applied to a scenegraph drawn multiple times.
     * 
     * @param state
     *            state to enforce
     */
    public void setPassState(final RenderState state) {
        passStates.put(state.getType(), state);
    }

    /**
     * Clears an enforced render state index by setting it to null. This allows object specific states to be used.
     * 
     * @param type
     *            The type of RenderState to clear enforcement on.
     */
    public void clearPassState(final RenderState.StateType type) {
        passStates.remove(type);
    }

    /**
     * sets all enforced states to null.
     * 
     * @see RenderContext#clearEnforcedState(int)
     */
    public void clearPassStates() {
        passStates.clear();
    }

    protected void applyPassStates() {
        for (final RenderState.StateType type : passStates.keySet()) {
            final RenderState pState = passStates.get(type);
            savedStates.put(type, context.getEnforcedState(type));
            context.enforceState(pState);
        }
    }

    protected abstract void doRender(Renderer r);

    protected void resetOldStates() {
        for (final RenderState.StateType type : savedStates.keySet()) {
            final RenderState sState = savedStates.get(type);
            if (sState == null) {
                context.clearEnforcedState(type);
            } else {
                context.enforceState(sState);
            }
        }
        savedStates.clear();
    }

    /** if enabled, call doUpdate to update information for this pass. */
    public final void updatePass(final double tpf) {
        if (!enabled) {
            return;
        }
        doUpdate(tpf);
    }

    protected void doUpdate(final double tpf) {}

    public void add(final Spatial toAdd) {
        spatials.add(toAdd);
    }

    public Spatial get(final int index) {
        return spatials.get(index);
    }

    public boolean contains(final Spatial s) {
        return spatials.contains(s);
    }

    public boolean remove(final Spatial toRemove) {
        return spatials.remove(toRemove);
    }

    public int size() {
        return spatials.size();
    }

    /**
     * @return Returns the enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled
     *            The enabled to set.
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return Returns the zFactor.
     */
    public float getZFactor() {
        return zFactor;
    }

    /**
     * Sets the polygon offset param - factor - for this Pass.
     * 
     * @param factor
     *            The zFactor to set.
     */
    public void setZFactor(final float factor) {
        zFactor = factor;
    }

    /**
     * @return Returns the zOffset.
     */
    public float getZOffset() {
        return zOffset;
    }

    /**
     * Sets the polygon offset param - offset - for this Pass.
     * 
     * @param offset
     *            The zOffset to set.
     */
    public void setZOffset(final float offset) {
        zOffset = offset;
    }

    public void cleanUp() {}

}
