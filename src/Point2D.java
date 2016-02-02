//****************************************************************************
//       2D Point Class from PA1
//****************************************************************************
// History :
//   Nov 6, 2014 Created by Stan Sclaroff
//

public class Point2D
{
	public int x, y;
	float z;
	public float u, v; // uv coordinates for texture mapping
	public ColorType c;
	public Point2D(int _x, int _y, float _z, ColorType _c)
	{
		u = 0;
		v = 0;
		x = _x;
		y = _y;
		z = _z;
		c = _c;
	}
	public Point2D(int _x, int _y, float _z, ColorType _c, float _u, float _v)
	{
		u = _u;
		v = _v;
		x = _x;
		y = _y;
		z = _z;
		c = _c;
	}
	public Point2D()
	{
		c = new ColorType(1.0f, 1.0f, 1.0f);
	}
	public Point2D( Point2D p)
	{
		u = p.u;
		v = p.v;
		x = p.x;
		y = p.y;
		z = p.z;
		c = new ColorType(p.c.r, p.c.g, p.c.b);
	}
}