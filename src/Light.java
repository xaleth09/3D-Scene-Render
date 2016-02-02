
public interface Light {
	public ColorType applyLight(Material mat, Vector3D v, Vector3D n, Point2D p);
	public ColorType applyLight(Material mat, Vector3D v, Vector3D n);
}
