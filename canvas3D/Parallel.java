package canvas3D;

import javafx.scene.paint.*;
import java.util.*;

public class Parallel extends Face{
	
	/**
	 * pを始点とし、ベクトルv1,v2で張られる平行四辺形
	 */
	public Parallel(Paint paint, Point p, Point v1, Point v2){
		path = new ArrayList<Point>();
		path.add(p); path.add(p.plus(v1));
		path.add(p.plus(v1).plus(v2)); path.add(p.plus(v2));
		this.paint = paint;
	}

	public Parallel(Point p, Point v1, Point v2){
		path = new ArrayList<Point>();
		path.add(p); path.add(p.plus(v1));
		path.add(p.plus(v1).plus(v2)); path.add(p.plus(v2));
		this.paint = Color.WHITE;
	}

	public Parallel(Paint paint, Point... points){
		super(paint, points);
	}
}
