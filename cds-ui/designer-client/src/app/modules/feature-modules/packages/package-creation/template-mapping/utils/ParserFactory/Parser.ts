export interface Parser {
    variables: Set<string>;
    getVariables(fileContent: string): string[];
}
