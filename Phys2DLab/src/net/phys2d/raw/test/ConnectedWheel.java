package net.phys2d.raw.test;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import io.github.warnotte.waxlib3.OBJ2GUI.JWPanel;
import io.github.warnotte.waxlib3.OBJ2GUI.ParseurAnnotations;
import io.github.warnotte.waxlib3.OBJ2GUI.Annotations.GUI_CLASS;
import net.phys2d.math.Vector2f;
import net.phys2d.raw.BasicJoint;
import net.phys2d.raw.Body;
import net.phys2d.raw.BodyList;
import net.phys2d.raw.DistanceJoint;
import net.phys2d.raw.JointList;
import net.phys2d.raw.SlideJoint;
import net.phys2d.raw.SpringJoint;
import net.phys2d.raw.StaticBody;
import net.phys2d.raw.World;
import net.phys2d.raw.shapes.Box;
import net.phys2d.raw.shapes.Circle;
import net.phys2d.raw.shapes.Line;

/**
 * A test to show the FixedAngleJoint at work
 * 
 * @author guRuQu 
 */
public class ConnectedWheel extends AbstractDemo {
	/** The wheel being turned */
	private Body wheel;
	private Body wheel2;
	private final Random rand = new Random(); 
	JPanel panel = new JPanel();
	
	SpringJoint ba6 = null;
	SpringJoint ba7 = null;
	
	/**
	 * Create a new test
	 * @throws Exception 
	 */
	public ConnectedWheel() throws Exception{
		super("Connected Wheel");
		
	
	}
	
