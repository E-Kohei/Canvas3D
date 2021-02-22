package canvas3D;

import javafx.scene.canvas.*;
import java.util.*;

public class Shape3D implements Cloneable{
	protected List<Face> faces;
	// Shape3Dは次の３つのうちいづれかに描画される
	protected CanvasStage3D canvasStage; // この図形が描かれるCanvasStage
	protected Canvas3D canvas;           // この図形が描かれるCanvas
	protected Pane3D pane;               // この図形が描かれるPane

	public Shape3D(Face... fs){
		faces = new ArrayList<Face>();
		for (Face f : fs)
			faces.add(f);
	}

	@Override
	public String toString(){
		return faces.toString();
	}

	@Override
	public boolean equals(Object obj){
		if (!(obj instanceof Shape3D))
			return false;
		if (faces.size() != ((Shape3D)obj).getFaces().size())
			return false;
		Set<Face> faceSet1 = new HashSet<Face>(faces);
		Set<Face> faceSet2 = new HashSet<Face>(((Shape3D)obj).getFaces());
		if (faceSet1.equals(faceSet2))
			return true;
		else
			return false;
	}

	@Override
	public int hashCode(){
		return faces.stream()
		.mapToInt(f -> f.hashCode())
		.reduce(0, (a,b) -> a+b);
	}

	@Override
	public Object clone(){
		Face[] CopyFaces = new Face[faces.size()];
		for (int i = 0; i < faces.size(); i++){
			CopyFaces[i] = faces.get(i).copy();
		}
		return new Shape3D(CopyFaces);
	}

	/**
	 * 立体を構成するFaceのListを返す
	 */
	public List<Face> getFaces(){
		return this.faces;
	}

	/**
	 * 立体を構成するFaceのListのコピーを返す
	 */
	public List<Face> getFacesCopy(){
		List<Face> CopyFaces = new ArrayList<>();
		for (Face f : this.faces){
			CopyFaces.add(f.copy());
		}
		return CopyFaces;
	}

	public Shape3D copy(){
		return (Shape3D) this.clone();
	}

	public boolean isSameShape(Shape3D shape){
		List<Face> faces2 = shape.getFaces();
		if (this.faces.size() != faces2.size())
			return false;
		for (int i = 0; i < faces.size(); i++)
			if (!(faces.get(i).isSamePoly(faces2.get(i))))
				return false;
		return true;
	}

	/**
	 * CanvasStageをセット
	 */
	public void setCanvasStage(CanvasStage3D canvasStage){
		this.canvasStage = canvasStage;
	}

	/**
	 * Canvasをセット
	 */
	public void setCanvas(Canvas3D canvas){
		this.canvas = canvas;
	}

	/**
	 * Paneをセット
	 */
	public void setPane(Pane3D pane){
		this.pane = pane;
	}
	
	/**
	 * CanvasStageをリセット
	 */
	public void resetCanvasStage(){
		this.canvasStage = null;
	}

	/**
	 * Canvasをリセット
	 */
	public void resetCanvas(){
		this.canvas = null;
	}

	/**
	 * Paneをリセット
	 */
	public void resetPane(){
		this.pane = null;
	}

	/**
	 * 同じ立体であれば、その立体の位置に変換する
	 */
	public void transformTo(Shape3D shape){
		List<Face> faces2 = shape.getFaces();
		if (this.isSameShape(shape)){
			for (int i = 0; i < faces.size(); i++){
				faces.get(i).transformTo(faces2.get(i));
			}
		}
	}

	/**
	 * (dx, dy, dz)方向の平行移動
	 */
	public void parallelMove(double dx, double dy, double dz){
		for (Face f : faces)
			f.parallelMove(dx, dy, dz);
	}

	/**
	 * ベクトルv方向の平行移動
	 */
	public void parallelMove(Point v){
		for (Face f : faces)
			f.parallelMove(v);
	}

