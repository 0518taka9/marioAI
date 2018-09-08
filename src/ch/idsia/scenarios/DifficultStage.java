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

package ch.idsia.scenarios;

import ch.idsia.agents.*;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.tools.MarioAIOptions;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, sergey at idsia dot ch Date: Mar 17, 2010 Time: 8:28:00 AM
 * Package: ch.idsia.scenarios
 */
public final class DifficultStage {
    public static void main(String[] args) {
        final MarioAIOptions marioAIOptions = new MarioAIOptions(args);

        AgentsPool.addAgent(AgentsPool.loadAgent("/Users/taka/marioAI/results/AStar-difficultstage-99-99-clear.xml", false));
//        AgentsPool.addAgent(AgentsPool.loadAgent("AStar-difficultstage-999-999-clear.xml", false));
//        AgentsPool.addAgent(AgentsPool.loadAgent("AStar-difficultstage-9999-9999-clear.xml", false));
//        AgentsPool.addAgent(AgentsPool.loadAgent("AStar-difficultstage-99999-99999-clear.xml", false));
//        AgentsPool.addAgent(AgentsPool.loadAgent("AStar-difficultstage-0-99999-clear.xml", false));
        Agent agent = AgentsPool.getCurrentAgent();

        marioAIOptions.setAgent(agent);

        marioAIOptions.setLevelRandSeed(99);
        marioAIOptions.setLevelDifficulty(99);

        final BasicTask basicTask = new BasicTask(marioAIOptions);
        basicTask.setOptionsAndReset(marioAIOptions);
        basicTask.doEpisodes(1,true,1);
//
//        LearningAgent learningAgent = new LearningWithAStar(0, 99999);r
//        learningAgent.learn();

        System.exit(0);
    }

}
