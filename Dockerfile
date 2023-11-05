FROM azul/zulu-openjdk:17

VOLUME /tmp
ADD target/pdfmaker.jar app.jar

ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN sed -i '/ko_KR.UTF-8/s/^# //g' /etc/locale.gen && locale-gen
ENV LANG ko_KR.UTF-8
ENV LANGUAGE ko_KR.UTF-8
ENV LC_ALL ko_KR.UTF-8

ENV JAVA_OPTS="-XX:TieredStopAtLevel=1 -Djava.security.egd=file:/dev/./urandom "
ENTRYPOINT exec java $JAVA_OPTS -jar /app.jar