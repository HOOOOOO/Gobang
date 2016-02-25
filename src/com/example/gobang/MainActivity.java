package com.example.gobang;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import android.R.color;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements SurfaceHolder.Callback{

	private SurfaceHolder holder;
	private Paint paint;
	private float heightOfSurfaceView;
	private float widthOfSurfaceView;
	static private float startOfX = 40;
	private float endOfX;
	static private float startOfY = 40;
	private float endOfY;
	private Bitmap ai;
	private Bitmap human;
	private float space;
	private int widthOfChess;
	private int[][] cordinate;
	static private int row = 14;
	static private int line;
	private int linesOfY;
	private boolean turnToHuman = true;
	private int human_one = 0;
	private int ai_one = 1;
	private int no_one = 2;
	private int x_min = 0;
	private int x_max;
	private int y_min = 0;
	private int y_max;
	private boolean gameOver = false;
	/*设置搜索的边界值*/
	private int xCor_min = 0;
	private int xCor_max;
	private int yCor_min = 0;
	private int yCor_max;
	private int deep = 3;
	private int weight = 15;
	private boolean first = true;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		paint = new Paint();
		final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
		//surfaceView.setZOrderOnTop(true);
		
		ViewTreeObserver viewTreeObserver = surfaceView.getViewTreeObserver();
		viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				// TODO Auto-generated method stub
				surfaceView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				heightOfSurfaceView = surfaceView.getHeight();
				widthOfSurfaceView = surfaceView.getWidth();
			}
		});
		ai = BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.ai);
		human = BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.human);
		widthOfChess = ai.getWidth();
		
		holder = surfaceView.getHolder();
		holder.setFormat(PixelFormat.TRANSLUCENT);
	    holder.addCallback(this);
	    //Canvas canvas = holder.lockCanvas();
	    cordinate = new int[50][50];
	    surfaceView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					if(turnToHuman && !gameOver){
						float cx = (float) event.getX();
						float cy = (float) event.getY();
						int indexOfX = (int) ((cx - startOfX) / space + 0.5);
					    int indexOfY = (int) ((cy - startOfY) / space + 0.5);
					    //Toast.makeText(MainActivity.this, String.valueOf(indexOfX)+" "+String.valueOf(indexOfY), Toast.LENGTH_SHORT).show();
					    
					    if(cordinate[indexOfY][indexOfX] == no_one && indexOfY <= y_max && indexOfX <= x_max){
					    	cordinate[indexOfY][indexOfX] = human_one;
						    putChess();
						    if(hasWin(indexOfX, indexOfY))
						    {
						    	gameOver = true;
						    	new AlertDialog.Builder(MainActivity.this).setTitle("提示")
								.setMessage("人类胜利").setPositiveButton("确定", null)
								.show();
						    }
						    else{
						        //Toast.makeText(MainActivity.this, String.valueOf(indexOfY)+" "+String.valueOf(indexOfX)+"="+String.valueOf(cordinate[indexOfX][indexOfY]), Toast.LENGTH_SHORT).show();
							    if(first){
							    	if(indexOfX-1 >= x_min)
							    		xCor_min = indexOfX-1;
							    	if(indexOfX+1 <= x_max)
							    		xCor_max = indexOfX+1;
							    	if(indexOfY-1 >= y_min)
							    		yCor_min = indexOfY-1;
							    	if(indexOfY+1 <= y_max)
							    		yCor_max = indexOfY+1;
							    	first = false;
							    }
							    else{
							    	resetMaxMin(indexOfX, indexOfY);
							    }
								turnToHuman = false;
								turnToAi();
						    }
					    }
					}
				}
				return false;
			}
	
		});
	    Button again = (Button) findViewById(R.id.button_again);
		again.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				gameOver = false;
				first = true;
				surfaceCreated(holder);
				for(int i = 0; i < 30; i++)
					for(int j = 0; j < 30; j++)
						cordinate[i][j] = no_one;
			}
		});
	}
    
	private void turnToAi() {
		// TODO Auto-generated method stub
		Point point = new Point();
		point = findForAi();
		cordinate[point.y][point.x] = ai_one;
		putChess();
		if(hasWin(point.x, point.y)){
			gameOver = true;
	    	new AlertDialog.Builder(MainActivity.this).setTitle("提示")
			.setMessage("计算机胜利").setPositiveButton("确定", null)
			.show();
		}
		turnToHuman = true;
	}
	
	//绘制棋子
	private void putChess() {
		Canvas canvas = holder.lockCanvas();
	    drawBackGround(canvas);
	    for(int i = 0; i < 30; i++)
	    	for(int j = 0; j < 30; j++)
	    	{
	    		if(cordinate[j][i] == human_one)
	    		{
	    			float x = startOfX + i * space;
				    float y = startOfY + j * space;
	    			canvas.drawBitmap(human, (float)(x-widthOfChess/2), (float)(y-widthOfChess/2), paint);
	    		}
	    		if(cordinate[j][i] == ai_one)
	    		{
	    			float x = startOfX + i * space;
				    float y = startOfY + j * space;
	    			canvas.drawBitmap(ai, (float)(x-widthOfChess/2), (float)(y-widthOfChess/2), paint);
	    		}
	    	}
	    holder.unlockCanvasAndPost(canvas);
	    holder.lockCanvas(new Rect(0, 0, 0, 0));
		holder.unlockCanvasAndPost(canvas);
	}

	//SurfaceHolder.Callback
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		for(int i = 0; i < 30; i++)
			for(int j = 0; j < 30; j++)
				cordinate[i][j] = no_one;
		endOfX = widthOfSurfaceView - startOfX;
		space = (endOfX - startOfX) / row;
		endOfY = heightOfSurfaceView - startOfY;
		line = row + 1;
		xCor_max = x_max = row;
		linesOfY = (int) ((endOfY - startOfY) / space + 1);
		endOfY = startOfY + space * (linesOfY - 1);
		yCor_max = y_max = linesOfY - 1;
		//Toast.makeText(MainActivity.this, ""+"line:"+line+" "+"x_max:"+x_max+" "+
		//"xCor_max:"+xCor_max+" "+"y_max:"+y_max+" "+"yCor_max:"+yCor_max, Toast.LENGTH_LONG).show();
		//holder.setFormat(PixelFormat.TRANSLUCENT);
		Canvas canvas = holder.lockCanvas();
		drawBackGround(canvas);
		holder.unlockCanvasAndPost(canvas);
		holder.lockCanvas(new Rect(0, 0, 0, 0));
		holder.unlockCanvasAndPost(canvas);
	}
	
	//绘制棋盘
	private void drawBackGround(Canvas canvas) {
		canvas.drawColor(Color.WHITE);
		paint.setColor(Color.BLACK);
		float tempY = startOfY;
		paint.setStrokeWidth(2);
		for(int i = 1; i <= linesOfY; i++)
		{
			if(i == 1 || i == linesOfY)
				paint.setStrokeWidth(5);
			canvas.drawLine(startOfX, tempY, endOfX, tempY, paint);
			paint.setStrokeWidth(2);
			tempY += space;
		}
		float tempX = startOfX;
		for(int i = 1; i <= line; i++)
		{
			if(i == 1 || i == line)
				paint.setStrokeWidth(4);
			canvas.drawLine(tempX, startOfY, tempX, endOfY, paint);
			paint.setStrokeWidth(2);
			tempX += space;
		}
		paint.setStrokeWidth(15);
		int col = linesOfY - 1;
		canvas.drawCircle(startOfX+row/2*space, startOfY+col/2*space, 8, paint);
		canvas.drawCircle(startOfX+(row/2-4)*space, startOfY+(col/2-4)*space, 5, paint);
		canvas.drawCircle(startOfX+(row/2+4)*space, startOfY+(col/2+4)*space, 5, paint);
		canvas.drawCircle(startOfX+(row/2-4)*space, startOfY+(col/2+4)*space, 5, paint);
		canvas.drawCircle(startOfX+(row/2+4)*space, startOfY+(col/2-4)*space, 5, paint);
		canvas.drawCircle(startOfX+(row/2+4)*space, startOfY+(col/2)*space, 5, paint);
		canvas.drawCircle(startOfX+(row/2-4)*space, startOfY+(col/2)*space, 5, paint);
		canvas.drawCircle(startOfX+(row/2)*space, startOfY+(col/2-4)*space, 5, paint);
		canvas.drawCircle(startOfX+(row/2)*space, startOfY+(col/2+4)*space, 5, paint);
	}
    
	//SurfaceHolder.Callback
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	//SurfaceHolder.Callback
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
	}

    /*------------------用剪枝算法寻找最佳下棋位置-----------------*/
	class Point
	{
		public int x;
		public int y;
		
		Point() {
			// TODO Auto-generated constructor stub
			x = 0;
			y = 0;
		}
	}
	
	private Point findForAi() {
		Point point = new Point();
		int x = -1;
		int y = -1;
		int mx = -1000000000;
		int[][] availablePoint = getAvailablePoint(ai_one);
		for(int k = 0; k < availablePoint.length; k++){
			int i = availablePoint[k][0];
			int j = availablePoint[k][1];
			if(getType(i, j, ai_one) == 1){
				x = i;
				y = j;
				break;
			}
			if(getType(i, j, human_one) == 1){
				x = i;
				y = j;
				break;
			}
			int tmp1 = xCor_min, tmp2 = xCor_max, tmp3 = yCor_min, tmp4 = yCor_max;
			cordinate[j][i] = ai_one;
			resetMaxMin(i, j);
			int t = findMin(-100000000, 100000000, deep);
			cordinate[j][i] = no_one;
			xCor_min = tmp1;
			xCor_max = tmp2;
			yCor_min = tmp3;
			yCor_max = tmp4;
			if (t - mx > 1000 || Math.abs(t - mx)<1000 && randomTest(3)) {
	              x = i;
	              y = j;
	              mx = t;
	        }	
		}
		point.x = x;
		point.y = y;
		System.out.println("[" + x + "," + y + "]");
	    // 重设边界值
	    resetMaxMin(x,y);
		return point;
	}
	
	private boolean randomTest(int kt) {
		Random rm = new Random();
	    return rm.nextInt() % kt == 0;
	}

	//---------搜索当前搜索状态极大值--------------------------------//
	//alpha 祖先节点得到的当前最小最大值，用于alpha 剪枝
	//beta  祖先节点得到的当前最大最小值，用于beta 剪枝。
	//step  还要搜索的步数
	//return 当前搜索子树极大值
	protected int findMax(int alpha, int beta, int step) {
		int max = alpha;
	    if (step == 0) {
	    	return evaluate();
	    }
	    int[][] availablePoint = getAvailablePoint(ai_one);
	    for (int i = 0; i < availablePoint.length; i++) {
	    	int x = availablePoint[i][0];
	      	int y = availablePoint[i][1];
	      	if (getType(x, y, ai_one) == 1)   //电脑可取胜
	      		return 100 * ( getMark(1) + step*1000 );
	        cordinate[y][x] = ai_one;
	        // 预存当前边界值
	        int temp1=xCor_min, temp2=xCor_max, temp3=yCor_min, temp4=yCor_max;
	        resetMaxMin(x,y);
	        int t = findMin(max, beta, step - 1);
	        cordinate[y][x] = no_one;
	        // 还原预设边界值
	        xCor_min=temp1;
	        xCor_max=temp2;
	        yCor_min=temp3;
	        yCor_max=temp4;
	        if (t > max)
	        	max = t;
	        //beta 剪枝
	        if (max >= beta) 
	        	return max;
	     }
	     return max;
	}
	
	//-----------------------搜索当前搜索状态极小值---------------------------------//
	//alpha 祖先节点得到的当前最小最大值，用于alpha 剪枝
	//beta  祖先节点得到的当前最大最小值，用于beta 剪枝
	//step  还要搜索的步数
	//return 当前搜索子树极小值。
	protected int findMin(int alpha, int beta, int step) {
		int min = beta;
	    if (step == 0) {
	    	return evaluate();
	    }
	    int[][] availablePoint = getAvailablePoint(human_one);
	    for (int i = 0; i < availablePoint.length; i++) {
	    	int x = availablePoint[i][0];
	        int y = availablePoint[i][1];
	        int type = getType(x, y, human_one);
	        if (type == 1)     					  			//玩家成5
	        	return -120 * ( getMark(1) + step*1000 );
	        // 预存当前边界值
	        int temp1=xCor_min,temp2=xCor_max,temp3=yCor_min,temp4=yCor_max;
	        cordinate[y][x] = human_one;
	        resetMaxMin(x, y);
	        int t = findMax(alpha, min, step-1);
	        cordinate[y][x] = no_one;
	        // 还原预设边界值
	        xCor_min=temp1;
	        xCor_max=temp2;
	        yCor_min=temp3;
	        yCor_max=temp4;
	        if (t < min)
	        	min = t;
	        //alpha 剪枝
	        if (min <= alpha) {
	        	return min;
	        }
	    }
	    return min;
	}

	private int[][] getAvailablePoint(int chessType) {
		// TODO Auto-generated method stub
		int i_min = (xCor_min == 0 ? xCor_min:xCor_min-1);
		int i_max = (xCor_max == (x_max+1) ? xCor_max:xCor_max+1);
		int j_min = (yCor_min == 0 ? yCor_min:yCor_min-1);
		int j_max = (yCor_max == (y_max+1) ? yCor_max:yCor_max+1);
		int [][] tmp = new int[(i_max - i_min)*(j_max - j_min)][3];
		int n = 0;
		int type_1, type_2;
		for(int i = i_min; i < i_max; i++)
			for(int j = j_min; j < j_max; j++)
			{
				if(cordinate[j][i] == no_one)
				{
					tmp[n][0] = i;
					tmp[n][1] = j;
					type_1 = getType(i, j, chessType);
					type_2 = getType(i, j, 1-chessType);
					tmp[n][2] = getMark(type_1) + getMark(type_2);
					n++;
				}
			}
		//if(chessType == ai_one){
		Arrays.sort(tmp, new ArrComparator());
	//	}
		//else{
		//	Arrays.sort(tmp, new ArrComparator2());
		//}
	    int size = weight > n? n:weight;
	    int[][] availablePoint = new int[size][3];
	    System.arraycopy(tmp, 0, availablePoint, 0, size);
	    return availablePoint;
	}

	
	private int getType(int x, int y, int chessType) {
		// TODO Auto-generated method stub
		if(cordinate[y][x] != no_one)
			return -1;
		int [][] types = new int[4][2];
		types[0] = count(x, y, 0, 1, chessType);//竖直
		types[1] = count(x, y, 1, 0, chessType);//水平
		types[2] = count(x, y, -1, 1, chessType);//斜上
		types[3] = count(x, y, 1, 1, chessType);//斜下
		// 各种棋型的方向的数目
	    int longfive = 0;
	    int five_OR_more = 0;
	    int four_died = 0, four_live = 0;
	    int three_died = 0, three_live = 0;
	    int two_died  = 0, two_live = 0;
	    // 各方向上棋型的判别
	    for (int k = 0; k < 4; k++) {
	    	if (types[k][0] > 5) {  
	    		longfive++;              // 长连
	      	    five_OR_more++;
	      	}
	      	else if (types[k][0] == 5)
	      		five_OR_more++;          // 成5
	        else if (types[k][0] == 4 && types[k][1] == 2)
	         	four_live++;             // 活4
	        else if (types[k][0] == 4 && types[k][1] != 2)
	          	four_died++;             // 死4
	        else if (types[k][0] == 3 && types[k][1] == 2)
	          	three_live ++;           // 活3
	        else if (types[k][0] == 3 && types[k][1] != 2)
	          	three_died++;            // 死3
	        else if (types[k][0] == 2 && types[k][1] == 2)
	          	two_live++;              // 活2
	        else if (types[k][0] == 2 && types[k][1] != 2)
	          	two_died++;              // 死2
	        else
	            ;
	   }
	    if (five_OR_more != 0)
	    	return 1;   // 成5
	    if (four_live != 0 || four_died >= 2 || four_died != 0 && three_live != 0)
	        return 2;   // 成活4或者是双死4或者是死4活3
	    if (three_live  >= 2)
	        return 3;   // 成双活3
	    if (three_died != 0 && three_live  != 0)
	        return 4;   // 成死3活3
	    if (four_died != 0)
	        return 5;   // 成死4
	    if (three_live  != 0)
	        return 6;   // 单活3
	    if (two_live >= 2)
	        return 7;   // 成双活2
	    if (three_died != 0)
	        return 8;   // 成死3
	    if (two_live != 0 && two_died != 0)
	        return 9;   // 成死2活2
	    if (two_live != 0)
	        return 10;  // 成活2
	    if (two_died != 0)
	        return 11;  // 成死2
	    return 12;
	}
	
	private int[] count(int x, int y, int ex, int ey, int chessType) {
		// TODO Auto-generated method stub
		if(!makeSense(x, y, ex, ey, chessType))
			return new int[]{0, 1};
		
		int forward_count = 1, reverse_count = 1;
		int totall_count = 1;
		int forward_live = 0, reverse_live = 0;
		int totall_live = 0;
		boolean forward_mid = false, reverse_mid = false;
		int forward_midPosition = 1, reverse_midPosition = 1;
		
		int index;
		//forward search
		for(index = 1; x+index*ex <= x_max && x+index*ex >= x_min && y+index*ey <= y_max && y+index*y >= y_min; index++){
			if(cordinate[y+index*ey][x+index*ex] == chessType)
				forward_count++;
			else if(cordinate[y+index*ey][x+index*ex] == no_one){
				if(!forward_mid){
					forward_mid = true;
					forward_midPosition = index;
				}
				else
					break;
			}
			else 
				break;
		}
		//forward live
		if(x+index*ex <= x_max && x+index*ex >= x_min && y+index*ey <= y_max && y+index*ey >= y_min){
			if(cordinate[y+index*ey][x+index*ex] == no_one){
				forward_live++;
				if(forward_count == forward_midPosition)
					forward_mid = false;
				if(forward_mid && forward_count >= 4 && forward_midPosition <= 3)
					forward_live--;
			}
			else if(cordinate[y+index*ey][x+index*ex] != chessType && index >= 2){
				if(cordinate[y+(index-1)*ey][x+(index-1)*ex] == no_one){
					forward_live++;
					forward_mid = false;
				}
			}
		}
		else if(index >= 2 && cordinate[y+(index-1)*ey][x+(index-1)*ex] == no_one){
			forward_live++;
			forward_mid = false;
		}
		
		//reverse search
		for(index = 1; x-index*ex <= x_max && x-index*ex >= x_min && y-index*ey <= y_max && y-index*ey >= y_min; index++){
			if(cordinate[y-index*ey][x-index*ex] == chessType)
				reverse_count++;
			else if(cordinate[y-index*ey][x-index*ex] == no_one){
				if(!reverse_mid){
					reverse_mid = true;
					reverse_midPosition = index;
				}
				else
					break;
			}
			else 
				break;
		}
		//reverse live
		if(x-index*ex <= x_max && x-index*ex >= x_min && y-index*ey <= y_max && y-index*ey >= y_min){
			if(cordinate[y-index*ey][x-index*ex] == no_one){
				reverse_live++;
				if(reverse_count == reverse_midPosition)
					reverse_mid = false;
				if(reverse_mid && reverse_count >= 4 && reverse_midPosition <= 3)
					reverse_live--;
			}
			else if(cordinate[y-index*ey][x-index*ex] != chessType && index >= 2){
				if(cordinate[y-(index-1)*ey][x-(index-1)*ex] == no_one){
					reverse_live++;
					reverse_mid = false;
				}
			}
		}
		else if(index >= 2 && cordinate[y-(index-1)*ey][x-(index-1)*ex] == no_one){
			reverse_live++;
			reverse_mid = false;
		}
		//分析棋子类型
		//两边都没中空
		if(!forward_mid && !reverse_mid){
			totall_count = forward_count + reverse_count;
			totall_live = forward_live + reverse_live;
			return new int[]{totall_count, totall_live};
		}
		//两边都有空
		else if(forward_mid && reverse_mid){
			int temp = forward_count + reverse_count - 1;
			if(temp >= 5)
				return new int[]{temp, 2};
			if(temp == 4)
				return new int[]{temp, 2};
			if(forward_count + reverse_midPosition - 1 >= 4 || reverse_count + forward_midPosition - 1 >= 4)
				return new int[]{4, 1};//死4
			if(forward_count + reverse_midPosition - 1 == 3 && forward_live > 0 || reverse_count + forward_midPosition - 1 == 3 && reverse_live > 0)
				return new int[]{3, 2};//活3
			return new int[]{3, 1};
		}
		//一边有中空
		else{
			if(forward_count + reverse_count - 1 < 5)
				return new int[]{forward_count + reverse_count - 1, forward_live + reverse_live};
			else{
				if(forward_mid && reverse_count + forward_midPosition - 1 >= 5)
					return new int[]{reverse_count + forward_midPosition - 1, reverse_live+1};
				if(reverse_mid && forward_count + reverse_midPosition - 1 >= 5)
					return new int[]{forward_count + reverse_midPosition - 1, forward_live+1};
				if(forward_mid && (reverse_count + forward_midPosition - 1 == 4 && reverse_live == 1 || forward_midPosition == 4))
					return new int[]{4, 2};
				if(reverse_mid && (forward_count + reverse_midPosition - 1 == 4 && forward_live == 1 || reverse_midPosition == 4))
					return new int[]{4, 2};
				return new int[]{4, 1};
			}
		}
	}

	private int getMark(int k) {
		// TODO Auto-generated method stub
		switch (k) {
	      case 1:                   
	          return 100000;
	      case 2:                   
	          return 30000;
	      case 3:
	          return 5000;
	      case 4:
	          return 1000;
	      case 5:
	          return 500;
	      case 6:
	          return 200;
	      case 7:
	          return 100;
	      case 8:
	          return 50;
	      case 9:
	          return 10;
	      case 10:
	          return 5;
	      case 11:
	          return 3;
	      case 12:
	       	  return 2;
	      default:                     
	          return 0;
	      }
	}
	
	//判断在该方向下棋是否有意义
	private boolean makeSense(int x, int y, int ex, int ey, int chessType) {
		// TODO Auto-generated method stub
		int count = 1;
		for(int i = 1; x+i*ex <= x_max && x+i*ex >= x_min && y+i*ey <= y_max && y+i*ey >= y_min && count < 5; i++){
			if(cordinate[y+i*ey][x+i*ex] != 1-chessType)
				count++;
			else 
				break;
		}
		for(int i = 1; x-i*ex <= x_max && x-i*ex >= x_min && y-i*ey <= y_max && y-i*ey >= y_min && count < 5; i++){
			if(cordinate[y-i*ey][x-i*ex] != 1-chessType)
				count++;
			else
				break;
		}
		return count >= 5;
	}

    //给棋面打分
	private int evaluate(){
		int grade = 0, mt_ai = 1, mt_human = 1;
		if(turnToHuman)
			mt_human = 2;
	    else
	    	mt_ai = 2;
		int i_min = (xCor_min == 0 ? xCor_min:xCor_min-1);
		int i_max = (xCor_max == (x_max+1) ? xCor_max:xCor_max+1);
		int j_min = (yCor_min == 0 ? yCor_min:yCor_min-1);
		int j_max = (yCor_max == (y_max+1) ? yCor_max:yCor_max+1);
		int type;
		for(int i = i_min; i < i_max; i++)
			for(int j = j_min; j < j_max; j++){
				if(cordinate[j][i] == no_one){
					type = getType(i, j, ai_one);
					if(type == 1)
						grade += 30 * mt_ai * getMark(type);
					else if(type == 2)
						grade += 10 * mt_ai * getMark(type);
					else if(type == 3)
						grade += 3 * mt_ai * getMark(type);
					else
						grade += mt_ai * getMark(type);
					type = getType(i, j, human_one);
					if(type == 1)
						grade -= 30 * mt_ai * getMark(type);
					else if(type == 2)
						grade -= 10 * mt_ai * getMark(type);
					else if(type == 3)
						grade -= 3 * mt_ai * getMark(type);
					else
						grade -= mt_ai * getMark(type);
				}
			}
		return grade;
	}
	
	public void resetMaxMin(int x,int y){
		if(x-1 >= 0)
			xCor_min = (xCor_min < x-1 ? xCor_min:x-1);
		if(x+1 <= (x_max+1))
			xCor_max = (xCor_max > x+1 ? xCor_max:x+1);
		if(y-1 >= 0)
			yCor_min = (yCor_min < y-1 ? yCor_min:y-1);
		if(y+1 <= (y_max+1))
			yCor_max = (yCor_max > y+1 ? yCor_max:y+1);
    }
	
	//判断是否赢棋
	private boolean hasWin(int x, int y) {
		int chessType = cordinate[y][x];
		//竖向
		int count = 1;
		for(int i = y+1; i < y+5; i++)
		{
			if(i > y_max)
				break;
			if(cordinate[i][x] != chessType)
				break;
			count++;
		}
		for(int i = y-1; i > y-5; i--)
		{
			if(i < y_min)
				break;
			if(cordinate[i][x] != chessType)
				break;
			count++;
		}
		if(count >= 5)
			return true;
		//横向
		count = 1;
		for(int i = x+1; i < x+5; i++)
		{
			if(i > x_max)
				break;
			if(cordinate[y][i] != chessType)
				break;
			count++;
		}
		for(int i = x-1; i > x-5; i--)
		{
			if(i < x_min)
				break;
			if(cordinate[y][i] != chessType)
				break;
			count++;
		}
		if(count >= 5)
			return true;
		//斜向'/'
		count = 1;
		for(int i = y-1, j = x+1; i > y-5; i--, j++)
		{
			if(i < y_min || j > x_max)
				break;
			if(cordinate[i][j] != chessType)
				break;
			count++;
		}
		for(int i = y+1, j = x-1; i < y+5; i++, j--)
		{
			if(i > y_max || j < x_min)
				break;
			if(cordinate[i][j] != chessType)
				break;
			count++;
		}
		if(count >= 5)
			return true;
		//斜向‘\’
		count = 1;
		for(int i = y+1, j = x+1; i < y+5; i++, j++)
		{
			if(i > y_max || j > x_max)
				break;
			if(cordinate[i][j] != chessType)
				break;
			count++;
		}
		for(int i = y-1, j = x-1; i > y-5; i--, j--)
		{
			if(i < y_min || j < x_min)
				break;
			if(cordinate[i][j] != chessType)
				break;
			count++;
		}
		if(count >= 5)
			return true;
		return false;
	}
}

  /*
 * 排序 Comparator
 */
class ArrComparator implements Comparator<Object> {
	int column = 2;
	int sortOrder = -1; // 递减
	public ArrComparator() {}
	
    public int compare(Object a, Object b) {
    	if (a instanceof int[]) {
    		return sortOrder * (((int[]) a)[column] - ((int[]) b)[column]);
        }
        throw new IllegalArgumentException("param a,b must int[].");
    }
}

class ArrComparator2 implements Comparator<Object> {
	int column = 2;
	int sortOrder = 1; // 递增
	public ArrComparator2() {}
	
    public int compare(Object a, Object b) {
    	if (a instanceof int[]) {
    		return sortOrder * (((int[]) a)[column] - ((int[]) b)[column]);
        }
        throw new IllegalArgumentException("param a,b must int[].");
    }
}

