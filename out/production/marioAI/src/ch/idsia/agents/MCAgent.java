package ch.idsia.agents;
import java.util.*;
import java.math.*;

import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.engine.sprites.Sprite;
import ch.idsia.benchmark.mario.environments.*;
import ch.idsia.agents.KeyOfMC;

public class MCAgent extends BasicMarioAIAgent implements Agent{
	static String name = "MCAgent";
	//前方2マスの縦何マスを取得するか
	public static final int width = 3;
	//取り得る行動の数
	public static final int numOfAction = 12;
	//J：ジャンプ　S：ファイア　R：右　L：左　D：下
	/*enum Action{
		J,
		S,
		R,
		L,
		D,
		JS,
		JR,
		JL,
		JD,
		JSR,
		JSL,
		NONE,
	}*/
	//毎フレームもっとも価値の高い行動をするが、確率epsilonで他の行動を等確率で選択
	public static final float epsilon = 0.005f;
	//もっとも良い選択の再現に使用
	private static int frameCounter = 0;
	//毎エピソードで選択した行動を全フレーム分とっておく
	public static List<Integer> actions;
	//学習中にもっとも良かった行動群
	public static List<Integer> best;
	//学習中にもっとも良かったスコア
	public static float bestScore;
	//マリオの周りの状態とマリオが地面についているか
	private static int state = 0;
	//前1マスに崖があるか 0 : ない 1 : ある
	private static int cliff = 0;
	//マリオがジャンプできるか 0 : できない 1 : できる
	private static int ableToJump = 0;
	//毎フレームで貪欲な選択をするかどうか
	public static boolean mode = false;
	//各エピソードで、ある状態である行動を取ったかどうか KeyOfMCはint4つでstate,cliff,ableToJump,action
	//valueのIntegerはこのMCでは使わない
	public static HashMap<KeyOfMC,Integer> selected;
	//行動価値関数　これを基に行動を決める
	public static float[][][][] qValue;
	//各状態行動対におけるそれまで得た報酬の合計
	public static float[][][][] sumValue;
	//ある状態である行動を取った回数
	public static int[][][][] num;
	public static void setMode(boolean b){
		mode = b;
	}
	public static void ini(){
		frameCounter = 0;
		selected.clear();
		actions.clear();
	}
	//使わない
	/*
	public static void setPolicy(){
		for(int i= 0; i < (int)Math.pow(2.0,4 * width + 1); ++i){
			for(int j = 0; j < 2; ++j){
				for(int k = 0; k < 2; ++k){
					float r = (float)(Math.random());
					int idx = 0;
					if(r < epsilon){
						float sum = 0;
						float d = epsilon / (float)numOfAction;
						sum += d;
						while(sum < r){
							sum += d;
							idx++;
						}
					}else{
						float max = -Float.MAX_VALUE;
						for(int t = 0; t < numOfAction; ++t){
							float q = qValue[state][cliff][ableToJump][t];
							if(q > max){
								max = q;
								idx = t;
							}
						}
					}
				}
			}
		}
	}
	*/
	//コンストラクタ
	public MCAgent(){
		super(name);
		qValue = new float[(int)Math.pow(2.0,4 * width + 1)][2][2][numOfAction];
		sumValue = new float[(int)Math.pow(2.0,4 * width  + 1)][2][2][numOfAction];
		num = new int[(int)Math.pow(2.0,4 * width + 1)][2][2][numOfAction];
		selected = new HashMap<KeyOfMC,Integer>();
		for(int i = 0; i < (int)Math.pow(2.0,4 * width + 1); ++i){
			for(int j = 0; j < 2; ++j){
				for(int k = 0; k < 2; ++k){
					for(int t = 0; t < numOfAction; ++t){
						qValue[i][j][k][t] = 0.0f;
						//一応全パターンは1回は試したいのである程度の値は持たせる
						sumValue[i][j][k][t] = 4096.0f;
						num[i][k][k][t] = 1;
					}
				}
			}
		}
		actions = new ArrayList<Integer>();
		best = new ArrayList<Integer>();
	}
	//行動価値関数を取得
	public static float[][][][] getQ(){
		return qValue;
	}
	//行動価値関数を取得
	//学習した後に再現で使う
	public static void setQ(float[][][][] q){
		qValue = q;
	}
	//障害物を検出し、stateの各bitに0,1で格納
	//ここでマリオが得る情報をほとんど決めている
	//ついでにマリオが地面にいるかも取得
	public void detectObstacle(){
		state = 0;
		for(int j = 0; j < width; ++j){
			if(getEnemiesCellValue(marioEgoRow + j - 1,marioEgoCol + 1) != Sprite.KIND_NONE)
				state += (int)Math.pow(2,j);
		}
		for(int j = 0; j < width; ++j){
			if(getReceptiveFieldCellValue(marioEgoRow + j - 1,marioEgoCol + 1) != 0)
				state += (int)Math.pow(2,width + j);
		}
		for(int j = 0; j < width; ++j){
			if(getEnemiesCellValue(marioEgoRow + j - 1,marioEgoCol + 2) != Sprite.KIND_NONE)
				state += (int)Math.pow(2, 2 * width + j);
		}
		for(int j = 0; j < width; ++j){
			if(getReceptiveFieldCellValue(marioEgoRow + j - 1,marioEgoCol + 2) != 0)
				state += (int)Math.pow(2,3 * width + j);
		}
		if(isMarioOnGround)
			state += (int)Math.pow(2, 4 * width);
	}
	//boolをintへ
	public int boolToInt(boolean b){
		return (b) ? 1 : 0;
	}
	//崖検出
	public void detectCliff(){
		
		boolean b = true;
		for(int i = 0; i < 10; ++i){
			if(getReceptiveFieldCellValue(marioEgoRow + i,marioEgoCol + 1) != 0){
				b = false;
				break;
			}
		}
		cliff = (b) ? 1 : 0;
	}
	//ソフトマックス手法
	//使わない
	/*
	public int chooseActionS(){
		float sum = 0.0f;
		int idx = 0;
		for(int i = 0; i < numOfAction; ++i){
			sum += Math.pow(Math.E,qValue[state][cliff][ableToJump][i] / 25f);
		}
		float r = (float)(Math.random());
		float f = 0.0f;
		for(int i = 0; i < numOfAction; ++i){
			f += Math.pow(Math.E,qValue[state][cliff][ableToJump][i] / 25f) / sum;
			if(f > r){
				idx = i;
				break;
			}
		}
		return idx;
	}*/
	//行動価値関数を基に行動選択
	public int chooseAction(){
		float r = (float)(Math.random());
		int idx = 0;
		if(r < epsilon){
			float sum = 0;
			float d = epsilon / (float)numOfAction;
			sum += d;
			while(sum < r){
				sum += d;
				idx++;
			}
		}else{
			float max = -Float.MAX_VALUE;
			for(int i = 0; i < numOfAction; ++i){
				float q = qValue[state][cliff][ableToJump][i];
				if(q > max){
					max = q;
					idx = i;
				}
			}
		}
		return idx;
	}
	//貪欲に行動を選択
	public int chooseActionG(){
		int idx = 0;
		float max = -Float.MAX_VALUE;
		for(int i = 0; i < numOfAction; ++i){
			float q = qValue[state][cliff][ableToJump][i];
			if(q > max){
				max = q;
				idx = i;
			}
		}
		return idx;
	}
	//行動選択前にactionを一旦全部falseにする
	public void clearAction(){
		for(int i = 0; i < Environment.numberOfKeys; ++i){
			action[i] = false;
		}
	}
	//int(0-11)をacitonにする
	public void intToAction(int n){
		if(n == 0 || (n > 4 && n < 11))
			action[Mario.KEY_JUMP] = true;
		if(n == 1 || n == 5 || n == 9 || n == 10)
			action[Mario.KEY_SPEED] = true;
		if(n == 2 || n == 6 || n == 9)
			action[Mario.KEY_RIGHT] = true;
		if(n == 3 || n == 7 || n == 10)
			action[Mario.KEY_LEFT] = true;
		if(n == 4 || n == 8)
			action[Mario.KEY_DOWN] = true;
	}
	public boolean[] getAction(){
		detectObstacle();
		detectCliff();
		ableToJump = boolToInt(isMarioAbleToJump);
		clearAction();
		int currAction = 0;
		if(!mode){
			currAction = chooseAction();
			actions.add(currAction);
			intToAction(currAction);
			if(!selected.containsKey(new KeyOfMC(state,cliff,ableToJump,currAction)))
				selected.put(new KeyOfMC(state,cliff,ableToJump,currAction),1);	
			else
				selected.put(new KeyOfMC(state,cliff,ableToJump,currAction), selected.get(new KeyOfMC(state,cliff,ableToJump,currAction)) + 1);
		}
		else{
			//currAction = chooseActionG();
			if(frameCounter < best.size())
				currAction = best.get(frameCounter);
			intToAction(currAction);
		}
		frameCounter++;
		return action;
	}
}
