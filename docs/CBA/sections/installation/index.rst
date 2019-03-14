.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2019 IBM.

Installation
============
   
Building client html and js files
=================================

	* FROM alpine:3.8 as builder

	* RUN apk add --no-cache npm

	* WORKDIR /opt/cds-ui/client/

	* COPY client/package.json /opt/cds-ui/client/

	* RUN npm install

	* COPY client /opt/cds-ui/client/

	* RUN npm run build


Building and creating server
============================

	* FROM alpine:3.8

	* WORKDIR /opt/cds-ui/

	* RUN apk add --no-cache npm

	* COPY server/package.json /opt/cds-ui/

	* RUN npm install

	* COPY server /opt/cds-ui/
	* COPY --from=builder /opt/cds-ui/server/public /opt/cds-ui/public

	* RUN npm run build

	* EXPOSE 3000

	* CMD [ "npm", "start" ]