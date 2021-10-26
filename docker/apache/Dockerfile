FROM maven:3.8.2-openjdk-17 AS maven

ARG ROOT_URL

RUN mkdir --parents /docker/
WORKDIR /docker/

# Copy the JS and CSS files (along with the pom.xmls) to run the minify plugin
COPY ./pom.xml /pom.xml
COPY ./docker/pom.xml /docker/pom.xml
COPY ./docker/apache/css /docker/apache/css
COPY ./docker/apache/js /docker/apache/js

# 1 - Replace '%ROOT_URL%' in the JS files with the actual URL for the deployment (changing sed delimiter for URL slashes)
# 2 - Run the minification and merging of JS and CSS files
# 3 - Remove any files that are not minified (do not include 'min' in their filename)
RUN grep -rl '%ROOT_URL%' . | xargs sed -i "s|%ROOT_URL%|${ROOT_URL}|g" && \
    mvn package -Dminify && \
    find ./apache/site/res -iname *.js -o -iname *.css | grep -v "min" | xargs rm

FROM node:lts-alpine3.14 AS node
# Install raml2html
RUN npm i -g raml2html

COPY ./docker/apache/raml/ /docker/apache/raml/
RUN raml2html --input /docker/apache/raml/api.raml --output /docker/apache/api.html

FROM httpd:2.4-alpine
WORKDIR /usr/local/apache2/htdocs/

# Used for healthcheck
RUN apk add curl

# Copy the RAML API and minified JS/CSS files
COPY --from=node /docker/apache/api.html ./api.html
COPY --from=maven /docker/apache/site/res ./res

# Copy the static content (images, 3PP JS/CSS files)
COPY ./docker/apache/res/img ./res/img
COPY ./docker/apache/res/js/*.js ./res/js/
COPY ./docker/apache/res/css/*.css ./res/css/

# Website structure
COPY ./docker/apache/pages/robots.txt ./robots.txt
COPY ./docker/apache/pages/index.html ./index.html

# The index.html file requires the file extension since that is what the axihub URL points to
# The other files are all references relatively, so can get away without the extension to clean up the displayed URL
COPY ./docker/apache/pages/results.html ./results
COPY ./docker/apache/pages/system.html ./system
COPY ./docker/apache/pages/historic/historic_team_daily.html ./historic_team_daily
COPY ./docker/apache/pages/historic/historic_team_hourly.html ./historic_team_hourly
COPY ./docker/apache/pages/historic/historic_team_monthly.html ./historic_team_monthly
COPY ./docker/apache/pages/historic/historic_user_daily.html ./historic_user_daily
COPY ./docker/apache/pages/historic/historic_user_hourly.html ./historic_user_hourly
COPY ./docker/apache/pages/historic/historic_user_monthly.html ./historic_user_monthly
