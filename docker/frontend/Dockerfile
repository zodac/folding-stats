ARG NODE_VERSION
FROM node:${NODE_VERSION}-alpine AS raml
# Install raml2html
RUN npm i -g raml2html

ARG REST_ENDPOINT_URL
ARG TITLE

RUN mkdir /docker/
WORKDIR /docker/

COPY ./docker/frontend/raml/ /docker/apache/raml/

# Replace environment arguments in RAML files, then build RAML output HTML
RUN grep -rl '%REST_ENDPOINT_URL%' . | xargs sed -i "s|%REST_ENDPOINT_URL%|${REST_ENDPOINT_URL}|g" && \
    grep -rl '%TITLE%' . | xargs sed -i "s|%TITLE%|${TITLE}|g" && \
    raml2html --input /docker/apache/raml/api.raml --output /docker/apache/api.html

FROM mysocialobservations/docker-tdewolff-minify AS minifier

ARG REST_ENDPOINT_URL
ARG UPDATE_ENABLED

RUN mkdir /docker/
WORKDIR /docker/

# Copy the JS and CSS files to run the minification
COPY ./docker/frontend/css /docker/apache/css
COPY ./docker/frontend/js /docker/apache/js

# Replace environment arguments in JS files
RUN grep -rl '%REST_ENDPOINT_URL%' . | xargs sed -i "s|%REST_ENDPOINT_URL%|${REST_ENDPOINT_URL}|g" && \
    grep -rl '%UPDATE_ENABLED%' . | xargs sed -i "s|%UPDATE_ENABLED%|${UPDATE_ENABLED}|g"

# Minify JS and CSS files, then rename to end with *.min.*
RUN minify --verbose --match=\.*.css$ --type=css --output /docker/apache/site/res/css/ /docker/apache/css/ && \
    minify --verbose --match=\.*.js$ --type=js --output /docker/apache/site/res/js/ /docker/apache/js/ && \
    minify --verbose --match=\.*.js$ --type=js --output /docker/apache/site/res/js/ /docker/apache/js/util && \
    find /docker/apache/site/res/ -iname "*.css" | sed 's/....$//' | xargs -i mv {}.css {}.min.css && \
    find /docker/apache/site/res/ -iname "*.js" | sed 's/...$//' | xargs -i mv {}.js {}.min.js

FROM httpd:2.4-alpine

# Updating system packages, and installing any required packages
RUN apk update && apk upgrade && \
    apk add \
      brotli

# Apache configuration (SSL certs are provided through a volume mount)
COPY ./docker/frontend/conf/httpd.conf /usr/local/apache2/conf/httpd.conf
COPY ./docker/frontend/conf/httpd-ssl.conf /usr/local/apache2/conf/original/extra/httpd-ssl.conf
COPY ./docker/frontend/conf/httpd-ssl.conf /usr/local/apache2/conf/extra/httpd-ssl.conf

WORKDIR /usr/local/apache2/htdocs/

# Copy the RAML API and minified JS/CSS files
COPY --from=raml /docker/apache/api.html ./api.html
COPY --from=minifier /docker/apache/site/res ./res

# Copy the static content (images, 3PP JS/CSS files)
COPY ./docker/frontend/res/img ./res/img
COPY ./docker/frontend/res/js/*.js ./res/js/
COPY ./docker/frontend/res/css/*.css ./res/css/

# Website structure
COPY ./docker/frontend/pages/robots.txt ./robots.txt
COPY ./docker/frontend/pages/index.html ./index.html

# The index.html file requires the file extension since that is what the axihub URL points to
# The other files are all references relatively, so can get away without the extension to clean up the displayed URL
COPY ./docker/frontend/pages/results.html ./results
COPY ./docker/frontend/pages/system.html ./system
COPY ./docker/frontend/pages/historic/historic_team_daily.html ./historic_team_daily
COPY ./docker/frontend/pages/historic/historic_team_hourly.html ./historic_team_hourly
COPY ./docker/frontend/pages/historic/historic_team_monthly.html ./historic_team_monthly
COPY ./docker/frontend/pages/historic/historic_user_daily.html ./historic_user_daily
COPY ./docker/frontend/pages/historic/historic_user_hourly.html ./historic_user_hourly
COPY ./docker/frontend/pages/historic/historic_user_monthly.html ./historic_user_monthly

ARG FORUM_LINK
ARG ROOT_URL
ARG TITLE

# Replace environment arguments in HTML files
RUN grep -rl '%FORUM_LINK%' . | xargs sed -i "s|%FORUM_LINK%|${FORUM_LINK}|g" && \
    grep -rl '%ROOT_URL%' . | xargs sed -i "s|%ROOT_URL%|${ROOT_URL}|g" && \
    grep -rl '%TITLE%' . | xargs sed -i "s|%TITLE%|${TITLE}|g"
