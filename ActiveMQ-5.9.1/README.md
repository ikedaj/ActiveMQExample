# ActiveMQ 5.9 動作確認メモ

Docker環境を利用して、ActiveMQ 5.9の動作確認を実施した際のメモ

## CentOS6のコンテナを起動する。

```
# docker run \
 --name=activemq \
 --hostname=activemq \
 -p 8161:8161 \
 -it -d centos:6 /bin/bash
```

## CentOS6のコンテナにログインする。

```
# docker exec -it activemq /bin/bash
```

## タイムゾーンを設定する。

```
[root@activemq /]# ln -sf /usr/share/zoneinfo/Japan /etc/localtime
```

## プロキシを設定する。

```
[root@activemq /]# export http_proxy=http://USERNAME:PASSWORD@PROXYHOST:PORT/
[root@activemq /]# export https_proxy=http://USERNAME:PASSWORD@PROXYHOST:PORT/
```

## 必要なRPMをインストールする。

```
[root@activemq /]# yum update -y
[root@activemq /]# yum install -y java-1.7.0-openjdk-devel upstart initscripts vim wget tar
[root@activemq /]# yum clean all
```

## ActiveMQ 5.9.1のインストール資材を入手する。

```
[root@activemq /]# wget http://repo1.maven.org/maven2/org/apache/activemq/apache-activemq/5.9.1/apache-activemq-5.9.1-bin.tar.gz
```

## ActiveMQ 5.9.1をインストールする。

| 項目                        | 値                                   | 
|----------------------------|--------------------------------------|
| 資材展開先                   |　/opt/activemq/apache-activemq-5.9.1　|
| インストール先(上記ディレクトリをリンク) |　/opt/activemq/current               |


```
[root@activemq /]# mkdir -p /opt/activemq
[root@activemq /]# tar xvzf apache-activemq-5.9.1-bin.tar.gz -C /opt/activemq
[root@activemq /]# ln -s /opt/activemq/apache-activemq-5.9.1 /opt/activemq/current
[root@activemq /]# rm -f apache-activemq-5.9.1-bin.tar.gz
```

## 環境変数を設定する。

```
[root@activemq /]# export JAVA_HOME=/usr/lib/jvm/java
[root@activemq /]# export ACTIVEMQ_HOME=/opt/activemq/current
[root@activemq /]# export PATH=$PATH:$ACTIVEMQ_HOME/bin
```

## 起動スクリプトをリンクする。

```
[root@activemq /]# ln -s $ACTIVEMQ_HOME/bin/linux-x86-64/activemq /etc/init.d/activemq
```

## ActiveMQを起動する。
* 起動時のログは、デフォルトでは　$ACTIVEMQ_HOME/data/activemq.log　に出力される。

```
[root@activemq /]# service activemq start
[root@activemq /]# service activemq status
[root@activemq /]# tail -f /opt/activemq/current/data/activemq.log
```

## ActiveMQの管理コンソールにログインする。
* ブラウザから管理コンソールにログインする。
* Manage ActiveMQ brokerをクリックすると、管理ユーザとパスワードの入力が要求され、管理画面に遷移する。

| 項目     | 値                               | 
|----------|---------------------------------|
| コンソール  |　http://DockerホストのIPアドレス:8161/　|
| ユーザ    |　admin　                           |
| パスワード  |　admin                            |


## 設定ファイル(activemq.xml)を編集し、JMS接続を有効化する。

```
[root@activemq /]# cp -p /opt/activemq/current/conf/activemq.xml /opt/activemq/current/conf/activemq.xml.org
[root@activemq /]# vim /opt/activemq/current/conf/activemq.xml
```

* brokerタグに　useJmx="true"　を追加する。

```
    <broker xmlns="http://activemq.apache.org/schema/core"
            brokerName="localhost"
            dataDirectory="${activemq.data}"
            useJmx="true">
```

* managementContext を、createConnector="true" connectorPort="1099" へ変更する。
　
```
        <managementContext>
            <managementContext createConnector="true" connectorPort="1099"/>
        </managementContext>
```

* サービスを再起動し、設定変更を反映する。

```
[root@activemq /]# service activemq restart
```

* JMXのポート(1099)を確認する。

```
[root@activemq /]# netstat -tunap
```

* JMXでActiveMQに接続する。

