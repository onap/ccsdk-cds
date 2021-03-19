# Prepare stage for multistage image build
## START OF stage0 ##
# Building client html and js files
FROM alpine:3.8 AS stage0
WORKDIR /opt/cds-ui/client/

RUN apk add --no-cache npm

COPY client/package.json /opt/cds-ui/client/
RUN npm install

COPY client /opt/cds-ui/client/
RUN npm run build

## END OF STAGE0 ##

##############################################

## START OF stage1 ##
# Building and creating server
FROM alpine:3.8 AS stage1
WORKDIR /opt/cds-ui/

RUN apk add --no-cache npm

COPY server/package.json /opt/cds-ui/
RUN npm install

COPY server /opt/cds-ui/
RUN npm run build

## END OF STAGE1 ##

##############################################

## This will create actual image

FROM alpine:3.8
WORKDIR /opt/cds-ui/

RUN apk add --no-cache npm

COPY --from=stage0 /opt/cds-ui /opt/cds-ui
COPY --from=stage1 /opt/cds-ui/server/public /opt/cds-ui/public

EXPOSE 3000
CMD [ "npm", "start" ]
