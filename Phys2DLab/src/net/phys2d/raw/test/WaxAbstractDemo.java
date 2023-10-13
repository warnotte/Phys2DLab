package net.phys2d.raw.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import io.github.warnotte.waxlib3.OBJ2GUI.JWPanel;
import io.github.warnotte.waxlib3.OBJ2GUI.ParseurAnnotations;
import io.github.warnotte.waxlib3.OBJ2GUI.Annotations.GUI_CLASS;
import io.github.warnotte.waxlib3.OBJ2GUI.Annotations.GUI_FIELD_TYPE;
import io.github.warnotte.waxlib3.waxlibswingcomponents.Dialog.DialogDivers;
import net.phys2d.math.ROVector2f;
import net.phys2d.math.Vector2f;
import net.phys2d.raw.BasicJoint;
import net.phys2d.raw.Body;
import net.phys2d.raw.Contact;
import net.phys2d.raw.DistanceJoint;
import net.phys2d.raw.Joint;
import net.phys2d.raw.JointList;
import net.phys2d.raw.SlideJoint;
import net.phys2d.raw.SpringJoint;
import net.phys2d.raw.StaticBody;
import net.phys2d.raw.World;
import net.phys2d.raw.collide.Collider;
import net.phys2d.raw.collide.ColliderFactory;
import net.phys2d.raw.collide.ColliderUnavailableException;
import net.phys2d.raw.shapes.AABox;
import net.phys2d.raw.shapes.Box;
import net.phys2d.raw.shapes.Circle;
import net.phys2d.raw.shapes.DynamicShape;
import net.phys2d.raw.shapes.Line;


@GUI_CLASS(type=GUI_CLASS.Type.BoxLayout, BoxLayout_property=GUI_CLASS.Type_BoxLayout.Y)
public abstract class WaxAbstractDemo extends AbstractDemo implements MouseMotionListener, MouseListener, MouseWheelListener {

	enum MODE {EDIT_BODY,CREATE_BODY, CREATE_JOINT};
	enum OBJECT_TO_ADD {BOX, CIRCLE, LINE, STATIC_LINE};
	enum JOINT_TO_ADD {BASIC, DISTANCE, SPRING, SLIDE, ELASTIC};
	
	protected JPanel Panel_Bouton = new JPanel();
	protected JWPanel Configurationpanel_object;
	protected JWPanel Configurationpanel_object_to_add;
	protected JXTaskPaneContainer Configurationpanel;
	JScrollPane jscp =null;
	
