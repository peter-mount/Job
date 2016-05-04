var win = {}, winid = 0, body;
var modes = {
    ".job": "ace/mode/javascript",
    ".js": "ace/mode/javascript",
    ".txt": "ace/mode/text",
    ".c": "ace/mode/c",
    ".java": "ace/mode/java",
};
var tree;
$(function () {
    body = $('#body');

    var openWindow = function (p) {
        if (!p.left)
            p.left = '20em';
        if (!p.top)
            p.top = '20em';
        if (!p.width)
            p.width = '30em';
        if (!p.height)
            p.height = '30em';
        if (!p.close)
            p.close = function (p) {
                return true;
            };
        p['winid'] = ++winid;
        p['divid'] = "w" + p.winid;
        p['handleid'] = "h" + p.winid;
        p['paneid'] = "p" + p.winid;
        var c = $('#' + p.divid);
        if (c.length === 0) {
            p.wt = $('<div></div>').addClass("wintitle").attr({'id': p.handleid}).append(p.title);
            p.wp = $('<div></div>').addClass("winpane").attr({'id': p.paneid});
            c = $('<div></div>').attr({'id': p.divid})
                    .addClass("window")
                    .appendTo(body)
                    .append(p.wt)
                    .append(p.wp)
                    .draggable({handle: '.wintitle', containment: '#body'})
                    .resizable({
                        minHeight: 150,
                        minWidth: 150,
                        resize: function (evt, ui) {
                            p.editor.resize();
                        }});

            p['window'] = c;
            win[p.divid] = p;
            console.log(win, p.divid);

            if (p.closable !== false) {
                p.wt.append($('<span class="winclose"></span>').attr({id: 'wc' + p.winid})
                        .addClass("winbut"));
                $(document).on("click", '#wc' + p.winid, function () {
                    console.log(win, p.divid);
                    if (p.close(p)) {
                        $('#' + p.divid).remove();
                        delete win[p.divid];
                        console.log(win, p.divid);
                    }
                });
            }

            setTimeout(function () {
                c.css({
                    position: 'absolute',
                    top: p.top,
                    left: p.left,
                    width: p.width,
                    height: p.height,
                    'z-index': 10 * winid
                });
            }, 100);
        }
        return c;
    };
    var openEditor = function (p) {
        $.ajax({
            url: '/get?path=' + p.id,
            type: 'GET',
            dataType: 'json',
            async: true,
            success: function (data, textStatus, jqXHR) {
                var c = openWindow(p);
                var id = 'ed_' + p.winid;
                $('<div></div>')
                        .attr({id: id, style: "position:absolute;top:25px;left:0;right:0;bottom:0;"})
                        .append(data.content)
                        .appendTo(p.wp);
                p.editor = ace.edit(id);
                p.editor.setTheme("ace/theme/twilight");
                var t = null, i = p.id.lastIndexOf('.');
                if (i > 0) {
                    t = modes[p.id.substring(i)];
                }
                if (!t)
                    t = "ace/mode/text";
                p.editor.session.setMode(t);
                setTimeout(function () {
                    p.editor.resize();
                }, 200);
            }
        });
    };

    var showStats = function () {
        var p = {
            id: "stats",
            width: '20em',
            height: '40em',
            title: 'Statistics'
        };
        openWindow(p);
        var d = $('<div></div>').appendTo(p.wp);
        var stats = {};

        //var ws = new SockJS('http://172.17.4.84:15674/stomp');
        var ws = new WebSocket('ws://172.17.4.84:15674/ws');
        var client = Stomp.over(ws);
        client.connect('peter', 'password', function (frame) {
            console.log("Connected");
            client.subscribe('/topic/statistics', function (m) {
                var msg = JSON.parse(m.body);
                if (msg.type === 'statistic') {
                    msg = msg.value;
                    if (!stats[msg.name]) {
                        stats[msg.name] = $('<div></div>').attr({id: 'stat_' + msg.name.replace('.', '_')}).appendTo(d);
                        d.children("div").sort(function (a, b) {
                            console.log($(a).attr('id'), $(b).attr('id'));
                            return $(a).attr('id') > $(b).attr('id');
                        }).each(function () {
                            var e = $(this);
                            e.remove();
                            e.appendTo(d);
                        });
                    }
                    $(stats[msg.name]).empty()
                            .append(msg.name)
                            .append($('<span></span>').attr({style: 'float:right'}).append(msg.count))
                }
                console.log(msg);
            });
        }, function () {
            console.log("Error");
            d.empty().append("Failed to connect");
        }, '/');
    };

    tree = $('#tree');
    tree.on('changed.jstree', function (e, data) {
        var i, j;
        for (i = 0, j = data.selected.length; i < j; i++) {
            var s = data.instance.get_node(data.selected[i]);
            if (!s.id.endsWith("/")) {
                openEditor({"id": s.id, "title": s.text});
            }
        }
    }).jstree({
        'core': {
            'data': {
                'url': function (node) {
                    return node.id === '#' ? '/listTree?path=/' : ('/listTree?path=' + node.id);
                }
            }
        }
    });

    showStats();
});
