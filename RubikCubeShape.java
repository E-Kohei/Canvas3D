/// Pane3D専用のルービックキューブ

import canvas3D.*;

import javafx.scene.paint.*;
import java.util.*;

public class RubikCubeShape extends Shape3D{
	private Point center;
	private double length;
	private Face[][][] outerFaces;
	//private Face[] internalFaces;
	private Point axis1;
	private Point axis2;
	private Point axis3;

	/**
	 * 中心がcenter、 辺の長さがlengthのルービックキューブを作成。
	 * 初期状態は前面(face0)がyz平面上に、左面(face2)がzx平面上に、
	 * 上面(face4)がxy平面上にあるな状態
	 */
	public RubikCubeShape(Point center, double length){
		this.center = center;
		this.length = length;
		this.outerFaces = new Face[6][3][3];   // 6つの面の9つのブロック
		//this.internalFaces = new Face[3]; // 回すときに中が透けないようにするための白い面
		this.axis1 = new Point(0,0,-1);
		this.axis2 = new Point(-1,0,0);
		this.axis3 = new Point(0,-1,0);
		double len = length/3;
		Point vx = new Point(len,0,0);
		Point vy = new Point(0,len,0);
		Point vz = new Point(0,0,len);
		Point minusV = new Point(-length/2, -length/2, -length/2);
		for (int i = 0; i < 3; i++){
			for (int j = 0; j < 3; j++){
				Parallel face0 = new Parallel(Color.RED, center.plus(new Point(0, len*j, len*i))
						.plus(minusV), vz,vy);
				outerFaces[0][i][j] = face0;

				Parallel face1 = new Parallel(Color.ORANGE, center.plus(new Point(length, length-len*i, length-len*j))
						.plus(minusV), vy.minus(),vz.minus());
				outerFaces[1][i][j] = face1;

				Parallel face2 = new Parallel(Color.YELLOW, center.plus(new Point(len*i, 0, len*j))
						.plus(minusV), vx,vz);
				outerFaces[2][i][j] = face2;

				Parallel face3 = new Parallel(Color.WHITE, center.plus(new Point(length-len*j, length, length-len*i))
						.plus(minusV), vz.minus(),vx.minus());
				outerFaces[3][i][j] = face3;

				Parallel face4 = new Parallel(Color.GREEN, center.plus(new Point(len*j, len*i, 0))
						.plus(minusV), vy,vx);
				outerFaces[4][i][j] = face4;

				Parallel face5 = new Parallel(Color.BLUE, center.plus(new Point(length-len*i, length-len*j, length))
						.plus(minusV), vx.minus(),vy.minus());
				outerFaces[5][i][j] = face5;
				faces.add(face0); faces.add(face1); faces.add(face2); faces.add(face3); faces.add(face4); faces.add(face5);
			}
		}
		/*
		Face inter0 = new Parallel(center.plus(new Point(-length/2,-length/2,0)), new Point(1,0,0), new Point(0,1,0));
		Face inter1 = new Parallel(center.plus(new Point(0,-length/2,-length/2)), new Point(0,1,0), new Point(0,0,1));
		Face inter2 = new Parallel(center.plus(new Point(-length/2,0,-length/2)), new Point(0,0,1), new Point(1,0,0));
		internalFaces[0] = inter0; internalFaces[1] = inter1; internalFaces[2] = inter2;
		faces.add(inter0); faces.add(inter1); faces.add(inter2);*/

	}

	/**
	 * 中心が原点、辺の長さがlengthのルービックキューブを作成
	 */
	public RubikCubeShape(double length){
		this(new Point(0,0,0), length);
	}

	/**
	 * U,Dの回転軸axis1を得る
	 */
	public Point getAxis1(){ return this.axis1; }

	/**
	 * F,Bの回転軸axis2を得る
	 */
	public Point getAxis2(){ return this.axis2; }

	/**
	 * R,Lの回転軸axis3を得る
	 */
	public Point getAxis3(){ return this.axis3; }

	/**
	 * U,Dの回転軸axis1をセット
	 */
	public void setAxis1(Point newAxis){ this.axis1.moveTo(newAxis); }

