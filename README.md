### HOW TO

1. Requirements
       
    * sbt 0.13.8
    * java 1.8
    * scala 2.11
    
2. Running app
    
    * Source code can be found in directory `./github-twitter-search`
      
    * package and run app
    
          1.   configure twitter OAuth2 credentials:
               set twitter key and secret in file `./github-twitter-search/src/main/resources/application.conf`
                    
          2.   package app to jar file
                     
                    run `sbt assembly`
          
          2.   running app
                    
            run `sbt run`
                    
            or
                     
            run `java -jar ./target/scala-2.12/github-twitter-search-assembly-1.0.jar`