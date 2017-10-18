/*
 * Copyright (c) 2009-2010, Sergey Karakovskiy and Julian Togelius
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  Neither the name of the Mario AI nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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

package ch.idsia.scenarios.champ;

import ch.idsia.agents.Agent;
import ch.idsia.agents.LearningAgent;
import ch.idsia.agents.LearningWithGA;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.benchmark.tasks.LearningTask;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.MarioAIOptions;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey at idsia dot ch
 * Date: Mar 17, 2010 Time: 8:34:17 AM
 * Package: ch.idsia.scenarios
 */

/**
 * Class used for agent evaluation in Learning track
 * http://www.marioai.org/learning-track
 */

public final class LearningTrack {
    private final static long numberOfTrials = 1000;
    final static boolean scoring = false;
    private static int killsSum = 0;
    private static float marioStatusSum = 0;
    private static int timeLeftSum = 0;
    private static int marioModeSum = 0;
    private static boolean detailedStats = false;

    final static int populationSize = 1000;

    private static int evaluateSubmission(MarioAIOptions marioAIOptions, LearningAgent learningAgent) {
    /* -----------------------学習--------------------------*/

	/* LearningTaskオブジェクトを作成 */
        LearningTask learningTask = new LearningTask(marioAIOptions);

    /* 学習制限回数を取得 */
        learningAgent.setEvaluationQuota(LearningTask.getEvaluationQuota());

    /* 作ったオブジェクトをLearningAgentのTaskとして渡す */
        learningAgent.setLearningTask(learningTask);

    /* LearningAgentの初期化 */
        learningAgent.init();

    /* 学習の実行 */
        learningAgent.learn(); // launches the training process. numberOfTrials happen here

    /* BestAgentを取得 */
        Agent agent = learningAgent.getBestAgent(); // this agent will be evaluated

    /* 評価のvisualize */
        marioAIOptions.setVisualization(true);

    /* AgentをmarioAIOptionsのAgentにセット */
        marioAIOptions.setAgent(agent);

    /* BasicTaskで1トラック実行 */
        BasicTask basicTask = new BasicTask(marioAIOptions);
        basicTask.setOptionsAndReset(marioAIOptions);

    /* １トラック終了後にスコアを画面に出力するか */
        boolean verbose = true;

    /* 1トラック実行(制限時間を超えたらFalse)
     * 学習後のAgentを用いて，runSingleEpisodeメソッドで1回ステージを
     * プレイさせ，その結果をfという変数に入れ，返す．
     */

        if (!basicTask.runSingleEpisode(1))  // make evaluation on the same episode once
        {
            System.out.println("MarioAI: out of computational time"
                    + " per action! Agent disqualified!");
        }

    /* 結果を取得 */
        EvaluationInfo evaluationInfo = basicTask.getEvaluationInfo();
        //System.out.println(evaluationInfo.toString());


    /* DEBUG */
        int distance = evaluationInfo.distancePassedCells;
        System.out.println("distance : " + distance);


    /*このステージの得点を取得 */
        int f = evaluationInfo.computeWeightedFitness();

    /* verbose = true なら結果，得点を出力 */
        if (verbose) {
            System.out.println("Intermediate SCORE = " + f + ";\n Details: "
                    + evaluationInfo.toString());
        }

    /* Fitnessを返す */
        return f;
    }

    public static void main(String[] args) {

	/* 学習に用いるAgentを指定 */

	/* MainTask3.java */
//        LearningAgent learningAgent = new LearningWithGA("-lhs off -ltb on -lg off -lb off -ld 1 -ls 0 -le g");

	/* MainTask4_1.java */
//        LearningAgent learningAgent = new LearningWithGA("-lde on -ltb off -ld 2 -ls 0 -le g");

	/* MainTask4_2.java */
        LearningAgent learningAgent = new LearningWithGA("-lco off -lb on -le off -lhb off -lg on -ltb on -lhs off -lca on -lde on -ld 5 -ls 133829");

	/* MainTask4_3.java */
//         LearningAgent learningAgent = new LearningWithGA("-lde on -i off -ld 30 -ls 133434 -lhb on");

        System.out.println("main.learningAgent = " + learningAgent);

	/* パラメータを設定する */
        MarioAIOptions marioAIOptions = new MarioAIOptions(args);
        //LearningAgent learningAgent = new MLPESLearningAgent(); // Learning track competition entry goes here

	/* 学習するステージを生成 */

	/* MainTask4_1.java */
//        marioAIOptions.setArgs("-lde on -ltb off -ld 2 -ls 0 -le g");

    /* MainTask4_2.java */
        marioAIOptions.setArgs("-lco off -lb on -le off -lhb off -lg on -ltb on -lhs off -lca on -lde on -ld 5 -ls 133829");

    /* MainTask4_3.java */
//        marioAIOptions.setArgs("-lde on -i off -ld 30 -ls 133434 -lhb on");

	/* 学習後の得点をfinalScoreに保存し画面へ出力 */
        float finalScore = LearningTrack.evaluateSubmission(marioAIOptions, learningAgent);

        System.out.println("finalScore = " + finalScore);
        System.exit(0);
    }
}
