import { Parser } from './Parser';

export class JinjaYMLParser implements Parser {
    variables: Set<string> = new Set();
    getVariables(fileContent: string): string[] {
        if (fileContent.includes('{{')) {
            // '[{]+[ ]*.[V-v]alues.' old regex
            const xmlSplit = fileContent.split(new RegExp('[{]+[ ]*.'));
            for (const val of xmlSplit) {
                const res = val.substring(0, val.indexOf('}}'));
                if (res && res.length > 0) {
                    console.log(res);
                    if (res.includes('Value')) {
                        this.variables.add(this.extractValues(res.trim()));
                    } else {
                        this.variables.add(this.extractParent(res.trim()).toLowerCase());
                    }
                }
            }
        }
        return [...this.variables];
    }

    extractValues(value) {
        return value.split('Values.')[1];
    }
    extractParent(value): string {
        return value.split('.')[0];
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
