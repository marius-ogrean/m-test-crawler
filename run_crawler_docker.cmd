docker run --network m-test-crawler_default -it ^
--rm ^
-v %~dp0"/crawler-conf.yaml:/apache-storm/crawler-conf.yaml" ^
-v %~dp0"/crawler.flux:/apache-storm/crawler.flux" ^
-v %~dp0"/target/stormcrawler-1.0-SNAPSHOT.jar:/apache-storm/stormcrawler-1.0-SNAPSHOT.jar" ^
storm:latest ^
storm jar target/stormcrawler-1.0-SNAPSHOT.jar org.apache.storm.flux.Flux --remote crawler.flux