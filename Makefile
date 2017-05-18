VERSION=1.0.24

deploy-snapshot:
	mvn clean install -DskipTests deploy:deploy -DaltDeploymentRepository=oss-jfrog::default::http://oss.jfrog.org/artifactory/oss-snapshot-local

galeb-manager: clean
	mvn package -DskipTests

test:
	mvn test

clean:
	mvn clean; rm -f galeb-manager-${VERSION}-1.el7.noarch.rpm

dist: galeb-manager
	type fpm > /dev/null 2>&1 && \
  cd target && \
  fpm -s dir \
      -t rpm \
      -n "galeb-manager" \
      -v ${VERSION} \
      --iteration 1.el7 \
      -a noarch \
      --rpm-os linux \
      --prefix /opt/galeb/manager/lib \
      -m '<galeb@corp.globo.com>' \
      --vendor 'Globo.com' \
      --description 'Galeb manager service' \
      -f -p ../galeb-manager-${VERSION}-1.el7.noarch.rpm galeb-manager-${VERSION}-SNAPSHOT.jar && \
  cd -; \
