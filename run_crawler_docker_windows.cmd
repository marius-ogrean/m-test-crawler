docker run --network m-test-crawler_default -it ^
--rm ^
-v ".\\crawler-conf.yaml:/apache-storm/crawler-conf.yaml" ^
-v ".\\crawler.flux:/apache-storm/crawler.flux" ^
-v ".\\target\\stormcrawler-1.0-SNAPSHOT.jar:/apache-storm/stormcrawler-1.0-SNAPSHOT.jar" ^
storm:latest ^
storm jar /apache-storm/stormcrawler-1.0-SNAPSHOT.jar org.apache.storm.flux.Flux --remote crawler.flux