package ch.idsia.agents;

import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.GeneralizerLevelScene;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.evolution.Evolvable;

import java.util.Random;

 /* シフト演算子 x << y
 * xをyビットだけ左シフトする。空いた下位ビット(右側)には0を挿入。
 */
public class OwnGGAgent extends BasicMarioAIAgent
        implements Agent,Evolvable,Comparable,Cloneable{

    static String name = "GAAgent";

    /* 遺伝子情報 */
    public byte[] gene;

    /* 各個体の評価値保存用変数 */
    public int fitness;

    /* 環境から取得する入力数 */
    public int inputNum = 24;

    /* 乱数用変数 r */
    Random r = new Random();

    /* コンストラクタ */
    public OwnGGAgent(){

        super(name);

		/* 16ビットの入力なので，65536(=2^16)個用意する */
        gene = new byte[(1 << inputNum)];

		/* 出力は32(=2^5)パターン */
        int num = 1 << (Environment.numberOfKeys -1);

        int flag = 1;

		/* geneの初期値は乱数(0から31)で取得 */
        for(int i=0; i<gene.length; i++){
            if(flag == 1){
                switch(r.nextInt(8)){
                    case 0:
                        gene[i] = 0; break;
                    case 1:
                        gene[i] = 2; break;
                    case 2:
                        gene[i] = 8; break;
                    case 3:
                        gene[i] = 10; break;
                    case 4:
                        gene[i] = 16; break;
                    case 5:
                        gene[i] = 18; break;
                    case 6:
                        gene[i] = 24; break;
                    case 7:
                        gene[i] = 26; break;

                }
            }else{
                gene[i]=(byte)r.nextInt(num);
            }


//			gene[i] = (byte)r.nextInt(17);
        }

		/* 評価値を0で初期化 */
        fitness = 0;


    }

	/* compfit()追加記述 */

    int distance;

    public void setFitness(int fitness){
        this.fitness = fitness;
    }

    public int getFitness(){
        return fitness;
    }

    /* 降順にソート */
    public int compareTo(Object obj){
        OwnGGAgent otherUser = (OwnGGAgent) obj;
        return -(this.fitness - otherUser.getFitness());
//        return -(this.distance - otherUser.getDistance());
    }

	/* compFit()追加記述ここまで */


    public boolean[] getAction(){

        int input = 0;

		/* 環境情報から input を決定
		 * 上位ビットから周辺近傍の状態を格納していく
		 */

		// 穴があるかどうか判定
        input += isObstacle(8, 1) * (1 << 16);
        input += isObstacle(8, 2) * (1 << 19);

        // 3マス先
        input += isObstacle(0, 3) * (1 << 21);
        input += isObstacle(-1, 3) * (1 << 20);
        input += isObstacle(-2, 3) * (1 << 23);

        // 2マス先
        input += isObstacle(0, 2) * (1 << 18);
        input += isObstacle(-1, 2) * (1 << 17);
        input += isObstacle(-2, 2) * (1 << 22);

        // 下がブロックかどうか
//        input += isBrick(1, 0) * (1 << 16);

		/* enemies情報(上位7桁) */
        input += isEnemy(-1, -1) * (1 << 15);
        input += isEnemy(0, -1) * (1 << 14);
        input += isEnemy(1, -1) * (1 << 13);
        input += isEnemy(-1, 0) * (1 << 12);
        input += isEnemy(-1, 1)* (1 << 11);
        input += isEnemy(0, 1) * (1 << 10);
        input += isEnemy(1, 1)* (1 << 9);

		/* levelScene情報 */

//        input += isObstacle(-3, 2) * (1 << 19);
//        input += isObstacle(-2, 2) * (1 << 18);
//        input += isObstacle(-1, 2) * (1 << 17);
//        input += isObstacle(0, 2) * (1 << 16);
//        input += isObstacle(1, 2) * (1 << 15);
//        input += isObstacle(2, 2) * (1 << 14);
//
//        input += isObstacle(-3, 1) * (1 << 13);
//        input += isObstacle(-2, 1) * (1 << 12);
//        input += isObstacle(2, 1) * (1 << 11);
//
//        input += isObstacle(-3, 0) * (1 << 10);
//        input += isObstacle(-2, 0) * (1 << 9);


        // 周囲7マス
        input += isObstacle(-1, -1) * (1 << 8);
        input += isObstacle(0, -1) * (1 << 7);
        input += isObstacle(1, -1) * (1 << 6);
        input += isObstacle(-1, 0) * (1 << 5);
        input += isObstacle(-1, 1)* (1 << 4);
        input += isObstacle(0, 1) * (1 << 3);
        input += isObstacle(1, 1)* (1 << 2);

        input += (isMarioOnGround ? 1: 0) * (1 << 1);
        input += (isMarioAbleToJump ? 1: 0) * (1 << 0);

//		System.out.println("enemies : "+ isEnemy(0, 1));

		/* input から output(act)を決定する */
        int act = gene[input];	//遺伝子のinput番目の数値を読み取る
        for(int i=0; i<Environment.numberOfKeys; i++){
            action[i] = (act %2 == 1);	//2で割り切れるならtrue
            act /= 2;
        }

        return action;
    }

    private double isEnemy(int x, int y) {
        int realX = x + 9;
        int realY = y + 9;
        return (getEnemiesCellValue(realX, realY) != 0) ? 1 : 0;
    }

    private double isObstacle(int x, int y) {
        int realX = x + 9;
        int realY = y + 9;
        return (getReceptiveFieldCellValue(realX, realY) < 0) ? 1 : 0;
    }

    private double isBrick(int x, int y) {
        int realX = x + 9;
        int realY = y + 9;
        return (getReceptiveFieldCellValue(realX, realY) == GeneralizerLevelScene.BRICK) ? 1 : 0;
    }

//    private double isAbleToGo() {
//
//    }

    public byte getGene(int i){
        return gene[i];
    }

    public void setGene(int j,byte gene){
        this.gene[j] = gene;
    }

    public void setDistance(int distance){
        this.distance = distance;
    }
    public int getDistance(){
        return distance;
    }


    @Override
    public Evolvable getNewInstance() {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public OwnGGAgent clone(){

        OwnGGAgent res = null;
        try{
            res = (OwnGGAgent) super.clone();
        }catch(CloneNotSupportedException e){
            throw new InternalError(e.toString());
        }

        return res;
    }

    @Override
    public void mutate() {
        // TODO 自動生成されたメソッド・スタブ

    }

    @Override
    public Evolvable copy() {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }


}
