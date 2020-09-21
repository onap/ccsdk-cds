import { Parser } from './Parser';

export class JinjaXMLParser implements Parser {
    getVariables(fileContent: string): string[] {
        const variables = [];
        if (fileContent.includes('>[')) {
            const xmlSplit = fileContent.split('>[');
            for (const val of xmlSplit) {
                const res = val.substring(0, val.indexOf(']</'));
                if (res && res.length > 0) {
                    variables.push(res);
                }

            }
        }
        return variables;
    }

}

/*

<?xml version="1.0" encoding="UTF-8"?>
<configuration xmlns:junos="http://xml.juniper.net/junos/17.4R1/junos">
<system xmlns="http://yang.juniper.net/junos-qfx/conf/system">
<host-name operation="delete" />
<host-name operation="create">[hostname]</host-name>
</system>
</configuration>

*/
