
To Delete Topics
------------------
kafka-topics --zookeeper localhost:2181 --delete  --topic prioritize-input-topic
kafka-topics --zookeeper localhost:2181 --delete  --topic prioritize-output-topic
kafka-topics --zookeeper localhost:2181 --delete  --topic prioritize-expired-topic
kafka-topics --zookeeper localhost:2181 --delete  --topic test-prioritize-application-PriorityMessage-changelog

Create Topics
--------------

kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic prioritize-input-topic
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic prioritize-output-topic
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic prioritize-expired-topic

To List topics
----------------
kafka-topics --list --bootstrap-server localhost:9092


To Listen for Output
----------------------
kafka-console-consumer --bootstrap-server localhost:9092 --topic prioritize-output-topic --from-beginning

kafka-console-consumer --bootstrap-server localhost:9092 --topic prioritize-expired-topic --from-beginning

