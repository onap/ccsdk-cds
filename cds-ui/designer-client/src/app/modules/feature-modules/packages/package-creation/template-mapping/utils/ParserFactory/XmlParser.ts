import { Parser } from './Parser';

export class XmlParser implements Parser {
    getVariables(fileContent: string): string[] {
        const variables = [];
        const xmlSplit = fileContent.split('$');
        for (const val of xmlSplit) {
            const res = val.substring(0, val.indexOf('</'));
            if (res && res.length > 0) {
                variables.push(res);
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