	/**
	 * F,Bの回転軸axis2をセット
	 */
	public void setAxis2(Point newAxis){ this.axis2.moveTo(newAxis); }

	/**
	 * R,Lの回転軸axis3をセット
	 */
	public void setAxis3(Point newAxis){ this.axis3.moveTo(newAxis); }

	/**
	 * キューブの面を6x3x3の配列で返す
	 */
	public Face[][][] getOuterFaces(){
		return this.outerFaces;
	}

	/**
	 * RubikCubeShapeのコピーを返す
	 */
	@Override
	public RubikCubeShape copy(){
		RubikCubeShape clone = new RubikCubeShape(this.center, this.length);
		clone.setAxis1(this.axis1);
		clone.setAxis2(this.axis2);
		clone.setAxis3(this.axis3);
		clone.transformTo(this);
		return clone;
	}

	/**
	 * ルービックキューブの基本的な操作で、face iのrow番目の行を
	 * 右に90°回転させる。Numberと行ブロックの関係は以下の通り
	 * 
	 * Number  :  ブロック行
	 * 0から2  :  face0のrow番目
	 * 3から5  :  face2のrow番目
	 * 6から8  :  face4のrow番目
	 */
	public void T(int Number){
		// fundamental transformation of rubik cube
		// slide blocks to the right, and if its row is top or bottom,
		// rotate the top face or the bottom face
		//
		// Number indicates which block-row to move;
		// 0 <= Number <= 3-1 : row_Number of face0
		// 3 <= Number <= 2*3-1 : row_Number of face2
		// 2*3 <= Number <= 3*3-1 : row_Number of face 4
		int f = 2*(Number / 3);
		int row = Number % 3;
		Point axis = new Point(0,0,0);
		switch (f){
			case 0: axis = axis1;break;
			case 2: axis = axis2;break;
			case 4: axis = axis3;break;
		}
		if (row == 0){    // top row
			int top_f = (f + 4) % 6;
			slideBlocksR(f, row);
			rotateBlocksL(top_f);
		}
		else if (row == 3-1){   // bottom row
			int bottom_f = (f + 5) % 6;
			slideBlocksR(f, row);
			rotateBlocksR(bottom_f);
		}
		else{   // middle row
			slideBlocksR(f, row);
			//internalFaces[Number/3].rotate(Math.PI/2, center, axis);
		}
	}

	/**
	 * 上のTにおいて、回す角度をPI/2*rotとした操作。
	 * ここでrot = 1,2,3が好ましい。
	 */
	public void T(int Number, int rot){
		for (int i = 0; i < rot; i++)
			T(Number);
	}

	/**
	 * 上のTにおいて、回す角度をthetaとした操作。
	 */
	public void T(int Number, double theta){
		int f = 2*(Number / 3);
		int row = Number % 3;
		Point axis = new Point(0,0,0);
		switch (f){
			case 0: axis = axis1;break;
			case 2: axis = axis2;break;
			case 4: axis = axis3;break;
		}
		if (row == 0){    // top row
			int top_f = (f + 4) % 6;
			slideBlocksR(f, row, theta);
			rotateBlocksL(top_f, theta);
		}
		else if (row == 3-1){   // bottom row
			int bottom_f = (f + 5) % 6;
			slideBlocksR(f, row, theta);
			rotateBlocksR(bottom_f, theta);
		}
		else{   // middle row
			slideBlocksR(f, row, theta);
			//internalFaces[Number/3].rotate(Math.PI/2, center, axis);
		}
	}

