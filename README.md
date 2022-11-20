# Search-engine
This application is a Spring application (a JAR file that runs on any server or computer) that works with a locally installed MySQL database, has a simple web interface and API through which it can be managed and receive search results on request in Russian. The application is at the stage of checking the code style and optimizing the operation of the main components. However, you can use this application right now.
# The principle of operation. 
1.Before launching the application, specify in the configuration file the addresses of the sites on which the engine should search.

2.The search engine independently crawls all the pages of the specified sites and adds them to the index so that you can then find the most relevant pages for any search query.

3.Send a request via the engine API. A query is a set of words by which you need to find the pages of a site.

4.The application receives basic forms of words of the Russian language from the query words according to certain algorithms.

5.The index searches for those pages where all these words occur.

6.Get a sorted search result.
# Startup Settings.
## Dependencies.
This project uses standard dependencies and dependencies from GitHub. To successfully download and connect to the dependencies project from, you need to configure the Maven configuration in the file settings.xml. In the file pom.xml added repository for getting jar files: <template>
 
  ```html
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub Apache Maven Packages - Russian Morphology</name>
        <url>https://maven.pkg.github.com/skillbox-java/russianmorphology</url>
    </repository>
</repositories>
  ```
  
 Since GitHub requires token authorization to get data from a public repository, to specify the token, find the file settings.xml
-In Windows, it is located in the directory C:/Users /<Your user name>/.m2
-In Linux directory /home/<Your user name>/.m2
-In macOS at /Users/<Your user name>/.m2
Attention! 
The current token, the string to be inserted into the <value> tag...</value> is located in the document by reference
https://docs.google.com/document/d/1REA7E14HWNvpp3a6XqEoPLcISaxkDe8iQXVv98Nzktw/edit 
  ```html
  <servers>
    <server>
        <id>github</id>
        <configuration>
            <httpHeaders>
                <property>
                    <name>Authorization</name>
                    <value>Токен доступа</value>
                </property>
            </httpHeaders>
        </configuration>
    </server>
</servers>
   ```
Don't forget to change the token to the current one!
  
❗️If there is no file, then create settings.xml and insert into it:
```html
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
 https://maven.apache.org/xsd/settings-1.0.0.xsd">

   <servers>
       <server>
           <id>github</id>
           <configuration>
               <httpHeaders>
                   <property>
                       <name>Authorization</name>
                       <value>Токен доступа</value>
                   </property>
               </httpHeaders>
           </configuration>
        </server>
   </servers>

</settings>
``` 
Don't forget to change the token to the current one!
  
After that, update the dependencies in the project or force update the data from pom.xml 
  
To do this, call the context menu of the pom file.xml in the Project file tree and select the menu item Maven -> Reload Project.
  
If after that you still have an error:
 ```html
Could not transfer artifact org.apache.lucene.morphology:morph:pom:1.5
from/to github (https://maven.pkg.github.com/skillbox-java/russianmorphology):
authentication failed for
https://maven.pkg.github.com/skillbox-java/russianmorphology/org/apache/lucene/morphology/morph/1.5/morph-1.5.pom,
status: 401 Unauthorized
 ```
 Clean the Maven cache. The most reliable way is to delete a directory:

-Windows C:\Users\<user_name>\.m2\repository

-macOs /Users/<user_name>/.m2/repository

-Linux /home/<user_name>/.m2/repository
  
 After that, try again to update this from pom.xml
 ## DB Connection Settings.
 A driver for connecting to the MySQL database has been added to the project. To run the project, make sure that you have a MySQL 8.x server running.
 If you have docker installed, you can run a container with ready-made settings for the project with the command:
 ```html
 docker run -d --name=Search-engine -e="MYSQL_ROOT_PASSWORD=11111111" -e="MYSQL_DATABASE=search_engine" -p3306:3306 mysql
 ```
 The default user name is root, the project settings in src/resources/application.yml correspond to the container settings, you do not need to change them.
 
 ❗️ If you have a MacBook with an M1 processor, you need to use a special image for ARM processors:
 ```html
 docker run -d --name=Search-engine -e="MYSQL_ROOT_PASSWORD=11111111" -e="MYSQL_DATABASE=search_engine" -p3306:3306 arm64v8/mysql:oracle
 ```
 If you use MySQL without docker, then create a search_engine database and replace the username and password in the src/resources/application.yml configuration file:
 ```html
 spring.jpa.hibernate.use-new-id-generator-mappings: false
 spring.datasource.url: jdbc:mysql://localhost:3306/search_engine?useSSL=false
 spring.datasource.username: root # user name
 spring.datasource.password: 11111111 # password
 ```
 After that, you can start the project. If the correct data is entered, the project will start successfully. If the launch ends with errors, study the error text, make
 corrections and try again.
 
 You can also use PostgreSQL DB in the application.
 
 ## Indexing Settings.
 Indexing is the process of forming a search index based on a certain amount of information. A search index is a specially organized database that allows you to 
 quickly and conveniently search for this information.
 The speed of search by the search index in any search engines, as a rule, takes a short time (usually fractions of a second) compared to the usual search by searching 
 through the entire array of information.
 For successful indexing, you must specify a site or a list of sites in application.yml as shown in the example:
 ```html
 sites:
        	- url: https://www.lenta.ru/
           name: Лента.ру
         - url: https://www.skillbox.ru/
           name: Skillbox
 ```
 For the correct relevance of the found pages, it is also necessary to specify its value for the page fields in application.yml. For example:
 ```html
 fields:
  fieldList:
    - name: title
      selector: title
      weight: 1.0 # relevance
    - name: body
      selector: body
      weight: 0.8 # relevance
 ```

 If you want to create new data after application restart, you need to set "newDB" field to "true", else set to "false" in application.yml.
 The relevance (weight) must be specified between 0 and 1.
 # Project structure
 The application uses the concept of MVC.
