ARG HTTPD_VERSION
ARG MINIFY_VERSION
ARG NODE_VERSION
FROM node:${NODE_VERSION}-alpine AS raml
# Install raml2html
RUN npm install --ignore-scripts -g raml2html

ARG REST_ENDPOINT_URL
ARG TITLE

RUN mkdir /docker/
WORKDIR /docker/

COPY ./docker/frontend/raml/ /docker/apache/raml/

# Replace environment arguments in RAML files, then build RAML output HTML
SHELL ["/bin/ash", "-o", "pipefail", "-c"]
RUN grep -rl '%REST_ENDPOINT_URL%' . | xargs sed -i "s|%REST_ENDPOINT_URL%|${REST_ENDPOINT_URL}|g" && \
    grep -rl '%TITLE%' . | xargs sed -i "s|%TITLE%|${TITLE}|g" && \
    raml2html --input /docker/apache/raml/api.raml --output /docker/apache/api.html

FROM tdewolff/minify:${MINIFY_VERSION} AS minifier

ARG REST_ENDPOINT_URL
ARG UPDATE_ENABLED

RUN mkdir /docker/
WORKDIR /docker/

# Copy the JS and CSS files to run the minification
COPY ./docker/frontend/css /docker/apache/css
COPY ./docker/frontend/js /docker/apache/js

# Replace environment arguments in JS files
SHELL ["/bin/ash", "-o", "pipefail", "-c"]
RUN grep -rl '%REST_ENDPOINT_URL%' . | xargs sed -i "s|%REST_ENDPOINT_URL%|${REST_ENDPOINT_URL}|g" && \
    grep -rl '%UPDATE_ENABLED%' . | xargs sed -i "s|%UPDATE_ENABLED%|${UPDATE_ENABLED}|g"

# Minify JS and CSS files, then rename to end with *.min.*
WORKDIR /docker/apache/
RUN minify --recursive --verbose --type=css --output /docker/apache/site/res/css/ css/ && \
    minify --recursive --verbose --type=js --output /docker/apache/site/res/js/ js/ && \
    mv /docker/apache/site/res/js/util/*.js /docker/apache/site/res/js/ && \
    rm -rf /docker/apache/site/res/js/util/ && \
    find /docker/apache/site/res/ -iname "*.css" | sed 's|....$||' | xargs -i mv {}.css {}.min.css && \
    find /docker/apache/site/res/ -iname "*.js" | sed 's|...$||' | xargs -i mv {}.js {}.min.js

FROM httpd:${HTTPD_VERSION}-alpine

# Installing any required packages
RUN apk --no-cache add \
      brotli

# Apache configuration (SSL certs are provided through a volume mount)
COPY ./docker/frontend/conf/httpd.conf /usr/local/apache2/conf/httpd.conf
COPY ./docker/frontend/conf/httpd-ssl.conf /usr/local/apache2/conf/extra/httpd-ssl.conf

ARG ADMIN_EMAIL_ADDRESS
ARG ROOT_URL

# Replace environment arguments in httpd config files
RUN sed -i -e "s|%ROOT_URL%|${ROOT_URL}|g" /usr/local/apache2/conf/extra/httpd-ssl.conf && \
    sed -i -e "s|%ADMIN_EMAIL_ADDRESS%|${ADMIN_EMAIL_ADDRESS}|g" /usr/local/apache2/conf/extra/httpd-ssl.conf

WORKDIR /usr/local/apache2/htdocs/

# Website structure
COPY ./docker/frontend/pages/robots.txt \
     ./docker/frontend/pages/index.html \
     ./

# The index.html file requires the file extension since that is what the axihub URL points to
# The other files are all referenced relatively, so can get away without the extension to clean up the displayed URL
COPY ./docker/frontend/pages/results.html ./results
COPY ./docker/frontend/pages/admin.html ./admin
COPY ./docker/frontend/pages/requests.html ./requests
COPY ./docker/frontend/pages/historic/historic_team_daily.html ./historic_team_daily
COPY ./docker/frontend/pages/historic/historic_team_hourly.html ./historic_team_hourly
COPY ./docker/frontend/pages/historic/historic_team_monthly.html ./historic_team_monthly
COPY ./docker/frontend/pages/historic/historic_user_daily.html ./historic_user_daily
COPY ./docker/frontend/pages/historic/historic_user_hourly.html ./historic_user_hourly
COPY ./docker/frontend/pages/historic/historic_user_monthly.html ./historic_user_monthly

# Copy the RAML API and minified JS/CSS files
COPY --from=raml /docker/apache/api.html ./api.html
COPY --from=minifier /docker/apache/site/res ./res

# Add a hash to each of the JS files to avoid Apache caching them after changes are made
# Also update all existing HTML pages references to the files to the new name
COPY ./docker/frontend/scripts/hash_files.sh /hash_files.sh
RUN chmod +x /hash_files.sh && \
    sh /hash_files.sh "js" && \
    sh /hash_files.sh "css" && \
    rm -f /usr/local/apache2/htdocs/hash_files.sh

# Copy the static content (images, 3PP JS/CSS files) only after the src JS files have been renamed and moved
COPY ./docker/frontend/res/img ./res/img
COPY ./docker/frontend/res/js/*.js ./res/js/
COPY ./docker/frontend/res/css/*.css ./res/css/

ARG FORUM_LINK
ARG ROOT_URL
ARG TITLE

# Replace environment arguments in HTML files
SHELL ["/bin/ash", "-o", "pipefail", "-c"]
RUN grep -rl '%FORUM_LINK%' . | xargs sed -i "s|%FORUM_LINK%|${FORUM_LINK}|g" && \
    grep -rl '%ROOT_URL%' . | xargs sed -i "s|%ROOT_URL%|${ROOT_URL}|g" && \
    grep -rl '%TITLE%' . | xargs sed -i "s|%TITLE%|${TITLE}|g"

# Create non-root user, make owner of log directory
RUN adduser -D frontend_user && \
    mkdir --parents /usr/local/apache2/logs && \
    chown -R frontend_user:frontend_user /usr/local/apache2/logs
USER frontend_user

HEALTHCHECK --interval=30s \
            --timeout=5s \
            --start-period=10s \
            --retries=3 \
            CMD [ "/usr/bin/wget", "--no-check-certificate", "--no-verbose", "--tries=1", "--spider", "https://localhost:443" ]
