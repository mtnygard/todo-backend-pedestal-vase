FROM java:8
MAINTAINER Your Name <you@example.com>

ADD target/vase-osv-0.0.1-SNAPSHOT-standalone.jar /vase-osv/app.jar

EXPOSE 8080

CMD ["java", "-cp", "/vase-osv/app.jar", "clojure.main", "-m", "vase-osv"]
