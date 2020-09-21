import { Parser } from './Parser';

export class VtlParser implements Parser {
    getVariables(fileContent: string): string[] {
        const variables: string[] = [];
        const stringsSlittedByBraces = fileContent.split('${');
        const stringsDefaultByDollarSignOnly = fileContent.split('"$');

        for (let i = 1; i < stringsSlittedByBraces.length; i++) {
            const element = stringsSlittedByBraces[i];
            if (element) {
                const firstElement = element.split('}')[0];
                if (!variables.includes(firstElement)) {
                    variables.push(firstElement);
                } else {
                    console.log(firstElement);
                }
            }
        }

        for (let i = 1; i < stringsDefaultByDollarSignOnly.length; i++) {
            const element = stringsDefaultByDollarSignOnly[i];
            if (element && !element.includes('$')) {
                const firstElement = element.split('"')[0]
                    .replace('{', '')
                    .replace('}', '').trim();
                if (!variables.includes(firstElement)) {
                    variables.push(firstElement);
                }
            }
        }
        return variables;
    }

}

/*

<vlb-business-vnf-onap-plugin xmlns="urn:opendaylight:params:xml:ns:yang:vlb-business-vnf-onap-plugin">
    <vdns-instances>
        <vdns-instance>
            <ip-addr>$vdns_int_private_ip_0</ip-addr>
            <oam-ip-addr>$vdns_onap_private_ip_0</oam-ip-addr>
            <tag>aaaa</tag>
            <enabled>false</enabled>
            <tag>dddd</tag>
        </vdns-instance>
    </vdns-instances>
</vlb-business-vnf-onap-plugin>

*/
