import * as fs from 'fs';
import * as uuidv1 from 'uuid/v1';
const grpc = require('grpc');
import * as protoLoader from '@grpc/proto-loader';
import {processorApiConfig} from '../config/app-config';

const PROTO_PATH = processorApiConfig.grpc.bluePrintManagement.protoPath;

// Suggested options for similarity to existing grpc.load behavior
const packageDefinition: protoLoader.PackageDefinition = protoLoader.loadSync(
    PROTO_PATH,
    {
        keepCase: true,
        longs: String,
        enums: String,
        defaults: true,
        oneofs: true
    });

const protoDescriptor = grpc.loadPackageDefinition(packageDefinition);
// The protoDescriptor object has the full package hierarchy

const stub = new protoDescriptor.org.onap.ccsdk.cds.controllerblueprints.management.api.BluePrintManagementService(
    "" + processorApiConfig.grpc.host + ":" + processorApiConfig.grpc.port + "",
    grpc.credentials.createInsecure());

const metadata = new grpc.Metadata();
metadata.add('Authorization', processorApiConfig.grpc.authToken);

class BluePrintManagementServiceGrpcClient {

    async uploadBlueprint(filePath: string): Promise<any> {

        let input = {
            commonHeader: {
                timestamp: new Date(),
                originatorId: "cds-ui",
                requestId: uuidv1(),
                subRequestId: "1234-56",
            },
            fileChunk: {
                chunk: fs.readFileSync(filePath)
            }
        }

        let removeTempFile = () => {
            fs.unlink(filePath, (err: any) => {
                if (err) {
                    console.error(err);
                }
            });
        }

        return new Promise<any>((resolve, reject) => {
            stub.uploadBlueprint(input, metadata, (err: any, output: any) => {
                if (err) {
                    removeTempFile();
                    reject(err);
                    return;
                }

                removeTempFile();
                resolve(output);
            });
        });

    }
}

export const bluePrintManagementServiceGrpcClient = new BluePrintManagementServiceGrpcClient();

