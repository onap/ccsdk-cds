export interface Parser {
    getVariables(fileContent: string): string[];
}
