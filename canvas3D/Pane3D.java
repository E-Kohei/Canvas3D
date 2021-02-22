package canvas3D;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.shape.*;
import javafx.scene.paint.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.*;

public class Pane3D{
	// Paneのプロパティ
	private Stage pstage;          // Pane3Dの本質はStageである
	private Scene scene;
	protected Pane root;           // Root Pane
	private double width, height;  // Paneの高さ幅

	// カメラのプロパティ
	private double cameraWidth, cameraHeight;  // カメラの高さ幅
	private double WMag, HMag;     // Paneとカメラの倍率
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

	// このPaneが持つ立体たち
	private List<Shape3D> shapes = new ArrayList<Shape3D>();
	// 描かれたPolygonたち
	private Map<Integer, List<Polygon>> drawedShapes = new HashMap<Integer, List<Polygon>>();


	/**
	 * Pane3Dの本質はStageであり、Platform.startupによりアプリケーションを
	 * 実行する
	 */
	public Pane3D(double width, double height, double cameraWidth, double cameraHeight,
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
		RunnableFuture<Scene> startup = new FutureTask<Scene>(() -> {
			pstage = new Stage();
			root = new Pane();
			root.setPrefSize(width, height);
			scene = new Scene(root);
			pstage.setScene(scene);
			pstage.setTitle("3D Pane");
			pstage.show();
			return scene;
		});
		Platform.startup(startup);
		try{
			scene = startup.get();
		} catch(InterruptedException | ExecutionException e){
			e.printStackTrace();
		}
	}

	public Pane3D(){
		// デフォルトに設定
		this(400, 400, 1, 1, O, defaultV, defaultR, 1);
	}

	public Stage getStage() { return this.pstage; }
	
	public Pane getPane(){ return this.root; }

	public Scene getScene() {return this.scene; }

	public Point getCameraPoint(){ return cameraPoint.copy(); }

	public Point getCameraVector(){ return cameraVector.copy(); }

	public Point getRightVector(){ return rightVector.copy(); }

	public double getfd(){ return fd; }

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

	public List<Shape3D> getShapes(){
		return shapes;
	}

	public Map<Integer, List<Polygon>> getDrawedShapes(){
		return drawedShapes;
	}

	/**
	 * Shape3DをこのPaneの描画対象にに加える
	 */
	public void add(Shape3D shape){
		shape.setPane(this);
		shapes.add(shape);
		drawShapes();
	}

	/**
	 * Shape3DをこのPaneの描画対象から外す
	 */
	public void remove(int shapeID){
		shapes.remove(shapeID);
		root.getChildren().removeAll(drawedShapes.get(shapeID));
	}

	/**
	 * Paneをクリア
	 */
	synchronized public void clear(){
		Platform.runLater(() -> {
			root.getChildren().clear();
		});
	}

	/**
	 * 一つのShape3Dを描く
	 */
	synchronized private void drawShape(int shapeID){
		Shape3D shape = shapes.get(shapeID);
		if (drawedShapes.get(shapeID) == null){
			// このshapeをdrawedShapesに加える
			drawedShapes.put(shapeID, new ArrayList<Polygon>());
			for (int i = 0; i < shape.getFaces().size(); i++)
				drawedShapes.get(shapeID).add(new Polygon());
		}
		List<Face> FacesCopy = shape.getFacesCopy();
		for (Face f : FacesCopy){
			// カメラがデフォルトの位置に来るように面を動かす
			f.parallelMove(-cameraPoint.getX(),-cameraPoint.getY(),-cameraPoint.getZ());
			f.rotate(theta1, O, cameraVector.cross(defaultV));
			f.rotateX(theta2);
		}
		List<Integer> permutation = new ArrayList<Integer>();
		for (int i = 0; i < FacesCopy.size(); i++)
			permutation.add(i);
		// カメラと面との距離でインデックスをソートする
		permutation.sort(Comparator.comparing(i ->
				-FacesCopy.get((int)i).getCenter().norm()));
		// 多角形の点のデータを作る
		int maxN = FacesCopy.stream().mapToInt(f -> f.getPath().size())
			.max().orElse(10);
		Double[][] pointsMat = new Double[FacesCopy.size()][2*maxN];
		for (Integer i : permutation){
			Face f = FacesCopy.get(i);
			List<Point> ps = f.getPathCopy().stream()
				.filter(p -> p.getX() > 0)
				.collect(Collectors.toList());
			for (int j = 0; j < ps.size(); j++){
				double x = ps.get(j).getX();
				double y = ps.get(j).getY();
				double z = ps.get(j).getZ();
				pointsMat[i][2*j]   = width/2-y/x*fd*WMag;
				pointsMat[i][2*j+1] = height/2-z/x*fd*HMag;
			}
			Polygon poly = drawedShapes.get(shapeID).get(i);
			poly.getPoints().clear();
			poly.getPoints().addAll(pointsMat[i]);
			poly.setFill(f.getPaint());
			poly.setStroke(Color.BLACK);
		}

		Platform.runLater(() -> {
			for (Integer i : permutation){
				Polygon poly = drawedShapes.get(shapeID).get(i);
				root.getChildren().add(poly);
			}
		});
	}

