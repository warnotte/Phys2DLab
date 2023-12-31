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
package net.phys2d.math;

import java.io.Serializable;

import io.github.warnotte.waxlib3.OBJ2GUI.Annotations.GUI_CLASS;
import io.github.warnotte.waxlib3.OBJ2GUI.Annotations.GUI_FIELD_TYPE;

/**
 * A two dimensional vector
 * 
 * @author Kevin Glass
 */
@GUI_CLASS(type=GUI_CLASS.Type.BoxLayout, BoxLayout_property=GUI_CLASS.Type_BoxLayout.Y)
public strictfp class Vector2f implements ROVector2f, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** The x component of this vector */
	@GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.TEXTFIELD)
	public float X;
	/** The y component of this vector */
	@GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.TEXTFIELD)
	public float Y;
	
	public void setX(float x) {
		X = x;
	}

	public void setY(float y) {
		Y = y;
	}

	/**
	 * Create an empty vector
	 */
	public Vector2f() {
	}
	
	/**
	 * @see net.phys2d.math.ROVector2f#getX()
	 */
	public float getX() {
		return X;
	}
	
	/**
	 * @see net.phys2d.math.ROVector2f#getY()
	 */
	public float getY() {
		return Y;
	}
	
	/**
	 * Create a new vector based on another
	 * 
	 * @param other The other vector to copy into this one
	 */
	public Vector2f(ROVector2f other) {
		this(other.getX(),other.getY());
	}
	
	/**
	 * Create a new vector 
	 * 
	 * @param x The x component to assign
	 * @param y The y component to assign
	 */
	public Vector2f(float x, float y) {
		this.X = x;
		this.Y = y;
	}

	/**
	 * Set the value of this vector
	 * 
	 * @param other The values to set into the vector
	 */
	public void set(ROVector2f other) {
		set(other.getX(),other.getY());
	}
	
	/**
	 * @see net.phys2d.math.ROVector2f#dot(net.phys2d.math.ROVector2f)
	 */
	public float dot(ROVector2f other) {
		return (X * other.getX()) + (Y * other.getY());
	}
	
	/**
	 * Set the values in this vector
	 * 
	 * @param x The x component to set
	 * @param y The y component to set
	 */
	public void set(float x, float y) { 
		this.X = x; 
		this.Y = y; 
	}

	/**
	 * Negate this vector 
	 * 
	 * @return A copy of this vector negated
	 */
	public Vector2f negate() {
		return new Vector2f(-X, -Y); 
	}
	
	/**
	 * Add a vector to this vector
	 * 
	 * @param v The vector to add
	 */
	public void add(ROVector2f v)
	{
		X += v.getX(); 
		Y += v.getY();
	}
	
	/**
	 * Subtract a vector from this vector
	 * 
	 * @param v The vector subtract
	 */
	public void sub(ROVector2f v)
	{
		X -= v.getX(); 
		Y -= v.getY();
	}

	/**
	 * Scale this vector by a value
	 * 
	 * @param a The value to scale this vector by
	 */
	public void scale(float a)
	{
		X *= a; 
		Y *= a;
	}

	/**
	 * Normalise the vector
	 *
	 */
	public void normalise() {
		float l = length();
		
		if ( l == 0 )
			return;
		
		X /= l;
		Y /= l;
	}
	
	/**
	 * The length of the vector squared
	 * 
	 * @return The length of the vector squared
	 */
	public float lengthSquared() {
		return (X * X) + (Y * Y);
	}
	
	/**
	 * @see net.phys2d.math.ROVector2f#length()
	 */
	public float length() 
	{
		return (float) Math.sqrt(lengthSquared());
	}
	
	/**
	 * Project this vector onto another
	 * 
	 * @param b The vector to project onto
	 * @param result The projected vector
	 */
	public void projectOntoUnit(ROVector2f b, Vector2f result) {
		float dp = b.dot(this);
		
		result.X = dp * b.getX();
		result.Y = dp * b.getY();
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[Vec "+X+","+Y+" ("+length()+")]";
	}
	
	/**
	 * Get the distance from this point to another
	 * 
	 * @param other The other point we're measuring to
	 * @return The distance to the other point
	 */
	public float distance(ROVector2f other) {
		return (float) Math.sqrt(distanceSquared(other));
	}

	/**
	 * Get the distance squared from this point to another
	 * 
	 * @param other The other point we're measuring to
	 * @return The distance to the other point
	 */
	public float distanceSquared(ROVector2f other) {
		float dx = other.getX() - getX();
		float dy = other.getY() - getY();
		
		return (dx*dx)+(dy*dy);
	}
	
	/**
	 * Compare two vectors allowing for a (small) error as indicated by the delta.
	 * Note that the delta is used for the vector's components separately, i.e.
	 * any other vector that is contained in the square box with sides 2*delta and this
	 * vector at the center is considered equal. 
	 *  
	 * @param other The other vector to compare this one to
	 * @param delta The allowed error
	 * @return True iff this vector is equal to other, with a tolerance defined by delta
	 */
	public boolean equalsDelta(ROVector2f other, float delta) {
		return (other.getX() - delta < X &&
				other.getX() + delta > X &&
				other.getY() - delta < Y &&
				other.getY() + delta > Y );
		
	}
}
