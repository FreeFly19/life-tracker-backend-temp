### Build and Publish Docker image

```
<project-root>$ docker build . -t freefly19/life-tracker-temp
<project-root>$ docker push freefly19/life-tracker-temp
```

### Run on production server

```
<project-root>$ docker-compose up -d
```

### Run locally

Step #1: Run postgres

```
<project-root>$ cd dev
<project-root>/dev$ docker-compose up -d
```

Step #2: Make code changes and run the app

```
<project-root>$ sbt run
```

