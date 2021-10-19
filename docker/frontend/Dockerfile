FROM maven:3.8.2-openjdk-17 AS maven

RUN mkdir --parents /docker/

COPY ./docker/pom.xml /docker/pom.xml
COPY ./docker/frontend/src/css /docker/frontend/src/css
COPY ./docker/frontend/src/js /docker/frontend/src/js

WORKDIR /docker/
RUN mvn package -Dminify && find ./frontend/site/res -iname *.js | grep -v "min" | xargs rm
RUN find ./frontend/site/res -iname *.css | grep -v "min" | xargs rm

FROM node:lts-alpine3.14 AS node
RUN npm i -g raml2html

RUN mkdir --parents /docker/frontend/src/raml/
COPY ./docker/frontend/src/raml/ /docker/frontend/src/raml/
RUN raml2html --input /docker/frontend/src/raml/api.raml --output /docker/frontend/api.html

FROM httpd:2.4-alpine
WORKDIR /usr/local/apache2/htdocs/

COPY --from=node /docker/frontend/api.html ./api.html
COPY --from=maven /docker/frontend/site/res ./res

COPY ./docker/frontend/src/site/robots.txt ./robots.txt
COPY ./docker/frontend/src/img ./res/img
COPY ./docker/frontend/src/js/final/*.js ./res/js/
COPY ./docker/frontend/src/css/final/*.css ./res/css/

# Website structure
COPY ./docker/frontend/src/pages/index.html ./index.html
COPY ./docker/frontend/src/pages/results.html ./results.html
COPY ./docker/frontend/src/pages/system.html ./system.html
COPY ./docker/frontend/src/pages/historic/historic_team_daily.html ./historic_team_daily.html
COPY ./docker/frontend/src/pages/historic/historic_team_hourly.html ./historic_team_hourly.html
COPY ./docker/frontend/src/pages/historic/historic_team_monthly.html ./historic_team_monthly.html
COPY ./docker/frontend/src/pages/historic/historic_user_daily.html ./historic_user_daily.html
COPY ./docker/frontend/src/pages/historic/historic_user_hourly.html ./historic_user_hourly.html
COPY ./docker/frontend/src/pages/historic/historic_user_monthly.html ./historic_user_monthly.html
