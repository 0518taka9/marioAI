package ch.idsia.agents;

import ch.idsia.agents.controllers.BasicMarioAIAgent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;
import java.util.Random;

/**
 * Created by nakatsukatakahiro on 2017/10/27.
 */
public class AStarAgent extends BasicMarioAIAgent implements Agent, Cloneable {

    static String name = "AStarAgent";

    // マリオのactionを格納する配列
    public byte[] actions;

    // actionsのインデックス
    private int actionIndex = 0;

    // ダメージを受けた時のインデックス
    private int damagedIndex = 0;

    // 直前のdistance
    private int preDist = 0;

    // 同じ場所に居続けたインデックス数
    private int samePlace = 0;

    // 行き詰まって死んだ時のインッデクス
    private int deadEndIndex = 0;

    // 行き詰まって死んだかどうかを表す
    private boolean isDeadEnd = false;

    /* コンストラクタ */
    public AStarAgent() {
        super(name);
        actions = new byte[3000];
    }

    public boolean[] getAction() {

        byte act;

        // actionが確定していればそれを読み込む
        // そうでなければランダムに決定しactionsに格納
        if (actions[actionIndex] != 0) {
            act = actions[actionIndex];
        } else {
            act = setRandomAction();
            actions[actionIndex] = act;
        }

        // actの値からactionを決定
        for (int i = 0; i < Environment.numberOfKeys; i++) {
            action[i] = (act % 2 == 1);
            act /= 2;
        }

        // マリオが初ダメージを受けた時のactionIndexをdamagedIndexに格納
        if (damagedIndex == 0 && marioMode != 2) damagedIndex = actionIndex;

        // 進んでない場合
        if (distancePassedCells <= preDist) {
            samePlace++;
            if (samePlace > 15 && !isDeadEnd) {
                isDeadEnd = true;
                deadEndIndex = actionIndex;
            } else if (samePlace > 7) {
                action[Mario.KEY_RIGHT] = true;
            }
        } else {
            samePlace = 0;
            preDist = distancePassedCells;
        }

        actionIndex++;

        return action;
    }

    /*
     * actionをランダムに決める
     * 0〜31の整数値
     * action[0] = left 1
     * action[1] = right 2
     * action[2] = down 4
     * action[3] = jump 8
     * action[4] = speed 16
     */
    private byte setRandomAction() {
        Random r = new Random();
        byte act = 0;
        int rnd = r.nextInt(99);
        if (rnd < 10) act += 1;                  // left
        else if (rnd >= 20) act += 2;            // right
//        if (r.nextInt(99) < 10) act += 4;      // down
        if (r.nextInt(99) < 80) act += 8;     // jump
        if (r.nextInt(99) < 70) act += 16;    // speed
        return act;
    }

    byte[] getActions() {
        return actions;
    }

    void setActions(byte[] actions) {
        this.actions = actions;
    }

    int getActionIndex() {
        return actionIndex;
    }

    int getDamagedIndex() {
        return damagedIndex;
    }

    boolean getIsDeadEnd() {
        return isDeadEnd;
    }

    int getDeadEndIndex() {
        return deadEndIndex;
    }

    @Override
    public AStarAgent clone() {

        AStarAgent res;
        try {
            res = (AStarAgent) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
        return res;
    }

}
