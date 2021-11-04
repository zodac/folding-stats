FROM mysocialobservations/docker-tdewolff-minify AS minifier

ARG ROOT_URL
ARG UPDATE_MINUTE

RUN mkdir /docker/
WORKDIR /docker/

# Copy the JS and CSS files to run the minification
COPY ./docker/apache/css /docker/apache/css
COPY ./docker/apache/js /docker/apache/js

# Replace '%ROOT_URL%' in the JS files with the actual URL for the deployment (changing sed delimiter for URL slashes)
# Also replaces '%UPDATE_MINUTE%' for the site timer
RUN grep -rl '%ROOT_URL%' . | xargs sed -i "s|%ROOT_URL%|${ROOT_URL}|g" && \
    grep -rl '%UPDATE_MINUTE%' . | xargs sed -i "s|%UPDATE_MINUTE%|${UPDATE_MINUTE}|g" && \

# Minify JS and CSS files, then rename to end with *.min.*
RUN minify --verbose --match=\.*.css$ --type=css --output /docker/apache/site/res/css/ /docker/apache/css/ && \
    minify --verbose --match=\.*.js$ --type=js --output /docker/apache/site/res/js/ /docker/apache/js/ && \
    minify --verbose --match=\.*.js$ --type=js --output /docker/apache/site/res/js/ /docker/apache/js/util && \
    find /docker/apache/site/res/ -iname "*.css" | sed 's/....$//' | xargs -i mv {}.css {}.min.css && \
    find /docker/apache/site/res/ -iname "*.js" | sed 's/...$//' | xargs -i mv {}.js {}.min.js

FROM node:lts-alpine3.14 AS raml
# Install raml2html
RUN npm i -g raml2html

COPY ./docker/apache/raml/ /docker/apache/raml/
RUN raml2html --input /docker/apache/raml/api.raml --output /docker/apache/api.html

FROM httpd:2.4-alpine

# Apache configuration (SSL certs are provided through a volume mount)
COPY ./docker/apache/conf/httpd.conf /usr/local/apache2/conf/httpd.conf
COPY ./docker/apache/conf/httpd-ssl.conf /usr/local/apache2/conf/original/extra/httpd-ssl.conf
COPY ./docker/apache/conf/httpd-ssl.conf /usr/local/apache2/conf/extra/httpd-ssl.conf

WORKDIR /usr/local/apache2/htdocs/

# Copy the RAML API and minified JS/CSS files
COPY --from=raml /docker/apache/api.html ./api.html
COPY --from=minifier /docker/apache/site/res ./res

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