	protected void slideBlocksR(int f, int row){
		// row slide to the right f -> f2 -> f3 -> f4 -> f
		// where f is face0, face2 or face4
		int f2 = (f + 3) % 6;
		int f3 = f + 1;
		int f4 = (f + 2) % 6;
		Point axis = new Point(0,0,0);
		switch (f){
			case 0: axis = axis1;break;
			case 2: axis = axis2;break;
			case 4: axis = axis3;break;
		}
		for (int i = 0; i < 3; i++){
			Face temp = outerFaces[f][row][i];
			outerFaces[f][row][i].rotate(Math.PI/2, center, axis);      outerFaces[f][row][i] = outerFaces[f4][3-1-i][row];
			outerFaces[f4][2-i][row].rotate(Math.PI/2, center, axis);   outerFaces[f4][3-1-i][row] = outerFaces[f3][i][3-1-row];
			outerFaces[f3][i][2-row].rotate(Math.PI/2, center, axis);   outerFaces[f3][i][3-1-row] = outerFaces[f2][3-1-row][3-1-i];
			outerFaces[f2][2-row][2-i].rotate(Math.PI/2, center, axis); outerFaces[f2][3-1-row][3-1-i] = temp;
		}
	}
	protected void slideBlocksR(int f, int row, double theta){
		// row slide to the right f -> f2 -> f3 -> f4 -> f
		// where f is face0, face2 or face4
		int f2 = (f + 3) % 6;
		int f3 = f + 1;
		int f4 = (f + 2) % 6;
		Point axis = new Point(0,0,0);
		switch (f){
			case 0: axis = axis1;break;
			case 2: axis = axis2;break;
			case 4: axis = axis3;break;
		}
		for (int i = 0; i < 3; i++){
			outerFaces[f][row][i].rotate(theta, center, axis);
			outerFaces[f4][2-i][row].rotate(theta, center, axis);
			outerFaces[f3][i][2-row].rotate(theta, center, axis);
			outerFaces[f2][2-row][2-i].rotate(theta, center, axis);
		}
	}

	protected void slideBlocksL(int f, int row){
		// row slide to the left f -> f4 -> f3 -> f2 -> f
		// where f is face0, face2 or face4
		int f2 = (f + 3) % 6;
		int f3 = f + 1;
		int f4 = (f + 2) % 6;
		Point axis = new Point(0,0,0);
		switch (f){
			case 0: axis = axis1;break;
			case 2: axis = axis2;break;
			case 4: axis = axis3;break;
		}
		for (int i = 0; i < 3; i++){
			Face temp = outerFaces[f][row][i];
			outerFaces[f][row][i].rotate(-Math.PI/2, center, axis); outerFaces[f][row][i] = outerFaces[f2][3-1-row][3-1-i];
			outerFaces[f2][2-row][2-i].rotate(-Math.PI/2, center, axis); outerFaces[f2][3-1-row][3-1-i] = outerFaces[f3][i][3-1-row];
			outerFaces[f3][i][2-row].rotate(-Math.PI/2, center, axis); outerFaces[f3][i][3-1-row] = outerFaces[f4][3-1-i][row];
			outerFaces[f4][2-i][row].rotate(-Math.PI/2, center, axis); outerFaces[f4][3-1-i][row] = temp;
		}
	}
	protected void slideBlocksL(int f, int row, double theta){
		// row slide to the left f -> f4 -> f3 -> f2 -> f
		// where f is face0, face2 or face4
		int f2 = (f + 3) % 6;
		int f3 = f + 1;
		int f4 = (f + 2) % 6;
		Point axis = new Point(0,0,0);
		switch (f){
			case 0: axis = axis1;break;
			case 2: axis = axis2;break;
			case 4: axis = axis3;break;
		}
		for (int i = 0; i < 3; i++){
			outerFaces[f][row][i].rotate(-theta, center, axis);
			outerFaces[f2][2-row][2-i].rotate(-theta, center, axis);
			outerFaces[f3][i][2-row].rotate(-theta, center, axis);
			outerFaces[f4][2-i][row].rotate(-theta, center, axis);
		}
	}

