/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Mario AI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package ch.idsia.agents.controllers;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.GeneralizerLevelScene;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.engine.sprites.Sprite;
import ch.idsia.benchmark.mario.environments.Environment;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey.karakovskiy@gmail.com
 * Date: Apr 8, 2009
 * Time: 4:03:46 AM
 */

public class OwnAgent extends BasicMarioAIAgent implements Agent {
    int trueJumpCounter = 0;
    int trueSpeedCounter = 0;
    private int judgeCount = 0;
    private boolean judge = false;

    public OwnAgent() {
        super("OwnAgent");
        reset();
    }

    public void reset() {
        action = new boolean[Environment.numberOfKeys];
        action[Mario.KEY_RIGHT] = true;
//        action[Mario.KEY_SPEED] = true;
    }

    public boolean[] getAction() {

        // 障害物の先に穴があるとき１マスジャンプ
        // ここから
        if (!isAbleJump1(marioEgoRow, marioEgoCol)) {
            action[Mario.KEY_RIGHT] = false;
            action[Mario.KEY_LEFT] = true;
            action[Mario.KEY_SPEED] = false;
            judge = true;
        }
        if (judge) {
            judgeCount++;
            if (judgeCount == 7) {
                action[Mario.KEY_RIGHT] = true;
                action[Mario.KEY_LEFT] = false;
                judgeCount = 0;
                judge = false;
            }
            action[Mario.KEY_SPEED] = true;
        }
        // ここまで

        if ((isObstacle(marioEgoRow, marioEgoCol + 1)
            || isEnemies(marioEgoRow, marioEgoCol)
            || isHole(marioEgoRow, marioEgoCol))
                && isAbleJump2(marioEgoRow, marioEgoCol)
            ) {
            action[Mario.KEY_JUMP] = isMarioAbleToJump || !isMarioOnGround;
        }

        return action;
    }

    private boolean isObstacle(int r, int c) {
        return getReceptiveFieldCellValue(r, c) == GeneralizerLevelScene.BRICK
                || getReceptiveFieldCellValue(r, c) == GeneralizerLevelScene.BORDER_CANNOT_PASS_THROUGH
                || getReceptiveFieldCellValue(r, c + 1) == GeneralizerLevelScene.BORDER_CANNOT_PASS_THROUGH
                || getReceptiveFieldCellValue(r, c) == GeneralizerLevelScene.FLOWER_POT_OR_CANNON
                || getReceptiveFieldCellValue(r, c + 1) == GeneralizerLevelScene.FLOWER_POT_OR_CANNON
                || getReceptiveFieldCellValue(r, c) == GeneralizerLevelScene.LADDER;
    }

    private boolean isEnemies(int r, int c) {
        return getEnemiesCellValue(r, c + 2) != Sprite.KIND_NONE
                || getEnemiesCellValue(r, c + 1) != Sprite.KIND_NONE;
    }

    private boolean isHole(int r, int c) {
        return getReceptiveFieldCellValue(r + 1, c + 1) == 0
                && getReceptiveFieldCellValue(r + 2, c + 1) == 0
                && getReceptiveFieldCellValue(r + 1, c + 2) == 0
                && getReceptiveFieldCellValue(r + 2, c + 2) == 0;
    }

    private boolean isAbleJump1(int r, int c) {
        //  障害物の先に穴があるかどうかを判定
        boolean judge = true;
        if (isMarioOnGround
                && getReceptiveFieldCellValue(r, c + 1) != 0
                && getReceptiveFieldCellValue(r + 1, c + 2) == 0
                && getReceptiveFieldCellValue(r + 2, c + 2) == 0
                ) {
            judge = false;
        }
        return judge;
    }

    private boolean isAbleJump2(int r, int c) {
        // 着地先に穴があるかどうかを判定
        boolean judge = true;
        if (isMarioOnGround
                && getReceptiveFieldCellValue(r, c+1) == 0
                && getReceptiveFieldCellValue(r+1, c+6) == 0
                && getReceptiveFieldCellValue(r+2, c+6) == 0
                && getReceptiveFieldCellValue(r+3, c+6) == 0
                && getReceptiveFieldCellValue(r+4, c+6) == 0
                && getReceptiveFieldCellValue(r+5, c+6) == 0
                && getReceptiveFieldCellValue(r+1, c+7) == 0
                && getReceptiveFieldCellValue(r+2, c+7) == 0
                && getReceptiveFieldCellValue(r+3, c+7) == 0
                && getReceptiveFieldCellValue(r+4, c+7) == 0
                && getReceptiveFieldCellValue(r+5, c+7) == 0
                ) {
            judge = false;
        }
        return judge;
    }
}