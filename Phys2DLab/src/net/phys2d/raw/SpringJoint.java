/*
 * Phys2D - a 2D physics engine based on the work of Erin Catto. The
 * original source remains:
 * 
 * Copyright (c) 2006 Erin Catto http://www.gphysics.com
 * 
 * This source is provided under the terms of the BSD License.
 * 
 * Copyright (c) 2006, Phys2D
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or 
 * without modification, are permitted provided that the following 
 * conditions are met:
 * 
 *  * Redistributions of source code must retain the above 
 *    copyright notice, this list of conditions and the 
 *    following disclaimer.
 *  * Redistributions in binary form must reproduce the above 
 *    copyright notice, this list of conditions and the following 
 *    disclaimer in the documentation and/or other materials provided 
 *    with the distribution.
 *  * Neither the name of the Phys2D/New Dawn Software nor the names of 
 *    its contributors may be used to endorse or promote products 
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND 
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS 
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 * OF SUCH DAMAGE.
 */
package net.phys2d.raw;

import java.io.Serializable;

import io.github.warnotte.waxlib3.OBJ2GUI.Annotations.GUI_CLASS;
import io.github.warnotte.waxlib3.OBJ2GUI.Annotations.GUI_FIELD_TYPE;
import net.phys2d.math.MathUtil;
import net.phys2d.math.Matrix2f;
import net.phys2d.math.ROVector2f;
import net.phys2d.math.Vector2f;

/**
 * A joint representing a spring. The spring can have different constants for 
 * the stretched and compressed states. It also has a maximum and minimum
 * compression and stretch. If the spring is compressed or stretched beyond
 * those points, the connected bodies will push or pull each other directly.
 * 
 * @author Gideon Smeding
 */
@GUI_CLASS(type=GUI_CLASS.Type.BoxLayout, BoxLayout_property=GUI_CLASS.Type_BoxLayout.Y)
public strictfp class SpringJoint implements Joint,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6214101210435804896L;

	/** The next ID to be used */
	public static int NEXT_ID = 0;
	
	/** The first body attached to the joint */
	private Body body1;
	/** The second body attached to the joint */
	private Body body2;

	/** The local anchor for the first body */
	@GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.PANELISABLE)
	private Vector2f LocalAnchor1 = new Vector2f();
	/** The local anchor for the second body */
	@GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.PANELISABLE)
	private Vector2f LocalAnchor2 = new Vector2f();