	protected void rotateBlocksL(int f){
		// left rotation of face f
		// rotate outside blocks and then inside blocks
		Point axis = new Point(0,0,0);
		switch (f){
			case 0: axis = axis2;break;  
			case 1: axis = axis2.minus();break;
			case 2: axis = axis3;break;  
			case 3: axis = axis3.minus();break;
			case 4: axis = axis1;break;  
			case 5: axis = axis1.minus();break;
		}
		for (int i = 0; i < 3/2; i++){
			for (int j = i; j < 3-1-i; j++){
				Face temp = outerFaces[f][i][j];
				outerFaces[f][i][j].rotate(Math.PI/2, center, axis); outerFaces[f][i][j] = outerFaces[f][j][3-1-i];
				outerFaces[f][j][2-i].rotate(Math.PI/2, center, axis); outerFaces[f][j][3-1-i] = outerFaces[f][3-1-i][3-1-j];
				outerFaces[f][2-i][2-j].rotate(Math.PI/2, center, axis); outerFaces[f][3-1-i][3-1-j] = outerFaces[f][3-1-j][i];
				outerFaces[f][2-j][i].rotate(Math.PI/2, center, axis); outerFaces[f][3-1-j][i] = temp;
			}
		}
	}
	protected void rotateBlocksL(int f, double theta){
		// left rotation of face f
		// rotate outside blocks and then inside blocks
		Point axis = new Point(0,0,0);
		switch (f){
			case 0: axis = axis2;break;  
			case 1: axis = axis2.minus();break;
			case 2: axis = axis3;break;  
			case 3: axis = axis3.minus();break;
			case 4: axis = axis1;break;  
			case 5: axis = axis1.minus();break;
		}
		for (int i = 0; i < 3/2; i++){
			for (int j = i; j < 3-1-i; j++){
				outerFaces[f][i][j].rotate(theta, center, axis);
				outerFaces[f][j][2-i].rotate(theta, center, axis);
				outerFaces[f][2-i][2-j].rotate(theta, center, axis);
				outerFaces[f][2-j][i].rotate(theta, center, axis);
			}
		}
		outerFaces[f][3/2][3/2].rotate(theta, center, axis);
	}

	protected void rotateBlocksR(int f){
		// right rotation of face f
		// rotate outside blocks and then inside blocks
		Point axis = new Point(0,0,0);
		switch (f){
			case 0: axis = axis2.minus();break;  
			case 1: axis = axis2;break;
			case 2: axis = axis3.minus();break;  
			case 3: axis = axis3;break;
			case 4: axis = axis1.minus();break;  
			case 5: axis = axis1;break;
		}
		for (int i = 0; i < 3/2; i++){
			for (int j = i; j < 3-1-i; j++){
				Face temp = outerFaces[f][i][j];
				outerFaces[f][i][j].rotate(Math.PI/2, center, axis); outerFaces[f][i][j] = outerFaces[f][3-1-j][i];
				outerFaces[f][2-j][i].rotate(Math.PI/2, center, axis); outerFaces[f][3-1-j][i] = outerFaces[f][3-1-i][3-1-j];
				outerFaces[f][2-i][2-j].rotate(Math.PI/2, center, axis); outerFaces[f][3-1-i][3-1-j] = outerFaces[f][j][3-1-i];
				outerFaces[f][j][2-i].rotate(Math.PI/2, center, axis); outerFaces[f][j][3-1-i] = temp;
			}
		}
	}
	protected void rotateBlocksR(int f, double theta){
		// right rotation of face f
		// rotate outside blocks and then inside blocks
		Point axis = new Point(0,0,0);
		switch (f){
			case 0: axis = axis2.minus();break;  
			case 1: axis = axis2;break;
			case 2: axis = axis3.minus();break;  
			case 3: axis = axis3;break;
			case 4: axis = axis1.minus();break;  
			case 5: axis = axis1;break;
		}
		for (int i = 0; i < 3/2; i++){
			for (int j = i; j < 3-1-i; j++){
				outerFaces[f][i][j].rotate(theta, center, axis);
				outerFaces[f][2-j][i].rotate(theta, center, axis);
				outerFaces[f][2-i][2-j].rotate(theta, center, axis);
				outerFaces[f][j][2-i].rotate(theta, center, axis);
			}
		}
		outerFaces[f][3/2][3/2].rotate(theta, center, axis);
	}

	/**
	 * 前面の時計回りの90°回転
	 */
	public void F(){ T(3, 3); }

	/**
	 * 後面の時計回りの90°回転
	 */
	public void B(){ T(5); }

	/**
	 * 左面の時計回りの90°回転
	 */
	public void L(){ T(6, 3); }

