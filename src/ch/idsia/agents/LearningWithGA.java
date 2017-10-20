package ch.idsia.agents;

import java.util.Arrays;
import java.util.Random;

import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.benchmark.tasks.LearningTask;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.MarioAIOptions;
import ch.idsia.utils.wox.serial.Easy;

public class LearningWithGA implements LearningAgent {

    /* 個体数 */
    private final int popsize = 100;

    /* エリート数 */
    private final int bestnum = 2;

    /* エポック数 */
    private final int EndEpoch = 200;

    private final float mutateRate = 0.3f;

    OwnGAAgent agent = new OwnGAAgent();

    String name = "LearningWithGA";
    OwnGAAgent[] agents;
    private Agent bestAgent;
    private String args;
    /* 評価時最大値保持用変数 */
    private float fmax;

    Random r = new Random();

    /* 学習制限回数 */
    private long evaluationQuota;


    /* LearningWithGA コンストラクタ */
    public LearningWithGA(String args) {

		/* 評価値を初期化 */
        fmax = 0;

		/* 個体数分だけAgentを作成 */
        agents = new OwnGAAgent[popsize];
        for (int i = 0; i < agents.length; i++) {
            agents[i] = new OwnGAAgent();
        }

		/* agent[0] をbestAgentとして初期化 */
        bestAgent = agents[0].clone();
        this.args = args;
    }

    public void learn() {


        for (int generation = 0; generation < evaluationQuota; generation++) {

            System.out.println("世代 : " + generation);

			/* 100個体の評価 */

            compFit();
            OwnGAAgent[] nextagents = new OwnGAAgent[popsize];
            for (int i = 0; i < popsize; i++) {
                nextagents[i] = new OwnGAAgent();
            }



			/* エリート戦略によって2個体残す */
            for (int i = 0; i < bestnum; i++) {
                nextagents[i] = agents[i].clone();    //ディープコピー必要あり
            }


			/* 選択，交叉 */

            for (int i = bestnum; i < popsize; i += 2) {
                int[] parentsGene = select();
                cross(nextagents, parentsGene, i);
            }

			/* Agent をコピー */
            agents = nextagents;

			/* 突然変異 */
            mutate();

            if (generation == EndEpoch) {
                System.out.println("Generation[" + generation + "] : Playing!");
                halfwayPlayMario(bestAgent);
                writeFile();
                System.out.println("Learning is stopped");
                break;
            }

        }
    }


    /* 1世代で生成したすべての個体を1回ずつステージでプレイさせ評価値を算出．
     * その結果を降順にソート．もし過去の最高評価額(fmax)を超える個体が生成
     * できたら，xmlファイルを生成する．xml生成はwriteFileメソッドで行う．
     */
    private void compFit() {

		/* GAAgents[i]をプレイさせる */
        MarioAIOptions marioAIOptions = new MarioAIOptions();
        BasicTask basicTask = new BasicTask(marioAIOptions);

		/* ステージ生成 */
        marioAIOptions.setArgs(this.args);

	    /* プレイ画面出力するか否か */
        marioAIOptions.setVisualization(false);

        for (int i = 0; i < popsize; i++) {


			/* GAAgents[i]をセット */
            marioAIOptions.setAgent(agents[i]);
            basicTask.setOptionsAndReset(marioAIOptions);

            if (!basicTask.runSingleEpisode(1)) {
                System.out.println("MarioAI: out of computational time"
                        + " per action! Agent disqualified!");
            }

			/* 評価値(距離)をセット */
            EvaluationInfo evaluationInfo = basicTask.getEvaluationInfo();
            agents[i].setFitness(evaluationInfo.computeWeightedFitness());
//            agents[i].setFitness(evalFitness(evaluationInfo));
//            agents[i].setFitness(evaluationInfo.computeBasicFitness());
//            agents[i].setFitness(evaluationInfo.distancePassedCells);
            agents[i].setDistance(evaluationInfo.distancePassedCells);
        }
        /* 降順にソートする */
        Arrays.sort(agents);

		/* 首席Agentが過去の最高評価値を超えた場合，xmlを生成． */
        int presentBestAgentFitness = agents[0].getFitness();
        int presentBestAgentDistance = agents[0].getDistance();

        System.out.println("agents[0]Fitness : " + presentBestAgentFitness + "\n" + "agents[0].distance : " + presentBestAgentDistance);

        if (presentBestAgentFitness > fmax) {
            this.bestAgent = agents[0].clone();    //bestAgentを更新
            fmax = presentBestAgentFitness;    //fmax更新
            if (presentBestAgentDistance == 256)
                writeFile();            //bestAgentのxmlを出力
            System.out.println("fmax : " + fmax);
        }
    }