//	/** Determince the damping caused by compressing or stretching of the spring */ 
//	private float damping;
	
	/** The spring constant of Hooke's law, when the spring is streched */
	@GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.SLIDER, min=0, max=5000, divider=1,slider_type=GUI_FIELD_TYPE.Type_SLIDER.FLAT)
	private float StretchedSpringConst;
	/** The spring constant of Hooke's law, when the spring is compressed */
	@GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.SLIDER, min=0, max=5000, divider=1,slider_type=GUI_FIELD_TYPE.Type_SLIDER.FLAT)
	private float CompressedSpringConst;
	/** The spring constant of Hooke's law, when the spring is out of 
	 * the bounds determined by min and maxSpringsize */
	@GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.SLIDER, min=0, max=5000, divider=1,slider_type=GUI_FIELD_TYPE.Type_SLIDER.FLAT)
	private float BrokenSpringConst;
	
	/** Size of the spring */
	@GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.SLIDER, min=0, max=5000, divider=1,slider_type=GUI_FIELD_TYPE.Type_SLIDER.FLAT)
	private float SpringSize;
	/** Maximum length of a stretched spring, at which point the spring will not stretch any more */
	@GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.SLIDER, min=0, max=5000, divider=1,slider_type=GUI_FIELD_TYPE.Type_SLIDER.FLAT)
	private float MaxSpringSize;
	/** Minimum length of a stretched spring, at which point the spring will not compress any more */
	@GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.SLIDER, min=0, max=5000, divider=1,slider_type=GUI_FIELD_TYPE.Type_SLIDER.FLAT)
	private float MinSpringSize;
	
	/** The ID of this joint */
	private final int id;
	
	/**
	 * Create a joint holding two bodies together
	 * 
	 * @param b1 The first body attached to the joint
	 * @param b2 The second body attached to the joint
	 * @param anchor1 The location of the attachment to the first body, in absolute coordinates.
	 * @param anchor2 The location of the attachment to the second body, in absolute coordinates.
	 */
	public SpringJoint(Body b1, Body b2, ROVector2f anchor1, ROVector2f anchor2) {
		id = NEXT_ID++;
		
		StretchedSpringConst = 100;
		CompressedSpringConst = 100;
		BrokenSpringConst = 100;
		
		Vector2f spring = new Vector2f(anchor1);
		spring.sub(anchor2);
		SpringSize = spring.length();
		MinSpringSize = 0;
		MaxSpringSize = 2 * SpringSize;
		
		set(b1,b2,anchor1,anchor2);
	}
	
	/**
	 * Retrieve the anchor for the first body attached
	 * 
	 * @return The anchor for the first body
	 */
	public ROVector2f getLocalAnchor1() {
		return LocalAnchor1;
	}

	/**
	 * Retrieve the anchor for the second body attached
	 * 
	 * @return The anchor for the second body
	 */
	public ROVector2f getLocalAnchor2() {
		return LocalAnchor2;
	}
	
	/**
	 * Get the first body attached to this joint
	 * 
	 * @return The first body attached to this joint
	 */
	public Body getBody1() {
		return body1;
	}

	/**
	 * Get the second body attached to this joint
	 * 
	 * @return The second body attached to this joint
	 */
	public Body getBody2() {
		return body2;
	}
	
	/**
	 * Reconfigure this joint
	 * 
	 * @param b1 The first body attached to this joint
	 * @param b2 The second body attached to this joint
	 * @param anchor1 The location of the attachment to the first body, in absolute coordinates.
	 * @param anchor2 The location of the attachment to the second body, in absolute coordinates.
	 */
	public void set(Body b1, Body b2, ROVector2f anchor1, ROVector2f anchor2) {
		body1 = b1;
		body2 = b2;	

		Matrix2f rot1 = new Matrix2f(body1.getRotation());
		Matrix2f rot1T = rot1.transpose();
		Vector2f a1 = new Vector2f(anchor1);
		a1.sub(body1.getROVPosition());
		LocalAnchor1 = MathUtil.mul(rot1T,a1);
		
		Matrix2f rot2 = new Matrix2f(body2.getRotation());
		Matrix2f rot2T = rot2.transpose();
		Vector2f a2 = new Vector2f(anchor2);
		a2.sub(body2.getROVPosition());
		LocalAnchor2 = MathUtil.mul(rot2T,a2);
	}

	/**
	 * Precaculate everything and apply initial impulse before the
	 * simulation step takes place
	 * 
	 * @param invDT The amount of time the simulation is being stepped by
	 */
	public void preStep(float invDT) {

		// calculate the spring's vector (pointing from body1 to body2) and length
		spring = new Vector2f(body2.getROVPosition());
		spring.add(r2);
		spring.sub(body1.getROVPosition());
		spring.sub(r1);
		springLength = spring.length();
		
		// the spring vector needs to be normalized for applyImpulse as well!
		spring.normalise();
		
		// calculate the spring's forces
		// note that although theoretically invDT could never be 0
		// but here it can
		float springConst;
		
		if ( springLength < MinSpringSize || springLength > MaxSpringSize ) { 
			// Pre-compute anchors, mass matrix, and bias.
			Matrix2f rot1 = new Matrix2f(body1.getRotation());
			Matrix2f rot2 = new Matrix2f(body2.getRotation());
	
			r1 = MathUtil.mul(rot1,LocalAnchor1);
			r2 = MathUtil.mul(rot2,LocalAnchor2);
			
			// the mass normal or 'k'
			float rn1 = r1.dot(spring);
			float rn2 = r2.dot(spring);
			float kNormal = body1.getInvMass() + body2.getInvMass();
			kNormal += body1.getInvI() * (r1.dot(r1) - rn1 * rn1) + body2.getInvI() * (r2.dot(r2) - rn2 * rn2);
			massNormal = 1 / kNormal;
			
			
			// The spring is broken so apply force to correct it
			// note that we use biased velocities for this
			float springImpulse =
				invDT != 0 ? BrokenSpringConst * (springLength - SpringSize) / invDT : 0;
			
			Vector2f impulse = MathUtil.scale(spring, springImpulse);
			body1.adjustBiasedVelocity(MathUtil.scale(impulse, body1.getInvMass()));
			body1.adjustBiasedAngularVelocity((body1.getInvI() * MathUtil.cross(r1, impulse)));

			body2.adjustBiasedVelocity(MathUtil.scale(impulse, -body2.getInvMass()));
			body2.adjustBiasedAngularVelocity(-(body2.getInvI() * MathUtil.cross(r2, impulse)));
			
			isBroken = true;
			return;
			
		} else if ( springLength < SpringSize ) {
			springConst = CompressedSpringConst;
			isBroken = false;
		} else { // if ( springLength >= springSize )
			springConst = StretchedSpringConst;
			isBroken = false;
		}
		
		float springImpulse =
			invDT != 0 ? springConst * (springLength - SpringSize) / invDT : 0;

		// apply the spring's forces
		Vector2f impulse = MathUtil.scale(spring, springImpulse);
		body1.adjustVelocity(MathUtil.scale(impulse, body1.getInvMass()));
		body1.adjustAngularVelocity((body1.getInvI() * MathUtil.cross(r1, impulse)));

		body2.adjustVelocity(MathUtil.scale(impulse, -body2.getInvMass()));
		body2.adjustAngularVelocity(-(body2.getInvI() * MathUtil.cross(r2, impulse)));
	}
	
	// The following variables are set by preStep() to be used in applyImpulse()
	
	/** Current lenght of the spring */
	private float springLength;
	/** The spring's normalized vector */
	private Vector2f spring;
	/** The massNormal, normalizes the speed to get the impulse (right?) */
	private float massNormal;
	/** The rotation of the anchor of the first body */
	private Vector2f r1 = new Vector2f();
	/** The rotation of the anchor of the second body */
	private Vector2f r2 = new Vector2f();
	/** True iff the spring is overstretched or overcompressed */
	private boolean isBroken;
	
	/**
	 * Apply the impulse caused by the joint to the bodies attached.
	 */
	public void applyImpulse() {
		if ( isBroken ) {
			// calculate difference in velocity
			// TODO: share this code with BasicJoint and Arbiter
			Vector2f relativeVelocity =  new Vector2f(body2.getVelocity());
			relativeVelocity.add(MathUtil.cross(body2.getAngularVelocity(), r2));
			relativeVelocity.sub(body1.getVelocity());
			relativeVelocity.sub(MathUtil.cross(body1.getAngularVelocity(), r1));
			
			// project the relative velocity onto the spring vector and apply the mass normal
			float normalImpulse = massNormal * relativeVelocity.dot(spring);
			
//			// TODO: Clamp the accumulated impulse?
//			float oldNormalImpulse = accumulatedNormalImpulse;
//			accumulatedNormalImpulse = Math.max(oldNormalImpulse + normalImpulse, 0.0f);
//			normalImpulse = accumulatedNormalImpulse - oldNormalImpulse;
	
			// only apply the impulse if we are pushing or pulling in the right way
			// i.e. pulling if the string is overstretched and pushing if it is too compressed
			if ( springLength < MinSpringSize && normalImpulse < 0
					|| springLength > MaxSpringSize && normalImpulse > 0 ) {
				// now apply the impulses to the bodies
				Vector2f impulse = MathUtil.scale(spring, normalImpulse);
				body1.adjustVelocity(MathUtil.scale(impulse, body1.getInvMass()));
				body1.adjustAngularVelocity((body1.getInvI() * MathUtil.cross(r1, impulse)));
		
				body2.adjustVelocity(MathUtil.scale(impulse, -body2.getInvMass()));
				body2.adjustAngularVelocity(-(body2.getInvI() * MathUtil.cross(r2, impulse)));
			}
		}
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		if (other.getClass() == getClass()) {
			return ((SpringJoint) other).id == id;
		}
		
		return false;
	}

	/**
	 * This function is disabled for this joint
	 * because this class allows a far more precise
	 * control over the relaxation through the various
	 * spring constants and the spring's size.
	 * 
	 * @param relaxation useless parameter of a useless function
	 */
	public void setRelaxation(float relaxation) {
	}

	/**
	 * Get the spring constant of Hooke's law, when the spring is out of 
	 * the bounds determined by min and maxSpringsize.
	 * This is especially useful when either the compressed or stretched 
	 * spring constants are zero.
	 * 
	 * @return the spring constant that is used when the spring is out of bounds
	 */
	public float getBrokenSpringConst() {
		return BrokenSpringConst;
	}

	/**
	 * Set the spring constant of Hooke's law, when the spring is out of 
	 * the bounds determined by min and maxSpringsize.
	 * This is especially useful when either the compressed or stretched 
	 * spring constants are zero.
	 * 
	 * @param brokenSpringConst The spring constant that is used when the spring is out of bounds
	 */
	public void setBrokenSpringConst(float brokenSpringConst) {
		this.BrokenSpringConst = brokenSpringConst;
	}

	/**
	 * Get the spring constant of Hooke's law, when the spring is compressed.
	 * 
	 * @return The spring constant that is used when the spring is compressed.
	 */
	public float getCompressedSpringConst() {
		return CompressedSpringConst;
	}

	/**
	 * Set the spring constant of Hooke's law, when the spring is compressed.
	 * 
	 * @param compressedSpringConst The spring constant that is used when the spring is compressed.
	 */
	public void setCompressedSpringConst(float compressedSpringConst) {
		this.CompressedSpringConst = compressedSpringConst;
	}

