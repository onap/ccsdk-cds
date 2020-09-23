import { XmlParser } from './XmlParser';
import { ParserFactory } from './ParserFactory';
import { FileExtension } from '../TemplateType';
import { JinjaXMLParser } from './JinjaXML';

fdescribe('ImportsTabComponent', () => {

    const parserFactory = new ParserFactory();


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

        const parser = parserFactory.getParser(fileContent, FileExtension.XML);
        const res = parser.getVariables(fileContent);
        console.log(res);
        expect(res.length).toEqual(2);
        expect(res[0]).toEqual('vdns_int_private_ip_0');
        expect(res[1]).toEqual('vdns_onap_private_ip_0');
    });

    it('Test J2 XML Parser', () => {
        const fileContent = `<?xml version="1.0" encoding="UTF-8"?>
        <configuration xmlns:junos="http://xml.juniper.net/junos/17.4R1/junos">
        <system xmlns="http://yang.juniper.net/junos-qfx/conf/system">
        <host-name operation="delete" />
        <host-name operation="create">[hostname]</host-name>
        </system>
        </configuration>`;

        const parser = parserFactory.getParser(fileContent, FileExtension.Jinja);
        const res = parser.getVariables(fileContent);
        console.log(typeof (res));
        console.log(res);
        expect(res.length).toEqual(1);
        expect(res[0]).toEqual('hostname');

    });

    it('Test J2 YML Parser', () => {
        const fileContent = `apiVersion: v1
        kind: Service
        metadata:
        name: {{ .Values.vpg_name_0 }}-ssh
        labels:
        vnf-name: {{ .Values.vnf_name }}
        vf-module-name: {{ .Values.vpg_name_0 }}
        release: {{ .Release.Name }}
        chart: {{ .Chart.Name }}
        spec:
        type: NodePort
        ports:
        port: 22
        nodePort: \${vpg-management-port}
        selector:
        vf-module-name: {{ .Values.vpg_name_0 }}
        release: {{ .Release.Name }}
        chart: {{ .Chart.Name }}`;

        const parser = parserFactory.getParser(fileContent, FileExtension.Jinja);
        const res = parser.getVariables(fileContent);
        console.log(res);
        expect(res.length).toEqual(2);
        expect(res[0]).toEqual('vpg_name_0');
        expect(res[1]).toEqual('vnf_name');

    });
});
