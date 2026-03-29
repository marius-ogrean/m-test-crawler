About the crawler:
-

It uses Apache StormCrawler: https://stormcrawler.apache.org/

It is an open source project based on Apache Storm: https://storm.apache.org/

Apache Storm being a "distributed realtime computation system" it allows scaling the performance by adding more machines (nodes).

That's why I chose it, to ensure that high performance requirements can be met.

The logic of the parsing of datapoints is in the class CompanyDataIndexer. The datapoints are stored in Apache Solr. I chose Solr instead of Elastic Search because it is free.

The id of each document in Solr is the domain name of the website. I chose this since it is already unique and we have them in the list of websites to crawl.

The crawled data from the websites is stored in three fields in the document: phoneData, socialsData and addressData.

The crawling of the pages is done like this: the body of the html is taken and a depth search is done on it to find keywords like "phone", "facebook", "instagram" and "address". The text containing that keyword in saved in one of the three fields above. 

Steps to run the crawler:
-

1. First follow the steps in the project https://github.com/marius-ogrean/m-test-crawler-api so that you build the Docker image for the api
2. Go to the root folder of THIS project
3. Run "mvn clean package" to build the jar
4. Run the script run_docker_compose.cmd or the command inside it
5. Run the script put_urls.cmd or the command inside it
6. Open a browser and go to this url: http://localhost:4444/companyData/uploadCompanyNamesFromFile (I made it a GET so that you can run it directly without having to use a tool to send a POST)
7. To start the crawl run the script run_crawler_docker_windows.cmd or on Linux/Mac run_crawler_docker_linux.sh (I have at home a Windows machine, I couldn't test the sh script)
8. To improve performance, go to http://localhost:8080/, under Topology Summary click the "crawler", then press Change log level, in the Logger column write ROOT, in the level column pick ERROR and in the Timeout column write 3600. This will stop the logging of all the components. Without it, after 10 minutes the log file gets 11 MB big, with this change, it stays at around 130KB. 

How to view the status of the crawl:
-
You can check the coverage here: http://localhost:4444/companyData/coverage

You can check the fill rate here: http://localhost:4444/companyData/fillRate

You can query the solr server here: http://localhost:8983/solr/#/companies/query Sample query: fromCrawl:(true)

You can view the StormUI here: http://localhost:8080/

You can view details about the crawler pipeline by clicking on the "crawler" under Topology Summary. There you can kill the crawl if you want by pressing the Kill button. After killing the crawler you can't start it again. You have to delete the docker compose project that is running and restart from step 4  from above.

About the performance of this crawler:
-

It doesn't finish crawling under 10 minutes in the current setup because it is only one Storm worker running. If we would have a Storm cluster of multiple nodes, each running workers, the crawl would run much quicker.

Also, the Solr performance can be improved if necessary with a Solr cluster.

When testing it on my machine, with Docker Desktop, the performance varied. Once I got it to crawl 500 of the 1000 websites in around 30 minutes or more. Other times, it gets at around 100-200 crawled in 10 minutes. Stronger machines would have better results with only one worker.

Regarding the extraction of the data points, there is much room to improve. The logic I implemented is pretty simple but the best approach is to use machine learning. 

