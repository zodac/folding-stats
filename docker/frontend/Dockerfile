FROM maven:3.8.2-openjdk-17 AS maven

ARG ROOT_URL

RUN mkdir --parents /docker/
WORKDIR /docker/

# Copy the JS and CSS files (along with the pom.xml) to run the minify plugin
COPY ./docker/pom.xml /docker/pom.xml
COPY ./docker/frontend/src/css /docker/frontend/src/css
COPY ./docker/frontend/src/js /docker/frontend/src/js

# 1 - Replace '%ROOT_URL%' in the JS files with the actual URL for the deployment (changing sed delimiter for URL slashes)
# 2 - Run the minification and merging of JS and CSS files
# 3 - Remove any files that are not minified (do not include 'min' in their filename)
RUN grep -rl '%ROOT_URL%' . | xargs sed -i "s|%ROOT_URL%|${ROOT_URL}|g" && \
    mvn package -Dminify && \
    find ./frontend/site/res -iname *.js -o -iname *.css | grep -v "min" | xargs rm

FROM node:lts-alpine3.14 AS node
# Install raml2html
RUN npm i -g raml2html

COPY ./docker/frontend/src/raml/ /docker/frontend/src/raml/
RUN raml2html --input /docker/frontend/src/raml/api.raml --output /docker/frontend/api.html

FROM httpd:2.4-alpine
WORKDIR /usr/local/apache2/htdocs/

# Used for healthcheck
RUN apk add curl

# Copy the RAML API and minified JS/CSS files
COPY --from=node /docker/frontend/api.html ./api.html
COPY --from=maven /docker/frontend/site/res ./res

# Copy the static content (images, 3PP JS/CSS files)
COPY ./docker/frontend/src/img ./res/img
COPY ./docker/frontend/src/js/final/*.js ./res/js/
COPY ./docker/frontend/src/css/final/*.css ./res/css/

# Website structure
COPY ./docker/frontend/src/pages/robots.txt ./robots.txt
COPY ./docker/frontend/src/pages/index.html ./index.html

# The index.html file requires the file extension since that is what the axihub URL points to
# The other files are all references relatively, so can get away without the extension to clean up the displayed URL
COPY ./docker/frontend/src/pages/results.html ./results
COPY ./docker/frontend/src/pages/system.html ./system
COPY ./docker/frontend/src/pages/historic/historic_team_daily.html ./historic_team_daily
COPY ./docker/frontend/src/pages/historic/historic_team_hourly.html ./historic_team_hourly
COPY ./docker/frontend/src/pages/historic/historic_team_monthly.html ./historic_team_monthly
COPY ./docker/frontend/src/pages/historic/historic_user_daily.html ./historic_user_daily
COPY ./docker/frontend/src/pages/historic/historic_user_hourly.html ./historic_user_hourly
COPY ./docker/frontend/src/pages/historic/historic_user_monthly.html ./historic_user_monthly
