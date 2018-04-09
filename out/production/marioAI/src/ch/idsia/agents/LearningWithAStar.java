package ch.idsia.agents;

import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.benchmark.tasks.LearningTask;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.MarioAIOptions;
import ch.idsia.utils.wox.serial.Easy;

import java.util.HashMap;

/**
 * Created by nakatsukatakahiro on 2017/10/29.
 */
public class LearningWithAStar implements LearningAgent {

    AStarAgent agent = new AStarAgent();

    String name = "LearningWithAStar";

    private AStarAgent bestAgent;

    private String args;

    private int seed, difficulty;

    // actionsのインデックス
    private static int actionIndex = 0;

    // ダメージを受けた時のインデックス
    private static int damagedIndex = 0;

    // 死んだ地点
    private static int deathPoint = 0;

    // 死んだ地点を記録するハッシュマップ
    private HashMap<Integer, Integer> deathPoints = new HashMap<Integer, Integer>();


    /* LearningWithAStarのコンストラクタ */
    public LearningWithAStar(String args) {
        agent = new AStarAgent();
        bestAgent = agent.clone();
        this.args = args;
    }

    public LearningWithAStar(int seed, int difficulty) {
        agent = new AStarAgent();
        bestAgent = agent.clone();
        this.seed = seed;
        this.difficulty = difficulty;
    }

    public void learn() {

        MarioAIOptions marioAIOptions = new MarioAIOptions();
        BasicTask basicTask = new BasicTask(marioAIOptions);
//        marioAIOptions.setArgs(args);
        marioAIOptions.setLevelRandSeed(seed);
        marioAIOptions.setLevelDifficulty(difficulty);

	    /* プレイ画面出力するか否か */
        marioAIOptions.setVisualization(false);

        /* 学習が完了するまでwhile文の中をループ */
        int k = 0;
        while (true) {

//            if (k % 10000 == 0) {
//                marioAIOptions.setVisualization(true);
//            } else {
//                marioAIOptions.setVisualization(false);
//            }

            // 実行
            marioAIOptions.setAgent(agent);
            basicTask.setOptionsAndReset(marioAIOptions);
            if (!basicTask.runSingleEpisode(1)) {
                System.out.println("MarioAI: out of computational time"
                        + " per action! Agent disqualified!");
            }

            // 情報を取得
            EvaluationInfo evaluationInfo = basicTask.getEvaluationInfo();

            // 死んだ地点を取得し記録する
            deathPoint = evaluationInfo.distancePassedCells;
            if (deathPoints.containsKey(deathPoint))
                deathPoints.put(deathPoint, deathPoints.get(deathPoint) + 1);
            else
                deathPoints.put(deathPoint, 1);

            // 次の世代のagentを用意
            AStarAgent nextAgent = new AStarAgent();

            damagedIndex = agent.getDamagedIndex();
            actionIndex = agent.getActionIndex();

            // クリアしたらループを抜ける
            if (evaluationInfo.distancePassedCells >= 256 && evaluationInfo.marioMode == 2) {
                setNextAgent(agent, nextAgent, 1);
                agent = nextAgent.clone();
                bestAgent = agent.clone();
                break;
            }

            // 次のactionsを決定
            setNextAgent(agent, nextAgent, 0);

            // agentにコピー
            agent = nextAgent.clone();

            // 初期化
            damagedIndex = 0;
            actionIndex = 0;
            deathPoint = 0;

            k++;
            System.out.println("学習：" + k + "回目");
            System.out.println("distance：" + evaluationInfo.distancePassedCells);

        }

        // xmlで出力
        writeFile();

        // 確認のためbestAgentを実行
        marioAIOptions.setAgent(bestAgent);
        marioAIOptions.setVisualization(true);
        basicTask.setOptionsAndReset(marioAIOptions);
        if (!basicTask.runSingleEpisode(1)) {
            System.out.println("MarioAI: out of computational time"
                    + " per action! Agent disqualified!");
        }

    }

    /* 次のactionsを決定する
     * actionIndex：死んだ時のindex
     * damagedIndex：ダメージを受けた時のindex
     * deathPoint：死んだ地点
     */
    private void setNextAgent(AStarAgent agent, AStarAgent nextAgent, int n) {

        // agentのactionsを取得
        byte[] actions = agent.getActions();

        // 巻き戻すインデックス
        int rewindIndex = deathPoints.get(deathPoint) / 300 * 20;

        // クリアした時
        if (n == 1) {
            nextAgent.setActions(actions);
            return;
        }
        // ダメージを受けた時
        if (damagedIndex < actionIndex && damagedIndex != 0) {
            for (int i = damagedIndex - 10; i < actions.length; i++) {
                actions[i] = 0;
            }
        }
        // 行き詰まった時
        if (agent.getIsDeadEnd()) {
            for (int i = agent.getDeadEndIndex() - 30; i < actions.length; i++) {
                actions[i] = 0;
            }
        }
        // 同じ場所で死に過ぎたらカウントを初期化
        if (rewindIndex > actionIndex) {
            deathPoints.put(deathPoint, 1);
        // 同じ場所で何回も死んだ時
        } else if (rewindIndex >= 20) {
            for (int i = actionIndex - rewindIndex; i < actions.length; i++) {
                actions[i] = 0;
            }
        }
        // 普通に死んだ時
        for (int i = actionIndex - 10; i < actions.length; i++) {
            actions[i] = 0;
        }
        // actionsをコピー
        nextAgent.setActions(actions);
    }

    /* xml作成 */
    private void writeFile() {
        String fileName = name + "-"
                + GlobalOptions.getTimeStamp() + ".xml";
        Easy.save(bestAgent, fileName);
    }

    @Override
    public void giveReward(float reward) {

    }

    @Override
    public void newEpisode() {

    }

    @Override
    public void setLearningTask(LearningTask learningTask) {

    }

    @Override
    public void setEvaluationQuota(long num) {

    }

    @Override
    public Agent getBestAgent() {
        return null;
    }

    @Override
    public void init() {

    }

    @Override
    public boolean[] getAction() {
        return new boolean[0];
    }

    @Override
    public void integrateObservation(Environment environment) {

    }

    @Override
    public void giveIntermediateReward(float intermediateReward) {

    }

    @Override
    public void reset() {

    }

    @Override
    public void setObservationDetails(int rfWidth, int rfHeight, int egoRow, int egoCol) {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName(String name) {

    }
}
