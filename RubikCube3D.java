import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.canvas.*;
import javafx.scene.shape.*;
import javafx.scene.input.*;
import javafx.animation.*;
import javafx.event.*;

import java.util.*;
import java.util.stream.Collectors;

import canvas3D.*;

public class RubikCube3D{
	static final int NULL = 0;
	static final int VERT_OR_HORI = 1;
	static final int FLAT = 2;
	static final Point O = new Point(0,0,0);
	static String[][][] polygonsStr;

	public static void main(String... args){
		// Pane3D上にルービックキューブを描画
		double w = 600;
		double h = 600;
		RubikCubeShape rubik = new RubikCubeShape(2);
		Pane3D pane3d = new Pane3D(w,h, 2.2,2.2, new Point(-2,-2,2), new Point(1,1,-1), new Point(1,-1,0), 1);
		pane3d.add(rubik);


		Pane pane = pane3d.getPane();
		Scene scene = pane3d.getScene();
		RubikCubeShape clone = rubik.copy();
		// Polygonの配列
		Polygon[][][] polygons = new Polygon[6][3][3];
		// Polygonの最初の位置をStringで表した配列
		polygonsStr = new String[6][3][3];
		List<Polygon> polygonList = pane3d.getDrawedShapes().get(0);
		for (int f = 0; f < 6; f++){
			for (int i = 0; i < 3; i++){
				for (int j = 0; j < 3; j++){
					polygons[f][i][j] = polygonList.get(f+18*i+6*j);
					polygonsStr[f][i][j] = f+","+i+","+j;
				}
			}
		}
		// 三面体の回転する向き
		Point motionV1 = new Point(-1,1,0);
		Point motionV2 = new Point(0,-1,1);
		Point motionV3 = new Point(1,0,-1);
		Point motionV4 = new Point(1,1,0);
		Point motionV5 = new Point(0,1,1);
		Point motionV6 = new Point(1,0,1);
		// 3面体のためのmotionV
		// 一行目が通常の向き、二行目が裏の向き。列はそれぞれ右、横（回転）、上向きの動き
		Point[][] motionVsFor3 = {{motionV1, motionV2, motionV3}, {motionV4, motionV5, motionV6}};
		// 2面体のためのmotionV
		// 一行目が通常の向き、二行目が裏の向き。列はそれぞれ右、横（回転）、上向きの動き
		Point[][] motionVsFor2 = {{motionV4, motionV2, motionV3}, {motionV1, motionV5, motionV6}};
		// マウスが押された座標や、回転の方向を記憶するためのMap
		Map<String, String> startProperty = new HashMap<String, String>();
		Map<String, String> flipProperty = new HashMap<String, String>();
		flipProperty.put("isFlip", "false");
		flipProperty.put("initialized", "false");
		// 描画されたキューブを構成するPolygonのイベント処理
		for (int _f = 0; _f < 6; _f++){
			for (int _i = 0; _i < 3; _i++){
				for (int _j = 0; _j < 3; _j++){
					int f1 = _f, i1 = _i, j1 = _j;
					Polygon poly = polygons[f1][i1][j1];
					// Polygonが押されたとき
					poly.setOnMousePressed((event) -> {
						// polyの位置情報を取得
						int f2 = 0, i2 = 0, j2 = 0;
out:						for (f2 = 0; f2 < 6; f2++){
							for (i2 = 0; i2 < 3; i2++){
								for (j2 = 0; j2 < 3; j2++){
									// f1,i1,j1は面のもともとの座標、f2,i2,j2は現在の座標
									if (polygonsStr[f2][i2][j2].equals(f1+","+i1+","+j1)){
										// この面の現在の座標を取得
										flipProperty.put("f",String.valueOf(f2));
										flipProperty.put("i",String.valueOf(i2));
										flipProperty.put("j",String.valueOf(j2));
										break out;
									}
								}
							}
						}
						// polyに相当する面を動かしうるTのNumberを取得
						int rot = (f2 % 2 == 0) ? (f2/2 * 3 + 3) % 9 : ((f2-1)/2 * 3 + 5) % 9;   // その面を反時計回りに回す操作
						int toR = (f2 % 2 == 0) ? Math.floorMod(rot-3+i2, 9) : (rot+3-i2)%9;     // その面を右に回す操作
						int toU = (f2 % 2 == 0) ? (rot+3+j2)%9               : Math.floorMod(rot-3-j2, 9);  // その面を上に回す操作
						// flipPropertyに保存
						flipProperty.put("rot", String.valueOf(rot));
						flipProperty.put("toR", String.valueOf(toR));
						flipProperty.put("toU", String.valueOf(toU));
						flipProperty.put("isFlip", "true");
					});
				}
			}
		}
		// paneのイベント処理
		pane.setOnMousePressed((event) -> {
			//pane.getChildren().addAll(new Rectangle(0,75,75,450,i), new Rectangle(525,75,75,450),
			//		new Rectangle(0,0,600,75), new Rectangle(0,525,600,75), new Circle(300,300,225), new Circle(300,300,150));
			double _x = event.getX();
			double _y = event.getY();
			startProperty.put("startX", String.valueOf(_x));
			startProperty.put("startY", String.valueOf(_y));
			clone.transformTo(rubik);
			if (!(isInsideCir(_x, _y, 300, 300, 225)))
				startProperty.put("howToRotate", String.valueOf(VERT_OR_HORI));
			else if (isInsideCir(_x, _y, 300,300, 225) && !(isInsideCir(_x, _y, 300,300, 150)))
				startProperty.put("howToRotate", String.valueOf(FLAT));
			else
				startProperty.put("howToRotate", String.valueOf(NULL));
		});
		pane.setOnMouseDragged((event) -> {
			double startX = Double.parseDouble(startProperty.get("startX"));
			double startY = Double.parseDouble(startProperty.get("startY"));
			int  howToRotate = Integer.parseInt(startProperty.get("howToRotate"));
			double newX = event.getX();
			double newY = event.getY();
			rubik.transformTo(clone);     // これから動かすためにドラッグのたびにもとの位置に戻しておく
			// 以下3つはキューブ全体を動かす操作
			if (howToRotate == VERT_OR_HORI){
				rubik.rotate((newY-startY)*2*Math.PI/450, O, new Point(1,-1,0));
				rubik.rotateZ((newX-startX)*2*Math.PI/600);
				pane3d.drawShapes();
			}
			else if (howToRotate == FLAT){
				rubik.rotateDraw(Math.atan2(newY-300,newX-300)-Math.atan2(startY-300,startX-300), O, new Point(1,1,-1));
			}
			// 以下はキューブのフリップ操作
			if (flipProperty.get("isFlip").equals("true")){
				// startPropertyからpolyの位置や、マウスの座標を取得
				int f = Integer.parseInt(flipProperty.get("f"));
				int i = Integer.parseInt(flipProperty.get("i"));
				int j = Integer.parseInt(flipProperty.get("j"));
				int rot = Integer.parseInt(flipProperty.get("rot"));
				int toR = Integer.parseInt(flipProperty.get("toR"));
				int toU = Integer.parseInt(flipProperty.get("toU"));
				Point dragV = new Point(newX-startX, newY-startY, 0);
				if (flipProperty.get("initialized").equals("false")){ // TのNumberが決まっていない
					if (dragV.norm() > 10){  // ドラッグの線分がある程度長さと向きを持てばTのNumberを決められる
						List<Integer> permutation = Arrays.asList(new Integer[] {0,1,2});
						List<Point> motionVsOfF = new ArrayList<Point>();
						if (i == 0 && j == 0){
							motionVsOfF.add(motionVsFor3[0][toR/3]);
							motionVsOfF.add(motionVsFor3[0][rot/3]);
							motionVsOfF.add(motionVsFor3[0][toU/3]);
						}
						else if (i == 0 && j == 2){
							motionVsOfF.add(motionVsFor3[1][toR/3].scalarMul(Math.pow(-1,f)));
							motionVsOfF.add(motionVsFor3[1][rot/3].minus());
							motionVsOfF.add(motionVsFor3[0][toU/3]);
						}
						else if (i == 2 && j == 0){
							motionVsOfF.add(motionVsFor3[0][toR/3]);
							motionVsOfF.add(motionVsFor3[1][rot/3]);
							motionVsOfF.add(motionVsFor3[1][toU/3].minus().scalarMul(Math.pow(-1,f)));
						}
						else if (i == 2 && j == 2){
							motionVsOfF.add(motionVsFor3[1][toR/3].scalarMul(Math.pow(-1,f)));
							motionVsOfF.add(motionVsFor3[0][rot/3].minus());
							motionVsOfF.add(motionVsFor3[1][toU/3].minus().scalarMul(Math.pow(-1,f)));
						}
						else if (i == 0 && j == 1){
							motionVsOfF.add(motionVsFor2[0][toR/3]);
							motionVsOfF.add(motionVsFor2[0][rot/3]);
							motionVsOfF.add(motionVsFor2[0][toU/3]);
						}
						else if (i == 1 && j == 0){
							motionVsOfF.add(motionVsFor2[1][toR/3].scalarMul(Math.pow(-1,f)));
							motionVsOfF.add(motionVsFor2[1][rot/3]);
							motionVsOfF.add(motionVsFor2[0][toU/3]);
						}
						else if (i == 1 && j == 2){
							motionVsOfF.add(motionVsFor2[0][toR/3]);
							motionVsOfF.add(motionVsFor2[1][rot/3].minus());
							motionVsOfF.add(motionVsFor2[0][toU/3]);
						}
						else if (i == 2 && j == 1){
							motionVsOfF.add(motionVsFor2[0][toR/3]);
							motionVsOfF.add(motionVsFor2[0][rot/3].minus());
							motionVsOfF.add(motionVsFor2[1][toU/3].minus().scalarMul(Math.pow(-1,f)));
						}
						Point center = rubik.getOuterFaces()[f][i][j].getCenter();
						List<Point> flips = motionVsOfF.stream().map(v -> v.plus(center))  // ベクトルの終点のList
							.map(p -> getMappedVector(center, p, pane3d))              // 射影されたmotionVのList
							.map(v -> v.normalize())
							.collect(Collectors.toList());
						int index = permutation.stream().max( Comparator.comparing(I -> 
									Math.abs(flips.get(I).dot(dragV))) ).get();
						Point howToFlip = flips.stream().max(Comparator.comparing(v -> Math.abs(v.dot(dragV)))).get();
						flipProperty.put("index", String.valueOf(index));
						flipProperty.put("howToFlip", howToFlip.toString());
						flipProperty.put("initialized", "true");
						System.out.println("toR,rot,toU : " + toR+","+rot+","+toU);
						System.out.println("index: "+index); 
						System.out.println("howToFlip : " + howToFlip.toString());
						System.out.println(motionVsOfF.get(index));
					}
				}
				else{  // TのNumberが決まった
					int index = Integer.parseInt(flipProperty.get("index"));
					Point howToFlip = Point.parsePoint(flipProperty.get("howToFlip"));
					if (index == 1){   // その面を回す操作(rot)
						Point center = pane3d.mapToPane(rubik.getOuterFaces()[f][1][1].getCenter());
						double cX = center.getX(); double cY = center.getY();
						double theta = (Math.atan2(startY-cY,startX-cX) - Math.atan2(newY-cY,newX-cX)) * Math.pow(-1,f);
						rubik.TDraw(rot, theta);
						flipProperty.put("Number", String.valueOf(rot));
						flipProperty.put("theta", String.valueOf(theta));
					}
					else if (index == 0){     // 右へ(toR)
						double theta = (howToFlip.dot(dragV)*2*Math.PI/600) * Math.pow(-1,f);
						rubik.TDraw(toR, theta);
						flipProperty.put("Number", String.valueOf(toR));
						flipProperty.put("theta", String.valueOf(theta));
					}
					else{                     // 上へ(toU)
						double theta = (howToFlip.dot(dragV)*2*Math.PI/600) * Math.pow(-1,f);
						rubik.TDraw(toU, theta);
						flipProperty.put("Number", String.valueOf(toU));
						flipProperty.put("theta", String.valueOf(theta));
					}
				}
			}
		});
		pane.setOnMouseReleased((event) -> {
			double startX = Double.parseDouble(startProperty.get("startX"));
			double startY = Double.parseDouble(startProperty.get("startY"));
			int  howToRotate = Integer.parseInt(startProperty.get("howToRotate"));
			double endX = event.getX();
			double endY = event.getY();
			if (howToRotate == VERT_OR_HORI){
				// motionVsFor3とmotionVsFor2のPointは共通しているので、一方を変換すれば十分
				for (int i = 0; i < 2; i++){
					for (int j = 0; j < 3; j++){
						motionVsFor3[i][j].rotate((endY-startY)*2*Math.PI/450, O, new Point(1,-1,0));
						motionVsFor3[i][j].rotateZ((endX-startX)*2*Math.PI/600);
					}
				}
			}
			else if (howToRotate == FLAT){
				for (int i = 0; i < 2; i++)
					for (int j = 0; j < 3; j++)
						motionVsFor3[i][j].rotate(Math.atan2(endY-300,endX-300)-Math.atan2(startY-300,startX-300),
								O, new Point(1,1,-1));
			}
			if (flipProperty.get("isFlip").equals("true") && flipProperty.get("initialized").equals("true")){
				int Number = Integer.parseInt(flipProperty.get("Number"));
				double theta = Double.parseDouble(flipProperty.get("theta"));
				int round = Math.toIntExact(Math.round(theta*2/Math.PI));
				rubik.transformTo(clone);
				rubik.TDraw(Number, Math.floorMod(round, 4));
				TForPolygon(Number, Math.floorMod(round, 4));
			}
			startProperty.put("howToRotate", String.valueOf(NULL));
			flipProperty.put("initialized", "false");
			flipProperty.put("isFlip", "false");
		});
		scene.setOnKeyPressed((event) -> {
			if (event.getCode() == KeyCode.F){
				rubik.FDraw();
				TForPolygon(3,3);
			}
			else if (event.getCode() == KeyCode.B){
				rubik.BDraw();
				TForPolygon(5);
			}
			else if (event.getCode() == KeyCode.R){
				rubik.RDraw();
				TForPolygon(8);
			}
			else if(event.getCode() == KeyCode.L){
				rubik.LDraw();
				TForPolygon(6,3);
			}
			else if (event.getCode() == KeyCode.U){
				rubik.UDraw();
				TForPolygon(0,3);
			}
			else if (event.getCode() == KeyCode.D){
				rubik.DDraw();
				TForPolygon(2);
			}
		});
	}