```
[root@activemq /]# activemq list

INFO: Using default configuration
(you can configure options in one of these file: /etc/default/activemq /root/.activemqrc)

INFO: Invoke the following command to create a configuration file
/opt/activemq/current/bin/activemq setup [ /etc/default/activemq | /root/.activemqrc ]

INFO: Using java '/usr/lib/jvm/java/bin/java'
Java Runtime: Oracle Corporation 1.7.0_91 /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.91.x86_64/jre
  Heap sizes: current=1013632k  free=1008039k  max=1013632k
    JVM args: -Xms1G -Xmx1G -Djava.util.logging.config.file=logging.properties -Djava.security.auth.login.config=/opt/activemq/current/conf/login.config -Dactivemq.classpath=/opt/activemq/current/conf; -Dactivemq.home=/opt/activemq/current -Dactivemq.base=/opt/activemq/current -Dactivemq.conf=/opt/activemq/current/conf -Dactivemq.data=/opt/activemq/current/data
Extensions classpath:
  [/opt/activemq/current/lib,/opt/activemq/current/lib/camel,/opt/activemq/current/lib/optional,/opt/activemq/current/lib/web,/opt/activemq/current/lib/extra]
ACTIVEMQ_HOME: /opt/activemq/current
ACTIVEMQ_BASE: /opt/activemq/current
ACTIVEMQ_CONF: /opt/activemq/current/conf
ACTIVEMQ_DATA: /opt/activemq/current/data
Connecting to JMX URL: service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi
brokerName = localhost
```

