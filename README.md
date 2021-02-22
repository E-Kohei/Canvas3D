# Canvas3D

<img src="https://github.com/E-Kohei/Canvas3D/blob/main/RubikCubeExample.png" alt="RubikCube"/>


JavaFXを使った、シンプルな3Dドローイングライブラリ


要件
----
JavaFX > 11



使い方
---
1. ```Face```クラスを使用して、```Shape3D```オブジェクトを作成。
```Java
Face f1 = new Face(new Point(1,1,0), new Point(-1,1,0), new Point(0,-1,0));
Face f2 = new Face(new Point(1,1,0), new Point(-1,1,0), new Point(0,0,1));
Face f3 = new Face(new Point(-1,1,0), new Point(0,-1,0), new Point(0,0,1));
Face f4 = new Face(new Point(0,-1,0), new Point(1,1,0), new Point(0,0,1));
Shape3D tetra = new Shape3D(f1, f2, f3, f4);
```

2. ```Pane3D```オブジェクトを作成。ここで、カメラの位置、向き、焦点距離なども設定できる。
```Java
Pane3D pane3d = new Pane3D();

pane3d.setCameraPoint(new Point(2,2,2));
// use Point as Vector
pane3d.setCameraVector(new Point(-1,-1,-1));
pane3d.setRightVector(new Point(-1,1,0));
pane3d.setfd(1);
```

3. 最後に```Pane3D```オブジェクトに、```Shape3D```オブジェクトを登録
```Java
pane3d.add(tetra);
```

4. JavaFXを使うため、モジュールを指定して、コンパイル、実行！
```shell
javac --module-path PATH_TO_FX --add-module javafx.controls Program.java
java --module-path PATH_TO_FX --add-module javafx.controls Program
```



おまけ　ルービックキューブ
--------------------------
1. javafxのパスを指定してコンパイル。Windowsでは、```-encoding UTF-8```オプションが必要かもしれない。
```shell
javac --module-path PATH_TO_FX --add-module javafx.controls RubikCube3D.java
```
2. 同じオプションでjavaを実行。
```
java --module-path PATH_TO_FX --add-module javafx.controls RubikCube3D
```
