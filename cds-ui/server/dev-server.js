// Dev startup: HTTP only, no AAF TLS certs required.
// Usage: node dev-server.js
const application = require('./dist');

const config = {
    rest: {
        protocol: 'http',
        port: +process.env.PORT || 3000,
        host: process.env.HOST || 'localhost',
        openApiSpec: { setServersFromRequest: true },
    },
};

application.main(config).catch(err => {
    console.error('Cannot start the application.', err);
    process.exit(1);
});
