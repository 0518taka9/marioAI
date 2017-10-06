package ch.idsia.agents.controllers;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.GeneralizerLevelScene;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.engine.sprites.Sprite;
import ch.idsia.benchmark.mario.environments.Environment;

/**
 * Created by nakatsukatakahiro on 2017/10/06.
 */
public class OwnAgentTask3 extends BasicMarioAIAgent implements Agent {

    private int jumpCount = 0;
    private boolean jumpFire = false;
    private boolean jumping = false;

    public OwnAgentTask3() {
        super("OwnAgentTask3");
        reset();
    }

    public void reset() {
        action = new boolean[Environment.numberOfKeys];
        action[Mario.KEY_RIGHT] = true;
//        action[Mario.KEY_SPEED] = true;
    }

    public boolean[] getAction() {

        int r = marioEgoRow;
        int c = marioEgoCol;

        /*
         * メモ
         * 1瞬ジャンプ：3マス先に着地
         */

        action[Mario.KEY_SPEED] = false;
        action[Mario.KEY_JUMP] = false;


        // 目の前に敵がいる場合ジャンプで回避
        if (isEnemy(r, c + 1)) {
            action[Mario.KEY_JUMP] = isMarioAbleToJump;
            return action;
        }

//        // 着地先に敵がいる場合ファイアで撃退
//        if (!isMarioOnGround && (isEnemy(r, c + 2) || isEnemy(r + 1, c + 2) || isEnemy(r + 3, c + 3))) {
//            action[Mario.KEY_SPEED] = isMarioAbleToShoot;
//        }

        // 壁の手前でジャンプした着地先に敵がいる場合タイミングを遅らせる
        if (isObstacle(r, c + 1) && isEnemy(r - 1, c + 4)) {
            return action;
        }

        // 着地先に地面がない場合着地点を左に調整
        if (!isMarioOnGround && isObstacle(r + 2, c + 2) && isNothing(r + 2, c + 3)
                && isNothing(r, c + 1) && isNothing(r + 1, c + 1)) {
            action[Mario.KEY_RIGHT] = false;
            jumping = true;
            return action;
        }

        // 着地先に敵がいる場合着地点を左に調整
        if (!isMarioOnGround && (isEnemy(r + 2, c + 2) || isEnemy(r + 2, c + 3))) {
            action[Mario.KEY_RIGHT] = false;
            jumping = true;
            return action;
        }

        if (jumping) {
            jumpCount++;
            if (jumpCount == 3) {
                action[Mario.KEY_RIGHT] = true;
                jumping = false;
                jumpCount = 0;
            }
        }

        // 右上から敵が落ちてくる場合
        if (isEnemy(r - 2, c + 3)) {
            action[Mario.KEY_JUMP] = isMarioAbleToJump;
            return action;
        }

        // 目の前に障害物があればジャンプ
        if (isObstacle(r, c + 1) || isObstacle(r, c + 2))
            action[Mario.KEY_JUMP] = isMarioAbleToJump || !isMarioOnGround;

        // 目の前に穴があればジャンプ
        if (isMarioOnGround && isHole(r, c + 1))
            action[Mario.KEY_JUMP] = isMarioAbleToJump || !isMarioOnGround;

        return action;
    }

    private boolean isEnemy(int r, int c) {
        // (r, c)に敵がいたらtrue

        return getEnemiesCellValue(r, c) != 0;
    }

    private boolean isObstacle(int r, int c) {
        // (r, c)に障害物があればtrueを返す

        return getReceptiveFieldCellValue(r, c) == GeneralizerLevelScene.BRICK
                || getReceptiveFieldCellValue(r, c) == GeneralizerLevelScene.BORDER_CANNOT_PASS_THROUGH
                || getReceptiveFieldCellValue(r, c) == GeneralizerLevelScene.FLOWER_POT_OR_CANNON;
    }

    private boolean isHole(int r, int c) {
        // (r, c)に穴があればtrueを返す

        int i = 1;
        while (i <= 9) {
            if (getReceptiveFieldCellValue(r + i, c) != 0) {
                return false;
            }
            i++;
        }
        return true;
    }

    private boolean isNothing(int r, int c) {
        // (r, c)に何もなければtrue

        return getReceptiveFieldCellValue(r, c) == 0;
    }
}
