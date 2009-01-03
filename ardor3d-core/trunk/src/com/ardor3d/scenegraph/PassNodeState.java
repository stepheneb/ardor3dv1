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
import java.io.Serializable;
import java.util.EnumMap;

import com.ardor3d.renderer.RenderContext;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.RenderState;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.util.export.Ardor3DExporter;
import com.ardor3d.util.export.Ardor3DImporter;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;
import com.ardor3d.util.export.Savable;

public class PassNodeState implements Savable, Serializable {

    private static final long serialVersionUID = 1L;

    /** if false, pass will not be updated or rendered. */
    protected boolean _enabled = true;

    /**
     * offset params to use to differentiate multiple passes of the same scene in the zbuffer.
     */
    protected float _zFactor;
    protected float _zOffset;

    /**
     * RenderStates registered with this pass - if a given state is not null it overrides the corresponding state set
     * during rendering.
     */
    protected final EnumMap<RenderState.StateType, RenderState> _passStates = new EnumMap<RenderState.StateType, RenderState>(
            RenderState.StateType.class);

    /** a place to internally save previous states setup before rendering this pass */
    protected final EnumMap<RenderState.StateType, RenderState> _savedStates = new EnumMap<RenderState.StateType, RenderState>(
            RenderState.StateType.class);

    /**
     * Applies all currently set renderstates and z offset parameters to the supplied context
     * 
     * @param r
     * @param context
     */
    public void applyPassNodeState(final Renderer r, final RenderContext context) {
        applyPassStates(context);
        r.setPolygonOffset(_zFactor, _zOffset);
    }

    /**
     * Resets currently set renderstates and z offset parameters on the supplied context
     * 
     * @param r
     * @param context
     */
    public void resetPassNodeStates(final Renderer r, final RenderContext context) {
        r.clearPolygonOffset();
        resetOldStates(context);
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
        _passStates.put(state.getType(), state);
    }

    /**
     * @param type
     *            the type to query
     * @return the state enforced for a give state type, or null if none.
     */
    public RenderState getPassState(final StateType type) {
        return _passStates.get(type);
    }

    /**
     * Clears an enforced render state index by setting it to null. This allows object specific states to be used.
     * 
     * @param type
     *            The type of RenderState to clear enforcement on.
     */
    public void clearPassState(final StateType type) {
        _passStates.remove(type);
    }

    /**
     * sets all enforced states to null.
     * 
     * @see RenderContext#clearEnforcedState(int)
     */
    public void clearPassStates() {
        _passStates.clear();
    }

    /**
     * Applies all currently set render states to the supplied context
     * 
     * @param context
     */
    protected void applyPassStates(final RenderContext context) {
        for (final RenderState.StateType type : _passStates.keySet()) {
            final RenderState pState = _passStates.get(type);
            _savedStates.put(type, context.getEnforcedState(type));
            context.enforceState(pState);
        }
    }

    /**
     * Resets all render states on the supplied context
     * 
     * @param context
     */
    protected void resetOldStates(final RenderContext context) {
        for (final RenderState.StateType type : _savedStates.keySet()) {
            final RenderState sState = _savedStates.get(type);
            if (sState == null) {
                context.clearEnforcedState(type);
            } else {
                context.enforceState(sState);
            }
        }
        _savedStates.clear();
    }

    /** @return Returns the enabled. */
    public boolean isEnabled() {
        return _enabled;
    }

    /**
     * @param enabled
     *            The enabled to set.
     */
    public void setEnabled(final boolean enabled) {
        _enabled = enabled;
    }

    /** @return Returns the zFactor. */
    public float getZFactor() {
        return _zFactor;
    }

    /**
     * Sets the polygon offset param - factor - for this Pass.
     * 
     * @param factor
     *            The zFactor to set.
     */
    public void setZFactor(final float factor) {
        _zFactor = factor;
    }

    /** @return Returns the zOffset. */
    public float getZOffset() {
        return _zOffset;
    }

    /**
     * Sets the polygon offset param - offset - for this Pass.
     * 
     * @param offset
     *            The zOffset to set.
     */
    public void setZOffset(final float offset) {
        _zOffset = offset;
    }

    public Class<? extends PassNodeState> getClassTag() {
        return this.getClass();
    }

    public void write(final Ardor3DExporter e) throws IOException {
        final OutputCapsule oc = e.getCapsule(this);
        oc.write(_enabled, "enabled", true);
        oc.write(_zFactor, "zFactor", 0);
        oc.write(_zOffset, "zOffset", 0);
        oc.write(_passStates.values().toArray(new RenderState[0]), "passStates", null);
        oc.write(_savedStates.values().toArray(new RenderState[0]), "savedStates", null);
    }

    public void read(final Ardor3DImporter e) throws IOException {
        final InputCapsule ic = e.getCapsule(this);
        _enabled = ic.readBoolean("enabled", true);
        _zFactor = ic.readFloat("zFactor", 0);
        _zOffset = ic.readFloat("zOffset", 0);
        Savable[] temp = ic.readSavableArray("passStates", null);
        _passStates.clear();
        if (temp != null) {
            for (int x = 0; x < temp.length; x++) {
                final RenderState state = (RenderState) temp[x];
                _passStates.put(state.getType(), state);
            }
        }

        temp = ic.readSavableArray("savedStates", null);
        _savedStates.clear();
        if (temp != null) {
            for (int x = 0; x < temp.length; x++) {
                final RenderState state = (RenderState) temp[x];
                _savedStates.put(state.getType(), state);
            }
        }
    }
}
