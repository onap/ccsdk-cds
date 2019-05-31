import { createLogger, format, transports } from 'winston';

const { combine, timestamp, printf, splat, errors, colorize } = format;
const logFormat = printf(({ level, message, timestamp }) => {
    return `${timestamp} ${level} ${message}`
});
const logger = createLogger({
    level: 'info',
    format: combine(
        splat(),
        timestamp(),
        colorize(),
        errors({ stack: true }),
        logFormat
    ),
    transports: [
        new transports.Console()
    ]
});

if (process.env.NODE_ENV === 'production') {
    logger.add(new transports.File({ filename: '/var/log/ONAP/CDS-UI/server/server.log'}))
}

export { logger };