* activemqコマンドで接続先を指定しない場合のデフォルトは --jmxurl service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi である。
* 接続IPアドレスを制限する場合は、managementContextでconnectorHostを指定する。 
    * [[ManagementContext Properties Reference]](http://activemq.apache.org/jmx.html)

* --jmxurl オプションで接続先を指定した場合

```
[root@activemq /]# activemq list --jmxurl service:jmx:rmi:///jndi/rmi://activemq:1099/jmxrmi
```

* キューの情報を表示する。

```
[root@activemq /]# activemq dstat --jmxurl service:jmx:rmi:///jndi/rmi://activemq:1099/jmxrmi

Name                                                Queue Size  Producer #  Consumer #   Enqueue #   Dequeue #    Memory %
ActiveMQ.Advisory.MasterBroker                               0           0           0           1           0           0
```

## 設定ファイル(activemq.xml)を編集し、キューを作成する。

[root@activemq /]# vim /opt/activemq/current/conf/activemq.xml

* SAMPLE キューを作成する。

```
    <broker xmlns="http://activemq.apache.org/schema/core"
            brokerName="localhost"
            dataDirectory="${activemq.data}"
            useJmx="true">

        <destinations>
           <queue physicalName="SAMPLE" />
        </destinations>

        <destinationPolicy>
```

* キューの設定を追加する。

```
                </policyEntry>
                <policyEntry queue=">" producerFlowControl="true" memoryLimit="1mb">
                  <pendingQueuePolicy>
                    <vmQueueCursor/>
                  </pendingQueuePolicy>
                </policyEntry>
              </policyEntries>
```

* サービスを再起動し、設定変更を反映する。

```
[root@activemq /]# service activemq restart
```

* キューの情報を表示する。

```
[root@activemq /]# activemq dstat --jmxurl service:jmx:rmi:///jndi/rmi://activemq:1099/jmxrmi

Name                                                Queue Size  Producer #  Consumer #   Enqueue #   Dequeue #    Memory %
SAMPLE                                                       0           0           0           0           0           0
ActiveMQ.Advisory.MasterBroker                               0           0           0           1           0           0
```

* キューの設定関連で読むべきドキュメント
    * [[Producer Flow Control]](http://activemq.apache.org/producer-flow-control.html)
    * [[Per Destination Policies]](http://activemq.apache.org/per-destination-policies.html)
    * [[Message Cursors]](http://activemq.apache.org/message-cursors.html)

## キューにメッセージを送受信する。
* [サンプル](http://activemq.apache.org/hello-world.html)を参考に、メッセージ送受信プログラム(App.java)を作成する。
* 送信対象のキュー名は TEST.FOO 。送信メッセージは固定。
* App.javaはサンプルをコピペしてよい。
    * 変更箇所(HelloWorldProducer, HelloWorldConsumer の二箇所)
        *  ActiveMQConnectionFactory("vm://localhost")　→ ActiveMQConnectionFactory("tcp://localhost:61616")

```
[root@activemq /]# mkdir /tmp/sample
[root@activemq /]# cd /tmp/sample
[root@activemq /]# vim App.java
```

* App.jarを作成する。

```
[root@activemq /]# javac -classpath /opt/activemq/current/activemq-all-5.9.1.jar App.java
```

* MANIFEST.MF を編集する。

```
[root@activemq /]# jar cvf App.jar *.class
[root@activemq /]# jar xvf App.jar
[root@activemq /]# vim META-INF/MANIFEST.MF

Manifest-Version: 1.0
Created-By: 1.7.0_91 (Oracle Corporation)
Main-Class: App ← 追加
```

* MANIFEST.MF を取り込んで App.jarを作成する。

```
[root@activemq /]# jar cvfm App.jar META-INF/MANIFEST.MF *.class
```

* App.javaを実行する。

```
[root@activemq /]# java -classpath /opt/activemq/current/activemq-all-5.9.1.jar:App.jar -Dlog4j.configuration=file:///opt/activemq/current/conf/log4j.properties App


Sent message: 311814461 : Thread-0
Sent message: 1626138246 : Thread-1
Sent message: 635445017 : Thread-6
Sent message: 270519011 : Thread-4
Received: Hello world! From: Thread-1 : 1975214396
Received: Hello world! From: Thread-4 : 1739647788
Received: Hello world! From: Thread-0 : 740393997
Sent message: 189640158 : Thread-13
Sent message: 1989515980 : Thread-14
Received: Hello world! From: Thread-6 : 944025979
Sent message: 1861339376 : Thread-10
Received: Hello world! From: Thread-13 : 704680309
Received: Hello world! From: Thread-14 : 416403358
Sent message: 367683957 : Thread-24
Sent message: 943834195 : Thread-22
Sent message: 785609991 : Thread-17
Received: Hello world! From: Thread-10 : 1220855602
Sent message: 26907556 : Thread-27
Sent message: 1321511429 : Thread-20
Received: Hello world! From: Thread-24 : 852780005
Received: Hello world! From: Thread-22 : 1353812880
Received: Hello world! From: Thread-17 : 1385746758
Received: Hello world! From: Thread-27 : 792834113
Received: Hello world! From: Thread-20 : 2016083685
```

* キューの情報を表示する。
    * App.java で指定した TEST.FOO キューが作成され、12個のメッセージが送受信されている。

```
[root@activemq /]# activemq dstat --jmxurl service:jmx:rmi:///jndi/rmi://activemq:1099/jmxrmi

Name                                                Queue Size  Producer #  Consumer #   Enqueue #   Dequeue #    Memory %
SAMPLE                                                       0           0           0           0           0           0
ActiveMQ.Advisory.Queue                                      0           0           0           1           0           0
ActiveMQ.Advisory.Producer.Queue.TEST.FOO                    0           0           0          24           0           0
ActiveMQ.Advisory.Connection                                 0           0           0          48           0           0
TEST.FOO                                                     0           0           0          12          12           0
ActiveMQ.Advisory.MasterBroker                               0           0           0           1           0           0
ActiveMQ.Advisory.Consumer.Queue.TEST.FOO                    0           0           0          24           0           0
```

## キューにメッセージを送信する。
* [サンプル](http://activemq.apache.org/hello-world.html)を参考に、メッセージ送信プログラム(Producer.java)を作成する。
* 送信対象のキュー名は SAMPLE 。送信メッセージは実行時に引数として指定可能。
* Producer.java の例

* Producer.javaをコンパイルする。

```
[root@activemq /]# javac -classpath /opt/activemq/current/activemq-all-5.9.1.jar Producer.java
```

* Producer.javaを実行する。引数として送信メッセージ(Hello World)を指定する。

```
[root@activemq /]# java -classpath .:/opt/activemq/current/activemq-all-5.9.1.jar -Dlog4j.configuration=file:///opt/activemq/current/conf/log4j.properties Producer "Hello World"         
```

* キューの情報を表示する。
    * Producer.java で指定した SAMPLE キューに1個のメッセージが送信されている。

```
[root@activemq /]# activemq dstat --jmxurl service:jmx:rmi:///jndi/rmi://activemq:1099/jmxrmi

Name                                                Queue Size  Producer #  Consumer #   Enqueue #   Dequeue #    Memory %
SAMPLE                                                       1           0           0           1           0           0
ActiveMQ.Advisory.Queue                                      0           0           0           1           0           0
ActiveMQ.Advisory.Producer.Queue.TEST.FOO                    0           0           0          24           0           0
ActiveMQ.Advisory.Connection                                 0           0           0          50           0           0
TEST.FOO                                                     0           0           0          12          12           0
ActiveMQ.Advisory.MasterBroker                               0           0           0           1           0           0
ActiveMQ.Advisory.Consumer.Queue.TEST.FOO                    0           0           0          24           0           0
ActiveMQ.Advisory.Producer.Queue.SAMPLE                      0           0           0           2           0           0
```

* メッセージの情報を表示する。

```
[root@activemq /]# activemq browse --amqurl tcp://localhost:61616 SAMPLE

JMS_BODY_FIELD:JMSText = Hello World
JMS_HEADER_FIELD:JMSExpiration = 0
JMS_HEADER_FIELD:JMSMessageID = ID:activemq-34211-1446632149371-1:1:1:1:1
JMS_HEADER_FIELD:JMSPriority = 4
JMS_HEADER_FIELD:JMSDestination = SAMPLE
JMS_HEADER_FIELD:JMSTimestamp = 1446632149593
JMS_HEADER_FIELD:JMSRedelivered = false
JMS_HEADER_FIELD:JMSDeliveryMode = non-persistent
```

* TODO
    * maxBrowsePageSize の確認

* JBoss A-MQのドキュメント
    * https://access.redhat.com/documentation/en-US/Red_Hat_JBoss_A-MQ/6.0/html/Installation_Guide/files/FMQInstallVerify.html
    * https://access.redhat.com/documentation/en-US/Red_Hat_JBoss_A-MQ/6.0/html/Console_Reference/files/ConsoleRefIntro.html
    * https://access.redhat.com/documentation/en-US/Red_Hat_JBoss_A-MQ/6.0/html/Console_Reference/files/Consoleactivemq.html


