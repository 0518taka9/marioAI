# marioAI

# 実行方法

## Agentのセット
`src/ch/idsia/scenarios/`以下にある`MainTask[hoge].java`において、実行するAgentをセットする。
例えば、`MainTask4_3.java`では、
```java
AgentsPool.addAgent(AgentsPool.loadAgent("/Users/taka/marioAI/results/AStar-task4-3-clear-fire-2.xml", false));
```
のようにAgentをセットしている。Pathについては各自読み替えて絶対パスを指定する。
Agentは`results/`以下にある`.xml`ファイル。Agentの学習が終了した際に自動的に書き出されるファイルである。

## 実行
`MainTask[hoge].java`を実行すると、マリオのAgentがステージを進む様子が表示される。

# 学習の方法

`src/ch/idsia/agents/`以下にある`LearningWith[hoge].java`を実行するとAgentの学習が始まる。
各自パラメーターの値などを良い感じに調節してAgentを作成する。

## LearningWith[hoge]
- AStar: A*アルゴリズムっぽいアルゴリズムで良い感じに行動を学習
- GA: Genetic algorithm(遺伝アルゴリズム)で学習
- MC: Monte Carlo(モンテカルロ法)で学習

## xmlファイル出力
```java
private void writeFile() {
    String fileName = path + agentName + "-" + GlobalOptions.getTimeStamp() + ".xml";
    Easy.save(this.bestAgent, fileName);
}
```
を学習終了時に呼ぶ。