	/**
	 * 右面の時計回りの90°回転
	 */
	public void R(){ T(8); }

	/**
	 * 上面の時計回りの90°回転
	 */
	public void U(){ T(0, 3); }

	/**
	 * 下面の時計回りの90°回転
	 */
	public void D(){ T(2); }

	/**
	 * Tを行い、キューブを再描画
	 */
	public void TDraw(int Number){
		if (pane != null){
			T(Number);
			pane.drawShapes();
		}
	}

	/**
	 * Tをrot回行い、キューブを再描画
	 */
	public void TDraw(int Number, int rot){
		if (pane != null){
			T(Number, rot);
			pane.drawShapes();
		}
	}

	/**
	 * Tでtheta(rad)回転させ、キューブを再描画
	 */
	public void TDraw(int Number, double theta){
		if (pane != null){
			T(Number, theta);
			pane.drawShapes();
		}
	}

	public void FDraw(){
		if (pane != null){
			F();
			pane.drawShapes();
		}
	}

	public void BDraw(){
		if (pane != null){
			B();
			pane.drawShapes();
		}
	}

	public void LDraw(){
		if (pane != null){
			L();
			pane.drawShapes();
		}
	}

	public void RDraw(){
		if (pane != null){
			R();
			pane.drawShapes();
		}
	}

	public void UDraw(){
		if (pane != null){
			U();
			pane.drawShapes();
		}
	}

	public void DDraw(){
		if (pane != null){
			D();
			pane.drawShapes();
		}
	}

	@Override
	public void transformTo(Shape3D rubik){
		if (rubik instanceof RubikCubeShape){
			super.transformTo(rubik);
			setAxis1(((RubikCubeShape)rubik).getAxis1());
			setAxis2(((RubikCubeShape)rubik).getAxis2());
			setAxis3(((RubikCubeShape)rubik).getAxis3());
		}
	}

	@Override
	public void parallelMove(double dx, double dy, double dz){
		for (int f = 0; f < 6; f++)
			for (int i = 0; i < 3; i++)
				for (int j = 0; j < 3; j++)
					outerFaces[f][i][j].parallelMove(dx,dy,dz);
	}

	@Override
	public void parallelMove(Point p){
		for (int f = 0; f < 6; f++)
			for (int i = 0; i < 3; i++)
				for (int j = 0; j < 3; j++)
					outerFaces[f][i][j].parallelMove(p);
	}

	@Override
	public void rotateX(double theta){
		super.rotateX(theta);
		axis1.rotateX(theta);
		axis2.rotateX(theta);
		axis3.rotateX(theta);
	}

	@Override
	public void rotateY(double theta){
		super.rotateY(theta);
		axis1.rotateY(theta);
		axis2.rotateY(theta);
		axis3.rotateY(theta);
	}

	@Override
	public void rotateZ(double theta){
		super.rotateZ(theta);
		axis1.rotateZ(theta);
		axis2.rotateZ(theta);
		axis3.rotateZ(theta);
	}

	@Override
	public void rotate(double theta, Point p, Point v){
		super.rotate(theta, p, v);
		axis1.rotate(theta, p, v);
		axis2.rotate(theta, p, v);
		axis3.rotate(theta, p, v);
	}

	/*
	public void moveDraw(double dx, double dy, double dz){
		if (pane != null){
			parallelMove(dx,dy,dz);
			pane.drawShapes();
		}
	}

	public void moveDraw(Point v){
		if (pane != null){
			parallelMove(v);
			pane.drawShapes();
		}
	}

	public void rotateXDraw(double theta){
		if (pane != null){
			rotateX(theta);
			pane.drawShapes();
		}
	}

	public void rotateYDraw(double theta){
		if (pane != null){
			rotateY(theta);
			pane.drawShapes();
		}
	}

	public void rotateZDraw(double theta){
		if (pane != null){
			rotateZ(theta);
			pane.drawShapes();
		}
	}

	public void rotateDraw(double theta, Point p, Point v){
		if (pane != null){
			rotate(theta, p, v);
			pane.drawShapes();
		}
	}*/
}
