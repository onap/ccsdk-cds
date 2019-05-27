import { Interceptor } from '@loopback/context';
import { logger } from '../logger/logger';

const logInterceptor: Interceptor = async (invocationCtx, next) => {
    logger.info('log: before-%s', invocationCtx.methodName);
    try {
        const result = await next();
        logger.info('log: after-%s', invocationCtx.methodName);
        return result;
    } catch (err) {
        logger.error('logError: error-%s', invocationCtx.methodName);
        throw err;
    }
};

export { logInterceptor };
