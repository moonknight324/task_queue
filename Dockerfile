FROM eclipse-temurin:17-jdk AS build
WORKDIR /src
COPY src ./src
COPY manifest.txt .
RUN javac -d out src/*.java && jar cfm ScanQueue.jar manifest.txt -C out .

FROM eclipse-temurin:17-jre
ADD https://github.com/tsl0922/ttyd/releases/download/1.7.7/ttyd.x86_64 /usr/local/bin/ttyd
RUN chmod +x /usr/local/bin/ttyd
COPY --from=build /src/ScanQueue.jar /app/ScanQueue.jar
CMD ["sh", "-c", "exec ttyd -p ${PORT:-10000} -W -m 5 -c \"$TTYD_USER:$TTYD_PASS\" java -jar /app/ScanQueue.jar"]
