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
package net.phys2d.raw.test;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import net.phys2d.math.MathUtil;
import net.phys2d.math.Matrix2f;
import net.phys2d.math.ROVector2f;
import net.phys2d.math.Vector2f;
import net.phys2d.raw.AngleJoint;
import net.phys2d.raw.Arbiter;
import net.phys2d.raw.ArbiterList;
import net.phys2d.raw.BasicJoint;
import net.phys2d.raw.Body;
import net.phys2d.raw.BodyList;
import net.phys2d.raw.Contact;
import net.phys2d.raw.DistanceJoint;
import net.phys2d.raw.FixedJoint;
import net.phys2d.raw.Joint;
import net.phys2d.raw.JointList;
import net.phys2d.raw.SlideJoint;
import net.phys2d.raw.SpringJoint;
import net.phys2d.raw.World;
import net.phys2d.raw.shapes.Box;
import net.phys2d.raw.shapes.Circle;
import net.phys2d.raw.shapes.Line;
import net.phys2d.raw.shapes.Polygon;
import net.phys2d.raw.strategies.QuadSpaceStrategy;

/**
 * A common demo box super class.
 * 
 * @author Kevin Glass
 */
public abstract class AbstractDemo {
	/** The frame displaying the demo */
	protected Frame frame;
	/** The title of the current demo */
	protected String title;
	/** The world containing the physics model */
	protected World world = new World(new Vector2f(0.0f, 10.0f), 10, new QuadSpaceStrategy(20,5));  //  @jve:decl-index=0:
	/** True if the simulation is running */
	private boolean running = true;
	/** The rendering strategy */
	private BufferStrategy strategy;
	
	/** True if we should reset the demo on the next loop */
	protected boolean needsReset;
	/** True if we should render normals */
	private boolean normals = true;
	/** True if we should render contact points */
	private boolean contacts = true;
	
	private boolean pausing = false;
	
	int WorldSizeX = 800;
	int WorldSizeY = 600;
	
	/**
	 * Create a new demo
	 * 
	 * @param title The title of the demo
	 */
	public AbstractDemo(String title) {
		this.title = title;
		initGUI();
		
		
	}
	
	
	/** 
	 * Retrieve the title of the demo
	 * 
	 * @return The title of the demo
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Notification that a key was pressed
	 * 
	 * @param c The character of key hit
	 */
	protected void keyHit(char c) {
		if (c == 'r') {
			needsReset = true;
		}
		if (c == 'c') {
			normals = !normals;
			contacts = !contacts;
		}
		if (c == 'd') {
			world.clear();
		}
	
	}
	
