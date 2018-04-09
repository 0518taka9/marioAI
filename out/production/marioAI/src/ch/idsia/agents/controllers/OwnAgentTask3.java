package ch.idsia.agents.controllers;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.GeneralizerLevelScene;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

public class OwnAgentTask3 extends BasicMarioAIAgent implements Agent {

    private int jumpCount = 0;
    private boolean jumping = false;
    private boolean escape = false;
    private boolean stomp = false;

    public OwnAgentTask3() {
        super("OwnAgentTask3");
        reset();
    }

    public void reset() {
        action = new boolean[Environment.numberOfKeys];
        action[Mario.KEY_RIGHT] = true;
    }

    public boolean[] getAction() {

        int r = marioEgoRow;
        int c = marioEgoCol;

        action[Mario.KEY_SPEED] = false;
        action[Mario.KEY_JUMP] = false;


        // 目の前に敵がいる場合ジャンプで回避(右)
        if (action[Mario.KEY_RIGHT] && isEnemy(r, c + 1) || isEnemy(r, c + 2)) {
            action[Mario.KEY_JUMP] = isMarioAbleToJump;
            return action;
        }

        // 目の前に敵がいる場合ジャンプで回避(左)
        if (action[Mario.KEY_LEFT] && isEnemy(r, c - 1)) {
            action[Mario.KEY_JUMP] = isMarioAbleToJump;
            return action;
        }

        // 壁の手前でジャンプした着地先に敵がいる場合タイミングを遅らせる
        if (isObstacle(r, c + 1) && isEnemy(r - 1, c + 4)) {
            return action;
        }

        // 着地点の後ろから敵が来ている場合着地点を左に調整して敵を踏む
        if (!isMarioOnGround
                && isEnemy(r + 2, c - 1) && !isObstacle(r + 2, c - 1)) {
            action[Mario.KEY_RIGHT] = false;
            action[Mario.KEY_LEFT] = true;
            stomp = true;
            return action;
        }

        // 左に調整
        if (stomp) {
            action[Mario.KEY_LEFT] = false;
            action[Mario.KEY_RIGHT] = true;
            stomp = false;
            jumpCount = 0;
        }

        // 微調整
        if (!isMarioOnGround && isObstacle(r + 2, c + 2) && isNothing(r + 2, c + 3)
                && isNothing(r, c + 1) && isNothing(r + 1, c + 1)) {
            action[Mario.KEY_RIGHT] = false;
            jumping = true;
            return action;
        }

        // 着地先に敵がいる場合着地点を左に調整して敵を避ける
        if (!isMarioOnGround && (isEnemy(r + 1, c + 4)
                || (isEnemy(r + 1, c + 3) && isNothing(r + 2, c))
                || (isEnemy(r, c + 4) && isNothing(r + 2, c))
                || (isEnemy(r + 2, c + 2) && !isObstacle(r + 2, c + 2))
                || (isEnemy(r + 2, c + 3) && !isObstacle(r + 2, c + 3)))
                && !isNothing(r + 3, c + 2) && !isNothing(r + 3, c + 3)
                ) {
            action[Mario.KEY_RIGHT] = false;
            jumping = true;
            return action;
        }

        // 着地点の調節
        if (jumping) {
            jumpCount++;
            if (jumpCount >= 5) {
                action[Mario.KEY_RIGHT] = true;
                jumping = false;
                jumpCount = 0;
            }
        }

        // 右上から敵が落ちてくる場合(踏んで倒す)
        if (isEnemy(r - 2, c + 3)) {
            action[Mario.KEY_JUMP] = isMarioAbleToJump || !isMarioOnGround;
            return action;
        }

        // 右上から敵が落ちてくる場合(左に避ける)
        if (isEnemy(r - 2, c + 2)) {
            action[Mario.KEY_RIGHT] = false;
            action[Mario.KEY_LEFT] = true;
            escape = true;
            return action;
        }

        // 左に避ける
        if (escape) {
            jumpCount++;
            if (jumpCount >= 10) {
                action[Mario.KEY_LEFT] = false;
                action[Mario.KEY_RIGHT] = true;
                escape = false;
                jumpCount = 0;
            }
        }

        // 目の前に障害物があればジャンプ
        if (isObstacle(r, c + 1) || isObstacle(r, c + 2))
            action[Mario.KEY_JUMP] = isMarioAbleToJump || !isMarioOnGround;

        return action;
    }

    private boolean isObstacle(int r, int c) {
        // (r, c)に障害物があればtrueを返す
        return getReceptiveFieldCellValue(r, c) == GeneralizerLevelScene.BRICK
                || getReceptiveFieldCellValue(r, c) == GeneralizerLevelScene.BORDER_CANNOT_PASS_THROUGH
                || getReceptiveFieldCellValue(r, c) == GeneralizerLevelScene.FLOWER_POT_OR_CANNON;
    }

    private boolean isEnemy(int r, int c) {
        // (r, c)に敵がいたらtrue
        return getEnemiesCellValue(r, c) != 0;
    }

    private boolean isNothing(int r, int c) {
        // (r, c)に何もなければtrue
        return getReceptiveFieldCellValue(r, c) == 0;
    }
}
