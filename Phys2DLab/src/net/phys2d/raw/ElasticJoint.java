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
 * A joint between two bodies. The joint affects the impulses applied to 
 * each body each step constraining the movement. Behaves a bit like elastic
 * 
 * Behaviour is undefined - it does something and it's definitely odd. Might
 * be useful for something
 * 
 * @author Kevin Glass
 */

@GUI_CLASS(type=GUI_CLASS.Type.BoxLayout, BoxLayout_property=GUI_CLASS.Type_BoxLayout.Y)
public strictfp class ElasticJoint implements Joint, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1684581952224070626L;

	/** The next ID to be used */
	public static int NEXT_ID = 0;
	
	/** The first body attached to the joint */
	private Body body1;
	/** The second body attached to the joint */
	private Body body2;

	/** The matrix describing the connection between two bodies */
	private Matrix2f M = new Matrix2f();
	/** The local anchor for the first body */
	@GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.PANELISABLE)
	private Vector2f localAnchor1 = new Vector2f();
	/** The local anchor for the second body */
	@GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.PANELISABLE)
	private Vector2f localAnchor2 = new Vector2f();
	/** The rotation of the anchor of the first body */
	@GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.PANELISABLE)
	private Vector2f r1 = new Vector2f();
	/** The rotation of the anchor of the second body */
	@GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.PANELISABLE)
	private Vector2f r2 = new Vector2f();
	/** ? */
	private Vector2f bias = new Vector2f();
	/** The impulse to be applied throught the joint */
	@GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.PANELISABLE)
	private final Vector2f accumulatedImpulse = new Vector2f();
	/** How much slip there is in the joint */
	@GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.SLIDER, min=-500, max=500, divider=10,slider_type=GUI_FIELD_TYPE.Type_SLIDER.FLAT)
	private float relaxation;

	/** The ID of this joint */
	private final int id;
	
	/**
	 * Create a joint holding two bodies together
	 * 
	 * @param b1 The first body attached to the joint
	 * @param b2 The second body attached to the joint
	 */
	public ElasticJoint(Body b1, Body b2) {
		id = NEXT_ID++;
		accumulatedImpulse.set(0.0f, 0.0f);
		relaxation = 1.0f;
		
		set(b1,b2);
	}

	/**
	 * Set the relaxtion value on this joint. This value determines
	 * how loose the joint will be
	 * 
	 * @param relaxation The relaxation value
	 */
	public void setRelaxation(float relaxation) {
		this.relaxation = relaxation;
	}
	
	/**
	 * Retrieve the anchor for the first body attached
	 * 
	 * @return The anchor for the first body
	 */
	public ROVector2f getLocalAnchor1() {
		return localAnchor1;
	}

	/**
	 * Retrieve the anchor for the second body attached
	 * 
	 * @return The anchor for the second body
	 */
	public ROVector2f getLocalAnchor2() {
		return localAnchor2;
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
	 */
	public void set(Body b1, Body b2) {
		body1 = b1;
		body2 = b2;

		Matrix2f rot1 = new Matrix2f(body1.getRotation());
		Matrix2f rot2 = new Matrix2f(body2.getRotation());
		Matrix2f rot1T = rot1.transpose();
		Matrix2f rot2T = rot2.transpose();

		Vector2f a1 = new Vector2f(body2.getROVPosition());
		a1.sub(body1.getROVPosition());
		localAnchor1 = MathUtil.mul(rot1T,a1);
		Vector2f a2 = new Vector2f(body1.getROVPosition());
		a2.sub(body2.getROVPosition());
		localAnchor2 = MathUtil.mul(rot2T,a2);

		accumulatedImpulse.set(0.0f, 0.0f);
		relaxation = 1.0f;
	}

	/**
	 * Precaculate everything and apply initial impulse before the
	 * simulation step takes place
	 * 
	 * @param invDT The amount of time the simulation is being stepped by
	 */
	public void preStep(float invDT) {
		// Pre-compute anchors, mass matrix, and bias.
		Matrix2f rot1 = new Matrix2f(body1.getRotation());
		Matrix2f rot2 = new Matrix2f(body2.getRotation());

		r1 = MathUtil.mul(rot1,localAnchor1);
		r2 = MathUtil.mul(rot2,localAnchor2);

		// deltaV = deltaV0 + K * impulse
		// invM = [(1/m1 + 1/m2) * eye(2) - skew(r1) * invI1 * skew(r1) - skew(r2) * invI2 * skew(r2)]
		//      = [1/m1+1/m2     0    ] + invI1 * [r1.y*r1.y -r1.x*r1.y] + invI2 * [r1.y*r1.y -r1.x*r1.y]
		//        [    0     1/m1+1/m2]           [-r1.x*r1.y r1.x*r1.x]           [-r1.x*r1.y r1.x*r1.x]
		Matrix2f K1 = new Matrix2f();
		K1.col1.X = body1.getInvMass() + body2.getInvMass();	K1.col2.X = 0.0f;
		K1.col1.Y = 0.0f;								K1.col2.Y = body1.getInvMass() + body2.getInvMass();

		Matrix2f K2 = new Matrix2f();
		K2.col1.X =  body1.getInvI() * r1.Y * r1.Y;		K2.col2.X = -body1.getInvI() * r1.X * r1.Y;
		K2.col1.Y = -body1.getInvI() * r1.X * r1.Y;		K2.col2.Y =  body1.getInvI() * r1.X * r1.X;

		Matrix2f K3 = new Matrix2f();
		K3.col1.X =  body2.getInvI() * r2.Y * r2.Y;		K3.col2.X = -body2.getInvI() * r2.X * r2.Y;
		K3.col1.Y = -body2.getInvI() * r2.X * r2.Y;		K3.col2.Y =  body2.getInvI() * r2.X * r2.X;

		Matrix2f K = MathUtil.add(MathUtil.add(K1,K2),K3);
		M = K.invert();

		Vector2f p1 = new Vector2f(body1.getROVPosition());
		p1.add(r1);
		Vector2f p2 = new Vector2f(body2.getROVPosition());
		p2.add(r2);
		Vector2f dp = new Vector2f(p2);
		dp.sub(p1);
		
		bias = new Vector2f(dp);
		bias.scale(-0.1f);
		bias.scale(invDT);

		// Apply accumulated impulse.
		accumulatedImpulse.scale(relaxation);
		
		Vector2f accum1 = new Vector2f(accumulatedImpulse);
		accum1.scale(-body1.getInvMass());
		body1.adjustVelocity(accum1);
		body1.adjustAngularVelocity(-(body1.getInvI() * MathUtil.cross(r1, accumulatedImpulse)));

		Vector2f accum2 = new Vector2f(accumulatedImpulse);
		accum2.scale(body2.getInvMass());
		body2.adjustVelocity(accum2);
		body2.adjustAngularVelocity(body2.getInvI() * MathUtil.cross(r2, accumulatedImpulse));
	}
	
	/**
	 * Apply the impulse caused by the joint to the bodies attached.
	 */
	public void applyImpulse() {
		Vector2f dv = new Vector2f(body2.getVelocity());
		dv.add(MathUtil.cross(body2.getAngularVelocity(),r2));
		dv.sub(body1.getVelocity());
		dv.sub(MathUtil.cross(body1.getAngularVelocity(),r1));
	    dv.scale(-1);
	    dv.add(bias);
	    
	    if (dv.lengthSquared() == 0) {
	    	return;
	    }
	    
		Vector2f impulse = MathUtil.mul(M, dv);

		Vector2f delta1 = new Vector2f(impulse);
		delta1.scale(-body1.getInvMass());
		body1.adjustVelocity(delta1);
		body1.adjustAngularVelocity(-body1.getInvI() * MathUtil.cross(r1,impulse));

		Vector2f delta2 = new Vector2f(impulse);
		delta2.scale(body2.getInvMass());
		body2.adjustVelocity(delta2);
		body2.adjustAngularVelocity(body2.getInvI() * MathUtil.cross(r2,impulse));

		accumulatedImpulse.add(impulse);
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
			return ((ElasticJoint) other).id == id;
		}
		
		return false;
	}
}
