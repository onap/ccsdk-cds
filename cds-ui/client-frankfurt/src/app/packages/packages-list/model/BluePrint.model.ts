export class BlueprintModel {


    constructor(id: string, artifactUUId: null, artifactType: string,
                artifactVersion: string, artifactDescription: string,
                internalVersion: null, createdDate: string, artifactName: string,
                published: string, updatedBy: string, tags: string) {
        this.id = id;
        this.artifactUUId = artifactUUId;
        this.artifactType = artifactType;
        this.artifactVersion = artifactVersion;
        this.artifactDescription = artifactDescription;
        this.internalVersion = internalVersion;
        this.createdDate = createdDate;
        this.artifactName = artifactName;
        this.published = published;
        this.updatedBy = updatedBy;
        this.tags = tags;
    }

    id: string;
    artifactUUId?: null;
    artifactType: string;
    artifactVersion: string;
    artifactDescription: string;
    internalVersion?: null;
    createdDate: string;
    artifactName: string;
    published: string;
    updatedBy: string;
    tags: string;
}
