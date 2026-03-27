set location=%cd%

docker run --network m-test-crawler_default -it ^
--rm ^
-v "D:\\Development\\m-test-crawler\\crawler-conf.yaml:/apache-storm/crawler-conf.yaml" ^
-v "D:\\Development\\m-test-crawler\\crawler.flux:/apache-storm/crawler.flux" ^
-v "D:\\Development\\m-test-crawler\\target\\stormcrawler-1.0-SNAPSHOT.jar:/apache-storm/stormcrawler-1.0-SNAPSHOT.jar" ^
storm:latest ^
storm jar /apache-storm/stormcrawler-1.0-SNAPSHOT.jar org.apache.storm.flux.Flux --remote crawler.flux