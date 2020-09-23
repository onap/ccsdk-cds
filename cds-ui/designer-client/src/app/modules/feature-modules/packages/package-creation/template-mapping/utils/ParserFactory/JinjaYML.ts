import { Parser } from './Parser';

export class JinjaYMLParser implements Parser {
    variables: Set<string> = new Set();
    getVariables(fileContent: string): string[] {
        if (fileContent.includes('{{')) {
            const xmlSplit = fileContent.split(new RegExp('[{]+[ ]*.[V-v]alues.'));
            for (const val of xmlSplit) {
                const res = val.substring(0, val.indexOf('}}'));
                if (res && res.length > 0) {
                    this.variables.add(res.trim());
                }

            }
        }
        return [...this.variables];
    }

}

/*
vf-module-name: {{ .Values.vpg_name_0 }}
<?xml version="1.0" encoding="UTF-8"?>
<configuration xmlns:junos="http://xml.juniper.net/junos/17.4R1/junos">
<system xmlns="http://yang.juniper.net/junos-qfx/conf/system">
<host-name operation="delete" />
<host-name operation="create">[hostname]</host-name>
</system>
</configuration>

*/
