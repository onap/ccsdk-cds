
import { XmlParser } from './XmlParser';
import { Parser } from './Parser';
import { VtlParser } from './VtlParser';
import { FileExtension } from '../TemplateType';
import { JinjaXMLParser } from './JinjaXML';

export class ParserFactory {

    getParser(fileContent: string, fileExtension: string): Parser {
        let parser: Parser;
        console.log('file extension =' + fileExtension);
        if (fileExtension === FileExtension.Velocity) {
            if (this.isXML(fileContent)) {
                parser = new XmlParser();
            } else {
                parser = new VtlParser();
            }
        } else if (fileExtension === FileExtension.Jinja) {
            if (this.isXML(fileContent)) {
                parser = new JinjaXMLParser();
            }
        } else if (fileExtension === FileExtension.XML) {
            parser = new XmlParser();
        }
        return parser;
    }

    private isXML(fileContent: string): boolean {
        return fileContent.includes('<?xml version="1.0" encoding="UTF-8"?>');
    }
}
