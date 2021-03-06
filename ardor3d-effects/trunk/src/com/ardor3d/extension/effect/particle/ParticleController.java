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
import java.util.List;

import com.ardor3d.math.MathUtils;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.Camera.FrustumIntersect;
import com.ardor3d.scenegraph.Controller;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.export.Ardor3DExporter;
import com.ardor3d.util.export.Ardor3DImporter;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;

/**
 * <code>ParticleController</code> controls and maintains the parameters of a particle system over time.
 */
public class ParticleController extends Controller {

    private static final long serialVersionUID = 1L;

    private ParticleSystem particles;
    private int particlesToCreate = 0;
    private double releaseVariance;
    private double currentTime;
    private double prevTime;
    private double releaseParticles;
    private double timePassed;
    private double precision;
    private boolean controlFlow;
    private boolean updateOnlyInView;
    private Camera viewCamera;

    private int iterations;
    private List<ParticleInfluence> influences;
    protected List<ParticleControllerListener> listeners;

    public ParticleController() {}

    /**
     * ParticleManager constructor
     * 
     * @param system
     *            Target ParticleGeometry to act upon.
     */
    public ParticleController(final ParticleSystem system) {
        particles = system;

        setMinTime(0);
        setMaxTime(Float.MAX_VALUE);
        setRepeatType(Controller.RT_WRAP);
        setSpeed(1.0f);

        releaseVariance = 0;
        controlFlow = false;
        updateOnlyInView = false;
        precision = .01f; // 10ms

        system.updateRotationMatrix();
    }

    protected boolean _ignoreOneUpdate = false;

    protected void ignoreNextUpdate() {
        _ignoreOneUpdate = true;
    }

    /**
     * Update the particles managed by this manager. If any particles are "dead" recreate them at the origin position
     * (which may be a point, line or rectangle.)
     * 
     * @param secondsPassed
     *            double
     */
    @Override
    public void update(final double secondsPassed) {

        if (_ignoreOneUpdate) {
            _ignoreOneUpdate = false;
            return;
        }

        // If instructed, check to see if our last frustum check passed
        if (isUpdateOnlyInView()) {
            final Camera cam = viewCamera != null ? viewCamera : ContextManager.getCurrentContext().getCurrentCamera();
            final int state = cam.getPlaneState();
            final boolean out = cam.contains(particles.getWorldBound()).equals(FrustumIntersect.Outside);
            cam.setPlaneState(state);
            if (out) {
                return;
            }
        }

        // Add time and unless we have more than precision time passed
        // since last real update, do nothing
        currentTime += secondsPassed * getSpeed();

        // Check precision passes
        timePassed = currentTime - prevTime;
        if (timePassed < precision * getSpeed()) {
            return;
        }

        // We are actually going to do a real update,
        // so this is our new previous time
        prevTime = currentTime;

        // Update the current rotation matrix if needed.
        particles.updateRotationMatrix();

        // If we are in the time window where this controller is active
        // (defaults to 0 to Float.MAX_VALUE for ParticleController)
        if (currentTime >= getMinTime() && currentTime <= getMaxTime()) {

            // If we are controlling the flow (ie the rate of particle spawning.)
            if (controlFlow) {
                // Release a number of particles based on release rate,
                // timePassed (already scaled for speed) and variance. This
                // is added to any current value Note this is a double value,
                // so we will keep adding up partial particles

                releaseParticles += (particles.getReleaseRate() * timePassed * (1.0 + releaseVariance
                        * (MathUtils.nextRandomFloat() - 0.5)));

                // Try to create all "whole" particles we have added up
                particlesToCreate = (int) releaseParticles;

                // If we have any whole particles, then subtract them from
                // releaseParticles
                if (particlesToCreate > 0) {
                    releaseParticles -= particlesToCreate;
                } else {
                    particlesToCreate = 0;
                }
            }

            particles.updateInvScale();

            // If we have any influences, prepare them all
            if (influences != null) {
                for (final ParticleInfluence influence : influences) {
                    influence.prepare(particles);
                }
            }

            // Track particle index
            int i = 0;

            // Track whether the whole set of particles is "dead" - if any
            // particles are still alive, this will be set to false
            boolean dead = true;

            // opposite of above boolean, but tracked separately
            boolean anyAlive = false;

            // i is index through all particles
            while (i < particles.getNumParticles()) {
                // Current particle
                final Particle p = particles.getParticle(i);

                // If we have influences and particle is alive
                if (influences != null && p.getStatus() == Particle.Status.Alive) {
                    // Apply each enabled influence to the current particle
                    for (int x = 0; x < influences.size(); x++) {
                        final ParticleInfluence inf = influences.get(x);
                        if (inf.isEnabled()) {
                            inf.apply(timePassed, p, i);
                        }
                    }
                }

                // Update and check the particle.
                // If this returns true, indicating particle is ready to be
                // reused, we may reuse it. Do so if we are not using
                // control flow, OR we intend to create particles based on
                // control flow count calculated above
                final boolean reuse = p.updateAndCheck(timePassed);
                if (reuse && (!controlFlow || particlesToCreate > 0)) {

                    // Don't recreate the particle if it is dead, and we are clamped
                    if (p.getStatus() == Particle.Status.Dead && getRepeatType() == RT_CLAMP) {
                        ;

                        // We plan to reuse the particle
                    } else {
                        // Not all particles are dead (this one will be reused)
                        dead = false;

                        // If we are doing flow control, decrement
                        // particlesToCreate, since we are about to create
                        // one
                        if (controlFlow) {
                            particlesToCreate--;
                        }

                        // Recreate the particle
                        p.recreateParticle(particles.getRandomLifeSpan());
                        p.setStatus(Particle.Status.Alive);
                        particles.initParticleLocation(i);
                        particles.resetParticleVelocity(i);
                        p.updateVerts(null);
                    }

                } else if (!reuse || (controlFlow && particles.getReleaseRate() > 0)) {
                    // The particle wasn't dead, or we expect more particles
                    // later, so we're not dead!
                    dead = false;
                }

                // Check for living particles so we know when to update our boundings.
                if (p.getStatus() == Particle.Status.Alive) {
                    anyAlive = true;
                }

                // Next particle
                i++;
            }

            // If we are dead, deactivate and tell our listeners
            if (dead) {
                setActive(false);
                if (listeners != null && listeners.size() > 0) {
                    for (final ParticleControllerListener listener : listeners) {
                        listener.onDead(particles);
                    }
                }
            }

            // If we have a bound and any live particles, update it
            if (particles.getParticleGeometry().getWorldBound() != null && anyAlive) {
                particles.getParticleGeometry().updateModelBound();
                particles.updateWorldBoundManually();
            }
        }
    }

