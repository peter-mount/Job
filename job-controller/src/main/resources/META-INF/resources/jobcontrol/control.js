var editor, jclEditor;
$(function () {
    //$("#tabs").tabs();

    editor = ace.edit("editor");
    editor.setTheme("ace/theme/twilight");
    editor.session.setMode("ace/mode/javascript");

    jclEditor = ace.edit("jclEditor");
    jclEditor.setTheme("ace/theme/twilight");
    jclEditor.session.setMode("ace/mode/javascript");

});