	private Body Touched_Body;
	private Body Selected_Body;
	private Body newBody;
	boolean moveable;
	boolean rotatable;
	int old_x=-1;
	int old_y=-1;
	int original_X=0;
	int original_Y=0;
//	@GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.COMBO)
	private MODE Mode=MODE.EDIT_BODY;
	@GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.COMBO)
	private OBJECT_TO_ADD Object_to_add=OBJECT_TO_ADD.BOX;
	@GUI_FIELD_TYPE(type=GUI_FIELD_TYPE.Type.COMBO)
	private JOINT_TO_ADD Joint_To_Add=JOINT_TO_ADD.DISTANCE;
	
	
	public WaxAbstractDemo(String title) {
		super(title);
		
		JFrame frame = new JFrame();
		Configurationpanel=new JXTaskPaneContainer();
	//	Configurationpanel.setLayout(new BoxLayout(Configurationpanel, BoxLayout.Y_AXIS));
		Configurationpanel_object_to_add=null;
		Configurationpanel_object=null;
		frame.setSize(480,640);
		jscp = new JScrollPane(Configurationpanel);
		frame.add(jscp, BorderLayout.CENTER);
		try {
		    Configurationpanel_object_to_add = (JWPanel) ParseurAnnotations.CreatePanelFromObject("Global", this,false);
		    frame.add(Configurationpanel_object_to_add, BorderLayout.NORTH);
		    Body b = new Body(new Box(2,2), 0);
		    Configurationpanel_object = (JWPanel) ParseurAnnotations.CreatePanelFromObject("Body Properties",  b,false);
		    frame.add(Configurationpanel_object, BorderLayout.EAST);
		} catch (Exception e1) {
		    frame.add(new JLabel("Error #65498"), BorderLayout.NORTH);
		    e1.printStackTrace();
		}
		
		JButton button_refresh = new JButton("Refresh");
		button_refresh.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
			    generate_GUI();
			}
		});
		JButton button_clear = new JButton("Reset World");
		button_clear.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				world.clear();
			}
		});
		
		JButton button_load = new JButton("Load");
		button_load.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				try {
					String filename = DialogDivers.LoadDialog(new JFrame(), "dat").getAbsolutePath();
					load(filename);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (HeadlessException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (Exception e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		JButton button_save = new JButton("Save");
		button_save.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				try {
					String filename = DialogDivers.SaveDialog(new JFrame(), "dat"); 
					save(filename);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		Panel_Bouton.add(button_refresh);
		Panel_Bouton.add(button_clear);
		Panel_Bouton.add(button_load);
		Panel_Bouton.add(button_save);
		frame.add(Panel_Bouton, BorderLayout.SOUTH);
		
		generate_GUI();
		
		frame.setVisible(true);
		this.frame.addMouseMotionListener(this);
		this.frame.addMouseListener(this);
		this.frame.addMouseWheelListener(this);
	}


	    
	
	
	@Override
	protected abstract void init(World world);


	@Override
	protected void draw(Graphics2D g) {
		super.draw(g);
		
		int DX = -(original_X - old_x);
		int DY = -(original_Y - old_y);
		
		
		
		if (Mode==MODE.CREATE_BODY)
		{
			g.setColor(Color.GREEN);
			if (Object_to_add==OBJECT_TO_ADD.BOX)
				g.drawRect(original_X-DX/2, original_Y-DY/2, DX, DY);
			if (Object_to_add==OBJECT_TO_ADD.CIRCLE)
			{
			    int radius =(int) Math.sqrt((DX*DX+DY*DY))*2;
				g.drawOval(original_X-radius/2, original_Y-radius/2, radius, radius);
			}
			if (Object_to_add==OBJECT_TO_ADD.LINE)
				g.drawLine(original_X, original_Y, original_X+DX, original_Y+DY);
			if (Object_to_add==OBJECT_TO_ADD.STATIC_LINE)
			{
				g.setColor(Color.BLACK);
				g.drawLine(original_X, original_Y, original_X+DX, original_Y+DY);
			}
		}
		if (Mode==MODE.EDIT_BODY)
		{
			
		}
		if (Mode==MODE.CREATE_JOINT)
		{
			if (Selected_Body!=null)
			{
				g.setColor(Color.RED);
				g.drawLine(original_X, original_Y, original_X+DX, original_Y+DY);
			}
		}
		
		// Entoure l'objet selectionn�
		if (Selected_Body!=null)
		{
		    AABox box = Selected_Body.getShape().getBounds();
		    g.setColor(Color.GRAY);
		    //int radius = 50;
		    int original_X = (int) Selected_Body.getROVPosition().getX();
		    int original_Y = (int) Selected_Body.getROVPosition().getY();
		   // g.drawOval(original_X-radius/2, original_Y-radius/2, radius, radius);
		    g.drawRect((int)(original_X-box.getWidth()/2-5),
			    (int)(original_Y-box.getHeight()/2-5),
			    (int)(box.getWidth()+10),
			    (int)(box.getHeight()+10));//original_X-radius/2, original_Y-radius/2, radius, radius);
		}
		
		g.setColor(Color.black);
		String mode = Mode.toString();
		Font oldfont = g.getFont();
		FontRenderContext frc = g.getFontRenderContext();
		Rectangle2D rect = font.getStringBounds(mode, frc);
		g.setFont(font);
		g.drawString(mode, (int)(700-rect.getWidth()/2), 50);
		g.setFont(oldfont);
		
	}
	Font font = new Font("Arial",Font.BOLD,20);
	
	
	protected Body getTouchedObject(double x, double y)
	{
	    Body b = new Body(new Box(2,2), 0);
	    b.setPosition((int)x,(int)y);
	    Body i = null;
	    int nbr = world.getBodies().size();
	    for (int z = 0 ; z < nbr;z++)
	    {
		i = world.getBodies().get(z);;
	    Contact[] contacts = new Contact[] {new Contact(), new Contact()};
	    ColliderFactory cf = new ColliderFactory();
	    Collider c;
	    try {
		c = cf.createCollider(b, i);
		 int count = c.collide(contacts, b, i);
		 if (count !=0)
		 {
		   return i;
		 }
	    } catch (ColliderUnavailableException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    }
		
	    //world.getBodies().get(0).isTouchingStatic(path)
	    return null;
	    
	}
	

	@Override
	public void mouseDragged(MouseEvent arg0) {
	    int x = arg0.getX();
	    int y = arg0.getY();
	    
	    int dx = (old_x - x);
	    int dy = (old_y - y);
	    
	    old_x=x;
	    old_y=y;
	    int modif = arg0.getModifiers();
            if ((modif & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) 
            {
            	if (Mode==MODE.EDIT_BODY)
            	{
		    	    if (Selected_Body!=null)
		    	    {
		    	    //	System.err.println("DxDy = "+dx+", "+dy);
		    	    	//Selected_Body.setPosition(x, y);
		    	    	Selected_Body.move(x, y);
		    	    	
		    	    }
		        }
            	if (Mode==MODE.CREATE_JOINT)
            	{
            		
            	}
            }
            if ((modif & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
            {
        	
    	    }
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
	    int x,y;
	    x= arg0.getX();
	    y= arg0.getY();
	    
	    
	}
	
	
	public void mouseReleased(MouseEvent arg0)
	{
	    int x,y;
	    System.err.println("MouseMoved");
	    x= arg0.getX();
	    y= arg0.getY();
	    int dx = (old_x - x);
	    int dy = (old_y - y);
	    old_x=x;
	    old_y=y;
    	    int DX = -(original_X - x);
	    int DY = -(original_Y - y);
	    if (arg0.getButton()==1)
	    {
		
		
	    		if (Mode==MODE.EDIT_BODY)
	    		{
	    			if (Selected_Body!=null)
	    	    	{
	    				Selected_Body.setMoveable(moveable);
	    				Selected_Body.setRotatable(rotatable);
	    			
	    			//  if ((dx!=0) || (dy!=0))
	    			{
	    			//	System.err.println("DxDy2 "+dx+","+dy);
	    				//	Touched_Body.addForce(new Vector2f(250*DX,250*DY));
	    			//	Touched_Body.addForce(new Vector2f(250*DX,250*DY));
	    			}	
	    	    	}
	    		}
				if (Mode==MODE.CREATE_JOINT)
				{
					if (Selected_Body!=null)
			    	{
					Body first_body = Selected_Body;
					if (first_body!=null)
					{
					    Body second_body = getTouchedObject(x,y);
						if (second_body!=null)
						{
							ROVector2f Dm1 = first_body.getROVPosition();
							ROVector2f Dm2 = second_body.getROVPosition();
							float distance = Dm1.distance(Dm2);//;(float) Math.sqrt(DX*DX+DY*DY)*1.35f;
							System.err.println("Distance = "+distance);
							Joint cj = null;
							
							if (Joint_To_Add==JOINT_TO_ADD.SLIDE)
								cj = new SlideJoint(first_body,second_body, new Vector2f(0,0), new Vector2f(0,0),distance/2,distance,0.5f);
							if (Joint_To_Add==JOINT_TO_ADD.SPRING)
								cj = new SpringJoint(first_body, second_body, new Vector2f(Dm1),  new Vector2f(Dm2));
							if (Joint_To_Add==JOINT_TO_ADD.DISTANCE)
								cj = new DistanceJoint(first_body, second_body, new Vector2f(),new Vector2f(0,0),distance);
							if (Joint_To_Add==JOINT_TO_ADD.BASIC)
								cj = new BasicJoint(first_body, second_body, new Vector2f(Dm1));
							
							//DistanceJoint dj = new DistanceJoint(first_body, second_body,new Vector2f(0, 0), new Vector2f(0, 0), distance);
							world.add(cj);
							generate_GUI();
							Selected_Body=second_body;
						}
					}
			    	}
				}
				if (Mode==MODE.CREATE_BODY)
				{
					//Cr�e un nouveau element
			    	//Object_to_add
			    	DynamicShape shape = null;
			    	
			    	if (Object_to_add==OBJECT_TO_ADD.BOX) shape = new Box(DX,DY);
			    	if (Object_to_add==OBJECT_TO_ADD.CIRCLE) shape = new Circle((float) Math.sqrt((DX*DX+DY*DY)));
			    	if (Object_to_add==OBJECT_TO_ADD.LINE) shape = new Line(x-original_X,y-original_Y);
			    	
			    	//if (Object_to_add==OBJECT_TO_ADD.BOX) shape = new AABox(DX,DY);
			    	//if (Object_to_add==OBJECT_TO_ADD.BOX) shape = new ConvexPolygon((int)DX,(int)DY);
			    	//if (Object_to_add==OBJECT_TO_ADD.BOX) shape = new Polygon(DX,DY);
			    	if (Object_to_add==OBJECT_TO_ADD.STATIC_LINE)
			    	{
			    		newBody = new StaticBody("Line2", new Line(x-original_X,y-original_Y));
			    		newBody.setPosition(original_X,original_Y);
			    	}
			    	else
			    	{
			    		newBody = new Body(shape, DX*DX/10);
			    		newBody.setPosition(original_X,original_Y);
			    		//newBody.setPosition(x-DX/2,y-DY/2);
						newBody.setMoveable(false);
						newBody.setRotatable(false);
			    	}
					world.add(newBody);
					original_X=x;
					original_Y=y;
					Selected_Body=newBody;
				}
	    }
	    if (arg0.getButton()==2)
		{
		}
	    if (arg0.getButton()==3)
	    {
		Body body = getTouchedObject(x,y);
		Selected_Body=body;
		if (body!=null)
		{
		    refresh_panel_edition_objet(body);
		}
	    }
	}
	
		
	
	private void refresh_panel_edition_objet(Body selected_Body2) {
	     try {
		ParseurAnnotations.Refresh_PanelEditor_For_Object("Edit properties", Configurationpanel_object, selected_Body2, Configurationpanel_object, false);
	    } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}





	public void mousePressed(MouseEvent arg0)
	{
	    int x,y;
	    System.err.println("MouseMoved");
	    x= arg0.getX();
	    y= arg0.getY();
	    original_X = x;
	    original_Y = y;
	    old_x=x;
	    old_y=y;
	    if (arg0.getButton()==1)
	    {
		
		
	    	if (Mode==MODE.EDIT_BODY)
	    	{
	    		//Body body = getTouchedObject((double)x,(double)y);
	    		
	    		Selected_Body = getTouchedObject(x,y);
	    	if (Selected_Body!=null)
	    	{
	    		if (Selected_Body!=null)
	    		   refresh_panel_edition_objet(Selected_Body);
	    		moveable = Selected_Body.isMoveable();
	    		rotatable = Selected_Body.isRotatable();
	    		Selected_Body.setMoveable(false);
	    		Selected_Body.setRotatable(false);
	    		Selected_Body.setForce(0,0);
	    		System.err.println("Body selected = "+Touched_Body);
	    	}
	    	}
	    	if (Mode==MODE.CREATE_JOINT)
	    	{
	    		Selected_Body = getTouchedObject(x,y);
	    	}
	    }
	    if (arg0.getButton()==2)
	    {
	    }
		if (arg0.getButton()==3)
	    {
	    }
	}
	@Override
	public void mouseClicked(MouseEvent e) {
	    // TODO Auto-generated method stub
	}
	@Override
	public void mouseEntered(MouseEvent e) {
	    // TODO Auto-generated method stub
	}
	@Override
	public void mouseExited(MouseEvent e) {
	    // TODO Auto-generated method stub
	}
	@Override
	public void mouseWheelMoved(MouseWheelEvent e){
		int dy = e.getWheelRotation();
		MODE lst []=MODE.values();
		if (dy>1)
		{
			Mode=MODE.values()[((Mode.ordinal()+1)%lst.length)];
		}
		else
		{
			int v = ((Mode.ordinal()-1)%lst.length);
			if (v<=-1) v=lst.length-1;
			Mode=MODE.values()[v];
		}
		System.err.println("Mode == "+Mode);
		
		 // TODO Auto-generated method stub
		/*if (Mode==MODE.CREATE)
		{
			
		}
		if (Mode==MODE.LINK)
		{
		}
		if (Mode==MODE.MOVE)
		{
			
		}*/
	}
	
	@Override
	protected void keyHit(char c) {
	    super.keyHit(c);
	    if (c==KeyEvent.VK_ESCAPE)
	    {
	    	Selected_Body=null;
	    }
	    if (c==KeyEvent.VK_DELETE)
	    {
	    	world.remove(Selected_Body);
	    	Selected_Body=null;
	    	
	    }
	    
		

	}
	
	
	private void generate_GUI() {
		Configurationpanel.removeAll();
		/*BodyList v = world.getBodies();
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
		/*	JXTaskPane jx = new JXTaskPane();
			jx.add(panel1);
			Configurationpanel.add(jx);
			
		}*/
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
			JXTaskPane task = new JXTaskPane();
			task.setAnimated(false);
			//task.setExpanded(false);
			task.setTitle(z.toString());
			task.add(panel1);
			Configurationpanel.add(task);
		}
		Configurationpanel.validate();
		Configurationpanel.doLayout();
		jscp.validate();
		jscp.doLayout();
		this.frame.validate();
		this.frame.doLayout();
		this.frame.repaint();
		
	}





	public MODE getMode() {
		return Mode;
	}





	public void setMode(MODE mode) {
		Mode = mode;
	}





	public OBJECT_TO_ADD getObject_to_add() {
		return Object_to_add;
	}





	public void setObject_to_add(OBJECT_TO_ADD object_to_add) {
		Object_to_add = object_to_add;
	}





	public JOINT_TO_ADD getJoint_To_Add() {
		return Joint_To_Add;
	}
	




	public void setJoint_To_Add(JOINT_TO_ADD joint_To_Add) {
		Joint_To_Add = joint_To_Add;
	}
}