	/**
	 * x軸回りのtheta回転
	 */
	public void rotateX(double theta){
		for (Face f : faces)
			f.rotateX(theta);
	}
	/**
	 * y軸回りのtheta回転
	 */
	public void rotateY(double theta){
		for (Face f : faces)
			f.rotateY(theta);
	}
	/**
	 * z軸回りのtheta回転
	 */
	public void rotateZ(double theta){
		for (Face f : faces)
			f.rotateZ(theta);
	}

	/**
	 * pを通りvに平行な直線を軸とするtheta回転
	 */
	public void rotate(double theta, Point p, Point v){
		for (Face f : faces)
			f.rotate(theta, p, v);
	}

	/**
	 * 立体の中心を返す。一般には全点の算術平均を返す
	 */
	public Point getCenter(){
		double x_sum = 0, y_sum = 0, z_sum = 0;
		Set<Point> ps = new HashSet<Point>();
		for (Face face : faces){
			for (Point p : face.getPath()){
				ps.add(p);
			}
		}
		int len = ps.size();
		for (Point p : ps){
			x_sum += p.getX();
			y_sum += p.getY();
			z_sum += p.getZ();
		}
		return new Point(x_sum/len, y_sum/len, z_sum/len);
	}

	/**
	 * 立体を平行移動して描き直す
	 */
	public void moveDraw(double dx, double dy, double dz){
		if (canvasStage != null){
			parallelMove(dx, dy, dz);
			canvasStage.remove(this);
			canvasStage.add(this);
		}
		if (canvas != null){
			parallelMove(dx, dy, dz);
			canvas.remove(this);
			canvas.add(this);
		}
		if (pane != null){
			parallelMove(dx, dy, dz);
			pane.drawShapes();
		}
	}

	/**
	 * 立体を平行移動して描き直す
	 */
	public void moveDraw(Point v){
		if (canvasStage != null){
			parallelMove(v);
			canvasStage.remove(this);
			canvasStage.add(this);
		}
		if (canvas != null){
			parallelMove(v);
			canvas.remove(this);
			canvas.add(this);
		}
		if (pane != null){
			parallelMove(v);
			pane.drawShapes();
		}
	}

	/**
	 * rotateXを実行して立体を描き直す
	 */
	public void rotateXDraw(double theta){
		if (canvasStage != null){
			rotateX(theta);
			canvasStage.remove(this);
			canvasStage.add(this);
		}
		if (canvas != null){
			rotateX(theta);
			canvas.remove(this);
			canvas.add(this);
		}
		if (pane != null){
			rotateX(theta);
			pane.drawShapes();
		}
	}
	/**
	 * rotateYを実行して立体を描き直す
	 */
	public void rotateYDraw(double theta){
		if (canvasStage != null){
			rotateY(theta);
			canvasStage.remove(this);
			canvasStage.add(this);
		}
		if (canvas != null){
			rotateY(theta);
			canvas.remove(this);
			canvas.add(this);
		}
		if (pane != null){
			rotateY(theta);
			pane.drawShapes();
		}
	}

	/**
	 * rotateZを実行して立体を描き直す
	 */
	public void rotateZDraw(double theta){
		if (canvasStage != null){
			rotateZ(theta);
			canvasStage.remove(this);
			canvasStage.add(this);
		}
		if (canvas != null){
			rotateZ(theta);
			canvas.remove(this);
			canvas.add(this);
		}
		if (pane != null){
			rotateZ(theta);
			pane.drawShapes();
		}
	}

	/**
	 * rotateを実行して立体を描き直す
	 */
	public void rotateDraw(double theta, Point p, Point v){
		if (canvasStage != null){
			rotate(theta, p, v);
			canvasStage.remove(this);
			canvasStage.add(this);
		}
		if (canvas != null){
			rotate(theta, p, v);
			canvas.remove(this);
			canvas.add(this);
		}
		if (pane != null){
			rotate(theta, p, v);
			pane.drawShapes();
		}
	}
}
