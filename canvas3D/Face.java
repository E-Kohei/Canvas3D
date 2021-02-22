package canvas3D;

import javafx.scene.paint.*;
import java.util.*;

public class Face implements Cloneable{
	protected List<Point> path = new ArrayList<Point>();
	protected Paint paint;

	public Face(Paint paint, Point... points){
		for (Point p : points){
			path.add(p);
		}
		this.paint = paint;
	}

	public Face(Point... points){
		for (Point p : points){
			path.add(p);
		}
		this.paint = Color.WHITE;
	}

	public Face(Paint paint, double[] points){
		for (int i = 0; i < points.length/3; i++){
			path.add(
			new Point(points[3*i], points[3*i+1], points[3*i+2])
			);
		}
		this.paint = paint;
	}

	public Face(double[] points){
		for (int i = 0; i < points.length/3; i++){
			path.add(
			new Point(points[3*i], points[3*i+1], points[3*i+2])
			);
		}
		this.paint = Color.WHITE;
	}

	@Override
	public String toString(){
		return path.toString();
	}

	@Override
	public boolean equals(Object obj){
		if (!(obj instanceof Face))
			return false;
		if (this.path.size() != ((Face)obj).getPath().size())
			return false;
		Set<Point> pathSet1 = new HashSet<Point>(path);
		Set<Point> pathSet2 = new HashSet<Point>(((Face)obj).getPath());
		if (pathSet1.equals(pathSet2) 
			&& this.paint.equals(((Face)obj).getPaint()))
			return true;
		else
			return false;
	}

	@Override
	public int hashCode(){
		return path.stream()
		.mapToInt(p -> p.hashCode())
		.reduce(0, (a,b) -> a+b);
	}

	@Override
	public Object clone(){
		Point[] CopyPath = new Point[path.size()];
		for (int i = 0; i < path.size(); i++){
			CopyPath[i] = path.get(i).copy();
		}
		return new Face(this.paint, CopyPath);
	}

	/**
	 * この面のpathを返す
	 */
	public List<Point> getPath(){
		return this.path;
	}

	/**
	 * この面のpathのコピーを返す
	 */
	public List<Point> getPathCopy(){
		List<Point> CopyPath = new ArrayList<>();
		for (Point p : this.path){
			CopyPath.add(p.copy());
		}
		return CopyPath;
	}

	/**
	 * この面の色を返す
	 */
	public Paint getPaint(){
		return this.paint;
	}

	/**
	 * この面の色を設定する
	 */
	public void setPaint(Paint paint){
		this.paint = paint;
	}

	/**
	 * この面のコピーを返す
	 */
	public Face copy(){
		return (Face) this.clone();
	}

	/**
	 * 同じ多角形かどうか調べる
	 */
	public boolean isSamePoly(Face f){
		if (path.size() == f.getPath().size())
			return true;
		else
			return false;
	}

	/**
	 * 面の形によらず、同じ多角形であれば
	 * fに変形し、色も変える
	 */
	public void transformTo(Face f){
		List<Point> path2 = f.getPath();
		if (path.size() == path2.size()){
			for (int i = 0; i < path.size(); i++){
				path.get(i).moveTo(path2.get(i));
			}
		}
		this.setPaint(f.getPaint());
	}

	/**
	 * (dx, dy, dz)方向の平行移動
	 */
	public void parallelMove(double dx, double dy, double dz){
		for (Point p : path)
			p.parallelMove(dx, dy, dz);
	}

	/**
	 * ベクトルv方向の平行移動
	 */
	public void parallelMove(Point v){
		for (Point p : path)
			p.parallelMove(v);
	}

	/**
	 * x軸回りのtheta回転
	 */
	public void rotateX(double theta){
		for (Point p : path)
			p.rotateX(theta);
	}

	/**
	 * y軸回りのtheta回転
	 */
	public void rotateY(double theta){
		for (Point p : path)
			p.rotateY(theta);
	}

	/**
	 * z軸回りのtheta回転
	 */
	public void rotateZ(double theta){
		for (Point p : path)
			p.rotateZ(theta);
	}

	/**
	 * 点pを通りvに平行な直線を軸とするtheta回転
	 */
	public void rotate(double theta, Point p, Point v){
		for (Point point : path)
			point.rotate(theta, p, v);
	}

	/**
	 * 面の中心を返す。一般には全点の算術平均を返す
	 */
	public Point getCenter(){
		double x_sum = 0, y_sum = 0, z_sum = 0;
		int len = path.size();
		for (int i = 0; i < len; i++){
			x_sum += path.get(i).getX();
			y_sum += path.get(i).getY();
			z_sum += path.get(i).getZ();
		}
		return new Point(x_sum/len, y_sum/len, z_sum/len);
	}
}

