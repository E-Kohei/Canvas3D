public class RubikCube{
	private int dim;               // dimension of rubik cube (i.e 3x3x3)
	private int[][][] faceTensor;  // condition of face (what color face is)
	// Color constant int
	static final int RED = 0;
	static final int BLUE = 1;
	static final int YELLOW = 2;
	static final int WHITE = 3;
	static final int GREEN = 4;
	static final int ORANGE = 5;

	public RubikCube(int dim){
		this.dim = dim;
		faceTensor = new int[6][dim][dim];
		for (int f = 0; f < 6; f++){
			for (int i = 0; i < dim; i++){
				for (int j = 0; j < dim; j++){
					// f is equals to Color constant int
					faceTensor[f][i][j] = f;
				}
			}
		}
	}

	/**
	 * gets dimension of rubik cube
	 */
	public int getDim(){
		return dim;
	}
	/**
	 * gets tensor that indicates the condition of all faces
	 */
	public int[][][] getFaceTensor(){
		int[][][] copyTensor = new int[6][dim][dim];
		for (int f = 0; f < 6; f++){
			for (int i = 0; i < dim; i++){
				for (int j = 0; j < dim; j++){
					copyTensor[f][i][j] = 
						faceTensor[f][i][j];
				}
			}
		}
		return copyTensor;
	}
	/**
	 * gets matrix that indicates the condition of a face
	 */
	public int[][] getFaceMatrix(int face){
		return getFaceTensor()[face];
	}

/*	public void T_1(){
		// row slide (0 -> 3 -> 1 -> 2)
		for (int i = 0; i < dim; i++){
			int temp = faceTensor[0][0][i];
			faceTensor[0][0][i] = faceTensor[2][dim-1-i][0];
			faceTensor[2][dim-1-i][0] = faceTensor[1][i][dim-1];
			faceTensor[1][i][dim-1] = faceTensor[3][dim-1][dim-1-i];
			faceTensor[3][dim-1][dim-1-i] = temp;
		}
		// left rotation of face 4
		// rotate outside blocks and then inside blocks
		for (int i = 0; i < dim/2; i++){
			for (int j = i; j < dim-1-i; j++){
				int temp2 = faceTensor[4][i][j];
				faceTensor[4][i][j] = faceTensor[4][j][dim-1-i];
				faceTensor[4][j][dim-1-i] =
					faceTensor[4][dim-1-i][dim-1-j];
				faceTensor[4][dim-1-i][dim-1-j] = 
					faceTensor[4][dim-1-j][i];
				faceTensor[4][dim-1-j][i] = temp2;
			}
		}
	}
*/
	public void T(int Number){
		// fundamental transformation of rubik cube
		// slide blocks to the right, and if its row is top or bottom,
		// rotate the top face or the bottom face
		//
		// Number indicates which block-row to move;
		// 0 <= Number <= dim-1 : row_Number of face0
		// dim <= Number <= 2*dim-1 : row_Number of face2
		// 2*dim <= Number <= 3*dim-1 : row_Number of face 4
		int f = 2*(Number / dim);
		int row = Number % dim;
		if (row == 0){    // top row
			int top_f = (f + 4) % 6;
			slideBlocksR(f, row);
			rotateBlocksL(top_f);
		}
		else if (row == dim-1){   // bottom row
			int bottom_f = (f + 5) % 6;
			slideBlocksR(f, row);
			rotateBlocksR(bottom_f);
		}
		else{
			slideBlocksR(f, row);
		}
	}
	public void S(int Number){
		// supplimentary transformation of rubik cube
		// slide blocks to the left, and its row is top or bottom,
		// rotate the top face or the bottom face
		// (this is same as doing T 3 times
		// that's why this transformation is supplimentary)
		//
		// Number indicates which block-row to move;
		// 0 <= Number <= dim-1 : row_Number of face 0
		// dim <= Number <= 2*dim-1 : row_Number of face 2
		// dim <= Number <= 3*dim-1 : row_Number of face 4
		int f = 2*(Number / dim);
		int row = Number % dim;
		if (row == 0){    // top row
			int top_face = (f + 4) % 6;
			slideBlocksL(f, row);
			rotateBlocksR(top_face);
		}
		else if (row == dim-1){   // bottom row
			int bottom_face = (f + 5) % 6;
			slideBlocksL(f, row);
			rotateBlocksL(bottom_face);
		}
		else{
			slideBlocksL(f, row);
		}
	}
	
	protected void slideBlocksR(int f, int row){
		// row slide to the right f -> f2 -> f3 -> f4 -> f
		// where f is face0, face2 or face4
		int f2 = (f + 3) % 6;
		int f3 = f + 1;
		int f4 = (f + 2) % 6;
		for (int i = 0; i < dim; i++){
			int temp = faceTensor[f][row][i];
			faceTensor[f][row][i] = faceTensor[f4][dim-1-i][row];
			faceTensor[f4][dim-1-i][row] =
				faceTensor[f3][i][dim-1-row];
			faceTensor[f3][i][dim-1-row] =
				faceTensor[f2][dim-1-row][dim-1-i];
			faceTensor[f2][dim-1-row][dim-1-i] =
				temp;
		}
	}
	protected void slideBlocksL(int f, int row){
		// row slide to the left f -> f4 -> f3 -> f2 -> f
		// where f is face0, face2 or face4
		int f2 = (f + 3) % 6;
		int f3 = f + 1;
		int f4 = (f + 2) % 6;
		for (int i = 0; i < dim; i++){
			int temp = faceTensor[f][row][i];
			faceTensor[f][row][i] = 
				faceTensor[f2][dim-1-row][dim-1-i];
			faceTensor[f2][dim-1-row][dim-1-i] =
				faceTensor[f3][i][dim-1-row];
			faceTensor[f3][i][dim-1-row] =
				faceTensor[f4][dim-1-i][row];
			faceTensor[f4][dim-1-i][row] = temp;
		}
	}
	protected void rotateBlocksL(int f){
		// left rotation of face f
		// rotate outside blocks and then inside blocks
		for (int i = 0; i < dim/2; i++){
			for (int j = i; j < dim-1-i; j++){
				int temp = faceTensor[f][i][j];
				faceTensor[f][i][j] = faceTensor[f][j][dim-1-i];
				faceTensor[f][j][dim-1-i] =
					faceTensor[f][dim-1-i][dim-1-j];
				faceTensor[f][dim-1-i][dim-1-j] =
					faceTensor[f][dim-1-j][i];
				faceTensor[f][dim-1-j][i] = temp;
			}
		}
	}
	protected void rotateBlocksR(int f){
		// right rotation of face f
		// rotate outside blocks and then inside blocks
		for (int i = 0; i < dim/2; i++){
			for (int j = i; j < dim-1-i; j++){
				int temp = faceTensor[f][i][j];
				faceTensor[f][i][j] = faceTensor[f][dim-1-j][i];
				faceTensor[f][dim-1-j][i] =
					faceTensor[f][dim-1-i][dim-1-j];
				faceTensor[f][dim-1-i][dim-1-j] =
					faceTensor[f][j][dim-1-i];
				faceTensor[f][j][dim-1-i] = temp;
			}
		}
	}

}
