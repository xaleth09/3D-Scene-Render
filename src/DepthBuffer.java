
//****************************************************************************
//      DepthBuffer Class
//****************************************************************************
// History :
//   Created by Roberto Garza
//


import java.awt.image.BufferedImage;;

public class DepthBuffer {
	Float DB [][];
	int r,c;
	float nINFINITY = Float.NEGATIVE_INFINITY;
	float pINFINITY = Float.POSITIVE_INFINITY;
	
	public DepthBuffer(BufferedImage buff){
		r = buff.getWidth();
		c = buff.getHeight();
		DB = new Float[r][c];
		for(int i = 0; i < buff.getWidth(); i++){
			for(int j = 0; j < buff.getHeight(); j++){
				DB[i][j] = nINFINITY;
			}
		}
	}
	
	public boolean checkDepth(Point2D p){

		
		
		if(p.z > DB[p.x][p.y]){
			//System.out.println(p.z+" > "+DB[p.x][p.y]+"\n");
			return true;
		}
		return false;
	}
	
	public void updateDepth(Point2D p){
		DB[p.x][p.y] = (float)p.z;
	}
	
}
