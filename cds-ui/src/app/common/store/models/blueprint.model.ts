import { MetaData } from './metadata.model';
import { ImportModel } from './imports.model';


export interface Blueprint {
        metadata: MetaData;
        fileImports: Array<ImportModel>
        toplogyTemplates: string;
}