	/**
	 * Initialise the GUI 
	 */
	private void initGUI() {
		frame = new Frame(title);
		frame.setResizable(false);
		frame.setIgnoreRepaint(true);
		frame.setSize(WorldSizeX,WorldSizeY);
		
		int x = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() - WorldSizeX) / 2;
		int y = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() - WorldSizeY) / 2;
		
		frame.setLocation(x,y);
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				running = false;
				System.exit(0);
			}
		});
		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				keyHit(e.getKeyChar());
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 27) {
					System.exit(0);
				}
			}
			
		});
		
		frame.setVisible(true);
		frame.createBufferStrategy(2);
		
		strategy = frame.getBufferStrategy();
	}
	
	/**
	 * Start the simulation running
	 */
	public void start() {
	    	
		initDemo();
		
		evo();
	}
	
	private void evo() {
		float target = 1000 / 60.0f;
		float frameAverage = target;
		long lastFrame = System.currentTimeMillis();
		float yield = 10000f;
		float damping = 0.1f;
		
		long renderTime = 0;
		long logicTime = 0;
		
		while (running) {
			while(isPausing())
			{
				System.err.println("Pausing");
				try {
					Thread.sleep(100);
					synchronized(this)
					{
					notify();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			// adaptive timing loop from Master Onyx
			long timeNow = System.currentTimeMillis();
			frameAverage = (frameAverage * 10 + (timeNow - lastFrame)) / 11;
			lastFrame = timeNow;
			
			yield+=yield*((target/frameAverage)-1)*damping+0.05f;

			for(int i=0;i<yield;i++) {
				Thread.yield();
			}
			
			// render
			long beforeRender = System.currentTimeMillis();
			Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
			g.setColor(Color.white);
			g.fillRect(0,0,WorldSizeX,WorldSizeY);
			
			draw(g);
			renderGUI(g);
			g.setColor(Color.black);
			g.drawString("FAv: "+frameAverage,10,50);
			g.drawString("FPS: "+(int) (1000 / frameAverage),10,70);
			g.drawString("Yield: "+yield,10,90);
			synchronized (this)
			{
			g.drawString("Arbiters: "+world.getArbiters().size(),10,110);
			g.drawString("Bodies: "+world.getBodies().size(),10,130);
			g.drawString("R: "+renderTime,10,150);
			g.drawString("L: "+logicTime,10,170);
			g.drawString("Energy: "+world.getTotalEnergy(),10,190);
			}
			g.dispose();
			strategy.show();
			renderTime = System.currentTimeMillis() - beforeRender;
			
			// update data model
			long beforeLogic = System.currentTimeMillis();
			synchronized (this)
			{
			for (int i=0;i<5;i++) {
				world.step();
			}
			}
			logicTime = System.currentTimeMillis() - beforeLogic;
			
			if (needsReset) {
				synchronized (this)
				{
				world.clear();
				initDemo();
				needsReset = false;
				frameAverage = target;
				yield = 10000f;
				}
			}
			synchronized (this)
			{
			update();
			}
			
		}
	}


	/**
	 * Update the demo - just in case we want to add anything over
	 * the top
	 */
	protected void update() {
	    // TODO : Kill all body that are out of world if needed.
	}
	
	/**
	 * Demo customisable GUI render
	 * 
	 * @param g The graphics context to use for rendering here
	 */
	protected void renderGUI(Graphics2D g) {
		g.setColor(Color.black);
		g.drawString("Del - Delete Body",15,490);
		g.drawString("D - Clear All",15,510);
		g.drawString("R - Restart Demo",15,530);
		g.drawString("C - Show Contact/Normals",15,550);
		
		g.drawString("Left Click - Select, move, link, create ...",15,570);
	//	g.drawString("Right Click - Select body/Joint",15,470);
	}
	
	/**
	 * Draw a specific contact point determined from the simulation
	 * 
	 * @param g The graphics context on which to draw
	 * @param contact The contact to draw
	 */
	protected void drawContact(Graphics2D g, Contact contact) {
		int x = (int) contact.getPosition().getX();
		int y = (int) contact.getPosition().getY();
		if (contacts) {
			g.setColor(Color.blue);
			g.fillOval(x-3,y-3,6,6);
		}
		
		if (normals) {
			int dx = (int) (contact.getNormal().getX() * 10);
			int dy = (int) (contact.getNormal().getY() * 10);
			g.setColor(Color.darkGray);
			g.drawLine(x,y,x+dx,y+dy);
		}
	}
	
	/**
	 * Draw a body 
	 * 
	 * @param g The graphics contact on which to draw
	 * @param body The body to be drawn
	 */
	protected void drawBody(Graphics2D g, Body body) {
		if (body.getShape() instanceof Box) {
			drawBoxBody(g,body,(Box) body.getShape());
		}
		if (body.getShape() instanceof Circle) {
			drawCircleBody(g,body,(Circle) body.getShape());
		}
		if (body.getShape() instanceof Line) {
			drawLineBody(g,body,(Line) body.getShape());
		}
		if (body.getShape() instanceof Polygon) {
			drawPolygonBody(g,body,(Polygon) body.getShape());
		}
	}
	
	/**
	 * Draw a polygon into the demo
	 * 
	 * @param g The graphics to draw the poly onto
	 * @param body The body describing the poly's position
	 * @param poly The poly to be drawn
	 */
	protected void drawPolygonBody(Graphics2D g, Body body, Polygon poly) {
		g.setColor(Color.black);

		ROVector2f[] verts = poly.getVertices(body.getROVPosition(), body.getRotation());
		for ( int i = 0, j = verts.length-1; i < verts.length; j = i, i++ ) {			
			g.drawLine(
					(int) (0.5f + verts[i].getX()),
					(int) (0.5f + verts[i].getY()), 
					(int) (0.5f + verts[j].getX()),
					(int) (0.5f + verts[j].getY()));
		}
	}

	/**
	 * Draw a line into the demo
	 * 
	 * @param g The graphics to draw the line onto
	 * @param body The body describing the line's position
	 * @param line The line to be drawn
	 */
	protected void drawLineBody(Graphics2D g, Body body, Line line) {
		g.setColor(Color.black);
//
//		float x = body.getPosition().getX();
//		float y = body.getPosition().getY();
//		float dx = line.getDX();
//		float dy = line.getDY();
//		
//		g.drawLine((int) x,(int) y,(int) (x+dx),(int) (y+dy));
		Vector2f[] verts = line.getVertices(body.getROVPosition(), body.getRotation());
		g.drawLine(
				(int) verts[0].getX(),
				(int) verts[0].getY(), 
				(int) verts[1].getX(),
				(int) verts[1].getY());
	}
	
	/**
	 * Draw a circle in the world
	 * 
	 * @param g The graphics contact on which to draw
	 * @param body The body to be drawn
	 * @param circle The shape to be drawn
	 */
	protected void drawCircleBody(Graphics2D g, Body body, Circle circle) {
		g.setColor(Color.black);
		float x = body.getROVPosition().getX();
		float y = body.getROVPosition().getY();
		float r = circle.getRadius();
		float rot = body.getRotation();
		float xo = (float) (Math.cos(rot) * r);
		float yo = (float) (Math.sin(rot) * r);
		
		g.drawOval((int) (x-r),(int) (y-r),(int) (r*2),(int) (r*2));
		g.drawLine((int) x,(int) y,(int) (x+xo),(int) (y+yo));
	}
	
	/**
	 * Draw a box in the world
	 * 
	 * @param g The graphics contact on which to draw
	 * @param body The body to be drawn
	 * @param box The shape to be drawn
	 */
	protected void drawBoxBody(Graphics2D g, Body body, Box box) {
		Vector2f[] pts = box.getPoints(body.getROVPosition(), body.getRotation());
		
		Vector2f v1 = pts[0];
		Vector2f v2 = pts[1];
		Vector2f v3 = pts[2];
		Vector2f v4 = pts[3];
		
		g.setColor(Color.black);
		g.drawLine((int) v1.X,(int) v1.Y,(int) v2.X,(int) v2.Y);
		g.drawLine((int) v2.X,(int) v2.Y,(int) v3.X,(int) v3.Y);
		g.drawLine((int) v3.X,(int) v3.Y,(int) v4.X,(int) v4.Y);
		g.drawLine((int) v4.X,(int) v4.Y,(int) v1.X,(int) v1.Y);
	}

	/**
	 * Draw a joint 
	 * 
	 * @param g The graphics contact on which to draw
	 * @param j The joint to be drawn
	 */
	public void drawJoint(Graphics2D g, Joint j) {
		if (j instanceof FixedJoint) {
			FixedJoint joint = (FixedJoint) j;
			
			g.setColor(Color.red);
			float x1 = joint.getBody1().getROVPosition().getX();
			float x2 = joint.getBody2().getROVPosition().getX();
			float y1 = joint.getBody1().getROVPosition().getY();
			float y2 = joint.getBody2().getROVPosition().getY();
			
			g.drawLine((int) x1,(int) y1,(int) x2,(int) y2);
		}
		if(j instanceof SlideJoint){
			SlideJoint joint = (SlideJoint) j;
			
			Body b1 = joint.getBody1();
			Body b2 = joint.getBody2();
	
			Matrix2f R1 = new Matrix2f(b1.getRotation());
			Matrix2f R2 = new Matrix2f(b2.getRotation());
	
			ROVector2f x1 = b1.getROVPosition();
			Vector2f p1 = MathUtil.mul(R1,joint.getAnchor1());
			p1.add(x1);
	
			ROVector2f x2 = b2.getROVPosition();
			Vector2f p2 = MathUtil.mul(R2,joint.getAnchor2());
			p2.add(x2);
			
			Vector2f im = new Vector2f(p2);
			im.sub(p1);
			im.normalise();
			
			
			
			g.setColor(Color.red);
			g.drawLine((int)p1.X,(int)p1.Y,(int)(p1.X+im.X*joint.getMinDistance()),(int)(p1.Y+im.Y*joint.getMinDistance()));
			g.setColor(Color.blue);
			g.drawLine((int)(p1.X+im.X*joint.getMinDistance()),(int)(p1.Y+im.Y*joint.getMinDistance()),(int)(p1.X+im.X*joint.getMaxDistance()),(int)(p1.Y+im.Y*joint.getMaxDistance()));
		}
		if(j instanceof AngleJoint){
			AngleJoint angleJoint = (AngleJoint)j;
			Body b1 = angleJoint.getBody1();
			Body b2 = angleJoint.getBody2();
			float RA = j.getBody1().getRotation() + angleJoint.getRotateA();
			float RB = j.getBody1().getRotation() + angleJoint.getRotateB();
			
			Vector2f VA = new Vector2f((float) Math.cos(RA), (float) Math.sin(RA));
			Vector2f VB = new Vector2f((float) Math.cos(RB), (float) Math.sin(RB));
			
			Matrix2f R1 = new Matrix2f(b1.getRotation());
			Matrix2f R2 = new Matrix2f(b2.getRotation());
			
			ROVector2f x1 = b1.getROVPosition();
			Vector2f p1 = MathUtil.mul(R1,angleJoint.getAnchor1());
			p1.add(x1);
	
			ROVector2f x2 = b2.getROVPosition();
			Vector2f p2 = MathUtil.mul(R2,angleJoint.getAnchor2());
			p2.add(x2);
			
			g.setColor(Color.red);
			g.drawLine((int)p1.X,(int)p1.Y,(int)(p1.X+VA.X*20),(int)(p1.Y+VA.Y*20));
			g.drawLine((int)p1.X,(int)p1.Y,(int)(p1.X+VB.X*20),(int)(p1.Y+VB.Y*20));
		}
		if (j instanceof BasicJoint) {
			BasicJoint joint = (BasicJoint) j;
			
			Body b1 = joint.getBody1();
			Body b2 = joint.getBody2();
	
			Matrix2f R1 = new Matrix2f(b1.getRotation());
			Matrix2f R2 = new Matrix2f(b2.getRotation());
	
			ROVector2f x1 = b1.getROVPosition();
			Vector2f p1 = MathUtil.mul(R1,joint.getLocalAnchor1());
			p1.add(x1);
	
			ROVector2f x2 = b2.getROVPosition();
			Vector2f p2 = MathUtil.mul(R2,joint.getLocalAnchor2());
			p2.add(x2);
	
			g.setColor(Color.red);
			g.drawLine((int) x1.getX(), (int) x1.getY(), (int) p1.X, (int) p1.Y);
			g.drawLine((int) p1.X, (int) p1.Y, (int) x2.getX(), (int) x2.getY());
			g.drawLine((int) x2.getX(), (int) x2.getY(), (int) p2.X, (int) p2.Y);
			g.drawLine((int) p2.X, (int) p2.Y, (int) x1.getX(), (int) x1.getY());
		}
		if(j instanceof DistanceJoint){
			DistanceJoint joint = (DistanceJoint) j;
			
			Body b1 = joint.getBody1();
			Body b2 = joint.getBody2();
	
			Matrix2f R1 = new Matrix2f(b1.getRotation());
			Matrix2f R2 = new Matrix2f(b2.getRotation());
	
			ROVector2f x1 = b1.getROVPosition();
			Vector2f p1 = MathUtil.mul(R1,joint.getAnchor1());
			p1.add(x1);
	
			ROVector2f x2 = b2.getROVPosition();
			Vector2f p2 = MathUtil.mul(R2,joint.getAnchor2());
			p2.add(x2);
			
			g.setColor(Color.red);
			g.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.X, (int) p2.Y);
		}
		if (j instanceof SpringJoint) {
			SpringJoint joint = (SpringJoint) j;
			
			Body b1 = joint.getBody1();
			Body b2 = joint.getBody2();
	
			Matrix2f R1 = new Matrix2f(b1.getRotation());
			Matrix2f R2 = new Matrix2f(b2.getRotation());
	
			ROVector2f x1 = b1.getROVPosition();
			Vector2f p1 = MathUtil.mul(R1,joint.getLocalAnchor1());
			p1.add(x1);
	
			ROVector2f x2 = b2.getROVPosition();
			Vector2f p2 = MathUtil.mul(R2,joint.getLocalAnchor2());
			p2.add(x2);
			
			g.setColor(Color.red);
			g.drawLine((int) x1.getX(), (int) x1.getY(), (int) p1.X, (int) p1.Y);
			g.drawLine((int) p1.X, (int) p1.Y, (int) p2.getX(), (int) p2.getY());
			g.drawLine((int) p2.getX(), (int) p2.getY(), (int) x2.getX(), (int) x2.getY());
		}
	}
	
	/**
	 * Draw the whole simulation
	 * 
	 * @param g The graphics context on which to draw
	 */
	protected void draw(Graphics2D g) {
		BodyList bodies = world.getBodies();
		
		for (int i=0;i<bodies.size();i++) {
			Body body = bodies.get(i);
			
			drawBody(g, body);
		}
		
		JointList joints = world.getJoints();
		
		for (int i=0;i<joints.size();i++) {
			Joint joint = joints.get(i);
			
			drawJoint(g, joint);
		}
		
		ArbiterList arbs = world.getArbiters();
		
		for (int i=0;i<arbs.size();i++) {
			Arbiter arb = arbs.get(i);
			
			Contact[] contacts = arb.getContacts();
			int numContacts = arb.getNumContacts();
			
			for (int j=0;j<numContacts;j++) {
				drawContact(g, contacts[j]);
			}
		}
	}
	
	/**
	 * Initialise the demo - clear the world
	 */
	public final void initDemo() {
		world.clear();
		world.setGravity(0,10);
		
		System.out.println("Initialising:" +getTitle());
		init(world);
	}

	/**
	 * Should be implemented by the demo, add the bodies/joints
	 * to the world.
	 * 
	 * @param world The world in which the simulation is going to run
	 */
	
	protected abstract void init(World world);
	
	public void save(String filename) throws IOException
	{
		File f = new File(filename);
		FileOutputStream fos = new FileOutputStream(f);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		setPausing(true);
		//this.setRunning(false);
		oos.writeObject(this.world);
		//this.setRunning(true);
		oos.flush();
		oos.close();
		fos.flush();
		fos.close();
		setPausing(false);
	}
	public void load(String filename) throws IOException, ClassNotFoundException
	{
		File f = new File(filename);
		FileInputStream fis = new FileInputStream(f);
		ObjectInputStream iis = new ObjectInputStream(fis);
		setPausing(true);
		world.clear();
		world =(World) iis.readObject();
		iis.close();
		fis.close();
		setPausing(false);
	}


	public synchronized boolean isRunning() {
		return running;
	}


	public synchronized void setRunning(boolean running) {
		this.running = running;
		
		
	}


	public synchronized boolean isPausing() {
		return pausing;
	}


	public synchronized void setPausing(boolean pausing) {
		this.pausing = pausing;
		if (pausing==true)
		//synchronized(this)
		{
			try {
				System.err.println("waiting for the pause");
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