	// 点(x,y)が、(rx,ry)を始点とし、幅w、高さhの
	// 長方形の内部にあるかどうかを調べる
	static private boolean isInsideRect(double x, double y, double rx, double ry, double w, double h){
		return (rx <= x && x <= rx+w) && (ry <= y && y <= ry+h);
	}

	// 点(x,y)が、(cx,cy)を中心とする半径rの円の
	// 内部にあるかどうかを調べる
	static private boolean isInsideCir(double x, double y, double cx, double cy, double r){
		return Math.pow(x-cx,2)+Math.pow(y-cy,2) <= Math.pow(r,2);
	}

	// Pane3Dの三次元ベクトルをPaneに射影したときの二次元ベクトルを返す
	static private Point getMappedVector(Point startP, Point endP, Pane3D pane3d){
		// 本質的にはベクトルの始点と終点を射影して、
		// その差を取る
		Point mappedStartP = pane3d.mapToPane(startP);
		Point mappedEndP = pane3d.mapToPane(endP);
		return mappedEndP.plus(mappedStartP.minus());
	}

	// TによるPolygonの位置を把握するためのT
	static private  void TForPolygon(int Number){
		int f = 2*(Number / 3);
		int row = Number % 3;
		if (row == 0){    // top row
			int top_f = (f + 4) % 6;
			slideBlocksRForPolygon(f, row);
			rotateBlocksLForPolygon(top_f);
		}
		else if (row == 3-1){   // bottom row
			int bottom_f = (f + 5) % 6;
			slideBlocksRForPolygon(f, row);
			rotateBlocksRForPolygon(bottom_f);
		}
		else{   // middle row
			slideBlocksRForPolygon(f, row);
		}
	}

