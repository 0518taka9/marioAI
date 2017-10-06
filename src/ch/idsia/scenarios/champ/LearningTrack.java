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

//    for(int i=0 ; i<LearningTask.getEvaluationQuota() ; i++){	//forで繰り返す???
//    	System.out.println("世代 : "+i);
        learningAgent.learn(); // launches the training process. numberOfTrials happen here

//    }

        Agent agent = learningAgent.getBestAgent(); // this agent will be evaluated

    /* 評価のvisualize */
        marioAIOptions.setVisualization(true);

//    System.out.println("/*---------------------- finished learning --------------------*/");
//    System.out.println("LearningTrack best agent = " + agent);

    /* AgentをmarioAIOptionsのAgentにセット */
        marioAIOptions.setAgent(agent);

    /* BasicTaskで1トラック実行 */
        BasicTask basicTask = new BasicTask(marioAIOptions);
        basicTask.setOptionsAndReset(marioAIOptions);


        //System.out.println("basicTask = " + basicTask);
        //System.out.println("agent = " + agent);

    /* １トラック終了後にスコアを画面に出力するか */
        boolean verbose = false;

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

    private static int oldEval(MarioAIOptions marioAIOptions, LearningAgent learningAgent) {
        boolean verbose = false;
        float fitness = 0;
        int disqualifications = 0;

        marioAIOptions.setVisualization(false);
        //final LearningTask learningTask = new LearningTask(marioAIOptions);
        //learningTask.setAgent(learningAgent);
        LearningTask task = new LearningTask(marioAIOptions);

        learningAgent.newEpisode();
        learningAgent.setLearningTask(task);
        learningAgent.setEvaluationQuota(numberOfTrials);
        learningAgent.init();

        for (int i = 0; i < numberOfTrials; ++i) {
            System.out.println("-------------------------------");
            System.out.println(i + " trial");
            //learningTask.reset(marioAIOptions);
            task.setOptionsAndReset(marioAIOptions);
        /* inform your agent that new episode is coming,
         * pick up next representative in population.
         */

            //     learningAgent.learn();
            task.runSingleEpisode(1);
        /*if (!task.runSingleEpisode())  // make evaluation on an episode once
        {
            System.out.println("MarioAI: out of computational time per action!");
            disqualifications++;
            continue;
        }*/

            EvaluationInfo evaluationInfo = task.getEnvironment().getEvaluationInfo();
            float f = evaluationInfo.computeWeightedFitness();
            if (verbose) {
                System.out.println("Intermediate SCORE = " + f + "; Details: " + evaluationInfo.toStringSingleLine());
            }
            // learn the reward
            //learningAgent.giveReward(f);
        }
        // do some post processing if you need to. In general: select the Agent with highest score.
        learningAgent.learn();
        // perform the gameplay task on the same level
        marioAIOptions.setVisualization(true);
        Agent bestAgent = learningAgent.getBestAgent();
        marioAIOptions.setAgent(bestAgent);
        BasicTask basicTask = new BasicTask(marioAIOptions);
        basicTask.setOptionsAndReset(marioAIOptions);
//        basicTask.setAgent(bestAgent);
        if (!basicTask.runSingleEpisode(1))  // make evaluation on the same episode once
        {
            System.out.println("MarioAI: out of computational time per action!");
            disqualifications++;
        }
        EvaluationInfo evaluationInfo = basicTask.getEnvironment().getEvaluationInfo();
        int f = evaluationInfo.computeWeightedFitness();
        if (verbose) {
            System.out.println("Intermediate SCORE = " + f + "; Details: " + evaluationInfo.toStringSingleLine());
        }
        System.out.println("LearningTrack final score = " + f);
        return f;
    }


    public static void main(String[] args) {

	/* 学習に用いるAgentを指定 */

	/* MainTask3.java */
//        LearningAgent learningAgent = new LearningWithGA("-lhs off -ltb on -lg off -lb off -ld 1 -ls 0 -le g");

	/* MainTask4_1.java */
        LearningAgent learningAgent = new LearningWithGA("-lde on -i on -ltb off -ld 2 -ls 0 -le g");

	/* MainTask4_2.java */
        // LearningAgent learningAgent = new LearningWithGA("-lco off -lb on -le off -lhb off -lg on -ltb on -lhs off -lca on -lde on -ld 5 -ls 133829");

	/* MainTask4_3.java */
        // LearningAgent learningAgent = new LearningWithGA("-lde on -i off -ld 30 -ls 133434 -lhb on");

        System.out.println("main.learningAgent = " + learningAgent);

	/* パラメータを設定する */
        MarioAIOptions marioAIOptions = new MarioAIOptions(args);
        //LearningAgent learningAgent = new MLPESLearningAgent(); // Learning track competition entry goes here




	/* 学習するステージを生成 */

	/*------ Level 0 ------*/


//	marioAIOptions = new MarioAIOptions(args);
//	marioAIOptions.setArgs("-lf on -lg on");
//	finalScore += LearningTrack.evaluateSubmission(marioAIOptions, learningAgent);
    /* ステージ生成 */
        //marioAIOptions.setArgs("-le 1 -ld 2");


	/*------ Level 1 ------*/

	/* MarioAIOptions 追加
	 * float で宣言
	 */

//	marioAIOptions = new MarioAIOptions(args);
//	marioAIOptions.setArgs("-lco off -lb on -le off -lhb off -lg on -ltb on -lhs off -lca on -lde on -ld 5 -ls 133829");
//	finalScore += LearningTrack.evaluateSubmission(marioAIOptions, learningAgent);


	/*------ Level 2 ------*/
    /*
    marioAIOptions = new MarioAIOptions(args);
    */
        //marioAIOptions.setArgs("-lde on -i on -ld 30 -ls 133434");
    /*finalScore += LearningTrack.evaluateSubmission(marioAIOptions, learningAgent);
	*/

	/*------ Level 3 ------*/
    /*
    marioAIOptions = new MarioAIOptions(args);
    marioAIOptions.setArgs("-lde on -i on -ld 30 -ls 133434 -lhb on");
    finalScore += LearningTrack.evaluateSubmission(marioAIOptions, learningAgent);
	*/

	/*------ Level 4 ------*/

//    marioAIOptions = new MarioAIOptions(args);
//    marioAIOptions.setArgs("-lla on -le off -lhs on -lde on -ld 5 -ls 1332656");
//    finalScore += LearningTrack.evaluateSubmission(marioAIOptions, learningAgent);


    /* Level 5 (bonus level) */

//    marioAIOptions = new MarioAIOptions(args);
//    marioAIOptions.setArgs("-le off -lhs on -lde on -ld 5 -ls 1332656");
//    finalScore += LearningTrack.evaluateSubmission(marioAIOptions, learningAgent);

	/* 学習後の得点をfinalScoreに保存し画面へ出力 */
        float finalScore = LearningTrack.evaluateSubmission(marioAIOptions, learningAgent);


        System.out.println("finalScore = " + finalScore);
        System.exit(0);
    }
}
