import { Parser } from './Parser';

export class ASCIIParser implements Parser {
    variables: Set<string> = new Set();
    getVariables(fileContent: string): string[] {
        if (fileContent.includes('$(')) {
            const xmlSplit = fileContent.split('$(');
            for (const val of xmlSplit) {
                const res = val.substring(0, val.indexOf(')'));
                if (res && res.length > 0) {
                    this.variables.add(res);
                }

            }
        }
        return [...this.variables];
    }

}
