package canvas3D;

import java.util.*;

public class Parallelpiped extends Shape3D{

	public Parallelpiped(Face... fs){
		super(fs);
	}

	public Parallelpiped(Point p, Point v1, Point v2, Point v3){
		faces = new ArrayList<Face>();
		Point p1 = p;
		Point p2 = p1.plus(v1);
		Point p3 = p1.plus(v1).plus(v2);
		Point p4 = p1.plus(v2);
		Point p5 = p1.plus(v3);
		Point p6 = p1.plus(v3).plus(v1);
		Point p7 = p1.plus(v3).plus(v1).plus(v2);
		Point p8 = p1.plus(v3).plus(v2);
		faces.add(new Face(p1.copy(), p2.copy(),
					p3.copy(), p4.copy()));
		faces.add(new Face(p1.copy(), p2.copy(),
				       	p6.copy(), p5.copy()));
		faces.add(new Face(p1.copy(), p5.copy(),
				       	p8.copy(), p4.copy()));
		faces.add(new Face(p2.copy(), p6.copy(),
				       	p7.copy(), p3.copy()));
		faces.add(new Face(p4.copy(), p3.copy(), 
					p7.copy(), p8.copy()));
		faces.add(new Face(p5.copy(), p6.copy(), 
					p7.copy(), p8.copy()));
	}
}