    /*  GA のアルゴリズムにおける優良な遺伝子の選択行為を行っている。
     * これは交叉の親となる 2 つの遺伝子をルーレット戦略によって
     * 選択するメソッドになる。
     */
    private int[] select() {

		/* 生存確率[i] = 適合度[i]/総計適合度 */
        double[] selectProb = new double[popsize];    //各個体の生存確率
        double[] accumulateRoulette = new double[popsize];    //selectProbの累積値

        int[] parentsGene = new int[2];

		/* 適合度の和を求める */
        double sumOfFit = 0;
        for (int i = bestnum; i < popsize; i++) {
            sumOfFit += (double) agents[i].getFitness();
        }

		/* ルーレットを作る */
        for (int i = bestnum; i < popsize; i++) {
            selectProb[i] = (double) agents[i].getFitness() / (double) sumOfFit;
            accumulateRoulette[i] = accumulateRoulette[i - 1] + selectProb[i];

        }

		/* ルーレットで選ぶ */
        for (int i = 0; i < 2; i++) {    //2回繰り返す

			/* 2から99までの乱数を作成し，99で割る */
            double selectedParent = (2.0 + (int) (r.nextInt(100) * 98.0) / 100.0) / 99.0;    //全てdoubleに直さないとアカン

            for (int j = bestnum + 1; j < popsize; j++) {
                if (selectedParent < accumulateRoulette[2]) {
                    parentsGene[i] = 2;
                    break;
                }

                if (accumulateRoulette[j - 1] < selectedParent
                        && selectedParent < accumulateRoulette[j]) {
                    parentsGene[i] = j;
                    break;
                }

				/* 例外処理 */
                if (selectedParent > 1.0 || selectedParent < 0.0) {
                    parentsGene[i] = 2;
                    break;
                }

            }
        }
        /* 返り値として，交叉する親の番号が入っている．要素数は2．*/
        return parentsGene;

    }

    /* 交叉 */
    private void cross(OwnGAAgent[] nextagents, int[] parentsGene, int i) {

        int geneLength = (1 << agent.inputNum);

        int sum = parentsGene[0] + parentsGene[1];
        float roulette = 1 - (float) parentsGene[0] / (float) sum;
        for (int k = 0; k < 2; k++) {    //2回繰り返す
            for (int j = 0; j < geneLength; j++) {

                float ran = (float) r.nextInt(sum) / (float) sum;    //ルーレット作成
                if (ran < roulette) {    //親A
                    byte parentsGeneA = agents[parentsGene[0]].getGene(j);
                    nextagents[i + k].setGene(j, parentsGeneA);
                } else if (ran > roulette) {    //親B
                    byte parentsGeneB = agents[parentsGene[1]].getGene(j);
                    nextagents[i + k].setGene(j, parentsGeneB);
                }
            }
        }
    }


    private void halfwayPlayMario(Agent agent) {
        /* GAAgents[i]をプレイさせる */
        MarioAIOptions marioAIOptions = new MarioAIOptions();
        BasicTask basicTask = new BasicTask(marioAIOptions);

		/* ステージ生成 */
        marioAIOptions.setArgs(this.args);

	    /* プレイ画面出力するか否か */
        marioAIOptions.setVisualization(true);

		/* GAAgents[i]をセット */
        marioAIOptions.setAgent(agent);
        basicTask.setOptionsAndReset(marioAIOptions);

        if (!basicTask.runSingleEpisode(1)) {
            System.out.println("MarioAI: out of computational time"
                    + " per action! Agent disqualified!");
        }

    }

    /* 突然変異 */
    private void mutate() {

        int popsize2 = popsize - bestnum;
        int mutationInt = (int) Math.floor(popsize * mutateRate);    //突然変異させる個体数(int型)
        int mutateGeneInt = (int) Math.floor((1 << agent.inputNum) * mutateRate);    //突然変異させる遺伝子座の個数(int型)(65536)

        int[] ran = new int[mutationInt];        //乱数格納用配列
        int geneRan;

        for (int i = 0; i < mutationInt; i++) {        //乱数で突然変異させる個体番号を決定(最初の2個体(エリート)は除く)
            ran[i] = r.nextInt(popsize2) + bestnum;
        }

        int num = 1 << (Environment.numberOfKeys - 1);    //遺伝子座に格納する値(0～32)を設定

        for (int i = bestnum; i < popsize; i++) {
            for (int j = 0; j < mutationInt; j++) {
                if (i == ran[j]) {    //突然変異させる個体を発見したら
                    for (int k = 0; k < mutateGeneInt; k++) {    //全体の10%を突然変異させる
                        geneRan = r.nextInt(1 << agent.inputNum);
                        agents[i].setGene(geneRan, (byte) r.nextInt(num));
                    }
                }
            }
        }
    }

    /* xml作成 */
    private void writeFile() {
        String fileName = name + "-"
                + GlobalOptions.getTimeStamp() + ".xml";
        Easy.save(this.bestAgent, fileName);
    }

    /* 評価用関数 */
    private int evalFitness(EvaluationInfo evaluationInfo) {
        return evaluationInfo.distancePassedPhys * 3
                + evaluationInfo.marioStatus * 2000
                + evaluationInfo.killsTotal * 200
                + evaluationInfo.coinsGained * 200
                + evaluationInfo.marioMode * 1000;
    }

    @Override
    public boolean[] getAction() {
        // TODO 自動生成されたメソッド・スタブ
        return null;
    }

    @Override
    public void integrateObservation(Environment environment) {
        // TODO 自動生成されたメソッド・スタブ

    }

    @Override
    public void giveIntermediateReward(float intermediateReward) {
        // TODO 自動生成されたメソッド・スタブ

    }

    @Override
    public void reset() {
        // TODO 自動生成されたメソッド・スタブ

    }

    @Override
    public void setObservationDetails(int rfWidth, int rfHeight, int egoRow,
                                      int egoCol) {
        // TODO 自動生成されたメソッド・スタブ

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void giveReward(float reward) {
        // TODO 自動生成されたメソッド・スタブ

    }

    @Override
    public void newEpisode() {
        // TODO 自動生成されたメソッド・スタブ

    }

    @Override
    public void setLearningTask(LearningTask learningTask) {
        // TODO 自動生成されたメソッド・スタブ

    }

    @Override
    public void setEvaluationQuota(long num) {
        this.evaluationQuota = num;
    }


    @Override
    public Agent getBestAgent() {
        return this.bestAgent;
    }

    @Override
    public void init() {

    }

}