    /**
     * Get how soon after the last update the manager will send updates to the particles.
     * 
     * @return The precision.
     */
    public double getPrecision() {
        return precision;
    }

    /**
     * Set how soon after the last update the manager will send updates to the particles. Defaults to .01f (10ms)<br>
     * <br>
     * This means that if an update is called every 2ms (e.g. running at 500 FPS) the particles position and stats will
     * be updated every fifth frame with the elapsed time (in this case, 10ms) since previous update.
     * 
     * @param precision
     *            in seconds
     */
    public void setPrecision(final double precision) {
        this.precision = precision;
    }

    /**
     * Get the variance possible on the release rate. 0.0f = no variance 0.5f = between releaseRate / 2f and 1.5f *
     * releaseRate
     * 
     * @return release variance as a percent.
     */
    public double getReleaseVariance() {
        return releaseVariance;
    }

    /**
     * Set the variance possible on the release rate.
     * 
     * @param variance
     *            release rate +/- variance as a percent (eg. .5 = 50%)
     */
    public void setReleaseVariance(final double variance) {
        releaseVariance = variance;
    }

    /**
     * Does this manager regulate the particle flow?
     * 
     * @return true if this manager regulates how many particles per sec are emitted.
     */
    public boolean isControlFlow() {
        return controlFlow;
    }

    /**
     * Set the regulate flow property on the manager.
     * 
     * @param regulate
     *            regulate particle flow.
     */
    public void setControlFlow(final boolean regulate) {
        controlFlow = regulate;
    }

    /**
     * Does this manager use the particle's bounding volume to limit updates?
     * 
     * @return true if this manager only updates the particles when they are in view.
     */
    public boolean isUpdateOnlyInView() {
        return updateOnlyInView;
    }

    /**
     * Set the updateOnlyInView property on the manager.
     * 
     * @param updateOnlyInView
     *            use the particle's bounding volume to limit updates.
     */
    public void setUpdateOnlyInView(final boolean updateOnlyInView) {
        this.updateOnlyInView = updateOnlyInView;
    }

    /**
     * @return the camera to be used in updateOnlyInView situations. If null, the current displaySystem's renderer
     *         camera is used.
     */
    public Camera getViewCamera() {
        return viewCamera;
    }

