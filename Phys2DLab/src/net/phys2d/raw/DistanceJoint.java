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
import net.phys2d.math.Vector2f;

/**
 * A joint that constrains the distance that two bodies can be from each other
 * 
 * @author guRuQu
 */
@GUI_CLASS(type=GUI_CLASS.Type.BoxLayout, BoxLayout_property=GUI_CLASS.Type_BoxLayout.Y)
public class DistanceJoint implements Joint,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -952403672634628092L;
	/** The cached impulse through the calculation to yield correct impulse faster */
	protected float accumulatedImpulse;
	/** Anchor point for first body, on which impulse is going to apply*/
	@GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.PANELISABLE)
	protected Vector2f Anchor1;
	/** Anchor point for second body, on which impulse is going to apply*/
	@GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.PANELISABLE)
	protected Vector2f Anchor2;
	/** The caculated bias */
	protected float bias;
	/** The first body in this joint */
	protected Body body1;
	/** The second bodu in this joint */
	protected Body body2;
	/** The distance between the bodies */
	
	@GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.SLIDER, min=0, max=10000, divider=1,slider_type=GUI_FIELD_TYPE.Type_SLIDER.FLAT)
	private float Distant;
	/** Distance Vector*/
	protected Vector2f dp;
	/** The matrix for applying impulse */
	protected Matrix2f M;
	/** The rotation of the first body */
	protected Vector2f r1;
	/** The rotation of the second body */
	protected Vector2f r2;
	/** The scalar */
	protected float sc;

	/**
	 * @param body1	The first body to be attached on constraint
	 * @param body2 The second body to be attached on constraint
	 * @param anchor1 The anchor point on first body
	 * @param anchor2 The anchor point on second body
	 * @param distant The fixed distance that is going to keep between two bodies
	 */
	public DistanceJoint(Body body1, Body body2, Vector2f anchor1,
			Vector2f anchor2, float distant) {
		this.body1 = body1;
		this.body2 = body2;
		this.Anchor1 = anchor1;
		this.Anchor2 = anchor2;
		this.Distant = distant * distant;
	}
	
	/**
	 * @see net.phys2d.raw.Joint#applyImpulse()
	 */
	public void applyImpulse() {
		Vector2f dv = new Vector2f(body2.getVelocity());
		dv.add(MathUtil.cross(body2.getAngularVelocity(), r2));
		dv.sub(body1.getVelocity());
		dv.sub(MathUtil.cross(body1.getAngularVelocity(), r1));

		float ju = -dv.dot(dp) + bias;
		float p = ju / sc;

		Vector2f impulse = new Vector2f(dp);
		impulse.scale(p);

		if (!body1.isStatic()) {
			Vector2f accum1 = new Vector2f(impulse);
			accum1.scale(-body1.getInvMass());
			body1.adjustVelocity(accum1);
			body1.adjustAngularVelocity(-(body1.getInvI() * MathUtil.cross(r1,
					impulse)));
		}

		if (!body2.isStatic()) {
			Vector2f accum2 = new Vector2f(impulse);
			accum2.scale(body2.getInvMass());
			body2.adjustVelocity(accum2);
			body2.adjustAngularVelocity(body2.getInvI()
					* MathUtil.cross(r2, impulse));
		}

		accumulatedImpulse += p;
	}

	/**
	 * Get the anchor of the joint on the first body
	 * 
	 * @return The anchor of the joint on the first body
	 */
	public Vector2f getAnchor1() {
		return Anchor1;
	}

	/**
	 * Get the anchor of the joint on the second body
	 * 
	 * @return The anchor of the joint on the second body
	 */
	public Vector2f getAnchor2() {
		return Anchor2;
	}

	/**
	 * @see net.phys2d.raw.Joint#getBody1()
	 */
	public Body getBody1() {
		return body1;
	}

	/**
	 * @see net.phys2d.raw.Joint#getBody2()
	 */
	public Body getBody2() {
		return body2;
	}

	/**
	 * @see net.phys2d.raw.Joint#preStep(float)
	 */
	public void preStep(float invDT) {
		Matrix2f rot1 = new Matrix2f(body1.getRotation());
		Matrix2f rot2 = new Matrix2f(body2.getRotation());
		r1 = MathUtil.mul(rot1, Anchor1);
		r2 = MathUtil.mul(rot2, Anchor2);

		Matrix2f K1 = new Matrix2f();
		K1.col1.X = body1.getInvMass() + body2.getInvMass();
		K1.col2.X = 0.0f;
		K1.col1.Y = 0.0f;
		K1.col2.Y = body1.getInvMass() + body2.getInvMass();

		Matrix2f K2 = new Matrix2f();
		K2.col1.X = body1.getInvI() * r1.Y * r1.Y;
		K2.col2.X = -body1.getInvI() * r1.X * r1.Y;
		K2.col1.Y = -body1.getInvI() * r1.X * r1.Y;
		K2.col2.Y = body1.getInvI() * r1.X * r1.X;

		Matrix2f K3 = new Matrix2f();
		K3.col1.X = body2.getInvI() * r2.Y * r2.Y;
		K3.col2.X = -body2.getInvI() * r2.X * r2.Y;
		K3.col1.Y = -body2.getInvI() * r2.X * r2.Y;
		K3.col2.Y = body2.getInvI() * r2.X * r2.X;

		Matrix2f K = MathUtil.add(MathUtil.add(K1, K2), K3);

		Vector2f p1 = new Vector2f(body1.getROVPosition());
		p1.add(r1);
		Vector2f p2 = new Vector2f(body2.getROVPosition());
		p2.add(r2);
		dp = new Vector2f(p2);
		dp.sub(p1);

		float biasFactor = 0.3f;
		bias = biasFactor * (-dp.lengthSquared() + Distant);

		dp.normalise();

		sc = MathUtil.mul(K, dp).dot(dp);

		Vector2f impulse = new Vector2f(dp);
		impulse.scale(accumulatedImpulse);

		if (!body1.isStatic()) {
			Vector2f accum1 = new Vector2f(impulse);
			accum1.scale(-body1.getInvMass());
			body1.adjustVelocity(accum1);
			body1.adjustAngularVelocity(-(body1.getInvI() * MathUtil.cross(r1,
					impulse)));
		}

		if (!body2.isStatic()) {
			Vector2f accum2 = new Vector2f(impulse);
			accum2.scale(body2.getInvMass());
			body2.adjustVelocity(accum2);
			body2.adjustAngularVelocity(body2.getInvI()
					* MathUtil.cross(r2, impulse));
		}
	}

	/**
	 * @see net.phys2d.raw.Joint#setRelaxation(float)
	 */
	public void setRelaxation(float relaxation) {
	}

	public float getDistant() {
		return Distant;
	}

	public void setDistant(float distant) {
		Distant = distant;
	}

}
