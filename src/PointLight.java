//****************************************************************************
//       Point light source class
//****************************************************************************
// History :
//   Nov 2015 Roberto Garza
//
public class PointLight implements Light
{
	public Point2D position;
	public Vector3D direction;
	public ColorType color;
	
	public PointLight(ColorType _c, Vector3D _direction, Point2D _pos)
	{
		color = new ColorType(_c);
		direction = new Vector3D(_direction);
		position = new Point2D(_pos);
	}
	
	// apply this light source to the vertex / normal, given material
	// return resulting color value
	public ColorType applyLight(Material mat, Vector3D v, Vector3D n, Point2D p){
		ColorType res = new ColorType();
		
		// dot product between light direction and normal
		// light must be facing in the positive direction
		// dot <= 0.0 implies this light is facing away (not toward) this point
		// therefore, light only contributes if dot > 0.0
		
		Vector3D dir = new Vector3D();
		
		dir.x = position.x - p.x;
		dir.y = position.y - p.y;
		dir.z = position.z - p.z;
		
		dir.x = (float)dir.x/(float)dir.magnitude();
		dir.y = (float)dir.y/(float)dir.magnitude();
		dir.z = (float)dir.z/(float)dir.magnitude();
		
		float xsqrd = (float)Math.pow(position.x-p.x,2);
		float ysqrd = (float)Math.pow(position.y-p.y, 2);
		float zsqrd = (float)Math.pow(position.z-p.z, 2);
		float distance = (float)Math.sqrt(xsqrd + ysqrd + zsqrd);
		
		//float frad = (float)((float)1/(float)(1000+20*dir.magnitude()*20*Math.pow(dir.magnitude(),2)));
		float frad = (float)((float)1/(float)(1+(2/(float)400)*distance*((float)1/(Math.pow(400, 2)))*Math.pow(distance,2)));
		//System.out.println(frad+" "+distance);
		frad = 1f;

		double dot = dir.dotProduct(n);
		if(dot>0.0)
		{
			// diffuse component
			if(mat.diffuse)
			{
				res.r = (float)(dot*mat.kd.r*color.r);
				res.g = (float)(dot*mat.kd.g*color.g);
				res.b = (float)(dot*mat.kd.b*color.b);
			}
			// specular component
			if(mat.specular)
			{
				Vector3D r = dir.reflect(n);
				dot = r.dotProduct(v);
				if(dot>0.0)
				{
					res.r += (float)Math.pow((dot*mat.ks.r*color.r),mat.ns);
					res.g += (float)Math.pow((dot*mat.ks.g*color.g),mat.ns);
					res.b += (float)Math.pow((dot*mat.ks.b*color.b),mat.ns);
				}
			}
			//System.out.println("FIRST: "+res.r +" "+res.g+" "+res.b);
			//res.r = (float)res.r*frad;
			//res.g = (float)res.g*frad;
			//res.b = (float)res.b*frad;
			//System.out.println("SEC: "+res.r +" "+res.g+" "+res.b+"\n");
			// clamp so that allowable maximum illumination level is not exceeded
			res.r = (float) Math.min(1.0, res.r);
			res.g = (float) Math.min(1.0, res.g);
			res.b = (float) Math.min(1.0, res.b);
		}
		return(res);
	}
	
	public ColorType applyLight(Material mat, Vector3D v, Vector3D n){return new ColorType(0,0,0);}
}
