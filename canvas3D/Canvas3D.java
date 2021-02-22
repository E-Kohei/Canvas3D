package canvas3D;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.canvas.*;
import javafx.scene.paint.*;

import java.util.*;
import java.util.stream.Collectors;

public class Canvas3D{
	// Canvasのプロパティ
	private double width, height;  // Canvasの高さ幅
	private Canvas canvas;         // 実際に図形を描くCanvas
	private GraphicsContext gc;    // CanvasのgraphicsContext

	// カメラのプロパティ
	private double cameraWidth, cameraHeight;  // カメラの高さ幅
	private double WMag, HMag;     // Canvasとカメラの倍率
	private Point cameraPoint;     // レンズの位置
	private Point cameraVector;    // カメラの向き
	private double fd;             // レンズから投影版までの距離
	private Point rightVector;    // カメラの右を表すベクトル(これによりカメラの垂直方向の傾きが定まる)
	// デフォルトのカメラのプロパティ
	// デフォルトはレンズが原点、向きがx軸正の向き、右がy軸負の向きのカメラ
	static private Point O = new Point(0, 0, 0);
	static private Point defaultV = new Point(1, 0, 0);
	static private Point defaultR = new Point(0,-1,0);
	// カメラをデフォルトの位置に動かすために必要な回転角度
	// あとで描画時にShape3Dを動かすときに使う
	private double theta1;  // cameraVectorをdefaultVに合わせるための回転角
	private double theta2;  // rightVectorをdefaultRに合わせるための回転角

	// このCanvasが持つ立体たち
	private List<Shape3D> shapes = new ArrayList<Shape3D>();


	/**
	 * Canvas3Dの本質はStageであり、Platform.startupによりアプリケーションを
	 * 実行する
	 */
	public Canvas3D(double width, double height, double cameraWidth, double cameraHeight,
			Point cameraPoint, Point cameraVector, Point right, double fd){
		this.width = width;
		this.height = height;
		this.cameraWidth = cameraWidth;
		this.cameraHeight = cameraHeight;
		// 一般には縦横の倍率は等しいことが望ましい
		this.WMag = width / cameraWidth;
		this.HMag = height / cameraHeight;
		this.cameraPoint = cameraPoint;
		this.cameraVector = cameraVector;
		this.rightVector = right;
		this.fd = fd;
		updateThetas();
		this.canvas = new Canvas(width, height);
		this.gc = canvas.getGraphicsContext2D();
	}

	public Canvas3D(){
		// デフォルトに設定
		this(400, 400, 1, 1, O, defaultV, defaultR, 1);
	}

	public Point getCameraPoint(){ return cameraPoint.copy(); }

	public Point getCameraVector(){ return cameraVector.copy(); }

	public Point getRightVector(){ return rightVector.copy(); }

	public double getfd(){ return fd; }

	public Canvas getCanvas(){
		return this.canvas;
	}

	public GraphicsContext getGraphicsContext3D(){
		return this.gc;
	}

	public void setCameraPoint(Point newP){ 
		cameraPoint = newP;
		updateThetas();
	}

	public void setCameraVector(Point newV){ 
		cameraVector = newV;
		updateThetas();
	}

	public void setRightVector(Point newR){ 
		rightVector = newR;
		updateThetas();
       	}

	public void setfd(double newfd){ 
		fd = newfd;
		updateThetas();
	}

	// カメラのプロパティからtheta1, theta2を設定し直す
	private void updateThetas(){
		this.theta1 = Math.acos(cameraVector.dot(defaultV)/cameraVector.norm());
		Point movedV = cameraPoint.plus(rightVector)
			.plus(cameraPoint.minus());
		movedV.rotate(theta1, O, cameraVector.cross(defaultV));
		this.theta2 = Math.atan2(movedV.getZ(), -movedV.getY());
	}

	/**
	 * Shape3DをこのCanvasの描画対象にに加える
	 */
	public void add(Shape3D shape){
		shape.setCanvas(this);
		shapes.add(shape);
		drawShapes();
	}

	/**
	 * Shape3DをこのCanvasの描画対象から外す
	 */
	public void remove(Shape3D shape){
		shapes.remove(shape);
		drawShapes();
	}

	/**
	 * Canvasをクリア
	 */
	public void clear(){
		gc.clearRect(0, 0, width, height);
	}

	/**
	 * 一つのShape3Dを描く
	 */
	private void drawShape(Shape3D shape){
		List<Face> FacesCopy = shape.getFacesCopy();
		for (Face f : FacesCopy){
			// translate faces to set camera default position
			f.parallelMove(-cameraPoint.getX(),-cameraPoint.getY(),-cameraPoint.getZ());
			f.rotate(theta1, O, cameraVector.cross(defaultV));
			f.rotateX(theta2);
		}
		FacesCopy.sort(Comparator.comparing(f -> 
				-f.getCenter().norm()));
		for (Face f : FacesCopy){
			List<Point> ps = f.getPathCopy().stream()
				.filter(p -> p.getX() > 0)
				.collect(Collectors.toList());
			gc.setLineWidth(1);
			gc.strokePolygon(ps.stream()
					.mapToDouble(p -> width/2-p.getY()/p.getX()*fd*WMag)
					.toArray(),
					ps.stream()
					.mapToDouble(p -> height/2-p.getZ()/p.getX()*fd*HMag)
					.toArray(),
					ps.size());
			gc.setFill(Color.WHITE);
			gc.fillPolygon(ps.stream()
					.mapToDouble(p -> width/2-p.getY()/p.getX()*fd*WMag)
					.toArray(),
					ps.stream()
					.mapToDouble(p -> height/2-p.getZ()/p.getX()*fd*HMag)
					.toArray(),
					ps.size());
		}
	}

	/**
	 * 立体の前後関係などを考慮して描画対象になっているすべての
	 * Shape3Dを描く
	 */
	private void drawShapes(){
		shapes.sort(Comparator.comparing(s -> 
				-s.getCenter().dist(cameraPoint)));
		clear();
		for (Shape3D shape : shapes){
			drawShape(shape);
		}
	}

}
