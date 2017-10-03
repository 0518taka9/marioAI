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

public class LearningWithGA implements LearningAgent{

	/* 個体数 */
	final int popsize = 100;

	/* エリート数 */
	final int bestnum = 2;

	final float mutateRate = 0.1f;
	final float crossRate = 0.5f;

//	private LearningTask task = null;
	private String name = "LearningWithGA";
	private GAAgent[] agents;
	private Agent bestAgent;
	private String args;
	/* 評価時最大値保持用変数 */
	float fmax;

	Random r = new Random();

	/* ゲーム諸データ取得用 */
//	private EvaluationInfo localEvaluationInfo;

	/* 学習制限回数 */
	long evaluationQuota;


	/* LearningWithGA コンストラクタ */
	public LearningWithGA(String args){

		/* 評価値を初期化 */
		fmax = 0;

		/* 個体数分だけAgentを作成 */
		agents = new GAAgent[popsize];
		for (int i=0; i<agents.length; i++){
			agents[i] = new GAAgent();
		}

		/* agent[0] をbestAgentとして初期化 */
		bestAgent = agents[0].clone();
		this.args = args;
	}

	public void learn(){


		for(int generation=0 ; generation<evaluationQuota ; generation++){

			System.out.println("世代 : "+generation);

			/* 100個体の評価 */

			compFit();
			GAAgent nextagents[] = new GAAgent[popsize];
			for (int i=bestnum; i<popsize; i++){
				nextagents[i] = new GAAgent();
			}



			/* エリート戦略によって2個体残す */
			for(int i=0; i<bestnum; i++){
				nextagents[i] = agents[i].clone();	//ディープコピー必要あり
			}


			/* 選択，交叉 */

			for(int i=bestnum; i<popsize; i+=2){
				int[] parentsGene = select();
				cross(nextagents, parentsGene, i);
			}

			/* Agent をコピー */
			agents = nextagents;

			/* 突然変異 */
			mutate();
			
			int EndEpoch = 10;
			if( generation == EndEpoch){
				System.out.println("Generation["+generation+"] : Playing!");
				halfwayPlayMario(bestAgent);
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

		for(int i=0 ; i<popsize ; i++){


			/* GAAgents[i]をセット */
			marioAIOptions.setAgent(agents[i]);
			basicTask.setOptionsAndReset(marioAIOptions);

			if ( !basicTask.runSingleEpisode(1) ){
				System.out.println("MarioAI: out of computational time"
				+ " per action! Agent disqualified!");
			}

			/* 評価値(距離)をセット */
			EvaluationInfo evaluationInfo = basicTask.getEvaluationInfo();
			agents[i].setFitness(evaluationInfo.distancePassedCells);

			agents[i].setDistance(evaluationInfo.distancePassedCells);

		}

		/* 降順にソートする */
		Arrays.sort(agents);

		/* 首席Agentが過去の最高評価値を超えた場合，xmlを生成． */
		int presentBestAgentDistance = agents[0].getFitness();

		System.out.println("agents[0]Fitness : "+presentBestAgentDistance +"\n"+ "agents[0].distance : " + agents[0].getDistance());

		if(presentBestAgentDistance > fmax){
			bestAgent = (Agent)agents[0].clone();	//bestAgentを更新
			fmax = presentBestAgentDistance;	//fmax更新
//			writeFile();			//bestAgentのxmlを出力
			System.out.println("fmax : "+fmax);
		}
		return;

	}


	/*  GA のアルゴリズムにおける優良な遺伝子の選択行為を行っている。
	 * これは交叉の親となる 2 つの遺伝子をルーレット戦略によって
	 * 選択するメソッドになる。
	 */
	private int[] select() {

		/* 生存確率[i] = 適合度[i]/総計適合度 */
		double[] selectProb = new double[popsize];	//各個体の生存確率
		double[] accumulateRoulette = new double[popsize];	//selectProbの累積値

		int[] parentsGene = new int[2];

		/* 適合度の和を求める */
		double sumOfFit = 0;
		for(int i=bestnum ; i<popsize ; i++){
			sumOfFit += (double)agents[i].getFitness();
		}

		/* ルーレットを作る */
		for(int i=bestnum ; i<popsize ; i++){
			selectProb[i] = (double)agents[i].getFitness()/(double)sumOfFit;
			accumulateRoulette[i] = accumulateRoulette[i-1] + selectProb[i];

		}

		/* ルーレットで選ぶ */

		for(int i=0 ; i<2 ; i++){	//2回繰り返す

			/* 2から99までの乱数を作成し，99で割る */
			double selectedParent = (2.0+(int)(r.nextInt(100)*98.0)/100.0)/ 99.0;	//全てdoubleに直さないとアカン

			for(int j=bestnum+1 ; j<popsize ; j++){

				if(selectedParent < accumulateRoulette[2]){
					parentsGene[i] = 2;
					break;
				}

				if(accumulateRoulette[j-1] < selectedParent
						&& selectedParent < accumulateRoulette[j]){
					parentsGene[i] = j;
					break;
				}

				/* 例外処理 */
				if(selectedParent > 1.0 || selectedParent < 0.0){
					parentsGene[i] = 2;
					break;
				}

			}
		}
		/* 返り値として，交叉する親の番号が入っている．要素数は2．*/
		return parentsGene;

	}

	/* 交叉 */
	private void cross(GAAgent[] nextagents, int[] parentsGene, int i) {

		int geneLength = (1<<16);

		int sum = parentsGene[0]+parentsGene[1];
		float roulette = 1 - (float)parentsGene[0]/(float)sum;
//		System.out.println("parentsGene[0] : "+parentsGene[0]);
//		System.out.println("parentsGene[1] : "+parentsGene[1]);
//		System.out.println("roulette : "+roulette);
//		int Acount=0,Bcount=0;
		for(int k=0 ; k<2 ; k++){	//2回繰り返す
			for(int j=0 ; j<geneLength ; j++){

				float ran = (float)r.nextInt(sum)/(float)sum;	//ルーレット作成
				if(ran <roulette){	//親A
//					Acount++;
					byte parentsGeneA = agents[parentsGene[0]].getGene(j);
					nextagents[i+k].setGene(j ,parentsGeneA);
				}else if(ran > roulette){	//親B
//					Bcount++;
					byte parentsGeneB = agents[parentsGene[1]].getGene(j);
					nextagents[i+k].setGene(j, parentsGeneB);
				}else{	//エラー処理?

				}
			}
		}
//		System.out.println("Aselect : " + (float)(Acount)/(Acount+Bcount)*100 +"\n"+"Bselect:"+(float)(Bcount)/(Acount+Bcount)*100);
	}


	private void halfwayPlayMario(Agent agent){
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

		if ( !basicTask.runSingleEpisode(1) ){
			System.out.println("MarioAI: out of computational time"
			+ " per action! Agent disqualified!");
		}

	}

	/* 突然変異 */
	private void mutate() {

		int popsize2 = popsize - 2;
//		float mutation = popsize * mutateRate;	//突然変異させる個体数(float型)を設定(現在10個体)
		int mutationInt = (int) Math.floor(popsize*mutateRate);	//突然変異させる個体数(int型)
//		float mutation3 = (1 << 16) * mutateRate;	//突然変異させる遺伝子座の個数(float型)
		int mutateGeneInt = (int) Math.floor((1<<16) * mutateRate);	//突然変異させる遺伝子座の個数(int型)(65536)

		int[] ran = new int[mutationInt];		//乱数格納用配列
		int geneRan;

		for(int i=0; i<mutationInt; i++){		//乱数で突然変異させる個体番号を決定(最初の2個体(エリート)は除く)
			ran[i] = r.nextInt(popsize2) + 2;
		}

		int num = 1 << (Environment.numberOfKeys -1);	//遺伝子座に格納する値(0～32)を設定

		for(int i=bestnum; i<popsize; i++){
			for(int j=0; j<mutationInt; j++){
				if(i == ran[j]){	//突然変異させる個体を発見したら
					for(int k=0 ; k<mutateGeneInt ; k++){	//全体の10%を突然変異させる
						geneRan = r.nextInt(1<<16);
						agents[i].setGene(geneRan,(byte) r.nextInt(num));
					}
				}
			}
		}
	}

	/* xml作成 */
	private void writeFile(){
		String fileName = name + "-"
				+ GlobalOptions.getTimeStamp() + ".xml";
		Easy.save(bestAgent, fileName);
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
		return bestAgent;
	}

	@Override
	public void init() {

	}

}
