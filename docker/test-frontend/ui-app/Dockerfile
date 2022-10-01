ARG NODE_VERSION
FROM node:${NODE_VERSION}-alpine

WORKDIR /app
# Add node_modules to PATH
ENV PATH /app/node_modules/.bin:$PATH

# Copy dependencies
COPY ./docker/test-frontend/ui-app/package.json .
COPY ./docker/test-frontend/ui-app/package-lock.json .

RUN npm install
COPY ./docker/test-frontend/ui-app/. .

EXPOSE 3000

# Start application
CMD ["npm", "start"]
