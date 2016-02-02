//****************************************************************************
//       Example Main Program for CS480 PA1
//****************************************************************************
// Description: 
//   
//   This is a template program for the sketching tool.  
//
//     LEFTMOUSE: rotate camera view 
//
//     The following keys control the program:
//
//		Q,q: quit 
//		C,c: clear polygon (set vertex count=0)
//		R,r: randomly change the color
//		S,s: toggle the smooth shading for triangle 
//			 (no smooth shading by default)
//		T,t: show testing examples
//		>:	 increase the step number for examples
//		<:   decrease the step number for examples
//
//****************************************************************************
// History :
//   Aug 2004 Created by Jianming Zhang based on the C
//   code by Stan Sclaroff
//  Nov 2014 modified to include test cases for shading example for PA4
//  Nov 2015 modified to add my rendered scenes using gouraud and flat shading,
//				a general scene rendering method, and edited keyboard controls


import javax.swing.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.*; 
import java.awt.image.*;
//import java.io.File;
//import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

//import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;//for new version of gl
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;

import com.jogamp.opengl.util.FPSAnimator;//for new version of gl


public class lab11 extends JFrame
	implements GLEventListener, KeyListener, MouseListener, MouseMotionListener
{
	
	private static final long serialVersionUID = 1L;
	private final int DEFAULT_WINDOW_WIDTH=512;
	private final int DEFAULT_WINDOW_HEIGHT=512;
	private final float DEFAULT_LINE_WIDTH=1.0f;

	private GLCapabilities capabilities;
	private GLCanvas canvas;
	private FPSAnimator animator;

	final private int numMats;
	private int matUsed;
	final private int numTestCase;
	private int testCase;
	private BufferedImage buff;
	@SuppressWarnings("unused")
	private ColorType color;
	private Random rng;
	
	 // specular exponent for materials
	private int ns=5; 
	
	private ArrayList<Point2D> lineSegs;
	private ArrayList<Point2D> triangles;
	private boolean doSmoothShading;
	private boolean doAmb;
	private boolean doDiff;
	private boolean doSpec;
	private boolean lightSwitch;
	private boolean switches[];
	private int Nsteps;
	float radius;

	/** The quaternion which controls the rotation of the world. */
    private Quaternion viewing_quaternion = new Quaternion();
    private Vector3D viewing_center = new Vector3D((float)(DEFAULT_WINDOW_WIDTH/2),(float)(DEFAULT_WINDOW_HEIGHT/2),(float)0.0);
    /** The last x and y coordinates of the mouse press. */
    private int last_x = 0, last_y = 0;
    /** Whether the world is being rotated. */
    private boolean rotate_world = false;
    
	public lab11()
	{
	    capabilities = new GLCapabilities(null);
	    capabilities.setDoubleBuffered(true);  // Enable Double buffering

	    canvas  = new GLCanvas(capabilities);
	    canvas.addGLEventListener(this);
	    canvas.addMouseListener(this);
	    canvas.addMouseMotionListener(this);
	    canvas.addKeyListener(this);
	    canvas.setAutoSwapBufferMode(true); // true by default. Just to be explicit
	    canvas.setFocusable(true);
	    getContentPane().add(canvas);

	    animator = new FPSAnimator(canvas, 60); // drive the display loop @ 60 FPS

	    numMats = 4;
	    matUsed = 0;
	    numTestCase = 3;
	    testCase = 0;
	    Nsteps = 12;
	    radius = 50f;

	    setTitle("CS480/680 Lab 11");
	    setSize( DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setVisible(true);
	    setResizable(false);
	    
	    rng = new Random();
	    color = new ColorType(1.0f,0.0f,0.0f);
	    lineSegs = new ArrayList<Point2D>();
	    triangles = new ArrayList<Point2D>();
	    doSmoothShading = false;
	    doAmb = true;
	    doDiff = true;
	    doSpec = true;
	    lightSwitch = false;
	    switches = new boolean [4];
	    switches[0] = switches[1] = switches[2] = switches[3] = true;
	}

	public void run()
	{
		animator.start();
	}

	public static void main( String[] args )
	{
	    lab11 P = new lab11();
	    P.run();
	}

	//*********************************************** 
	//  GLEventListener Interfaces
	//*********************************************** 
	public void init( GLAutoDrawable drawable) 
	{
	    GL gl = drawable.getGL();
	    gl.glClearColor( 0.0f, 0.0f, 0.0f, 0.0f);
	    gl.glLineWidth( DEFAULT_LINE_WIDTH );
	    Dimension sz = this.getContentPane().getSize();
	    buff = new BufferedImage(sz.width,sz.height,BufferedImage.TYPE_3BYTE_BGR);
	    clearPixelBuffer();
	}

	// Redisplaying graphics
	public void display(GLAutoDrawable drawable)
	{
	    GL2 gl = drawable.getGL().getGL2();
	    WritableRaster wr = buff.getRaster();
	    DataBufferByte dbb = (DataBufferByte) wr.getDataBuffer();
	    byte[] data = dbb.getData();

	    gl.glPixelStorei(GL2.GL_UNPACK_ALIGNMENT, 1);
	    gl.glDrawPixels (buff.getWidth(), buff.getHeight(),
                GL2.GL_BGR, GL2.GL_UNSIGNED_BYTE,
                ByteBuffer.wrap(data));
        drawTestCase();
	}

	// Window size change
	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
	{
		// deliberately left blank
	}
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
	      boolean deviceChanged)
	{
		// deliberately left blank
	}
	
	void clearPixelBuffer()
	{
		lineSegs.clear();
    	triangles.clear();
		Graphics2D g = buff.createGraphics();
	    g.setColor(Color.BLACK);
	    g.fillRect(0, 0, buff.getWidth(), buff.getHeight());
	    g.dispose();
	}
	
	// drawTest
	void drawTestCase()
	{  
		/* clear the window and vertex state */
		clearPixelBuffer();
	  
		//System.out.printf("Test case = %d\n",testCase);

		switch (testCase){
		case 0:
			sceneOne(doSmoothShading, doAmb, doDiff, doSpec); /* Box and Torus, Ambient and Infinite Lighting */
			break;
		case 1:
			sceneTwo(doSmoothShading, doAmb, doDiff, doSpec); /* Top, Torus, Sphere, Ambient and Infinite Lighting */
			break;
		case 2:
			sceneThree(doSmoothShading, doAmb, doDiff, doSpec);
			break;
		}	
	}


	//*********************************************** 
	//          KeyListener Interfaces
	//*********************************************** 
	public void keyTyped(KeyEvent key)
	{
	//      Q,q: quit 
	//      C,c: clear polygon (set vertex count=0)
	//		R,r: randomly change the color
	//		S,s: toggle the smooth shading
	//		T,t: show testing examples (toggles between smooth shading and flat shading test cases)
	//		>:	 increase the step number for examples
	//		<:   decrease the step number for examples
	//     +,-:  increase or decrease spectral exponent

	    switch ( key.getKeyChar() ) 
	    {
	    case 'Q' :
	    case 'q' : 
	    	new Thread()
	    	{
	          	public void run() { animator.stop(); }
	        }.start();
	        System.exit(0);
	        break;
	    case 'R' :
	    	radius+=5;
	    	break;
	    case 'r' :
	    	if(radius != 0f){
	    		radius-=5;
	    	}
	    	break;
	    case '1' :
	    	if(lightSwitch){
	    		
	    	}
	    	break;
	    case '2' :
	    	if(lightSwitch){
	    		
	    	}
	    	break;
	    case '3' :
	    	if(lightSwitch){
	    		
	    	}
	    	break;
	    case '4' :
	    	if(lightSwitch){
	    		
	    	}
	    	break;
	    case 'L' :
	    case 'l' :
	    	lightSwitch = !lightSwitch;
	    	break;
	    case 'C' :
	    case 'c' :
	    	clearPixelBuffer();
	    	break;
	    case 'M' :
	    case 'm' :
	    	matUsed = (matUsed+1)%numMats;
	    	break;
	    case 'G' :
	    case 'g' :
	    	doSmoothShading = true; // This is a placeholder (implemented in 'T')
	    	break;
	    case 'F' :
	    case 'f' :
	    	doSmoothShading = false; // This is a placeholder (implemented in 'T')
	    	break;
	    case 'A' :
	    case 'a' :
	    	doAmb = !doAmb; // This is a placeholder (implemented in 'T')
	    	break;
	    case 'S' :
	    case 's' :
	    	doSpec = !doSpec; // This is a placeholder (implemented in 'T')
	    	break;
	    case 'D' :
	    case 'd' :
	    	doDiff = !doDiff; // This is a placeholder (implemented in 'T')
	    	break;
	    case 'T' :
	    case 't' : 
	    	radius = 50;
	    	testCase = (testCase+1)%numTestCase;
	    	drawTestCase();
	        break; 
	    case '<':  
	        Nsteps = Nsteps < 4 ? Nsteps: Nsteps / 2;
	        System.out.printf( "Nsteps = %d \n", Nsteps);
	        drawTestCase();
	        break;
	    case '>':
	        Nsteps = Nsteps > 190 ? Nsteps: Nsteps * 2;
	        System.out.printf( "Nsteps = %d \n", Nsteps);
	        drawTestCase();
	        break;
	    case 'u':
	    case 'U':
	    	ns++;
	        drawTestCase();
	    	break;
	    case '-':
	    	if(ns>0)
	    		ns--;
	        drawTestCase();
	    	break;
	    default :
	        break;
	    }
	}

	public void keyPressed(KeyEvent key)
	{
	    switch (key.getKeyCode()) 
	    {
	    case KeyEvent.VK_ESCAPE:
	    	new Thread()
	        {
	    		public void run()
	    		{
	    			animator.stop();
	    		}
	        }.start();
	        System.exit(0);
	        break;
	      default:
	        break;
	    }
	}

	public void keyReleased(KeyEvent key)
	{
		// deliberately left blank
	}

	//************************************************** 
	// MouseListener and MouseMotionListener Interfaces
	//************************************************** 
	public void mouseClicked(MouseEvent mouse)
	{
		// deliberately left blank
	}
	  public void mousePressed(MouseEvent mouse)
	  {
	    int button = mouse.getButton();
	    if ( button == MouseEvent.BUTTON1 )
	    {
	      last_x = mouse.getX();
	      last_y = mouse.getY();
	      rotate_world = true;
	    }
	  }

	  public void mouseReleased(MouseEvent mouse)
	  {
	    int button = mouse.getButton();
	    if ( button == MouseEvent.BUTTON1 )
	    {
	      rotate_world = false;
	    }
	  }

	public void mouseMoved( MouseEvent mouse)
	{
		// Deliberately left blank
	}

	/**
	   * Updates the rotation quaternion as the mouse is dragged.
	   * 
	   * @param mouse
	   *          The mouse drag event object.
	   */
	  public void mouseDragged(final MouseEvent mouse) {
	    if (this.rotate_world) {
	      // get the current position of the mouse
	      final int x = mouse.getX();
	      final int y = mouse.getY();

	      // get the change in position from the previous one
	      final int dx = x - this.last_x;
	      final int dy = y - this.last_y;

	      // create a unit vector in the direction of the vector (dy, dx, 0)
	      final float magnitude = (float)Math.sqrt(dx * dx + dy * dy);
	      if(magnitude > 0.0001)
	      {
	    	  // define axis perpendicular to (dx,-dy,0)
	    	  // use -y because origin is in upper lefthand corner of the window
	    	  final float[] axis = new float[] { -(float) (dy / magnitude),
	    			  (float) (dx / magnitude), 0 };

	    	  // calculate appropriate quaternion
	    	  final float viewing_delta = 3.1415927f / 180.0f;
	    	  final float s = (float) Math.sin(0.5f * viewing_delta);
	    	  final float c = (float) Math.cos(0.5f * viewing_delta);
	    	  final Quaternion Q = new Quaternion(c, s * axis[0], s * axis[1], s
	    			  * axis[2]);
	    	  this.viewing_quaternion = Q.multiply(this.viewing_quaternion);

	    	  // normalize to counteract acccumulating round-off error
	    	  this.viewing_quaternion.normalize();

	    	  // save x, y as last x, y
	    	  this.last_x = x;
	    	  this.last_y = y;
	          drawTestCase();
	      }
	    }

	  }
	  
	public void mouseEntered( MouseEvent mouse)
	{
		// Deliberately left blank
	}

	public void mouseExited( MouseEvent mouse)
	{
		// Deliberately left blank
	} 


	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		
	}


	//Draw Object Meshes and fill them in by adding the light sources while also accounting
	//for their material
	void drawScene(boolean doSmooth, Vector3D view_vector, DepthBuffer dbuff, Mesh3D mesh, Material objmat, Light [] lights, int n, int m){
		
		// normal to the plane of a triangle
        // to be used in backface culling / backface rejection
        Vector3D triangle_normal = new Vector3D();
        
		int i, j;
		
		// temporary variables for triangle 3D vertices and 3D normals
		Vector3D v0,v1, v2, n0, n1, n2;
		
		// projected triangle, with vertex colors
		Point2D[] tri = {new Point2D(), new Point2D(), new Point2D()};
		
		
		ColorType mat1_ka = new ColorType(0.5f,0.5f,0.5f);
        ColorType mat1_kd = new ColorType(0.7f,0.7f,0.7f);
        ColorType mat1_ks = new ColorType(1.0f,1.0f,1.0f);
        
        ColorType mat2_ka = new ColorType(0.75f, 0.75f, 0.75f);
        ColorType mat2_kd = new ColorType(0.20f,  0.20f,  0.20f);
        ColorType mat2_ks = new ColorType(1.0f, 1.0f, 1.0f);

        ColorType mat3_ka = new ColorType(0.5f, 0.5f, 0.5f);
        ColorType mat3_kd = new ColorType(0.5f, 0.5f, 0.5f);
        ColorType mat3_ks = new ColorType(0.5f, 0.5f, 0.5f);

        ColorType mat4_ka = new ColorType(0.2f, 0.2f, 0.2f);
        ColorType mat4_kd = new ColorType(0.8f, 0.8f, 0.8f);
        ColorType mat4_ks = new ColorType(0.7f, 0.7f, 0.7f);
        
        Material[] mats = {new Material(mat1_ka, mat1_kd, mat1_ks, ns), new Material(mat2_ka, mat2_kd, mat2_ks, ns), new Material(mat3_ka, mat3_kd, mat3_ks, ns), new Material(mat4_ka, mat4_kd, mat4_ks, ns)};

		
		
		//Use M to cycle through defined materials
		Material mat = objmat;
		if(objmat.ambient){
			mat.ka = mat.ka.mult(mats[matUsed].ka);
		}
		if(objmat.diffuse){
			mat.kd = mat.kd.mult(mats[matUsed].kd);
		}
		if(objmat.specular){
			mat.ks = mat.ks.mult(mats[matUsed].ks);
		}
		
		// draw triangles for the current surface, using vertex colors
		// this works for Gouraud and flat shading only (not Phong)
		for(i=0; i < m-1; ++i)
	    {
			for(j=0; j < n-1; ++j)
			{
				for(int l = 0; l < 2; ++l){
					v0 = mesh.v[i][j];
					v1 = mesh.v[i][j+1];
					v2 = mesh.v[i+1][j+1];
					triangle_normal = computeTriangleNormal(v0,v1,v2);
					
					if(view_vector.dotProduct(triangle_normal) > 0.0)  // front-facing triangle?
					{	
						if(doSmooth)  
						{
							// vertex colors for Gouraud shading
							n0 = mesh.n[i][j];
							n1 = mesh.n[i][j+1];
							n2 = mesh.n[i+1][j+1];
							tri[0].c = lights[0].applyLight(mat, view_vector, n0,tri[0]);
							tri[1].c = lights[0].applyLight(mat, view_vector, n1,tri[1]);
							tri[2].c = lights[0].applyLight(mat, view_vector, n2,tri[2]);

							for(int ls = 1; ls < lights.length; ls++){
							 tri[0].c = tri[0].c.add(lights[ls].applyLight(mat, view_vector, n0,tri[0]));
							 tri[1].c = tri[1].c.add(lights[ls].applyLight(mat, view_vector, n1,tri[1]));
							 tri[2].c = tri[2].c.add(lights[ls].applyLight(mat, view_vector, n2,tri[2]));
							}
						}
						else 
						{
							// flat shading: use the normal to the triangle itself
							n2 = n1 = n0 =  triangle_normal;
							tri[0].c = lights[0].applyLight(mat, view_vector, triangle_normal,tri[0]);
							tri[1].c = lights[0].applyLight(mat, view_vector, triangle_normal,tri[1]);
							tri[2].c = lights[0].applyLight(mat, view_vector, triangle_normal,tri[2]);
							for(int ls = 1; ls < lights.length; ls++){
								tri[0].c = tri[0].c.add(lights[ls].applyLight(mat, view_vector, triangle_normal,tri[0]));
								tri[1].c = tri[1].c.add(lights[ls].applyLight(mat, view_vector, triangle_normal,tri[1]));
								tri[2].c = tri[2].c.add(lights[ls].applyLight(mat, view_vector, triangle_normal,tri[2]));
							}
							
						}

						tri[0].x = (int)v0.x;
						tri[0].y = (int)v0.y;
						tri[0].z = (int)v0.z;
						
						tri[1].x = (int)v1.x;
						tri[1].y = (int)v1.y;
						tri[1].z = (int)v1.z;
						
						tri[2].x = (int)v2.x;
						tri[2].y = (int)v2.y;
						tri[2].z = (int)v2.z;
						
						SketchBase.drawTriangle(buff, dbuff, tri[0],tri[1],tri[2],doSmooth);      
					}
					
					v0 = mesh.v[i][j];
					v1 = mesh.v[i+1][j+1];
					v2 = mesh.v[i+1][j];
					triangle_normal = computeTriangleNormal(v0,v1,v2);
					
					if(view_vector.dotProduct(triangle_normal) > 0.0)  // front-facing triangle?
					{	
						if(doSmooth)
						{
							// vertex colors for Gouraud shading
							n0 = mesh.n[i][j];
							n1 = mesh.n[i+1][j+1];
							n2 = mesh.n[i+1][j];
							tri[0].c = lights[0].applyLight(mat, view_vector, n0,tri[0]);
							tri[1].c = lights[0].applyLight(mat, view_vector, n1,tri[1]);
							tri[2].c = lights[0].applyLight(mat, view_vector, n2,tri[2]);
							for(int ls = 1; ls < lights.length; ls++){
								tri[0].c = tri[0].c.add(lights[ls].applyLight(mat, view_vector, n0,tri[0]));
								tri[1].c = tri[1].c.add(lights[ls].applyLight(mat, view_vector, n1,tri[1]));
								tri[2].c = tri[2].c.add(lights[ls].applyLight(mat, view_vector, n2,tri[2]));
							}
						}
						else 
						{
							// flat shading: use the normal to the triangle itself
							n2 = n1 = n0 =  triangle_normal;
							tri[0].c = lights[0].applyLight(mat, view_vector, triangle_normal,tri[0]);
							tri[1].c = lights[0].applyLight(mat, view_vector, triangle_normal,tri[1]);
							tri[2].c = lights[0].applyLight(mat, view_vector, triangle_normal,tri[2]);
							for(int ls = 1; ls < lights.length; ls++){
								tri[0].c = tri[0].c.add(lights[ls].applyLight(mat, view_vector, triangle_normal,tri[0]));
								tri[1].c = tri[1].c.add(lights[ls].applyLight(mat, view_vector, triangle_normal,tri[1]));
								tri[2].c = tri[2].c.add(lights[ls].applyLight(mat, view_vector, triangle_normal,tri[2]));
							}
						}	
			
						tri[0].x = (int)v0.x;
						tri[0].y = (int)v0.y;
						tri[0].z = (int)v0.z;
						
						tri[1].x = (int)v1.x;
						tri[1].y = (int)v1.y;
						tri[1].z = (int)v1.z;
						
						tri[2].x = (int)v2.x;
						tri[2].y = (int)v2.y;
						tri[2].z = (int)v2.z;
						
						SketchBase.drawTriangle(buff, dbuff, tri[0],tri[1],tri[2],doSmooth);      
					}
				}
			}
	    }
	}
	

	
	//************************************************** 
	// Test Cases
	// Nov 2015 Roberto Garza -- removed line and triangle test cases
	//************************************************** 

	void sceneOne(boolean doSmooth, boolean amb, boolean diff, boolean spec){
			
		// the simple example scene includes one sphere and one torus
        Torus3D torus = new Torus3D((float)156.0, (float)384.0, (float)256.0, (float)0.8*radius, (float)1.25*radius, Nsteps, Nsteps);
        SuperEllipsoid box = new SuperEllipsoid((float)128.0, (float)128.0, (float)128.0, (float)1.5*radius, (float)1.5*radius, (float)1.5*radius ,0.5f, 0.5f, Nsteps, Nsteps);
        SuperToroid superT = new SuperToroid((float)400.0, (float)256.0, (float)256.0, (float)0.5*radius, (float)0.5*radius, (float)0.5*radius, (float)0.05*radius,(float).25, (float)2.5, Nsteps, Nsteps);
        
		// view vector is defined along z axis
        // this example assumes simple othorgraphic projection
        // view vector is used in 
        //   (a) calculating specular lighting contribution
        //   (b) backface culling / backface rejection
        Vector3D view_vector = new Vector3D((float)0.0,(float)0.0,(float)1.0);
      
        // material properties for the sphere and torus
        // ambient, diffuse, and specular coefficients
        // specular exponent is a global variable
        ColorType torus_ka = new ColorType(0.0f,0.5f,0.9f);
        ColorType torus_kd = new ColorType(0.0f,0.5f,0.9f);
        ColorType torus_ks = new ColorType(1.0f,1.0f,1.0f);
        
        ColorType box_ka = new ColorType(0.2f, 0.8f, 0.2f);
        ColorType box_kd = new ColorType(0.2f, 0.8f, 0.2f);
        ColorType box_ks = new ColorType(1.0f,1.0f,1.0f);

        ColorType superT_ka = new ColorType(.8f, .8f, 0f);
        ColorType superT_kd = new ColorType(.8f, .8f, 0f);
        ColorType superT_ks = new ColorType(1.0f,1.0f,1.0f);

        
        Material[] mats = {new Material(box_ka, box_kd, box_ks, ns), new Material(torus_ka, torus_kd, torus_ks, ns), new Material(superT_ka, superT_kd, superT_ks, ns)};
        for(int i = 0; i < mats.length; i++){
        	mats[i].ambient = amb;
        	mats[i].diffuse = diff;
        	mats[i].specular = spec;
        }
        
        // define one infinite light source, with color = white
        ColorType light_color = new ColorType(1f,1f,1f);
        Point2D light_pos = new Point2D(500,700, 700f, light_color);
        Vector3D light_direction = new Vector3D((float)0.0,(float)(-1.0/Math.sqrt(2.0)),(float)(1.0/Math.sqrt(2.0)));
        
        ColorType inf_color = new ColorType(.5f,.5f,.5f);
        Vector3D inf_direction = new Vector3D((float)0.0,(float)(-1.0/Math.sqrt(2.0)),(float)(1.0/Math.sqrt(2.0)));
        
        InfiniteLight Inflight = new InfiniteLight(inf_color,inf_direction);
        PointLight Plight = new PointLight(light_color,light_direction, light_pos);
        AmbientLight Amblight = new AmbientLight(new ColorType(.6f,.6f,.6f));
        if(!doAmb){
        	Amblight = new AmbientLight(new ColorType(0f,0f,0f));
        }
        
        Light [] lights = {Plight, Amblight, Inflight};
        
       //DepthBuffer initialized to size of FrameBuffer
       //to implement 3D Viewing
       DepthBuffer dbuff = new DepthBuffer(buff);
        
       int n, m;
        
		for(int k=0;k<3;++k) // loop twice: shade sphere, then torus
		{
			if(k == 0)
			{
				n=box.get_n();
				m=box.get_m();
				// rotate the surface's 3D mesh using quaternion
				box.mesh.rotateMesh(viewing_quaternion, viewing_center);
				drawScene(doSmooth, view_vector, dbuff, box.mesh, mats[0], lights, n, m);
			}
			else if(k == 1)
			{
				n=torus.get_n();
				m=torus.get_m();
				// rotate the surface's 3D mesh using quaternion
				torus.mesh.rotateMesh(viewing_quaternion, viewing_center);
				drawScene(doSmooth, view_vector, dbuff, torus.mesh, mats[1], lights, n, m);
			}else{
				n=superT.get_n();
				m=superT.get_m();
				// rotate the surface's 3D mesh using quaternion
				superT.mesh.rotateMesh(viewing_quaternion, viewing_center);
				drawScene(doSmooth, view_vector, dbuff, superT.mesh, mats[2], lights, n, m);
			}
		}
	}
	
	
	void sceneTwo(boolean doSmooth, boolean amb, boolean diff, boolean spec){
		// the simple example scene includes one sphere and one torus
        Sphere3D sphere = new Sphere3D((float)400.0, (float)128.0, (float)58.0, (float)1*radius, Nsteps, Nsteps);
        Torus3D torus = new Torus3D((float)256.0, (float)384.0, (float)256.0, (float)0.2*radius, (float)1.75*radius, Nsteps, Nsteps);
        SuperEllipsoid top = new SuperEllipsoid((float)128.0, (float)128.0, (float)128.0, (float)1.5*radius, (float)1.5*radius, (float)1.5*radius ,2f, 1f, Nsteps, Nsteps);
       
		// view vector is defined along z axis
        // this example assumes simple othorgraphic projection
        // view vector is used in 
        //   (a) calculating specular lighting contribution
        //   (b) backface culling / backface rejection
        Vector3D view_vector = new Vector3D((float)0.0,(float)0.0,(float)1.0);
      
        // material properties for the sphere and torus
        // ambient, diffuse, and specular coefficients
        // specular exponent is a global variable
        ColorType torus_ka = new ColorType(0.0f,0.5f,0.9f);
        ColorType sphere_ka = new ColorType(0.9f,0.3f,0.1f);
        ColorType top_ka = new ColorType(0.2f, 0.8f, 0.2f);
        ColorType torus_kd = new ColorType(0.0f,0.5f,0.9f);
        ColorType sphere_kd = new ColorType(0.9f,0.3f,0.1f);
        ColorType top_kd = new ColorType(0.2f, 0.8f, 0.2f);
        ColorType torus_ks = new ColorType(1.0f,1.0f,1.0f);
        ColorType sphere_ks = new ColorType(.0f,.0f,.0f);
        ColorType top_ks = new ColorType(1.0f,1.0f,1.0f);

        Material[] mats = {new Material(top_ka, top_kd, top_ks, ns), new Material(torus_ka, torus_kd, torus_ks, ns), new Material(sphere_ka, sphere_kd, sphere_ks, ns)};
        for(int i = 0; i < mats.length; i++){
        	mats[i].ambient = amb;
        	mats[i].diffuse = diff;
        	mats[i].specular = spec;
        }
        
        // define one infinite light source, with color = white
        ColorType light_color = new ColorType(.7f,.7f,.7f);
        Vector3D light_direction = new Vector3D((float)0.0,(float)(-1.0/Math.sqrt(2.0)),(float)(1.0/Math.sqrt(2.0)));
        InfiniteLight Inflight = new InfiniteLight(light_color,light_direction);
    	AmbientLight Amblight = new AmbientLight(new ColorType(.2f,.2f,.2f));
        if(!doAmb){
        	Amblight = new AmbientLight(new ColorType(0f,0f,0f));
        }
        Light [] lights = {Inflight, Amblight};
        
       //DepthBuffer initialized to size of FrameBuffer
       //to implement 3D Viewing
       DepthBuffer dbuff = new DepthBuffer(buff);
        
       int n, m;
        
		for(int k=0;k<3;++k) // loop twice: shade sphere, then torus
		{
			if(k==0)
			{
				n=top.get_n();
				m=top.get_m();
				// rotate the surface's 3D mesh using quaternion
				top.mesh.rotateMesh(viewing_quaternion, viewing_center);
				drawScene(doSmooth, view_vector, dbuff, top.mesh, mats[0], lights, n, m);
			}
			else if(k == 1)
			{
				n=torus.get_n();
				m=torus.get_m();
				// rotate the surface's 3D mesh using quaternion
				torus.mesh.rotateMesh(viewing_quaternion, viewing_center);
				drawScene(doSmooth, view_vector, dbuff, torus.mesh, mats[1], lights, n, m);
			}else{
				n=sphere.get_n();
				m=sphere.get_m();
				sphere.mesh.rotateMesh(viewing_quaternion, viewing_center);
				drawScene(doSmooth, view_vector, dbuff, sphere.mesh, mats[2], lights, n, m);
			}
		}
	}
	
	
	void sceneThree(boolean doSmooth, boolean amb, boolean diff, boolean spec){
		// the simple example scene includes one sphere and one torus
        Cylinder cylinder = new Cylinder((float)400.0, (float)128.0, (float)58.0, (float)1.5*radius, (float)50, Nsteps, Nsteps);
        Ellipsoid3D ellipsoid = new Ellipsoid3D((float)256.0, (float)384.0, (float)256.0, (float)0.8*radius, (float)1.25*radius, (float)1.75*radius, Nsteps, Nsteps);
        SuperEllipsoid top = new SuperEllipsoid((float)128.0, (float)128.0, (float)128.0, (float)1.5*radius, (float)1.5*radius, (float)1.5*radius ,.1f, 2f, Nsteps, Nsteps);
       
		// view vector is defined along z axis
        // this example assumes simple othorgraphic projection
        // view vector is used in 
        //   (a) calculating specular lighting contribution
        //   (b) backface culling / backface rejection
        Vector3D view_vector = new Vector3D((float)0.0,(float)0.0,(float)1.0);
      
        // material properties for the sphere and torus
        // ambient, diffuse, and specular coefficients
        // specular exponent is a global variable
        ColorType ellipsoid_ka = new ColorType(0.0f,0.5f,0.9f);
        ColorType cylinder_ka = new ColorType(0.9f,0.3f,0.1f);
        ColorType top_ka = new ColorType(0.2f, 0.8f, 0.2f);
        ColorType ellipsoid_kd = new ColorType(0.0f,0.5f,0.9f);
        ColorType cylinder_kd = new ColorType(0.9f,0.3f,0.1f);
        ColorType top_kd = new ColorType(0.2f, 0.8f, 0.2f);
        ColorType ellipsoid_ks = new ColorType(1.0f,1.0f,1.0f);
        ColorType cylinder_ks = new ColorType(1.0f,1.0f,1.0f);
        ColorType top_ks = new ColorType(1.0f,1.0f,1.0f);

        Material[] mats = {new Material(top_ka, top_kd, top_ks, ns), new Material(ellipsoid_ka, ellipsoid_kd, ellipsoid_ks, ns), new Material(cylinder_ka, cylinder_kd, cylinder_ks, ns)};
        for(int i = 0; i < mats.length; i++){
        	mats[i].ambient = amb;
        	mats[i].diffuse = diff;
        	mats[i].specular = spec;
        }
        // define one infinite light source, with color = white
        ColorType light_color = new ColorType(.7f,.7f,.7f);
        Point2D light_pos = new Point2D(0,500,450f,new ColorType());
        Vector3D light_direction = new Vector3D((float)0.0,(float)(-1.0/Math.sqrt(2.0)),(float)(1.0/Math.sqrt(2.0)));
        PointLight plight = new PointLight(light_color,light_direction, light_pos);
        AmbientLight Amblight = new AmbientLight(new ColorType(.2f,.2f,.2f));
        if(!doAmb){
        	Amblight = new AmbientLight(new ColorType(0f,0f,0f));
        }
        
        Light [] lights = {plight, Amblight};
        
       //DepthBuffer initialized to size of FrameBuffer
       //to implement 3D Viewing
       DepthBuffer dbuff = new DepthBuffer(buff);
        
       int n, m;
        
		for(int k=0;k<3;++k) // loop twice: shade sphere, then torus
		{
			if(k==0)
			{
				n=top.get_n();
				m=top.get_m();
				// rotate the surface's 3D mesh using quaternion
				top.mesh.rotateMesh(viewing_quaternion, viewing_center);
				drawScene(doSmooth, view_vector, dbuff, top.mesh, mats[0], lights, n, m);
			}
			else if(k == 1)
			{
				n=ellipsoid.get_n();
				m=ellipsoid.get_m();
				// rotate the surface's 3D mesh using quaternion
				ellipsoid.mesh.rotateMesh(viewing_quaternion, viewing_center);
				drawScene(doSmooth, view_vector, dbuff, ellipsoid.mesh, mats[1], lights, n, m);
			}else{
				n=cylinder.get_n();
				m=cylinder.get_m();
				cylinder.mesh.rotateMesh(viewing_quaternion, viewing_center);
				cylinder.meshTop.rotateMesh(viewing_quaternion, viewing_center);
				cylinder.meshBot.rotateMesh(viewing_quaternion, viewing_center);
				drawScene(doSmooth, view_vector, dbuff, cylinder.mesh, mats[2], lights, n, m);
				drawScene(doSmooth, view_vector, dbuff, cylinder.meshTop, mats[2], lights, n, m);
				drawScene(doSmooth, view_vector, dbuff, cylinder.meshBot, mats[2], lights, n, m);
			}
		}
	}
	
	
	// helper method that computes the unit normal to the plane of the triangle
	// degenerate triangles yield normal that is numerically zero
	private Vector3D computeTriangleNormal(Vector3D v0, Vector3D v1, Vector3D v2)
	{
		Vector3D e0 = v1.minus(v2);
		Vector3D e1 = v0.minus(v2);
		Vector3D norm = e0.crossProduct(e1);
		
		if(norm.magnitude()>0.000001)
			norm.normalize();
		else 	// detect degenerate triangle and set its normal to zero
			norm.set((float)0.0,(float)0.0,(float)0.0);

		return norm;
	}

}