	/*synchronized public void redrawShape(Shape3D shape){
		List<Face> FacesCopy = shape.getFacesCopy();
		for (Face f : FacesCopy){
			// translate faces to set camera default position
			f.parallelMove(-cameraPoint.getX(),-cameraPoint.getY(),-cameraPoint.getZ());
			f.rotate(theta1, O, cameraVector.cross(defaultV));
			f.rotateX(theta2);
		}
		FacesCopy.sort(Comparator.comparing(f -> 
				-f.getCenter().norm()));
		drawedShapes.get(shape).sort(Comparator.comparing)
		Platform.runLater(() -> {
			for (Face f : FacesCopy){
				List<Point> ps = f.getPathCopy().stream()
					.filter(p -> p.getX() > 0)
					.collect(Collectors.toList());
				double[] ps2 = new double[ps.size()*2];
				for (int i = 0; i < ps.size(); i++){
					double x = ps.get(i).getX();
					double y = ps.get(i).getY();
					double z = ps.get(i).getZ();
					ps2[2*i]   = width/2-y/x*fd*WMag;
					ps2[2*i+1] = height/2-z/x*fd*HMag;
				}
				drawedShapes.get(shape).
			}
		});
	}*/

	/**
	 * 立体の前後関係などを考慮して描画対象になっているすべての
	 * Shape3Dを描く
	 */
	public void drawShapes(){
		List<Integer> permutation = new ArrayList<Integer>();
		for (int shapeID = 0; shapeID < shapes.size(); shapeID++)
			permutation.add(shapeID);
		permutation.sort(Comparator.comparing(id -> 
				-shapes.get(id).getCenter().dist(cameraPoint)));
		clear();
		for (Integer shapeID : permutation){
			drawShape(shapeID);
		}
	}

	public Point mapToPane(Point p){
		Point p2 = p.copy();
		p2.parallelMove(-cameraPoint.getX(),-cameraPoint.getY(),-cameraPoint.getZ());
		p2.rotate(theta1, O, cameraVector.cross(defaultV));
		p2.rotateX(theta2);
		double x = p2.getX();
		double y = p2.getY();
		double z = p2.getZ();
		return new Point(width/2-y/x*fd*WMag, height/2-z/x*fd*HMag,0);
	}

	/*synchronized public void drawShapes(){
		clear();
		// shapeをカメラとの距離でソートする
		shapes.sort(Comparator.comparing(s -> 
				-s.getCenter().dist(cameraPoint)));
		// すべてのshapeのためのpolygonの点のデータを作る
		int maxF = shapes.stream().mapToInt(s -> s.getFaces().size()).max().orElse(10);
		int maxN = shapes.stream().map(s -> s.getFacesCopy())
			.mapToInt(fs -> fs.stream().mapToInt(f -> f.getPath().size()).max().orElse(10))
			.max().orElse(10);
		Double[][][] pointsAry = new Double[shapes.size()][maxF][2*maxN];
		// polygonを描く順番
		List<List<Integer>> permutations = new ArrayList<List<Integer>>();
		for (int i = 0; i < shapes.size(); i++){
			Shape3D shape = shapes.get(i);
			if (!drawedShapes.keySet().contains(shape)){
				// このshapeをdrawedShapesに加える
				drawedShapes.put(shape, new ArrayList<Polygon>());
				for (int j = 0; j < shape.getFaces().size(); j++)
					drawedShapes.get(shape).add(new Polygon());
			}
			List<Face> FacesCopy = shape.getFacesCopy();
			for (Face f : FacesCopy){
				// カメラがデフォルトの位置に来るように面を動かす
				f.parallelMove(-cameraPoint.getX(),-cameraPoint.getY(),-cameraPoint.getZ());
				f.rotate(theta1, O, cameraVector.cross(defaultV));
				f.rotateX(theta2);
			}
			permutations.add(new ArrayList<Integer>());
			for (int j = 0; j < FacesCopy.size(); j++)
				permutations.get(i).add(j);
			// カメラと面との距離でインデックスをソートする
			permutations.get(i).sort(Comparator.comparing(I ->
						-FacesCopy.get((int)I).getCenter().norm()));
			for (Integer j : permutations.get(i)){
				Face f = FacesCopy.get(j);
				List<Point> ps = f.getPathCopy().stream()
					.filter(p -> p.getX() > 0)
					.collect(Collectors.toList());
				for (int k = 0; k < ps.size(); k++){
					double x = ps.get(k).getX();
					double y = ps.get(k).getY();
					double z = ps.get(k).getZ();
					pointsAry[i][j][2*k]   = width/2-y/x*fd*WMag;
					pointsAry[i][j][2*k+1] = height/2-z/x*fd*HMag;
				}
				Polygon poly = drawedShapes.get(shape).get(j);
				poly.getPoints().clear();
				poly.getPoints().addAll(pointsAry[i][j]);
				poly.setFill(f.getPaint());
				poly.setStroke(Color.BLACK);
			}
		}

		Platform.runLater(() -> {
			for (int i = 0; i < shapes.size(); i++){
				Shape3D shape = shapes.get(i);
				for (Integer j : permutations.get(i)){
					Polygon poly = drawedShapes.get(shape).get(j);
					root.getChildren().add(poly);
				}
			}
		});
	}*/

}
