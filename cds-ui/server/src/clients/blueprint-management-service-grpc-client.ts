/**
  ~  Copyright © 2019 Bell Canada.
  ~
  ~  Licensed under the Apache License, Version 2.0 (the "License");
  ~  you may not use this file except in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing, software
  ~  distributed under the License is distributed on an "AS IS" BASIS,
  ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~  See the License for the specific language governing permissions and
  ~  limitations under the License.
*/
import * as fs from 'fs';
import * as uuidv1 from 'uuid/v1';
const grpc = require('grpc');
import * as protoLoader from '@grpc/proto-loader';
import { processorApiConfig } from '../config/app-config';

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

const stub = new protoDescriptor.org.onap.ccsdk.cds.controllerblueprints.management.api.BlueprintManagementService(
    "" + processorApiConfig.grpc.host + ":" + processorApiConfig.grpc.port + "",
    grpc.credentials.createInsecure());

const metadata = new grpc.Metadata();
metadata.add('Authorization', processorApiConfig.grpc.authToken);

class BlueprintManagementServiceGrpcClient {

    async uploadBlueprint(filePath: string, actionName: string): Promise<any> {

        let input = {
            commonHeader: {
                timestamp: new Date(),
                originatorId: "cds-ui",
                requestId: uuidv1(),
                subRequestId: "1234-56",
            },
            fileChunk: {
                chunk: fs.readFileSync(filePath)
            },
            actionIdentifiers: {
                mode: "sync",
                blueprintName: "cds.zip",
                actionName: actionName
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

    async downloadBlueprint(blueprintName: string,blueprintVersion: string): Promise<any> {

        let input = {
            commonHeader: {
                timestamp: new Date(),
                originatorId: "cds-ui",
                requestId: uuidv1(),
                subRequestId: "1234-56",
            },
            actionIdentifiers: {
                mode: "sync",
                blueprintName: blueprintName,
                blueprintVersion: blueprintVersion
            }
        }

        return new Promise<any>((resolve, reject) => {
            stub.downloadBlueprint(input, metadata, (err: any, output: any) => {
                if (err) {
                    reject(err);
                    return;
                }
                resolve(output);
            });
        });

    }
}

export const bluePrintManagementServiceGrpcClient = new BlueprintManagementServiceGrpcClient();

