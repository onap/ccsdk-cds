import { AfterViewInit, Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { AceEditorComponent } from 'ng2-ace-editor';
// import 'brace/ext/searchbox';
// import 'ace-builds/webpack-resolver';
// import 'brace';
// import 'brace/ext/language_tools';
// import 'ace-builds/src-min-noconflict/snippets/html';

@Component({
    selector: 'app-source-editor',
    templateUrl: './source-editor.component.html',
    styleUrls: ['./source-editor.component.css']
})
export class SourceEditorComponent implements OnInit, AfterViewInit {


    @Input() text: string;
    @Output() textChange = new EventEmitter();
    @Input() lang: string;
    @ViewChild('editor', { static: false }) editor: AceEditorComponent;

    ngOnInit(): void {
        //  throw new Error("Method not implemented.");
    }


    ngAfterViewInit() {
        console.log(this.lang);
        this.editor.setTheme('eclipse');
        this.editor.getEditor().setOptions({
            enableBasicAutocompletion: true,
            highlightSelectedWord: true,
            enableSnippets: true,
            enableLiveAutocompletion: true,
            showFoldWidgets: true,
            maxLines: 900000,
            // autoScrollEditorIntoView: true,
            // vScrollBarAlwaysVisible: true
        });

        this.editor.getEditor().commands.addCommand({
            name: 'showOtherCompletions',
            bindKey: 'Ctrl-.',
            exec(editor) {

            }
        });
    }

    onChange(editor) {
        this.textChange.emit(this.text);
    }
}
