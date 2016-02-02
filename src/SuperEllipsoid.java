
//****************************************************************************
//      SuperEllipsoid class
//****************************************************************************
// History :
//   Nov 2015 Roberto Garza
//

public class SuperEllipsoid
{
	private Vector3D center;
	private float rx, ry, rz;
	private float e1, e2;
	private int m,n;
	public Mesh3D mesh;
	
	public SuperEllipsoid(float _x, float _y, float _z, float _rx, float _ry, float _rz, float _e1, float _e2, int _m, int _n)
	{
		center = new Vector3D(_x,_y,_z);
		rx = _rx;
		ry = _ry;
		rz = _rz;
		e1 = _e1;
		e2 = _e2;
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
	
	public void set_radius(float _rx, float _ry, float _rz)
	{
		rx = _rx;
		ry = _ry;
		rz = _rz;
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
		fillMesh();  // set the mesh vertices and normals
	}
		
	// fill the triangle mesh vertices and normals
	// using the current parameters for the sphere
	private void fillMesh()
	{
		int i,j;		
		float theta, phi;
		float d_theta=(float)(2.0*Math.PI)/ ((float)(m-1));
		float d_phi=(float)Math.PI / ((float)n-1);
		float c_theta,s_theta;
		float c_phi, s_phi;
		
		for(i=0,theta=-(float)Math.PI;i<m;++i,theta += d_theta)
	    {
			c_theta=(float)Math.cos(theta);
			s_theta=(float)Math.sin(theta);
			
			for(j=0,phi=(float)(-0.5*Math.PI);j<n;++j,phi += d_phi)
			{
				// vertex location
				c_phi = (float)Math.cos(phi);
				s_phi = (float)Math.sin(phi);
				mesh.v[i][j].x=(float) (center.x+rx * (Math.signum(c_phi)*Math.pow(Math.abs(c_phi),e1)) * (Math.signum(c_theta)*Math.pow(Math.abs(c_theta), e2)) );
				mesh.v[i][j].y=(float) (center.y+ry * (Math.signum(c_phi)*Math.pow(Math.abs(c_phi),e1)) * (Math.signum(s_theta)*Math.pow(Math.abs(s_theta), e2)) );
				mesh.v[i][j].z=(float) (center.z+rz * Math.signum(s_phi)*Math.pow(Math.abs(s_phi), e1));
				
				// unit normal to sphere at this vertex
				// unit normal to sphere at this vertex
				mesh.n[i][j].x = c_phi*c_theta;
				mesh.n[i][j].y = c_phi*s_theta;
				mesh.n[i][j].z=s_phi;
			}
	    }
		
		
	}
}