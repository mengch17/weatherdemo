# weatherdemo

### Environment Requirement
Java17 or Higher  
Maven 3.8.6 or Higher  
Docker 27.5.1 or Higher ( for running Redis )

### Local Deployment Steps
1.) ```mvn clean install```  
2.) run redis: ```docker-compose up -d``` ( stop redis: ```docker-compose down``` )  
3.) run default ```WeatherdemoApplication``` in Intellij or ```mvn clean install```  
4.) quick test ```curl "http://localhost:8080/v1/weather/health/full"```  
5.) expecting ```Health Check Ok```


### Some Helpful Note:
- Login into redis & check keys:  
1.) ```docker exec -it <CONTAINER_ID> redis-cli```  
2.) ```keys *```

- stop redis: ```docker-compose down```

- delete all data in redis: ```FLUSHDB```

### User Guide
todo