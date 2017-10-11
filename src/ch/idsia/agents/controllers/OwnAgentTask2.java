package ch.idsia.agents.controllers;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.GeneralizerLevelScene;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;

/**
 * Created by nakatsukatakahiro on 2017/10/06.
 */
public class OwnAgentTask2 extends BasicMarioAIAgent implements Agent {

    public OwnAgentTask2() {
        super("OwnAgentTask2");
        reset();
    }

    public void reset() {
        action = new boolean[Environment.numberOfKeys];
        action[Mario.KEY_RIGHT] = true;
    }

    public boolean[] getAction() {

        int r = marioEgoRow;
        int c = marioEgoCol;

        // 目の前に障害物があればジャンプ
        if (isObstacle(r, c + 1) || isObstacle(r, c + 2)) {
            action[Mario.KEY_JUMP] = isMarioAbleToJump || !isMarioOnGround;
        }

        // 目の前に穴があればジャンプ
        if (isHole(r, c + 1)) action[Mario.KEY_JUMP] = isMarioAbleToJump || !isMarioOnGround;

        return action;
    }

    private boolean isObstacle(int r, int c) {
        // (r, c)に障害物があればtrueを返す

        return getReceptiveFieldCellValue(r, c)  == GeneralizerLevelScene.BRICK
                || getReceptiveFieldCellValue(r, c)  == GeneralizerLevelScene.BORDER_CANNOT_PASS_THROUGH
                || getReceptiveFieldCellValue(r, c)  == GeneralizerLevelScene.FLOWER_POT_OR_CANNON;
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

    private boolean isNothing(int r, int c) {
        // (r, c)に何もなければtrue
        return getReceptiveFieldCellValue(r, c) == 0;
    }
}