	Body CarBody;
	/**
	 * @see net.phys2d.raw.test.AbstractDemo#init(net.phys2d.raw.World)
	 */
	@Override
	protected void init(final World world) {
		
		frame.addMouseListener(new MouseAdapter() {
			private int START_X;
			private int START_Y;

			@Override
			public void mousePressed(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				if (e.getButton()==1)
				{
				}
				if (e.getButton()==3)
				{
					START_X = x;
					START_Y = y;
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				if (e.getButton()==1)
				{
				System.err.println(x+","+y);
				Body box = new Body("Faller", new Circle(3), 4000);
				box.setPosition(x,y);
				world.add(box);
				}
				if (e.getButton()==3)
				{
					StaticBody land = new StaticBody("Line2", new Line(x-START_X,y-START_Y));
					land.setPosition(START_X,START_Y);
					world.add(land);
				}
			}
			
			  
			
		});
		
		create_world();
		
		JFrame frame = new JFrame();
		frame.setSize(320,200);
		frame.add(panel, BorderLayout.CENTER);
		generate_GUI();
		frame.setVisible(true);
	}
	
	private void generate_GUI() {
		panel.removeAll();
		BodyList v = world.getBodies();
		for (int i = 0 ; i < v.size();i++)
		{
			Object z = v.get(i);
			System.err.println(""+z);
			GUI_CLASS annotation = z.getClass().getAnnotation(GUI_CLASS.class);
			if (annotation==null)
				continue;
			
			JWPanel panel1 = null;
			try {
				panel1 = (JWPanel) ParseurAnnotations.CreatePanelFromObject(((Body)z).getName(), z,false);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			/*panel.addMyEventListener(new MyEventListener()
			{
				public void myEventOccurred(MyChangedEvent e)
				{
					System.err.println("*** Object has changed make the needed things ...");
				}
			});*/
			panel.add(panel1);
			
		}
		JointList v2 = world.getJoints();
		for (int i = 0 ; i < v2.size();i++)
		{
			Object z = v2.get(i);
			System.err.println(""+z);
			GUI_CLASS annotation = z.getClass().getAnnotation(GUI_CLASS.class);
			if (annotation==null)
				continue;
			
			JWPanel panel1 = null;
			try {
				panel1 = (JWPanel) ParseurAnnotations.CreatePanelFromObject(z.toString(), z,false);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			/*panel.addMyEventListener(new MyEventListener()
			{
				public void myEventOccurred(MyChangedEvent e)
				{
					System.err.println("*** Object has changed make the needed things ...");
				}
			});*/
			panel.add(panel1);
			
		}
	}

	private void create_world() {

		/*Body leftAxis = new StaticBody(new Circle(10));
		leftAxis.setPosition(100,250);
		leftAxis.setRestitution(1);
		world.add(leftAxis);*/
		
		Body sol = new Body(new Box(600,8),250);
		sol.setPosition(300, 150);
		sol.setMoveable(false);
		sol.setRotatable(false);
		world.add(sol);
		
		Body sol1 = new Body(new Box(600,8),250);
		sol1.setPosition(50, 12);
		sol1.setMoveable(false);
		sol1.setRotatable(false);
		world.add(sol1);
		
	/*	Body sol2 = new Body(new Box(40,10),500);
		sol2.setPosition(250,-400);
		sol2.setMoveable(true);
	//	sol2.setRotatable(false);
		world.add(sol2);*/
	
	/*	
		Body sol3 = new Body(new Box(40,10),500);
		sol3.setPosition(250,120);
	//	sol3.setMoveable(true);
	//	sol2.setRotatable(false);
		world.add(sol3);*/
	
		
		// I create 2 wheel for my car
		wheel = new Body(new Circle(12),0200);
		wheel.setPosition(25, 75);
		wheel.setMoveable(true);
		wheel.setDamping(1f);
		wheel.setFriction(250f);
		world.add(wheel);
		
		wheel2 = new Body(new Circle(12),0200);
		wheel2.setPosition(175, 75);
		wheel2.setMoveable(true);
		wheel2.setDamping(1f);
		wheel2.setFriction(250f);
		world.add(wheel2);
		
		// The Car body
		CarBody = new Body(new Box(100,2),25000);
		CarBody.setIsResting(true);
		CarBody.setPosition(100, 30);
		world.add(CarBody);
		
		
		// The part where the suspension will be attached
		Body wheel_anc_1 = new Body(new Box(3,3),500);
		wheel_anc_1.setIsResting(true);
		wheel_anc_1.setPosition(60, 27);
		world.add(wheel_anc_1);
		// for the other wheel
		Body wheel_anc_2 = new Body(new Box(3,3),500);
		wheel_anc_2.setIsResting(true);
		wheel_anc_2.setPosition(140, 27);
		world.add(wheel_anc_2);
		
		// I attache these 2 pieces on left and right of the car to put suspension on it. (it could be made without these 2 wheel anchor)
		BasicJoint ba1 = new BasicJoint(CarBody, wheel_anc_1, new Vector2f(wheel_anc_1.getROVPosition()));
		world.add(ba1);
		BasicJoint ba2 = new BasicJoint(CarBody, wheel_anc_2, new Vector2f(wheel_anc_2.getROVPosition()));
		world.add(ba2);
		
		/*BasicJoint ba1 = new BasicJoint(CarBody, wheel_anc_1, new Vector2f(wheel_anc_1.getPosition()));
		world.add(ba1);
		BasicJoint ba2 = new BasicJoint(CarBody, wheel_anc_2, new Vector2f(wheel_anc_2.getPosition()));
		world.add(ba2);
		*/
	// I attache the wheels with fixed joint to keep the distance between one wheel and the body.
		DistanceJoint angle = new DistanceJoint(CarBody, wheel, new Vector2f(),new Vector2f(0,0),60f);
		world.add(angle);
		DistanceJoint angle2 = new DistanceJoint(CarBody, wheel2, new Vector2f(),new Vector2f(0,0),60f);
		world.add(angle2);
		
		// There i need to make a vertical joint to linke the center of the weel to the anchor
		// to simulate a suspension.
		
		ba6 = new SpringJoint(wheel, wheel_anc_1, new Vector2f(wheel.getROVPosition()),  new Vector2f(wheel_anc_1.getROVPosition()));
		ba6.setMinSpringSize(65);
		ba6.setMaxSpringSize(477);
		ba6.setSpringSize(180);
	//	ba6.setRelaxation(0.75f);
		
		world.add(ba6);
		ba7 = new SpringJoint(wheel2, wheel_anc_2, new Vector2f(wheel2.getROVPosition()),  new Vector2f(wheel_anc_2.getROVPosition()));
		ba7.setMinSpringSize(65);
		ba7.setMaxSpringSize(477);
		ba7.setSpringSize(180);
	//	ba7.setRelaxation(0.75f);
		world.add(ba7);
		
		SlideJoint cj = new SlideJoint(wheel,wheel_anc_1, new Vector2f(0,0), new Vector2f(0,0),25,105,0.5f);
		SlideJoint cj2 = new SlideJoint(wheel2,wheel_anc_2, new Vector2f(0,0), new Vector2f(0,0),25,105,0.5f);
		SlideJoint cj3 = new SlideJoint(wheel,wheel2, new Vector2f(0,0), new Vector2f(0,0),55,125,0.5f);
		world.add(cj);
		world.add(cj2);
		world.add(cj3);
		
		
		
	}

	/**
	 * Notification that a key was pressed
	 * 
	 * @param c The character of key hit
	 */
	@Override
	protected void keyHit(char c) {
		if (c == 'z') {
			wheel.adjustAngularVelocity(5f);
			wheel2.adjustAngularVelocity(5f);
			wheel.setTorque(20000);
			wheel2.setTorque(20000);
		}
		if (c == 'a') {
				wheel.adjustAngularVelocity(-5f);
				wheel2.adjustAngularVelocity(-5f);
				wheel.setTorque(-20000);
				wheel2.setTorque(-20000);
			}
		
		if (c == 'f') {
			//	wheel.adjustAngularVelocity(-0.5f);
			wheel2.adjustBiasedVelocity(new Vector2f(0, -100));
			//	wheel.adjustAngularVelocity(wheel.getAngularVelocity()-0.01f);
			//	wheel2.adjustAngularVelocity(wheel2.getAngularVelocity()-0.01f);
			//	wheel.setTorque(-2000);
			}
		
		if (c=='d')
		{
			Body sol2 = new Body(new Box(20,5),50);
			sol2.setPosition(rand.nextInt(650),100);
			sol2.setMoveable(true);
		//	sol2.setRotatable(false);
			world.add(sol2);
		}
		
		if (c=='r')
		{
			world.clear();
			create_world();
		}
		
	}
	
	/**
	 * @see net.phys2d.raw.test.AbstractDemo#update()
	 */
	@Override
	protected void update(){
		
		wheel.setTorque(0);
		wheel2.setTorque(0);
		
		//wheel.setTorque(200);
		//wheel2.setTorque(200);
	}

	/**
	 * Entry point for tetsing
	 * 
	 * @param argv The arguments to the test
	 * @throws Exception 
	 */
	public static void main(String[] argv) throws Exception {
		ConnectedWheel cw = new ConnectedWheel();
		cw.start();
		
		
	}

}