	static private void TForPolygon(int Number, int rot){
		for (int i = 0; i < rot; i++){
			TForPolygon(Number);
		}
	}

	// 以下、Tのための補助メソッド

	static private void slideBlocksRForPolygon(int f, int row){
		int f2 = (f + 3) % 6;
		int f3 = f + 1;
		int f4 = (f + 2) % 6;
		for (int i = 0; i < 3; i++){
			String temp = polygonsStr[f][row][i];
			polygonsStr[f][row][i] = polygonsStr[f4][3-1-i][row];
			polygonsStr[f4][3-1-i][row] = polygonsStr[f3][i][3-1-row];
			polygonsStr[f3][i][3-1-row] = polygonsStr[f2][3-1-row][3-1-i];
			polygonsStr[f2][3-1-row][3-1-i] = temp;
		}
	}

	static private void slideBlocksLForPolygon(int f, int row){
		int f2 = (f + 3) % 6;
		int f3 = f + 1;
		int f4 = (f + 2) % 6;
		for (int i = 0; i < 3; i++){
			String temp = polygonsStr[f][row][i];
			polygonsStr[f][row][i] = polygonsStr[f2][3-1-row][3-1-i];
			polygonsStr[f2][3-1-row][3-1-i] = polygonsStr[f3][i][3-1-row];
			polygonsStr[f3][i][3-1-row] = polygonsStr[f4][3-1-i][row];
			polygonsStr[f4][3-1-i][row] = temp;
		}
	}

	static private void rotateBlocksLForPolygon(int f){
		for (int i = 0; i < 3/2; i++){
			for (int j = i; j < 3-1-i; j++){
				String temp = polygonsStr[f][i][j];
				polygonsStr[f][i][j] = polygonsStr[f][j][3-1-i];
				polygonsStr[f][j][3-1-i] = polygonsStr[f][3-1-i][3-1-j];
				polygonsStr[f][3-1-i][3-1-j] = polygonsStr[f][3-1-j][i];
				polygonsStr[f][3-1-j][i] = temp;
			}
		}
	}

	static private void rotateBlocksRForPolygon(int f){
		for (int i = 0; i < 3/2; i++){
			for (int j = i; j < 3-1-i; j++){
				String temp = polygonsStr[f][i][j];
				polygonsStr[f][i][j] = polygonsStr[f][3-1-j][i];
				polygonsStr[f][3-1-j][i] = polygonsStr[f][3-1-i][3-1-j];
				polygonsStr[f][3-1-i][3-1-j] = polygonsStr[f][j][3-1-i];
				polygonsStr[f][j][3-1-i] = temp;
			}
		}
	}
}