//	public float getDamping() {
//		return damping;
//	}
//
//	public void setDamping(float damping) {
//		this.damping = damping;
//	}

	/**
	 * Get the maximum size of the spring, if it stretches beyond this size
	 * the string is considered 'broken'. This means the string will start
	 * acting more or less like a rope (the impulses from the connected bodies
	 * will be transferred directly).
	 * 
	 * @return The maximum spring size
	 */
	public float getMaxSpringSize() {
		return MaxSpringSize;
	}

	/**
	 * Set the maximum size of the spring, if it stretches beyond this size
	 * the string is considered 'broken'. This means the string will start
	 * acting more or less like a rope (the impulses from the connected bodies
	 * will be transferred directly).
	 * 
	 * Note that this function will ensure that maxSpringSize >= springSize
	 * changing the springSize accordingly.
	 * 
	 * @param maxSpringSize The new maximum spring size
	 */
	public void setMaxSpringSize(float maxSpringSize) {
		this.MaxSpringSize = maxSpringSize;
		SpringSize = SpringSize > maxSpringSize ? maxSpringSize : SpringSize;
	}

	/**
	 * Get the minimum size of the spring, if it compressed beyond this size
	 * the string is considered 'broken'. This means the string will start
	 * acting more or less as if the bodies have direct contact at the axes.
	 * 
	 * @return The minimum spring size
	 */
	public float getMinSpringSize() {
		return MinSpringSize;
	}

	/**
	 * Set the minimum size of the spring, if it compressed beyond this size
	 * the string is considered 'broken'. This means the string will start
	 * acting more or less as if the bodies have direct contact at the axes.
	 * 
	 * Note that this will ensure that minSpringSize <= springSize by 
	 * changing the springSize accordingly.
	 * 
	 * @param minSpringSize The minimum spring size
	 */
	public void setMinSpringSize(float minSpringSize) {
		this.MinSpringSize = minSpringSize;
		SpringSize = SpringSize < minSpringSize ? minSpringSize : SpringSize;
	}

	/** Get the spring's size.
	 * 
	 * @return the spring's size.
	 */
	public float getSpringSize() {
		return SpringSize;
	}

	/**
	 * Set the spring's size.
	 * 
	 * Note that we maintain  minSpringSize <= springSize <= maxSpringSize
	 * by setting either min or maxSpringSize. 
	 * 
	 * @param springSize the new spring's size
	 */
	public void setSpringSize(float springSize) {
		this.SpringSize = springSize;
		MaxSpringSize = springSize > MaxSpringSize ? springSize : MaxSpringSize;
		MinSpringSize = springSize < MinSpringSize ? springSize : MinSpringSize;
	}

	/**
	 * Get the spring constant of Hooke's law, when the spring is streched.
	 * 
	 * @return The spring constant that is used when the spring is stretched.
	 */
	public float getStretchedSpringConst() {
		return StretchedSpringConst;
	}
	
	/**
	 * Set the spring constant of Hooke's law, when the spring is streched.
	 * 
	 * @param stretchedSpringConst The spring constant that is used when the spring is stretched.
	 */
	public void setStretchedSpringConst(float stretchedSpringConst) {
		this.StretchedSpringConst = stretchedSpringConst;
	}
}
