import { Component, OnInit, ViewChild, Input, AfterViewInit } from '@angular/core';
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


  @Input() Data: string;
  @Input() lang: string;
  mode = 'json';
  @ViewChild('editor', { static: false }) editor;
  text = '';

  ngOnInit(): void {
    //  throw new Error("Method not implemented.");
  }



  ngAfterViewInit() {
    this.editor.setTheme('eclipse');

    this.editor.getEditor().setOptions({
      enableBasicAutocompletion: true
    });

    this.editor.getEditor().commands.addCommand({
      name: 'showOtherCompletions',
      bindKey: 'Ctrl-.',
      exec(editor) {

      }
    });
  }

}
