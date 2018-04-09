package ch.idsia.scenarios.champ;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import ch.idsia.agents.Agent;
import ch.idsia.agents.LearningAgent;
import ch.idsia.agents.LearningWithGA;
import ch.idsia.agents.LearningWithMC;
import ch.idsia.agents.MCAgent;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.benchmark.tasks.LearningTask;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.MarioAIOptions;


public final class LearningTrackMC
{
final static long numberOfTrials = 10000;
final static boolean scoring = false;
private static int killsSum = 0;
private static float marioStatusSum = 0;
private static int timeLeftSum = 0;
private static int marioModeSum = 0;
private static boolean detailedStats = false;

final static int populationSize = 1000;

public static void replay(String filename,MarioAIOptions marioAIOptions, LearningWithMC learningAgent){
	try{
		File f = new File(filename);
		BufferedReader br = new BufferedReader(new FileReader(f));
		for(int i = 0; i < Math.pow(2.0, MCAgent.width * 4 + 1); ++i){
			for(int j = 0; j < 2; ++j){
				for(int k = 0;k < 2; ++k){
					for(int t = 0; t < MCAgent.numOfAction; ++t){
						String s = br.readLine();
						if(s != null)
							MCAgent.qValue[i][j][k][t] = Float.parseFloat(s);
					}
				}
			}
		}
	}
	catch(IOException e){
	    System.out.println(e);
	}
	learningAgent.show();
}

private static void evaluateSubmission(MarioAIOptions marioAIOptions, LearningAgent learningAgent)
{
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
    learningAgent.learn();
   	// launches the training process. numberOfTrials happen here
   	/*for(int i = 0; i < 64; ++i){
   		System.out.println(QAgent.qValue[i][0][2]);
   	}*/

}

private static int oldEval(MarioAIOptions marioAIOptions, LearningAgent learningAgent)
{
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

    for (int i = 0; i < numberOfTrials; ++i)
    {
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
        if (verbose)
        {
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
    if (verbose)
    {
        System.out.println("Intermediate SCORE = " + f + "; Details: " + evaluationInfo.toStringSingleLine());
    }
    System.out.println("LearningTrack final score = " + f);
    return f;
}


public static void main(String[] args){

	/* 学習に用いるAgentを指定 */
	/* MainTask4_1.java */
	LearningAgent learningAgent = new LearningWithMC("-lde on -i on -ltb off -ld 2 -ls 0 -le g");
	
	/* MainTask4_2.java */
	// LearningAgent learningAgent = new LearningWithMC("-lco off -lb on -le off -lhb off -lg on -ltb on -lhs off -lca on -lde on -ld 5 -ls 133829");
	
	/* MainTask4_3.java */
	// LearningAgent learningAgent = new LearningWithMC("-lde on -i off -ld 30 -ls 133434 -lhb on");

	System.out.println("main.learningAgent = " + learningAgent);

	/* パラメータを設定する */
	MarioAIOptions marioAIOptions = new MarioAIOptions(args);
	//LearningAgent learningAgent = new MLPESLearningAgent(); // Learning track competition entry goes here
	evaluateSubmission(marioAIOptions,learningAgent);
	//replay("MonteCarloMainTask3.txt",marioAIOptions,(LearningWithMC2)learningAgent);


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


    System.exit(0);
}
}
