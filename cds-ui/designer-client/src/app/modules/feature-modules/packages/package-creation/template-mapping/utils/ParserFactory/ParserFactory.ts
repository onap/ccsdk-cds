
import { XmlParser } from './XmlParser';
import { Parser } from './Parser';
import { VtlParser } from './VtlParser';
import { FileExtension } from '../TemplateType';
import { JinjaXMLParser } from './JinjaXML';
import { VtlYMLParser } from './VtlYMLParser';
import { JinjaYMLParser } from './JinjaYML';

export class ParserFactory {

    getParser(fileContent: string, fileExtension: string): Parser {
        let parser: Parser;
        console.log('file extension =' + fileExtension);

        if (fileExtension === FileExtension.Velocity) {

            if (this.isXML(fileContent)) {
                parser = new XmlParser();
            } else if (this.isJSON(fileContent)) {
                parser = new VtlParser();
            } else {
                parser = new VtlYMLParser();
            }

        } else if (fileExtension === FileExtension.Jinja) {

            if (this.isXML(fileContent)) {
                parser = new JinjaXMLParser();
            } else if (this.isJSON(fileContent)) {
                // TODO: implement JSON parser
            } else {
                parser = new JinjaYMLParser();
            }

        } else if (fileExtension === FileExtension.XML) {
            parser = new XmlParser();
        }
        return parser;
    }

    private isXML(fileContent: string): boolean {
        return fileContent.includes('<?xml version="1.0" encoding="UTF-8"?>');
    }

    private isJSON(fileContent: string): boolean {
        try {
            JSON.parse(fileContent);
        } catch (e) {
            return false;
        }
        return true;
    }
}
