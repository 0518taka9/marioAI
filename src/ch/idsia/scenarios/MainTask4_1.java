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
public final class MainTask4_1 {
    public static void main(String[] args) {
        final MarioAIOptions marioAIOptions = new MarioAIOptions(args);

//    AgentsPool.addAgent(AgentsPool.loadAgent("LearningWithGA-2017-10-14_23-53-10.xml", false));   //10038
//    AgentsPool.addAgent(AgentsPool.loadAgent("LearningWithGA-2017-10-14_23-41-19.xml", false)); //10074
//    AgentsPool.addAgent(AgentsPool.loadAgent("LearningWithGA-2017-10-17_13-08-23.xml", false)); //9956
//    AgentsPool.addAgent(AgentsPool.loadAgent("AStar-task4-1-clear-big.xml", false)); //10288
//    AgentsPool.addAgent(AgentsPool.loadAgent("AStar-task4-1-clear-fire-2.xml", false)); //10314
    AgentsPool.addAgent(AgentsPool.loadAgent("/Users/taka/marioAI/results/AStar-task4-1-clear-fire.xml", false)); //10352
    Agent agent = AgentsPool.getCurrentAgent();

    marioAIOptions.setAgent(agent);

    marioAIOptions.setArgs("-lde on -ltb off -ld 2 -ls 0 -le g");

    final BasicTask basicTask = new BasicTask(marioAIOptions);
    basicTask.setOptionsAndReset(marioAIOptions);
    basicTask.doEpisodes(1,true,1);
//
//        LearningAgent learningAgent = new LearningWithAStar("-lde on -ltb off -ld 2 -ls 0 -le g");
//        learningAgent.learn();

    System.exit(0);
    }

}