    /**
     * @param viewCamera
     *            sets the camera to be used in updateOnlyInView situations. If null, the current displaySystem's
     *            renderer camera is used.
     */
    public void setViewCamera(final Camera viewCamera) {
        this.viewCamera = viewCamera;
    }

    /**
     * Get the Spatial that holds all of the particle information for display.
     * 
     * @return Spatial holding particle information.
     */
    public Spatial getParticles() {
        return particles;
    }

    /**
     * Return the number this manager has warmed up
     * 
     * @return int
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * Sets the iterations for the warmup and calls warmUp with the number of iterations as the argument
     * 
     * @param iterations
     */
    public void setIterations(final int iterations) {
        this.iterations = iterations;
    }

    /**
     * Add an external influence to this particle controller.
     * 
     * @param influence
     *            ParticleInfluence
     */
    public void addInfluence(final ParticleInfluence influence) {
        if (influences == null) {
            influences = new ArrayList<ParticleInfluence>(1);
        }
        influences.add(influence);
    }

    /**
     * Remove an influence from this particle controller.
     * 
     * @param influence
     *            ParticleInfluence
     * @return true if found and removed.
     */
    public boolean removeInfluence(final ParticleInfluence influence) {
        if (influences == null) {
            return false;
        }
        return influences.remove(influence);
    }

    /**
     * Returns the list of influences acting on this particle controller.
     * 
     * @return ArrayList
     */
    public List<ParticleInfluence> getInfluences() {
        return influences;
    }

    public void clearInfluences() {
        if (influences != null) {
            influences.clear();
        }
    }

    /**
     * Subscribe a listener to receive mouse events. Enable event generation.
     * 
     * @param listener
     *            to be subscribed
     */
    public void addListener(final ParticleControllerListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<ParticleControllerListener>();
        }

        listeners.add(listener);
    }

    /**
     * Unsubscribe a listener. Disable event generation if no more listeners.
     * 
     * @param listener
     *            to be unsuscribed
     * @see #addListener(ParticleControllerListener)
     */
    public void removeListener(final ParticleControllerListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Remove all listeners and disable event generation.
     */
    public void removeListeners() {
        if (listeners != null) {
            listeners.clear();
        }
    }

    /**
     * Check if a listener is allready added to this ParticleController
     * 
     * @param listener
     *            listener to check for
     * @return true if listener is contained in the listenerlist
     */
    public boolean containsListener(final ParticleControllerListener listener) {
        if (listeners != null) {
            return listeners.contains(listener);
        }
        return false;
    }

    /**
     * Get all added ParticleController listeners
     * 
     * @return ArrayList of listeners added to this ParticleController
     */
    public List<ParticleControllerListener> getListeners() {
        return listeners;
    }

    /**
     * Runs the update method of this particle manager for iteration seconds with an update every .1 seconds (IE
     * <code>iterations</code> 10 update(.1) calls). This is used to "warm up" and get the particle manager going.
     * 
     * @param iterations
     *            The number of iterations to warm up.
     */
    public void warmUp(int iterations) {
        // first set the initial positions of all the verts
        particles.initAllParticlesLocation();

        iterations *= 10;
        for (int i = iterations; --i >= 0;) {
            update(.1);
        }
        ignoreNextUpdate();
    }

    @Override
    public void write(final Ardor3DExporter e) throws IOException {
        super.write(e);
        final OutputCapsule capsule = e.getCapsule(this);
        capsule.write(particles, "particleMesh", null);
        capsule.write(releaseVariance, "releaseVariance", 0);
        capsule.write(precision, "precision", 0);
        capsule.write(controlFlow, "controlFlow", false);
        capsule.write(updateOnlyInView, "updateOnlyInView", false);
        capsule.write(iterations, "iterations", 0);
        capsule.writeSavableList(influences, "influences", null);
    }

    @Override
    public void read(final Ardor3DImporter e) throws IOException {
        super.read(e);
        final InputCapsule capsule = e.getCapsule(this);
        particles = (ParticleSystem) capsule.readSavable("particleMesh", null);
        releaseVariance = capsule.readFloat("releaseVariance", 0);
        precision = capsule.readFloat("precision", 0);
        controlFlow = capsule.readBoolean("controlFlow", false);
        updateOnlyInView = capsule.readBoolean("updateOnlyInView", false);
        iterations = capsule.readInt("iterations", 0);
        influences = capsule.readSavableList("influences", null);
    }
}
