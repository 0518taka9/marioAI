package ch.idsia.agents;

import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.GeneralizerLevelScene;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.evolution.Evolvable;

import java.util.Random;

/* シフト演算子 x << y
* xをyビットだけ左シフトする。空いた下位ビット(右側)には0を挿入。
*/
public class OwnGAAgent extends BasicMarioAIAgent
        implements Agent, Evolvable, Comparable, Cloneable {

    static String name = "GAAgent";

    /* 遺伝子情報 */
    public byte[] gene;

    /* 各個体の評価値保存用変数 */
    public int fitness;

    /* 環境から取得する入力数 */
    public int inputNum = 19;

    /* 乱数用変数 r */
    private Random r = new Random();

    /* コンストラクタ */
    public OwnGAAgent() {

        super(name);

		/* 16ビットの入力なので，65536(=2^16)個用意する */
        gene = new byte[(1 << inputNum)];

		/* geneの初期化 */
        for (int i = 0; i < gene.length; i++) {
            switch (r.nextInt(8)) {
                case 0:
                    gene[i] = 0;
                    break;
                case 1:
                    gene[i] = 2;
                    break;
                case 2:
                    gene[i] = 8;
                    break;
                case 3:
                    gene[i] = 10;
                    break;
                case 4:
                    gene[i] = 16;
                    break;
                case 5:
                    gene[i] = 18;
                    break;
                case 6:
                    gene[i] = 24;
                    break;
                case 7:
                    gene[i] = 26;
                    break;
            }
        }
        /* 評価値を0で初期化 */
        fitness = 0;
    }

	/* compfit()追加記述 */

    private int distance;

    public void setFitness(int fitness) {
        this.fitness = fitness;
    }

    public int getFitness() {
        return fitness;
    }

    /* 降順にソート */
    public int compareTo(Object obj) {
        OwnGAAgent otherUser = (OwnGAAgent) obj;
        return -(this.fitness - otherUser.getFitness());
    }

	/* compFit()追加記述ここまで */


	int jumpCount = 0;
	boolean jumping = false;

    public boolean[] getAction() {

        int r = marioEgoRow;
        int c = marioEgoCol;
        int input = 0;

        // ジャンプ長押し
        if (jumping) {
            action[Mario.KEY_JUMP] = true;
            jumpCount++;
            if (jumpCount == 5) {
                jumping = false;
                jumpCount = 0;
            }
            return action;
        }

        /* 遺伝アルゴリズム */

		/* 環境情報から input を決定
         * 上位ビットから周辺近傍の状態を格納していく
		 */

		/* enemies情報(上位7桁) */
        input += isEnemy(-1, -1) * (1 << 15);
        input += isEnemy(0, -1) * (1 << 14);
        input += isEnemy(1, -1) * (1 << 13);
        input += isEnemy(-1, 0) * (1 << 12);
        input += isEnemy(-1, 1) * (1 << 11);
        input += isEnemy(0, 1) * (1 << 10);
        input += isEnemy(1, 1) * (1 << 9);

        /* levelScene情報 */
        // 4-2
        input += isObstacle(-1, 2) * (1 << 18);
        input += isObstacle(-2, 1) * (1 << 17);
        input += isObstacle(-2, 2) * (1 << 16);

        input += isObstacle(-1, -1) * (1 << 8);
        input += isObstacle(0, -1) * (1 << 7);
        input += isObstacle(1, -1) * (1 << 6);
        input += isObstacle(-1, 0) * (1 << 5);
        input += isObstacle(-1, 1) * (1 << 4);
        input += isObstacle(0, 1) * (1 << 3);
        input += isObstacle(1, 1) * (1 << 2);

        input += (isMarioOnGround ? 1 : 0) * (1 << 1);
        input += (isMarioAbleToJump ? 1 : 0) * (1 << 0);

		/* input から output(act)を決定する */
        int act = gene[input];    //遺伝子のinput番目の数値を読み取る
        for (int i = 0; i < Environment.numberOfKeys; i++) {
            action[i] = (act % 2 == 1);    //2で割り切れるならtrue
            act /= 2;
        }

        /* ルールベース */

        // 左を押していなければ右を押す
        if (!action[Mario.KEY_LEFT])
            action[Mario.KEY_RIGHT] = true;

        // 目の前に障害物があればジャンプ
        if (isObstacle2(r, c + 1) || isObstacle2(r, c + 2)) {
//            action[Mario.KEY_JUMP] = isMarioAbleToJump || !isMarioOnGround;
            // 4-2
            jumping = true;
        }

        // 目の前に穴があればジャンプ
        if (isHole(r, c + 1)) action[Mario.KEY_JUMP] = isMarioAbleToJump || !isMarioOnGround;

        // 目の前に敵がいる場合ジャンプで回避(右)
        if (action[Mario.KEY_RIGHT] && getEnemiesCellValue(r, c + 1) != 0
                || getReceptiveFieldCellValue(r, c + 2) != 0
                ) {
            action[Mario.KEY_JUMP] = isMarioAbleToJump;
        }

        // 4-1で崖を超えるための記述
//        if (distancePassedCells >= 95 && distancePassedCells < 127) {
//            action[Mario.KEY_RIGHT] = true;
//            action[Mario.KEY_SPEED] = false;
//        }
//        if (distancePassedCells >= 98 && distancePassedCells < 127) {
//            action[Mario.KEY_JUMP] = isMarioAbleToJump || !isMarioOnGround;
//        }
//        if (distancePassedCells >= 105 && distancePassedCells < 127) {
//            action[Mario.KEY_SPEED] = true;
//        }

        // 4-2で穴を超えるための記述
        if (distancePassedCells >= 108 && distancePassedCells < 117) {
            action[Mario.KEY_JUMP] = false;
            action[Mario.KEY_RIGHT] = true;
            action[Mario.KEY_SPEED] = true;
            action[Mario.KEY_LEFT] = false;
            action[Mario.KEY_DOWN] = false;
        }
        if (distancePassedCells >= 117 && distancePassedCells < 127) {
            action[Mario.KEY_JUMP] = true;
            action[Mario.KEY_RIGHT] = true;
            action[Mario.KEY_SPEED] = true;
            action[Mario.KEY_LEFT] = false;
            action[Mario.KEY_DOWN] = false;
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

    private boolean isObstacle2(int r, int c) {
        // (r, c)に障害物があればtrueを返す
        return getReceptiveFieldCellValue(r, c) == GeneralizerLevelScene.BRICK
                || getReceptiveFieldCellValue(r, c) == GeneralizerLevelScene.BORDER_CANNOT_PASS_THROUGH
                || getReceptiveFieldCellValue(r, c) == GeneralizerLevelScene.FLOWER_POT_OR_CANNON;
    }

    private boolean isHole(int r, int c) {
        // (r, c)に穴があればtrueを返す
        int i = 1;
        while (i <= 9) {
            if (getReceptiveFieldCellValue(r + i, c) != 0)
                return false;
            i++;
        }
        return true;
    }

    byte getGene(int i) {
        return gene[i];
    }

    void setGene(int j, byte gene) {
        this.gene[j] = gene;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getDistance() {
        return distance;
    }


    @Override
    public Evolvable getNewInstance() {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public OwnGAAgent clone() {

        OwnGAAgent res;
        try {
            res = (OwnGAAgent) super.clone();
        } catch (CloneNotSupportedException e) {
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
