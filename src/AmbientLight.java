//****************************************************************************
//       Infinite light source class
//****************************************************************************
// History :
//   Created by Roberto Garza
//
public class AmbientLight implements Light
{
	public ColorType Ia;
	public ColorType color;
	
	public AmbientLight(ColorType _Ia)
	{
		Ia = new ColorType(_Ia);
	}
	
	// apply this light source to the vertex / normal, given material
	// return resulting color value
	public ColorType applyLight(Material mat, Vector3D v, Vector3D n){
		ColorType res = new ColorType();
		
		
			res.r = (float)(mat.ka.r*Ia.r); //Iar
			res.g = (float)(mat.ka.g*Ia.g); //Iag
			res.b = (float)(mat.ka.b*Ia.b); //Iab

			// clamp so that allowable maximum illumination level is not exceeded
			res.r = (float) Math.min(1.0, res.r);
			res.g = (float) Math.min(1.0, res.g);
			res.b = (float) Math.min(1.0, res.b);

		return(res);
	}
	
	public ColorType applyLight(Material mat, Vector3D v, Vector3D n, Point2D p){return applyLight(mat, v, n);}
}
