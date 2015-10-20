## Introduction

This is a little tool which query Solr server with certain query string and save all the results entries into CSV format files and archived (into ZIP). In my environment, solr collections are created everyday and named by the format of yyyyMMdd, to store all the data received that day.
The Solr Server Url format is like: http://[serverIp]:8983/solr/log20150831. You may need to modify the Server Url format to fit your Solr Server Url and make this tool work. Also there are few configurations need to be configured in csv_dumper.properties which is describe more detail in the following section.


## Configurations
There are 4 configurations in the csv_dumper.properties file under conf folder:
baseday: the day you want to start retrieving data, in the date format of yyyyMMdd.
intervaldays: the interval days before the baseday, for example if you set baseday to 20150830 and intervaldays to 10, collections storing data of 2015 8/30, 8/29, 8/28, ..., 8/22, 8/21 will be retreived.
serverip: your Solr server IP.
querystring: your query string to query solr. reference the <b>solr query string format</b>: <a href="https://wiki.apache.org/solr/SolrQuerySyntax">https://wiki.apache.org/solr/SolrQuerySyntax</a>
maxrow: the rows limit in one query. this should be tuning according to your solr server and client to find the best number to retreive in one query. if you set maxrow to 5000, and say there are 7500 results in the colleciton, there will be 2 CSV files generated, one has 5000 rows and the other contains 2500 rows. they will be archived into one ZIP file.


## Libraries Required
During the development of this tool, I use some of java libraries listed below to achieve some function:

* commons-logging-1.1.1.jar
* httpclient-4.3.4.jar
* httpcore-4.4.3.jar

## Questions
This tool is made in a very short time, and it may have many thing needs to be improved. If you have any problem or suggestion, please feel free to contact me at fengchien@gmail.com
