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
    find /docker/apache/site/res/ -iname "*.css" | sed 's|....$||' | xargs -i mv {}.css {}.min.css && \
    find /docker/apache/site/res/ -iname "*.js" | sed 's|...$||' | xargs -i mv {}.js {}.min.js

FROM httpd:2.4-alpine

# Updating system packages, and installing any required packages
RUN apk update && apk upgrade && \
    apk add \
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
WORKDIR /usr/local/apache2/htdocs/res/js
RUN for file_name in *.js; do \
      base=$(basename "${file_name}") && \
      name_without_extension=$(echo "${base%%.*}")  && \
      hash=$(cat "${file_name}" | md5sum | tr -s ' ' | cut -d ' ' -f1) && \
      new_file_name="${name_without_extension}-${hash}.min.js" && \
      mv "${file_name}" "${new_file_name}" && \
      echo "'${file_name}'" && \
      grep -rl "${file_name}" /usr/local/apache2/htdocs | xargs sed -i "s|${file_name}|${new_file_name}|g" \
    ; done
WORKDIR /usr/local/apache2/htdocs/

# Do the same for the CSS files
WORKDIR /usr/local/apache2/htdocs/res/css
RUN for file_name in *.css; do \
      base=$(basename "${file_name}") && \
      name_without_extension=$(echo "${base%%.*}")  && \
      hash=$(cat "${file_name}" | md5sum | tr -s ' ' | cut -d ' ' -f1) && \
      new_file_name="${name_without_extension}-${hash}.min.css" && \
      mv "${file_name}" "${new_file_name}" && \
      echo "'${file_name}'" && \
      grep -rl "${file_name}" /usr/local/apache2/htdocs | xargs sed -i "s|${file_name}|${new_file_name}|g" \
    ; done
WORKDIR /usr/local/apache2/htdocs/

# Copy the static content (images, 3PP JS/CSS files) only after the src JS files have been renamed and moved
COPY ./docker/frontend/res/img ./res/img
COPY ./docker/frontend/res/js/*.js ./res/js/
COPY ./docker/frontend/res/css/*.css ./res/css/

ARG FORUM_LINK
ARG ROOT_URL
ARG TITLE

# Replace environment arguments in HTML files
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
