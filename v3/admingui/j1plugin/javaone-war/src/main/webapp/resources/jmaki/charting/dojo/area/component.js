jmdojo.require("jmdojo.collections.Store");
jmdojo.require("jmdojo.charting.Chart");
jmdojo.require('jmdojo.json');

jmaki.namespace("jmaki.widgets.jmaki.charting.dojo.area");

jmaki.widgets.jmaki.charting.dojo.area.Widget = function(wargs) {
    
    var self = this;                
    var line = true;
    // widget size
    var _height = 250;
    var _width = 700;
 
    // padding
    var _pr = 30;
    var _pl = 50;
    var _pb = 30; 
    
    var autoSizeH = true;
    var autoSizeW = true;
   
    var container = document.getElementById(wargs.uuid);
    
    if (wargs.args) {
        if (wargs.args.line) {
            line = wargs.args.line;
        }
        if (wargs.args.height) {
            _height = wargs.args.height;
        }
        if (wargs.args.width) {
            _widget = wargs.args.width;
        }
        if (wargs.args.paddingRight) {
            _pr = wargs.paddingRight;
        }
        if (wargs.args.paddingLeft) {
            _pr = wargs.paddingLeft;
        }
        if (wargs.args.paddingBottom) {
            _pb = wargs.args.paddingBottom;
        }
    }
    
    if (autoSizeH || autoSizeW) {
        var _tNode = container.parentNode;
        while(_tNode != null &&
        (_tNode.clientHeight == 0 ||
        typeof _tNode.clientWidth == 'undefined')) {
            _tNode = _tNode.parentNode;
        }
        if (_tNode != null) {
            _width = _tNode.clientWidth;
            _height = _tNode.clientHeight;
        }
    }
    
    container.style.height = _height + "px";
    container.style.width = (_width  - _pr - _pl)+ "px";     
    
    // default data set (only if now value was set
      if (wargs.value) {
        self.ds = wargs.value;
        init();
    } else if (wargs.service) {
            jmaki.doAjax({url: wargs.service, callback: function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
              self.ds = eval('(' + req.responseText + ')');
              if (!wargs.value) wargs.value = {};
              wargs.value.xAxis = self.ds.xAxis;
              wargs.value.yAxis = self.ds.yAxis;
              init();
          }
        }
      }});
    } else {
       self.ds = jmaki.charting.defaultData;    
    }
     
    function init() {
        var pa;
        var chart;
        var serises = [];
        // the plot area
        var plot;        
        pa = new jmdojo.charting.PlotArea();
        pa.size={width:_width,height:_height};
        pa.padding={top:20, right:_pr, bottom:_pb, left:_pl };
        
        // compute the range
        var hl = jmaki.charting.Common.calculateHighLow(self.ds.data);
        var high = hl.high;
        var low = 0;
        var xA = jmaki.charting.Common.createXAxis(wargs, self.ds.data[0].values.length, _width, _pl, _pr);
        xA.origin="max";
        
        var yA = jmaki.charting.Common.createYAxis(wargs, high, low);
        
        // scan the data set for highs and lows
        for (var bl = 0; bl < self.ds.data.length; bl++) {
            var _color;
            if (self.ds.data[bl].color) {
                _color = self.ds.data[bl].color;
            } else {
                _color = pa.nextColor();
            }
            var _lbl = "Series " + bl;
            if (self.ds.data[bl].label) {
                _lbl = self.ds.data[bl].label;
            }
            var prev = null;
            if (bl > 0) prev = self.ds.data[bl - 1].values;
            serises.push(jmaki.charting.Common.createSeries(self.ds.data[bl].values, prev, xA.xWidth, '', _color, _lbl));
        }
        
        yA.showdata = true;
        
        // now plot the data
        plot = new jmdojo.charting.Plot(xA, yA);
        plot.renderType = jmdojo.charting.RenderPlotSeries.Grouped;
        
        for (var i = 0; i < serises.length; i++) {
            plot.addSeries({ data:serises[i], plotter: jmdojo.charting.Plotters.StackedCurvedArea });
        }
        pa.plots.push(plot);
        
        chart = new jmdojo.charting.Chart(null, "jMaki chart", "Some description");
        chart.addPlotArea({ x:0,y:0, plotArea:pa });
        chart.node = jmdojo.byId(wargs.uuid);
        chart.render();
        if (wargs.args && wargs.args.legend) {
            jmaki.charting.Common.showLegend(pa, wargs.args.legend);
        }        
    }
    
    this.addSeries = function(d) {
        self.ds.data.push(d);
        init();
    }
    
    // reset the data 
    this.setData = function(_d) {
        serises = [];
        self.ds = _d;
        if (_d.xAxis) wargs.args.xAxis = _d.xAxis;
        if (_d.yAxis) wargs.args.yAxis = _d.yAxis;
        init();
    }
}