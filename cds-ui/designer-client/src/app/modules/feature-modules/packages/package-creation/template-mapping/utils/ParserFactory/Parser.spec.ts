import { XmlParser } from './XmlParser';

fdescribe('ImportsTabComponent', () => {
    const parser: XmlParser = new XmlParser();


    beforeEach(() => {
    });

    it('Test xml Parser', () => {
        const fileContent = `<vlb-business-vnf-onap-plugin xmlns="urn:opendaylight:params:xml:ns:yang:vlb-business-vnf-onap-plugin">
        <vdns-instances>
            <vdns-instance>
                <ip-addr>$vdns_int_private_ip_0</ip-addr>
                <oam-ip-addr>$vdns_onap_private_ip_0</oam-ip-addr>
                <enabled>false</enabled>
                <tag>dddd</tag>
            </vdns-instance>
        </vdns-instances>
    </vlb-business-vnf-onap-plugin>`;

        const res = parser.getVariables(fileContent);
        console.log(res);
        expect(res.length).toEqual(2);
        expect(res[0]).toEqual('vdns_int_private_ip_0');
        expect(res[1]).toEqual('vdns_onap_private_ip_0');
    });
});
