
//****************************************************************************
//    	Cylinder class
//****************************************************************************
// History :
//   Created by Roberto Garza
//

public class Cylinder
{
	private Vector3D center;
	private float r,h;
	private int m,n;
	public Mesh3D mesh;
	public Mesh3D meshTop;
	public Mesh3D meshBot;
	
	public Cylinder(float _x, float _y, float _z, float _r, float _h, int _m, int _n)
	{
		center = new Vector3D(_x,_y,_z);
		r = _r;
		h = _h;
		m = _m;
		n = _n;
		initMesh();
	}
	
	public void set_center(float _x, float _y, float _z)
	{
		center.x=_x;
		center.y=_y;
		center.z=_z;
		fillMesh();  // update the triangle mesh
	}
	
	public void set_radius(float _r)
	{
		r = _r;
		fillMesh(); // update the triangle mesh
	}
	
	public void set_height(float _h)
	{
		h = _h;
		fillMesh(); // update the triangle mesh
	}
	
	public void set_m(int _m)
	{
		m = _m;
		initMesh(); // resized the mesh, must re-initialize
	}
	
	public void set_n(int _n)
	{
		n = _n;
		initMesh(); // resized the mesh, must re-initialize
	}
	
	public int get_n()
	{
		return n;
	}
	
	public int get_m()
	{
		return m;
	}

	private void initMesh()
	{
		mesh = new Mesh3D(m,n);
		meshTop = new Mesh3D(m,n);
		meshBot = new Mesh3D(m,n);
		fillMesh();  // set the mesh vertices and normals
	}
		
	// fill the triangle mesh vertices and normals
	// using the current parameters for the sphere
	private void fillMesh()
	{
		int i,j;
		float zs;
		float theta, phi;
		float d_theta=(float)(2.0*Math.PI)/ ((float)(m-1));
		float d_phi=(float)Math.PI / ((float)n-1);
		float c_theta,s_theta;
		float c_phi, s_phi;
		
		float umin = (float)center.z - (h/(float)2);
		float umax = (float)center.z + (h/(float)2);
		float zInc = (umax-umin)/(float)(n-1);
		
		for(j=0, zs = umin; j<m; ++j, zs+=zInc )
		{
		  for(i=0,theta=-(float)Math.PI;i<n;++i,theta += d_theta){
			c_theta=(float)Math.cos(theta);
			s_theta=(float)Math.sin(theta);
			
				// vertex location
				mesh.v[i][j].x=center.x+r*c_theta;
				mesh.v[i][j].y=center.y+r*s_theta;
				mesh.v[i][j].z=zs;
				
				// unit normal to sphere at this vertex
				mesh.n[i][j].x = 0;
				mesh.n[i][j].y = mesh.v[i][j].y - center.y;
				mesh.n[i][j].z = zs;
		  }
	    }
		
		for(i=0,theta=-(float)Math.PI;i<m;++i,theta += d_theta)
	    {
			c_theta=(float)Math.cos(theta);
			s_theta=(float)Math.sin(theta);
			
			for(j=0,phi=(float)(-0.5*Math.PI);j<n;++j,phi += d_phi)
			{
				// vertex location
				c_phi = (float)Math.cos(phi);
				s_phi = (float)Math.sin(phi);
				meshTop.v[i][j].x =(float)((float)center.x + r * c_phi*c_theta);
				meshTop.v[i][j].y =(float)((float)center.y + r * c_phi*s_theta);
				meshTop.v[i][j].z = umax+1;
				
				meshBot.v[i][j].x =(float)((float)center.x + r * c_phi*c_theta);
				meshBot.v[i][j].y =(float)((float)center.y + r * c_phi*s_theta);
				meshBot.v[i][j].z = umin-1;
				
				// unit normal to sphere at this vertex
				// unit normal to sphere at this vertex
				meshBot.n[i][j].x = 0;
				meshBot.n[i][j].y = 0;
				meshBot.n[i][j].z = -1;
			}
	    }
		
	}
}