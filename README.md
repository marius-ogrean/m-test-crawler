Steps to run the crawler:
-

1. First follow the steps in the project m-test-crawler-api so that you build the Docker image for the api
2. Go to the root folder of THIS project
3. Run "mvn clean package" to build the jar
4. Run the script run_docker_compose.cmd or the command inside it
5. Run the script put_urls.cmd or the command inside it
6. Open a browser and go to this url: http://localhost:4444/companyData/uploadCompanyNamesFromFile (I made it a GET so that you can run it directly without having to use a tool to send a POST)
7. Run the script run_crawler_docker_windows.cmd or on Linux/Mac run_crawler_docker_linux.sh (I have at home a Windows machine, I couldn't test the sh script)

How to view the status of the crawl:
-
You can check the coverage here: http://localhost:4444/companyData/coverage

You can check the fill rate here: http://localhost:4444/companyData/fillRate

You can query the solr server here: http://localhost:8983/solr/#/companies/query

You can view the StormUI here: http://localhost:8080/

If you want to kill the crawl, inside StormUI click the crawler under Topology Summary and press Kill. After killing the crawler you can't start it again. You have to delete all the docker containers and start from step 4  from above.

About the performance of the crawler:
-

It doesn't finish crawling under 10 minutes in the current setup because it is only one Storm worker running. If we would have a Storm cluster of multiple nodes, each running workers, the crawl would run much quicker.

Also, the Solr performance can be improved if necessary with a Solr cluster

