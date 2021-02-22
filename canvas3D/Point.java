package canvas3D;

public class Point implements Cloneable{
	double x;
	double y;
	double z;

	public Point(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}

	static public Point parsePoint(String s){
		String s2 = s.replace("(", "").replace(")", "");
		String[] values = s2.split(", ");
		double x = Double.parseDouble(values[0]);
		double y = Double.parseDouble(values[1]);
		double z = Double.parseDouble(values[2]);
		return new Point(x,y,z);
	}

	@Override
	public String toString(){
		return String.format("(%3.5f, %3.5f, %3.5f)", x, y, z);
	}

	@Override
	public boolean equals(Object obj){
		if (!(obj instanceof Point)){
			return false;
		}
		Point p = (Point) obj;
		return this.x == p.x && this.y == p.y && this.z == p.z;
	}

	@Override
	public int hashCode(){
		return (int) Math.floor(x+y+z);
	}

	@Override
	public Object clone(){
		return new Point(this.x, this.y, this.z);
	}

	public double getX(){ return x; }

	public double getY(){ return y; }

	public double getZ(){ return z; }

	public void setX(double x){ this.x = x; }

	public void setY(double y){ this.y = y; }

	public void setZ(double z){ this.z = z; }

	public Point copy(){
		return (Point) this.clone();
	}

	public void moveTo(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void moveTo(Point p){
		this.x = p.x;
		this.y = p.y;
		this.z = p.z;
	}

	public void parallelMove(double dx, double dy, double dz){
		this.x += dx;
		this.y += dy;
		this.z += dz;
	}

	public void parallelMove(Point p){
		this.x += p.x;
		this.y += p.y;
		this.z += p.z;
	}

	/**
	 * x軸周りのtheta回転
	 */
	public void rotateX(double theta){
		double _y = y;
		double _z = z;
		this.y = Math.cos(theta)*_y - Math.sin(theta)*_z;
		this.z = Math.sin(theta)*_y + Math.cos(theta)*_z;
	}

	/**
	 * y軸周りのtheta回転
	 */
	public void rotateY(double theta){
		double _z = z;
		double _x = x;
		this.z = Math.cos(theta)*_z - Math.sin(theta)*_x;
		this.x = Math.sin(theta)*_z + Math.cos(theta)*_x;
	}

	/**
	 * z軸周りのtheta回転
	 */
	public void rotateZ(double theta){
		double _x = x;
		double _y = y;
		this.x = Math.cos(theta)*_x - Math.sin(theta)*_y;
		this.y = Math.sin(theta)*_x + Math.cos(theta)*_y;
	}

	/**
	 * 点pを通りベクトルvに平行な直線を軸とするtheta回転
	 */
	public void rotate(double theta, Point p, Point v){
		if (v.equals(new Point(0,0,0)))
			return;
		double phi1 = Math.atan2(v.y, v.x);    // vのxy平面の成分の角度
		double phi2 = Math.asin(v.z / v.norm());// vのxy平面との角度

		// 回転軸が原点を通るように平行移動
		this.parallelMove(-p.x, -p.y, -p.z);
		// 回転軸をx軸に移す
		this.rotateZ(-phi1);
		this.rotateY(phi2);
		// theta回転
		this.rotateX(theta);
		// 回転軸をもとに戻す
		this.rotateY(-phi2);
		this.rotateZ(phi1);
		// 回転軸がpを通るようにもとに戻す
		this.parallelMove(p.x, p.y, p.z);
	}

	/**
	 * 別の点pとの距離を返す
	 */
	public double dist(Point p){
		double dx_sqr = Math.pow(this.x-p.x, 2);
		double dy_sqr = Math.pow(this.y-p.y, 2);
		double dz_sqr = Math.pow(this.z-p.z, 2);
		return Math.sqrt(dx_sqr + dy_sqr + dz_sqr);
	}

	/**
	 * 原点からの距離を返す
	 */
	public double norm(){
		return Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));
	}

	/**
	 * 別の点との和としての点を返す
	 */
	public Point plus(Point p){
		return new Point(x+p.x, y+p.y, z+p.z);
	}

	/**
	 * Point(-x,-y,-z)を返す
	 */
	public Point minus(){
		return new Point(-x, -y, -z);
	}

	/**
	 * スカラー倍Point(c*x,c*y,c*z)を返す
	 */
	public Point scalarMul(double c){
		return new Point(c*x, c*y, c*z);
	}

	/**
	 * ベクトルとしての内積を返す
	 */
	public double dot(Point v){
		return x*v.x + y*v.y + z*v.z;
	}

	/**
	 * ベクトルとしての外積を返す
	 */
	public Point cross(Point v){
		return new Point(y*v.z-v.y*z, z*v.x-v.z*x, x*v.y-v.x*y);
	}

	/**
	 * ベクトルとして正規化ものを返す
	 */
	public Point normalize(){
		double norm = this.norm();
		return new Point(x/norm, y/norm, z/norm);
	}
}
